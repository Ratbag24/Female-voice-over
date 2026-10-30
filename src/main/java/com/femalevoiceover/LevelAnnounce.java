package com.femalevoiceover;

/** How often a level up is worth a voice line. */
public enum LevelAnnounce
{
	EVERY("Every level"),
	MILESTONES("Every 10th level, and 99"),
	MAX_ONLY("Only 99");

	private final String label;

	LevelAnnounce(String label)
	{
		this.label = label;
	}

	@Override
	public String toString()
	{
		return label;
	}
}
