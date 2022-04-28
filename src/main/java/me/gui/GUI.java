package me.gui;

import me.config.configTypes.CoreConfig;
import me.config.configTypes.JacobConfig;
import me.config.configTypes.MiscellaneousConfig;
import me.config.configTypes.WebhookConfig;
import me.gui.JellyGui.GuiComponents.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import org.lwjgl.input.Keyboard;

import java.awt.*;

public class GUI extends GuiScreen {

   // public static GuiMenuComponent DraggableProfitGUI = new GuiMenuComponent(0, 2, 140, 77, new Color(0, 0, 0, 100).getRGB(), new JacobConfig(), null);
    public static GuiMenuComponent miscellaneousTab = new GuiMenuComponent(200, 10, 160, new Color(40, 40, 40).getRGB(), new MiscellaneousConfig(),
           new GuiTitleComponent(25, "Miscellaneous", -1, new Color(0, 0, 0).getRGB()));
    public static GuiMenuComponent coreTab = new GuiMenuComponent(10, 10, 160, new Color(40, 40, 40).getRGB(), new CoreConfig(),
            new GuiTitleComponent(25, "Core", -1, new Color(0, 0, 0).getRGB()));
    public static GuiMenuComponent webhookTab = new GuiMenuComponent(200, 200, 160, new Color(40, 40, 40).getRGB(), new WebhookConfig(),
            new GuiTitleComponent(25, "Webhook", -1, new Color(0, 0, 0).getRGB()));
    public static GuiMenuComponent jacobTab = new GuiMenuComponent(10, 180, 160, new Color(40, 40, 40).getRGB(), new JacobConfig(),
            new GuiTitleComponent(25, "Jacob", -1, new Color(0, 0, 0).getRGB()));



    public static void init() {
        miscellaneousTab.addButton("Move GUI", 25, new Color(20, 20, 20).getRGB(), -1, new Color(200, 200, 200, 125).getRGB(), () -> Minecraft.getMinecraft().displayGuiScreen(new GUIDragScreen()));
    }
    @Override
    public void initGui() {


        super.initGui();



        //GuiScreenComponent.initGui(); // will be called from mixin later
      //  webhookTab.setTitle(new GuiTitleComponent(30, "Webhook", -1, new Color(0, 0, 0).getRGB()));

   //0xa9a9a9 0x


    }



    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);

        miscellaneousTab.draw(mouseX, mouseY, partialTicks);
        coreTab.draw(mouseX, mouseY, partialTicks);
        webhookTab.draw(mouseX, mouseY, partialTicks);
        jacobTab.draw(mouseX, mouseY, partialTicks);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame(){
        return false;
    }


    @Override
    protected void actionPerformed(GuiButton button){


    }

    @Override
    public void onGuiClosed(){
       // GuiScreenComponent.onGuiClosed(); // will be called from mixin later

    }

    public void keyTyped(char par1, int par2)
    {
        try {
            if (par2 == Keyboard.KEY_ESCAPE) {
                mc.thePlayer.closeScreen();
            }
            //GuiScreenComponent.keyTyped(par1, par2); // will be called from mixin later
        }catch (Exception e){
            e.printStackTrace();
        }


    }
    public void updateScreen()
    {
        super.updateScreen();
        //GuiScreenComponent.updateScreen(); // will be called from mixin later
    }
   public void mouseClicked(int x, int y, int btn) {
        try {
           // GuiScreenComponent.mouseClicked(x, y, btn); // will be called from mixin later

        }catch(Exception e){
            e.printStackTrace();
        }
    }



}
