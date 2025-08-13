package com.aftertime.ratallofyou.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;

public class BeaconBeamRenderer {
    private static final ResourceLocation beaconBeam = new ResourceLocation("textures/entity/beacon_beam.png");
    private static final Minecraft mc = Minecraft.getMinecraft();

    public static void renderBeaconBeam(Vec3 position, Color color, boolean depthCheck, float height, float partialTicks) {
        if (color.getAlpha() == 0) return;

        float bottomOffset = 0;
        float topOffset = bottomOffset + height;

        // Setup depth
        if (!depthCheck) {
            GlStateManager.disableDepth();
        } else {
            GlStateManager.enableDepth();
        }

        // Bind texture
        mc.getTextureManager().bindTexture(beaconBeam);

        // Texture parameters
        GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
        GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);

        // Save and setup GL state
        GlStateManager.pushMatrix();
        GlStateManager.disableLighting();
        GlStateManager.enableCull();
        GlStateManager.enableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE, GL11.GL_ZERO);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);

        // Adjust for viewer position
        GlStateManager.translate(
                -mc.getRenderManager().viewerPosX,
                -mc.getRenderManager().viewerPosY,
                -mc.getRenderManager().viewerPosZ
        );

        // Animation calculations
        double time = mc.theWorld.getWorldTime() + partialTicks;
        double d1 = MathHelper.func_181162_h(-time * 0.2 - Math.floor(-time * 0.1));
        double d2 = time * 0.025 * -1.5;
        double d4 = 0.5 + Math.cos(d2 + 2.356194490192345) * 0.2;
        double d5 = 0.5 + Math.sin(d2 + 2.356194490192345) * 0.2;
        double d6 = 0.5 + Math.cos(d2 + (Math.PI / 4)) * 0.2;
        double d7 = 0.5 + Math.sin(d2 + (Math.PI / 4)) * 0.2;
        double d8 = 0.5 + Math.cos(d2 + 3.9269908169872414) * 0.2;
        double d9 = 0.5 + Math.sin(d2 + 3.9269908169872414) * 0.2;
        double d10 = 0.5 + Math.cos(d2 + 5.497787143782138) * 0.2;
        double d11 = 0.5 + Math.sin(d2 + 5.497787143782138) * 0.2;
        double d14 = -1 + d1;
        double d15 = height * 2.5 + d14;

        // Get renderer instances
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();

        // Main beam rendering
        worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
        addBeamVertices(worldRenderer, position, d4, d5, d6, d7, d8, d9, d10, d11, d14, d15, topOffset, bottomOffset, color);
        tessellator.draw();

        // Inner beam rendering
        GlStateManager.disableCull();
        double d12 = -1 + d1;
        double d13 = height + d12;

        worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
        addInnerBeamVertices(worldRenderer, position, d12, d13, topOffset, bottomOffset, color);
        tessellator.draw();

        // Restore GL state
        GlStateManager.resetColor();
        if (!depthCheck) {
            GlStateManager.enableDepth();
        }
        GlStateManager.enableCull();
        GlStateManager.popMatrix();
    }

    private static void addBeamVertices(WorldRenderer worldRenderer, Vec3 pos,
                                        double d4, double d5, double d6, double d7,
                                        double d8, double d9, double d10, double d11,
                                        double d14, double d15,
                                        float topOffset, float bottomOffset, Color color) {
        float r = color.getRed() / 255f;
        float g = color.getGreen() / 255f;
        float b = color.getBlue() / 255f;
        float a = color.getAlpha() / 255f;

        double x = pos.xCoord;
        double y = pos.yCoord;
        double z = pos.zCoord;

        // Outer beam vertices
        worldRenderer.pos(x + d4, y + topOffset, z + d5).tex(1.0, d15).color(r, g, b, a).endVertex();
        worldRenderer.pos(x + d4, y + bottomOffset, z + d5).tex(1.0, d14).color(r, g, b, a).endVertex();
        worldRenderer.pos(x + d6, y + bottomOffset, z + d7).tex(0.0, d14).color(r, g, b, a).endVertex();
        worldRenderer.pos(x + d6, y + topOffset, z + d7).tex(0.0, d15).color(r, g, b, a).endVertex();

        worldRenderer.pos(x + d10, y + topOffset, z + d11).tex(1.0, d15).color(r, g, b, a).endVertex();
        worldRenderer.pos(x + d10, y + bottomOffset, z + d11).tex(1.0, d14).color(r, g, b, a).endVertex();
        worldRenderer.pos(x + d8, y + bottomOffset, z + d9).tex(0.0, d14).color(r, g, b, a).endVertex();
        worldRenderer.pos(x + d8, y + topOffset, z + d9).tex(0.0, d15).color(r, g, b, a).endVertex();

        worldRenderer.pos(x + d6, y + topOffset, z + d7).tex(1.0, d15).color(r, g, b, a).endVertex();
        worldRenderer.pos(x + d6, y + bottomOffset, z + d7).tex(1.0, d14).color(r, g, b, a).endVertex();
        worldRenderer.pos(x + d10, y + bottomOffset, z + d11).tex(0.0, d14).color(r, g, b, a).endVertex();
        worldRenderer.pos(x + d10, y + topOffset, z + d11).tex(0.0, d15).color(r, g, b, a).endVertex();

        worldRenderer.pos(x + d8, y + topOffset, z + d9).tex(1.0, d15).color(r, g, b, a).endVertex();
        worldRenderer.pos(x + d8, y + bottomOffset, z + d9).tex(1.0, d14).color(r, g, b, a).endVertex();
        worldRenderer.pos(x + d4, y + bottomOffset, z + d5).tex(0.0, d14).color(r, g, b, a).endVertex();
        worldRenderer.pos(x + d4, y + topOffset, z + d5).tex(0.0, d15).color(r, g, b, a).endVertex();
    }

    private static void addInnerBeamVertices(WorldRenderer worldRenderer, Vec3 pos,
                                             double d12, double d13,
                                             float topOffset, float bottomOffset, Color color) {
        float r = color.getRed() / 255f;
        float g = color.getGreen() / 255f;
        float b = color.getBlue() / 255f;
        float a = color.getAlpha() / 255f * 0.25f;

        double x = pos.xCoord;
        double y = pos.yCoord;
        double z = pos.zCoord;

        // Inner beam vertices
        worldRenderer.pos(x + 0.2, y + topOffset, z + 0.2).tex(1.0, d13).color(r, g, b, a).endVertex();
        worldRenderer.pos(x + 0.2, y + bottomOffset, z + 0.2).tex(1.0, d12).color(r, g, b, a).endVertex();
        worldRenderer.pos(x + 0.8, y + bottomOffset, z + 0.2).tex(0.0, d12).color(r, g, b, a).endVertex();
        worldRenderer.pos(x + 0.8, y + topOffset, z + 0.2).tex(0.0, d13).color(r, g, b, a).endVertex();

        worldRenderer.pos(x + 0.8, y + topOffset, z + 0.8).tex(1.0, d13).color(r, g, b, a).endVertex();
        worldRenderer.pos(x + 0.8, y + bottomOffset, z + 0.8).tex(1.0, d12).color(r, g, b, a).endVertex();
        worldRenderer.pos(x + 0.2, y + bottomOffset, z + 0.8).tex(0.0, d12).color(r, g, b, a).endVertex();
        worldRenderer.pos(x + 0.2, y + topOffset, z + 0.8).tex(0.0, d13).color(r, g, b, a).endVertex();

        worldRenderer.pos(x + 0.8, y + topOffset, z + 0.2).tex(1.0, d13).color(r, g, b, a).endVertex();
        worldRenderer.pos(x + 0.8, y + bottomOffset, z + 0.2).tex(1.0, d12).color(r, g, b, a).endVertex();
        worldRenderer.pos(x + 0.8, y + bottomOffset, z + 0.8).tex(0.0, d12).color(r, g, b, a).endVertex();
        worldRenderer.pos(x + 0.8, y + topOffset, z + 0.8).tex(0.0, d13).color(r, g, b, a).endVertex();

        worldRenderer.pos(x + 0.2, y + topOffset, z + 0.8).tex(1.0, d13).color(r, g, b, a).endVertex();
        worldRenderer.pos(x + 0.2, y + bottomOffset, z + 0.8).tex(1.0, d12).color(r, g, b, a).endVertex();
        worldRenderer.pos(x + 0.2, y + bottomOffset, z + 0.2).tex(0.0, d12).color(r, g, b, a).endVertex();
        worldRenderer.pos(x + 0.2, y + topOffset, z + 0.2).tex(0.0, d13).color(r, g, b, a).endVertex();
    }

    // Simple Color class if you don't have one
    public static class Color {
        private final int red;
        private final int green;
        private final int blue;
        private final int alpha;

        public Color(int red, int green, int blue, int alpha) {
            this.red = red;
            this.green = green;
            this.blue = blue;
            this.alpha = alpha;
        }

        public int getRed() { return red; }
        public int getGreen() { return green; }
        public int getBlue() { return blue; }
        public int getAlpha() { return alpha; }
    }
}