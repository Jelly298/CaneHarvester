package me.gui.JellyGui.GuiComponents;

public interface IGuiComponent {
    int getHeight();
    void draw(int baseX, int baseY, int totalWidth);
    String getText();
}
