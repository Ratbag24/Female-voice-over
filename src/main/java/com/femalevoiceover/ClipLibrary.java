package com.femalevoiceover;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

/**
 * The recordings available for each event.
 *
 * <p>Clips in the player's own audio folder win outright for the events they cover: dropping one
 * {@code death} clip in replaces the bundled death lines without touching any other event. That is
 * what lets a voice actor re-record a line and hear it on the next death with no rebuild.
 */
public class ClipLibrary
{
	/** Formats {@code javax.sound.sampled} opens without an extra codec on the classpath. */
	private static final List<String> EXTENSIONS = Arrays.asList(".wav", ".au", ".aif", ".aiff");

	private static final String MANIFEST = "/com/femalevoiceover/sounds/index.txt";

	private final File root;
	private final Random random;
	private final Map<VoiceEvent, List<Clip>> clips = new EnumMap<>(VoiceEvent.class);
	private final Map<VoiceEvent, String> lastPlayed = new EnumMap<>(VoiceEvent.class);

	public ClipLibrary(File root)
	{
		this(root, new Random());
	}

	ClipLibrary(File root, Random random)
	{
		this.root = root;
		this.random = random;
	}

	/** Re-reads the audio folder. Cheap enough to call on startup and from the reload command. */
	public void reload()
	{
		Map<VoiceEvent, List<Clip>> bundled = readManifest();

		clips.clear();
		for (VoiceEvent event : VoiceEvent.values())
		{
			List<Clip> local = readLocal(event);
			List<Clip> chosen = local.isEmpty()
				? bundled.getOrDefault(event, Collections.emptyList())
				: local;

			clips.put(event, Collections.unmodifiableList(chosen));
		}

		// A clip that no longer exists must not keep blocking the no-repeat pick.
		lastPlayed.keySet().removeIf(event -> clips.get(event).isEmpty());
	}

	public List<Clip> clips(VoiceEvent event)
	{
		return clips.getOrDefault(event, Collections.emptyList());
	}

	/**
	 * A clip for this event, or null if none is installed.
	 *
	 * <p>With more than one clip the previous pick is excluded, so repeated deaths cycle through the
	 * lines instead of landing on the same one twice in a row.
	 */
	public Clip pick(VoiceEvent event)
	{
		List<Clip> available = clips(event);
		if (available.isEmpty())
		{
			return null;
		}

		List<Clip> pool = available;
		String previous = lastPlayed.get(event);
		if (previous != null && available.size() > 1)
		{
			pool = new ArrayList<>(available);
			pool.removeIf(clip -> clip.name().equals(previous));
			if (pool.isEmpty())
			{
				pool = available;
			}
		}

		Clip picked = pool.get(random.nextInt(pool.size()));
		lastPlayed.put(event, picked.name());
		return picked;
	}

	public int total()
	{
		int total = 0;
		for (VoiceEvent event : VoiceEvent.values())
		{
			total += clips(event).size();
		}

		return total;
	}

	public File root()
	{
		return root;
	}

	/**
	 * Creates the audio folder, one directory per event, and a note explaining what goes in them.
	 * Called on startup so the folder is waiting rather than something to be described in a README.
	 */
	public void createFolders() throws IOException
	{
		for (VoiceEvent event : VoiceEvent.values())
		{
			File dir = new File(root, event.folder());
			if (!dir.isDirectory() && !dir.mkdirs())
			{
				throw new IOException("Could not create the audio folder " + dir);
			}
		}

		File readme = new File(root, "README.txt");
		if (!readme.exists())
		{
			writeReadme(readme);
		}
	}

	private List<Clip> readLocal(VoiceEvent event)
	{
		List<Clip> found = new ArrayList<>();

		// A single clip can sit beside the folders as "death.wav" instead of in "death/".
		for (String extension : EXTENSIONS)
		{
			File flat = new File(root, event.folder() + extension);
			if (flat.isFile())
			{
				found.add(Clip.of(flat));
			}
		}

		File[] files = new File(root, event.folder()).listFiles();
		if (files != null)
		{
			List<File> audio = new ArrayList<>();
			for (File file : files)
			{
				if (file.isFile() && isAudio(file.getName()))
				{
					audio.add(file);
				}
			}

			audio.sort((a, b) -> a.getName().compareToIgnoreCase(b.getName()));
			for (File file : audio)
			{
				found.add(Clip.of(file));
			}
		}

		return found;
	}

	/**
	 * Reads the list of clips shipped in the jar. A jar cannot be listed like a directory, so the
	 * build keeps an index of the bundled paths and this reads that.
	 */
	private Map<VoiceEvent, List<Clip>> readManifest()
	{
		Map<VoiceEvent, List<Clip>> bundled = new EnumMap<>(VoiceEvent.class);

		try (InputStream stream = openManifest())
		{
			if (stream == null)
			{
				return bundled;
			}

			BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
			String line;
			while ((line = reader.readLine()) != null)
			{
				String path = line.trim();
				if (path.isEmpty() || path.startsWith("#"))
				{
					continue;
				}

				int slash = path.indexOf('/');
				if (slash <= 0 || !isAudio(path))
				{
					continue;
				}

				VoiceEvent event = VoiceEvent.byFolder(path.substring(0, slash));
				if (event != null)
				{
					bundled.computeIfAbsent(event, key -> new ArrayList<>())
						.add(Clip.bundled("/com/femalevoiceover/sounds/" + path));
				}
			}
		}
		catch (IOException e)
		{
			// A missing or unreadable index only means nothing ships with the plugin yet.
			return bundled;
		}

		return bundled;
	}

	/** Seam for the tests, which have no jar to read. */
	InputStream openManifest()
	{
		return ClipLibrary.class.getResourceAsStream(MANIFEST);
	}

	private static boolean isAudio(String name)
	{
		String lower = name.toLowerCase(Locale.ROOT);
		for (String extension : EXTENSIONS)
		{
			if (lower.endsWith(extension))
			{
				return true;
			}
		}

		return false;
	}

	private void writeReadme(File readme) throws IOException
	{
		try (PrintWriter out = new PrintWriter(new FileWriter(readme)))
		{
			out.println("Female Voice Over - audio folder");
			out.println();
			out.println("Drop recordings into the folder for the event they belong to. Any number of");
			out.println("clips per folder is fine: one is picked at random each time, and the same clip");
			out.println("is never played twice in a row.");
			out.println();
			out.println("Format: .wav, 44100 Hz, 16-bit PCM. Mono is fine. Keep lines under ~4 seconds.");
			out.println("Filenames do not matter, only the folder. Delete a clip to stop it playing.");
			out.println();
			out.println("Anything you put here replaces the clips shipped with the plugin for that event.");
			out.println();
			out.println("Type ::vo in game to see what the plugin has loaded, and ::vo reload after");
			out.println("adding files. ::vo test death plays a death line.");
			out.println();
			out.println("Folders:");
			for (VoiceEvent event : VoiceEvent.values())
			{
				out.println("  " + event.folder() + " - " + event.displayName());
			}
		}
	}
}
