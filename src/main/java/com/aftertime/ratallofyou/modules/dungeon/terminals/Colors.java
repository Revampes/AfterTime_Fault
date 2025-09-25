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
import org.lwjgl.input.Mouse;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Custom GUI and click helper for the "Colors" terminal: "Select all the <color> items!".
 */
public class Colors {
    private static boolean enabled = true;
    private static boolean registered = false;
    private static final Colors INSTANCE = new Colors();

    static {
        try { MinecraftForge.EVENT_BUS.register(INSTANCE); registered = true; } catch (Throwable ignored) {}
    }

    // Use shared defaults and click tracker
    private static final TerminalGuiCommon.ClickTracker CLICK = new TerminalGuiCommon.ClickTracker();

    // State
    private static boolean inTerminal = false;
    private static long openedAt = 0L;
    private static int windowSize = 0;
    private static String targetColor = null; // normalized color prefix
    private static final List<Integer> solution = new ArrayList<>();
    private static final Deque<int[]> queue = new ArrayDeque<>();

    private static final Pattern TITLE_PATTERN = Pattern.compile("^Select all the ([\\w ]+) items!$");

    // Name normalization replacements (based on JS)
    private static final LinkedHashMap<String, String> REPLACEMENTS = new LinkedHashMap<>();
    static {
        REPLACEMENTS.put("light gray", "silver");
        REPLACEMENTS.put("wool", "white");
        REPLACEMENTS.put("bone", "white");
        REPLACEMENTS.put("ink", "black");
        REPLACEMENTS.put("lapis", "blue");
        REPLACEMENTS.put("cocoa", "brown");
        REPLACEMENTS.put("dandelion", "yellow");
        REPLACEMENTS.put("rose", "red");
        REPLACEMENTS.put("cactus", "green");
    }

    // API
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
        targetColor = null;
        solution.clear();
        queue.clear();
    }

    // Events
    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        if (!enabled) return;
        if (event.gui instanceof GuiChest) {
            GuiChest chest = (GuiChest) event.gui;
            String title = TerminalGuiCommon.getChestTitle(chest);
            if (title != null) {
                Matcher m = TITLE_PATTERN.matcher(title);
                if (m.matches()) {
                    targetColor = m.group(1).toLowerCase();
                    inTerminal = true;
                    CLICK.reset();
                    openedAt = System.currentTimeMillis();
                    queue.clear();
                    windowSize = TerminalGuiCommon.getChestWindowSize(chest);
                    return;
                }
            }
        }
        resetState();
    }

    @SubscribeEvent
    public void onDrawScreenPre(GuiScreenEvent.DrawScreenEvent.Pre event) {
        if (!enabled) return;
        if (!(event.gui instanceof GuiChest)) return;
        if (!inTerminal || targetColor == null) return;
        event.setCanceled(true);
        solveFromInventory();
        processQueueIfReady();
        drawOverlay();
    }

    // Re-add timeout handling for parity with other terminals
    @SubscribeEvent
    public void onClientTick(net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent event) {
        if (!enabled || !inTerminal) return;
        if (event.phase != net.minecraftforge.fml.common.gameevent.TickEvent.Phase.END) return;
        if (TerminalGuiCommon.hasTimedOut(CLICK, TerminalGuiCommon.Defaults.timeoutMs)) {
            queue.clear();
            CLICK.reset();
            solveFromInventory();
        }
    }

    @SubscribeEvent
    public void onGuiRender(GuiScreenEvent.DrawScreenEvent.Pre event) {
        if (!enabled || !inTerminal || !(Minecraft.getMinecraft().currentScreen instanceof GuiChest)) return;
        while (!queue.isEmpty() && !CLICK.clicked) {
            int[] click = queue.poll();
            if (click != null) {
                TerminalGuiCommon.doClickAndMark(click[0], click[1], CLICK);
            }
        }
    }

    // Logic
    private static void drawOverlay() {
        Minecraft mc = Minecraft.getMinecraft();
        FontRenderer fr = mc.fontRendererObj;
        int[] grid = TerminalGuiCommon.computeGrid(windowSize, TerminalGuiCommon.Defaults.scale, TerminalGuiCommon.Defaults.offsetX, TerminalGuiCommon.Defaults.offsetY);
        int width = grid[1], height = grid[2], offX = grid[3], offY = grid[4];

        String title = "§8[§bRAT Terminal§8] §aColors";

        GlStateManager.pushMatrix();
        GlStateManager.scale(TerminalGuiCommon.Defaults.scale, TerminalGuiCommon.Defaults.scale, 1f);
        TerminalGuiCommon.drawRoundedRect(offX - 2, offY - 2, offX + width + 2, offY + height + 2, TerminalGuiCommon.Defaults.cornerRadiusBg, TerminalGuiCommon.Defaults.backgroundColor);
        fr.drawStringWithShadow(title, offX, offY, 0xFFFFFFFF);
        // Iterate solution directly to avoid scanning all slots
        for (int slot : solution) {
            int curX = (slot % 9) * 18 + offX;
            int curY = (slot / 9) * 18 + offY;
            TerminalGuiCommon.drawRoundedRect(curX, curY, curX + 16, curY + 16, TerminalGuiCommon.Defaults.cornerRadiusCell, TerminalGuiCommon.Defaults.overlayColor);
        }
        GlStateManager.popMatrix();
    }

    private static int lastSolutionHash = 0;

    private static void solveFromInventory() {
        solution.clear();
        if (targetColor == null) return;
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
                    String normalized = normalizeName(name.toLowerCase());
                    return normalized.startsWith(targetColor) ? s : null;
                })
                .filter(Objects::nonNull)
                .forEach(solution::add);
        // Unlock clicker as soon as solution changes (inventory update)
        int solutionHash = solution.hashCode();
        if (solutionHash != lastSolutionHash) {
            CLICK.clicked = false;
            lastSolutionHash = solutionHash;
        }
        // Process the queue immediately after unlocking
        processQueueIfReady();
    }

    private static String normalizeName(String name) {
        for (Map.Entry<String, String> e : REPLACEMENTS.entrySet()) {
            if (name.startsWith(e.getKey())) {
                name = e.getValue() + name.substring(e.getKey().length());
            }
        }
        return name;
    }

    private static void processQueueIfReady() {
        int[] first = TerminalGuiCommon.processQueueIfReady(queue, solution, CLICK);
        if (first != null) TerminalGuiCommon.doClickAndMark(first[0], first[1], CLICK);
    }

    @SubscribeEvent
    public void onMouseInputPre(GuiScreenEvent.MouseInputEvent.Pre event) {
        if (!enabled) return;
        if (!(Minecraft.getMinecraft().currentScreen instanceof GuiChest)) return;
        if (!inTerminal || targetColor == null) return;
        event.setCanceled(true);
        if (!Mouse.getEventButtonState()) return;
        int button = Mouse.getEventButton();
        if (button != 0) return; // left click only
        long now = System.currentTimeMillis();
        if (openedAt + TerminalGuiCommon.Defaults.firstClickBlockMs > now) return;
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

    private static void queueClick(int slot, int button) {
        if (slot >= 0 && slot < windowSize) {
            queue.offer(new int[]{slot, button});
        }
    }
}