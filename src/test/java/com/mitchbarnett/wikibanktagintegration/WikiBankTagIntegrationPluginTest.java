package com.mitchbarnett.wikibanktagintegration;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class WikiBankTagIntegrationPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(WikiBankTagIntegrationPlugin.class);
		RuneLite.main(args);
	}
}