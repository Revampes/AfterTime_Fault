package com.aftertime.ratallofyou.UI.newui.layout;

import com.aftertime.ratallofyou.UI.config.ConfigData.AllConfig;
import com.aftertime.ratallofyou.UI.config.ConfigData.BaseConfig;
import com.aftertime.ratallofyou.UI.config.ConfigData.UIPosition;
import com.aftertime.ratallofyou.UI.config.ConfigIO;
import com.aftertime.ratallofyou.UI.newui.util.TextRender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.*;

public class NewUILayout extends GuiScreen {
    private static GuiScreen previous;

    private static class HudItem {
        final String key; // pos key, e.g., "p3ticktimer_pos"
        final String label;
        UIPosition pos;
        int width;
        int height;
        int anchorX;
        int anchorY;
        boolean selected = false;

        HudItem(String key, String label, UIPosition pos) {
            this.key = key; this.label = label; this.pos = pos;
        }
    }

    private final List<HudItem> items = new ArrayList<HudItem>();
    private HudItem dragging = null;
    private int dragOffsetX = 0, dragOffsetY = 0;

    public static void open() {
        Minecraft mc = Minecraft.getMinecraft();
        previous = mc.currentScreen;
        mc.displayGuiScreen(new NewUILayout());
    }

    @Override
    public void initGui() {
        super.initGui();
        buttonList.clear();
        int bw = 80, bh = 20; int padding = 6;
        buttonList.add(new GuiButton(1, padding, padding, bw, bh, "Save"));
        buttonList.add(new GuiButton(2, padding + bw + padding, padding, bw, bh, "Cancel"));
        buttonList.add(new GuiButton(3, padding + (bw + padding) * 2, padding, bw, bh, "Reset"));
        loadItems();
    }

    private void loadItems() {
        items.clear();
        for (Map.Entry<String, BaseConfig<?>> en : AllConfig.INSTANCE.Pos_CONFIGS.entrySet()) {
            BaseConfig<?> base = en.getValue();
            if (!(base.Data instanceof UIPosition)) continue;
            String key = en.getKey();
            UIPosition p = (UIPosition) base.Data;
            String label = base.name != null ? base.name : key;
            HudItem item = new HudItem(key, label, p);
            computeBox(item);
            items.add(item);
        }
        // Stable order
        Collections.sort(items, new Comparator<HudItem>() { public int compare(HudItem a, HudItem b) { return a.key.compareToIgnoreCase(b.key); }});
    }

    private float getScaleFor(String key) {
        if ("p3ticktimer_pos".equals(key)) return getScaledFloat("p3ticktimer_scale");
        if ("bonzo_pos".equals(key) || "spirit_pos".equals(key) || "phoenix_pos".equals(key) || "proc_pos".equals(key)) return getScaledFloat("invincible_scale");
        if ("arrowpoison_pos".equals(key)) return getScaledFloat("arrowpoison_scale");
        if ("flareflux_pos".equals(key)) return getScaledFloat("flareflux_scale");
        return 1.0f;
    }

    private int getInt(String key, int def) {
        BaseConfig<?> c = AllConfig.INSTANCE.Pos_CONFIGS.get(key);
        if (c == null || c.Data == null) return def;
        Object o = c.Data;
        if (o instanceof Integer) return (Integer) o;
        if (o instanceof Float) return Math.round((Float) o);
        if (o instanceof Double) return (int) Math.round((Double) o);
        return def;
    }

    private float getScaledFloat(String key) {
        BaseConfig<?> c = AllConfig.INSTANCE.Pos_CONFIGS.get(key);
        if (c == null || c.Data == null) return 1.0f;
        Object o = c.Data;
        if (o instanceof Float) return (Float) o;
        if (o instanceof Double) return ((Double) o).floatValue();
        if (o instanceof Integer) return ((Integer) o).floatValue();
        return 1.0f;
    }

    private void computeBox(HudItem it) {
        // Compute box similar to renderer metrics, but independent of old UI
        int fh = TextRender.height(fontRendererObj);
        float s = getScaleFor(it.key);
        int width = 40, height = 16, ax = 0, ay = 0;
        if ("searchbar_pos".equals(it.key)) {
            int w = getInt("searchbar_width", 192);
            int h = getInt("searchbar_height", 16);
            width = w; height = h; ax = 0; ay = 0;
        } else if ("p3ticktimer_pos".equals(it.key)) {
            int w = Math.max(30, Math.round(TextRender.width(fontRendererObj, "00.00") * s));
            width = w; height = Math.max(fh, Math.round(fh * s)); ax = width / 2;
        } else if ("bonzo_pos".equals(it.key) || "spirit_pos".equals(it.key) || "phoenix_pos".equals(it.key) || "proc_pos".equals(it.key)) {
            String sample = "Bonzo: READY";
            int w = Math.max(10, Math.round(TextRender.width(fontRendererObj, sample) * s));
            width = w; height = Math.max(fh, Math.round(fh * s)); ax = 0;
        } else if ("arrowpoison_pos".equals(it.key)) {
            int textMax = Math.max(TextRender.width(fontRendererObj, "Twilight: 000000"), TextRender.width(fontRendererObj, "Toxic: 000000"));
            int baseW = 16 + 4 + textMax;
            int baseH = 34;
            width = Math.max(24, Math.round(baseW * s)); height = Math.max(20, Math.round(baseH * s));
        } else if ("flareflux_pos".equals(it.key)) {
            int baseW = TextRender.width(fontRendererObj, "Flux/Flare") + 12;
            int baseH = fh + 4; width = Math.max(30, Math.round(baseW * s)); height = Math.max(fh, Math.round(baseH * s));
        } else {
            // Generic fallback
            width = Math.max(40, Math.round(80 * s)); height = Math.max(16, Math.round(16 * s));
        }
        it.width = width; it.height = height; it.anchorX = ax; it.anchorY = ay;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        ScaledResolution sr = new ScaledResolution(mc);
        // Dim overlay
        drawRect(0, 0, sr.getScaledWidth(), sr.getScaledHeight(), 0x80000000);

        // Center guides
        int cx = sr.getScaledWidth() / 2, cy = sr.getScaledHeight() / 2;
        drawRect(cx - 1, 0, cx + 1, sr.getScaledHeight(), 0x40FFFFFF);
        drawRect(0, cy - 1, sr.getScaledWidth(), cy + 1, 0x40FFFFFF);

        // Title bar area
        super.drawScreen(mouseX, mouseY, partialTicks);

        // Draw items
        for (HudItem it : items) {
            int left = it.pos.x - it.anchorX;
            int top = it.pos.y - it.anchorY;
            int right = left + it.width; int bottom = top + it.height;
            boolean hovered = mouseX >= left && mouseX <= right && mouseY >= top && mouseY <= bottom;
            int outline = it.selected ? 0xC0FFAA00 : (hovered ? 0xA0AAAAFF : 0x60FFFFFF);
            Gui.drawRect(left - 2, top - 2, right + 2, bottom + 2, outline);
            TextRender.drawWithShadow(fontRendererObj, it.label, left, Math.max(0, top - TextRender.height(fontRendererObj) - 2), 0xFFFFFF);
            // sample marker
            TextRender.draw(fontRendererObj, "[]", left + 2, top + 2, 0xAAAAAA);
        }

        // Help text
        TextRender.drawWithShadow(fontRendererObj, "Drag elements. Save to apply.", 6, 6 + 22, 0xFFFFFF);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (mouseButton != 0) return;
        // Select / start dragging
        HudItem hit = hitTest(mouseX, mouseY);
        if (hit != null) {
            for (HudItem it : items) it.selected = false;
            hit.selected = true;
            dragging = hit;
            int left = hit.pos.x - hit.anchorX;
            int top = hit.pos.y - hit.anchorY;
            dragOffsetX = mouseX - left;
            dragOffsetY = mouseY - top;
        }
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        if (dragging != null && clickedMouseButton == 0) {
            // Update position with snap to center lines
            ScaledResolution sr = new ScaledResolution(mc);
            int nx = mouseX - dragOffsetX + dragging.anchorX;
            int ny = mouseY - dragOffsetY + dragging.anchorY;
            int snap = 6;
            if (Math.abs(nx - sr.getScaledWidth() / 2) <= snap) nx = sr.getScaledWidth() / 2;
            if (Math.abs(ny - sr.getScaledHeight() / 2) <= snap) ny = sr.getScaledHeight() / 2;
            // clamp
            nx = Math.max(0, Math.min(nx, sr.getScaledWidth()));
            ny = Math.max(0, Math.min(ny, sr.getScaledHeight()));
            dragging.pos.x = nx;
            dragging.pos.y = ny;
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        dragging = null;
    }

    private HudItem hitTest(int mx, int my) {
        for (HudItem it : items) {
            int left = it.pos.x - it.anchorX;
            int top = it.pos.y - it.anchorY;
            int right = left + it.width; int bottom = top + it.height;
            if (mx >= left && mx <= right && my >= top && my <= bottom) return it;
        }
        return null;
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 1) { // Save
            savePositions();
            mc.displayGuiScreen(previous);
        } else if (button.id == 2) { // Cancel
            mc.displayGuiScreen(previous);
        } else if (button.id == 3) { // Reset
            resetPositions();
        }
    }

    private void savePositions() {
        for (HudItem it : items) {
            BaseConfig<?> base = AllConfig.INSTANCE.Pos_CONFIGS.get(it.key);
            if (base != null) {
                @SuppressWarnings("unchecked")
                BaseConfig<Object> cfgObj = (BaseConfig<Object>) base;
                cfgObj.Data = it.pos;
            }
        }
        // Persist to disk
        ConfigIO.INSTANCE.SaveProperties();
    }

    private void resetPositions() {
        // Build a fresh AllConfig to read original default UIPosition values
        AllConfig defaults = new AllConfig();
        for (HudItem it : items) {
            BaseConfig<?> defBase = defaults.Pos_CONFIGS.get(it.key);
            if (defBase != null && defBase.Data instanceof UIPosition) {
                UIPosition defPos = (UIPosition) defBase.Data;
                it.pos = new UIPosition(defPos.x, defPos.y);
                computeBox(it);
            }
        }
    }
}
