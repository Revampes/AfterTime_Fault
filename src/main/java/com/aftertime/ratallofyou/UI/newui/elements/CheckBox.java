package com.aftertime.ratallofyou.UI.newui.elements;

import net.minecraft.client.gui.Gui;

public class CheckBox extends UIElement {
    private boolean checked;
    private String label;
    private Runnable onChange;

    public CheckBox(int x, int y, int width, int height, String label, boolean initialValue, Runnable onChange) {
        super(x, y, width, height);
        this.label = label;
        this.checked = initialValue;
        this.onChange = onChange;
    }

    @Override
    public void draw(int mouseX, int mouseY) {
        if (!visible) return;

        hovered = isMouseOver(mouseX, mouseY);

        // Draw checkbox background
        int bgColor = hovered ? 0xFF555555 : 0xFF444444;
        Gui.drawRect(x, y, x + height, y + height, bgColor);

        // Draw checkmark if checked
        if (checked) {
            Gui.drawRect(x + 3, y + 3, x + height - 3, y + height - 3, 0xFF00FF00);
        }

        // Draw border
        Gui.drawRect(x, y, x + height, y + 1, 0xFF000000);
        Gui.drawRect(x, y + height - 1, x + height, y + height, 0xFF000000);
        Gui.drawRect(x, y, x + 1, y + height, 0xFF000000);
        Gui.drawRect(x + height - 1, y, x + height, y + height, 0xFF000000);

        // Draw label
        int textColor = hovered ? 0xFFFFFFAA : 0xFFFFFFFF;
        fontRenderer.drawString(label, x + height + 5, y + (height - 8) / 2, textColor);
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (!visible || !isMouseOver(mouseX, mouseY) || mouseButton != 0) return false;

        checked = !checked;
        if (onChange != null) onChange.run();
        return true;
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {}

    @Override
    public void keyTyped(char typedChar, int keyCode) {}

    public boolean isChecked() { return checked; }
    public void setChecked(boolean checked) { this.checked = checked; }
}