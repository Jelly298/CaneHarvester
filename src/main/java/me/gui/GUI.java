package me.gui;

import me.CaneHarvester;
import me.config.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class GUI extends GuiScreen {


    int buttonWidth = 85;
    int buttonHeight = 65;

    int fieldWidth = 200;
    int fieldHeight = 20;

    int field2Width = 100;
    int field2Height = 20;

    private GuiTextField urlTextBox;
    private GuiTextField jacobThresholdBox;

    public static GuiDraggableComponent draggableProfitGUI = new GuiDraggableComponent(0, 2, 200, 77, new Color(0, 0, 0, 100).getRGB(), new ArrayList<>());


    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {



    }
    @Override
    public void initGui() {
        super.initGui();

        urlTextBox = new GuiTextField(1, Minecraft.getMinecraft().fontRendererObj, this.width/2 - fieldWidth + 140, this.height / 2 - fieldHeight / 2 + 60, fieldWidth, fieldHeight);
        urlTextBox.setMaxStringLength(256);
        urlTextBox.setText(Config.urlText == null ? "" : Config.urlText);
        urlTextBox.setFocused(true);
        jacobThresholdBox = new GuiTextField(2, Minecraft.getMinecraft().fontRendererObj, this.width/2 - field2Width + 140, this.height / 2 - field2Height / 2 + 100, field2Width, field2Height);
        jacobThresholdBox.setMaxStringLength(7);
        jacobThresholdBox.setText(Config.jacobThreshold == null ? "" : Config.jacobThreshold);
        jacobThresholdBox.setFocused(false);

        this.buttonList.add(new GuiBetterButton(0, this.width/2 - buttonWidth /2 ,  this.height/2 - buttonHeight /2, buttonWidth, buttonHeight, "Resync : " + (Config.resync ? "on" : "off")));
        this.buttonList.add(new GuiBetterButton(1, this.width/2 - buttonWidth /2 ,  this.height/2 - buttonHeight /2 - 70, buttonWidth, buttonHeight, "Autosell : " + (Config.autosell ? "on" : "off")));
        this.buttonList.add(new GuiBetterButton(2, this.width/2 - buttonWidth /2 ,  this.height/2 - buttonHeight /2 - 140, buttonWidth, buttonHeight, "Sell inventory"));


    }


    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);

        //drawRect(0, 0, this.width, this.height, 0x30000000);
        //this.drawDefaultBackground();
        mc.fontRendererObj.drawStringWithShadow("Webhook URL", this.width/2 - 180, this.height / 2 + 60 - 3, -1);
        mc.fontRendererObj.drawStringWithShadow("Jacob's Event limit", this.width/2 - 180, this.height / 2 + 100 - 3, -1);
        draggableProfitGUI.draw(mouseX, mouseY, partialTicks);

        urlTextBox.drawTextBox();
        jacobThresholdBox.drawTextBox();

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame(){
        return false;
    }


    @Override
    protected void actionPerformed(GuiButton button){

        if(button.id == 0){
            Config.resync = !Config.resync;
            buttonList.get(0).displayString = "Resync : " + (Config.resync ? "on" : "off");
        }
        if(button.id == 1){
            Config.autosell = !Config.autosell;
            buttonList.get(1).displayString = "Autosell : " + (Config.autosell ? "on" : "off");
        }
        if(button.id == 2){
            ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
            executorService.execute(CaneHarvester.autoSell);
            executorService.shutdown();
        }

    }
    protected void keyTyped(char par1, int par2)
    {
        try {

            super.keyTyped(par1, par2);
            if(urlTextBox.isFocused())
                urlTextBox.textboxKeyTyped(par1, par2);
            if(jacobThresholdBox.isFocused()) {
                if(Character.isDigit(par1) || par2 == 14) //14 -> delete key code
                jacobThresholdBox.textboxKeyTyped(par1, par2);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }
    public void updateScreen()
    {
        super.updateScreen();
        urlTextBox.updateCursorCounter();
        jacobThresholdBox.updateCursorCounter();
    }
    protected void mouseClicked(int x, int y, int btn) {
        try {
            super.mouseClicked(x, y, btn);
            if(x >= urlTextBox.xPosition && y >= urlTextBox.yPosition && x < urlTextBox.xPosition + urlTextBox.width && y < urlTextBox.yPosition + urlTextBox.height){
                jacobThresholdBox.setFocused(false);
                urlTextBox.mouseClicked(x, y, btn);
            }
            else if(x >= jacobThresholdBox.xPosition && y >= jacobThresholdBox.yPosition && x < jacobThresholdBox.xPosition + jacobThresholdBox.width && y < jacobThresholdBox.yPosition + jacobThresholdBox.height){
                urlTextBox.setFocused(false);
                jacobThresholdBox.mouseClicked(x, y, btn);
            }
            else {
                urlTextBox.setFocused(false);
                jacobThresholdBox.setFocused(false);
            }

        }catch(Exception e){
            e.printStackTrace();
        }
    }
    @Override
    public void onGuiClosed(){
        Config.urlText = urlTextBox.getText().trim();
        Config.jacobThreshold = jacobThresholdBox.getText().trim();
        Config.writeConfig();
    }


}
