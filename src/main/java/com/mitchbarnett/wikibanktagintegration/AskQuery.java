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
import lombok.Data;

import java.util.List;
import java.util.Map;

interface AskQuery {
    
    @Data
    class Response {
        @SerializedName("query-continue-offset")
        private int queryContinueOffset;
        private Query query;
    }

    @Data
    class Query {
        @SerializedName("printrequests")
        private Request[] requests;
        private Map<String, Results> results;
        private String serializer;
        private int version;
        @SerializedName("meta")
        private Metadata metadata;
    }

    @Data
    class Request {
        private String label;
        private String key;
        private String redi;
        private String typeid;
        private int mode;
    }

    @Data
    class Metadata {
        private String hash;
        private int count;
        private int offset;
        private String source;
        private String time;
    }

    @Data
    class Printouts {
        @SerializedName("All Item ID")
        private List<Integer> allItemID;
    }

    @Data
    class Results {
        private Printouts printouts;
        private String fulltext;
        private String fullurl;
        private int namespace;
        private String exists;
        private String displaytitle;
    }
}

