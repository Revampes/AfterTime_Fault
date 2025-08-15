package com.aftertime.ratallofyou.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

public class HitBoxRenderer {
    public static void renderBlockHitbox(BlockPos pos, float r, float g, float b, float a, boolean phase, float lineWidth, boolean filled) {
        World world = Minecraft.getMinecraft().theWorld;
        if (world == null) return;

        double[] boundingBox = Utils.getBlockBoundingBox(world, pos);
        if (boundingBox == null || boundingBox.length < 6) return;

        renderBoxFromCorners(boundingBox[0], boundingBox[1], boundingBox[2],
                boundingBox[3], boundingBox[4], boundingBox[5],
                r, g, b, a, phase, lineWidth, filled);
    }

    public static void renderBoxFromCorners(double x0, double y0, double z0,
                                            double x1, double y1, double z1,
                                            float r, float g, float b, float a,
                                            boolean phase, float lineWidth, boolean filled) {
        // Save current GL state
        boolean cullFace = GL11.glGetBoolean(GL11.GL_CULL_FACE);
        boolean depthTest = GL11.glGetBoolean(GL11.GL_DEPTH_TEST);

        try {
            // Setup our rendering state
            GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
            GlStateManager.pushMatrix();

            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GlStateManager.disableTexture2D();
            GlStateManager.disableLighting();
            GlStateManager.depthMask(false);
            GL11.glLineWidth(lineWidth);
            GlStateManager.color(r, g, b, a);
            GL11.glDisable(GL11.GL_CULL_FACE); // Disable culling to render all faces

            if (phase) {
                GlStateManager.disableDepth();
            } else {
                GlStateManager.enableDepth();
            }

            // Translate to world coordinates
            GlStateManager.translate(
                    -Minecraft.getMinecraft().getRenderManager().viewerPosX,
                    -Minecraft.getMinecraft().getRenderManager().viewerPosY,
                    -Minecraft.getMinecraft().getRenderManager().viewerPosZ
            );

            if (filled) {
                GL11.glEnable(GL11.GL_POLYGON_OFFSET_FILL);
                GL11.glPolygonOffset(1.0f, -1.0f);

                Tessellator tessellator = Tessellator.getInstance();
                WorldRenderer worldRenderer = tessellator.getWorldRenderer();

                worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);

                // Draw all faces (front and back)
                // Bottom face (y = y0)
                worldRenderer.pos(x0, y0, z0).endVertex();
                worldRenderer.pos(x1, y0, z0).endVertex();
                worldRenderer.pos(x1, y0, z1).endVertex();
                worldRenderer.pos(x0, y0, z1).endVertex();

                // Top face (y = y1)
                worldRenderer.pos(x0, y1, z0).endVertex();
                worldRenderer.pos(x1, y1, z0).endVertex();
                worldRenderer.pos(x1, y1, z1).endVertex();
                worldRenderer.pos(x0, y1, z1).endVertex();

                // North face (z = z0)
                worldRenderer.pos(x0, y0, z0).endVertex();
                worldRenderer.pos(x0, y1, z0).endVertex();
                worldRenderer.pos(x1, y1, z0).endVertex();
                worldRenderer.pos(x1, y0, z0).endVertex();

                // South face (z = z1)
                worldRenderer.pos(x0, y0, z1).endVertex();
                worldRenderer.pos(x0, y1, z1).endVertex();
                worldRenderer.pos(x1, y1, z1).endVertex();
                worldRenderer.pos(x1, y0, z1).endVertex();

                // West face (x = x0)
                worldRenderer.pos(x0, y0, z0).endVertex();
                worldRenderer.pos(x0, y0, z1).endVertex();
                worldRenderer.pos(x0, y1, z1).endVertex();
                worldRenderer.pos(x0, y1, z0).endVertex();

                // East face (x = x1)
                worldRenderer.pos(x1, y0, z0).endVertex();
                worldRenderer.pos(x1, y0, z1).endVertex();
                worldRenderer.pos(x1, y1, z1).endVertex();
                worldRenderer.pos(x1, y1, z0).endVertex();

                tessellator.draw();

                GL11.glDisable(GL11.GL_POLYGON_OFFSET_FILL);
            }

            // Draw outline
            Tessellator tessellator = Tessellator.getInstance();
            WorldRenderer worldRenderer = tessellator.getWorldRenderer();

            worldRenderer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION);

            // Lower Rectangle
            worldRenderer.pos(x0, y0, z0).endVertex();
            worldRenderer.pos(x0, y0, z1).endVertex();
            worldRenderer.pos(x1, y0, z1).endVertex();
            worldRenderer.pos(x1, y0, z0).endVertex();
            worldRenderer.pos(x0, y0, z0).endVertex();

            // Upper Rectangle
            worldRenderer.pos(x0, y1, z0).endVertex();
            worldRenderer.pos(x0, y1, z1).endVertex();
            worldRenderer.pos(x1, y1, z1).endVertex();
            worldRenderer.pos(x1, y1, z0).endVertex();
            worldRenderer.pos(x0, y1, z0).endVertex();

            // Vertical lines
            worldRenderer.pos(x0, y1, z1).endVertex();
            worldRenderer.pos(x0, y0, z1).endVertex();

            worldRenderer.pos(x1, y0, z1).endVertex();
            worldRenderer.pos(x1, y1, z1).endVertex();

            worldRenderer.pos(x1, y1, z0).endVertex();
            worldRenderer.pos(x1, y0, z0).endVertex();

            tessellator.draw();
        } finally {
            // Restore original GL state
            if (cullFace) GL11.glEnable(GL11.GL_CULL_FACE);
            else GL11.glDisable(GL11.GL_CULL_FACE);

            if (depthTest) GL11.glEnable(GL11.GL_DEPTH_TEST);
            else GL11.glDisable(GL11.GL_DEPTH_TEST);

            GlStateManager.popMatrix();
            GL11.glPopAttrib();
        }
    }

    private static void drawFace(double x0, double y0, double z0, double x1, double y1, double z1, int face) {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();

        worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);

        switch(face) {
            case 0: // Bottom
                worldRenderer.pos(x0, y0, z0).endVertex();
                worldRenderer.pos(x1, y0, z0).endVertex();
                worldRenderer.pos(x1, y0, z1).endVertex();
                worldRenderer.pos(x0, y0, z1).endVertex();
                break;
            case 1: // Top
                worldRenderer.pos(x0, y1, z0).endVertex();
                worldRenderer.pos(x1, y1, z0).endVertex();
                worldRenderer.pos(x1, y1, z1).endVertex();
                worldRenderer.pos(x0, y1, z1).endVertex();
                break;
            case 2: // North
                worldRenderer.pos(x0, y0, z0).endVertex();
                worldRenderer.pos(x0, y1, z0).endVertex();
                worldRenderer.pos(x1, y1, z0).endVertex();
                worldRenderer.pos(x1, y0, z0).endVertex();
                break;
            case 3: // South
                worldRenderer.pos(x0, y0, z1).endVertex();
                worldRenderer.pos(x0, y1, z1).endVertex();
                worldRenderer.pos(x1, y1, z1).endVertex();
                worldRenderer.pos(x1, y0, z1).endVertex();
                break;
            case 4: // West
                worldRenderer.pos(x0, y0, z0).endVertex();
                worldRenderer.pos(x0, y0, z1).endVertex();
                worldRenderer.pos(x0, y1, z1).endVertex();
                worldRenderer.pos(x0, y1, z0).endVertex();
                break;
            case 5: // East
                worldRenderer.pos(x1, y0, z0).endVertex();
                worldRenderer.pos(x1, y0, z1).endVertex();
                worldRenderer.pos(x1, y1, z1).endVertex();
                worldRenderer.pos(x1, y1, z0).endVertex();
                break;
        }

        tessellator.draw();
    }
}