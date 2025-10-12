package com.aftertime.ratallofyou.modules.SkyBlock;

import com.aftertime.ratallofyou.config.ModConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;

public class MarkLocation {
    private static final Minecraft mc = Minecraft.getMinecraft();

    private boolean isModuleEnabled() {
        return ModConfig.enabledMarkLocation;
    }

    private int cfgHotkey() {
        // ModConfig.markLocationKeyBind is a key name like "U"; map to LWJGL code
        String name = ModConfig.markLocationKeyBind == null ? "" : ModConfig.markLocationKeyBind.trim();
        if (name.isEmpty()) return 0;
        try {
            int code = Keyboard.getKeyIndex(name.toUpperCase());
            return code > 0 ? code : 0;
        } catch (Throwable ignored) {
            return 0;
        }
    }

    // Hotkey handler - similar to AutoFish
    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        try {
            if (mc == null || mc.thePlayer == null) return;
            if (mc.currentScreen != null) return; // avoid triggering while typing in GUIs
            if (!isModuleEnabled()) return; // module must be enabled

            int bound = cfgHotkey();
            if (bound <= 0) return; // not bound
            if (!Keyboard.getEventKeyState()) return; // only on key down
            int key = Keyboard.getEventKey();
            if (key != bound) return;

            // Get the player's looking position (raytrace)
            Vec3 lookingPos = getPlayerLookingPosition();
            if (lookingPos != null) {
                sendLocationToPartyChat(lookingPos);
            }
        } catch (Throwable ignored) {}
    }

    /**
     * Get the position the player is looking at
     */
    private Vec3 getPlayerLookingPosition() {
        if (mc.thePlayer == null || mc.theWorld == null) return null;

        // Use raytrace to find what block the player is looking at
        MovingObjectPosition result = mc.thePlayer.rayTrace(61.0D, 1.0F); // 61 blocks

        if (result != null && result.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
            BlockPos pos = result.getBlockPos();
            return new Vec3(pos.getX(), pos.getY(), pos.getZ());
        }

        // Fallback: calculate position based on player's look direction
        float x0 = (float) mc.thePlayer.posX;
        float y0 = (float) mc.thePlayer.posY + 1.54f; // eye height
        float z0 = (float) mc.thePlayer.posZ;

        Vec3 lookVec = fromPitchYaw(mc.thePlayer.rotationPitch, mc.thePlayer.rotationYaw);
        lookVec = new Vec3(lookVec.xCoord * 61, lookVec.yCoord * 61, lookVec.zCoord * 61);

        float x1 = x0 + (float) lookVec.xCoord;
        float y1 = y0 + (float) lookVec.yCoord;
        float z1 = z0 + (float) lookVec.zCoord;

        return new Vec3(x1, y1, z1);
    }

    private static Vec3 fromPitchYaw(float pitch, float yaw) {
        float f = MathHelper.cos(-yaw * 0.017453292F - (float)Math.PI);
        float f1 = MathHelper.sin(-yaw * 0.017453292F - (float)Math.PI);
        float f2 = -MathHelper.cos(-pitch * 0.017453292F);
        float f3 = MathHelper.sin(-pitch * 0.017453292F);
        return new Vec3((double)(f1 * f2), (double)f3, (double)(f * f2));
    }

    private void sendLocationToPartyChat(Vec3 pos) {
        if (mc.thePlayer == null) return;

        int x = MathHelper.floor_double(pos.xCoord);
        int y = MathHelper.floor_double(pos.yCoord);
        int z = MathHelper.floor_double(pos.zCoord);

        String message = String.format("/pc x: %d, y: %d, z: %d", x, y, z);
        mc.thePlayer.sendChatMessage(message);
    }
}
