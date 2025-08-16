package com.aftertime.ratallofyou.UI;

import com.aftertime.ratallofyou.UI.config.ConfigStorage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.lwjgl.input.Mouse;

public class UIDragger {
    private static final UIDragger INSTANCE = new UIDragger();
    private final Map<String, UIPosition> elements = new HashMap<String, UIPosition>();
    private boolean isDragging = false;
    private String currentlyDragging = null;
    private int dragOffsetX, dragOffsetY;

    public static UIDragger getInstance() {
        return INSTANCE;
    }

    public String getDraggedElement() {
        return currentlyDragging;
    }

    public boolean tryStartDrag(String moduleName, int mouseX, int mouseY) {
        UIPosition pos = elements.get(moduleName);
        if (pos == null) return false;

        int elementWidth = getElementWidth(moduleName);
        int elementHeight = getElementHeight(moduleName);

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.currentScreen != null) {
            ScaledResolution res = new ScaledResolution(mc);
            mouseX = Mouse.getX() * res.getScaledWidth() / mc.displayWidth;
            mouseY = res.getScaledHeight() - Mouse.getY() * res.getScaledHeight() / mc.displayHeight - 1;
        }

        if (mouseX >= pos.x && mouseX <= pos.x + elementWidth &&
                mouseY >= pos.y && mouseY <= pos.y + elementHeight) {
            currentlyDragging = moduleName;
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

            UIPosition pos = elements.get(currentlyDragging);
            pos.x = mouseX - dragOffsetX;
            pos.y = mouseY - dragOffsetY;

            ScaledResolution res = new ScaledResolution(mc);
            pos.x = Math.max(0, Math.min(pos.x, res.getScaledWidth() - getElementWidth(currentlyDragging)));
            pos.y = Math.max(0, Math.min(pos.y, res.getScaledHeight() - getElementHeight(currentlyDragging)));
        }
    }

    public void updatePositions() {
        isDragging = false;
        currentlyDragging = null;
        savePositions();
    }

    public void registerElement(String moduleName, int defaultX, int defaultY) {
        if (!elements.containsKey(moduleName)) {
            elements.put(moduleName, new UIPosition(defaultX, defaultY));
        }
    }

    public UIPosition getPosition(String moduleName) {
        return elements.get(moduleName);
    }

    public Map<String, UIPosition> getAllElements() {
        return Collections.unmodifiableMap(elements);
    }

    public boolean isDragging() {
        return isDragging;
    }

    public int getElementWidth(String moduleName) {
        if ("Phase 3 Tick Timer".equals(moduleName)) return 50;
        if ("Invincible Timer".equals(moduleName)) return 80;
        return 60;
    }

    public int getElementHeight(String moduleName) {
        if ("Phase 3 Tick Timer".equals(moduleName)) return 10;
        if ("Invincible Timer".equals(moduleName)) return 30;
        return 15;
    }

    public static class UIPosition {
        public int x, y;

        public UIPosition(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    public void savePositions() {
        ConfigStorage.savePositionsConfig();
    }

    public void loadPositions() {
        ConfigStorage.loadPositionsConfig();
    }
}