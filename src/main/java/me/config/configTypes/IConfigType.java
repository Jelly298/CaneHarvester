package me.config.configTypes;

import org.json.simple.JSONObject;

import java.util.List;

public interface IConfigType {
    void setValue(JSONObject config) throws ClassCastException;
    List<Object> getConfigValueList();
    List<ConfigPair<Object>> getConfigPairList();

}
