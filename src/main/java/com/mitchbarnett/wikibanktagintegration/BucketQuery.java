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

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.List;

/**
 * Minimized version of the bucket query response when joining dropsline table and item_id table to get the items ids of
 * the drops from monsters.
 * See <a href="https://oldschool.runescape.wiki/w/Bucket:Dropsline">Bucket:Dropsline</a> and <a href="https://oldschool.runescape.wiki/w/Bucket:Item_id">Bucket:Item_id</a>
 * Minimum needed to restore functionality to the plugin.
 * <p>
 * The API currently does not support filtering items with no id like Clue Scrolls with Wiki item ID "N/A"
 * Example <a href="https://oldschool.runescape.wiki/w/Special:Bucket?bucket=item_id&select=*&where=%27item_id.id%27%2C+%27%3D%27%2C+%27N%2FA%27&limit=20&offset=0">Bucket query for Clue Scroll</a>.
 * <p>
 * Could not get <a href="https://meta.weirdgloop.org/w/Extension:Bucket/Usage#Condition"> bucket conditionals</a>
 * to work to filer out N/A as item_id.id is an array.
 * <p>
 * Potions (and probably other consumables with ) from Monster drops do not function properly as the dropsline
 * table only supports item_name e.g. Prayer potion(3) but the item_id table uses page_name_sub e.g.
 * <a href="https://oldschool.runescape.wiki/w/Special:Bucket?bucket=item_id&select=*&where=%7B%27item_id.page_name%27%2C+%27Prayer+potion%27%7D&limit=20&offset=0">Prayer potion#(3)</a>
 * so it is impossible to match these names to get the item ID.
 */
interface BucketQuery {

    @Data
    class Response {
        private String bucketQuery;
        private List<BucketItem> bucket;
    }

    @Data
    class BucketItem {
        @SerializedName("item_id.id")
        private List<String> itemIds;
    }
}
