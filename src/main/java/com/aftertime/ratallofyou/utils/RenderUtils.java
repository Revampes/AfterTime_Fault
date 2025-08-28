package com.aftertime.ratallofyou.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

public class RenderUtils {
    private static final Minecraft mc = Minecraft.getMinecraft();
    private static final ResourceLocation beaconBeam = new ResourceLocation("textures/entity/beacon_beam.png");

    // BoxRenderer methods
    public static void drawEspBox(AxisAlignedBB box, float red, float green, float blue, float alpha, float lineWidth) {
        GlStateManager.pushMatrix();
        GlStateManager.pushAttrib();

        try {
            Tessellator tessellator = Tessellator.getInstance();
            WorldRenderer worldRenderer = tessellator.getWorldRenderer();

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
            Tessellator.getInstance().draw();

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
            Tessellator.getInstance().draw();

            // Top square
            worldRenderer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
            worldRenderer.pos(minX, maxY, minZ).color(red, green, blue, alpha).endVertex();
            worldRenderer.pos(maxX, maxY, minZ).color(red, green, blue, alpha).endVertex();
            worldRenderer.pos(maxX, maxY, maxZ).color(red, green, blue, alpha).endVertex();
            worldRenderer.pos(minX, maxY, maxZ).color(red, green, blue, alpha).endVertex();
            worldRenderer.pos(minX, maxY, minZ).color(red, green, blue, alpha).endVertex();
            Tessellator.getInstance().draw();
        } finally {
            GlStateManager.enableDepth();
            GlStateManager.enableTexture2D();
            GlStateManager.disableBlend();
            GlStateManager.enableAlpha();
            GlStateManager.popAttrib();
            GlStateManager.popMatrix();
        }
    }

    public static void drawEntityEspBox(double x, double y, double z,
                                        double width, double height,
                                        float red, float green, float blue,
                                        float yOffset) {
        AxisAlignedBB box = new AxisAlignedBB(
                x - width/2, y + yOffset, z - width/2,
                x + width/2, y + yOffset + height, z + width/2
        );
        drawEspBox(box, red, green, blue, 1.0f, 2.0f);
    }

    public static void drawEntityBox(Entity entity, float red, float green, float blue, float alpha, float lineWidth, float partialTicks) {
        final RenderManager renderManager = mc.getRenderManager();

        GlStateManager.pushMatrix();
        GlStateManager.pushAttrib();

        try {
            GlStateManager.disableTexture2D();
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GlStateManager.depthMask(false);
            GL11.glLineWidth(lineWidth);
            GlStateManager.disableLighting();

            double x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks;
            double y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks;
            double z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks;

            AxisAlignedBB box = entity.getEntityBoundingBox()
                    .expand(0.05, 0.15, 0.05);

            drawEspBox(box, red, green, blue, alpha, lineWidth);
        } finally {
            GlStateManager.depthMask(true);
            GlStateManager.enableTexture2D();
            GlStateManager.disableBlend();
            GlStateManager.enableLighting();
            GlStateManager.popAttrib();
            GlStateManager.popMatrix();
        }
    }

    // BeaconBeamRenderer methods
    public static void renderBeaconBeam(Vec3 position, Color color, boolean depthCheck, float height, float partialTicks) {
        if (color.getAlpha() == 0) return;

        GlStateManager.pushMatrix();
        GlStateManager.pushAttrib();

        try {
            float bottomOffset = 0;
            float topOffset = bottomOffset + height;

            GlStateManager.depthMask(false);
            if (!depthCheck) {
                GlStateManager.disableDepth();
            }

            mc.getTextureManager().bindTexture(beaconBeam);

            GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, 10497.0f);
            GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, 10497.0f);

            GlStateManager.disableLighting();
            // Disable culling so the core beam is always visible from any angle
            GlStateManager.disableCull();
            GlStateManager.enableTexture2D();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);

            GlStateManager.translate(
                    position.xCoord - mc.getRenderManager().viewerPosX,
                    position.yCoord - mc.getRenderManager().viewerPosY,
                    position.zCoord - mc.getRenderManager().viewerPosZ
            );

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

            float r = color.getRed() / 255f;
            float g = color.getGreen() / 255f;
            float b = color.getBlue() / 255f;
            float a = color.getAlpha() / 255f;

            Tessellator tessellator = Tessellator.getInstance();
            WorldRenderer worldRenderer = tessellator.getWorldRenderer();

            worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);

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

            // For glow we also want culling disabled (already disabled)
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

            // Restore GL state for depth/cull etc.
            GlStateManager.enableCull();
            GlStateManager.depthMask(true);
            GlStateManager.enableLighting();
            GlStateManager.disableBlend();
            if (!depthCheck) {
                GlStateManager.enableDepth();
            }
        } finally {
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

    // HitBoxRenderer methods
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
        boolean cullFace = GL11.glIsEnabled(GL11.GL_CULL_FACE);
        boolean depthTest = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
        boolean prevBlend = GL11.glIsEnabled(GL11.GL_BLEND);
        boolean prevTex = GL11.glIsEnabled(GL11.GL_TEXTURE_2D);
        boolean prevLighting = GL11.glIsEnabled(GL11.GL_LIGHTING);
        boolean prevDepthMask = GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK);

        try {
            GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
            GlStateManager.pushMatrix();

            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GlStateManager.disableTexture2D();
            GlStateManager.disableLighting();
            GlStateManager.depthMask(false);
            GL11.glLineWidth(lineWidth);
            GlStateManager.color(r, g, b, a);
            GL11.glDisable(GL11.GL_CULL_FACE);

            if (phase) {
                GlStateManager.disableDepth();
            } else {
                GlStateManager.enableDepth();
            }

            GlStateManager.translate(
                    -Minecraft.getMinecraft().getRenderManager().viewerPosX,
                    -Minecraft.getMinecraft().getRenderManager().viewerPosY,
                    -Minecraft.getMinecraft().getRenderManager().viewerPosZ
            );

            if (filled) {
                GL11.glEnable(GL11.GL_POLYGON_OFFSET_FILL);
                GL11.glPolygonOffset(1.0f, -2000000.0f);

                Tessellator tessellator = Tessellator.getInstance();
                WorldRenderer worldRenderer = tessellator.getWorldRenderer();

                worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);

                worldRenderer.pos(x0, y0, z0).endVertex();
                worldRenderer.pos(x1, y0, z0).endVertex();
                worldRenderer.pos(x1, y0, z1).endVertex();
                worldRenderer.pos(x0, y0, z1).endVertex();

                worldRenderer.pos(x0, y1, z0).endVertex();
                worldRenderer.pos(x1, y1, z0).endVertex();
                worldRenderer.pos(x1, y1, z1).endVertex();
                worldRenderer.pos(x0, y1, z1).endVertex();

                worldRenderer.pos(x0, y0, z0).endVertex();
                worldRenderer.pos(x0, y1, z0).endVertex();
                worldRenderer.pos(x1, y1, z0).endVertex();
                worldRenderer.pos(x1, y0, z0).endVertex();

                worldRenderer.pos(x0, y0, z1).endVertex();
                worldRenderer.pos(x0, y1, z1).endVertex();
                worldRenderer.pos(x1, y1, z1).endVertex();
                worldRenderer.pos(x1, y0, z1).endVertex();

                worldRenderer.pos(x0, y0, z0).endVertex();
                worldRenderer.pos(x0, y0, z1).endVertex();
                worldRenderer.pos(x0, y1, z1).endVertex();
                worldRenderer.pos(x0, y1, z0).endVertex();

                worldRenderer.pos(x1, y0, z0).endVertex();
                worldRenderer.pos(x1, y0, z1).endVertex();
                worldRenderer.pos(x1, y1, z1).endVertex();
                worldRenderer.pos(x1, y1, z0).endVertex();

                tessellator.draw();

                GL11.glDisable(GL11.GL_POLYGON_OFFSET_FILL);
            }

            Tessellator tessellator = Tessellator.getInstance();
            WorldRenderer worldRenderer = tessellator.getWorldRenderer();

            worldRenderer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION);

            worldRenderer.pos(x0, y0, z0).endVertex();
            worldRenderer.pos(x0, y0, z1).endVertex();
            worldRenderer.pos(x1, y0, z1).endVertex();
            worldRenderer.pos(x1, y0, z0).endVertex();
            worldRenderer.pos(x0, y0, z0).endVertex();

            worldRenderer.pos(x0, y1, z0).endVertex();
            worldRenderer.pos(x0, y1, z1).endVertex();
            worldRenderer.pos(x1, y1, z1).endVertex();
            worldRenderer.pos(x1, y1, z0).endVertex();
            worldRenderer.pos(x0, y1, z0).endVertex();

            worldRenderer.pos(x0, y1, z1).endVertex();
            worldRenderer.pos(x0, y0, z1).endVertex();

            worldRenderer.pos(x1, y0, z1).endVertex();
            worldRenderer.pos(x1, y1, z1).endVertex();

            worldRenderer.pos(x1, y1, z0).endVertex();
            worldRenderer.pos(x1, y0, z0).endVertex();

            tessellator.draw();
        } finally {
            // Explicitly restore modified GL state to avoid leaking into other renderers
            if (cullFace) GL11.glEnable(GL11.GL_CULL_FACE); else GL11.glDisable(GL11.GL_CULL_FACE);
            if (depthTest) GL11.glEnable(GL11.GL_DEPTH_TEST); else GL11.glDisable(GL11.GL_DEPTH_TEST);
            if (prevBlend) GlStateManager.enableBlend(); else GlStateManager.disableBlend();
            if (prevTex) GlStateManager.enableTexture2D(); else GlStateManager.disableTexture2D();
            if (prevLighting) GlStateManager.enableLighting(); else GlStateManager.disableLighting();
            GlStateManager.depthMask(prevDepthMask);

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

    // New: floating text at world position, camera-facing
    public static void renderFloatingText(String text, double x, double y, double z, float scale, int color, boolean depthTest) {
        if (text == null || text.isEmpty()) return;
        RenderManager rm = mc.getRenderManager();
        FontRenderer fr = mc.fontRendererObj;

        GlStateManager.pushMatrix();
        GlStateManager.translate(x - rm.viewerPosX, y - rm.viewerPosY, z - rm.viewerPosZ);
        GlStateManager.rotate(-rm.playerViewY, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(rm.playerViewX, 1.0F, 0.0F, 0.0F);
        GlStateManager.scale(-0.025F * scale, -0.025F * scale, 0.025F * scale);

        GlStateManager.disableLighting();
        if (!depthTest) GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);

        int width = fr.getStringWidth(text) / 2;
        GlStateManager.disableTexture2D();
        Tessellator tess = Tessellator.getInstance();
        WorldRenderer wr = tess.getWorldRenderer();
        wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        // background rectangle (semi-transparent black)
        wr.pos(-width - 2, -2, 0).color(0F, 0F, 0F, 0.4F).endVertex();
        wr.pos(-width - 2, 9, 0).color(0F, 0F, 0F, 0.4F).endVertex();
        wr.pos(width + 2, 9, 0).color(0F, 0F, 0F, 0.4F).endVertex();
        wr.pos(width + 2, -2, 0).color(0F, 0F, 0F, 0.4F).endVertex();
        tess.draw();
        GlStateManager.enableTexture2D();

        fr.drawString(text, -width, 0, color, true);

        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }

    // New: floating text at world position, camera-facing, constant on-screen size
    public static void renderFloatingTextConstant(String text, double x, double y, double z, float pixelScale, int color, boolean depthTest) {
        if (text == null || text.isEmpty()) return;
        RenderManager rm = mc.getRenderManager();
        FontRenderer fr = mc.fontRendererObj;

        double dx = x - rm.viewerPosX;
        double dy = y - rm.viewerPosY;
        double dz = z - rm.viewerPosZ;
        float dist = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        // Scale strictly with distance so on-screen size remains constant regardless of distance
        float s = pixelScale * dist;

        GlStateManager.pushMatrix();
        GlStateManager.translate(dx, dy, dz);
        GlStateManager.rotate(-rm.playerViewY, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(rm.playerViewX, 1.0F, 0.0F, 0.0F);
        GlStateManager.scale(-0.025F * s, -0.025F * s, 0.025F * s);

        GlStateManager.disableLighting();
        if (!depthTest) GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);

        int width = fr.getStringWidth(text) / 2;
        GlStateManager.disableTexture2D();
        Tessellator tess = Tessellator.getInstance();
        WorldRenderer wr = tess.getWorldRenderer();
        wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        // background rectangle (semi-transparent black)
        wr.pos(-width - 2, -2, 0).color(0F, 0F, 0F, 0.4F).endVertex();
        wr.pos(-width - 2, 9, 0).color(0F, 0F, 0F, 0.4F).endVertex();
        wr.pos(width + 2, 9, 0).color(0F, 0F, 0F, 0.4F).endVertex();
        wr.pos(width + 2, -2, 0).color(0F, 0F, 0F, 0.4F).endVertex();
        tess.draw();
        GlStateManager.enableTexture2D();

        fr.drawString(text, -width, 0, color, true);

        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }
}