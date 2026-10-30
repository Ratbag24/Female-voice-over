package com.femalevoiceover;

/**
 * Maps a line of game chat to the event it announces.
 *
 * <p>Deliberately free of RuneLite types so the matching — the part most likely to be wrong — can
 * be unit tested against real message text.
 */
public final class ChatTriggers
{
	private ChatTriggers()
	{
	}

	/**
	 * The event this message announces, or null if it announces nothing.
	 *
	 * <p>Order matters below: a combat achievement and a slayer task both talk about "tasks", and a
	 * personal best arrives alongside a kill count, so the narrow phrasings are tested first.
	 */
	public static VoiceEvent match(String raw)
	{
		String message = normalise(raw);
		if (message.isEmpty())
		{
			return null;
		}

		if (message.contains("personal best"))
		{
			return VoiceEvent.PERSONAL_BEST;
		}

		// "You have a funny feeling like you're being followed", and the full-inventory variant.
		if (message.contains("funny feeling like") || message.contains("sneaking into your backpack"))
		{
			return VoiceEvent.PET;
		}

		if (message.contains("new item added to your collection log"))
		{
			return VoiceEvent.COLLECTION_LOG;
		}

		// "Congratulations, you've completed a hard combat task: ..."
		if (message.contains("combat task"))
		{
			return VoiceEvent.COMBAT_TASK;
		}

		// "You have completed 12 medium Treasure Trails."
		if (message.contains("treasure trails") && message.contains("complet"))
		{
			return VoiceEvent.CLUE;
		}

		if (isDiary(message))
		{
			return VoiceEvent.DIARY;
		}

		if (isSlayerTask(message))
		{
			return VoiceEvent.SLAYER;
		}

		if (message.contains("valuable drop"))
		{
			return VoiceEvent.VALUABLE_DROP;
		}

		if (message.contains("oh dear, you are dead"))
		{
			return VoiceEvent.DEATH;
		}

		return null;
	}

	/** "Well done! You have completed all of the easy tasks in the Varrock area." */
	private static boolean isDiary(String message)
	{
		if (message.contains("achievement diary") && message.contains("complet"))
		{
			return true;
		}

		return message.contains("completed all of the") && message.contains("task");
	}

	/** "You have completed your task!" and "You've completed 95 tasks; return to a Slayer master." */
	private static boolean isSlayerTask(String message)
	{
		if (message.contains("completed your task"))
		{
			return true;
		}

		if (message.contains("slayer task") && message.contains("complet"))
		{
			return true;
		}

		return message.contains("complet") && message.contains(" tasks");
	}

	/**
	 * Lower-cases the message and drops the markup the client wraps around coloured text, so a
	 * recolouring of a message by Jagex or another plugin does not stop it matching.
	 */
	static String normalise(String raw)
	{
		if (raw == null)
		{
			return "";
		}

		return raw.replaceAll("<[^>]*>", " ")
			.replace(' ', ' ')
			.replaceAll("\\s+", " ")
			.trim()
			.toLowerCase();
	}
}
