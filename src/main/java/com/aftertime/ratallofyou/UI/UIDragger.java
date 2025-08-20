package com.aftertime.ratallofyou.UI;

import com.aftertime.ratallofyou.UI.config.ConfigData.AllConfig;
import com.aftertime.ratallofyou.UI.config.ConfigData.UIPosition;
import com.aftertime.ratallofyou.UI.config.PropertyRef;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.lwjgl.input.Mouse;

public class UIDragger {
    private static final UIDragger INSTANCE = new UIDragger();
    private boolean isDragging = false;
    private UIPosition currentlyDragging = null;
    private int dragOffsetX, dragOffsetY;

    public static UIDragger getInstance() {
        return INSTANCE;
    }

    public UIPosition getDraggedElement() {
        return currentlyDragging;
    }

    public boolean tryStartDrag(UIPosition pos, int mouseX, int mouseY) {
        if (pos == null) return false;

        int elementWidth = 30;
        int elementHeight = 50;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.currentScreen != null) {
            ScaledResolution res = new ScaledResolution(mc);
            mouseX = Mouse.getX() * res.getScaledWidth() / mc.displayWidth;
            mouseY = res.getScaledHeight() - Mouse.getY() * res.getScaledHeight() / mc.displayHeight - 1;
        }

        if (mouseX >= pos.x && mouseX <= pos.x + elementWidth &&
                mouseY >= pos.y && mouseY <= pos.y + elementHeight) {
            currentlyDragging = pos;
            dragOffsetX = mouseX - pos.x;
            dragOffsetY = mouseY - pos.y;
            isDragging = true;
            return true;
        }
        return false;
    }

    public void updateDragPosition(int mouseX, int mouseY) {
        if (isDragging && currentlyDragging != null) {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc.currentScreen != null) {
                ScaledResolution res = new ScaledResolution(mc);
                mouseX = Mouse.getX() * res.getScaledWidth() / mc.displayWidth;
                mouseY = res.getScaledHeight() - Mouse.getY() * res.getScaledHeight() / mc.displayHeight - 1;
            }

            currentlyDragging.x = mouseX - dragOffsetX;
            currentlyDragging.y = mouseY - dragOffsetY;

            ScaledResolution res = new ScaledResolution(mc);
            currentlyDragging.x = Math.max(0, Math.min(currentlyDragging.x, res.getScaledWidth() - 30));
            currentlyDragging.y = Math.max(0, Math.min(currentlyDragging.y, res.getScaledHeight() - 50));
        }
    }

    public void updatePositions() {
        isDragging = false;
        currentlyDragging = null;
    }



//    public UIPosition getPosition(String moduleName) {
//        return elements.get(moduleName);
//    }

    public boolean isDragging() {
        return isDragging;
    }





}