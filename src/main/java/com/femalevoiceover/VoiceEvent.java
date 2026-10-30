package com.femalevoiceover;

/**
 * Everything the plugin can speak over, and the folder its recordings live in.
 *
 * <p>The folder name is part of the plugin's contract with whoever records the lines: renaming one
 * orphans the clips already on disk, so add new events rather than re-spelling old ones.
 */
public enum VoiceEvent
{
	DEATH("death", "Death"),
	QUEST("quest", "Quest complete"),
	LEVEL_UP("level-up", "Level up"),
	LEVEL_99("level-99", "Level 99"),
	DIARY("diary", "Achievement diary"),
	COMBAT_TASK("combat-task", "Combat achievement"),
	COLLECTION_LOG("collection-log", "Collection log slot"),
	PET("pet", "Pet drop"),
	CLUE("clue", "Clue scroll"),
	SLAYER("slayer", "Slayer task"),
	PERSONAL_BEST("personal-best", "Personal best"),
	VALUABLE_DROP("valuable-drop", "Valuable drop"),
	LOGIN("login", "Login greeting");

	private final String folder;
	private final String displayName;

	VoiceEvent(String folder, String displayName)
	{
		this.folder = folder;
		this.displayName = displayName;
	}

	/** Directory the clips for this event are read from, under the plugin's audio folder. */
	public String folder()
	{
		return folder;
	}

	public String displayName()
	{
		return displayName;
	}

	/** The event a folder name belongs to, or null if no event claims it. */
	public static VoiceEvent byFolder(String folder)
	{
		if (folder == null)
		{
			return null;
		}

		String wanted = folder.trim().toLowerCase();
		for (VoiceEvent event : values())
		{
			if (event.folder.equals(wanted))
			{
				return event;
			}
		}

		return null;
	}
}
