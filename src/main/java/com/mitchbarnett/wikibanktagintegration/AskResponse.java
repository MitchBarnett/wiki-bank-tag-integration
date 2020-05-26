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
