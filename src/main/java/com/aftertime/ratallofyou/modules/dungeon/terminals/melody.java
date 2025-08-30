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

import java.util.regex.Pattern;

/**
 * Custom GUI and click helper for the "Melody" terminal: "Click the button on time!".
 */
public class melody {
    // Enable by default and auto-register
    private static boolean enabled = true;
    private static boolean registered = false;
    private static final melody INSTANCE = new melody();

    static {
        try { MinecraftForge.EVENT_BUS.register(INSTANCE); registered = true; } catch (Throwable ignored) {}
    }

    // Shared click tracker and defaults
    private static final TerminalGuiCommon.ClickTracker CLICK = new TerminalGuiCommon.ClickTracker();

    // State
    private static boolean inTerminal = false;
    private static long openedAt = 0L;
    private static int windowSize = 0;

    // Melody indicators (derived from inventory every frame)
    // correct: column index 0..6 to highlight; button: row index 0..3; current: moving note column 0..6
    private static int correct = -1;
    private static int button = -1;
    private static int current = -1;

    // Track inventory changes to promptly unlock after successful click
    private static int invHash = 0;
    private static int hashAtClick = 0;

    private static final Pattern TITLE_PATTERN = Pattern.compile("^Click the button on time!$");

    private static final int[] BUTTON_SLOTS = new int[]{16, 25, 34, 43};

    // Overlay colors (ARGB)
    private static final int COLOR_COLUMN = 0xFFFFD700;         // gold column highlight
    private static final int COLOR_BUTTON_CORRECT = 0xFF2ECC71; // green for active button row's button
    private static final int COLOR_BUTTON_INCORRECT = 0xFFE74C3C;// red for other buttons
    private static final int COLOR_SLOT = 0xFF00BFFF;           // deep sky blue for current note slot

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
        correct = -1; button = -1; current = -1;
        invHash = 0; hashAtClick = 0;
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

        // Fallback detection if GuiOpen missed
        if (!inTerminal) {
            GuiChest chest = (GuiChest) event.gui;
            String title = TerminalGuiCommon.getChestTitle(chest);
            if (title != null && TITLE_PATTERN.matcher(title).matches()) {
                inTerminal = true;
                CLICK.reset();
                if (openedAt == 0L) openedAt = System.currentTimeMillis();
                windowSize = TerminalGuiCommon.getChestWindowSize(chest);
            } else {
                return;
            }
        }

        event.setCanceled(true);
        // Recompute from inventory and detect update to clear click lock promptly
        solveFromInventory();
        if (CLICK.clicked && invHash != hashAtClick) {
            CLICK.clicked = false;
        }
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
                windowSize = TerminalGuiCommon.getChestWindowSize(chest);
                solveFromInventory();
            } else {
                return; // not our GUI; don't cancel
            }
        }

        // Block other handlers while our GUI is active
        event.setCanceled(true);

        // Only on press
        if (!Mouse.getEventButtonState()) return;
        int buttonIdx = Mouse.getEventButton();
        if (buttonIdx != 0) return; // left click only

        long now = System.currentTimeMillis();
        if (openedAt + TerminalGuiCommon.Defaults.firstClickBlockMs > now) return;

        int slot = TerminalGuiCommon.computeSlotUnderMouse(windowSize, TerminalGuiCommon.Defaults.scale, TerminalGuiCommon.Defaults.offsetX, TerminalGuiCommon.Defaults.offsetY);
        if (slot < 0) return;

        if (isButtonSlot(slot)) {
            // Avoid overlapping sends: if a click is in-flight, let the inventory update unlock it
            if (CLICK.clicked) return;
            // Capture current inventory hash to detect the next server update
            hashAtClick = invHash;
            TerminalGuiCommon.doClickAndMark(slot, 0, CLICK);
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (!enabled || !inTerminal) return;
        if (event.phase != TickEvent.Phase.END) return;
        if (TerminalGuiCommon.hasTimedOut(CLICK, TerminalGuiCommon.Defaults.timeoutMs)) {
            CLICK.clicked = false;
            solveFromInventory();
        }
    }

    // ===================== Logic =====================

    private static boolean isButtonSlot(int slot) {
        for (int s : BUTTON_SLOTS) if (s == slot) return true;
        return false;
    }

    private static void drawOverlay() {
        Minecraft mc = Minecraft.getMinecraft();
        FontRenderer fr = mc.fontRendererObj;
        int[] grid = TerminalGuiCommon.computeGrid(windowSize, TerminalGuiCommon.Defaults.scale, TerminalGuiCommon.Defaults.offsetX, TerminalGuiCommon.Defaults.offsetY);
        int width = grid[1], height = grid[2], offX = grid[3], offY = grid[4];

        String title = "§8[§bRAT Terminal§8] §aMelody";

        GlStateManager.pushMatrix();
        GlStateManager.scale(TerminalGuiCommon.Defaults.scale, TerminalGuiCommon.Defaults.scale, 1f);
        TerminalGuiCommon.drawRoundedRect(offX - 2, offY - 2, offX + width + 2, offY + height + 2, TerminalGuiCommon.Defaults.cornerRadiusBg, TerminalGuiCommon.Defaults.backgroundColor);
        fr.drawStringWithShadow(title, offX, offY, 0xFFFFFFFF);

        // Column highlight (based on correct)
        if (correct >= 0) {
            int colX = offX + (correct + 1) * 18;
            TerminalGuiCommon.drawRoundedRect(colX, offY + 18, colX + 16, offY + 18 + 70, TerminalGuiCommon.Defaults.cornerRadiusCell, COLOR_COLUMN);
        }

        // Draw only the relevant slots without scanning the whole grid
        int buttonSlot = (button >= 0) ? (button * 9 + 16) : -1;
        int currentSlot = (button >= 0 && current >= 0) ? (button * 9 + 10 + current) : -1;

        if (buttonSlot >= 0 && buttonSlot < windowSize) {
            int curX = (buttonSlot % 9) * 18 + offX;
            int curY = (buttonSlot / 9) * 18 + offY;
            TerminalGuiCommon.drawRoundedRect(curX, curY, curX + 16, curY + 16, TerminalGuiCommon.Defaults.cornerRadiusCell, COLOR_BUTTON_CORRECT);
        }
        for (int s : BUTTON_SLOTS) {
            if (s == buttonSlot || s >= windowSize) continue;
            int curX = (s % 9) * 18 + offX;
            int curY = (s / 9) * 18 + offY;
            TerminalGuiCommon.drawRoundedRect(curX, curY, curX + 16, curY + 16, TerminalGuiCommon.Defaults.cornerRadiusCell, COLOR_BUTTON_INCORRECT);
        }
        if (currentSlot >= 0 && currentSlot < windowSize) {
            int curX = (currentSlot % 9) * 18 + offX;
            int curY = (currentSlot / 9) * 18 + offY;
            TerminalGuiCommon.drawRoundedRect(curX, curY, curX + 16, curY + 16, TerminalGuiCommon.Defaults.cornerRadiusCell, COLOR_SLOT);
        }
        GlStateManager.popMatrix();
    }

    private static void solveFromInventory() {
        correct = -1; button = -1; current = -1;
        Container container = Minecraft.getMinecraft().thePlayer != null ? Minecraft.getMinecraft().thePlayer.openContainer : null;
        if (!(container instanceof ContainerChest)) { invHash = 0; return; }

        // Compute a simple hash of the inventory state relevant to Melody to detect server updates quickly
        int h = 1;
        int size = Math.max(0, windowSize);
        int activeSlot = -1;
        int correctSlot = -1;
        for (int i = 0; i < size; i++) {
            Slot s = container.getSlot(i);
            ItemStack stack = s == null ? null : s.getStack();
            int id = (stack == null) ? 0 : Item.getIdFromItem(stack.getItem());
            int meta = (stack == null) ? -1 : stack.getItemDamage();
            // Hash every slot's id/meta lightly to reflect changes
            h = 31 * h + id;
            h = 31 * h + meta;
            // Find the active lime pane (meta 5) and the correct column marker (meta 2)
            if (stack == null || stack.hasEffect() || id != 160) continue;
            if (meta == 5) activeSlot = i;      // lime pane indicates moving note
            else if (meta == 2) correctSlot = i; // green pane indicates correct column marker (top row)
        }
        invHash = h;

        if (activeSlot != -1) {
            int row = activeSlot / 9;
            int col = activeSlot % 9;
            button = row - 1;           // JS: Math.floor(slot/9) - 1
            current = col - 1;          // JS: slot % 9 - 1
            if (button < 0) button = -1; // safety if reading unexpected rows
            if (current < 0) current = -1;
        }
        if (correctSlot != -1) {
            int col = correctSlot % 9;
            correct = col - 1;          // JS: correct = slot - 1 (top row => slot == col)
            if (correct < 0) correct = -1;
        }
    }
}