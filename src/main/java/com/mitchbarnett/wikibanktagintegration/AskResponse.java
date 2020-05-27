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

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

public class AskResponse {
    @SerializedName("query-continue-offset")
    public int continueOffset;
    public Query query;

    public class Query {
        List<Request> printrequests;
        Map<String, Results> results;
        String serialiser;
        int version;
        Meta meta;

        public class Request {
            public String label;
            public String key;
            public String redi;
            public String typeid;
            public int mode;
        }

        public Map<String, Results> getMasterlist() {
            return results;
        }

        public void setMasterlist(Map<String, Results> masterlist) {
            this.results = masterlist;
        }

        public class Results {
            Printouts printouts;
            String fulltext;
            String fullurl;
            int namespace;
            String exists;
            String displaytitle;

            public class Printouts {
                @SerializedName("All Item ID")
                List<Integer> allItemID;
            }
        }


        public class Meta {
            String hash;
            int counting;
            int offset;
            String source;
            String time;
        }
    }
}
