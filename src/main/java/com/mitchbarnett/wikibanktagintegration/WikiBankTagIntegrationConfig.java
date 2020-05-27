package com.mitchbarnett.wikibanktagintegration;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("wikibanktagintegration")
public interface WikiBankTagIntegrationConfig extends Config
{
	@ConfigItem(
		keyName = "chatCommand",
		name = "Chat command",
		description = "The chat command to use the plugin default is bt"
	)
	default String chatCommand()
	{
		return "bt";
	}
}