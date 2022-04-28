package me.config;

import me.config.configTypes.*;
import me.gui.GUI;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import scala.util.parsing.json.JSON;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Config {

    public static JSONObject config;
    static List<IConfigType> configTypeList = new ArrayList<>();
    private static final File configFile = new File("caneharvester.json");

    public static void init() {

        registerConfigType(new JacobConfig());
        registerConfigType(new MiscellaneousConfig());
        registerConfigType(new WebhookConfig());

        // Create config file if it doesn't exist
        if (!configFile.isFile()) {
            writeConfig(DefaultConfig.getDefaultConfig());
        }

        // Read config file
        org.json.simple.parser.JSONParser jsonParser = new JSONParser();
        try (FileReader reader = new FileReader("caneharvester.json")) {

            Object obj = jsonParser.parse(reader);
            config = (JSONObject) obj;

            List<Object> tempKeys = new ArrayList<>();
            for(IConfigType configType : configTypeList){
                for(int i = 0; i < configType.getConfigPairList().size(); i++){
                    tempKeys.add(configType.getConfigPairList().get(i).getConfigID());
                }
            }

            if(DefaultConfig.getDefaultConfig().keySet().toArray() != tempKeys.toArray()) {
                writeConfig(DefaultConfig.getDefaultConfig());
                config = DefaultConfig.getDefaultConfig();
            }



        } catch (Exception e) {
            writeConfig(DefaultConfig.getDefaultConfig());
            config = DefaultConfig.getDefaultConfig();
        }

        // Update all config categories
        updateInterfaces();
    }

    public static void writeConfig(JSONObject json) {
        try (FileWriter file = new FileWriter("caneharvester.json")) {
            System.out.println("writing");
            file.write(json.toJSONString());
            file.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void updateInterfaces() {
        try {
            for (IConfigType configType : configTypeList)
                configType.setValue(config);
        } catch(ClassCastException e){
            e.printStackTrace();
            writeConfig(DefaultConfig.getDefaultConfig());
        }
    }

    static public<T> T get(String property) {
        if (config.get(property) == null) {
            set(property, DefaultConfig.getDefaultConfig().get(property));
        }
        return (T)config.get(property);
    }

    public static void set(String property, Object value) {
        config.put(property, value);
        writeConfig(config);
        updateInterfaces();
    }

    static void registerConfigType(IConfigType configType) {
        configTypeList.add(configType);
    }


}
