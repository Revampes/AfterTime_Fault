package com.aftertime.ratallofyou.utils;

import net.minecraft.client.Minecraft;
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

    public static void renderBoxFromCorners(double x0, double y0, double z0, double x1, double y1, double z1,
                                            float r, float g, float b, float a, boolean phase, float lineWidth, boolean filled) {
        // Save all relevant GL state
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glPushMatrix();

        // Setup GL state
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDepthMask(false);

        if (phase) {
            GL11.glDisable(GL11.GL_DEPTH_TEST);
        } else {
            GL11.glEnable(GL11.GL_DEPTH_TEST);
        }

        GL11.glLineWidth(lineWidth);
        GL11.glColor4f(r, g, b, a);

        // Setup camera to render in world space
        net.minecraft.client.renderer.GlStateManager.translate(
                -Minecraft.getMinecraft().getRenderManager().viewerPosX,
                -Minecraft.getMinecraft().getRenderManager().viewerPosY,
                -Minecraft.getMinecraft().getRenderManager().viewerPosZ
        );

        // Draw the box
        if (filled) {
            GL11.glBegin(GL11.GL_QUADS);
            drawFace(x0, y0, z0, x1, y1, z1, 0); // Bottom
            drawFace(x0, y0, z0, x1, y1, z1, 1); // Top
            drawFace(x0, y0, z0, x1, y1, z1, 2); // North
            drawFace(x0, y0, z0, x1, y1, z1, 3); // South
            drawFace(x0, y0, z0, x1, y1, z1, 4); // West
            drawFace(x0, y0, z0, x1, y1, z1, 5); // East
            GL11.glEnd();
        }

        // Draw outline
        GL11.glBegin(GL11.GL_LINE_LOOP);
        GL11.glVertex3d(x0, y0, z0);
        GL11.glVertex3d(x1, y0, z0);
        GL11.glVertex3d(x1, y0, z1);
        GL11.glVertex3d(x0, y0, z1);
        GL11.glEnd();

        GL11.glBegin(GL11.GL_LINE_LOOP);
        GL11.glVertex3d(x0, y1, z0);
        GL11.glVertex3d(x1, y1, z0);
        GL11.glVertex3d(x1, y1, z1);
        GL11.glVertex3d(x0, y1, z1);
        GL11.glEnd();

        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex3d(x0, y0, z0); GL11.glVertex3d(x0, y1, z0);
        GL11.glVertex3d(x1, y0, z0); GL11.glVertex3d(x1, y1, z0);
        GL11.glVertex3d(x0, y0, z1); GL11.glVertex3d(x0, y1, z1);
        GL11.glVertex3d(x1, y0, z1); GL11.glVertex3d(x1, y1, z1);
        GL11.glEnd();

        // Restore GL state
        GL11.glPopMatrix();
        GL11.glPopAttrib();
    }

    private static void drawFace(double x0, double y0, double z0, double x1, double y1, double z1, int face) {
        switch(face) {
            case 0: // Bottom
                GL11.glVertex3d(x0, y0, z0);
                GL11.glVertex3d(x1, y0, z0);
                GL11.glVertex3d(x1, y0, z1);
                GL11.glVertex3d(x0, y0, z1);
                break;
            case 1: // Top
                GL11.glVertex3d(x0, y1, z0);
                GL11.glVertex3d(x1, y1, z0);
                GL11.glVertex3d(x1, y1, z1);
                GL11.glVertex3d(x0, y1, z1);
                break;
            case 2: // North
                GL11.glVertex3d(x0, y0, z0);
                GL11.glVertex3d(x0, y1, z0);
                GL11.glVertex3d(x1, y1, z0);
                GL11.glVertex3d(x1, y0, z0);
                break;
            case 3: // South
                GL11.glVertex3d(x0, y0, z1);
                GL11.glVertex3d(x0, y1, z1);
                GL11.glVertex3d(x1, y1, z1);
                GL11.glVertex3d(x1, y0, z1);
                break;
            case 4: // West
                GL11.glVertex3d(x0, y0, z0);
                GL11.glVertex3d(x0, y0, z1);
                GL11.glVertex3d(x0, y1, z1);
                GL11.glVertex3d(x0, y1, z0);
                break;
            case 5: // East
                GL11.glVertex3d(x1, y0, z0);
                GL11.glVertex3d(x1, y0, z1);
                GL11.glVertex3d(x1, y1, z1);
                GL11.glVertex3d(x1, y1, z0);
                break;
        }
    }
}