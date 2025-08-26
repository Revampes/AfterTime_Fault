package com.aftertime.ratallofyou.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.lwjgl.input.Keyboard;

/**
 * Utilities to support Hotbar swapping logic converted from the JS utilities.
 */
public class HotbarSwapUtils {
    public static final int NOT_FOUND = -1;

    private static final Minecraft mc = Minecraft.getMinecraft();

    // Cached movement key codes: forward, back, left, right
    public static int[] getMovementKeyCodes() {
        try {
            return new int[]{
                    mc.gameSettings.keyBindForward.getKeyCode(),
                    mc.gameSettings.keyBindBack.getKeyCode(),
                    mc.gameSettings.keyBindLeft.getKeyCode(),
                    mc.gameSettings.keyBindRight.getKeyCode()
            };
        } catch (Throwable t) {
            return new int[]{};
        }
    }

    public static void stopInputs() {
        for (int keyCode : getMovementKeyCodes()) {
            setKeyState(keyCode, false);
        }
    }

    public static void restartMovement() {
        for (int keyCode : getMovementKeyCodes()) {
            setKeyState(keyCode, Keyboard.isKeyDown(keyCode));
        }
    }

    public static void setKeyState(int key, boolean state) {
        try {
            KeyBinding.setKeyBindState(key, state);
        } catch (Throwable ignored) {
        }
    }

    public static String getUUID(ItemStack stack) {
        if (stack == null) return null;
        try {
            NBTTagCompound tag = stack.getTagCompound();
            if (tag == null) return null;
            if (!tag.hasKey("ExtraAttributes", 10)) return null; // 10 = compound
            NBTTagCompound ea = tag.getCompoundTag("ExtraAttributes");
            String s = ea.getString("uuid");
            return s != null && !s.isEmpty() ? s : null;
        } catch (Throwable t) {
            return null;
        }
    }

    public static String getSkyblockID(ItemStack stack) {
        if (stack == null) return null;
        try {
            NBTTagCompound tag = stack.getTagCompound();
            if (tag == null) return null;
            if (!tag.hasKey("ExtraAttributes", 10)) return null;
            NBTTagCompound ea = tag.getCompoundTag("ExtraAttributes");
            String s = ea.getString("id");
            return s != null && !s.isEmpty() ? s : null;
        } catch (Throwable t) {
            return null;
        }
    }
}
