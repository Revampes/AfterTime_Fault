package com.aftertime.ratallofyou.modules.render.FastHotKey;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import java.io.IOException;

public class FastHotKeyGui extends GuiScreen {
    private static final int CIRCLE_RADIUS = 120;
    private int centerX;
    private int centerY;
    private static final String[] COMMANDS = {"/ec", "/bp", "/trades", "/pets", "/wardrobe", "/eq"};
    private static final String[] LABELS = {"Ender Chest", "Storage", "Trades", "Pets", "Wardrobe", "Equipment"};
    private static final int REGION_COUNT = 6;

    @Override
    public void initGui() {
        super.initGui();
        this.centerX = width / 2;
        this.centerY = height / 2;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // Draw dark background (80% opacity)
        drawRect(0, 0, width, height, 0xCC000000);

        // Update center positions
        centerX = width / 2;
        centerY = height / 2;

        // Draw circle background (60% opacity dark gray)
        drawFilledCircle(centerX, centerY, CIRCLE_RADIUS, 0x99000000);

        // Highlight hovered region (bright red for testing)
        int hoveredRegion = getHoveredRegion(mouseX, mouseY);
        if (hoveredRegion != -1) {
            drawCircleSector(centerX, centerY, CIRCLE_RADIUS, hoveredRegion, 0x66FF0000);
        }

        // Draw white outline
        drawCircleOutline(centerX, centerY, CIRCLE_RADIUS, 0xFFFFFFFF);

        // Draw dividing lines
        for (int i = 0; i < REGION_COUNT; i++) {
            double angle = Math.PI * 2 * i / REGION_COUNT;
            drawLine(centerX, centerY,
                    (int)(centerX + Math.cos(angle) * CIRCLE_RADIUS),
                    (int)(centerY + Math.sin(angle) * CIRCLE_RADIUS),
                    0xFFFFFFFF);
        }

        // Draw labels
        for (int i = 0; i < REGION_COUNT; i++) {
            double angle = Math.PI * 2 * i / REGION_COUNT + Math.PI/REGION_COUNT;
            int x = (int)(centerX + Math.cos(angle) * CIRCLE_RADIUS * 0.7);
            int y = (int)(centerY + Math.sin(angle) * CIRCLE_RADIUS * 0.7);
            drawCenteredString(fontRendererObj, LABELS[i], x, y, 0xFFFFFF);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
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

    private void drawCircleOutline(int x, int y, int radius, int color) {
        float a = (float)(color >> 24 & 255) / 255.0F;
        float r = (float)(color >> 16 & 255) / 255.0F;
        float g = (float)(color >> 8 & 255) / 255.0F;
        float b = (float)(color & 255) / 255.0F;

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(r, g, b, a);

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

        double sectorSize = 2 * Math.PI / REGION_COUNT;
        double startAngle = region * sectorSize;
        double endAngle = (region + 1) * sectorSize;

        GL11.glBegin(GL11.GL_TRIANGLE_FAN);
        GL11.glVertex2f(x, y);
        for (double angle = startAngle; angle <= endAngle; angle += sectorSize/20) {
            GL11.glVertex2d(x + Math.cos(angle) * radius, y + Math.sin(angle) * radius);
        }
        GL11.glVertex2d(x + Math.cos(endAngle) * radius, y + Math.sin(endAngle) * radius);
        GL11.glEnd();

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
    }

    private void drawLine(int x1, int y1, int x2, int y2, int color) {
        float a = (float)(color >> 24 & 255) / 255.0F;
        float r = (float)(color >> 16 & 255) / 255.0F;
        float g = (float)(color >> 8 & 255) / 255.0F;
        float b = (float)(color & 255) / 255.0F;

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(r, g, b, a);

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
        double distance = Math.sqrt(dx*dx + dy*dy);

        if (distance > CIRCLE_RADIUS) return -1;

        double angle = Math.atan2(dy, dx);
        if (angle < 0) angle += 2 * Math.PI;

        return (int)(angle / (2 * Math.PI / REGION_COUNT));
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
                Minecraft.getMinecraft().thePlayer.sendChatMessage(COMMANDS[region]);
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