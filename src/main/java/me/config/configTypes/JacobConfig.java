package me.config.configTypes;

import me.config.Config;
import me.config.DefaultConfig;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JacobConfig implements IConfigType{
    public static List<ConfigPair<Object>> configList = Stream.of(
            new ConfigPair<>("jacob", "Jacob Failsafe", (Object) false),
            new ConfigPair<>("jacobcap", "Jacob Threshold", (Object) 0)
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
