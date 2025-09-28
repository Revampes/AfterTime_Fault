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
 * Custom GUI and click helper for the "Numbers" terminal: "Click in order!".
 * Default-enabled, no settings GUI wiring; uses sensible defaults.
 */
public class numbers {
    // Enable by default and auto-register
    private static boolean enabled = true;
    private static boolean registered = false;
    private static final numbers INSTANCE = new numbers();

    static {
        try { MinecraftForge.EVENT_BUS.register(INSTANCE); registered = true; } catch (Throwable ignored) {}
    }

    // Use shared click tracker and defaults
    private static final TerminalGuiCommon.ClickTracker CLICK = new TerminalGuiCommon.ClickTracker();

    // State
    private static boolean inTerminal = false;
    private static long openedAt = 0L;
    private static int windowSize = 0;
    private static Integer lastHeadSlot = null;

    // Solution order: only red panes (meta 14), sorted by stack size asc
    private static final List<Integer> solution = new ArrayList<>();
    // Queue of clicks for high ping mode: {slot, button}
    private static final Deque<int[]> queue = new ArrayDeque<>();

    private static final Pattern TITLE_PATTERN = Pattern.compile("^Click in order!$");

    // Only first two rows (15 slots) are used in Numbers terminal
    private static final int[] ALLOWED_SLOTS_NUMBERS = new int[]{
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25
    };

    // Public toggle (kept for parity, but default is enabled)
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
        lastHeadSlot = null;
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
        // Any other GUI => exit
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
        // Removed redundant manual queue flush; processQueueIfReady sends at most one when ready
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
        if (button != 0) return; // left click only
        long now = System.currentTimeMillis();
        if (openedAt + TerminalGuiCommon.Defaults.firstClickBlockMs > now) return;
        int slot = TerminalGuiCommon.computeSlotUnderMouse(windowSize, TerminalGuiCommon.Defaults.scale, TerminalGuiCommon.Defaults.offsetX, TerminalGuiCommon.Defaults.offsetY);
        if (slot < 0) return;
        if (solution.contains(slot)) {
            if (TerminalGuiCommon.Defaults.highPingMode || TerminalGuiCommon.Defaults.phoenixClientCompat || !CLICK.clicked) {
                TerminalGuiCommon.predictRemove(solution, slot);
            }
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
            // Unconditionally unlock after timeout (match JS behavior)
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

        String title = "§8[§bRAT Terminal§8] §aNumbers";

        GlStateManager.pushMatrix();
        GlStateManager.scale(TerminalGuiCommon.Defaults.scale, TerminalGuiCommon.Defaults.scale, 1f);
        // Rounded background panel
        TerminalGuiCommon.drawRoundedRect(offX - 2, offY - 2, offX + width + 2, offY + height + 2, TerminalGuiCommon.Defaults.cornerRadiusBg, TerminalGuiCommon.Defaults.backgroundColor);
        fr.drawStringWithShadow(title, offX, offY, 0xFFFFFFFF);

        // Highlight next up to 3 steps directly (show color only, no numeric labels)
        int[] stepColors = new int[]{ 0xFF00FF00, 0xFFFFFF00, 0xFFFF0000 };
        int max = Math.min(3, solution.size());
        for (int i = 0; i < max; i++) {
            int slot = solution.get(i);
            int curX = (slot % 9) * 18 + offX;
            int curY = (slot / 9) * 18 + offY;
            // Rounded highlight for cell
            TerminalGuiCommon.drawRoundedRect(curX, curY, curX + 16, curY + 16, TerminalGuiCommon.Defaults.cornerRadiusCell, stepColors[i]);
        }

        GlStateManager.popMatrix();
    }

    private static void solveFromInventory() {
        solution.clear();
        Container container = Minecraft.getMinecraft().thePlayer != null ? Minecraft.getMinecraft().thePlayer.openContainer : null;
        if (!(container instanceof ContainerChest)) return;
        int rows = Math.max(1, windowSize / 9);

        Arrays.stream(ALLOWED_SLOTS_NUMBERS)
                .filter(s -> s < rows * 9)
                .mapToObj(s -> {
                    Slot slotObj = container.getSlot(s);
                    ItemStack stack = slotObj == null ? null : slotObj.getStack();
                    if (stack == null) return null;
                    int id = net.minecraft.item.Item.getIdFromItem(stack.getItem());
                    if (id != 160) return null; // stained glass (pane) only
                    int meta = stack.getItemDamage();
                    if (meta != 14) return null; // red only for clicks
                    return new int[]{s, stack.stackSize};
                })
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingInt(a -> a[1]))
                .map(a -> a[0])
                .forEachOrdered(solution::add);

        Integer head = solution.isEmpty() ? null : solution.get(0);
        // unlock when server advanced the head (additional unlock also occurs on timeout)
        if (!Objects.equals(head, lastHeadSlot)) {
            CLICK.clicked = false;
            lastHeadSlot = head;
        }
        // Process the queue immediately after unlocking
        processQueueIfReady();
    }


    private static void predict(int slot) {
        if (!solution.isEmpty() && solution.get(0) == slot) {
            solution.remove(0);
        }
    }

    private static void processQueueIfReady() {
        if (queue.isEmpty()) return;
        // Respect highPingMode: timed unlock if server confirmation hasn't arrived
        if (CLICK.clicked) {
            if (TerminalGuiCommon.Defaults.highPingMode) {
                long delta = System.currentTimeMillis() - CLICK.lastClickAt;
                if (delta < TerminalGuiCommon.Defaults.queueIntervalMs) return; // wait a bit more
                CLICK.clicked = false; // timed unlock
            } else {
                return; // wait for inventory update when not in high ping mode
            }
        }
        // Validate queued clicks against current solution order (strict)
        int idx = 0;
        for (int[] q : queue) {
            if (q == null || q.length < 2) { queue.clear(); return; }
            int slot = q[0];
            if (solution.indexOf(slot) != idx) { queue.clear(); return; }
            idx++;
        }
        // Apply predictions for queued clicks to update overlay immediately
        for (int[] q : queue) predict(q[0]);
        // Send the first queued click now
        int[] next = queue.pollFirst();
        if (next != null) TerminalGuiCommon.doClickAndMark(next[0], next[1], CLICK);
    }
}