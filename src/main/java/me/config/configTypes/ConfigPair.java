package me.config.configTypes;

public class ConfigPair<T> {
    private String configID;
    private String configName;
    private T configValue;


    public ConfigPair(String configID,  String configName, T configValue) {
        this.configID = configID;
        this.configName = configName;
        this.configValue = configValue;
    }

    public String getConfigID() {
        return configID;
    }

    public void setConfigID(String configID) {
        this.configID = configID;
    }

    public String getConfigName() {
        return configName;
    }

    public void setConfigName(String configName) {
        this.configName = configName;
    }

    public T getConfigValue() {
        return configValue;
    }

    public void setConfigValue(T configValue) {
        this.configValue = configValue;
    }
}
