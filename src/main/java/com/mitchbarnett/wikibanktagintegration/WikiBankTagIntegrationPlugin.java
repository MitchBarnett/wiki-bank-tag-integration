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
import net.runelite.http.api.RuneLiteAPI;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.Collections;
import java.util.List;


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

		List<Integer> items = getCategoryIDs(category);

		tagItems(items, category);

		String message = "Added " + category + " tag to " + String.valueOf(items.size()) + " items.";
		client.addChatMessage(ChatMessageType.GAMEMESSAGE,"", message,"");
		createTab(category);
	}

	private void tagItems(List<Integer> items, String category)
	{
	}

	private void createTab(String category)
	{
	}


	private List<Integer> getCategoryIDs(String category)
	{
		try
		{
			String wikiResponse = getWikiResponse(category).body().string();
			return getIDsFromJSON(wikiResponse);
		}
		catch (IOException e)
		{
			log.error(e.getMessage());
			return Collections.emptyList();
		}
	}

	private Response getWikiResponse(String category) throws IOException {
		Request request = new Request.Builder()
				.url(createQueryURL(category))
				.build();

		return RuneLiteAPI.CLIENT.newCall(request).execute();
	}


	String createQueryURL(String category)
	{
		return "https://oldschool.runescape.wiki/api.php?action=ask&query=[[Category:]]" + category + "|?All+Item+ID&fpr,at=json";
	}

	List<Integer> getIDsFromJSON(String Json)
	{
		return Collections.emptyList();
	}


}

