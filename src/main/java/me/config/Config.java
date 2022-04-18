package me.config;

import me.gui.GUI;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;

public class Config {
    public static boolean resync = false;
    public static String urlText = "";
    public static void writeConfig(){
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter("scconfig.txt"));
            bufferedWriter.write("\n" + resync);
            bufferedWriter.write("\n" + urlText);
            bufferedWriter.close();
        }catch(Exception e){
            e.printStackTrace();
        }

    }
    public static void readConfig () throws Exception {
        try{
            BufferedReader bufferedReader = new BufferedReader(new FileReader("scconfig.txt"));
            bufferedReader.readLine();
            resync = Boolean.parseBoolean(bufferedReader.readLine());
            urlText = bufferedReader.readLine();
            bufferedReader.close();

        }catch(Exception e){
            throw new Exception();
        }
    }


}
