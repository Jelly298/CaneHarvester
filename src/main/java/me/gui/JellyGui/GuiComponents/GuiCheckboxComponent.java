package me.gui.JellyGui.GuiComponents;

import me.gui.JellyGui.GuiComponents.GuiConfig.GuiBooleanConfig;
import me.utils.Utils;
import net.minecraft.client.gui.Gui;
import org.lwjgl.input.Mouse;

public class GuiCheckboxComponent implements IGuiSettingsComponent, IGuiButtonComponent{

    final float checkboxTextBuffer = 0.3f;
    final float checkboxBuffer = 0.5f;
    String text;
    int defaultColor;
    int height;
    float length;
    int checkboxColor;
    int checkmarkColor;
    boolean clicked = false;
    boolean enabled = false;

    int buttonX;
    int buttonY;
    String configKey;


    public GuiCheckboxComponent(String text,  int height, int defaultColor, int checkboxColor, int checkmarkColor, String configKey) {
        this.text = text;
        this.defaultColor = defaultColor;
        this.height = height;
        this.checkboxColor = checkboxColor;
        this.checkmarkColor = checkmarkColor;
        this.configKey = configKey;
        length = height * GuiMenuComponent.checkboxBuffer;

    }
    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public void draw(int baseX, int baseY, int totalWidth) {

        if(text != null)
            Utils.drawString(text, baseX + 5, baseY + Utils.getVerticalOffset(height, checkboxTextBuffer), Utils.getStringSizeFromHeight(height, checkboxTextBuffer), defaultColor);

        buttonX = (int) (baseX + totalWidth - length - 7);
        buttonY = (int) (baseY + height/ 2 - height * checkboxBuffer / 2);
        Gui.drawRect(buttonX, buttonY, (int) (buttonX + length), (int)(buttonY + length), checkboxColor);
        Gui.drawRect(buttonX, buttonY, (int) (buttonX + length), (int)(buttonY + length), GuiBooleanConfig.getConfigValue(configKey) ? checkmarkColor : checkboxColor);

    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public String getConfigKey() {
        return configKey;
    }

    @Override
    public void updateCursor(int mouseX, int mouseY) {
        if(text != null){
            boolean hovered = false;
            hovered = (mouseX >= buttonX && mouseY >= buttonY &&
                    mouseX < (buttonX + length) && mouseY < (buttonY + length));

            if(Mouse.isButtonDown(0) && hovered && !clicked){
                clicked = true;
                GuiBooleanConfig.switchConfigValue(configKey);
            } else if(!Mouse.isButtonDown(0)){
                clicked = false;
            }

        }
    }
}
