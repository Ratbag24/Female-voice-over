package com.femalevoiceover;

/** A clip could not be played. Thrown so {@link SoundPlayer} needs no logger of its own. */
public class ClipException extends Exception
{
	private static final long serialVersionUID = 1L;

	public ClipException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
