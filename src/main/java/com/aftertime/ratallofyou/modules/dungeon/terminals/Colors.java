package com.aftertime.ratallofyou.modules.dungeon.terminals;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
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

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Custom GUI and click helper for the "Colors" terminal: "Select all the <color> items!".
 */
public class Colors {
    private static boolean enabled = false;
    private static boolean registered = false;
    private static final Colors INSTANCE = new Colors();

    static {
        if (enabled && !registered) {
            try { MinecraftForge.EVENT_BUS.register(INSTANCE); registered = true; } catch (Throwable ignored) {}
        }
    }

    // Settings (shared with startswith)
    public static boolean highPingMode = false;
    public static boolean phoenixClientCompat = false;
    private static int timeoutMs = 500;
    private static int firstClickBlockMs = 0;
    private static float scale = 2.0f;
    private static int offsetX = 0;
    private static int offsetY = 0;
    private static int overlayColor = 0xFF00FF00;
    private static int backgroundColor = 0x7F000000;

    // State
    private static boolean inTerminal = false;
    private static boolean clicked = false;
    private static long openedAt = 0L;
    private static long lastClickAt = 0L;
    private static int windowSize = 0;
    private static String targetColor = null; // normalized color prefix
    private static final List<Integer> solution = new ArrayList<Integer>();
    private static final Deque<int[]> queue = new ArrayDeque<int[]>();

    private static final Pattern TITLE_PATTERN = Pattern.compile("^Select all the ([\\w ]+) items!$");

    // Name normalization replacements (based on JS)
    private static final LinkedHashMap<String, String> REPLACEMENTS = new LinkedHashMap<String, String>();
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
    public static void setHighPingMode(boolean v) { highPingMode = v; }
    public static void setPhoenixClientCompat(boolean v) { phoenixClientCompat = v; }
    public static void setScale(float v) { scale = Math.max(0.25f, Math.min(4.0f, v)); }
    public static void setOffsetX(int v) { offsetX = v; }
    public static void setOffsetY(int v) { offsetY = v; }
    public static void setTimeoutMs(int v) { timeoutMs = Math.max(0, v); }
    public static void setFirstClickBlockMs(int v) { firstClickBlockMs = Math.max(0, v); }
    public static void setOverlayColor(int argb) { overlayColor = argb; }
    public static void setBackgroundColor(int argb) { backgroundColor = argb; }

    private static void resetState() {
        inTerminal = false;
        clicked = false;
        openedAt = 0L;
        lastClickAt = 0L;
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
                    clicked = false;
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

    @SubscribeEvent
    public void onMouseInputPre(GuiScreenEvent.MouseInputEvent.Pre event) {
        if (!enabled) return;
        if (!(Minecraft.getMinecraft().currentScreen instanceof GuiChest)) return;
        if (!inTerminal || targetColor == null) return;

        // Always block mouse input while our custom GUI is active to prevent other handlers from interfering
        event.setCanceled(true);

        // Only act on actual mouse button press (not release/wheel)
        if (!Mouse.getEventButtonState()) return;
        int button = Mouse.getEventButton();
        if (button != 0) return; // left click only

        long now = System.currentTimeMillis();
        if (openedAt + firstClickBlockMs > now) return;

        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        int mouseX = Mouse.getEventX() * sr.getScaledWidth() / Minecraft.getMinecraft().displayWidth;
        int mouseY = sr.getScaledHeight() - Mouse.getEventY() * sr.getScaledHeight() / Minecraft.getMinecraft().displayHeight - 1;

        int rows = Math.max(1, windowSize / 9);
        int width = (int) (9 * 18 * scale);
        int height = (int) (rows * 18 * scale);
        int offX = sr.getScaledWidth() / 2 - width / 2 + (int) (offsetX * scale);
        int offY = sr.getScaledHeight() / 2 - height / 2 + (int) (offsetY * scale);

        int slotX = (int) Math.floor((mouseX - offX) / (18f * scale));
        int slotY = (int) Math.floor((mouseY - offY) / (18f * scale));
        if (slotX < 0 || slotX > 8 || slotY < 0) return;
        int slot = slotX + slotY * 9;
        if (slot < 0 || slot >= windowSize) return;

        boolean isSolution = solution.contains(slot);
        if (isSolution) {
            if (highPingMode || phoenixClientCompat || !clicked) predict(slot, 0);
            if (highPingMode && clicked) {
                queue.addLast(new int[]{slot, 0});
            } else {
                doClick(slot, 0);
            }
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (!enabled || !inTerminal) return;
        if (event.phase != TickEvent.Phase.END) return;
        if (clicked && (System.currentTimeMillis() - lastClickAt) >= timeoutMs) {
            queue.clear();
            clicked = false;
            solveFromInventory();
        }
    }

    // Logic
    private static void drawOverlay() {
        Minecraft mc = Minecraft.getMinecraft();
        FontRenderer fr = mc.fontRendererObj;
        ScaledResolution sr = new ScaledResolution(mc);

        int rows = Math.max(1, windowSize / 9);
        int width = (int) (9 * 18);
        int height = (int) (rows * 18);

        int offX = (int) (sr.getScaledWidth() / scale / 2 - width / 2 + offsetX + 1);
        int offY = (int) (sr.getScaledHeight() / scale / 2 - height / 2 + offsetY);

        String title = "§8[§bRAT Terminal§8] §aColors";

        GlStateManager.pushMatrix();
        GlStateManager.scale(scale, scale, 1f);
        TerminalGuiCommon.drawRect(offX - 2, offY - 2, offX + width + 2, offY + height + 2, backgroundColor);
        fr.drawStringWithShadow(title, offX, offY, 0xFFFFFFFF);
        for (int i = 0; i < windowSize; i++) {
            if (!solution.contains(i)) continue;
            int curX = (i % 9) * 18 + offX;
            int curY = (i / 9) * 18 + offY;
            TerminalGuiCommon.drawRect(curX, curY, curX + 16, curY + 16, overlayColor);
        }
        GlStateManager.popMatrix();
    }

    private static void solveFromInventory() {
        solution.clear();
        if (targetColor == null) return;
        Container container = Minecraft.getMinecraft().thePlayer != null ? Minecraft.getMinecraft().thePlayer.openContainer : null;
        if (!(container instanceof ContainerChest)) return;
        int rows = Math.max(1, windowSize / 9);
        for (int s : TerminalGuiCommon.ALLOWED_SLOTS) {
            if (s < 0 || s >= rows * 9) continue;
            Slot slot = container.getSlot(s);
            if (slot == null) continue;
            ItemStack stack = slot.getStack();
            if (stack == null) continue;
            if (stack.hasEffect()) continue; // skip enchanted
            String name = EnumChatFormatting.getTextWithoutFormattingCodes(stack.getDisplayName());
            if (name == null) continue;
            String normalized = normalizeName(name.toLowerCase());
            if (normalized.startsWith(targetColor)) {
                solution.add(s);
            }
        }
    }

    private static String normalizeName(String name) {
        for (Map.Entry<String, String> e : REPLACEMENTS.entrySet()) {
            if (name.startsWith(e.getKey())) {
                name = e.getValue() + name.substring(e.getKey().length());
            }
        }
        return name;
    }

    private static void predict(int slot, int button) {
        solution.remove((Integer) slot);
    }

    private static void doClick(int slot, int button) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || mc.playerController == null) return;
        clicked = true;
        lastClickAt = System.currentTimeMillis();
        try {
            mc.playerController.windowClick(mc.thePlayer.openContainer.windowId, slot, button, 0, mc.thePlayer);
        } catch (Throwable ignored) {}
    }

    private static void processQueueIfReady() {
        if (queue.isEmpty()) return;
        boolean allValid = true;
        for (int[] q : queue) if (!solution.contains(q[0])) { allValid = false; break; }
        if (allValid) {
            for (int[] q : queue) predict(q[0], q[1]);
            int[] first = queue.pollFirst();
            if (first != null) doClick(first[0], first[1]);
        } else {
            queue.clear();
        }
    }
}
