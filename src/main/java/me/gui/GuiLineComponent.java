package me.gui;

public class GuiLineComponent {
    int relativeX;
    int relativeY;
    String text;
    float size;
    int defaultColor;

    public GuiLineComponent(int relativeX, int relativeY, String text, int defaultColor, float size) {
        this.relativeX = relativeX;
        this.relativeY = relativeY;
        this.text = text;
        this.defaultColor = defaultColor;
        this.size = size;
    }
}
