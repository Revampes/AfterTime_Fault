package com.aftertime.ratallofyou.modules.render;

import com.aftertime.ratallofyou.UI.config.ConfigData.AllConfig;

import com.aftertime.ratallofyou.UI.config.ConfigData.DataType_DropDown;
import com.aftertime.ratallofyou.UI.config.ConfigData.ModuleInfo;
import com.aftertime.ratallofyou.utils.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class EtherwarpOverlay {
    private static final Minecraft mc = Minecraft.getMinecraft();
    private static final float SNEAK_EYE_HEIGHT = 1.54f;
    private static final float PADDING = 0.005f;

    // Valid blocks for etherwarp feet (air by default)
    private static final int[] VALID_ETHERWARP_BLOCKS = {0}; // Air

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        onRenderWorld();
    }

    public static void onRenderWorld() {
        if (!isModuleEnabled() || mc.thePlayer == null || mc.theWorld == null) return;
        if (etherwarpOverlayOnlySneak() && !mc.thePlayer.isSneaking() && !isHoldingEtherwarpConduit()) return;
        if (!isHoldingAOTV() && !isHoldingEtherwarpConduit()) return;

        if (etherwarpSyncWithServer()) {
            doSyncedEther();
        } else {
            doSmoothEther();
        }
    }

    private static boolean isHoldingAOTV() {
        return mc.thePlayer != null
                && mc.thePlayer.getHeldItem() != null
                && (mc.thePlayer.getHeldItem().getDisplayName().contains("Aspect of the Void")
                || mc.thePlayer.getHeldItem().getDisplayName().contains("AOTV"));
    }

    private static boolean isHoldingEtherwarpConduit() {
        return mc.thePlayer != null
                && mc.thePlayer.getHeldItem() != null
                && mc.thePlayer.getHeldItem().getDisplayName().contains("Etherwarp Conduit");
    }

    private static void doSyncedEther() {
        Vec3 lastPos = getLastSentCoord();
        float[] lastLook = getLastSentLook();
        if (lastPos == null || lastLook == null) return;

        float x0 = (float) lastPos.xCoord;
        float y0 = (float) lastPos.yCoord + SNEAK_EYE_HEIGHT;
        float z0 = (float) lastPos.zCoord;

        Vec3 lookVec = fromPitchYaw(lastLook[0], lastLook[1]);
        lookVec = new Vec3(lookVec.xCoord * 61, lookVec.yCoord * 61, lookVec.zCoord * 61);

        float x1 = x0 + (float) lookVec.xCoord;
        float y1 = y0 + (float) lookVec.yCoord;
        float z1 = z0 + (float) lookVec.zCoord;

        handleEtherBlock(simEtherwarp(x0, y0, z0, x1, y1, z1));
    }

    private static void doSmoothEther() {
        float x0 = (float) mc.thePlayer.posX;
        float y0 = (float) mc.thePlayer.posY + SNEAK_EYE_HEIGHT;
        float z0 = (float) mc.thePlayer.posZ;

        Vec3 lookVec = fromPitchYaw(mc.thePlayer.rotationPitch, mc.thePlayer.rotationYaw);
        lookVec = new Vec3(lookVec.xCoord * 61, lookVec.yCoord * 61, lookVec.zCoord * 61);

        float x1 = x0 + (float) lookVec.xCoord;
        float y1 = y0 + (float) lookVec.yCoord;
        float z1 = z0 + (float) lookVec.zCoord;

        handleEtherBlock(simEtherwarp(x0, y0, z0, x1, y1, z1));
    }

    private static Vec3 fromPitchYaw(float pitch, float yaw) {
        float f = MathHelper.cos(-yaw * 0.017453292F - (float)Math.PI);
        float f1 = MathHelper.sin(-yaw * 0.017453292F - (float)Math.PI);
        float f2 = -MathHelper.cos(-pitch * 0.017453292F);
        float f3 = MathHelper.sin(-pitch * 0.017453292F);
        return new Vec3((double)(f1 * f2), (double)f3, (double)(f * f2));
    }

    private static Vec3 getLastSentCoord() {
        return new Vec3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
    }

    private static float[] getLastSentLook() {
        return new float[]{mc.thePlayer.rotationPitch, mc.thePlayer.rotationYaw};
    }

    private static EtherwarpResult simEtherwarp(float x0, float y0, float z0, float x1, float y1, float z1) {
        // Initialize position
        int x = MathHelper.floor_float(x0);
        int y = MathHelper.floor_float(y0);
        int z = MathHelper.floor_float(z0);

        int endX = MathHelper.floor_float(x1);
        int endY = MathHelper.floor_float(y1);
        int endZ = MathHelper.floor_float(z1);

        float dirX = x1 - x0;
        float dirY = y1 - y0;
        float dirZ = z1 - z0;

        int stepX = (int) Math.signum(dirX);
        int stepY = (int) Math.signum(dirY);
        int stepZ = (int) Math.signum(dirZ);

        float thingX = 1 / dirX;
        float thingY = 1 / dirY;
        float thingZ = 1 / dirZ;

        float tDeltaX = Math.min(thingX * stepX, 1);
        float tDeltaY = Math.min(thingY * stepY, 1);
        float tDeltaZ = Math.min(thingZ * stepZ, 1);

        float tMaxX = Math.abs((x + Math.max(stepX, 0) - x0) * thingX);
        float tMaxY = Math.abs((y + Math.max(stepY, 0) - y0) * thingY);
        float tMaxZ = Math.abs((z + Math.max(stepZ, 0) - z0) * thingZ);

        int iters = 0;
        World world = mc.theWorld;

        while (iters++ < 1000) {
            // Do block check
            BlockPos currentPos = new BlockPos(x, y, z);
            int currentId = world.getBlockState(currentPos).getBlock().getIdFromBlock(world.getBlockState(currentPos).getBlock());

            // Check if block is valid
            boolean isValidBlock = false;
            for (int validId : VALID_ETHERWARP_BLOCKS) {
                if (currentId == validId) {
                    isValidBlock = true;
                    break;
                }
            }

            // End Reached
            if (!isValidBlock) {
                // Check if block above is valid for feet
                BlockPos footPos = new BlockPos(x, y + 1, z);
                int footId = world.getBlockState(footPos).getBlock().getIdFromBlock(world.getBlockState(footPos).getBlock());

                boolean isFootValid = false;
                for (int validId : VALID_ETHERWARP_BLOCKS) {
                    if (footId == validId) {
                        isFootValid = true;
                        break;
                    }
                }

                if (!isFootValid) {
                    return new EtherwarpResult(false, x, y, z);
                }

                // Check head block
                BlockPos headPos = new BlockPos(x, y + 2, z);
                int headId = world.getBlockState(headPos).getBlock().getIdFromBlock(world.getBlockState(headPos).getBlock());

                boolean isHeadValid = false;
                for (int validId : VALID_ETHERWARP_BLOCKS) {
                    if (headId == validId) {
                        isHeadValid = true;
                        break;
                    }
                }

                if (!isHeadValid) {
                    return new EtherwarpResult(false, x, y, z);
                }

                return new EtherwarpResult(true, x, y, z);
            }

            // End Reached without finding a block
            if (x == endX && y == endY && z == endZ) {
                return null;
            }

            // Find next direction to step
            if (tMaxX < tMaxY) {
                if (tMaxX < tMaxZ) {
                    tMaxX += tDeltaX;
                    x += stepX;
                } else {
                    tMaxZ += tDeltaZ;
                    z += stepZ;
                }
            } else {
                if (tMaxY < tMaxZ) {
                    tMaxY += tDeltaY;
                    y += stepY;
                } else {
                    tMaxZ += tDeltaZ;
                    z += stepZ;
                }
            }
        }

        return null;
    }

    private static void handleEtherBlock(EtherwarpResult result) {
        if (result == null) return;

        if (!result.success) {
            if (!etherwarpShowFailLocation()) return;

            Color fail = etherwarpOverlayFailColor();
            renderEtherBlock(
                    result.x, result.y, result.z,
                    fail.getRed() / 255f,
                    fail.getGreen() / 255f,
                    fail.getBlue() / 255f,
                    fail.getAlpha() / 255f
            );
            return;
        }

        Color ok = etherwarpOverlayColor();
        renderEtherBlock(
                result.x, result.y, result.z,
                ok.getRed() / 255f,
                ok.getGreen() / 255f,
                ok.getBlue() / 255f,
                ok.getAlpha() / 255f
        );
    }

    private static void renderEtherBlock(int x, int y, int z, float r, float g, float b, float a) {
        float minX = x - PADDING;
        float minY = y - PADDING;
        float minZ = z - PADDING;
        float maxX = x + 1 + PADDING;
        float maxY = y + 1 + PADDING;
        float maxZ = z + 1 + PADDING;

        // Save current state
        GlStateManager.pushMatrix();
        GlStateManager.pushAttrib();

        try {
            // Common setup
            GlStateManager.disableTexture2D();
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glLineWidth(2.0f);

            // Enable depth testing but disable depth writes for filled rendering
            GlStateManager.enableDepth();

            int mode = etherwarpHighlightType();
            switch (mode) {
                case 0: // Edges Only
                    GlStateManager.depthMask(true);
                    RenderUtils.renderBoxFromCorners(
                            minX, minY, minZ, maxX, maxY, maxZ,
                            r, g, b, a, false, 2.0f, false
                    );
                    break;

                case 1: // Filled Only
                    GlStateManager.depthMask(false); // Disable depth writes for filled
                    RenderUtils.renderBoxFromCorners(
                            minX, minY, minZ, maxX, maxY, maxZ,
                            r, g, b, a, false, 2.0f, true
                    );
                    break;

                case 2: // Both
                    // First render filled (with depth writes disabled)
                    GlStateManager.depthMask(false);
                    RenderUtils.renderBoxFromCorners(
                            minX, minY, minZ, maxX, maxY, maxZ,
                            r, g, b, a/2, false, 2.0f, true
                    );

                    // Then render outline (with depth writes enabled)
                    GlStateManager.depthMask(true);
                    RenderUtils.renderBoxFromCorners(
                            minX, minY, minZ, maxX, maxY, maxZ,
                            r, g, b, a, false, 2.0f, false
                    );
                    break;
            }
        } finally {
            // Restore GL state even if rendering fails
            GlStateManager.depthMask(true);
            GlStateManager.enableTexture2D();
            GlStateManager.disableBlend();
            GlStateManager.popAttrib();
            GlStateManager.popMatrix();
        }
    }

    // Config helpers
    private static boolean isModuleEnabled() {
        ModuleInfo cfg = (ModuleInfo) AllConfig.INSTANCE.MODULES.get("render_etherwarpoverlay");
        return cfg != null && Boolean.TRUE.equals(cfg.Data);
    }

    private static boolean etherwarpOverlayOnlySneak() {
        Object v = AllConfig.INSTANCE.ETHERWARP_CONFIGS.get("etherwarp_only_show_when_sneak").Data;
        return v instanceof Boolean ? (Boolean) v : true; // default true per AllConfig
    }

    private static boolean etherwarpSyncWithServer() {
        Object v = AllConfig.INSTANCE.ETHERWARP_CONFIGS.get("etherwarp_sync_with_server").Data;
        return v instanceof Boolean ? (Boolean) v : false;
    }

    private static boolean etherwarpShowFailLocation() {
        Object v = AllConfig.INSTANCE.ETHERWARP_CONFIGS.get("etherwarp_show_fail_location").Data;
        return v instanceof Boolean ? (Boolean) v : true; // default true per AllConfig
    }

    private static int etherwarpHighlightType() {
        Object v = AllConfig.INSTANCE.ETHERWARP_CONFIGS.get("etherwarp_render_method").Data;
        if (v instanceof DataType_DropDown) {
            DataType_DropDown dd = (DataType_DropDown) v;
            return Math.max(0, Math.min(2, dd.selectedIndex));
        }
        return 0; // Edges by default
    }

    private static Color etherwarpOverlayColor() {
        Object v = AllConfig.INSTANCE.ETHERWARP_CONFIGS.get("etherwarp_OverlayColor").Data;
        return v instanceof Color ? (Color) v : new Color(0, 255, 0, 200);
    }

    private static Color etherwarpOverlayFailColor() {
        Object v = AllConfig.INSTANCE.ETHERWARP_CONFIGS.get("etherwarp_OverlayFailColor").Data;
        return v instanceof Color ? (Color) v : new Color(180, 0, 0, 200);
    }

    // Result container
    private static class EtherwarpResult {
        final boolean success;
        final int x, y, z;
        EtherwarpResult(boolean success, int x, int y, int z) {
            this.success = success;
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }
}
