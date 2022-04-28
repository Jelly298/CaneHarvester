package me.gui.JellyGui.GuiComponents.GuiConfig;

import me.config.Config;

public class GuiBooleanConfig{

    public static void setConfigValue(String configKey, boolean value){
        Config.set(configKey, value);
    }
    public static  void switchConfigValue(String configKey){
        Config.set(configKey, !((Boolean)Config.get(configKey)));
    }
    public static  Boolean getConfigValue(String configKey){
        return (Boolean) Config.get(configKey);
    }

}
