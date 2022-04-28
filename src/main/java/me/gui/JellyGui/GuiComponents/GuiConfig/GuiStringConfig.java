package me.gui.JellyGui.GuiComponents.GuiConfig;

import me.config.Config;

public class GuiStringConfig {
    public static void setConfigString(String configKey, String string){
        Config.set(configKey, string);
    }
    public static  String getConfigString(String configKey){
            return Config.get(configKey).toString();
    }
}
