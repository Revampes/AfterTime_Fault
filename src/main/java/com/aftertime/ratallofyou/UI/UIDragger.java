package com.aftertime.ratallofyou.UI;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
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
        Properties props = new Properties();
        File positionFile = new File("config/ratallofyou_positions.cfg");

        for (Map.Entry<String, UIPosition> entry : elements.entrySet()) {
            String key = entry.getKey().replace(" ", "_").toLowerCase();
            props.setProperty(key + "_x", String.valueOf(entry.getValue().x));
            props.setProperty(key + "_y", String.valueOf(entry.getValue().y));
        }

        FileOutputStream output = null;
        try {
            output = new FileOutputStream(positionFile);
            props.store(output, "UI Positions Configuration");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void loadPositions() {
        File positionFile = new File("config/ratallofyou_positions.cfg");
        if (!positionFile.exists()) return;

        Properties props = new Properties();
        FileInputStream input = null;
        try {
            input = new FileInputStream(positionFile);
            props.load(input);
            for (String moduleName : elements.keySet()) {
                String key = moduleName.replace(" ", "_").toLowerCase();
                try {
                    int x = Integer.parseInt(props.getProperty(key + "_x", String.valueOf(elements.get(moduleName).x)));
                    int y = Integer.parseInt(props.getProperty(key + "_y", String.valueOf(elements.get(moduleName).y)));
                    elements.put(moduleName, new UIPosition(x, y));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}