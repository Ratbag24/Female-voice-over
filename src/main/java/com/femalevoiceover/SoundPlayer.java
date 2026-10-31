package com.femalevoiceover;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * Plays one clip at a time on the system mixer.
 *
 * <p>Only one line is held open: two announcements landing in the same tick would otherwise talk
 * over each other, which sounds broken rather than busy.
 */
public class SoundPlayer implements AutoCloseable
{
	private final Object lock = new Object();

	private javax.sound.sampled.Clip active;

	/**
	 * Plays a clip at {@code volumePercent} of its recorded level.
	 *
	 * @param interrupt whether a clip already playing should be cut off, or this one dropped
	 */
	public void play(Clip clip, int volumePercent, boolean interrupt) throws ClipException
	{
		if (clip == null || volumePercent <= 0)
		{
			return;
		}

		synchronized (lock)
		{
			if (active != null && active.isActive())
			{
				if (!interrupt)
				{
					return;
				}

				release();
			}
		}

		javax.sound.sampled.Clip line;
		// Clip.open() reads the whole stream up front, so the stream can close before playback.
		try (InputStream raw = clip.open();
			BufferedInputStream buffered = new BufferedInputStream(raw);
			AudioInputStream audio = AudioSystem.getAudioInputStream(buffered))
		{
			// Acquiring the line and reading the file fail for unrelated reasons, and saying
			// "could not read your recording" when the real problem is a missing sound card
			// sends people off re-encoding perfectly good files.
			try
			{
				line = AudioSystem.getClip();
			}
			catch (IllegalArgumentException | LineUnavailableException e)
			{
				throw new ClipException("This system has no audio output available, so "
					+ clip.name() + " could not play. The recording itself is fine.", e);
			}

			try
			{
				line.open(audio);
			}
			catch (LineUnavailableException e)
			{
				line.close();
				throw new ClipException("The audio device was busy, so " + clip.name()
					+ " could not play.", e);
			}
			catch (IllegalArgumentException e)
			{
				line.close();
				throw new ClipException(clip.name() + " is " + audio.getFormat()
					+ ", which this system's audio device will not accept."
					+ " Re-save it as 16-bit PCM .wav.", e);
			}
		}
		catch (UnsupportedAudioFileException e)
		{
			throw new ClipException(clip.name() + " is not an audio format that can be read."
				+ " Re-save it as 16-bit PCM .wav. MP3 and OGG do not work.", e);
		}
		catch (IOException e)
		{
			throw new ClipException("Could not read " + clip.name() + " from disk.", e);
		}

		applyVolume(line, volumePercent);
		line.addLineListener(event ->
		{
			if (event.getType() == LineEvent.Type.STOP)
			{
				synchronized (lock)
				{
					// Only retire the line if it is still the current one.
					if (active == line)
					{
						release();
					}
					else
					{
						line.close();
					}
				}
			}
		});

		synchronized (lock)
		{
			active = line;
		}

		line.start();
	}

	/** Stops whatever is playing, if anything. */
	public void stop()
	{
		synchronized (lock)
		{
			release();
		}
	}

	@Override
	public void close()
	{
		stop();
	}

	private void release()
	{
		if (active != null)
		{
			active.stop();
			active.close();
			active = null;
		}
	}

	private static void applyVolume(javax.sound.sampled.Clip line, int volumePercent)
	{
		if (!line.isControlSupported(FloatControl.Type.MASTER_GAIN))
		{
			return;
		}

		FloatControl gain = (FloatControl) line.getControl(FloatControl.Type.MASTER_GAIN);
		gain.setValue(gainFor(volumePercent, gain.getMinimum(), gain.getMaximum()));
	}

	/**
	 * The gain, in decibels, for a percentage of the clip's recorded level. 100% is the recording
	 * untouched; halving the percentage drops it about 6 dB, which is how loudness is perceived.
	 */
	static float gainFor(int volumePercent, float min, float max)
	{
		if (volumePercent <= 0)
		{
			return min;
		}

		int capped = Math.min(volumePercent, 100);
		float decibels = (float) (20.0 * Math.log10(capped / 100.0));
		return Math.max(min, Math.min(max, decibels));
	}
}
