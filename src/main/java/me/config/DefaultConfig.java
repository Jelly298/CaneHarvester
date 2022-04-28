package me.config;

import org.json.simple.JSONObject;

public class DefaultConfig {
    public static JSONObject getDefaultConfig() {
        JSONObject config = new JSONObject();
        config.put("resync", false);
        config.put("autosell", false);
        config.put("dropstone", false);
        config.put("jacob", false);
        config.put("jacobcap", 200000);
        config.put("webhook", false);
        config.put("webhookurl", "paste here");
        config.put("webhookstatus", false);
        return config;
    }
}
