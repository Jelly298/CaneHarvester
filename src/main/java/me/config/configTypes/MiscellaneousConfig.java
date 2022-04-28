package me.config.configTypes;

import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MiscellaneousConfig implements IConfigType{

    public static List<ConfigPair<Object>> configList = Stream.of(
            new ConfigPair<>("xray", "XRay", (Object) false),
            new ConfigPair<>("autosell", "Autosell", (Object) false)

    ).collect(Collectors.toList());

    @Override
    public void setValue(JSONObject config) throws ClassCastException{
        for(ConfigPair<Object> configPair : configList) {
            configPair.setConfigValue(config.get(configPair.getConfigID()));
        }
    }
    @Override
    public List<Object> getConfigValueList() {
        List<Object> tempConfigList = new ArrayList<>();
        for(ConfigPair<Object> configPair : configList){
            tempConfigList.add(configPair.getConfigValue());
        }
        return tempConfigList;
    }

    @Override
    public List<ConfigPair<Object>> getConfigPairList() {
        return configList;
    }
}
