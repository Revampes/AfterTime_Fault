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

        GlStateManager.pushMatrix();
        GlStateManager.pushAttrib();

        try {
            float bottomOffset = 0;
            float topOffset = bottomOffset + height;

            // Setup depth
            GlStateManager.depthMask(false);
            if (!depthCheck) {
                GlStateManager.disableDepth();
            }

            // Bind texture
            mc.getTextureManager().bindTexture(beaconBeam);

            // Texture parameters
            GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, 10497.0f);
            GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, 10497.0f);

            // Setup GL state
            GlStateManager.disableLighting();
            GlStateManager.enableCull();
            GlStateManager.enableTexture2D();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);

            // Adjust for viewer position
            GlStateManager.translate(
                    position.xCoord - mc.getRenderManager().viewerPosX,
                    position.yCoord - mc.getRenderManager().viewerPosY,
                    position.zCoord - mc.getRenderManager().viewerPosZ
            );

            // Animation calculations
            double time = mc.theWorld.getTotalWorldTime() + partialTicks;
            double d1 = MathHelper.func_181162_h(-time * 0.2 - Math.floor(-time * 0.1));
            double d2 = time * 0.025 * -1.5;
            double d4 = Math.cos(d2 + 2.356194490192345) * 0.2;
            double d5 = Math.sin(d2 + 2.356194490192345) * 0.2;
            double d6 = Math.cos(d2 + (Math.PI / 4.0)) * 0.2;
            double d7 = Math.sin(d2 + (Math.PI / 4.0)) * 0.2;
            double d8 = Math.cos(d2 + 3.9269908169872414) * 0.2;
            double d9 = Math.sin(d2 + 3.9269908169872414) * 0.2;
            double d10 = Math.cos(d2 + 5.497787143782138) * 0.2;
            double d11 = Math.sin(d2 + 5.497787143782138) * 0.2;

            double d14 = -1.0 + d1;
            double d15 = height * 2.5 + d14;

            // Get color components
            float r = color.getRed() / 255f;
            float g = color.getGreen() / 255f;
            float b = color.getBlue() / 255f;
            float a = color.getAlpha() / 255f;

            // Get renderer instances
            Tessellator tessellator = Tessellator.getInstance();
            WorldRenderer worldRenderer = tessellator.getWorldRenderer();

            // Main beam rendering
            worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);

            // Outer beam vertices
            worldRenderer.pos(d4, topOffset, d5).tex(1.0, d15).color(r, g, b, a).endVertex();
            worldRenderer.pos(d4, bottomOffset, d5).tex(1.0, d14).color(r, g, b, a).endVertex();
            worldRenderer.pos(d6, bottomOffset, d7).tex(0.0, d14).color(r, g, b, a).endVertex();
            worldRenderer.pos(d6, topOffset, d7).tex(0.0, d15).color(r, g, b, a).endVertex();

            worldRenderer.pos(d10, topOffset, d11).tex(1.0, d15).color(r, g, b, a).endVertex();
            worldRenderer.pos(d10, bottomOffset, d11).tex(1.0, d14).color(r, g, b, a).endVertex();
            worldRenderer.pos(d8, bottomOffset, d9).tex(0.0, d14).color(r, g, b, a).endVertex();
            worldRenderer.pos(d8, topOffset, d9).tex(0.0, d15).color(r, g, b, a).endVertex();

            worldRenderer.pos(d6, topOffset, d7).tex(1.0, d15).color(r, g, b, a).endVertex();
            worldRenderer.pos(d6, bottomOffset, d7).tex(1.0, d14).color(r, g, b, a).endVertex();
            worldRenderer.pos(d10, bottomOffset, d11).tex(0.0, d14).color(r, g, b, a).endVertex();
            worldRenderer.pos(d10, topOffset, d11).tex(0.0, d15).color(r, g, b, a).endVertex();

            worldRenderer.pos(d8, topOffset, d9).tex(1.0, d15).color(r, g, b, a).endVertex();
            worldRenderer.pos(d8, bottomOffset, d9).tex(1.0, d14).color(r, g, b, a).endVertex();
            worldRenderer.pos(d4, bottomOffset, d5).tex(0.0, d14).color(r, g, b, a).endVertex();
            worldRenderer.pos(d4, topOffset, d5).tex(0.0, d15).color(r, g, b, a).endVertex();

            tessellator.draw();

            // Inner glow rendering
            GlStateManager.disableCull();
            double d12 = -1.0 + d1;
            double d13 = height + d12;

            worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);

            float glowAlpha = 0.25f;
            double glowRadius = 0.2;

            worldRenderer.pos(-glowRadius, topOffset, -glowRadius).tex(1.0, d13).color(r, g, b, glowAlpha).endVertex();
            worldRenderer.pos(-glowRadius, bottomOffset, -glowRadius).tex(1.0, d12).color(r, g, b, glowAlpha).endVertex();
            worldRenderer.pos(glowRadius, bottomOffset, -glowRadius).tex(0.0, d12).color(r, g, b, glowAlpha).endVertex();
            worldRenderer.pos(glowRadius, topOffset, -glowRadius).tex(0.0, d13).color(r, g, b, glowAlpha).endVertex();

            worldRenderer.pos(glowRadius, topOffset, glowRadius).tex(1.0, d13).color(r, g, b, glowAlpha).endVertex();
            worldRenderer.pos(glowRadius, bottomOffset, glowRadius).tex(1.0, d12).color(r, g, b, glowAlpha).endVertex();
            worldRenderer.pos(-glowRadius, bottomOffset, glowRadius).tex(0.0, d12).color(r, g, b, glowAlpha).endVertex();
            worldRenderer.pos(-glowRadius, topOffset, glowRadius).tex(0.0, d13).color(r, g, b, glowAlpha).endVertex();

            worldRenderer.pos(glowRadius, topOffset, -glowRadius).tex(1.0, d13).color(r, g, b, glowAlpha).endVertex();
            worldRenderer.pos(glowRadius, bottomOffset, -glowRadius).tex(1.0, d12).color(r, g, b, glowAlpha).endVertex();
            worldRenderer.pos(glowRadius, bottomOffset, glowRadius).tex(0.0, d12).color(r, g, b, glowAlpha).endVertex();
            worldRenderer.pos(glowRadius, topOffset, glowRadius).tex(0.0, d13).color(r, g, b, glowAlpha).endVertex();

            worldRenderer.pos(-glowRadius, topOffset, glowRadius).tex(1.0, d13).color(r, g, b, glowAlpha).endVertex();
            worldRenderer.pos(-glowRadius, bottomOffset, glowRadius).tex(1.0, d12).color(r, g, b, glowAlpha).endVertex();
            worldRenderer.pos(-glowRadius, bottomOffset, -glowRadius).tex(0.0, d12).color(r, g, b, glowAlpha).endVertex();
            worldRenderer.pos(-glowRadius, topOffset, -glowRadius).tex(0.0, d13).color(r, g, b, glowAlpha).endVertex();

            tessellator.draw();

            // Restore GL state
            GlStateManager.enableCull();
            GlStateManager.depthMask(true);
            GlStateManager.enableLighting();
            GlStateManager.disableBlend();
            if (!depthCheck) {
                GlStateManager.enableDepth();
            }
        } finally {
            // Ensure these are always called in reverse order
            GlStateManager.popAttrib();
            GlStateManager.popMatrix();
        }
    }

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