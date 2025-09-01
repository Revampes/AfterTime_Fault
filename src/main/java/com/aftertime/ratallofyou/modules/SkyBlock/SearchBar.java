package com.aftertime.ratallofyou.modules.SkyBlock;

import com.aftertime.ratallofyou.UI.UIHighlighter;
import com.aftertime.ratallofyou.UI.config.ConfigData.AllConfig;
import com.aftertime.ratallofyou.UI.config.ConfigData.ModuleInfo;
import com.aftertime.ratallofyou.UI.config.ConfigData.UIPosition;
import com.aftertime.ratallofyou.UI.config.ConfigData.BaseConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.SimpleBindings;
import java.util.ArrayList;
import java.util.List;

public class SearchBar {
    private final Minecraft mc = Minecraft.getMinecraft();

    private GuiTextField textField;
    private final List<Integer> highlight = new ArrayList<>();
    private final List<Integer> darken = new ArrayList<>();
    private String calc = null;

    // Removed fixed constants; use config-driven dimensions
    private int lastWidth = -1;
    private int lastHeight = -1;

    // Latch to avoid double-processing the same key press
    private static final boolean[] keyLatch = new boolean[256];

    private void clearKeyLatch() {
        for (int i = 0; i < keyLatch.length; i++) keyLatch[i] = false;
    }

    // Keep track of temporarily remapped Inventory key while focused
    private Integer savedInvKey = null;

    // Returns the actual inventory key code to treat as the inventory toggle even if we temporarily remapped it to NONE
    private int getEffectiveInventoryKey() {
        if (savedInvKey != null) return savedInvKey;
        if (mc != null && mc.gameSettings != null && mc.gameSettings.keyBindInventory != null)
            return mc.gameSettings.keyBindInventory.getKeyCode();
        return Keyboard.KEY_E;
    }

    private boolean isEnabled() {
        ModuleInfo cfg = (ModuleInfo) AllConfig.INSTANCE.MODULES.get("skyblock_searchbar");
        return cfg != null && Boolean.TRUE.equals(cfg.Data);
    }

    private int getWidthCfg() {
        BaseConfig<?> w = AllConfig.INSTANCE.Pos_CONFIGS.get("searchbar_width");
        Object v = w != null ? w.Data : null;
        if (v instanceof Integer) return (Integer) v;
        return 192;
    }

    private int getHeightCfg() {
        BaseConfig<?> h = AllConfig.INSTANCE.Pos_CONFIGS.get("searchbar_height");
        Object v = h != null ? h.Data : null;
        if (v instanceof Integer) return (Integer) v;
        return 16;
    }

    private UIPosition getPos() {
        UIPosition p = (UIPosition) AllConfig.INSTANCE.Pos_CONFIGS.get("searchbar_pos").Data;
        if (p == null) {
            ScaledResolution res = new ScaledResolution(mc);
            int WIDTH = getWidthCfg();
            p = new UIPosition(res.getScaledWidth() / 2 - WIDTH / 2, (res.getScaledHeight() * 6) / 7);
            @SuppressWarnings("unchecked")
            BaseConfig<UIPosition> cfg = (BaseConfig<UIPosition>) AllConfig.INSTANCE.Pos_CONFIGS.get("searchbar_pos");
            cfg.Data = p;
        }
        return p;
    }

    private void ensureField() {
        int WIDTH = getWidthCfg();
        int HEIGHT = getHeightCfg();
        if (textField == null || WIDTH != lastWidth || HEIGHT != lastHeight) {
            FontRenderer fr = mc.fontRendererObj;
            UIPosition pos = getPos();
            textField = new GuiTextField(0, fr, pos.x, pos.y, WIDTH, HEIGHT);
            textField.setMaxStringLength(128);
            textField.setFocused(false);
            lastWidth = WIDTH;
            lastHeight = HEIGHT;
        }
    }

    private static int getGuiLeft(GuiContainer gui) {
        try {
            Integer left = ReflectionHelper.getPrivateValue(GuiContainer.class, gui, "guiLeft", "field_147003_i");
            return left == null ? 0 : left;
        } catch (Throwable ignored) { }
        return 0;
    }

    private static int getGuiTop(GuiContainer gui) {
        try {
            Integer top = ReflectionHelper.getPrivateValue(GuiContainer.class, gui, "guiTop", "field_147009_r");
            return top == null ? 0 : top;
        } catch (Throwable ignored) { }
        return 0;
    }

    private boolean isInventoryGui(GuiScreen gui) {
        // Restrict search bar to chest-type containers only
        if (!(gui instanceof GuiContainer)) return false;
        if (gui instanceof GuiChest) return true;
        return mc != null && mc.thePlayer != null && (mc.thePlayer.openContainer instanceof ContainerChest);
    }

    private void recalcHighlights() {
        highlight.clear();
        darken.clear();
        if (mc.thePlayer == null) return;
        if (!(mc.currentScreen instanceof GuiContainer)) return;
        // Only operate on chest containers
        if (!(mc.thePlayer.openContainer instanceof ContainerChest)) return;
        String text = textField != null ? textField.getText() : "";
        if (text == null || text.isEmpty()) return;

        String search = text.replaceAll("[^a-zA-Z0-9&|]", "").toLowerCase();
        if (search.isEmpty()) return;

        String[] orGroups = search.split("\\|\\|");
        List<String[]> groups = new ArrayList<>();
        for (String g : orGroups) groups.add(g.split("&&"));

        Container cont = mc.thePlayer.openContainer;
        if (cont == null) return;
        int size = cont.inventorySlots == null ? 0 : cont.inventorySlots.size();
        for (int i = 0; i < size; i++) {
            Slot s = cont.getSlot(i);
            if (s == null) continue;
            ItemStack stack = s.getStack();
            if (stack == null) { darken.add(i); continue; }
            String name = EnumChatFormatting.getTextWithoutFormattingCodes(stack.getDisplayName());
            if (name == null) name = "";
            name = name.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
            // Build flat lore text
            String loreFlat = "";
            try {
                List<String> lines = stack.getTooltip(mc.thePlayer, false);
                if (lines != null && !lines.isEmpty()) {
                    StringBuilder sb = new StringBuilder();
                    for (String line : lines) {
                        String plain = EnumChatFormatting.getTextWithoutFormattingCodes(line);
                        if (plain != null) sb.append(plain);
                    }
                    loreFlat = sb.toString().replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
                }
            } catch (Throwable ignored) {}

            boolean matched = false;
            for (String[] ands : groups) {
                boolean ok = true;
                for (String term : ands) {
                    if (term.isEmpty()) continue;
                    if (!name.contains(term) && !loreFlat.contains(term)) { ok = false; break; }
                }
                if (ok) { matched = true; break; }
            }
            if (matched) highlight.add(i); else darken.add(i);
        }
    }

    private void recalcCalc() {
        calc = null;
        if (textField == null) return;
        String expr = textField.getText();
        if (expr == null || expr.trim().isEmpty()) return;
        try {
            ScriptEngine eng = new ScriptEngineManager(null).getEngineByName("JavaScript");
            if (eng != null) {
                Object result = eng.eval(expr, new SimpleBindings());
                if (result instanceof Number) {
                    double d = ((Number) result).doubleValue();
                    if (Double.isFinite(d)) {
                        double rounded = Math.round(d * 10000.0) / 10000.0;
                        calc = (Math.floor(rounded) == rounded) ? Long.toString((long) rounded) : Double.toString(rounded);
                    }
                }
            }
        } catch (Throwable ignored) {}
    }

    private void clearSearch() {
        if (textField != null) textField.setText("");
        highlight.clear();
        darken.clear();
        calc = null;
    }

    private static boolean isPrintable(char c) {
        return c >= 32 && c < 127; // basic ASCII printable
    }

    private void disableInventoryKeybind() {
        if (mc == null || mc.gameSettings == null) return;
        if (savedInvKey != null) return;
        KeyBinding inv = mc.gameSettings.keyBindInventory;
        if (inv == null) return;
        try {
            savedInvKey = inv.getKeyCode();
            ReflectionHelper.setPrivateValue(KeyBinding.class, inv, Keyboard.KEY_NONE, "keyCode", "field_151474_i");
            KeyBinding.resetKeyBindingArrayAndHash();
            KeyBinding.setKeyBindState(savedInvKey, false);
        } catch (Throwable ignored) {
            // Fallback: at least clear pressed state
            try { KeyBinding.setKeyBindState(inv.getKeyCode(), false); } catch (Throwable ignore2) {}
        }
    }

    private void restoreInventoryKeybind() {
        if (mc == null || mc.gameSettings == null) return;
        if (savedInvKey == null) return;
        KeyBinding inv = mc.gameSettings.keyBindInventory;
        if (inv == null) { savedInvKey = null; return; }
        try {
            ReflectionHelper.setPrivateValue(KeyBinding.class, inv, savedInvKey, "keyCode", "field_151474_i");
            KeyBinding.resetKeyBindingArrayAndHash();
        } catch (Throwable ignored) { }
        savedInvKey = null;
    }

    private boolean searchFocused = false;
    // Suppress any GUI open/close events for a couple of ticks after handling the inventory key while focused
    private int suppressGuiOpenTicks = 0;

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onMouseInput(GuiScreenEvent.MouseInputEvent.Pre event) {
        if (!isEnabled()) return;
        if (!isInventoryGui(event.gui)) return;
        if (UIHighlighter.isInMoveMode()) return;
        ensureField();

        if (!Mouse.getEventButtonState()) return;
        int button = Mouse.getEventButton();
        if (button != 0) return; // only left click should focus / trigger clear

        ScaledResolution res = new ScaledResolution(mc);
        int mouseX = Mouse.getX() * res.getScaledWidth() / mc.displayWidth;
        int mouseY = res.getScaledHeight() - Mouse.getY() * res.getScaledHeight() / mc.displayHeight - 1;

        if (isOverClearButton(mouseX, mouseY)) {
            if (textField != null && (textField.getText() != null && !textField.getText().isEmpty())) {
                clearSearch();
                event.setCanceled(true);
                return;
            }
        }

        boolean wasFocused = textField != null && textField.isFocused();
        if (textField != null) textField.mouseClicked(mouseX, mouseY, button);
        boolean nowFocused = textField != null && textField.isFocused();
        searchFocused = nowFocused;
        if (!wasFocused && nowFocused) {
            disableInventoryKeybind();
            try { Keyboard.enableRepeatEvents(true); } catch (Throwable ignored) {}
        } else if (wasFocused && !nowFocused) {
            restoreInventoryKeybind();
            suppressGuiOpenTicks = 0; // clear any suppression so next E works normally
            try { KeyBinding.setKeyBindState(getEffectiveInventoryKey(), false); } catch (Throwable ignored) {}
            clearKeyLatch();
            try { Keyboard.enableRepeatEvents(false); } catch (Throwable ignored) {}
        }
        if (nowFocused) {
            recalcHighlights();
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onKeyInput(GuiScreenEvent.KeyboardInputEvent.Pre event) {
        if (!isEnabled()) return;
        if (mc.currentScreen == null) return;
        if (UIHighlighter.isInMoveMode()) return;
        ensureField();

        int key = Keyboard.getEventKey();
        if (key == Keyboard.KEY_NONE || key < 0 || key >= keyLatch.length) return;
        boolean down = Keyboard.getEventKeyState();
        if (!down) { keyLatch[key] = false; return; }
        if (keyLatch[key]) return;
        keyLatch[key] = true;

        char c = Keyboard.getEventCharacter();
        int invKey = getEffectiveInventoryKey();

        // If our field is focused (regardless of the container type), handle keys here and consume them
        if (textField != null && textField.isFocused()) {
            searchFocused = true;
            if (savedInvKey == null) disableInventoryKeybind();

            // ESC: unfocus and consume
            if (key == Keyboard.KEY_ESCAPE) {
                textField.setFocused(false);
                searchFocused = false;
                restoreInventoryKeybind();
                suppressGuiOpenTicks = 0; // don't block next GUI transition
                try { KeyBinding.setKeyBindState(getEffectiveInventoryKey(), false); } catch (Throwable ignored) {}
                clearKeyLatch();
                try { Keyboard.enableRepeatEvents(false); } catch (Throwable ignored) {}
                event.setCanceled(true);
                return;
            }

            // Always clear inventory key pressed state while focused to avoid vanilla toggle
            try { KeyBinding.setKeyBindState(invKey, false); } catch (Throwable ignored) {}

            if (key == invKey) {
                // Type the character (if any) and block opening inventory
                if (c != 0) {
                    try { textField.writeText(Character.toString(c)); } catch (Throwable ignored) {}
                    recalcHighlights();
                    recalcCalc();
                }
                // Suppress any GUI transitions that might be triggered this and next tick
                if (suppressGuiOpenTicks < 2) suppressGuiOpenTicks = 2;
                event.setCanceled(true);
                return;
            }

            try { textField.textboxKeyTyped(c, key); } catch (Throwable ignored) {}
            recalcHighlights();
            recalcCalc();
            // Suppress any GUI transitions briefly on any key while focused
            if (suppressGuiOpenTicks < 1) suppressGuiOpenTicks = 1;
            event.setCanceled(true);
            return;
        }

        // If not focused, only participate on supported inventory GUIs; otherwise, let vanilla handle
        if (!isInventoryGui(mc.currentScreen)) return;

        // Not focused on supported GUI: allow vanilla so inventory key closes UI
    }

    @SubscribeEvent
    public void onInitGui(GuiScreenEvent.InitGuiEvent.Post event) {
        if (!isEnabled()) return;
        if (!isInventoryGui(event.gui)) return;
        ensureField();
        // Recompute when a container GUI opens
        recalcHighlights();
        recalcCalc();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onGuiOpen(GuiOpenEvent event) {
        if (!isEnabled()) return;
        // Block any GUI opening/closing if we just handled the inventory key while focused
        if (suppressGuiOpenTicks > 0) {
            suppressGuiOpenTicks--;
            event.setCanceled(true);
            return;
        }
        // While search is focused, block any GUI transition entirely
        if (searchFocused) {
            event.setCanceled(true);
            return;
        }
        // Reset when changing GUI and ensure search text doesn't persist across UIs
        clearSearch();
        restoreInventoryKeybind();
        try { Keyboard.enableRepeatEvents(false); } catch (Throwable ignored) {}
        textField = null;
        lastWidth = -1;
        lastHeight = -1;
        searchFocused = false;
        suppressGuiOpenTicks = 0;
    }

    @SubscribeEvent
    public void onDrawPost(GuiScreenEvent.DrawScreenEvent.Post event) {
        if (!isEnabled()) return;
        if (!isInventoryGui(event.gui)) return;

        ensureField();
        UIPosition pos = getPos();
        textField.xPosition = pos.x;
        textField.yPosition = pos.y;

        int WIDTH = lastWidth;
        int HEIGHT = lastHeight;

        GuiContainer gui = (GuiContainer) event.gui;
        int guiLeft = getGuiLeft(gui);
        int guiTop = getGuiTop(gui);

        // Draw highlights/darken overlays
        if (!highlight.isEmpty() || !darken.isEmpty()) {
            List<Slot> slots = gui.inventorySlots == null ? null : gui.inventorySlots.inventorySlots;
            if (slots != null) {
                // White border for matched
                for (int idx : highlight) {
                    if (idx < 0 || idx >= slots.size()) continue;
                    Slot s = slots.get(idx);
                    if (s == null) continue;
                    int x = guiLeft + s.xDisplayPosition;
                    int y = guiTop + s.yDisplayPosition;
                    Gui.drawRect(x - 1, y - 1, x + 17, y + 17, 0xFFFFFFFF);
                }
                // Black fill for non-matched
                for (int idx : darken) {
                    if (idx < 0 || idx >= slots.size()) continue;
                    Slot s = slots.get(idx);
                    if (s == null) continue;
                    int x = guiLeft + s.xDisplayPosition;
                    int y = guiTop + s.yDisplayPosition;
                    Gui.drawRect(x, y, x + 16, y + 16, 0xFF000000);
                }
            }
        }

        // Draw search field
        textField.drawTextBox();

        // Draw a small clear button inside the right side of the text box ONLY when there is text
        String tfText = textField.getText();
        boolean showClear = tfText != null && !tfText.isEmpty();
        if (showClear) {
            int cbSize = Math.max(10, HEIGHT - 4);
            int cbX = pos.x + WIDTH - cbSize - 2;
            int cbY = pos.y + (HEIGHT - cbSize) / 2;
            Gui.drawRect(cbX, cbY, cbX + cbSize, cbY + cbSize, 0xAA222222);
            Gui.drawRect(cbX, cbY, cbX + cbSize, cbY + 1, 0x55FFFFFF);
            Gui.drawRect(cbX, cbY, cbX + 1, cbY + cbSize, 0x55FFFFFF);
            Gui.drawRect(cbX + cbSize - 1, cbY, cbX + cbSize, cbY + cbSize, 0x55000000);
            Gui.drawRect(cbX, cbY + cbSize - 1, cbX + cbSize, cbY + cbSize, 0x55000000);
            // Draw an 'x' centered
            String xMark = "x";
            int xw = mc.fontRendererObj.getStringWidth(xMark);
            int xt = cbX + (cbSize - xw) / 2;
            int yt = cbY + (cbSize - mc.fontRendererObj.FONT_HEIGHT) / 2;
            mc.fontRendererObj.drawStringWithShadow(xMark, xt, yt, 0xFFFFFFFF);
        }

        // Draw calc preview to the right
        if (calc != null && !calc.isEmpty()) {
            String preview = "§8" + calc; // DARK_GRAY
            int px = pos.x - mc.fontRendererObj.getStringWidth(preview) + WIDTH - 2;
            int py = pos.y + 4;
            mc.fontRendererObj.drawStringWithShadow(preview, px, py, 0xFFFFFFFF);
        }

        // In move mode, visualize bounds
        if (UIHighlighter.isInMoveMode()) {
            Gui.drawRect(pos.x - 1, pos.y - 1, pos.x + WIDTH + 1, pos.y + HEIGHT + 1, 0x60FFFF00);
        }
    }

    private boolean isOverClearButton(int mouseX, int mouseY) {
        if (textField == null) return false;
        String tfText = textField.getText();
        if (tfText == null || tfText.isEmpty()) return false; // only active when visible
        UIPosition pos = getPos();
        int WIDTH = lastWidth;
        int HEIGHT = lastHeight;
        int cbSize = Math.max(10, HEIGHT - 4);
        int cbX = pos.x + WIDTH - cbSize - 2;
        int cbY = pos.y + (HEIGHT - cbSize) / 2;
        return mouseX >= cbX && mouseX <= cbX + cbSize && mouseY >= cbY && mouseY <= cbY + cbSize;
    }

    @SubscribeEvent
    public void onGuiClosed(GuiOpenEvent event) {
        if (!isEnabled()) return;
        if (event.gui != null) return; // only when closing (next GUI is null)
        // Ensure text is cleared on close so it doesn't persist
        clearSearch();
        restoreInventoryKeybind();
        try { Keyboard.enableRepeatEvents(false); } catch (Throwable ignored) {}
        if (textField != null) textField.setFocused(false);
        searchFocused = false;
        suppressGuiOpenTicks = 0;
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        // Clear inventory key state in both phases while focused to avoid vanilla toggles
        if (!isEnabled()) return;
        if (textField == null || !textField.isFocused()) return;
        try {
            int invKey = getEffectiveInventoryKey();
            KeyBinding.setKeyBindState(invKey, false);
        } catch (Throwable ignored) {}
    }
}
