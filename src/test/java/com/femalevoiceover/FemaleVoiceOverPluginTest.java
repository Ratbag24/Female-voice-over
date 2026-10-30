package com.femalevoiceover;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

/** Starts RuneLite with the plugin loaded: ./gradlew run */
public class FemaleVoiceOverPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(FemaleVoiceOverPlugin.class);
		RuneLite.main(args);
	}
}
