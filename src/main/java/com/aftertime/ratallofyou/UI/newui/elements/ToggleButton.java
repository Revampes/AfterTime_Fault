package com.aftertime.ratallofyou.UI.newui.elements;

import net.minecraft.client.gui.Gui;

public class ToggleButton extends UIElement {
    private boolean toggled;
    private String label;
    private String description;
    private Runnable onToggle;

    public ToggleButton(int x, int y, int width, int height, String label, String description, boolean initialValue, Runnable onToggle) {
        super(x, y, width, height);
        this.label = label;
        this.description = description;
        this.toggled = initialValue;
        this.onToggle = onToggle;
    }

    @Override
    public void draw(int mouseX, int mouseY) {
        if (!visible) return;

        hovered = isMouseOver(mouseX, mouseY);

        // Draw button background - Aqua blue when enabled, grey when disabled
        int color;
        if (toggled) {
            color = hovered ? 0xFF20B2AA : 0xFF008080; // Aqua colors - darker on hover
        } else {
            color = hovered ? 0xFF666666 : 0xFF444444; // Grey colors
        }

        Gui.drawRect(x, y, x + width, y + height, color);

        // Draw border
        int borderColor = hovered ? 0xFFFFFF00 : 0xFF000000;
        Gui.drawRect(x, y, x + width, y + 1, borderColor);
        Gui.drawRect(x, y + height - 1, x + width, y + height, borderColor);
        Gui.drawRect(x, y, x + 1, y + height, borderColor);
        Gui.drawRect(x + width - 1, y, x + width, y + height, borderColor);

        // Draw label
        int textColor = toggled ? 0xFFFFFFFF : 0xFFCCCCCC; // White when enabled, light grey when disabled
        int textX = x + (width - fontRenderer.getStringWidth(label)) / 2;
        int textY = y + (height - 8) / 2;
        fontRenderer.drawString(label, textX, textY, textColor);
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (!visible || !isMouseOver(mouseX, mouseY) || mouseButton != 0) return false;

        toggled = !toggled;
        if (onToggle != null) onToggle.run();
        return true;
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {}

    @Override
    public void keyTyped(char typedChar, int keyCode) {}

    public boolean isToggled() { return toggled; }
    public void setToggled(boolean toggled) { this.toggled = toggled; }
    public String getDescription() { return description; }

    // New: allow binding or updating the toggle callback from outside
    public void setOnToggle(Runnable onToggle) {
        this.onToggle = onToggle;
    }
}