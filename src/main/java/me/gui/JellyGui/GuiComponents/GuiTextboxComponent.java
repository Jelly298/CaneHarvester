package me.gui.JellyGui.GuiComponents;

import me.gui.JellyGui.GuiComponents.GuiConfig.GuiStringConfig;
import me.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

public class GuiTextboxComponent implements IGuiButtonComponent, IGuiSettingsComponent{

    //copyright belongs to 𝓙𝓮𝓵𝓵𝔂𝓛𝓪𝓫
    public static final float textboxLineBuffer = 0.3f;

    private GuiTextField textField;
    private String text;
    private int totalHeight;
    private int textboxWidth;
    private int defaultColor;
    private String configKey;
    private int lastKeyCode = 0;
    boolean pressed;


    public GuiTextboxComponent(int totalHeight, int defaultColor, int textboxWdith, String text, String configKey) {
        this.totalHeight = totalHeight;
        this.defaultColor = defaultColor;
        this.text = text;
        this.configKey = configKey;
        this.textboxWidth = textboxWdith;
    }


    @Override
    public int getHeight() {
        return totalHeight;
    }

    @Override
    public void draw(int baseX, int baseY, int totalWidth) {

        if(text != null) {

            Utils.drawString(text, baseX + 5, baseY + Utils.getVerticalOffset(totalHeight/2, textboxLineBuffer),
                    Utils.getStringSizeFromHeight(totalHeight/2, textboxLineBuffer), defaultColor);

            if(textField == null){
                textField = new GuiTextField(0, Minecraft.getMinecraft().fontRendererObj, baseX + 5,
                        baseY + totalHeight/2, (int) (totalWidth * 0.7f), (int) (totalHeight * 0.5f * 0.7f));
                textField.setMaxStringLength(256);
                textField.setText(GuiStringConfig.getConfigString(configKey));
                textField.setFocused(false);
            }

            textField.xPosition = (baseX + 5);
            textField.yPosition = baseY + totalHeight/2;
            textField.drawTextBox();
        }

    }

    @Override
    public String getText() {
        return null;
    }


    void keyTyped(char par1, int par2){
        textField.textboxKeyTyped(par1, par2);
        GuiStringConfig.setConfigString(configKey, textField.getText());
    }


    @Override
    public String getConfigKey() {
        return configKey;
    }


    @Override
    public void updateCursor(int mouseX, int mouseY) {
        boolean hovered;
        hovered = (mouseX >= textField.xPosition && mouseY >= textField.yPosition &&
                mouseX < (textField.xPosition + textField.width) && mouseY < textField.yPosition + textField.height);

        if(Mouse.isButtonDown(0) && hovered){
            if(textField.getText().equals("paste here"))
                textField.setText("");
            textField.setFocused(true);
        } else if(Mouse.isButtonDown(0) && !hovered){
            textField.setFocused(false);
        }

        if (Keyboard.getEventKeyState())
        {
            if(lastKeyCode == Keyboard.getEventKey()) {
                if(!pressed) {
                    this.keyTyped(Keyboard.getEventCharacter(), Keyboard.getEventKey());
                    pressed = true;
                }
            } else {
                this.keyTyped(Keyboard.getEventCharacter(), Keyboard.getEventKey());
                pressed = true;

            }
            lastKeyCode = Keyboard.getEventKey();

        } else
            pressed = false;

        Minecraft.getMinecraft().dispatchKeypresses();

    }
}
