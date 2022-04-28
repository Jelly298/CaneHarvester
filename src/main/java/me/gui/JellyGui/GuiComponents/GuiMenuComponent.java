package me.gui.JellyGui.GuiComponents;

import me.config.Config;
import me.config.configTypes.IConfigType;
import me.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GuiMenuComponent {
    private int x;
    private int y;
    private int width;
    private int bgcolor;
    private int lastX;
    private int lastY;
    protected List<IGuiComponent> components = new ArrayList<>();
    private GuiTitleComponent guiTitle;
    private IConfigType configMenu;
    private boolean dragging;
    public static final float titleBuffer = 0.3f;
    public static final float lineBuffer = 0.5f;
    public static final float checkboxBuffer = 0.5f;
    public static final float checkboxTextBuffer = 0.3f;
    public static final float buttonBuffer = 0.5f;
    public static final int defaultHeight = 30;


    public GuiMenuComponent(int x, int y, int width, int bgcolor, IConfigType corrMenu, GuiTitleComponent guiTitle) {
        this.width = width;
        this.x = x;
        this.y = y;
        this.bgcolor = bgcolor;
        this.guiTitle = guiTitle;
        if(corrMenu != null)
        addComponents(corrMenu);
    }
    void addComponents(IConfigType configMenu){

        configMenu.setValue(Config.config);
        for(int i = 0; i < configMenu.getConfigValueList().size(); i++){
            if(configMenu.getConfigValueList().get(i) instanceof Boolean){
                components.add(new GuiCheckboxComponent(configMenu.getConfigPairList().get(i).getConfigName(), 30, -1,
                        new Color(20, 20, 20).getRGB(), new Color(52, 210, 52, 190).getRGB(), configMenu.getConfigPairList().get(i).getConfigID()));
            } else
            if(configMenu.getConfigValueList().get(i) instanceof String){
                components.add(new GuiTextboxComponent(defaultHeight * 2, -1, (int) (width * 0.7f), configMenu.getConfigPairList().get(i).getConfigName(), configMenu.getConfigPairList().get(i).getConfigID()));
            } else
            if(configMenu.getConfigValueList().get(i) instanceof Integer || configMenu.getConfigValueList().get(i) instanceof Long){
                components.add(new GuiTextboxComponent(defaultHeight * 2, -1, (int) (width * 0.7f), configMenu.getConfigPairList().get(i).getConfigName(), configMenu.getConfigPairList().get(i).getConfigID()));
            }
        }
    }
    public void draw(int mouseX, int mouseY, float partialTicks){
        if(Minecraft.getMinecraft().currentScreen != null) {
            draggingFix(mouseX, mouseY);

            boolean mouseOverX = (mouseX >= x && mouseX <= this.x + width);
            boolean mouseOverY;
            // System.out.println(mouseX);
            if (guiTitle != null)
                mouseOverY = (mouseY >= y && mouseY <= this.y + guiTitle.height);
            else
                mouseOverY = (mouseY >= y && mouseY <= this.y + getTotalHeight());
            if (mouseOverX && mouseOverY) {
                if (Mouse.isButtonDown(0)) {
                    if (!this.dragging) {
                        this.lastX = x - mouseX;
                        this.lastY = y - mouseY;
                        this.dragging = true;
                    }
                }
            }
        }
        int stackedHeight = 0;
        Gui.drawRect(x, y, x + width, y + getTotalHeight(), bgcolor);
        if (guiTitle != null)
            Gui.drawRect(x, y, x + width, y + guiTitle.height, guiTitle.bgColor);
        if (components != null) {
            if (guiTitle != null) {
                Utils.drawString(guiTitle.text, x + width/2 - Utils.getStringSize(guiTitle.text, Utils.getStringSizeFromHeight(guiTitle.height, titleBuffer))/2, y + Utils.getVerticalOffset(guiTitle.height, titleBuffer),
                        Utils.getStringSizeFromHeight(guiTitle.height, titleBuffer), guiTitle.defaultColor);
                stackedHeight += guiTitle.height;
            }
            for (IGuiComponent line : components) {
                if (line != null) {
                    line.draw(x, y + stackedHeight, width);
                    if(line instanceof IGuiButtonComponent)
                        ((IGuiButtonComponent) line).updateCursor(mouseX, mouseY);
                    stackedHeight += line.getHeight();
                }
            }
        }

    }
    public void draw(){
        draw(0, 0, 0);
    }
    public void addButton(String text, int height, int bgColor, int textColor, int hoveredColor, Runnable method){
        components.add(new GuiButtonComponent(text, height, bgColor, textColor, hoveredColor, method));
    }
    private int getTotalHeight(){
        try {
            if (components != null) {
                int height = 0;
                if(guiTitle != null)
                    height += guiTitle.height;
                for(IGuiComponent guiComponent : components)
                    height += guiComponent.getHeight();
                return height;
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return 0;
    }

    private void draggingFix(int mouseX, int mouseY) {
        if (this.dragging) {
            this.x = mouseX + this.lastX;
            this.y = mouseY + this.lastY;
            if(!Mouse.isButtonDown(0)) this.dragging = false;
        }
    }

    public void setLine(GuiLineComponent line, int index){
        try{
            components.set(index, line);
        } catch (Exception e){
            e.printStackTrace();
            components.add(line);
        }
    }
}
