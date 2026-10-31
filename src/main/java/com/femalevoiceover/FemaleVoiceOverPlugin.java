package com.femalevoiceover;

import com.google.inject.Provides;
import java.io.File;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Skill;
import net.runelite.api.events.ActorDeath;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.CommandExecuted;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.client.RuneLite;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@Slf4j
@PluginDescriptor(
	name = "Female Voice Over",
	description = "A voiced announcement when you die, finish a quest, level up and more",
	tags = {"voice", "voiceover", "audio", "sound", "announce", "death", "quest", "level", "immersion"}
)
public class FemaleVoiceOverPlugin extends Plugin
{
	/** Folder under .runelite that recordings are read from. */
	private static final String AUDIO_FOLDER = "female-voiceover";

	/**
	 * The quest completion scroll. A backstop only: quests are detected from the chat message,
	 * which is what C Engineer Completed uses and does not depend on an interface id staying
	 * put. Both firing is harmless, since one line per event per {@link #DEBOUNCE_MS} wins.
	 */
	private static final int QUEST_COMPLETED_GROUP_ID = 153;

	/**
	 * Two detectors can report the same moment — a death arrives as both an actor death and a chat
	 * line — and the collection log fires per item on a multi-drop. One line per event per window.
	 */
	private static final long DEBOUNCE_MS = 3000L;

	private static final String COMMAND = "vo";

	@Inject
	private Client client;

	@Inject
	private FemaleVoiceOverConfig config;

	@Inject
	private ChatMessageManager chatMessageManager;

	@Inject
	private ScheduledExecutorService executor;

	private final Map<VoiceEvent, Long> lastSpoken = new EnumMap<>(VoiceEvent.class);

	private ClipLibrary library;
	private SoundPlayer player;
	private boolean greeted;

	@Provides
	FemaleVoiceOverConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(FemaleVoiceOverConfig.class);
	}

	@Override
	protected void startUp()
	{
		player = new SoundPlayer();
		library = new ClipLibrary(new File(RuneLite.RUNELITE_DIR, AUDIO_FOLDER));

		try
		{
			library.createFolders();
		}
		catch (IOException e)
		{
			log.warn("Could not prepare the audio folder {}", library.root(), e);
		}

		library.reload();
		lastSpoken.clear();
		greeted = false;
	}

	@Override
	protected void shutDown()
	{
		if (player != null)
		{
			player.close();
			player = null;
		}

		library = null;
		lastSpoken.clear();
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOGIN_SCREEN)
		{
			// Reset here rather than on HOPPING, so a world hop does not greet you again.
			greeted = false;
		}
		else if (event.getGameState() == GameState.LOGGED_IN && !greeted)
		{
			greeted = true;
			speak(VoiceEvent.LOGIN);
		}
	}

	@Subscribe
	public void onActorDeath(ActorDeath event)
	{
		if (event.getActor() == client.getLocalPlayer())
		{
			speak(VoiceEvent.DEATH);
		}
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded event)
	{
		if (event.getGroupId() == QUEST_COMPLETED_GROUP_ID)
		{
			speak(VoiceEvent.QUEST);
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		if (!isGameChat(event.getType()))
		{
			return;
		}

		String message = event.getMessage();

		String skill = LevelUp.skill(message);
		if (skill != null)
		{
			onLevelUp(skill, message);
			return;
		}

		VoiceEvent voiceEvent = ChatTriggers.match(message);
		if (voiceEvent != null)
		{
			speak(voiceEvent);
		}
	}

	@Subscribe
	public void onCommandExecuted(CommandExecuted event)
	{
		if (!COMMAND.equalsIgnoreCase(event.getCommand()) || library == null || player == null)
		{
			return;
		}

		String[] args = event.getArguments();
		String action = args.length > 0 ? args[0].toLowerCase() : "";

		switch (action)
		{
			case "":
				reportLoaded();
				break;
			case "reload":
				library.reload();
				say("Reloaded " + library.total() + " clip(s) from " + library.root());
				break;
			case "folder":
				say("Audio folder: " + library.root());
				break;
			case "test":
				test(args.length > 1 ? args[1] : null);
				break;
			default:
				say("Usage: ::vo | ::vo reload | ::vo folder | ::vo test <event>");
				break;
		}
	}

	/**
	 * Announces a level up. The message does not always carry the number, so the client is asked for
	 * the skill's level instead; without one, only the "every level" setting can be honoured.
	 */
	private void onLevelUp(String skillName, String message)
	{
		int level = LevelUp.level(message);
		if (level < 0)
		{
			level = realLevel(skillName);
		}

		if (level < 0)
		{
			if (config.levelAnnounce() == LevelAnnounce.EVERY)
			{
				speak(VoiceEvent.LEVEL_UP);
			}

			return;
		}

		if (!LevelUp.worthAnnouncing(level, config.levelAnnounce()))
		{
			return;
		}

		// A 99 gets its own lines when any are installed, and the ordinary ones otherwise.
		if (level == 99 && !library.clips(VoiceEvent.LEVEL_99).isEmpty())
		{
			speak(VoiceEvent.LEVEL_99);
		}
		else
		{
			speak(VoiceEvent.LEVEL_UP);
		}
	}

	private int realLevel(String skillName)
	{
		try
		{
			return client.getRealSkillLevel(Skill.valueOf(skillName.toUpperCase()));
		}
		catch (IllegalArgumentException e)
		{
			// A skill this client build does not know, or a message that only looked like a level up.
			return -1;
		}
	}

	private void speak(VoiceEvent event)
	{
		if (library == null || player == null || !enabled(event))
		{
			return;
		}

		long now = System.currentTimeMillis();
		Long previous = lastSpoken.get(event);
		if (previous != null && now - previous < DEBOUNCE_MS)
		{
			return;
		}

		Clip clip = library.pick(event);
		if (clip == null)
		{
			return;
		}

		lastSpoken.put(event, now);

		SoundPlayer target = player;
		int volume = config.volume();
		boolean interrupt = config.interrupt();

		// Off the client thread: opening a line and decoding a wav both block.
		executor.execute(() ->
		{
			try
			{
				target.play(clip, volume, interrupt);
			}
			catch (ClipException e)
			{
				log.warn("Could not play {}: {}", clip.name(), e.getMessage(), e);
			}
		});
	}

	private boolean enabled(VoiceEvent event)
	{
		switch (event)
		{
			case DEATH:
				return config.death();
			case QUEST:
				return config.quest();
			case LEVEL_UP:
			case LEVEL_99:
				return config.levelUp();
			case DIARY:
				return config.diary();
			case COMBAT_TASK:
				return config.combatTask();
			case COLLECTION_LOG:
				return config.collectionLog();
			case PET:
				return config.pet();
			case CLUE:
				return config.clue();
			case SLAYER:
				return config.slayer();
			case PERSONAL_BEST:
				return config.personalBest();
			case VALUABLE_DROP:
				return config.valuableDrop();
			case LOGIN:
				return config.login();
			default:
				return false;
		}
	}

	private void test(String folder)
	{
		VoiceEvent event = VoiceEvent.byFolder(folder);
		if (event == null)
		{
			say("Unknown event. Try one of: " + folders());
			return;
		}

		Clip clip = library.pick(event);
		if (clip == null)
		{
			say("No clips installed for " + event.displayName() + ". Put a .wav in "
				+ new File(library.root(), event.folder()));
			return;
		}

		// Bypasses the enabled check and the debounce on purpose: this is how you audition a take.
		SoundPlayer target = player;
		int volume = config.volume();
		say("Playing " + clip.describe() + " for " + event.displayName());
		executor.execute(() ->
		{
			try
			{
				target.play(clip, volume, true);
			}
			catch (ClipException e)
			{
				log.warn("Could not play {}: {}", clip.name(), e.getMessage(), e);
			}
		});
	}

	private void reportLoaded()
	{
		say("Female Voice Over: " + library.total() + " clip(s) in " + library.root());
		for (VoiceEvent event : VoiceEvent.values())
		{
			int count = library.clips(event).size();
			if (count > 0)
			{
				say("  " + event.displayName() + ": " + count);
			}
		}

		if (library.total() == 0)
		{
			say("  Nothing loaded yet. Drop .wav files into the folders and run ::vo reload");
		}
	}

	private static String folders()
	{
		StringBuilder builder = new StringBuilder();
		for (VoiceEvent event : VoiceEvent.values())
		{
			if (builder.length() > 0)
			{
				builder.append(", ");
			}

			builder.append(event.folder());
		}

		return builder.toString();
	}

	private void say(String text)
	{
		String formatted = new ChatMessageBuilder()
			.append(ChatColorType.HIGHLIGHT)
			.append(text)
			.build();

		chatMessageManager.queue(QueuedMessage.builder()
			.type(ChatMessageType.CONSOLE)
			.runeLiteFormattedMessage(formatted)
			.build());
	}

	private static boolean isGameChat(ChatMessageType type)
	{
		return type == ChatMessageType.GAMEMESSAGE
			|| type == ChatMessageType.SPAM
			|| type == ChatMessageType.MESBOX;
	}
}
