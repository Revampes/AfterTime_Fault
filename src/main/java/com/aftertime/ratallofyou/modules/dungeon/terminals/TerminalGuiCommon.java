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
    private TerminalGuiCommon() {
    }


    // Force-load certain terminal classes so their static initializers can register event listeners
    static {
        try {
            Class.forName("com.aftertime.ratallofyou.modules.dungeon.terminals.numbers");
        } catch (Throwable ignored) {
        }
        try {
            Class.forName("com.aftertime.ratallofyou.modules.dungeon.terminals.startswith");
        } catch (Throwable ignored) {
        }
        try {
            Class.forName("com.aftertime.ratallofyou.modules.dungeon.terminals.Colors");
        } catch (Throwable ignored) {
        }
        try {
            Class.forName("com.aftertime.ratallofyou.modules.dungeon.terminals.redgreen");
        } catch (Throwable ignored) {
        }
        try {
            Class.forName("com.aftertime.ratallofyou.modules.dungeon.terminals.rubix");
        } catch (Throwable ignored) {
        }
        try {
            Class.forName("com.aftertime.ratallofyou.modules.dungeon.terminals.melody");
        } catch (Throwable ignored) {
        }
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
    }

    // Holder for per-terminal click timing state
    public static class ClickTracker {
        public boolean clicked = false;
        public long lastClickAt = 0L;

        public void reset() {
            clicked = false;
            lastClickAt = 0L;
        }
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
                } catch (Throwable ignored) {
                }
                return Math.min(54, Math.max(9, cont.inventorySlots.size() - 36));
            }
        } catch (Throwable ignored) {
        }
        // Reflection fallback
        try {
            Object lower = ReflectionHelper.getPrivateValue(GuiChest.class, chest, "lowerChestInventory", "field_147015_w");
            if (lower != null) {
                return (Integer) lower.getClass().getMethod("getSizeInventory").invoke(lower);
            }
        } catch (Throwable ignored) {
        }
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
                } catch (Throwable ignored) {
                }
                try {
                    Object lower = ReflectionHelper.getPrivateValue(ContainerChest.class, (ContainerChest) cont, "lowerChestInventory", "field_75155_e");
                    if (lower instanceof IInventory) {
                        IChatComponent comp = ((IInventory) lower).getDisplayName();
                        if (comp != null) return comp.getUnformattedText();
                    }
                } catch (Throwable ignored) {
                }
                try {
                    if (cont.inventorySlots != null && !cont.inventorySlots.isEmpty()) {
                        Slot s0 = cont.getSlot(0);
                        if (s0 != null && s0.inventory != null) {
                            IChatComponent comp = s0.inventory.getDisplayName();
                            if (comp != null) return comp.getUnformattedText();
                        }
                    }
                } catch (Throwable ignored) {
                }
            }
        } catch (Throwable ignored) {
        }
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
                    } catch (Throwable ignored) {
                    }
                }
            }
        } catch (Throwable ignored) {
        }
        return null;
    }

    // Simple filled-rect helper (ARGB)
    public static void drawRect(int left, int top, int right, int bottom, int color) {
        net.minecraft.client.gui.Gui.drawRect(left, top, right, bottom, color);
    }

    /**
     * Compute rows, width, height, and offsets for a terminal grid.
     * Returns int[]{rows, width, height, offX, offY}.
     */
    public static int[] computeGrid(int windowSize, float scale, int offsetX, int offsetY) {
        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution sr = new ScaledResolution(mc);
        int rows = Math.max(1, windowSize / 9);
        int width = 9 * 18;
        int height = rows * 18;
        float cx = sr.getScaledWidth() / scale / 2f;
        float cy = sr.getScaledHeight() / scale / 2f;
        int offX = (int) (cx - (width / 2f) + offsetX + 1);
        int offY = (int) (cy - (height / 2f) + offsetY);
        return new int[]{rows, width, height, offX, offY};
    }

    // ===================== Shared input helpers =====================

    /**
     * Returns the current mouse position in scaled GUI coordinates: {x, y}.
     */
    public static int[] getScaledMouseXY() {
        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        int x = Mouse.getEventX() * sr.getScaledWidth() / Minecraft.getMinecraft().displayWidth;
        int y = sr.getScaledHeight() - Mouse.getEventY() * sr.getScaledHeight() / Minecraft.getMinecraft().displayHeight - 1;
        return new int[]{x, y};
    }

    /**
     * Given the terminal grid parameters, computes the slot index under the mouse, or -1 if none/invalid.
     */
    public static int computeSlotUnderMouse(int windowSize, float scale, int offsetX, int offsetY) {
        if (windowSize <= 0) return -1;
        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        int[] xy = getScaledMouseXY();
        int mouseX = xy[0];
        int mouseY = xy[1];

        int rows = Math.max(1, windowSize / 9);
        int width = (int) (9 * 18 * scale);
        int height = (int) (rows * 18 * scale);
        int offX = sr.getScaledWidth() / 2 - width / 2 + (int) (offsetX * scale);
        int offY = sr.getScaledHeight() / 2 - height / 2 + (int) (offsetY * scale);

        int slotX = (int) Math.floor((mouseX - offX) / (18f * scale));
        int slotY = (int) Math.floor((mouseY - offY) / (18f * scale));
        if (slotX < 0 || slotX > 8 || slotY < 0) return -1;
        int slot = slotX + slotY * 9;
        if (slot < 0 || slot >= windowSize) return -1;
        return slot;
    }

    /**
     * Sends a window click to the server for the player's currently open container, without picking up or moving any item.
     * Returns true if the click was attempted, false if player/controller was null.
     */
    public static boolean windowClickNoPickup(int slot, int button) { // Notice that this is fake middle click, not a real one
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || mc.playerController == null) return false;
        try {
            int windowId = mc.thePlayer.openContainer.windowId;
            short actionNumber = mc.thePlayer.openContainer.getNextTransactionID(mc.thePlayer.inventory);
            // clickType 0 = PICKUP, see Container.java for click types
            net.minecraft.network.play.client.C0EPacketClickWindow packet =
                    new net.minecraft.network.play.client.C0EPacketClickWindow(windowId, slot, button, 0, null, actionNumber);
            mc.getNetHandler().addToSendQueue(packet);
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    /**
     * Helper to perform a container click and update a per-terminal click tracker.
     */
    public static void doClickAndMark(int slot, int button, ClickTracker tracker) {
        if (!windowClickNoPickup(slot, button)) return;
        if (tracker != null) {
            tracker.clicked = true;
            tracker.lastClickAt = System.currentTimeMillis();
        }
    }

    /**
     * Returns true if the given tracker is in a clicked state and the timeout elapsed.
     */
    public static boolean hasTimedOut(ClickTracker tracker, int timeoutMs) {
        return tracker != null && tracker.clicked && (System.currentTimeMillis() - tracker.lastClickAt) >= timeoutMs;
    }

    // ===================== Shared state helpers =====================

    /**
     * Remove a slot from a solution list by value (predictive UI update).
     */
    public static void predictRemove(List<Integer> solution, int slot) {
        if (solution == null) return;
        solution.remove((Integer) slot);
    }

    /**
     * Validate a queued series of clicks against the current solution. If all queued clicks are still valid,
     * applies predictRemove for each queued entry (in order) and returns the first queued click to send now.
     * If any queued click is invalid, clears the queue and returns null.
     */
    public static int[] processQueueIfReady(Deque<int[]> queue, List<Integer> solution) {
        if (queue == null || queue.isEmpty()) return null;
        if (solution == null) {
            queue.clear();
            return null;
        }
        for (int[] q : queue) {
            if (q == null || q.length < 2 || !solution.contains(q[0])) {
                queue.clear();
                return null;
            }
        }
        for (int[] q : queue) predictRemove(solution, q[0]);
        return queue.pollFirst();
    }
}
