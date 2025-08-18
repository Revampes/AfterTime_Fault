package com.aftertime.ratallofyou.modules.dungeon.terminals;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

/**
 * Common helpers and constants shared by dungeon terminal custom GUIs.
 */
public final class TerminalGuiCommon {
    private TerminalGuiCommon() {}

    public static final int[] ALLOWED_SLOTS = new int[]{
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };

    public static int getChestWindowSize(GuiChest chest) {
        try {
            Container cont = chest.inventorySlots;
            if (cont instanceof ContainerChest) {
                try {
                    Slot s0 = cont.getSlot(0);
                    if (s0 != null && s0.inventory != null) {
                        return s0.inventory.getSizeInventory();
                    }
                } catch (Throwable ignored) {}
                return Math.min(54, Math.max(9, cont.inventorySlots.size() - 36));
            }
        } catch (Throwable ignored) {}
        // Reflection fallback
        try {
            Object lower = ReflectionHelper.getPrivateValue(GuiChest.class, chest, "lowerChestInventory", "field_147015_w");
            if (lower != null) {
                return (Integer) lower.getClass().getMethod("getSizeInventory").invoke(lower);
            }
        } catch (Throwable ignored) {}
        return 54;
    }

    public static String getChestTitle(GuiChest chest) {
        try {
            Container cont = chest.inventorySlots;
            if (cont instanceof ContainerChest) {
                try {
                    Object lowerInv = ContainerChest.class.getMethod("getLowerChestInventory").invoke(cont);
                    if (lowerInv instanceof IInventory) {
                        IChatComponent comp = ((IInventory) lowerInv).getDisplayName();
                        if (comp != null) return comp.getUnformattedText();
                    }
                } catch (Throwable ignored) {}
                try {
                    Object lower = ReflectionHelper.getPrivateValue(ContainerChest.class, (ContainerChest) cont, "lowerChestInventory", "field_75155_e");
                    if (lower instanceof IInventory) {
                        IChatComponent comp = ((IInventory) lower).getDisplayName();
                        if (comp != null) return comp.getUnformattedText();
                    }
                } catch (Throwable ignored) {}
                try {
                    if (cont.inventorySlots != null && !cont.inventorySlots.isEmpty()) {
                        Slot s0 = cont.getSlot(0);
                        if (s0 != null && s0.inventory != null) {
                            IChatComponent comp = s0.inventory.getDisplayName();
                            if (comp != null) return comp.getUnformattedText();
                        }
                    }
                } catch (Throwable ignored) {}
            }
        } catch (Throwable ignored) {}
        try {
            Object lower = ReflectionHelper.getPrivateValue(GuiChest.class, chest, "lowerChestInventory", "field_147015_w");
            if (lower instanceof IInventory) {
                IChatComponent comp = ((IInventory) lower).getDisplayName();
                if (comp != null) return comp.getUnformattedText();
            } else if (lower != null) {
                Object comp = lower.getClass().getMethod("getDisplayName").invoke(lower);
                if (comp != null) {
                    try {
                        return (String) comp.getClass().getMethod("getUnformattedText").invoke(comp);
                    } catch (Throwable ignored) {}
                }
            }
        } catch (Throwable ignored) {}
        return null;
    }

    // Simple filled-rect helper (ARGB)
    public static void drawRect(int left, int top, int right, int bottom, int color) {
        net.minecraft.client.gui.Gui.drawRect(left, top, right, bottom, color);
    }
}

