package com.aftertime.ratallofyou.modules.dungeon.terminals;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Custom GUI and click helper for the "Starts With" terminal in SkyBlock dungeons.
 * Ported from the provided ChatTriggers JS logic into a Forge 1.8.9 Java module.
 */
public class startswith {
    // Runtime enable toggle
    private static boolean enabled = true;
    private static boolean registered = false;
    // Ensure we register an instance on the Forge event bus (not the Class object)
    private static final startswith INSTANCE = new startswith();

    static {
        try { MinecraftForge.EVENT_BUS.register(INSTANCE); registered = true; } catch (Throwable ignored) {}
    }

    // Use shared click tracker and defaults
    private static final TerminalGuiCommon.ClickTracker CLICK = new TerminalGuiCommon.ClickTracker();

    // State
    private static boolean inTerminal = false;
    private static long openedAt = 0L;

    private static int windowSize = 0; // number of slots in the chest window (rows*9)
    private static String startsWithLetter = null; // single letter to match

    private static final List<Integer> solution = new ArrayList<>();
    // Removed local allowedSlots; use TerminalGuiCommon.ALLOWED_SLOTS instead

    private static final Deque<int[]> queue = new ArrayDeque<>(); // entries: {slot, button}

    private static final Pattern TITLE_PATTERN = Pattern.compile("^What starts with: '([A-Za-z])'\\?$");

    public static void setEnabled(boolean on) {
        if (enabled == on) return;
        enabled = on;
        if (on) {
            if (!registered) {
                MinecraftForge.EVENT_BUS.register(INSTANCE);
                registered = true;
            }
        } else {
            if (registered) {
                try { MinecraftForge.EVENT_BUS.unregister(INSTANCE); } catch (Throwable ignored) {}
                registered = false;
            }
            resetState();
        }
    }

    private static void resetState() {
        inTerminal = false;
        CLICK.reset();
        openedAt = 0L;
        windowSize = 0;
        startsWithLetter = null;
        solution.clear();
        queue.clear();
    }

    // ==========================================
    // Event hooks
    // ==========================================

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        if (!enabled) return;
        // Determine if we are entering/leaving a Starts With terminal by title
        if (event.gui instanceof GuiChest) {
            GuiChest chest = (GuiChest) event.gui;
            String title = TerminalGuiCommon.getChestTitle(chest);
            if (title != null) {
                Matcher m = TITLE_PATTERN.matcher(title);
                if (m.matches()) {
                    startsWithLetter = m.group(1).toLowerCase();
                    inTerminal = true;
                    CLICK.reset();
                    openedAt = System.currentTimeMillis();
                    queue.clear();
                    // compute window size via common helper
                    windowSize = TerminalGuiCommon.getChestWindowSize(chest);
                    return;
                }
            }
        }
        // If any other GUI opens, exit terminal mode
        resetState();
    }

    @SubscribeEvent
    public void onDrawScreenPre(GuiScreenEvent.DrawScreenEvent.Pre event) {
        if (!enabled) return;
        if (!(event.gui instanceof GuiChest)) return;
        if (!inTerminal || startsWithLetter == null) return;

        // Prevent vanilla GUI from drawing
        event.setCanceled(true);

        // Re-solve every frame from current inventory
        solveFromInventory();
        // If we queued clicks (high ping mode) and they are still valid, send the next one
        processQueueIfReady();

        // Draw our custom overlay
        drawOverlay();
    }

    @SubscribeEvent
    public void onMouseInputPre(GuiScreenEvent.MouseInputEvent.Pre event) {
        if (!enabled) return;
        if (!(Minecraft.getMinecraft().currentScreen instanceof GuiChest)) return;
        if (!inTerminal || startsWithLetter == null) return;

        // Block mouse input while our custom GUI is active to avoid other handlers (vanilla or mods) processing releases
        event.setCanceled(true);

        // Only act on actual mouse button press (not wheel)
        if (!Mouse.getEventButtonState()) return;
        int button = Mouse.getEventButton();
        if (button != 0) return; // left click only

        long now = System.currentTimeMillis();
        if (openedAt + TerminalGuiCommon.Defaults.firstClickBlockMs > now) {
            return;
        }

        // Compute slot under mouse within our virtual grid using common helper
        int slot = TerminalGuiCommon.computeSlotUnderMouse(windowSize, TerminalGuiCommon.Defaults.scale, TerminalGuiCommon.Defaults.offsetX, TerminalGuiCommon.Defaults.offsetY);
        if (slot < 0) return;

        boolean isSolution = solution.contains(slot);
        // If it's part of the solution, handle click ourselves
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
            CLICK.reset();
            solveFromInventory();
        }
    }

    // ==========================================
    // Core logic
    // ==========================================

    private static void drawOverlay() {
        Minecraft mc = Minecraft.getMinecraft();
        FontRenderer fr = mc.fontRendererObj;
        // Use common grid computation
        int[] grid = TerminalGuiCommon.computeGrid(windowSize, TerminalGuiCommon.Defaults.scale, TerminalGuiCommon.Defaults.offsetX, TerminalGuiCommon.Defaults.offsetY);
        int width = grid[1], height = grid[2], offX = grid[3], offY = grid[4];

        String title = "§8[§bRAT Terminal§8] §aStarts With";

        GlStateManager.pushMatrix();
        GlStateManager.scale(TerminalGuiCommon.Defaults.scale, TerminalGuiCommon.Defaults.scale, 1f);
        // background
        TerminalGuiCommon.drawRect(offX - 2, offY - 2, offX + width + 2, offY + height + 2, TerminalGuiCommon.Defaults.backgroundColor);
        // title
        fr.drawStringWithShadow(title, offX, offY, 0xFFFFFFFF);

        // draw solution highlights directly from solution list
        for (int slot : solution) {
            int curX = (slot % 9) * 18 + offX;
            int curY = (slot / 9) * 18 + offY;
            TerminalGuiCommon.drawRect(curX, curY, curX + 16, curY + 16, TerminalGuiCommon.Defaults.overlayColor);
        }
        GlStateManager.popMatrix();
    }

    private static void solveFromInventory() {
        solution.clear();
        if (startsWithLetter == null) return;
        Container container = Minecraft.getMinecraft().thePlayer != null ? Minecraft.getMinecraft().thePlayer.openContainer : null;
        if (!(container instanceof ContainerChest)) return;
        int rows = Math.max(1, windowSize / 9);
        Arrays.stream(TerminalGuiCommon.ALLOWED_SLOTS)
                .filter(s -> s < rows * 9)
                .mapToObj(s -> {
                    Slot slot = container.getSlot(s);
                    ItemStack stack = slot == null ? null : slot.getStack();
                    if (stack == null || stack.hasEffect()) return null;
                    String name = EnumChatFormatting.getTextWithoutFormattingCodes(stack.getDisplayName());
                    if (name == null) return null;
                    return name.toLowerCase().startsWith(startsWithLetter) ? s : null;
                })
                .filter(Objects::nonNull)
                .forEach(solution::add);
    }

    private static void processQueueIfReady() {
        int[] first = TerminalGuiCommon.processQueueIfReady(queue, solution);
        if (first != null) TerminalGuiCommon.doClickAndMark(first[0], first[1], CLICK);
    }
}
