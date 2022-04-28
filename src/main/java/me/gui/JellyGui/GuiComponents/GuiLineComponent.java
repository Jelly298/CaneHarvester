package me.gui.JellyGui.GuiComponents;

import me.utils.Utils;

public class GuiLineComponent implements IGuiComponent{

    public static final float lineBuffer = 0.5f;
    String text;
    int defaultColor;
    int height;

    public GuiLineComponent(int height, String text, int defaultColor) {
        this.height = height;
        this.text = text;
        this.defaultColor = defaultColor;
    }

    @Override
    public int getHeight() {
        return height;
    }
    @Override
    public String getText() {
        return text;
    }

    @Override
    public void draw(int baseX, int baseY, int totalWidth){
        if(text != null)
            Utils.drawString(text, baseX + 5, baseY + Utils.getVerticalOffset(height, lineBuffer), Utils.getStringSizeFromHeight(height, lineBuffer), defaultColor);

    }
}
