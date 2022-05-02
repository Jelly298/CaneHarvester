package me.config;

import org.json.simple.JSONObject;
import org.lwjgl.input.Keyboard;

public class DefaultConfig {
    public static JSONObject getDefaultConfig() {
        JSONObject config = new JSONObject();
        config.put("resync", true);
        config.put("autosell", false);
        config.put("fastbreak", false);
        config.put("dropstone", false);
        config.put("jacob", false);
        config.put("jacobcap", 200000);
        config.put("webhook", false);
        config.put("webhookurl", "paste here");
        config.put("webhookstatus", false);
        config.put("log", true);
        config.put("openguikey", Keyboard.KEY_RSHIFT);
        config.put("togglekey", Keyboard.KEY_GRAVE);

        return config;
    }
}
