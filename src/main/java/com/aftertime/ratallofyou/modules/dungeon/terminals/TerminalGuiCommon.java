package com.aftertime.ratallofyou.modules.dungeon.terminals;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.lwjgl.input.Mouse;

import java.util.Deque;
import java.util.List;

/**
 * Common helpers and constants shared by dungeon terminal custom GUIs.
 */
public final class TerminalGuiCommon {
    private TerminalGuiCommon() {}

    // Force-load certain terminal classes so their static initializers can register event listeners
    static {
        try { Class.forName("com.aftertime.ratallofyou.modules.dungeon.terminals.numbers"); } catch (Throwable ignored) {}
        try { Class.forName("com.aftertime.ratallofyou.modules.dungeon.terminals.startswith"); } catch (Throwable ignored) {}
        try { Class.forName("com.aftertime.ratallofyou.modules.dungeon.terminals.Colors"); } catch (Throwable ignored) {}
        try { Class.forName("com.aftertime.ratallofyou.modules.dungeon.terminals.redgreen"); } catch (Throwable ignored) {}
        try { Class.forName("com.aftertime.ratallofyou.modules.dungeon.terminals.rubix"); } catch (Throwable ignored) {}
        try { Class.forName("com.aftertime.ratallofyou.modules.dungeon.terminals.melody"); } catch (Throwable ignored) {}
    }

    // ===================== Shared defaults/config =====================
    public static final class Defaults {
        public static boolean highPingMode = false;
        public static boolean phoenixClientCompat = false;
        public static int timeoutMs = 500;
        public static int firstClickBlockMs = 0;
        public static float scale = 1.0f;
        public static int offsetX = 0;
        public static int offsetY = 0;
        public static int overlayColor = 0xFF00FF00;    // opaque green
        public static int backgroundColor = 0x7F000000; // semi-transparent black
        public static int cornerRadiusBg = 1;           // background panel radius
        public static int cornerRadiusCell = 1;         // slot highlight radius
        public static int queueIntervalMs = 120;        // spacing between queued clicks in high ping mode
    }

    // Holder for per-terminal click timing state
    public static class ClickTracker {
        public boolean clicked = false;
        public long lastClickAt = 0L;
        public void reset() { clicked = false; lastClickAt = 0L; }
        public boolean canClick() { return !clicked; }
        public void onClick() { clicked = true; lastClickAt = System.currentTimeMillis(); }
    }

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
                    try { return (String) comp.getClass().getMethod("getUnformattedText").invoke(comp); } catch (Throwable ignored) {}
                }
            }
        } catch (Throwable ignored) {}
        return null;
    }

    // Simple filled-rect helper (ARGB)
    public static void drawRect(int left, int top, int right, int bottom, int color) {
        net.minecraft.client.gui.Gui.drawRect(left, top, right, bottom, color);
    }

    // Rounded filled-rect helper (ARGB). Falls back to drawRect if radius <= 0 or rect is too small.
    public static void drawRoundedRect(int left, int top, int right, int bottom, int radius, int color) {
        int w = right - left; int h = bottom - top;
        if (radius <= 0 || w <= 0 || h <= 0) { drawRect(left, top, right, bottom, color); return; }
        int r = Math.min(radius, Math.min(w, h) / 2);
        drawRect(left + r, top + r, right - r, bottom - r, color);            // center
        drawRect(left, top + r, left + r, bottom - r, color);                  // left band
        drawRect(right - r, top + r, right, bottom - r, color);                // right band
        drawRect(left + r, top, right - r, top + r, color);                    // top band
        drawRect(left + r, bottom - r, right - r, bottom, color);              // bottom band
        int cxL = left + r, cxR = right - r; int cyT = top + r, cyB = bottom - r;
        for (int dy = 0; dy < r; dy++) {
            int dx = (int) Math.floor(Math.sqrt(r * 1.0 - dy * dy));
            int y = cyT - 1 - dy; if (y >= top && y < top + r) { int x0 = Math.max(left, cxL - dx); if (x0 < cxL) drawRect(x0, y, cxL, y + 1, color); int x1 = Math.min(right, cxR + dx); if (cxR < x1) drawRect(cxR, y, x1, y + 1, color); }
            y = cyB + dy; if (y >= bottom - r && y < bottom) { int x0 = Math.max(left, cxL - dx); if (x0 < cxL) drawRect(x0, y, cxL, y + 1, color); int x1 = Math.min(right, cxR + dx); if (cxR < x1) drawRect(cxR, y, x1, y + 1, color); }
        }
    }

    /** Compute rows, width, height, and offsets for a terminal grid. Returns int[]{rows, width, height, offX, offY}. */
    public static int[] computeGrid(int windowSize, float scale, int offsetX, int offsetY) {
        Minecraft mc = Minecraft.getMinecraft(); ScaledResolution sr = new ScaledResolution(mc);
        int rows = Math.max(1, windowSize / 9); int width = 9 * 18; int height = rows * 18;
        float cx = sr.getScaledWidth() / scale / 2f; float cy = sr.getScaledHeight() / scale / 2f;
        int offX = (int) (cx - (width / 2f) + offsetX + 1); int offY = (int) (cy - (height / 2f) + offsetY);
        return new int[]{rows, width, height, offX, offY};
    }

    /** Returns current mouse position in scaled GUI coordinates: {x,y}. */
    public static int[] getScaledMouseXY() {
        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        int x = Mouse.getEventX() * sr.getScaledWidth() / Minecraft.getMinecraft().displayWidth;
        int y = sr.getScaledHeight() - Mouse.getEventY() * sr.getScaledHeight() / Minecraft.getMinecraft().displayHeight - 1;
        return new int[]{x, y};
    }

    /** Slot index under mouse or -1. */
    public static int computeSlotUnderMouse(int windowSize, float scale, int offsetX, int offsetY) {
        if (windowSize <= 0) return -1;
        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        int[] xy = getScaledMouseXY(); int mouseX = xy[0]; int mouseY = xy[1];
        int rows = Math.max(1, windowSize / 9);
        int width = (int) (9 * 18 * scale); int height = (int) (rows * 18 * scale);
        int offX = sr.getScaledWidth() / 2 - width / 2 + (int) (offsetX * scale);
        int offY = sr.getScaledHeight() / 2 - height / 2 + (int) (offsetY * scale);
        int slotX = (int) Math.floor((mouseX - offX) / (18f * scale));
        int slotY = (int) Math.floor((mouseY - offY) / (18f * scale));
        if (slotX < 0 || slotX > 8 || slotY < 0) return -1;
        int slot = slotX + slotY * 9; if (slot < 0 || slot >= windowSize) return -1; return slot;
    }

    /** Send a window click (no pickup). */
    public static boolean windowClickNoPickup(int slot, int button) {
        Minecraft mc = Minecraft.getMinecraft(); if (mc.thePlayer == null || mc.playerController == null) return false;
        try {
            int windowId = mc.thePlayer.openContainer.windowId;
            short actionNumber = mc.thePlayer.openContainer.getNextTransactionID(mc.thePlayer.inventory);
            net.minecraft.network.play.client.C0EPacketClickWindow packet = new net.minecraft.network.play.client.C0EPacketClickWindow(windowId, slot, button, 0, null, actionNumber);
            mc.getNetHandler().addToSendQueue(packet); return true;
        } catch (Throwable ignored) { return false; }
    }

    /** Perform click and mark tracker. */
    public static void doClickAndMark(int slot, int button, ClickTracker tracker) {
        if (!windowClickNoPickup(slot, button)) return;
        if (tracker != null) tracker.onClick();
    }

    /** Returns true if tracker clicked and timeout elapsed. */
    public static boolean hasTimedOut(ClickTracker tracker, int timeoutMs) {
        return tracker != null && tracker.clicked && (System.currentTimeMillis() - tracker.lastClickAt) >= timeoutMs;
    }

    /** Predictive remove. */
    public static void predictRemove(List<Integer> solution, int slot) { if (solution != null) solution.remove((Integer) slot); }

    /** Click slot helper. */
    public static boolean clickSlot(int slot, int button, boolean immediate) { return immediate && windowClickNoPickup(slot, button); }

    /** Process queued clicks; in highPingMode allow timed progression when no inventory update arrives. */
    public static int[] processQueueIfReady(Deque<int[]> queue, List<Integer> solution, ClickTracker tracker) {
        if (queue == null || queue.isEmpty()) return null;
        if (tracker != null && tracker.clicked) {
            if (Defaults.highPingMode) {
                long delta = System.currentTimeMillis() - tracker.lastClickAt;
                if (delta < Defaults.queueIntervalMs) return null; // wait interval
                tracker.clicked = false; // timed unlock
            } else {
                return null; // wait for inventory update
            }
        }
        if (solution == null) { queue.clear(); return null; }
        for (int[] e : queue) {
            if (e == null || e.length < 2 || !solution.contains(e[0])) { queue.clear(); return null; }
        }
        int[] first = queue.pollFirst();
        if (first != null) predictRemove(solution, first[0]);
        return first;
    }
}
