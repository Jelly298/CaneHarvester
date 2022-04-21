package me.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import org.lwjgl.input.Mouse;

import java.util.List;

public class GuiDraggableComponent{
    private int x;
    private int y;
    private int width;
    private int height;
    private int bgcolor;
    private int lastX;
    private int lastY;
    private List<GuiLineComponent> displayStrings;
    private boolean dragging;

    public GuiDraggableComponent(int x, int y, int width, int height, int bgcolor, List<GuiLineComponent> displayStrings) {
        this.width = width;
        this.height = height;
        this.x = x;
        this.y = y;
        this.bgcolor = bgcolor;
        this.displayStrings = displayStrings;

    }
    public GuiDraggableComponent(int x, int y, int width, int height, int bgcolor){
            this.width = width;
            this.height = height;
            this.x = x;
            this.y = y;
            this.bgcolor = bgcolor;
    }

    public void draw(int mouseX, int mouseY, float partialTicks){
        draggingFix(mouseX, mouseY);
        Gui.drawRect(x, y, x + width, y + height, bgcolor);
        if(displayStrings != null) {
            for (GuiLineComponent line : displayStrings) {
                if(line != null)
                Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(line.text, x + line.relativeX, y + line.relativeY, line.defaultColor);
            }
        }
        boolean mouseOverX = (mouseX >= x && mouseX <= this.x +width);
        boolean mouseOverY = (mouseY >= y && mouseY <= this.y+height);
        if(mouseOverX && mouseOverY){
            if(Mouse.isButtonDown(0)){
                if (!this.dragging) {
                    this.lastX = x - mouseX;
                    this.lastY = y - mouseY;
                    this.dragging = true;
                }
            }
        }
    }
    public void draw(){
        Gui.drawRect(x, y, x + width, y + height, bgcolor);
        if(displayStrings != null) {
            for (GuiLineComponent line : displayStrings) {
                if(line != null)
                    Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(line.text, x + line.relativeX, y + line.relativeY, line.defaultColor);
            }
        }
    }

    public void setLine(GuiLineComponent line, int index) {
        displayStrings.set(index, line);
    }

    public void addLine(GuiLineComponent line) {
        displayStrings.add(line);
    }

    private void draggingFix(int mouseX, int mouseY) {
        if (this.dragging) {
            this.x = mouseX + this.lastX;
            this.y = mouseY + this.lastY;
            if(!Mouse.isButtonDown(0)) this.dragging = false;
        }
    }
}
