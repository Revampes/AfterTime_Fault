package com.aftertime.ratallofyou.UI.newui.elements;

import net.minecraft.client.gui.Gui;
import java.util.function.Consumer;

public class Slider extends UIElement {
    private float value;
    private float minValue, maxValue;
    private String label;
    private boolean dragging = false;
    private Consumer<Float> onChange;

    public Slider(int x, int y, int width, int height, String label, float minValue, float maxValue, float initialValue) {
        super(x, y, width, height);
        this.label = label;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.value = initialValue;
    }

    @Override
    public void draw(int mouseX, int mouseY) {
        if (!visible) return;

        // Update while dragging for smooth changes
        if (dragging) {
            updateValue(mouseX);
        }

        hovered = isMouseOver(mouseX, mouseY);

        // Draw background
        Gui.drawRect(x, y, x + width, y + height, 0xFF444444);

        // Draw filled portion
        float percentage = (value - minValue) / (maxValue - minValue);
        int fillWidth = (int)(width * percentage);
        Gui.drawRect(x, y, x + fillWidth, y + height, 0xFF6666FF);

        // Draw label and value
        String displayText = label + ": " + (int)value;
        int textX = x + (width - fontRenderer.getStringWidth(displayText)) / 2;
        int textY = y + (height - 8) / 2;
        fontRenderer.drawString(displayText, textX, textY, 0xFFFFFFFF);

        // Draw handle
        int handleX = x + fillWidth - 2;
        Gui.drawRect(handleX, y - 2, handleX + 4, y + height + 2, 0xFFFFFFFF);
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (!visible || !isMouseOver(mouseX, mouseY)) return false;

        if (mouseButton == 0) {
            dragging = true;
            updateValue(mouseX);
            return true;
        }
        return false;
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        if (dragging) {
            updateValue(mouseX);
        }
        dragging = false;
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {}

    public void updateValue(int mouseX) {
        float prev = this.value;
        float newValue = minValue + ((mouseX - x) / (float)width) * (maxValue - minValue);
        value = Math.max(minValue, Math.min(maxValue, newValue));
        if (onChange != null && Math.abs(prev - value) > 0.001f) {
            onChange.accept(value);
        }
    }

    public float getValue() { return value; }
    public void setValue(float value) { this.value = value; }

    public void setOnChange(Consumer<Float> onChange) { this.onChange = onChange; }
}