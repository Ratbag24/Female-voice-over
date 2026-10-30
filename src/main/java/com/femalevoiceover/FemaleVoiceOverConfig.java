package com.femalevoiceover;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;

@ConfigGroup(FemaleVoiceOverConfig.GROUP)
public interface FemaleVoiceOverConfig extends Config
{
	String GROUP = "femalevoiceover";

	@ConfigSection(
		name = "Events",
		description = "Which moments get a voice line.",
		position = 100
	)
	String events = "events";

	@ConfigItem(
		keyName = "volume",
		name = "Volume",
		description = "How loud the voice lines are, as a percentage of how they were recorded.",
		position = 1
	)
	@Range(min = 0, max = 100)
	default int volume()
	{
		return 70;
	}

	@ConfigItem(
		keyName = "interrupt",
		name = "Interrupt the current line",
		description = "When two things happen at once: cut the first line off, or let it finish and"
			+ " drop the second.",
		position = 2
	)
	default boolean interrupt()
	{
		return false;
	}

	@ConfigItem(
		keyName = "levelAnnounce",
		name = "Announce levels",
		description = "Levelling anything from 1 to 99 is a lot of talking. This thins it out.",
		position = 3
	)
	default LevelAnnounce levelAnnounce()
	{
		return LevelAnnounce.MILESTONES;
	}

	@ConfigItem(
		keyName = "death",
		name = "Death",
		description = "When you die.",
		section = events,
		position = 1
	)
	default boolean death()
	{
		return true;
	}

	@ConfigItem(
		keyName = "quest",
		name = "Quest complete",
		description = "When the quest completion scroll appears.",
		section = events,
		position = 2
	)
	default boolean quest()
	{
		return true;
	}

	@ConfigItem(
		keyName = "levelUp",
		name = "Level up",
		description = "When a skill levels. Thinned out by the Announce levels setting above.",
		section = events,
		position = 3
	)
	default boolean levelUp()
	{
		return true;
	}

	@ConfigItem(
		keyName = "diary",
		name = "Achievement diary",
		description = "When you finish a set of diary tasks.",
		section = events,
		position = 4
	)
	default boolean diary()
	{
		return true;
	}

	@ConfigItem(
		keyName = "combatTask",
		name = "Combat achievement",
		description = "When you complete a combat task.",
		section = events,
		position = 5
	)
	default boolean combatTask()
	{
		return true;
	}

	@ConfigItem(
		keyName = "collectionLog",
		name = "Collection log slot",
		description = "When a new item is added to your collection log.",
		section = events,
		position = 6
	)
	default boolean collectionLog()
	{
		return true;
	}

	@ConfigItem(
		keyName = "pet",
		name = "Pet drop",
		description = "When you get the funny feeling.",
		section = events,
		position = 7
	)
	default boolean pet()
	{
		return true;
	}

	@ConfigItem(
		keyName = "clue",
		name = "Clue scroll",
		description = "When you finish a Treasure Trail.",
		section = events,
		position = 8
	)
	default boolean clue()
	{
		return true;
	}

	@ConfigItem(
		keyName = "slayer",
		name = "Slayer task",
		description = "When you finish a slayer task.",
		section = events,
		position = 9
	)
	default boolean slayer()
	{
		return true;
	}

	@ConfigItem(
		keyName = "personalBest",
		name = "Personal best",
		description = "When you set a new personal best time.",
		section = events,
		position = 10
	)
	default boolean personalBest()
	{
		return true;
	}

	@ConfigItem(
		keyName = "valuableDrop",
		name = "Valuable drop",
		description = "When a valuable drop is announced. Off by default: it fires a lot.",
		section = events,
		position = 11
	)
	default boolean valuableDrop()
	{
		return false;
	}

	@ConfigItem(
		keyName = "login",
		name = "Login greeting",
		description = "A line when you log in.",
		section = events,
		position = 12
	)
	default boolean login()
	{
		return false;
	}
}
