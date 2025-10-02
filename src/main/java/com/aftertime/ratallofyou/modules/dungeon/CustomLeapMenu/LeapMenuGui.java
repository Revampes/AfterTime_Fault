package com.aftertime.ratallofyou.modules.dungeon.CustomLeapMenu;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

import java.util.List;
import java.util.Map;

public class LeapMenuGui {
    private final Minecraft mc = Minecraft.getMinecraft();
    private int centerX, centerY;
    private int innerRadius = 70;
    private int outerRadius = 200;
    private static final float GAP_PX = 6f;
    private static final double ANGLE_OFFSET = Math.PI / 4;
    private static final float TEXT_SCALE = 1.5f;
    private static final Map<String, Integer> CLASS_COLORS = LeapMenu.CLASS_COLORS;

    public void drawMenu(int width, int height, int mouseX, int mouseY, List<String> orderedNames, Map<String, String> nameToClass) {
        centerX = width / 2;
        centerY = height / 2;
        // Dim background
        Gui.drawRect(0, 0, width, height, 0xB0000000);
        // Draw ring sectors
        drawRingSectors(mouseX, mouseY, 4, orderedNames, nameToClass);
        // Header
        FontRenderer fr = mc.fontRendererObj;
        String header = "Spirit Leap";
        GlStateManager.pushMatrix();
        GlStateManager.translate(centerX, centerY - outerRadius - 18, 0);
        GlStateManager.scale(TEXT_SCALE, TEXT_SCALE, 1.0f);
        GlStateManager.translate(-fr.getStringWidth(header) / 2.0f, -fr.FONT_HEIGHT / 2.0f, 0);
        fr.drawString(header, 0, 0, 0xFFFFFF, true);
        GlStateManager.popMatrix();
        // Footer hint
        String hint = orderedNames.isEmpty() ? "No targets" : ("Left click or press 1-" + Math.min(4, orderedNames.size()));
        fr.drawString(hint, (int)(centerX - fr.getStringWidth(hint) / 2.0f), centerY + outerRadius + 10, 0xAAAAAA, false);
    }

    private void drawRingSectors(int mouseX, int mouseY, int count, List<String> orderedNames, Map<String, String> nameToClass) {
        if (count <= 0) return;
        double sector = (Math.PI * 2.0) / count;
        double gapAngleInner = GAP_PX / Math.max(1.0, innerRadius);
        double gapAngleOuter = GAP_PX / Math.max(1.0, outerRadius);
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        GlStateManager.disableTexture2D();
        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);
        GlStateManager.disableCull();
        for (int i = 0; i < count; i++) {
            double start = ANGLE_OFFSET + i * sector;
            double end = ANGLE_OFFSET + (i + 1) * sector;
            double iStart = start + gapAngleInner * 0.5;
            double iEnd = end - gapAngleInner * 0.5;
            double oStart = start + gapAngleOuter * 0.5;
            double oEnd = end - gapAngleOuter * 0.5;
            if (iEnd <= iStart || oEnd <= oStart) continue;
            boolean hovered = (getHoveredRegion(mouseX, mouseY, count) == i);
            int base = hovered ? 0x40FFFFFF : 0x3020A0FF;
            float a = (base >> 24 & 255) / 255f;
            float r = (base >> 16 & 255) / 255f;
            float g = (base >> 8 & 255) / 255f;
            float b = (base & 255) / 255f;
            GL11.glColor4f(r, g, b, a);
            GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
            int steps = 48;
            for (int s = 0; s <= steps; s++) {
                double t = (double) s / steps;
                double oa = oStart + t * (oEnd - oStart);
                double ia = iStart + t * (iEnd - iStart);
                float ox = (float) (centerX + Math.cos(oa) * outerRadius);
                float oy = (float) (centerY + Math.sin(oa) * outerRadius);
                float ix = (float) (centerX + Math.cos(ia) * innerRadius);
                float iy = (float) (centerY + Math.sin(ia) * innerRadius);
                GL11.glVertex2f(ox, oy);
                GL11.glVertex2f(ix, iy);
            }
            GL11.glEnd();
            double mid = (start + end) * 0.5;
            int rx = (int) (centerX + Math.cos(mid) * (innerRadius + (outerRadius - innerRadius) * 0.65));
            int ry = (int) (centerY + Math.sin(mid) * (innerRadius + (outerRadius - innerRadius) * 0.65));
            GlStateManager.enableTexture2D();
            GL11.glColor4f(1f, 1f, 1f, 1f);
            FontRenderer fr = mc.fontRendererObj;
            if (i < orderedNames.size()) {
                String name = orderedNames.get(i);
                String classLetter = nameToClass.get(name);
                if (classLetter == null) classLetter = "?";
                int classColor = CLASS_COLORS.getOrDefault(classLetter, 0xFFAAAAAA);
                GlStateManager.pushMatrix();
                GlStateManager.translate(rx, ry, 0);
                GlStateManager.scale(TEXT_SCALE, TEXT_SCALE, 1.0f);
                String classFull = LeapMenu.fullClassName(classLetter);
                int nameWidth = fr.getStringWidth(name);
                int classWidth = fr.getStringWidth(classFull);
                fr.drawString(name, (int)(-nameWidth / 2.0f), -fr.FONT_HEIGHT, 0xFFFFFF, true);
                fr.drawString(classFull, (int)(-classWidth / 2.0f), 0, classColor, true);
                GlStateManager.popMatrix();
            }
            GlStateManager.disableTexture2D();
        }
        GL11.glLineWidth(2.5f);
        GL11.glColor4f(1f, 1f, 1f, 0.5f);
        GL11.glBegin(GL11.GL_LINES);
        drawRadialLine(Math.PI / 4);
        drawRadialLine(Math.PI / 4 + Math.PI);
        drawRadialLine(3 * Math.PI / 4);
        drawRadialLine(3 * Math.PI / 4 + Math.PI);
        GL11.glEnd();
        GL11.glLineWidth(2f);
        GL11.glColor4f(1f, 1f, 1f, 0.33f);
        GL11.glBegin(GL11.GL_LINE_LOOP);
        for (int s = 0; s < 144; s++) {
            double a = (Math.PI * 2.0) * s / 144.0;
            GL11.glVertex2f((float)(centerX + Math.cos(a) * outerRadius), (float)(centerY + Math.sin(a) * outerRadius));
        }
        GL11.glEnd();
        GL11.glBegin(GL11.GL_LINE_LOOP);
        for (int s = 0; s < 144; s++) {
            double a = (Math.PI * 2.0) * s / 144.0;
            GL11.glVertex2f((float)(centerX + Math.cos(a) * innerRadius), (float)(centerY + Math.sin(a) * innerRadius));
        }
        GL11.glEnd();
        GlStateManager.enableCull();
        GlStateManager.depthMask(true);
        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    private void drawRadialLine(double angle) {
        float ix = (float) (centerX + Math.cos(angle) * innerRadius);
        float iy = (float) (centerY + Math.sin(angle) * innerRadius);
        float ox = (float) (centerX + Math.cos(angle) * outerRadius);
        float oy = (float) (centerY + Math.sin(angle) * outerRadius);
        GL11.glVertex2f(ix, iy);
        GL11.glVertex2f(ox, oy);
    }

    int getHoveredRegion(int mouseX, int mouseY, int count) {
        if (count <= 0) return -1;
        double dx = mouseX - centerX;
        double dy = mouseY - centerY;
        double dist = Math.hypot(dx, dy);
        if (dist < innerRadius || dist > outerRadius) return -1;
        double ang = Math.atan2(dy, dx);
        if (ang < 0) ang += Math.PI * 2.0;
        ang -= ANGLE_OFFSET;
        if (ang < 0) ang += Math.PI * 2.0;
        double sector = (Math.PI * 2.0) / count;
        int idx = (int) Math.floor(ang / sector);
        double localGap = GAP_PX / Math.max(1.0, dist);
        double within = ang - idx * sector;
        if (within < localGap * 0.5 || within > sector - localGap * 0.5) return -1;
        return Math.max(0, Math.min(count - 1, idx));
    }
}
