package com.femalevoiceover;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Random;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Tests the parts with no RuneLite dependency: message matching, level up parsing, clip selection
 * and the volume curve. The message strings below are the ones the game actually sends.
 */
public class CoreTest
{
	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	// -------------------------------------------------------------- chat triggers

	@Test
	public void matchesDeath()
	{
		assertEquals(VoiceEvent.DEATH, ChatTriggers.match("Oh dear, you are dead!"));
	}

	@Test
	public void matchesPetBothWays()
	{
		assertEquals(VoiceEvent.PET,
			ChatTriggers.match("You have a funny feeling like you're being followed."));
		assertEquals(VoiceEvent.PET,
			ChatTriggers.match("You feel something weird sneaking into your backpack."));
	}

	@Test
	public void matchesCollectionLog()
	{
		assertEquals(VoiceEvent.COLLECTION_LOG,
			ChatTriggers.match("New item added to your collection log: Abyssal whip"));
	}

	@Test
	public void matchesCombatAchievementRatherThanSlayer()
	{
		assertEquals(VoiceEvent.COMBAT_TASK, ChatTriggers.match(
			"Congratulations, you've completed a hard combat task: Kill the Kalphite Queen."));
	}

	@Test
	public void matchesClueRatherThanSlayer()
	{
		assertEquals(VoiceEvent.CLUE,
			ChatTriggers.match("You have completed 12 medium Treasure Trails."));
	}

	@Test
	public void matchesDiaryRatherThanSlayer()
	{
		assertEquals(VoiceEvent.DIARY, ChatTriggers.match(
			"Well done! You have completed all of the easy tasks in the Varrock area."));
	}

	@Test
	public void matchesSlayerTask()
	{
		assertEquals(VoiceEvent.SLAYER, ChatTriggers.match("You have completed your task!"));
		assertEquals(VoiceEvent.SLAYER,
			ChatTriggers.match("You've completed 95 tasks; return to a Slayer master."));
	}

	@Test
	public void matchesPersonalBestBeforeKillCount()
	{
		assertEquals(VoiceEvent.PERSONAL_BEST,
			ChatTriggers.match("Fight duration: 1:23. Personal best: 1:20."));
	}

	@Test
	public void matchesValuableDropThroughColourTags()
	{
		assertEquals(VoiceEvent.VALUABLE_DROP, ChatTriggers.match(
			"<col=ef1020>Valuable drop: Dragon bones (2,900 coins)</col>"));
	}

	@Test
	public void ignoresOrdinaryChatter()
	{
		assertNull(ChatTriggers.match("You slice into the Rat, but it blocks."));
		assertNull(ChatTriggers.match("Your Attack level is 70."));
		assertNull(ChatTriggers.match(""));
		assertNull(ChatTriggers.match(null));
	}

	@Test
	public void normaliseStripsMarkupAndCase()
	{
		assertEquals("valuable drop: dragon bones",
			ChatTriggers.normalise("<col=ef1020>Valuable  drop:</col><br>Dragon bones"));
	}

	// ------------------------------------------------------------------- level up

	@Test
	public void readsSkillFromLevelUpMessage()
	{
		assertEquals("attack",
			LevelUp.skill("Congratulations, you've just advanced your Attack level."));
		assertEquals("runecraft",
			LevelUp.skill("Congratulations, you've just advanced your Runecraft level."));
	}

	@Test
	public void ignoresMessagesThatAreNotLevelUps()
	{
		assertNull(LevelUp.skill("Oh dear, you are dead!"));
		assertNull(LevelUp.skill(null));
	}

	@Test
	public void readsLevelWhenTheMessageCarriesOne()
	{
		assertEquals(70, LevelUp.level(
			"Congratulations, you've just advanced your Attack level. You are now level 70."));
		assertEquals(-1, LevelUp.level("Congratulations, you've just advanced your Attack level."));
	}

	@Test
	public void levelUpMessagesDoNotTriggerOtherEvents()
	{
		assertNull(ChatTriggers.match("Congratulations, you've just advanced your Slayer level."));
	}

	@Test
	public void milestonesAreEveryTenthAndNinetyNine()
	{
		assertTrue(LevelUp.worthAnnouncing(30, LevelAnnounce.MILESTONES));
		assertTrue(LevelUp.worthAnnouncing(99, LevelAnnounce.MILESTONES));
		assertFalse(LevelUp.worthAnnouncing(31, LevelAnnounce.MILESTONES));
	}

	@Test
	public void maxOnlyIsNinetyNineAlone()
	{
		assertTrue(LevelUp.worthAnnouncing(99, LevelAnnounce.MAX_ONLY));
		assertFalse(LevelUp.worthAnnouncing(90, LevelAnnounce.MAX_ONLY));
	}

	@Test
	public void everyLevelCountsFromTwo()
	{
		assertTrue(LevelUp.worthAnnouncing(2, LevelAnnounce.EVERY));
		assertFalse(LevelUp.worthAnnouncing(1, LevelAnnounce.EVERY));
	}

	// --------------------------------------------------------------- clip library

	@Test
	public void readsClipsFromEventFolders() throws IOException
	{
		File root = folder.newFolder("audio");
		write(root, "death/first.wav");
		write(root, "death/second.wav");
		write(root, "quest/done.wav");

		ClipLibrary library = emptyBundle(root);
		library.reload();

		assertEquals(2, library.clips(VoiceEvent.DEATH).size());
		assertEquals(1, library.clips(VoiceEvent.QUEST).size());
		assertEquals(3, library.total());
		assertTrue(library.clips(VoiceEvent.PET).isEmpty());
	}

	@Test
	public void readsASingleClipSittingBesideTheFolders() throws IOException
	{
		File root = folder.newFolder("audio");
		write(root, "death.wav");

		ClipLibrary library = emptyBundle(root);
		library.reload();

		assertEquals(1, library.clips(VoiceEvent.DEATH).size());
	}

	@Test
	public void ignoresFilesThatAreNotAudio() throws IOException
	{
		File root = folder.newFolder("audio");
		write(root, "death/notes.txt");
		write(root, "death/line.wav");

		ClipLibrary library = emptyBundle(root);
		library.reload();

		assertEquals(1, library.clips(VoiceEvent.DEATH).size());
		assertEquals("line.wav", library.clips(VoiceEvent.DEATH).get(0).name());
	}

	@Test
	public void missingFolderIsNotAnError()
	{
		ClipLibrary library = emptyBundle(new File(folder.getRoot(), "never-created"));
		library.reload();

		assertEquals(0, library.total());
		assertNull(library.pick(VoiceEvent.DEATH));
	}

	@Test
	public void bundledClipsFillInForEventsWithNoLocalOnes() throws IOException
	{
		File root = folder.newFolder("audio");
		write(root, "death/mine.wav");

		ClipLibrary library = bundle(root, "death/bundled.wav\nquest/bundled.wav\n");
		library.reload();

		// A local death clip replaces the bundled death clip, but quest still falls back.
		List<Clip> deaths = library.clips(VoiceEvent.DEATH);
		assertEquals(1, deaths.size());
		assertEquals("mine.wav", deaths.get(0).name());
		assertFalse(deaths.get(0).isBundled());

		List<Clip> quests = library.clips(VoiceEvent.QUEST);
		assertEquals(1, quests.size());
		assertTrue(quests.get(0).isBundled());
	}

	@Test
	public void manifestSkipsCommentsAndUnknownFolders()
	{
		ClipLibrary library = bundle(new File(folder.getRoot(), "none"),
			"# a comment\n\nnot-an-event/line.wav\ndeath/line.wav\ndeath/readme.txt\n");
		library.reload();

		assertEquals(1, library.total());
		assertEquals(1, library.clips(VoiceEvent.DEATH).size());
	}

	@Test
	public void pickNeverRepeatsTheLastClipWhenThereAreOthers() throws IOException
	{
		File root = folder.newFolder("audio");
		write(root, "death/a.wav");
		write(root, "death/b.wav");

		ClipLibrary library = emptyBundle(root);
		library.reload();

		String first = library.pick(VoiceEvent.DEATH).name();
		for (int i = 0; i < 20; i++)
		{
			String next = library.pick(VoiceEvent.DEATH).name();
			assertNotEquals(next, first);
			first = next;
		}
	}

	@Test
	public void pickKeepsReturningTheOnlyClipAvailable() throws IOException
	{
		File root = folder.newFolder("audio");
		write(root, "death/only.wav");

		ClipLibrary library = emptyBundle(root);
		library.reload();

		assertEquals("only.wav", library.pick(VoiceEvent.DEATH).name());
		assertEquals("only.wav", library.pick(VoiceEvent.DEATH).name());
	}

	@Test
	public void createFoldersMakesOneDirectoryPerEventAndANote() throws IOException
	{
		File root = new File(folder.getRoot(), "audio");
		ClipLibrary library = emptyBundle(root);
		library.createFolders();

		for (VoiceEvent event : VoiceEvent.values())
		{
			assertTrue(event.folder(), new File(root, event.folder()).isDirectory());
		}

		assertTrue(new File(root, "README.txt").isFile());
	}

	@Test
	public void createFoldersRunsTwiceWithoutComplaining() throws IOException
	{
		File root = new File(folder.getRoot(), "audio");
		ClipLibrary library = emptyBundle(root);
		library.createFolders();
		library.createFolders();

		assertTrue(new File(root, "README.txt").isFile());
	}

	// ------------------------------------------------------------- voice events

	@Test
	public void eventsAreFoundByFolderName()
	{
		assertEquals(VoiceEvent.COLLECTION_LOG, VoiceEvent.byFolder("collection-log"));
		assertEquals(VoiceEvent.DEATH, VoiceEvent.byFolder("  DEATH "));
		assertNull(VoiceEvent.byFolder("nonsense"));
		assertNull(VoiceEvent.byFolder(null));
	}

	@Test
	public void everyEventHasItsOwnFolder()
	{
		for (VoiceEvent event : VoiceEvent.values())
		{
			assertNotNull(event.folder(), VoiceEvent.byFolder(event.folder()));
			assertEquals(event, VoiceEvent.byFolder(event.folder()));
		}
	}

	// ------------------------------------------------------------------- volume

	@Test
	public void fullVolumeLeavesTheRecordingAlone()
	{
		assertEquals(0f, SoundPlayer.gainFor(100, -80f, 6f), 0.001f);
	}

	@Test
	public void halfVolumeIsAboutSixDecibelsDown()
	{
		assertEquals(-6.02f, SoundPlayer.gainFor(50, -80f, 6f), 0.01f);
	}

	@Test
	public void zeroVolumeIsTheQuietestTheLineAllows()
	{
		assertEquals(-80f, SoundPlayer.gainFor(0, -80f, 6f), 0.001f);
	}

	@Test
	public void gainStaysInsideWhatTheLineSupports()
	{
		assertEquals(-10f, SoundPlayer.gainFor(1, -10f, 6f), 0.001f);
		assertEquals(0f, SoundPlayer.gainFor(500, -80f, 6f), 0.001f);
	}

	// ------------------------------------------------------------------ helpers

	private static ClipLibrary emptyBundle(File root)
	{
		return bundle(root, "");
	}

	/** A library whose bundled clips are the given index, so no jar is needed. */
	private static ClipLibrary bundle(File root, String manifest)
	{
		return new ClipLibrary(root, new Random(7))
		{
			@Override
			InputStream openManifest()
			{
				return new ByteArrayInputStream(manifest.getBytes(StandardCharsets.UTF_8));
			}
		};
	}

	private static void write(File root, String path) throws IOException
	{
		File file = new File(root, path);
		File parent = file.getParentFile();
		assertTrue(parent.isDirectory() || parent.mkdirs());
		assertTrue(file.createNewFile());
	}
}
