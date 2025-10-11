package com.aftertime.ratallofyou.UI.newui.elements;

import net.minecraft.client.gui.Gui;
import java.awt.Color;

public class ColorPicker extends UIElement {
    private Color color;
    private String title;
    private boolean picking = false;
    private Runnable onChange;

    public ColorPicker(int x, int y, int width, int height, String title, Color initialColor, Runnable onChange) {
        super(x, y, width, height);
        this.title = title;
        this.color = initialColor;
        this.onChange = onChange;
    }

    @Override
    public void draw(int mouseX, int mouseY) {
        if (!visible) return;

        hovered = isMouseOver(mouseX, mouseY);

        // Draw title ABOVE the color box to prevent overlap
        fontRenderer.drawString(title, x, y - 10, 0xFFFFFFFF);

        // Draw color preview
        int mcColor = (color.getAlpha() << 24) | (color.getRed() << 16) | (color.getGreen() << 8) | color.getBlue();
        Gui.drawRect(x, y, x + width, y + height, mcColor);

        // Draw border
        int borderColor = hovered ? 0xFFFFFF00 : 0xFF000000;
        Gui.drawRect(x, y, x + width, y + 1, borderColor);
        Gui.drawRect(x, y + height - 1, x + width, y + height, borderColor);
        Gui.drawRect(x, y, x + 1, y + height, borderColor);
        Gui.drawRect(x + width - 1, y, x + width, y + height, borderColor);
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (!visible || !isMouseOver(mouseX, mouseY)) return false;

        if (mouseButton == 0) {
            picking = true;
            // In a real implementation, you'd open a color picker dialog here
            openColorPickerDialog();
            return true;
        }
        return false;
    }

    private void openColorPickerDialog() {
        // This would open a proper color picker
        // For now, we'll just cycle through some colors for demonstration
        int r = (color.getRed() + 64) % 256;
        int g = (color.getGreen() + 64) % 256;
        int b = (color.getBlue() + 64) % 256;
        setColor(new Color(r, g, b, color.getAlpha()));
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        picking = false;
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {}

    public Color getColor() { return color; }
    public void setColor(Color color) {
        this.color = color;
        if (onChange != null) onChange.run();
    }

    public int getMCColor() {
        return (color.getAlpha() << 24) | (color.getRed() << 16) | (color.getGreen() << 8) | color.getBlue();
    }

    // Reserve space above for the title text
    @Override
    public int getTopPadding() {
        return 12;
    }
}