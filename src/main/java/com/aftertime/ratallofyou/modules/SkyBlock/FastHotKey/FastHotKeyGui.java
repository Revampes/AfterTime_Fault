package com.aftertime.ratallofyou.modules.SkyBlock.FastHotKey;

import com.aftertime.ratallofyou.UI.config.ConfigStorage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import java.io.IOException;
import java.util.List;

public class FastHotKeyGui extends GuiScreen {
    private static final int CIRCLE_RADIUS = 120;
    private int centerX;
    private int centerY;
    private int regionCount = 0;

    @Override
    public void initGui() {
        super.initGui();
        this.centerX = width / 2;
        this.centerY = height / 2;
        List<ConfigStorage.FastHotKeyEntry> entries = ConfigStorage.getFastHotKeyEntries();
        this.regionCount = Math.min(12, entries.size());
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // Draw dark background (80% opacity)
        drawRect(0, 0, width, height, 0xCC000000);

        // Update center positions
        centerX = width / 2;
        centerY = height / 2;

        // Draw black circle background (100% opacity)
        drawFilledCircle(centerX, centerY, CIRCLE_RADIUS, 0xFF000000);

        // If no commands configured
        if (regionCount == 0) {
            String msg = "No Fast Hotkey commands configured";
            String hint = "Open Mod Settings > Fast Hotkey > Settings to add up to 12";
            drawCenteredString(fontRendererObj, msg, centerX, centerY - 10, 0xFFFFFF);
            drawCenteredString(fontRendererObj, hint, centerX, centerY + 5, 0xAAAAAA);
            super.drawScreen(mouseX, mouseY, partialTicks);
            return;
        }

        // Draw white circumference with thick line
        drawCircleOutline(centerX, centerY, CIRCLE_RADIUS, 0xFFFFFFFF, 5.0f);

        // Dividing lines
        for (int i = 0; i < regionCount; i++) {
            double angle = Math.PI * 2 * i / regionCount;
            drawLine(centerX, centerY,
                    (int)(centerX + Math.cos(angle) * CIRCLE_RADIUS),
                    (int)(centerY + Math.sin(angle) * CIRCLE_RADIUS),
                    0xFFFFFFFF, 5.0f);
        }

        // Hover effect
        int hoveredRegion = getHoveredRegion(mouseX, mouseY);
        if (hoveredRegion != -1) {
            drawCircleSector(centerX, centerY, CIRCLE_RADIUS, hoveredRegion, 0x66AAAAAA);
            drawCircleArc(centerX, centerY, CIRCLE_RADIUS, hoveredRegion, 0x99FF0000, 10.0f);

            double startAngle = Math.PI * 2 * hoveredRegion / regionCount;
            double endAngle = Math.PI * 2 * (hoveredRegion + 1) / regionCount;

            drawLine(centerX, centerY,
                    (int)(centerX + Math.cos(startAngle) * CIRCLE_RADIUS),
                    (int)(centerY + Math.sin(startAngle) * CIRCLE_RADIUS),
                    0x99FF0000, 10.0f);

            drawLine(centerX, centerY,
                    (int)(centerX + Math.cos(endAngle) * CIRCLE_RADIUS),
                    (int)(centerY + Math.sin(endAngle) * CIRCLE_RADIUS),
                    0x99FF0000, 10.0f);
        }

        // Labels
        List<ConfigStorage.FastHotKeyEntry> entries = ConfigStorage.getFastHotKeyEntries();
        for (int i = 0; i < regionCount; i++) {
            double angle = Math.PI * 2 * i / regionCount + Math.PI / regionCount;
            int x = (int)(centerX + Math.cos(angle) * CIRCLE_RADIUS * 0.7);
            int y = (int)(centerY + Math.sin(angle) * CIRCLE_RADIUS * 0.7);
            String label = entries.get(i).label;
            if (label == null || label.trim().isEmpty()) label = "Command " + (i + 1);
            drawCenteredString(fontRendererObj, label, x, y, 0xFFFFFF);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawCircleArc(int x, int y, int radius, int region, int color, float thickness) {
        float a = (float)(color >> 24 & 255) / 255.0F;
        float r = (float)(color >> 16 & 255) / 255.0F;
        float g = (float)(color >> 8 & 255) / 255.0F;
        float b = (float)(color & 255) / 255.0F;

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(r, g, b, a);
        GL11.glLineWidth(thickness);

        double sectorSize = 2 * Math.PI / regionCount;
        double startAngle = region * sectorSize;
        double endAngle = (region + 1) * sectorSize;

        GL11.glBegin(GL11.GL_LINE_STRIP);
        for (double angle = startAngle; angle <= endAngle; angle += sectorSize / 20) {
            GL11.glVertex2d(x + Math.cos(angle) * radius, y + Math.sin(angle) * radius);
        }
        GL11.glVertex2d(x + Math.cos(endAngle) * radius, y + Math.sin(endAngle) * radius);
        GL11.glEnd();

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
    }

    private void drawFilledCircle(int x, int y, int radius, int color) {
        float a = (float)(color >> 24 & 255) / 255.0F;
        float r = (float)(color >> 16 & 255) / 255.0F;
        float g = (float)(color >> 8 & 255) / 255.0F;
        float b = (float)(color & 255) / 255.0F;

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(r, g, b, a);

        GL11.glBegin(GL11.GL_TRIANGLE_FAN);
        GL11.glVertex2f(x, y);
        for (int i = 0; i <= 360; i++) {
            double angle = Math.PI * 2 * i / 360;
            GL11.glVertex2d(x + Math.cos(angle) * radius, y + Math.sin(angle) * radius);
        }
        GL11.glEnd();

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
    }

    private void drawCircleOutline(int x, int y, int radius, int color, float thickness) {
        float a = (float)(color >> 24 & 255) / 255.0F;
        float r = (float)(color >> 16 & 255) / 255.0F;
        float g = (float)(color >> 8 & 255) / 255.0F;
        float b = (float)(color & 255) / 255.0F;

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(r, g, b, a);
        GL11.glLineWidth(thickness);

        GL11.glBegin(GL11.GL_LINE_LOOP);
        for (int i = 0; i <= 360; i++) {
            double angle = Math.PI * 2 * i / 360;
            GL11.glVertex2d(x + Math.cos(angle) * radius, y + Math.sin(angle) * radius);
        }
        GL11.glEnd();

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
    }

    private void drawCircleSector(int x, int y, int radius, int region, int color) {
        float a = (float)(color >> 24 & 255) / 255.0F;
        float r = (float)(color >> 16 & 255) / 255.0F;
        float g = (float)(color >> 8 & 255) / 255.0F;
        float b = (float)(color & 255) / 255.0F;

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(r, g, b, a);

        double sectorSize = 2 * Math.PI / regionCount;
        double startAngle = region * sectorSize;
        double endAngle = (region + 1) * sectorSize;

        GL11.glBegin(GL11.GL_TRIANGLE_FAN);
        GL11.glVertex2f(x, y);
        for (double angle = startAngle; angle <= endAngle; angle += sectorSize / 20) {
            GL11.glVertex2d(x + Math.cos(angle) * radius, y + Math.sin(angle) * radius);
        }
        GL11.glVertex2d(x + Math.cos(endAngle) * radius, y + Math.sin(endAngle) * radius);
        GL11.glEnd();

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
    }

    private void drawLine(int x1, int y1, int x2, int y2, int color, float thickness) {
        float a = (float)(color >> 24 & 255) / 255.0F;
        float r = (float)(color >> 16 & 255) / 255.0F;
        float g = (float)(color >> 8 & 255) / 255.0F;
        float b = (float)(color & 255) / 255.0F;

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(r, g, b, a);
        GL11.glLineWidth(thickness);

        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex2i(x1, y1);
        GL11.glVertex2i(x2, y2);
        GL11.glEnd();

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
    }

    private int getHoveredRegion(int mouseX, int mouseY) {
        double dx = mouseX - centerX;
        double dy = mouseY - centerY;
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance > CIRCLE_RADIUS || regionCount == 0) return -1;

        double angle = Math.atan2(dy, dx);
        if (angle < 0) angle += 2 * Math.PI;

        return (int)(angle / (2 * Math.PI / regionCount));
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_G || keyCode == Keyboard.KEY_ESCAPE) {
            mc.displayGuiScreen(null);
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (mouseButton == 0) {
            int region = getHoveredRegion(mouseX, mouseY);
            if (region != -1) {
                List<ConfigStorage.FastHotKeyEntry> entries = ConfigStorage.getFastHotKeyEntries();
                if (region < entries.size()) {
                    String cmd = entries.get(region).command;
                    if (cmd != null && !cmd.trim().isEmpty()) {
                        Minecraft.getMinecraft().thePlayer.sendChatMessage(cmd);
                    }
                }
                mc.displayGuiScreen(null);
            }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}