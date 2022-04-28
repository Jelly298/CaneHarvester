package me.gui.JellyGui.GuiComponents;

public class GuiTitleComponent{
    int height;
    String text;
    int bgColor;
    int defaultColor;


    public GuiTitleComponent(int height, String text, int defaultColor, int bgColor) {
        this.bgColor = bgColor;
        this.height = height;
        this.text = text;
        this.defaultColor = defaultColor;
    }

}
