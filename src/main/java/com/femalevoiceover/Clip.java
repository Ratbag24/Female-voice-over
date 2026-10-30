package com.femalevoiceover;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * One recording: either a file the player dropped into their audio folder, or one shipped inside
 * the jar. Holding both behind a single type keeps {@link ClipLibrary} from caring which is which.
 */
public final class Clip
{
	private final String name;
	private final File file;
	private final String resource;

	private Clip(String name, File file, String resource)
	{
		this.name = name;
		this.file = file;
		this.resource = resource;
	}

	/** A clip read from the player's own audio folder. */
	public static Clip of(File file)
	{
		return new Clip(file.getName(), file, null);
	}

	/** A clip read from the jar, named by its classpath path. */
	public static Clip bundled(String resource)
	{
		int slash = resource.lastIndexOf('/');
		return new Clip(slash < 0 ? resource : resource.substring(slash + 1), null, resource);
	}

	public String name()
	{
		return name;
	}

	public boolean isBundled()
	{
		return resource != null;
	}

	/** Where this clip came from, for the in-game listing. */
	public String describe()
	{
		return isBundled() ? name + " (bundled)" : name;
	}

	public InputStream open() throws IOException
	{
		if (file != null)
		{
			return new FileInputStream(file);
		}

		InputStream stream = Clip.class.getResourceAsStream(resource);
		if (stream == null)
		{
			throw new IOException("Bundled clip is missing from the jar: " + resource);
		}

		return stream;
	}

	@Override
	public String toString()
	{
		return describe();
	}
}
