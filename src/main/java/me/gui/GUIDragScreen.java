package me.gui;

import me.CaneHarvester;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

public class GUIDragScreen extends GuiScreen {

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);

        Minecraft.getMinecraft().fontRendererObj.drawString("Drag the GUI Around",
                width/2 - fontRendererObj.getStringWidth("Drag the GUI Around")/2, height/2 - 3, -1);
        drawDefaultBackground();

        CaneHarvester.profitGUI.draw(mouseX, mouseY, partialTicks);
    }
}
