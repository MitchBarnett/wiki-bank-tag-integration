package com.mitchbarnett.wikibanktagintegration;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.events.CommandExecuted;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;


@Slf4j
@PluginDescriptor(
	name = "Wiki Bank Tag Integration"
)
public class WikiBankTagIntegrationPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private WikiBankTagIntegrationConfig config;

	@Override
	protected void startUp() throws Exception
	{
	}

	@Override
	protected void shutDown() throws Exception
	{
	}

	@Subscribe
	public void onCommandExecuted(CommandExecuted commandExecuted)
	{
		String[] args = commandExecuted.getArguments();

		if(commandExecuted.getCommand().equals("bt") && args.length == 1)
		{
			addTagsFromCategory(args[0]);
		}
	}

	@Provides
	WikiBankTagIntegrationConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(WikiBankTagIntegrationConfig.class);
	}

	private void addTagsFromCategory(String category)
	{
		log.info("attempting to add tags to items from " + category);
	}
}

