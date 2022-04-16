package me.utils;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Slot;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import java.util.Random;

public class Utils {

    public static void drawString(String text, int x, int y, float size, int color) {
        GlStateManager.scale(size,size,size);
        float mSize = (float)Math.pow(size,-1);
        Minecraft.getMinecraft().fontRendererObj.drawString(text,Math.round(x / size),Math.round(y / size),color);
        GlStateManager.scale(mSize,mSize,mSize);
    }
    public static void hardRotate(float yaw) {
        Minecraft mc = Minecraft.getMinecraft();
        if(Math.abs(mc.thePlayer.rotationYaw - yaw) < 0.2f) {
            mc.thePlayer.rotationYaw = yaw;
            return;
        }
        while(mc.thePlayer.rotationYaw > yaw) {
            mc.thePlayer.rotationYaw -= 0.1f;
        }
        while(mc.thePlayer.rotationYaw < yaw) {
            mc.thePlayer.rotationYaw += 0.1f;

        }
    }




    public static void drawHorizontalLine(int startX, int endX, int y, int color)
    {
        if (endX < startX)
        {
            int i = startX;
            startX = endX;
            endX = i;
        }

        Gui.drawRect(startX, y, endX + 1, y + 1, color);
    }

    public static void drawVerticalLine(int x, int startY, int endY, int color)
    {
        if (endY < startY)
        {
            int i = startY;
            startY = endY;
            endY = i;
        }

        Gui.drawRect(x, startY + 1, x + 1, endY, color);
    }
    public static float get360RotationYaw(){
        return Minecraft.getMinecraft().thePlayer.rotationYaw > 0?
                (Minecraft.getMinecraft().thePlayer.rotationYaw % 360) :
                (Minecraft.getMinecraft().thePlayer.rotationYaw < 360f ? 360 - (-Minecraft.getMinecraft().thePlayer.rotationYaw % 360)  :  360 + Minecraft.getMinecraft().thePlayer.rotationYaw);
    }
    public static float get360RotationYaw(int yaw){
        return yaw > 0?
                (yaw % 360) :
                (yaw < 360f ? 360 - (-yaw % 360)  :  360 + yaw);
    }
    static int getOppositeAngle(int angle){
        return (angle < 180) ? angle + 180 : angle - 180;
    }
    static boolean shouldRotateClockwise(int targetYaw360, int initialYaw360){
        return targetYaw360 - initialYaw360 > 0 ?
                targetYaw360 - initialYaw360 <= 180 : targetYaw360 - initialYaw360 < -180;
    }
    public static void smoothRotateTo(final int rotation360){
        new Thread(new Runnable() {
            @Override
            public void run() {

                while (get360RotationYaw() != rotation360) {
                    if(Math.abs(get360RotationYaw() - rotation360) < 1f) {
                        Minecraft.getMinecraft().thePlayer.rotationYaw = Math.round(Minecraft.getMinecraft().thePlayer.rotationYaw + Math.abs(get360RotationYaw() - rotation360));
                        return;
                    }
                    Minecraft.getMinecraft().thePlayer.rotationYaw += 0.3f + nextInt(3)/10.0f;
                    try {
                        Thread.sleep(1);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

    }
    public static void smoothRotateClockwise(final int rotationClockwise360){
        new Thread(new Runnable() {
            @Override
            public void run() {

                int targetYaw = (Math.round(get360RotationYaw()) + rotationClockwise360) % 360;
                while (get360RotationYaw() != targetYaw) {
                    if(Math.abs(get360RotationYaw() - targetYaw) < 1f) {
                        Minecraft.getMinecraft().thePlayer.rotationYaw = Math.round(Minecraft.getMinecraft().thePlayer.rotationYaw + Math.abs(get360RotationYaw() - targetYaw));
                        return;
                    }
                    Minecraft.getMinecraft().thePlayer.rotationYaw += 0.3f + nextInt(3)/10.0f;
                    try {
                        Thread.sleep(1);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }
        }).start();

    }
    public static float getActualRotationYaw(){ //f3
        return Minecraft.getMinecraft().thePlayer.rotationYaw > 0?
                (Minecraft.getMinecraft().thePlayer.rotationYaw % 360 > 180 ? -(180 - (Minecraft.getMinecraft().thePlayer.rotationYaw % 360 - 180)) :  Minecraft.getMinecraft().thePlayer.rotationYaw % 360  ) :
                (-Minecraft.getMinecraft().thePlayer.rotationYaw % 360 > 180 ? (180 - (-Minecraft.getMinecraft().thePlayer.rotationYaw % 360 - 180))  :  -(-Minecraft.getMinecraft().thePlayer.rotationYaw % 360));
    }
    public static float getActualRotationYaw(int yaw){ //f3
        return yaw > 0?
                (yaw % 360 > 180 ? -(180 - (yaw % 360 - 180)) :  yaw % 360  ) :
                (-yaw% 360 > 180 ? (180 - (-yaw % 360 - 180))  :  -(-yaw % 360));
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
    public static Block getFrontBlock(){
        Minecraft mc = Minecraft.getMinecraft();
        return (mc.theWorld.getBlockState(
                new BlockPos(mc.thePlayer.getLookVec().xCoord + mc.thePlayer.posX, mc.thePlayer.posY,
                        mc.thePlayer.getLookVec().zCoord + mc.thePlayer.posZ)).getBlock());
    }
    public static Block getFrontDownBlock(){
        Minecraft mc = Minecraft.getMinecraft();
        return (mc.theWorld.getBlockState(
                new BlockPos(mc.thePlayer.getLookVec().xCoord + mc.thePlayer.posX, mc.thePlayer.posY - 1,
                        mc.thePlayer.getLookVec().zCoord + mc.thePlayer.posZ)).getBlock());
    }
    public static Block getBackBlock(){
        Minecraft mc = Minecraft.getMinecraft();
        return (mc.theWorld.getBlockState(
                new BlockPos(mc.thePlayer.getLookVec().xCoord * -1 + mc.thePlayer.posX, mc.thePlayer.posY,
                        mc.thePlayer.getLookVec().zCoord * -1 + mc.thePlayer.posZ)).getBlock());
    }
    public static Block getRightBlock(){
        Minecraft mc = Minecraft.getMinecraft();
        return (mc.theWorld.getBlockState(
                new BlockPos(mc.thePlayer.getLookVec().zCoord * -1 + mc.thePlayer.posX, mc.thePlayer.posY,
                        mc.thePlayer.getLookVec().xCoord + mc.thePlayer.posZ)).getBlock());
    }

    public static Block getLeftBlock(){
        Minecraft mc = Minecraft.getMinecraft();
        return (mc.theWorld.getBlockState(
                new BlockPos(mc.thePlayer.getLookVec().zCoord + mc.thePlayer.posX, mc.thePlayer.posY,
                        mc.thePlayer.getLookVec().xCoord * -1 + mc.thePlayer.posZ)).getBlock());
    }

    public static double roundTo2DecimalPlaces(double d){
        return Math.floor(d * 100) / 100;
    }

    public static boolean isWalkable(Block block) {
        return block == Blocks.air || block == Blocks.water || block == Blocks.flowing_water || block == Blocks.reeds;
    }

    // 0, 0 = initial block
    public static Block getBlockAround(int rightOffset, int frontOffset){
        Minecraft mc = Minecraft.getMinecraft();
        double X = mc.thePlayer.posX;
        double Y = mc.thePlayer.posY;
        double Z = mc.thePlayer.posZ;

        return (mc.theWorld.getBlockState(
                new BlockPos(mc.thePlayer.getLookVec().zCoord * -1 * rightOffset + mc.thePlayer.getLookVec().xCoord * frontOffset + X, Y,
                        mc.thePlayer.getLookVec().xCoord * rightOffset + mc.thePlayer.getLookVec().zCoord * frontOffset + Z)).getBlock());

    }
    public static Block getBlockAround(int rightOffset, int frontOffset, int upOffset){
        Minecraft mc = Minecraft.getMinecraft();
        double X = mc.thePlayer.posX;
        double Y = mc.thePlayer.posY;
        double Z = mc.thePlayer.posZ;

        return (mc.theWorld.getBlockState(
                new BlockPos(mc.thePlayer.getLookVec().zCoord * -1 * rightOffset + mc.thePlayer.getLookVec().xCoord * frontOffset + X, Y + upOffset,
                        mc.thePlayer.getLookVec().xCoord * rightOffset + mc.thePlayer.getLookVec().zCoord * frontOffset + Z)).getBlock());

    }
    public static String getScoreboardDisplayName(int line){
        try {
            return Minecraft.getMinecraft().theWorld.getScoreboard().getObjectiveInDisplaySlot(line).getDisplayName();
        } catch(Exception e){
            return "";
        }
    }





}
