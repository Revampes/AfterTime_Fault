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
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
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

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        if (!isEnabled()) return;
        // Reset when changing GUI and ensure search text doesn't persist across UIs
        clearSearch();
        // Create field lazily when first draw happens
        textField = null;
        lastWidth = -1;
        lastHeight = -1;
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

        if (textField != null) textField.mouseClicked(mouseX, mouseY, button);
        if (textField != null && textField.isFocused()) {
            recalcHighlights();
        }
    }

    @SubscribeEvent
    public void onKeyInput(GuiScreenEvent.KeyboardInputEvent.Pre event) {
        if (!isEnabled()) return;
        if (mc.currentScreen == null) return;
        if (!isInventoryGui(mc.currentScreen)) return;
        if (UIHighlighter.isInMoveMode()) return;
        ensureField();

        int key = Keyboard.getEventKey();
        if (key == Keyboard.KEY_NONE || key < 0 || key >= keyLatch.length) return;
        boolean down = Keyboard.getEventKeyState();
        if (!down) { keyLatch[key] = false; return; }
        if (keyLatch[key]) return;
        keyLatch[key] = true;

        char c = Keyboard.getEventCharacter();

        // ESC: if focused, unfocus and consume; otherwise, let vanilla handle (may close GUI)
        if (key == Keyboard.KEY_ESCAPE) {
            if (textField != null && textField.isFocused()) {
                textField.setFocused(false);
                event.setCanceled(true);
            }
            return;
        }

        // Only handle keys when the search bar is focused (no auto-focus on typing)
        if (textField == null || !textField.isFocused()) return;

        try {
            textField.textboxKeyTyped(c, key);
        } catch (Throwable ignored) {}

        recalcHighlights();
        recalcCalc();

        // Consume keys while focused so inventory key (e.g., 'E') doesn't close the GUI
        event.setCanceled(true);
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

    @SubscribeEvent
    public void onGuiClosed(GuiOpenEvent event) {
        if (!isEnabled()) return;
        if (event.gui != null) return; // only when closing (next GUI is null)
        // Ensure text is cleared on close so it doesn't persist
        clearSearch();
        if (textField != null) textField.setFocused(false);
    }
}
