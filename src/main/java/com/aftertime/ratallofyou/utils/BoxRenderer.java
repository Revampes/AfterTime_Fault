package com.aftertime.ratallofyou.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.AxisAlignedBB;
import org.lwjgl.opengl.GL11;

public class BoxRenderer {
    private static final Minecraft mc = Minecraft.getMinecraft();

    public static void drawEspBox(AxisAlignedBB box, float red, float green, float blue, float alpha, float lineWidth) {
        // Save all relevant GL state
        GlStateManager.pushMatrix();
        GlStateManager.pushAttrib();

        try {
            Tessellator tessellator = Tessellator.getInstance();
            WorldRenderer worldRenderer = tessellator.getWorldRenderer();

            // Adjust for viewer position
            double minX = box.minX - mc.getRenderManager().viewerPosX;
            double minY = box.minY - mc.getRenderManager().viewerPosY;
            double minZ = box.minZ - mc.getRenderManager().viewerPosZ;
            double maxX = box.maxX - mc.getRenderManager().viewerPosX;
            double maxY = box.maxY - mc.getRenderManager().viewerPosY;
            double maxZ = box.maxZ - mc.getRenderManager().viewerPosZ;

            GlStateManager.disableTexture2D();
            GlStateManager.enableBlend();
            GlStateManager.disableAlpha();
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GlStateManager.disableDepth();
            GL11.glLineWidth(lineWidth);

            // Bottom square
            worldRenderer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
            worldRenderer.pos(minX, minY, minZ).color(red, green, blue, alpha).endVertex();
            worldRenderer.pos(maxX, minY, minZ).color(red, green, blue, alpha).endVertex();
            worldRenderer.pos(maxX, minY, maxZ).color(red, green, blue, alpha).endVertex();
            worldRenderer.pos(minX, minY, maxZ).color(red, green, blue, alpha).endVertex();
            worldRenderer.pos(minX, minY, minZ).color(red, green, blue, alpha).endVertex();
            tessellator.draw();

            // Vertical lines
            worldRenderer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
            worldRenderer.pos(minX, minY, minZ).color(red, green, blue, alpha).endVertex();
            worldRenderer.pos(minX, maxY, minZ).color(red, green, blue, alpha).endVertex();

            worldRenderer.pos(maxX, minY, minZ).color(red, green, blue, alpha).endVertex();
            worldRenderer.pos(maxX, maxY, minZ).color(red, green, blue, alpha).endVertex();

            worldRenderer.pos(minX, minY, maxZ).color(red, green, blue, alpha).endVertex();
            worldRenderer.pos(minX, maxY, maxZ).color(red, green, blue, alpha).endVertex();

            worldRenderer.pos(maxX, minY, maxZ).color(red, green, blue, alpha).endVertex();
            worldRenderer.pos(maxX, maxY, maxZ).color(red, green, blue, alpha).endVertex();
            tessellator.draw();

            // Top square
            worldRenderer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
            worldRenderer.pos(minX, maxY, minZ).color(red, green, blue, alpha).endVertex();
            worldRenderer.pos(maxX, maxY, minZ).color(red, green, blue, alpha).endVertex();
            worldRenderer.pos(maxX, maxY, maxZ).color(red, green, blue, alpha).endVertex();
            worldRenderer.pos(minX, maxY, maxZ).color(red, green, blue, alpha).endVertex();
            worldRenderer.pos(minX, maxY, minZ).color(red, green, blue, alpha).endVertex();
            tessellator.draw();
        } finally {
            // Clean up GL state
            GlStateManager.enableDepth();
            GlStateManager.enableTexture2D();
            GlStateManager.disableBlend();
            GlStateManager.enableAlpha();

            // Restore state - must be in reverse order of pushes
            GlStateManager.popAttrib();
            GlStateManager.popMatrix();
        }
    }

    public static void drawEntityEspBox(double x, double y, double z,
                                        double width, double height,
                                        float red, float green, float blue,
                                        float yOffset) {
        // Create bounding box with specified dimensions
        AxisAlignedBB box = new AxisAlignedBB(
                x - width/2, y + yOffset, z - width/2,
                x + width/2, y + yOffset + height, z + width/2
        );
        drawEspBox(box, red, green, blue, 1.0f, 2.0f);
    }
}