package me.gui;

import me.config.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;

public class GUI extends GuiScreen {


    int buttonWidth = 85;
    int buttonHeight = 65;

    int fieldWidth = 200;
    int fieldHeight = 20;

    private GuiTextField urlTextBox;


    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {



    }
    @Override
    public void initGui() {
        super.initGui();

        urlTextBox = new GuiTextField(1, Minecraft.getMinecraft().fontRendererObj, this.width/2 - fieldWidth / 2 + 30 , this.height / 2 - fieldHeight / 2 + 60, fieldWidth, fieldHeight);
        urlTextBox.setMaxStringLength(256);
        urlTextBox.setText(Config.urlText);
        urlTextBox.setFocused(true);
        this.buttonList.add(new GuiBetterButton(0, this.width/2 - buttonWidth /2 ,  this.height/2 - buttonHeight /2, buttonWidth, buttonHeight, "Resync : " + (Config.resync ? "on" : "off")));


    }


    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);

        drawRect(0, 0, this.width, this.height, 0x30000000);
        this.drawDefaultBackground();
        mc.fontRendererObj.drawStringWithShadow("Webhook URL", this.width/2 - fontRendererObj.getStringWidth("Webhook URL") / 2 -  120, this.height / 2 - fontRendererObj.getStringWidth("URL")/ 2 + 60 + fieldHeight / 4, -1);
        urlTextBox.drawTextBox();
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

    }
    protected void keyTyped(char par1, int par2)
    {
        try {
            super.keyTyped(par1, par2);
            urlTextBox.textboxKeyTyped(par1, par2);
        }catch (Exception e){
            e.printStackTrace();
        }

    }
    public void updateScreen()
    {
        super.updateScreen();
        urlTextBox.updateCursorCounter();
    }
    protected void mouseClicked(int x, int y, int btn) {
        try {
            super.mouseClicked(x, y, btn);
            urlTextBox.mouseClicked(x, y, btn);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    @Override
    public void onGuiClosed(){
        Config.urlText = urlTextBox.getText();
        Config.writeConfig();
    }


}
