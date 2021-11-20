/*
 * Copyright (c) 2020 Mitch Barnett <mitch@mitchbarnett.com Discord: Wizard Mitch#5072 Reddit: Wizard_Mitch>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.mitchbarnett.wikibanktagintegration;

import com.google.common.base.MoreObjects;
import com.google.gson.Gson;
import com.google.inject.Provides;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.events.CommandExecuted;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.banktags.BankTagsPlugin;
import net.runelite.client.plugins.banktags.TagManager;
import net.runelite.client.util.Text;
import net.runelite.http.api.RuneLiteAPI;
import okhttp3.Request;
import okhttp3.Response;
import static net.runelite.client.plugins.banktags.BankTagsPlugin.CONFIG_GROUP;
import static net.runelite.client.plugins.banktags.BankTagsPlugin.TAG_TABS_CONFIG;
import static net.runelite.client.plugins.banktags.BankTagsPlugin.ICON_SEARCH;
import java.io.IOException;


@Slf4j
@PluginDescriptor(
	name = "Bank Tag Generation"
)
@PluginDependency(value = BankTagsPlugin.class) // Required for bank tags TagManager
public class WikiBankTagIntegrationPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private WikiBankTagIntegrationConfig config;

	@Inject
	private ConfigManager configManager;

	@Inject
	private TagManager tagManager;

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

		if (commandExecuted.getCommand().equals(config.categoryChatCommand()) && args.length == 1)
		{
			addTagsFromCategory(args[0]);
		}
		else if (commandExecuted.getCommand().equals(config.dropsChatCommand()) && args.length == 1)
		{
			addTagsFromDrops(args[0]);
		}
	}

	@Provides
	WikiBankTagIntegrationConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(WikiBankTagIntegrationConfig.class);
	}

	/**
	 * Adds a tag of the monster to items found in the provided osrs monster drops
	 *
	 * @param monster The name of the osrs wiki category to generate a list of items to tag.
	 */
	private void addTagsFromDrops(String monster)
	{
		log.info("attempting to add tags to items dropped by " + monster);

		List<Integer> items = getDropIDs(monster);

		tagItems(items, monster + " drops");

		if (items.size() == 0)
		{
			String message = "No drops found for " + monster;
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", message, "");
		}
		else
		{
			String message = "Added " + monster + " drops tag to " + String.valueOf(items.size()) + " items.";
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", message, "");
			createTab(monster + " drops", Collections.min(items));
		}

	}

	/**
	 * Adds a tag of the category to items found in the provided osrs wiki category
	 *
	 * @param category The name of the osrs wiki category to generate a list of items to tag.
	 */
	private void addTagsFromCategory(String category)
	{
		log.info("attempting to add tags to items from " + category);

		List<Integer> items = getCategoryIDs(category);

		tagItems(items, category);

		if (items.size() == 0)
		{
			String message = "No items found for category " + category;
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", message, "");
		}
		else
		{
			String message = "Added " + category + " tag to " + String.valueOf(items.size()) + " items.";
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", message, "");
			createTab(category, Collections.min(items));
		}

	}

	/**
	 * Applies a BankTag tag to the provided items
	 *
	 * @param items A list of ItemIDs to be tagged.
	 * @param tag   the tag to be applied to the items
	 */
	private void tagItems(List<Integer> items, String tag)
	{
		for (int itemID : items)
		{
			tagManager.addTag(itemID, tag, false);
		}

	}

	/**
	 * Applies a BankTag tag to the provided items
	 *
	 * @return A list of bank tabs in string format.
	 */
	List<String> getAllTabs()
	{
		return Text.fromCSV(MoreObjects.firstNonNull(configManager.getConfiguration(CONFIG_GROUP, TAG_TABS_CONFIG), ""));
	}

	/**
	 * Creates a new BankTag tab
	 *
	 * @param tag        The name of the bank tag
	 * @param iconItemId the item ID of the item to be the tab icon
	 */
	private void createTab(String tag, int iconItemId)
	{
		// Banktags config must be change directly as TagManager is not public
		//String currentConfig = configManager.getConfiguration(CONFIG_GROUP, TAG_TABS_CONFIG);

		List<String> tabs = new ArrayList<>(getAllTabs());
		tabs.add(Text.standardize(tag));
		String tags = Text.toCSV(tabs);

		configManager.setConfiguration(CONFIG_GROUP, TAG_TABS_CONFIG, tags);

		configManager.setConfiguration(CONFIG_GROUP, ICON_SEARCH + Text.standardize(tag), iconItemId);

	}

	/**
	 * Gets the item IDs of all items within a OSRS wiki category
	 *
	 * @param category The name of the OSRS wiki category that will be Item Ids will be generated from
	 * @return A list of Item IDs found for the provided category.
	 */
	private List<Integer> getCategoryIDs(String category)
	{
		try
		{
			String query = "[[category:"+category+"]]|?All Item ID";
			String wikiResponse = getWikiResponse(query).body().string();
			return getIDsFromJSON(wikiResponse);
		}
		catch (IOException e)
		{
			String message = "There was an error retrieving data";
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", message, "");

			log.error(e.getMessage());
			return Collections.emptyList();
		}
	}

	/**
	 * Gets the item IDs of all items drops by a monster
	 *
	 * @param monster The name of the OSRS monster that will be Item Ids will be generated from
	 * @return A list of Item IDs found for the provided category.
	 */
	private List<Integer> getDropIDs(String monster)
	{
		try
		{
			String query = "[[Dropped from::"+monster+"]]|?Dropped item.All Item ID";
			String wikiResponse = getWikiResponse(query).body().string();
			return getIDsFromJSON(wikiResponse);
		}
		catch (IOException e)
		{
			String message = "There was an error retriving data";
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", message, "");

			log.error(e.getMessage());
			return Collections.emptyList();
		}
	}

	/**
	 * Makes and returns results for an ask Query the OSRS wiki for all item IDs in the provided category
	 *
	 * @param category The name of the OSRS wiki category that will be Item Ids will be generated from
	 * @return A okhttp3 HTTP response containing the results of a ask query in JSON format
	 */
	private Response getWikiResponse(String query) throws IOException
	{
		Request request = new Request.Builder()
			.url(createQueryURL(query))
			.build();

		return RuneLiteAPI.CLIENT.newCall(request).execute();
	}


	/**
	 * Makes a query URL to get all item IDs in the supplied category
	 *
	 * @param query The query to be used
	 * @return The full query URL
	 */
	String createQueryURL(String query)
	{
		return "https://oldschool.runescape.wiki/api.php?action=ask&query="+query+"|+limit=2000&format=json";
	}

	/**
	 * Extracts ItemIDs from a JSON HTTP response. The JSON must be in the format that returned by a query that uses the
	 * createQueryURL so it can be parsed into an AskResponse.
	 *
	 * @param jsonIn The JSON as a string. It must be in the correct format.
	 * @return A list of the item IDs pulled from the JSON results.
	 * @see AskResponse
	 */
	List<Integer> getIDsFromJSON(String jsonIn)
	{
		Gson gson = new Gson();
		AskResponse askResponse = gson.fromJson(jsonIn, AskResponse.class);

		List<Integer> itemIDs = new ArrayList<>();

		for (Iterator<Map.Entry<String, AskResponse.Query.Results>> it = askResponse.query.results.entrySet().iterator(); it.hasNext(); )
		{
			Map.Entry<String, AskResponse.Query.Results> entry = it.next();
			for (int itemID : entry.getValue().printouts.allItemID)
			{
				itemIDs.add(itemID);
			}
		}

		return itemIDs;
	}
}

