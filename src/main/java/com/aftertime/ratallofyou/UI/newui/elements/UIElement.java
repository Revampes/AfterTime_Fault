package com.aftertime.ratallofyou.UI.newui.elements;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

public abstract class UIElement {
    public Minecraft mc;
    public FontRenderer fontRenderer;
    protected int x, y, width, height;
    protected boolean visible = true;
    protected boolean hovered = false;

    public UIElement(int x, int y, int width, int height) {
        this.mc = Minecraft.getMinecraft();
        this.fontRenderer = mc.fontRendererObj;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public abstract void draw(int mouseX, int mouseY);
    public abstract boolean mouseClicked(int mouseX, int mouseY, int mouseButton);
    public abstract void mouseReleased(int mouseX, int mouseY, int mouseButton);
    public abstract void keyTyped(char typedChar, int keyCode);

    public boolean isMouseOver(int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    // Default: no overlay drawing. Elements with popups/overlays can override.
    public void drawOverlay(int mouseX, int mouseY) { /* no-op by default */ }

    // Default: no overlay click handling. Elements with popups/overlays can override.
    public boolean mouseClickedOverlay(int mouseX, int mouseY, int mouseButton) { return false; }

    // Default: element has no overlay open
    public boolean hasOverlayOpen() { return false; }

    // Default: nothing to close
    public void closeOverlay() { /* no-op */ }

    // Getters and setters
    public void setPosition(int x, int y) { this.x = x; this.y = y; }
    public void setSize(int width, int height) { this.width = width; this.height = height; }
    public void setVisible(boolean visible) { this.visible = visible; }
    public boolean isVisible() { return visible; }

    // New: expose intrinsic height for layout
    public int getHeight() { return this.height; }

    // New: optional extra space ABOVE this element (e.g., for a title drawn at y - n)
    public int getTopPadding() { return 0; }

    // New: total vertical space this element occupies including padding above
    public int getOuterHeight() { return getTopPadding() + getHeight(); }
}