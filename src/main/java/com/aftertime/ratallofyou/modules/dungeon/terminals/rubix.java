package com.aftertime.ratallofyou.modules.dungeon.terminals;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Mouse;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Custom GUI and click helper for the "Rubix" terminal: "Change all to same color!".
 * Supports left and right click based on required rotations.
 */
public class rubix {
    // Enable by default and auto-register
    private static boolean enabled = true;
    private static boolean registered = false;
    private static final rubix INSTANCE = new rubix();

    static {
        try { MinecraftForge.EVENT_BUS.register(INSTANCE); registered = true; } catch (Throwable ignored) {}
    }

    // Shared click tracker and defaults
    private static final TerminalGuiCommon.ClickTracker CLICK = new TerminalGuiCommon.ClickTracker();

    // State
    private static boolean inTerminal = false;
    private static long openedAt = 0L;
    private static int windowSize = 0;

    // Solution: per slot signed count. >0 => left-click N times, <0 => right-click N times, 0 => no action
    private static int[] solutionBySlot = new int[0];
    private static final Deque<int[]> queue = new ArrayDeque<>(); // entries: {slot, button}

    private static final Pattern TITLE_PATTERN = Pattern.compile("^Change all to same color!$");

    // Allowed slots are the 3x3 center grid
    private static final int[] ALLOWED_SLOTS = new int[]{
            12, 13, 14,
            21, 22, 23,
            30, 31, 32
    };

    // Color cycle order metas (matching JS): [14, 1, 4, 13, 11]
    private static final int[] ORDER = new int[]{14, 1, 4, 13, 11};

    // Overlay colors for direction
    private static final int OVERLAY_LEFT = 0xFF00AA00;  // green-ish for left clicks
    private static final int OVERLAY_RIGHT = 0xFFAA0000; // red-ish for right clicks

    // Track inventory changes to unblock queued clicks immediately after server updates
    private static int invHash = 0;
    private static int hashAtClick = 0;

    // API (kept for parity)
    public static void setEnabled(boolean on) {
        if (enabled == on) return;
        enabled = on;
        if (on) {
            if (!registered) { try { MinecraftForge.EVENT_BUS.register(INSTANCE); } catch (Throwable ignored) {} registered = true; }
        } else {
            if (registered) { try { MinecraftForge.EVENT_BUS.unregister(INSTANCE); } catch (Throwable ignored) {} registered = false; }
            resetState();
        }
    }

    private static void resetState() {
        inTerminal = false;
        CLICK.reset();
        openedAt = 0L;
        windowSize = 0;
        solutionBySlot = new int[0];
        queue.clear();
    }

    // ===================== Events =====================

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        if (!enabled) return;
        if (event.gui instanceof GuiChest) {
            GuiChest chest = (GuiChest) event.gui;
            String title = TerminalGuiCommon.getChestTitle(chest);
            if (title != null && TITLE_PATTERN.matcher(title).matches()) {
                inTerminal = true;
                CLICK.reset();
                openedAt = System.currentTimeMillis();
                queue.clear();
                windowSize = TerminalGuiCommon.getChestWindowSize(chest);
                ensureSolutionSize();
                return;
            }
        }
        resetState();
    }

    @SubscribeEvent
    public void onDrawScreenPre(GuiScreenEvent.DrawScreenEvent.Pre event) {
        if (!enabled) return;
        if (!(event.gui instanceof GuiChest)) return;
        if (!inTerminal) return;
        event.setCanceled(true);
        solveFromInventory();
        processQueueIfReady();
        drawOverlay();
        // Removed manual while-loop flushing of the queue; processing is paced by processQueueIfReady
    }

    private static void queueClick(int slot, int button) {
        if (slot >= 0 && slot < windowSize) {
            queue.offer(new int[]{slot, button});
        }
    }

    @SubscribeEvent
    public void onMouseInputPre(GuiScreenEvent.MouseInputEvent.Pre event) {
        if (!enabled) return;
        if (!(Minecraft.getMinecraft().currentScreen instanceof GuiChest)) return;
        if (!inTerminal) return;
        event.setCanceled(true);
        if (!Mouse.getEventButtonState()) return;
        int button = Mouse.getEventButton();
        if (button != 0 && button != 1) return; // left or right click only
        long now = System.currentTimeMillis();
        if (openedAt + TerminalGuiCommon.Defaults.firstClickBlockMs > now) return;
        int slot = TerminalGuiCommon.computeSlotUnderMouse(windowSize, TerminalGuiCommon.Defaults.scale, TerminalGuiCommon.Defaults.offsetX, TerminalGuiCommon.Defaults.offsetY);
        if (slot < 0) return;

        if (slot < solutionBySlot.length && solutionBySlot[slot] != 0) {
            int expectedButton = solutionBySlot[slot] > 0 ? 0 : 1; // positive -> left click, negative -> right click
            if (button == expectedButton) {
                if (TerminalGuiCommon.Defaults.highPingMode && CLICK.clicked) {
                    queue.addLast(new int[]{slot, button});
                } else {
                    TerminalGuiCommon.doClickAndMark(slot, button, CLICK);
                }
            }
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (!enabled || !inTerminal) return;
        if (event.phase != TickEvent.Phase.END) return;
        if (TerminalGuiCommon.hasTimedOut(CLICK, TerminalGuiCommon.Defaults.timeoutMs)) {
            queue.clear();
            CLICK.clicked = false;
            solveFromInventory();
        }
    }

    // ===================== Logic =====================

    private static void ensureSolutionSize() {
        int size = Math.max(0, windowSize);
        if (solutionBySlot.length != size) solutionBySlot = new int[size];
        else Arrays.fill(solutionBySlot, 0);
    }

    private static void drawOverlay() {
        Minecraft mc = Minecraft.getMinecraft();
        FontRenderer fr = mc.fontRendererObj;
        int[] grid = TerminalGuiCommon.computeGrid(windowSize, TerminalGuiCommon.Defaults.scale, TerminalGuiCommon.Defaults.offsetX, TerminalGuiCommon.Defaults.offsetY);
        int width = grid[1], height = grid[2], offX = grid[3], offY = grid[4];

        String title = "§8[§bRAT Terminal§8] §aRubix";

        GlStateManager.pushMatrix();
        GlStateManager.scale(TerminalGuiCommon.Defaults.scale, TerminalGuiCommon.Defaults.scale, 1f);
        TerminalGuiCommon.drawRoundedRect(offX - 2, offY - 2, offX + width + 2, offY + height + 2, TerminalGuiCommon.Defaults.cornerRadiusBg, TerminalGuiCommon.Defaults.backgroundColor);
        fr.drawStringWithShadow(title, offX, offY, 0xFFFFFFFF);
        // Draw overlays only for allowed 3x3 slots
        for (int i : ALLOWED_SLOTS) {
            if (i >= windowSize) continue;
            int need = getSolution(i);
            if (need == 0) continue;
            int curX = (i % 9) * 18 + offX;
            int curY = (i / 9) * 18 + offY;
            int color = need > 0 ? OVERLAY_LEFT : OVERLAY_RIGHT;
            TerminalGuiCommon.drawRoundedRect(curX, curY, curX + 16, curY + 16, TerminalGuiCommon.Defaults.cornerRadiusCell, color);
            String text = String.valueOf(need);
            fr.drawStringWithShadow(text, curX + (int) ((16 - fr.getStringWidth(text)) / 2f), curY + 4, 0xFFFFFFFF);
        }
        GlStateManager.popMatrix();
    }

    private static int lastSolutionHash = 0;

    private static void solveFromInventory() {
        ensureSolutionSize();
        Arrays.fill(solutionBySlot, 0);
        Container container = Minecraft.getMinecraft().thePlayer != null ? Minecraft.getMinecraft().thePlayer.openContainer : null;
        if (!(container instanceof ContainerChest)) { invHash = 0; return; }
        int rows = Math.max(1, windowSize / 9);

        // Collect metas for allowed slots present in this chest size
        List<int[]> metas = new ArrayList<>(); // entries: {slot, meta}
        for (int s : ALLOWED_SLOTS) {
            if (s >= rows * 9) continue;
            Slot slot = container.getSlot(s);
            ItemStack stack = slot == null ? null : slot.getStack();
            if (stack == null) continue;
            metas.add(new int[]{s, stack.getItemDamage()});
        }
        // Compute simple inventory hash from metas list
        int h = 1;
        for (int[] sm : metas) {
            h = 31 * h + sm[0];
            h = 31 * h + sm[1];
        }
        invHash = h;
        if (metas.isEmpty()) return;

        // Compute click counts for each possible origin index
        int[] clicks = new int[ORDER.length];
        for (int i = 0; i < ORDER.length; i++) clicks[i] = 0;
        for (int i = 0; i < ORDER.length; ++i) {
            for (int[] sm : metas) {
                int meta = sm[1];
                if (meta == ORDER[calcIndex(i)]) continue; // already target
                if (meta == ORDER[calcIndex(i - 2)]) clicks[i] += 2;
                else if (meta == ORDER[calcIndex(i - 1)]) clicks[i] += 1;
                else if (meta == ORDER[calcIndex(i + 1)]) clicks[i] += 1;
                else if (meta == ORDER[calcIndex(i + 2)]) clicks[i] += 2;
            }
        }
        int origin = 0;
        int min = Integer.MAX_VALUE;
        for (int i = 0; i < clicks.length; ++i) {
            if (clicks[i] < min) { min = clicks[i]; origin = i; }
        }

        // Build per-slot solution
        for (int[] sm : metas) {
            int slot = sm[0];
            int meta = sm[1];
            if (meta == ORDER[calcIndex(origin - 2)]) solutionBySlot[slot] = 2;
            else if (meta == ORDER[calcIndex(origin - 1)]) solutionBySlot[slot] = 1;
            else if (meta == ORDER[calcIndex(origin + 1)]) solutionBySlot[slot] = -1;
            else if (meta == ORDER[calcIndex(origin + 2)]) solutionBySlot[slot] = -2;
            else solutionBySlot[slot] = 0; // already correct
        }
        // Unlock clicker as soon as solution changes (inventory update)
        int solutionHash = Arrays.hashCode(solutionBySlot);
        if (solutionHash != lastSolutionHash) {
            CLICK.clicked = false;
            lastSolutionHash = solutionHash;
        }
        // Process the queue immediately after unlocking
        processQueueIfReady();
    }

    private static int calcIndex(int idx) {
        int n = ORDER.length;
        int v = idx % n;
        if (v < 0) v += n;
        return v;
    }

    private static int getSolution(int slot) {
        if (slot < 0 || slot >= solutionBySlot.length) return 0;
        return solutionBySlot[slot];
    }

    private static void setSolution(int slot, int val) {
        if (slot < 0 || slot >= solutionBySlot.length) return;
        solutionBySlot[slot] = val;
    }

    private static void predict(int slot, int button) {
        int need = getSolution(slot);
        if (need == 0) return;
        if (button == 0) { // left click
            if (need > 0) need -= 1; // consume one left click
            else return;            // invalid, ignore
        } else { // right click
            if (need < 0) need += 1; // consume one right click
            else return;             // invalid, ignore
        }
        setSolution(slot, need);
    }

    private static void processQueueIfReady() {
        if (queue.isEmpty()) return;
        if (CLICK.clicked) {
            if (TerminalGuiCommon.Defaults.highPingMode) {
                long delta = System.currentTimeMillis() - CLICK.lastClickAt;
                if (delta < TerminalGuiCommon.Defaults.queueIntervalMs) return; // space out queued clicks
                CLICK.clicked = false; // timed unlock
            } else {
                return; // wait for inventory update
            }
        }
        // Validate queued clicks against current solution; require all queued to still be valid
        for (int[] q : queue) {
            if (q == null || q.length < 2) { queue.clear(); return; }
            int slot = q[0];
            int button = q[1];
            int need = getSolution(slot);
            if (need == 0) { queue.clear(); return; }
            if (!((need > 0 && button == 0) || (need < 0 && button == 1))) { queue.clear(); return; }
        }
        // Apply predictions for queued clicks to update overlay immediately
        for (int[] q : queue) predict(q[0], q[1]);
        // Send the first queued click now
        int[] first = queue.pollFirst();
        if (first != null) TerminalGuiCommon.doClickAndMark(first[0], first[1], CLICK);
    }
}
