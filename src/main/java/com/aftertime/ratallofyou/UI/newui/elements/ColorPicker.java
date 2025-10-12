package com.aftertime.ratallofyou.UI.newui.elements;

import net.minecraft.client.gui.Gui;
import org.lwjgl.input.Mouse;

import java.awt.Color;
import com.aftertime.ratallofyou.UI.newui.util.TextRender;

public class ColorPicker extends UIElement {
    private Color color;
    private String title;
    private boolean picking = false; // overlay open
    private Runnable onChange;

    // HSV components and alpha [0..1]
    private float hue = 0f;      // 0..1
    private float sat = 1f;      // 0..1
    private float val = 1f;      // 0..1
    private float alpha = 1f;    // 0..1

    // dragging state inside overlay
    private boolean draggingSV = false;
    private boolean draggingHue = false;
    private boolean draggingAlpha = false;

    public ColorPicker(int x, int y, int width, int height, String title, Color initialColor, Runnable onChange) {
        super(x, y, width, height);
        this.title = title;
        this.onChange = onChange;
        setColor(initialColor);
    }

    @Override
    public void draw(int mouseX, int mouseY) {
        if (!visible) return;

        hovered = isMouseOver(mouseX, mouseY);

        // Draw title ABOVE the color box to prevent overlap (scaled)
        int th = TextRender.height(fontRenderer);
        TextRender.draw(fontRenderer, title, x, y - (th + 4), 0xFFFFFFFF);

        // Draw color preview with checkerboard background to show alpha
        drawCheckerboard(x, y, width, height, 3, 0xFFAAAAAA, 0xFF666666);

        int mcColor = getMCColor();
        Gui.drawRect(x, y, x + width, y + height, mcColor);

        // Draw border
        int borderColor = hovered ? 0xFFFFFF00 : 0xFF000000;
        Gui.drawRect(x, y, x + width, y + 1, borderColor);
        Gui.drawRect(x, y + height - 1, x + width, y + height, borderColor);
        Gui.drawRect(x, y, x + 1, y + height, borderColor);
        Gui.drawRect(x + width - 1, y, x + width, y + height, borderColor);
    }

    @Override
    public void drawOverlay(int mouseX, int mouseY) {
        if (!visible || !picking) return;

        // Layout overlay below the element, clamp to screen
        int overlayW = Math.max(170, Math.max(140, width));
        int svW = 120;
        int svH = 90;
        int hueW = 12;
        int alphaH = 12;
        int padding = 6;

        int overlayH = padding + svH + padding + alphaH + padding; // base without hue bar
        overlayH = Math.max(overlayH, padding + svH + padding) + padding; // ensure some padding

        // We'll place hue bar to the right of SV square
        int boxW = padding + svW + padding + hueW + padding;
        int boxH = padding + svH + padding + alphaH + padding;

        int overlayX = x;
        int overlayY = y + height + 2;
        // Clamp within screen
        int screenW = mc.currentScreen.width;
        int screenH = mc.currentScreen.height;
        if (overlayX + boxW > screenW - 4) overlayX = Math.max(4, screenW - boxW - 4);
        if (overlayY + boxH > screenH - 4) overlayY = Math.max(4, y - boxH - 4); // try above element

        // Draw overlay background
        Gui.drawRect(overlayX, overlayY, overlayX + boxW, overlayY + boxH, 0xF0101010);
        Gui.drawRect(overlayX, overlayY, overlayX + boxW, overlayY + 1, 0xFF000000);
        Gui.drawRect(overlayX, overlayY + boxH - 1, overlayX + boxW, overlayY + boxH, 0xFF000000);
        Gui.drawRect(overlayX, overlayY, overlayX + 1, overlayY + boxH, 0xFF000000);
        Gui.drawRect(overlayX + boxW - 1, overlayY, overlayX + boxW, overlayY + boxH, 0xFF000000);

        // Regions
        int svX = overlayX + padding;
        int svY = overlayY + padding;
        int hueX = svX + svW + padding;
        int hueY = svY;
        int alphaX = svX;
        int alphaY = svY + svH + padding;
        int alphaW = svW;

        // Draw SV square (full color range for current hue)
        for (int yy = 0; yy < svH; yy++) {
            float v = 1.0f - (yy / (float)(svH - 1));
            for (int xx = 0; xx < svW; xx++) {
                float s = xx / (float)(svW - 1);
                int rgb = Color.HSBtoRGB(hue, s, v); // alpha implicit 255
                int argb = ((int)(alpha * 255) << 24) | (rgb & 0x00FFFFFF);
                Gui.drawRect(svX + xx, svY + yy, svX + xx + 1, svY + yy + 1, argb);
            }
        }
        // SV crosshair at current sat/val
        int cx = svX + Math.round(sat * (svW - 1));
        int cy = svY + Math.round((1 - val) * (svH - 1));
        drawCrosshair(cx, cy, 3, 0xFFFFFFFF, 0xFF000000);

        // Draw hue bar (vertical gradient from 0..1)
        for (int yy = 0; yy < svH; yy++) {
            float h = yy / (float)(svH - 1);
            int rgb = Color.HSBtoRGB(h, 1f, 1f);
            Gui.drawRect(hueX, hueY + yy, hueX + hueW, hueY + yy + 1, 0xFF000000 | (rgb & 0x00FFFFFF));
        }
        // Hue selector
        int hy = hueY + Math.round(hue * (svH - 1));
        Gui.drawRect(hueX - 1, hy - 1, hueX + hueW + 1, hy + 1, 0xFFFFFFFF);
        Gui.drawRect(hueX, hy, hueX + hueW, hy + 1, 0xFF000000);

        // Alpha bar with checkerboard background
        drawCheckerboard(alphaX, alphaY, alphaW, alphaH, 4, 0xFFBBBBBB, 0xFF888888);
        for (int xx = 0; xx < alphaW; xx++) {
            float a = xx / (float)(alphaW - 1);
            int rgb = Color.HSBtoRGB(hue, sat, val);
            int argb = ((int)(a * 255) << 24) | (rgb & 0x00FFFFFF);
            Gui.drawRect(alphaX + xx, alphaY, alphaX + xx + 1, alphaY + alphaH, argb);
        }
        // Alpha selector
        int ax = alphaX + Math.round(alpha * (alphaW - 1));
        Gui.drawRect(ax - 1, alphaY - 1, ax + 1, alphaY + alphaH + 1, 0xFFFFFFFF);
        Gui.drawRect(ax, alphaY, ax + 1, alphaY + alphaH, 0xFF000000);

        // Handle dragging updates
        boolean lmbDown = Mouse.isButtonDown(0);
        if (!lmbDown) {
            draggingSV = draggingHue = draggingAlpha = false;
        } else {
            if (draggingSV || (mouseX >= svX && mouseX <= svX + svW && mouseY >= svY && mouseY <= svY + svH)) {
                draggingSV = true;
                float ns = (mouseX - svX) / (float)(svW - 1);
                float nv = 1.0f - (mouseY - svY) / (float)(svH - 1);
                ns = clamp01(ns); nv = clamp01(nv);
                if (ns != sat || nv != val) {
                    sat = ns; val = nv; updateColorFromHSV();
                }
            } else if (draggingHue || (mouseX >= hueX && mouseX <= hueX + hueW && mouseY >= hueY && mouseY <= hueY + svH)) {
                draggingHue = true;
                float nh = (mouseY - hueY) / (float)(svH - 1);
                nh = clamp01(nh);
                if (nh != hue) {
                    hue = nh; updateColorFromHSV();
                }
            } else if (draggingAlpha || (mouseX >= alphaX && mouseX <= alphaX + alphaW && mouseY >= alphaY && mouseY <= alphaY + alphaH)) {
                draggingAlpha = true;
                float na = (mouseX - alphaX) / (float)(alphaW - 1);
                na = clamp01(na);
                if (na != alpha) {
                    alpha = na; updateColorFromHSV();
                }
            }
        }
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (!visible) return false;
        if (mouseButton == 0 && isMouseOver(mouseX, mouseY)) {
            picking = !picking;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseClickedOverlay(int mouseX, int mouseY, int mouseButton) {
        if (!visible || !picking) return false;

        // Determine overlay rect to know if click is outside to close
        int svW = 120, svH = 90, hueW = 12, alphaH = 12, padding = 6;
        int boxW = padding + svW + padding + hueW + padding;
        int boxH = padding + svH + padding + alphaH + padding;
        int overlayX = x;
        int overlayY = y + height + 2;
        int screenW = mc.currentScreen.width;
        int screenH = mc.currentScreen.height;
        if (overlayX + boxW > screenW - 4) overlayX = Math.max(4, screenW - boxW - 4);
        if (overlayY + boxH > screenH - 4) overlayY = Math.max(4, y - boxH - 4);

        int svX = overlayX + padding;
        int svY = overlayY + padding;
        int hueX = svX + svW + padding;
        int hueY = svY;
        int alphaX = svX;
        int alphaY = svY + svH + padding;
        int alphaW = svW;

        if (mouseButton == 0) {
            // Inside SV
            if (mouseX >= svX && mouseX <= svX + svW && mouseY >= svY && mouseY <= svY + svH) {
                draggingSV = true;
                float ns = (mouseX - svX) / (float)(svW - 1);
                float nv = 1.0f - (mouseY - svY) / (float)(svH - 1);
                sat = clamp01(ns); val = clamp01(nv); updateColorFromHSV();
                return true;
            }
            // Inside Hue
            if (mouseX >= hueX && mouseX <= hueX + hueW && mouseY >= hueY && mouseY <= hueY + svH) {
                draggingHue = true;
                float nh = (mouseY - hueY) / (float)(svH - 1);
                hue = clamp01(nh); updateColorFromHSV();
                return true;
            }
            // Inside Alpha
            if (mouseX >= alphaX && mouseX <= alphaX + alphaW && mouseY >= alphaY && mouseY <= alphaY + alphaH) {
                draggingAlpha = true;
                float na = (mouseX - alphaX) / (float)(alphaW - 1);
                alpha = clamp01(na); updateColorFromHSV();
                return true;
            }
            // Elsewhere: close overlay and consume
            picking = false; draggingSV = draggingHue = draggingAlpha = false;
            return true;
        }
        return false;
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        draggingSV = draggingHue = draggingAlpha = false;
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        if (picking && keyCode == 1) { // ESC
            picking = false;
        }
    }

    public Color getColor() { return color; }

    public void setColor(Color color) {
        this.color = color;
        // update HSV/alpha from color
        float[] hsv = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        this.hue = hsv[0];
        this.sat = hsv[1];
        this.val = hsv[2];
        this.alpha = color.getAlpha() / 255.0f;
        if (onChange != null) onChange.run();
    }

    private void updateColorFromHSV() {
        int rgb = Color.HSBtoRGB(hue, sat, val);
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        this.color = new Color(r, g, b, Math.round(alpha * 255));
        if (onChange != null) onChange.run();
    }

    public int getMCColor() {
        return (color.getAlpha() << 24) | (color.getRed() << 16) | (color.getGreen() << 8) | color.getBlue();
    }

    // Allow attaching or changing the onChange callback after creation
    public void setOnChange(Runnable onChange) {
        this.onChange = onChange;
    }

    @Override
    public boolean hasOverlayOpen() { return picking; }

    @Override
    public void closeOverlay() { picking = false; draggingSV = draggingHue = draggingAlpha = false; }

    // Reserve space above for the title text
    @Override
    public int getTopPadding() {
        return 12;
    }

    // Helpers
    private static float clamp01(float f) { return f < 0 ? 0 : (f > 1 ? 1 : f); }

    private void drawCheckerboard(int px, int py, int w, int h, int cell, int c1, int c2) {
        for (int yy = 0; yy < h; yy += cell) {
            for (int xx = 0; xx < w; xx += cell) {
                boolean alt = ((xx / cell) + (yy / cell)) % 2 == 0;
                int col = alt ? c1 : c2;
                Gui.drawRect(px + xx, py + yy, px + Math.min(xx + cell, w), py + Math.min(yy + cell, h), col);
            }
        }
    }

    private void drawCrosshair(int cx, int cy, int r, int color1, int color2) {
        // white cross with black outline
        Gui.drawRect(cx - r - 1, cy, cx + r + 2, cy + 1, color2);
        Gui.drawRect(cx, cy - r - 1, cx + 1, cy + r + 2, color2);
        Gui.drawRect(cx - r, cy, cx + r + 1, cy + 1, color1);
        Gui.drawRect(cx, cy - r, cx + 1, cy + r + 1, color1);
    }
}