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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private static final Deque<int[]> queue = new ArrayDeque<>(); // entries: {slot, button}

    // Track inventory changes to promptly unlock after successful click
    private static int invHash = 0;
    private static int hashAtClick = 0;
    private static int lastSolutionHash = 0;

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
        invHash = 0; hashAtClick = 0;
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
        if (!inTerminal) return;
        event.setCanceled(true);
        solveFromInventory();
        processQueueIfReady();
        drawOverlay();
        // Removed manual queue flush; sending is controlled by processQueueIfReady
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
        int[] grid = TerminalGuiCommon.computeGrid(windowSize, TerminalGuiCommon.Defaults.scale, TerminalGuiCommon.Defaults.offsetX, TerminalGuiCommon.Defaults.offsetY);
        int width = grid[1], height = grid[2], offX = grid[3], offY = grid[4];

        String title = "§8[§bRAT Terminal§8] §aStarts With";

        GlStateManager.pushMatrix();
        GlStateManager.scale(TerminalGuiCommon.Defaults.scale, TerminalGuiCommon.Defaults.scale, 1f);
        TerminalGuiCommon.drawRoundedRect(offX - 2, offY - 2, offX + width + 2, offY + height + 2, TerminalGuiCommon.Defaults.cornerRadiusBg, TerminalGuiCommon.Defaults.backgroundColor);
        fr.drawStringWithShadow(title, offX, offY, 0xFFFFFFFF);

        for (int slot : solution) {
            int curX = (slot % 9) * 18 + offX;
            int curY = (slot / 9) * 18 + offY;
            TerminalGuiCommon.drawRoundedRect(curX, curY, curX + 16, curY + 16, TerminalGuiCommon.Defaults.cornerRadiusCell, TerminalGuiCommon.Defaults.overlayColor);
        }
        GlStateManager.popMatrix();
    }

    private static void solveFromInventory() {
        solution.clear();
        if (startsWithLetter == null) { invHash = 0; return; }
        Container container = Minecraft.getMinecraft().thePlayer != null ? Minecraft.getMinecraft().thePlayer.openContainer : null;
        if (!(container instanceof ContainerChest)) { invHash = 0; return; }
        int rows = Math.max(1, windowSize / 9);

        // Use a mutable holder for hash accumulation inside lambda
        final int[] hash = new int[]{1};
        Arrays.stream(TerminalGuiCommon.ALLOWED_SLOTS)
                .filter(s -> s < rows * 9)
                .forEach(s -> {
                    Slot slot = container.getSlot(s);
                    ItemStack stack = slot == null ? null : slot.getStack();
                    int id = (stack == null) ? 0 : Item.getIdFromItem(stack.getItem());
                    int meta = (stack == null) ? -1 : stack.getItemDamage();
                    // hash
                    hash[0] = 31 * hash[0] + id;
                    hash[0] = 31 * hash[0] + meta;
                    if (stack == null || stack.hasEffect()) return;
                    String name = EnumChatFormatting.getTextWithoutFormattingCodes(stack.getDisplayName());
                    if (name == null) return;
                    if (name.toLowerCase().startsWith(startsWithLetter)) solution.add(s);
                });
        invHash = hash[0];
        // Unlock clicker as soon as solution changes (inventory update)
        if (invHash != lastSolutionHash) {
            CLICK.clicked = false;
            lastSolutionHash = invHash;
        }
        // Process the queue immediately after unlocking
        processQueueIfReady();
    }

    private static void processQueueIfReady() {
        int[] first = TerminalGuiCommon.processQueueIfReady(queue, solution, CLICK);
        if (first != null) TerminalGuiCommon.doClickAndMark(first[0], first[1], CLICK);
    }
}