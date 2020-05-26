package com.mitchbarnett.wikibanktagintegration;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
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
		log.info("Wiki Bank Tag Integration started!");
	}

	@Override
	protected void shutDown() throws Exception
	{
		log.info("Wiki Bank Tag Integration stopped!");
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == GameState.LOGGED_IN)
		{
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Wiki Bank Tag Integration says " + config.greeting(), null);
		}
	}

	@Provides
	WikiBankTagIntegrationConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(WikiBankTagIntegrationConfig.class);
	}
}
