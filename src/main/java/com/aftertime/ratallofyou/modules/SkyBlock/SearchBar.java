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
import net.minecraft.inventory.Container;
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
        return gui instanceof GuiContainer;
    }

    private void recalcHighlights() {
        highlight.clear();
        darken.clear();
        if (mc.thePlayer == null) return;
        if (!(mc.currentScreen instanceof GuiContainer)) return;
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

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        if (!isEnabled()) return;
        // Reset when changing GUI
        highlight.clear();
        darken.clear();
        calc = null;
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
        // Sync to config position each frame (so Move Mode changes reflect immediately)
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

    @SubscribeEvent
    public void onMouseInput(GuiScreenEvent.MouseInputEvent.Pre event) {
        if (!isEnabled()) return;
        if (!isInventoryGui(event.gui)) return;
        if (UIHighlighter.isInMoveMode()) return; // ignore interactions during move mode
        ensureField();

        // Only respond on actual press events
        if (!Mouse.getEventButtonState()) return;
        int button = Mouse.getEventButton();
        if (button < 0) return;

        // Compute scaled mouse coords
        ScaledResolution res = new ScaledResolution(mc);
        int mouseX = Mouse.getX() * res.getScaledWidth() / mc.displayWidth;
        int mouseY = res.getScaledHeight() - Mouse.getY() * res.getScaledHeight() / mc.displayHeight - 1;

        textField.mouseClicked(mouseX, mouseY, button);
        // If clicked inside, keep focus; otherwise, if click elsewhere in GUI, allow normal handling
        if (textField.isFocused()) {
            // Recompute highlights shortly after click
            recalcHighlights();
        }
    }

    @SubscribeEvent
    public void onKeyInput(GuiScreenEvent.KeyboardInputEvent.Pre event) {
        if (!isEnabled()) return;
        if (mc.currentScreen == null) return;
        if (!isInventoryGui(mc.currentScreen)) return;
        if (UIHighlighter.isInMoveMode()) return; // ignore while moving UI
        ensureField();

        int key = Keyboard.getEventKey();
        if (key == Keyboard.KEY_NONE || key < 0 || key >= keyLatch.length) return;
        boolean down = Keyboard.getEventKeyState();

        // On key release, clear latch and exit
        if (!down) { keyLatch[key] = false; return; }

        // Only handle the first press, ignore duplicates within the same physical press
        if (keyLatch[key]) return;
        keyLatch[key] = true;

        if (!textField.isFocused()) return;
        char c = Keyboard.getEventCharacter();

        try {
            textField.textboxKeyTyped(c, key);
        } catch (Throwable ignored) {}

        // Update highlights and calc whenever text changes or keys are typed while focused
        recalcHighlights();
        recalcCalc();

        // Cancel further processing except ESC to allow unfocus
        if (key != Keyboard.KEY_ESCAPE) {
            event.setCanceled(true);
        } else {
            textField.setFocused(false);
        }
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
        if (textField != null) textField.setFocused(false);
        highlight.clear();
        darken.clear();
        calc = null;
    }
}
