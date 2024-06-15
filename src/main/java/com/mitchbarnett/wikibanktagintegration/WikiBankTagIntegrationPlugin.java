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
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import javax.inject.Inject;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static net.runelite.client.plugins.banktags.BankTagsPlugin.*;


@Slf4j
@PluginDescriptor(name = "Bank Tag Generation")
@PluginDependency(value = BankTagsPlugin.class)
public class WikiBankTagIntegrationPlugin extends Plugin {

    private static final String WIKI_QUERY_FORMAT = "https://oldschool.runescape.wiki/api.php?action=ask&query=%s|+limit=2000&format=json";
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Inject
    private Client client;

    @Inject
    private WikiBankTagIntegrationConfig config;

    @Inject
    private ConfigManager configManager;

    @Inject
    private TagManager tagManager;

    @Inject
    private OkHttpClient httpClient;

    @Subscribe
    public void onCommandExecuted(CommandExecuted commandExecuted) {
        String[] args = commandExecuted.getArguments();
        if (commandExecuted.getCommand().equals(config.categoryChatCommand()) && args.length > 0) {
            addTagsFromCategory(String.join(" ", args));
        } else if (commandExecuted.getCommand().equals(config.dropsChatCommand()) && args.length > 0) {
            addTagsFromDrops(String.join(" ", args));
        }
    }

    @Provides
    WikiBankTagIntegrationConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(WikiBankTagIntegrationConfig.class);
    }

    /**
     * Adds a tag of the monster to items found in the provided osrs monster drops
     *
     * @param monster The name of the osrs wiki category to generate a list of items to tag.
     */
    private void addTagsFromDrops(String monster) {
        log.info("Attempting to add tags to items dropped by {}", monster);
        int[] items = getDropIDs(monster);
        tagItems(items, monster + " drops");
        if (items.length == 0) {
            String message = String.format("No drops found for %s", monster);
            client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", message, "");
        } else {
            String message = String.format("Added %s drops tag to %s items.", monster, items.length);
            client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", message, "");
            createTab(monster + " drops", items[0]);
        }
    }

    /**
     * Adds a tag of the category to items found in the provided osrs wiki category
     *
     * @param category The name of the osrs wiki category to generate a list of items to tag.
     */
    private void addTagsFromCategory(String category) {
        log.info("Attempting to add tags to items from {}", category);
        int[] items = getCategoryIDs(category);
        tagItems(items, category);
        if (items.length == 0) {
            client.addChatMessage(ChatMessageType.GAMEMESSAGE,
                    "",
                    String.format("No items found for category %s", category),
                    "");
        } else {
            client.addChatMessage(ChatMessageType.GAMEMESSAGE,
                    "",
                    String.format("Added %s tag to %d items.", category, items.length),
                    "");
            createTab(category, items[0]);
        }

    }

    /**
     * Applies a BankTag tag to the provided items
     *
     * @param items The item ID's to be tagged
     * @param tag   the tag to be applied to the items
     */
    private void tagItems(int[] items, String tag) {
        for (int itemID : items) {
            tagManager.addTag(itemID, tag, false);
        }
    }

    /**
     * Applies a BankTag tag to the provided items
     *
     * @return A list of bank tabs in string format.
     */
    private List<String> getAllTabs() {
        return Text.fromCSV(MoreObjects.firstNonNull(configManager.getConfiguration(CONFIG_GROUP, TAG_TABS_CONFIG), ""));
    }

    /**
     * Creates a new BankTag tab
     *
     * @param tag        The name of the bank tag
     * @param iconItemId the item ID of the item to be the tab icon
     */
    private void createTab(String tag, int iconItemId) {
        // Bank tags config must be change directly as TagManager is not public
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
    int[] getCategoryIDs(String category) {
        try {
            String safe_query = URLEncoder.encode(category, "UTF-8");
            String query = String.format("[[category:%s]]|?All+Item+ID", safe_query);
            String wikiResponse = Objects.requireNonNull(getWikiResponse(query).body()).string();
            return getIDsFromJSON(wikiResponse);
        } catch (IOException e) {
            if (client != null)
                client.addChatMessage(ChatMessageType.GAMEMESSAGE,
                        "",
                        "There was an error retrieving data",
                        "");
            log.error(e.getMessage());
            return new int[0];
        }
    }

    /**
     * Gets the item IDs of all items drops by a monster
     *
     * @param monster The name of the OSRS monster that will be Item Ids will be generated from
     * @return A list of Item IDs found for the provided category.
     */
    int[] getDropIDs(String monster) {
        try {
            String safe_query = URLEncoder.encode(monster, "UTF-8");
            String query = String.format("[[Dropped from::%s]]|?Dropped item.All+Item+ID", safe_query);
            String wikiResponse = Objects.requireNonNull(getWikiResponse(query).body()).string();
            return getIDsFromJSON(wikiResponse);
        } catch (IOException e) {
            if (client != null)
                client.addChatMessage(ChatMessageType.GAMEMESSAGE,
                        "",
                        "There was an error retrieving data",
                        "");
            log.error(e.getMessage());
            return new int[0];
        }
    }

    /**
     * Queries the OSRS wiki and returns the response
     *
     * @param category The category query string
     * @return The results of the query
     */
    private Response getWikiResponse(String category) throws IOException {
        Request request = new Request.Builder()
                .url(createQueryURL(category))
                .build();
        return httpClient.newCall(request).execute();
    }


    /**
     * Constructs the URL of the specified query string
     *
     * @param query The query to be used
     * @return The full query URL
     */
    private String createQueryURL(String query) {
        return String.format(WIKI_QUERY_FORMAT, query);
    }

    /**
     * Extracts ItemIDs from a JSON HTTP response.
     *
     * @param jsonIn The JSON as a string. It must be in the correct format.
     * @return A list of the item IDs pulled from the JSON results.
     * @see AskQuery.Response
     */
    private int[] getIDsFromJSON(String jsonIn) {
        AskQuery.Response askResponse = gson.fromJson(jsonIn, AskQuery.Response.class);
        return askResponse.getQuery().getResults().values()
                .stream()
                .flatMap(v -> v.getPrintouts().getAllItemID().stream())
                .mapToInt(x -> x)
                .distinct()
                .toArray();
    }
}

