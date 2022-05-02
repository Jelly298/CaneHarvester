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
    public static GuiMenuComponent webhookTab = new GuiMenuComponent(200, 140, 160, new Color(40, 40, 40).getRGB(), new WebhookConfig(),
            new GuiTitleComponent(25, "Webhook", -1, new Color(0, 0, 0).getRGB()));
    public static GuiMenuComponent jacobTab = new GuiMenuComponent(10, 140, 160, new Color(40, 40, 40).getRGB(), new JacobConfig(),
            new GuiTitleComponent(25, "Jacob", -1, new Color(0, 0, 0).getRGB()));



    public static void init() {
        miscellaneousTab.addButton("Move GUI", 25, new Color(20, 20, 20).getRGB(), -1, new Color(200, 200, 200, 125).getRGB(), () -> Minecraft.getMinecraft().displayGuiScreen(new GUIDragScreen()));
    }
    @Override
    public void initGui() {
        super.initGui();
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
    public void keyTyped(char par1, int par2)
    {
        try {
            if (par2 == Keyboard.KEY_ESCAPE) {
                mc.thePlayer.closeScreen();
            }
        }catch (Exception e){
            e.printStackTrace();
        }


    }
    public void updateScreen()
    {
        super.updateScreen();
    }
}
