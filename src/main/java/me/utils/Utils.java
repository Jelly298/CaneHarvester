package me.utils;

import me.config.Config;
import me.config.DefaultConfig;
import me.config.configTypes.ConfigPair;
import me.config.configTypes.IConfigType;
import me.config.configTypes.WebhookConfig;
import me.network.DiscordWebhook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.io.IOException;
import java.util.*;

public class Utils {
    static Minecraft mc = Minecraft.getMinecraft();

    public static void drawString(String text, float x, float y, float size, int color) {
        GlStateManager.scale(size, size, size);
        float mSize = (float) Math.pow(size, -1);
        Minecraft.getMinecraft().fontRendererObj.drawString(text, Math.round(x / size), Math.round(y / size), color);
        GlStateManager.scale(mSize, mSize, mSize);
    }

    public static void drawStringWithShadow(String text, float x, float y, float size, int color) {
        GlStateManager.scale(size, size, size);
        float mSize = (float) Math.pow(size, -1);
        Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(text, Math.round(x / size), Math.round(y / size), color);
        GlStateManager.scale(mSize, mSize, mSize);
    }




    public static void drawHorizontalLine(int startX, int endX, int y, int color) {
        if (endX < startX) {
            int i = startX;
            startX = endX;
            endX = i;
        }

        Gui.drawRect(startX, y, endX + 1, y + 1, color);
    }

    public static void drawVerticalLine(int x, int startY, int endY, int color) {
        if (endY < startY) {
            int i = startY;
            startY = endY;
            endY = i;
        }

        Gui.drawRect(x, startY + 1, x + 1, endY, color);
    }







    public static int nextInt(int upperbound){
        Random r = new Random();
        return r.nextInt(upperbound);
    }
    public static void addCustomChat(String message, EnumChatFormatting color){
        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.DARK_PURPLE +
                "CANE_HARVESTER " + color + message));
    }
    public static void addCustomChat(String message){
        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.DARK_PURPLE +
                "CANE_HARVESTER " + EnumChatFormatting.GRAY + message));
    }
    public static void addCustomLog(String message){
        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.LIGHT_PURPLE +
                "CANE_HARVESTER_LOG " + EnumChatFormatting.GRAY + message));
    }


    public static double roundTo2DecimalPlaces(double d){
        return Math.floor(d * 100) / 100;
    }



    public static String formatNumber(int number){
        String s = Integer.toString(number);
        return String.format("%,d", number);

    }
    public static void sendWebhook(String message) {

        if(Config.<Boolean>get("webhook")) {
            System.out.println("sending");
            DiscordWebhook webhook = new DiscordWebhook(Config.get("webhookurl"));

            webhook.addEmbed(new DiscordWebhook.EmbedObject()
                    .setDescription("**Cane Harvester Log** ```" + message + "```")
                    .setColor(Color.decode("#228B22"))
                    .setFooter(Minecraft.getMinecraft().thePlayer.getName(), "")
            );
            new Thread(() -> {
                try {
                    webhook.execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        }



    }
    public static float getStringSize(String string, float size){
        return Minecraft.getMinecraft().fontRendererObj.getStringWidth(string) * size;
    }


    public static String formatInfo(String title, String value) {
        return  EnumChatFormatting.GRAY + "" + EnumChatFormatting.BOLD + title + EnumChatFormatting.DARK_GRAY +  " » " + EnumChatFormatting.GREEN + value;

    }
    public static float getStringSizeFromHeight(int height, float bufferScale) {
        return ((height * bufferScale)/(8 * 1.0f));
    }
    public static int getHoeSlot(){
        for(int i = 36; i < 44; i++){
            if(Minecraft.getMinecraft().thePlayer.inventoryContainer.inventorySlots.get(i).getStack() != null){
                if(Minecraft.getMinecraft().thePlayer.inventoryContainer.inventorySlots.get(i).getStack().getDisplayName().contains("Turing")){
                    return i - 36;
                }
            }
        }
        return 0;
    }
    public static boolean arrayHasPosAndNeg(ArrayList<Integer> ar) {
        boolean hasPos = false;
        boolean hasNeg = false;
        for (Integer integer : ar) {
            if (integer < 0)
                hasNeg = true;
            else
                hasPos = true;
        }
        return hasPos && hasNeg;

    }
    public static float getVerticalOffset(int height, float bufferScale){
        return height/2.0f - (height * bufferScale)/2.0f;
    }
    public static boolean isInCenterOfBlock(){
        return (Math.round(AngleUtils.get360RotationYaw()) == 180 || Math.round(AngleUtils.get360RotationYaw()) == 0) ?Math.abs(Minecraft.getMinecraft().thePlayer.posZ) % 1 > 0.3f && Math.abs(Minecraft.getMinecraft().thePlayer.posZ) % 1 < 0.7f :
                Math.abs(Minecraft.getMinecraft().thePlayer.posX) % 1 > 0.3f && Math.abs(Minecraft.getMinecraft().thePlayer.posX) % 1 < 0.7f;

    }




}
