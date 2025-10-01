package com.mitchbarnett.wikibanktagintegration;

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

import com.google.common.base.MoreObjects;
import com.google.gson.Gson;
import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.events.CommandExecuted;
import net.runelite.client.callback.ClientThread;
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
import okhttp3.Call;

import javax.inject.Inject;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import static net.runelite.client.plugins.banktags.BankTagsPlugin.*;


@Slf4j
@PluginDescriptor(name = "Bank Tag Generation")
@PluginDependency(value = BankTagsPlugin.class)
public class WikiBankTagIntegrationPlugin extends Plugin {

    private static final String WIKI_BUCKET_QUERY_FORMAT = "https://oldschool.runescape.wiki/api.php?action=bucket&query=%s.limit(2000).run()&format=json";

    @Inject
    private Client client;

    @Inject
    private WikiBankTagIntegrationConfig config;

    @Inject
    private ConfigManager configManager;

    @Inject
    private TagManager tagManager;

    @Inject
    private ClientThread clientThread;

    @Inject
    private OkHttpClient httpClient;

    @Inject
    private Gson gson;

    @Subscribe
    public void onCommandExecuted(CommandExecuted commandExecuted) {
        String[] args = commandExecuted.getArguments();
        if (args.length < 1) {
            // silently exit
            return;
        }

        if (commandExecuted.getCommand().equals(config.categoryChatCommand())) {
            addTagsFromCategory(String.join(" ", args));
        } else if (commandExecuted.getCommand().equals(config.dropsChatCommand())) {
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
        getDropIDs(monster, items -> {
            clientThread.invokeLater(() -> {
                tagItems(items, monster + " drops");
                if (items.length == 0) {
                    String message = String.format("No drops found for %s", monster);
                    client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", message, "");
                } else {
                    String message = String.format("Added %s drops tag to %s items.", monster, items.length);
                    client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", message, "");
                    createTab(monster + " drops", items[0]);
                }
            });
        });
    }


    /**
     * Adds a tag of the category to items found in the provided osrs wiki category
     *
     * @param category The name of the osrs wiki category to generate a list of items to tag.
     */
    private void addTagsFromCategory(String category) {
        log.info("Attempting to add tags to items from {}", category);
        getCategoryIDs(category, items -> {
            clientThread.invokeLater(() -> {
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
            });
        });
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
        configManager.setConfiguration(CONFIG_GROUP, TAG_ICON_PREFIX + Text.standardize(tag), iconItemId);

    }

    /**
     * Gets the item IDs of all items within a OSRS wiki category
     *
     * @param category The name of the OSRS wiki category that will be Item Ids will be generated from
     * @return A list of Item IDs found for the provided category.
     */
    public void getCategoryIDs(String category, Consumer<int[]> callback) {
        String safeQuery = getNormalisedQuery(category, ArgHandler.AggregateTerms.Format.CATEGORY);
        String query = String.format("bucket('item_id').select('item_id.id').where(%s)", safeQuery);
        getWikiResponse(query, new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                log.error("Error fetching category IDs: {}", e.getMessage());
                callback.accept(new int[0]); // Pass empty array to callback
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String wikiResponse = response.body().string();
                int[] ids = getIDsFromJSON(wikiResponse);
                callback.accept(ids);
            }
        });
    }



    /**
     * Gets the item IDs of all items drops by a monster
     *
     * @param monster The name of the OSRS monster that will be Item Ids will be generated from
     * @return A list of Item IDs found for the provided category.
     */
    public void getDropIDs(String monster, Consumer<int[]> callback) {
        String safeQuery = getNormalisedQuery(monster, ArgHandler.AggregateTerms.Format.MONSTER);
        String query = String.format("bucket('dropsline').join('item_id', 'dropsline.item_name', 'item_id.page_name').where(%s).select('item_id.id')", safeQuery);
        getWikiResponse(query, new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                log.error("Error fetching drop IDs: {}", e.getMessage());
                callback.accept(new int[0]); // Pass empty array to callback
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String wikiResponse = response.body().string();
                int[] ids = getIDsFromJSON(wikiResponse);
                callback.accept(ids);
            }
        });
    }

    /**
     * Normalises the subject by replacing underscores with spaces
     * and escaping it for safe use in query strings.
     *
     * @param subject the input string
     * @param format the format string
     * @return the normalised and escaped string
     */
    private String getNormalisedQuery(String subject, ArgHandler.AggregateTerms.Format format) {
        return Objects.requireNonNull(ArgHandler.parseAggregateTerms(client, subject)).toSmwString(format);
    }

    /**
     * Queries the OSRS wiki and returns the response
     *
     * @param subject The subject of the query string e.g. category name or monster name
     */
    private void getWikiResponse(String subject, okhttp3.Callback callback) {
        Request request = new Request.Builder()
                .url(createQueryURL(subject))
                .build();
        httpClient.newCall(request).enqueue(callback);
    }


    /**
     * Constructs the URL of the specified query string
     *
     * @param query The query to be used
     * @return The full query URL
     */
    private String createQueryURL(String query) {
        return String.format(WIKI_BUCKET_QUERY_FORMAT, query);
    }

    /**
     * Extracts ItemIDs from a JSON HTTP response.
     *
     * @param jsonIn The JSON as a string. It must be in the correct format.
     * @return A list of the item IDs pulled from the JSON results.
     * @see BucketQuery.Response
     */
    private int[] getIDsFromJSON(String jsonIn) {
        BucketQuery.Response bucketResponse = gson.fromJson(jsonIn, BucketQuery.Response.class);
        return bucketResponse.getBucket()
                .stream()
                .flatMap(bucketItems ->
                        Optional.ofNullable(bucketItems.getItemIds())
                                .orElse(Collections.emptyList())
                                .stream()
                )
                .map(WikiBankTagIntegrationPlugin::parseIntSafe)
                .flatMapToInt(opt -> opt.map(IntStream::of).orElseGet(IntStream::empty))
                .distinct()
                .toArray();
    }

    /**
     * Parses the given string into an {@code Integer}.
     * Returns an {@link Optional} with the value if parsing succeeds,
     * or {@link Optional#empty()} if it fails.
     *
     * @param itemIdString the string to parse
     * @return an optional integer
     */
    private static Optional<Integer> parseIntSafe(String itemIdString) {
        try {
            return Optional.of(Integer.parseInt(itemIdString));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
}
