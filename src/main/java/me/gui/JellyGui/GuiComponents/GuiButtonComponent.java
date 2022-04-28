package me.gui.JellyGui.GuiComponents;

import me.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import org.lwjgl.input.Mouse;

public class GuiButtonComponent implements IGuiButtonComponent{
    String text;
    int bgColor;
    int hoveredColor;
    int textColor;
    int height;
    boolean clicked = false;
    float buttonX;
    float buttonY;
    float buttonWidth;
    float buttonHeight;
    boolean hovered = false;
    public final static float buttonWidthBuffer = 0.8f;
    public final static float buttonHeightBuffer = 0.8f;
    public final static float buttonTextBuffer = 0.35f;

    Runnable method;

    public GuiButtonComponent(String text, int height, int bgColor, int textColor, int hoveredColor, Runnable method) {
        this.text = text;
        this.height = height;
        this.bgColor = bgColor;
        this.textColor = textColor;
        this.hoveredColor = hoveredColor;
        this.method = method;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public void draw(int baseX, int baseY, int totalWidth) {
        if(text != null) {
            buttonX = (int) (baseX + totalWidth - totalWidth * buttonWidthBuffer);
            buttonY = (int) (baseY + height - height * buttonHeightBuffer);
            buttonWidth = 2 * totalWidth * buttonWidthBuffer - totalWidth;
            buttonHeight = 2 * height * buttonHeightBuffer - height;
            float size = Utils.getStringSizeFromHeight(height, buttonTextBuffer);
            Gui.drawRect((int)buttonX, (int)buttonY, (int)(buttonX + buttonWidth), (int)(buttonY + buttonHeight), hovered ? hoveredColor : bgColor);
            Utils.drawString(text, baseX + totalWidth/2 - (Minecraft.getMinecraft().fontRendererObj.getStringWidth(text) * size) / 2,
                    baseY + Utils.getVerticalOffset(height, buttonTextBuffer), size, textColor);

        }
    }

    @Override
    public String getText() {
        return text;
    }


    @Override
    public void updateCursor(int mouseX, int mouseY) {
        if(text != null){

            hovered = (mouseX >= buttonX && mouseY >= buttonY &&
                    mouseX < (buttonX + buttonWidth) && mouseY < (buttonY + buttonHeight));

            if(Mouse.isButtonDown(0) && hovered && !clicked){
                clicked = true;
                method.run();
            } else if(!Mouse.isButtonDown(0)){
                clicked = false;
            }

        }
    }
}
