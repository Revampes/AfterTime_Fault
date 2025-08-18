package com.aftertime.ratallofyou.modules.dungeon.terminals;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.lwjgl.input.Mouse;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
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
        // Auto-register at class load if enabled to avoid missing events on startup
        if (enabled && !registered) {
            try {
                MinecraftForge.EVENT_BUS.register(INSTANCE);
                registered = true;
            } catch (Throwable ignored) {}
        }
    }

    // Settings (defaults taken from the provided JS config). We expose simple toggles via ModSettingsGui.
    public static boolean highPingMode = false;
    public static boolean phoenixClientCompat = false;

    // Internal tuning (kept as defaults for now)
    private static int timeoutMs = 500;          // Settings.terminalsTimeout
    private static int firstClickBlockMs = 0;    // Settings.terminalsFirstClick
    private static float scale = 2.0f;           // Settings.terminalsScale
    private static int offsetX = 0;              // Settings.terminalsOffsetX
    private static int offsetY = 0;              // Settings.terminalsOffsetY

    // Colors (ARGB)
    private static int overlayColor = 0xFF00FF00;        // Settings.terminalsColor (opaque green)
    private static int backgroundColor = 0x7F000000;     // Settings.terminalsBackgroundColor (black 50%)

    // State
    private static boolean inTerminal = false;
    private static boolean clicked = false;
    private static long openedAt = 0L;
    private static long lastClickAt = 0L;

    private static int windowSize = 0; // number of slots in the chest window (rows*9)
    private static String startsWithLetter = null; // single letter to match

    private static final List<Integer> solution = new ArrayList<Integer>();
    private static final int[] allowedSlots = new int[]{
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };

    private static final Deque<int[]> queue = new ArrayDeque<int[]>(); // entries: {slot, button}

    private static final Pattern TITLE_PATTERN = Pattern.compile("^What starts with: '([A-Za-z])'\\?$");

    // Debug support (disabled by default)
    private static boolean debugEnabled = false;
    private static boolean debugAnnouncedDraw = false;

    public static void setDebug(boolean enabled) { debugEnabled = enabled; }
    private static void debug(String msg) {
        if (!debugEnabled) return;
        // Keep silent by default; if enabled, log to console only
        System.out.println("[StartsWith] " + msg);
    }

    public static void setEnabled(boolean on) {
        if (enabled == on) return;
        enabled = on;
        if (on) {
            if (!registered) {
                MinecraftForge.EVENT_BUS.register(INSTANCE);
                registered = true;
            }
            debug("Module enabled, registering event listeners");
        } else {
            if (registered) {
                try { MinecraftForge.EVENT_BUS.unregister(INSTANCE); } catch (Throwable ignored) {}
                registered = false;
            }
            debug("Module disabled, unregistering event listeners and resetting state");
            resetState();
        }
    }

    public static void setHighPingMode(boolean v) { highPingMode = v; }
    public static void setPhoenixClientCompat(boolean v) { phoenixClientCompat = v; }
    public static void setOverlayColor(int argb) { overlayColor = argb; }
    public static void setBackgroundColor(int argb) { backgroundColor = argb; }
    // New setters to allow runtime config from GUI
    public static void setScale(float v) { scale = Math.max(0.25f, Math.min(4.0f, v)); }
    public static void setOffsetX(int v) { offsetX = v; }
    public static void setOffsetY(int v) { offsetY = v; }
    public static void setTimeoutMs(int v) { timeoutMs = Math.max(0, v); }
    public static void setFirstClickBlockMs(int v) { firstClickBlockMs = Math.max(0, v); }

    private static void resetState() {
        inTerminal = false;
        clicked = false;
        openedAt = 0L;
        lastClickAt = 0L;
        windowSize = 0;
        startsWithLetter = null;
        solution.clear();
        queue.clear();
        debugAnnouncedDraw = false;
        debug("State reset");
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
            String title = getChestTitle(chest);
            debug("GuiOpen: chest title=" + title);
            if (title != null) {
                Matcher m = TITLE_PATTERN.matcher(title);
                if (m.matches()) {
                    startsWithLetter = m.group(1).toLowerCase();
                    inTerminal = true;
                    clicked = false;
                    openedAt = System.currentTimeMillis();
                    queue.clear();
                    // compute window size via container
                    windowSize = getChestWindowSize(chest);
                    debugAnnouncedDraw = false;
                    debug("Detected Starts With terminal. Letter='" + startsWithLetter + "' windowSize=" + windowSize);
                    return;
                } else {
                    debug("Title did not match Starts With pattern");
                }
            }
        }
        // If any other GUI opens, exit terminal mode
        if (inTerminal) debug("Exiting terminal (other GUI opened)");
        resetState();
    }

    @SubscribeEvent
    public void onDrawScreenPre(GuiScreenEvent.DrawScreenEvent.Pre event) {
        if (!enabled) return;
        if (!(event.gui instanceof GuiChest)) return;
        if (!inTerminal || startsWithLetter == null) return;
        if (!debugAnnouncedDraw) { debug("DrawScreenPre: rendering custom overlay"); debugAnnouncedDraw = true; }

        // Prevent vanilla GUI from drawing
        event.setCanceled(true);

        // Re-solve every frame from current inventory
        solveFromInventory();
        // If we queued clicks (high ping mode) and they are still valid, send the next one
        processQueueIfReady();

        // Draw our custom overlay
        drawOverlay((GuiChest) event.gui, event.mouseX, event.mouseY);
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
        if (openedAt + firstClickBlockMs > now) {
            debug("Click blocked by first-click protection (" + (firstClickBlockMs) + "ms)");
            return;
        }

        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        int mouseX = Mouse.getEventX() * sr.getScaledWidth() / Minecraft.getMinecraft().displayWidth;
        int mouseY = sr.getScaledHeight() - Mouse.getEventY() * sr.getScaledHeight() / Minecraft.getMinecraft().displayHeight - 1;

        // Compute slot under mouse within our virtual grid
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
        debug("Mouse click at (" + mouseX + "," + mouseY + ") => slot=" + slot + " solution=" + isSolution + " clickedBefore=" + clicked + " queueSize=" + queue.size());
        // If it's part of the solution, handle click ourselves
        if (isSolution) {
            if (highPingMode || phoenixClientCompat || !clicked) predict(slot, 0);
            if (highPingMode && clicked) {
                queue.addLast(new int[]{slot, 0});
                debug("Queued click slot=" + slot + ", queue now=" + queue.size());
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
            debug("Timeout reached (" + timeoutMs + "ms), resyncing");
            queue.clear();
            clicked = false;
            solveFromInventory();
        }
    }

    // ==========================================
    // Core logic
    // ==========================================

    private static void drawOverlay(GuiChest guiChest, int mouseX, int mouseY) {
        Minecraft mc = Minecraft.getMinecraft();
        FontRenderer fr = mc.fontRendererObj;
        ScaledResolution sr = new ScaledResolution(mc);

        int rows = Math.max(1, windowSize / 9);
        int width = (int) (9 * 18);
        int height = (int) (rows * 18);

        int offX = (int) (sr.getScaledWidth() / scale / 2 - width / 2 + offsetX + 1);
        int offY = (int) (sr.getScaledHeight() / scale / 2 - height / 2 + offsetY);

        String title = "§8[§bRAT Terminal§8] §aStarts With";

        GlStateManager.pushMatrix();
        GlStateManager.scale(scale, scale, 1f);
        // background
        drawRect(offX - 2, offY - 2, offX + width + 2, offY + height + 2, backgroundColor);
        // title
        fr.drawStringWithShadow(title, offX, offY, 0xFFFFFFFF);

        // draw solution highlights
        for (int i = 0; i < windowSize; i++) {
            if (!solution.contains(i)) continue;
            int curX = (i % 9) * 18 + offX;
            int curY = (i / 9) * 18 + offY;
            drawRect(curX, curY, curX + 16, curY + 16, overlayColor);
        }
        GlStateManager.popMatrix();
    }

    private static void solveFromInventory() {
        solution.clear();
        if (startsWithLetter == null) return;
        Container container = Minecraft.getMinecraft().thePlayer != null ? Minecraft.getMinecraft().thePlayer.openContainer : null;
        if (!(container instanceof ContainerChest)) return;
        int rows = Math.max(1, windowSize / 9);
        int found = 0;
        // Build a quick lookup for allowed slots
        for (int i = 0; i < allowedSlots.length; i++) {
            int s = allowedSlots[i];
            if (s < 0 || s >= rows * 9) continue;
            Slot slot = container.getSlot(s);
            if (slot == null) continue;
            ItemStack stack = slot.getStack();
            if (stack == null) continue;
            if (stack.hasEffect()) continue; // not enchanted
            String name = EnumChatFormatting.getTextWithoutFormattingCodes(stack.getDisplayName());
            if (name == null) continue;
            if (name.toLowerCase().startsWith(startsWithLetter)) {
                solution.add(s);
                found++;
            }
        }
        debug("Solved: found " + found + " matching slots => " + solution);
    }

    private static void predict(int slot, int button) {
        // optimistic UI update: remove from solution until resync
        solution.remove((Integer) slot);
    }

    private static void doClick(int slot, int button) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || mc.playerController == null) return;
        clicked = true;
        lastClickAt = System.currentTimeMillis();
        debug("Sending click: windowId=" + mc.thePlayer.openContainer.windowId + " slot=" + slot + " button=" + button);
        try {
            mc.playerController.windowClick(mc.thePlayer.openContainer.windowId, slot, button, 0, mc.thePlayer);
        } catch (Throwable t) {
            debug("Click error: " + t.getMessage());
        }
    }

    private static void processQueueIfReady() {
        if (queue.isEmpty()) return;
        // Ensure all queued slots are still valid
        boolean allValid = true;
        for (int[] q : queue) {
            if (!solution.contains(q[0])) { allValid = false; break; }
        }
        debug("Processing queue size=" + queue.size() + ", allValid=" + allValid);
        if (allValid) {
            for (int[] q : queue) predict(q[0], q[1]);
            int[] first = queue.pollFirst();
            if (first != null) doClick(first[0], first[1]);
        } else {
            queue.clear();
        }
    }

    // ==========================================
    // Helpers
    // ==========================================

    private static int getChestWindowSize(GuiChest chest) {
        try {
            Container cont = chest.inventorySlots;
            if (cont instanceof ContainerChest) {
                // Prefer using the lower inventory size via first slot's inventory
                try {
                    Slot s0 = cont.getSlot(0);
                    if (s0 != null && s0.inventory != null) {
                        int size = s0.inventory.getSizeInventory();
                        debug("getChestWindowSize via slot0 inventory: " + size);
                        return size;
                    }
                } catch (Throwable t) {
                    debug("getChestWindowSize slot0 path failed: " + t.getMessage());
                }
                // Fallback: container slots minus player inv
                int size = Math.min(54, Math.max(9, cont.inventorySlots.size() - 36));
                debug("getChestWindowSize via container fallback: " + size);
                return size;
            }
        } catch (Throwable t) {
            debug("getChestWindowSize error: " + t.getMessage());
        }
        // Reflection fallback
        try {
            Object lower = ReflectionHelper.getPrivateValue(GuiChest.class, chest, "lowerChestInventory", "field_147015_w");
            if (lower != null) {
                int size = (Integer) lower.getClass().getMethod("getSizeInventory").invoke(lower);
                debug("getChestWindowSize via reflection: " + size);
                return size;
            }
        } catch (Throwable t) {
            debug("getChestWindowSize reflection failed: " + t.getMessage());
        }
        debug("getChestWindowSize unknown, defaulting 54");
        return 54;
    }

    private static String getChestTitle(GuiChest chest) {
        // Try via container (ContainerChest lower inventory)
        try {
            Container cont = chest.inventorySlots;
            if (cont instanceof ContainerChest) {
                // 1) Try public getter if present
                try {
                    Object lowerInv = ContainerChest.class.getMethod("getLowerChestInventory").invoke(cont);
                    if (lowerInv instanceof IInventory) {
                        IChatComponent comp = ((IInventory) lowerInv).getDisplayName();
                        if (comp != null) {
                            String title = comp.getUnformattedText();
                            debug("getChestTitle via ContainerChest#getLowerChestInventory: " + title);
                            return title;
                        }
                    }
                } catch (Throwable ignored) {}
                // 2) Reflection on ContainerChest private field
                try {
                    Object lower = ReflectionHelper.getPrivateValue(ContainerChest.class, (ContainerChest) cont, "lowerChestInventory", "field_75155_e");
                    if (lower instanceof IInventory) {
                        IChatComponent comp = ((IInventory) lower).getDisplayName();
                        if (comp != null) {
                            String title = comp.getUnformattedText();
                            debug("getChestTitle via ContainerChest private field: " + title);
                            return title;
                        }
                    }
                } catch (Throwable t) {
                    debug("getChestTitle ContainerChest reflection failed: " + t.getMessage());
                }
                // 3) Fallback to first slot inventory
                try {
                    if (cont.inventorySlots != null && !cont.inventorySlots.isEmpty()) {
                        Slot s0 = cont.getSlot(0);
                        if (s0 != null && s0.inventory != null) {
                            IChatComponent comp = s0.inventory.getDisplayName();
                            if (comp != null) {
                                String title = comp.getUnformattedText();
                                debug("getChestTitle via slot0: " + title);
                                return title;
                            }
                        }
                    }
                } catch (Throwable t) {
                    debug("getChestTitle via slot0 failed: " + t.getMessage());
                }
            } else if (cont != null) {
                debug("Non-ContainerChest: " + cont.getClass().getName() + " slots=" + (cont.inventorySlots == null ? -1 : cont.inventorySlots.size()));
            }
        } catch (Throwable t) {
            debug("getChestTitle container path error: " + t.getMessage());
        }
        // 4) Reflection fallback to GuiChest.lowerChestInventory
        try {
            Object lower = ReflectionHelper.getPrivateValue(GuiChest.class, chest, "lowerChestInventory", "field_147015_w");
            if (lower instanceof IInventory) {
                IChatComponent comp = ((IInventory) lower).getDisplayName();
                if (comp != null) {
                    String title = comp.getUnformattedText();
                    debug("getChestTitle via GuiChest private field: " + title);
                    return title;
                }
            } else if (lower != null) {
                Object comp = lower.getClass().getMethod("getDisplayName").invoke(lower);
                if (comp instanceof IChatComponent) {
                    String title = ((IChatComponent) comp).getUnformattedText();
                    debug("getChestTitle via GuiChest reflection IChatComponent: " + title);
                    return title;
                } else if (comp != null) {
                    try {
                        String title = (String) comp.getClass().getMethod("getUnformattedText").invoke(comp);
                        debug("getChestTitle via GuiChest reflection string method: " + title);
                        return title;
                    } catch (Throwable ignored) {}
                }
            }
        } catch (Throwable t) {
            debug("getChestTitle GuiChest reflection failed: " + t.getMessage());
        }
        debug("getChestTitle: null");
        return null;
    }

    // Simple filled-rect helper (ARGB)
    private static void drawRect(int left, int top, int right, int bottom, int color) {
        // Copied behavior of Gui.drawRect, but we avoid referencing Gui directly; use GL via Gui.drawRect if available.
        net.minecraft.client.gui.Gui.drawRect(left, top, right, bottom, color);
    }
}
