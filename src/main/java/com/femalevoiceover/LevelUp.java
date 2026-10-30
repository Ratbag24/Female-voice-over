package com.femalevoiceover;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reads the game's level up message.
 *
 * <p>The message is used rather than {@code StatChanged} because the client replays every skill on
 * login, and telling a replay apart from a real level up means guessing. The message only arrives
 * when the level actually goes up.
 */
public final class LevelUp
{
	/** "Congratulations, you've just advanced your Attack level." */
	private static final Pattern SKILL = Pattern.compile("advanced your ([a-z]+) level");

	/** "You are now level 70." — absent from some phrasings, so it is optional. */
	private static final Pattern LEVEL = Pattern.compile("you are now level (\\d+)");

	private LevelUp()
	{
	}

	/** The skill named by a level up message, lower-cased, or null if it is not one. */
	public static String skill(String raw)
	{
		Matcher matcher = SKILL.matcher(ChatTriggers.normalise(raw));
		return matcher.find() ? matcher.group(1) : null;
	}

	/** The level the message reports, or -1 if it does not carry one. */
	public static int level(String raw)
	{
		Matcher matcher = LEVEL.matcher(ChatTriggers.normalise(raw));
		if (!matcher.find())
		{
			return -1;
		}

		try
		{
			return Integer.parseInt(matcher.group(1));
		}
		catch (NumberFormatException e)
		{
			return -1;
		}
	}

	/** Whether a level is one the player asked to hear about. */
	public static boolean worthAnnouncing(int level, LevelAnnounce when)
	{
		switch (when)
		{
			case MAX_ONLY:
				return level == 99;
			case MILESTONES:
				return level == 99 || (level > 0 && level % 10 == 0);
			case EVERY:
			default:
				return level > 1;
		}
	}
}
