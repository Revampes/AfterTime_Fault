package com.aftertime.ratallofyou.modules.dungeon.terminals;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Mouse;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Custom GUI and click helper for the "Red Green" terminal: "Correct all the panes!".
 * Default-enabled, no settings GUI wiring; uses sensible defaults.
 */
public class redgreen {
    // Enable by default and auto-register
    private static boolean enabled = true;
    private static boolean registered = false;
    private static final redgreen INSTANCE = new redgreen();

    static {
        try { MinecraftForge.EVENT_BUS.register(INSTANCE); registered = true; } catch (Throwable ignored) {}
    }

    // Use shared click tracker and defaults
    private static final TerminalGuiCommon.ClickTracker CLICK = new TerminalGuiCommon.ClickTracker();

    // State
    private static boolean inTerminal = false;
    private static long openedAt = 0L;
    private static int windowSize = 0;
    private static int lastRemaining = -1;

    private static final List<Integer> solution = new ArrayList<>();
    private static final Deque<int[]> queue = new ArrayDeque<>(); // entries: {slot, button}

    private static final Pattern TITLE_PATTERN = Pattern.compile("^Correct all the panes!$");

    // Allowed slots for this terminal (center 5x3 grid)
    private static final int[] ALLOWED_SLOTS = new int[]{
            11, 12, 13, 14, 15,
            20, 21, 22, 23, 24,
            29, 30, 31, 32, 33
    };

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
        solution.clear();
        queue.clear();
        lastRemaining = -1;
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
                return;
            }
        }
        resetState();
    }

    @SubscribeEvent
    public void onDrawScreenPre(GuiScreenEvent.DrawScreenEvent.Pre event) {
        if (!enabled) return;
        if (!(event.gui instanceof GuiChest)) return;

        // If GuiOpen was missed, detect here using TerminalGuiCommon.getChestTitle
        if (!inTerminal) {
            GuiChest chest = (GuiChest) event.gui;
            String title = TerminalGuiCommon.getChestTitle(chest);
            if (title != null && TITLE_PATTERN.matcher(title).matches()) {
                inTerminal = true;
                CLICK.reset();
                if (openedAt == 0L) openedAt = System.currentTimeMillis();
                queue.clear();
                windowSize = TerminalGuiCommon.getChestWindowSize(chest);
            } else {
                return; // not our GUI
            }
        }

        event.setCanceled(true);
        solveFromInventory();
        processQueueIfReady();
        drawOverlay();
    }

    @SubscribeEvent
    public void onMouseInputPre(GuiScreenEvent.MouseInputEvent.Pre event) {
        if (!enabled) return;
        if (!(Minecraft.getMinecraft().currentScreen instanceof GuiChest)) return;

        // Ensure detection even if GuiOpen was missed so the first click works
        if (!inTerminal) {
            GuiChest chest = (GuiChest) Minecraft.getMinecraft().currentScreen;
            String title = TerminalGuiCommon.getChestTitle(chest);
            if (title != null && TITLE_PATTERN.matcher(title).matches()) {
                inTerminal = true;
                CLICK.reset();
                if (openedAt == 0L) openedAt = System.currentTimeMillis();
                queue.clear();
                windowSize = TerminalGuiCommon.getChestWindowSize(chest);
                // Precompute solution so the immediate click is handled
                solveFromInventory();
            } else {
                return; // not our GUI; don't cancel
            }
        }

        // Block other handlers while our GUI is active
        event.setCanceled(true);

        // Only on press
        if (!Mouse.getEventButtonState()) return;
        int button = Mouse.getEventButton();
        if (button != 0) return; // left click only

        long now = System.currentTimeMillis();
        if (openedAt + TerminalGuiCommon.Defaults.firstClickBlockMs > now) return;

        // Use common helper to compute slot under mouse
        int slot = TerminalGuiCommon.computeSlotUnderMouse(windowSize, TerminalGuiCommon.Defaults.scale, TerminalGuiCommon.Defaults.offsetX, TerminalGuiCommon.Defaults.offsetY);
        if (slot < 0) return;

        boolean isSolution = solution.contains(slot);
        if (isSolution) {
            if (TerminalGuiCommon.Defaults.highPingMode || TerminalGuiCommon.Defaults.phoenixClientCompat || !CLICK.clicked) TerminalGuiCommon.predictRemove(solution, slot);
            if (TerminalGuiCommon.Defaults.highPingMode && CLICK.clicked) {
                queue.addLast(new int[]{slot, 0});
            } else {
                TerminalGuiCommon.doClickAndMark(slot, 0, CLICK);
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

    private static void drawOverlay() {
        Minecraft mc = Minecraft.getMinecraft();
        FontRenderer fr = mc.fontRendererObj;
        int[] grid = TerminalGuiCommon.computeGrid(windowSize, TerminalGuiCommon.Defaults.scale, TerminalGuiCommon.Defaults.offsetX, TerminalGuiCommon.Defaults.offsetY);
        int width = grid[1], height = grid[2], offX = grid[3], offY = grid[4];

        String title = "§8[§bRAT Terminal§8] §aRed Green";

        GlStateManager.pushMatrix();
        GlStateManager.scale(TerminalGuiCommon.Defaults.scale, TerminalGuiCommon.Defaults.scale, 1f);
        TerminalGuiCommon.drawRoundedRect(offX - 2, offY - 2, offX + width + 2, offY + height + 2, TerminalGuiCommon.Defaults.cornerRadiusBg, TerminalGuiCommon.Defaults.backgroundColor);
        fr.drawStringWithShadow(title, offX, offY, 0xFFFFFFFF);

        // Iterate over solution slots directly
        for (int slot : solution) {
            int curX = (slot % 9) * 18 + offX;
            int curY = (slot / 9) * 18 + offY;
            TerminalGuiCommon.drawRoundedRect(curX, curY, curX + 16, curY + 16, TerminalGuiCommon.Defaults.cornerRadiusCell, TerminalGuiCommon.Defaults.overlayColor);
        }

        GlStateManager.popMatrix();
    }

    private static void solveFromInventory() {
        solution.clear();
        Container container = Minecraft.getMinecraft().thePlayer != null ? Minecraft.getMinecraft().thePlayer.openContainer : null;
        if (!(container instanceof ContainerChest)) return;

        int rows = Math.max(1, windowSize / 9);
        Arrays.stream(ALLOWED_SLOTS)
                .filter(s -> s < rows * 9)
                .mapToObj(s -> {
                    Slot slot = container.getSlot(s);
                    ItemStack stack = slot == null ? null : slot.getStack();
                    if (stack == null || stack.hasEffect()) return null;
                    int id = Item.getIdFromItem(stack.getItem());
                    if (id != 160) return null; // stained glass pane
                    int meta = stack.getItemDamage();
                    return meta == 14 ? s : null; // red only
                })
                .filter(Objects::nonNull)
                .forEach(solution::add);
        // If remaining red count changed, allow next click immediately
        int remaining = solution.size();
        if (remaining != lastRemaining) {
            CLICK.clicked = false;
            lastRemaining = remaining;
        }
        // Process the queue immediately after unlocking
        processQueueIfReady();
    }

    private static void processQueueIfReady() {
        int[] first = TerminalGuiCommon.processQueueIfReady(queue, solution, CLICK);
        if (first != null) TerminalGuiCommon.doClickAndMark(first[0], first[1], CLICK);
    }
}