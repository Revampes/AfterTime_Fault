package com.aftertime.ratallofyou.UI.newui.layout;

import com.aftertime.ratallofyou.UI.newui.util.TextRender;
import com.aftertime.ratallofyou.config.ModConfig;
import com.aftertime.ratallofyou.UI.newui.config.ModConfigIO;
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

    private static class Pos { int x, y; Pos(int x, int y){ this.x=x; this.y=y; } }

    private static class HudItem {
        final String key; // logical key, e.g., "p3ticktimer"
        final String label;
        Pos pos;
        int width;
        int height;
        int anchorX;
        int anchorY;
        boolean selected = false;
        HudItem(String key, String label, Pos pos) { this.key = key; this.label = label; this.pos = pos; }
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
        // Build from ModConfig fields
        items.add(new HudItem("p3ticktimer", "P3 Tick Timer", new Pos(ModConfig.p3ticktimerX, ModConfig.p3ticktimerY)));
        items.add(new HudItem("bonzo", "Bonzo", new Pos(ModConfig.bonzoX, ModConfig.bonzoY)));
        items.add(new HudItem("spirit", "Spirit", new Pos(ModConfig.spiritX, ModConfig.spiritY)));
        items.add(new HudItem("phoenix", "Phoenix", new Pos(ModConfig.phoenixX, ModConfig.phoenixY)));
        items.add(new HudItem("proc", "Proc", new Pos(ModConfig.procX, ModConfig.procY)));
        // New HUD items
        items.add(new HudItem("fluxflare", "Flux/Flare", new Pos(ModConfig.flarefluxX, ModConfig.flarefluxY)));
        items.add(new HudItem("arrowpoison", "Arrow Poison", new Pos(ModConfig.arrowpoisonX, ModConfig.arrowpoisonY)));
        items.add(new HudItem("autofish", "AutoFish Timer", new Pos(ModConfig.autofishTimerX, ModConfig.autofishTimerY)));
        items.add(new HudItem("searchbar", "Search Bar", new Pos(ModConfig.searchbarX, ModConfig.searchbarY)));
        for (HudItem item : items) computeBox(item);
        // Stable order
        Collections.sort(items, new Comparator<HudItem>() { public int compare(HudItem a, HudItem b) { return a.key.compareToIgnoreCase(b.key); }});
    }

    private float getScaleFor(String key) {
        if ("p3ticktimer".equals(key)) return ModConfig.p3ticktimerScale <= 0 ? 1.0f : ModConfig.p3ticktimerScale;
        if ("bonzo".equals(key) || "spirit".equals(key) || "phoenix".equals(key) || "proc".equals(key)) return ModConfig.invincibleScale <= 0 ? 1.0f : ModConfig.invincibleScale;
        if ("fluxflare".equals(key)) return ModConfig.flarefluxScale <= 0 ? 1.0f : ModConfig.flarefluxScale;
        if ("arrowpoison".equals(key)) return ModConfig.arrowpoisonScale <= 0 ? 1.0f : ModConfig.arrowpoisonScale;
        return 1.0f;
    }

    private void computeBox(HudItem it) {
        int fh = TextRender.height(fontRendererObj);
        float s = getScaleFor(it.key);
        int width = 40, height = 16, ax = 0, ay = 0;
        if ("p3ticktimer".equals(it.key)) {
            int w = Math.max(30, Math.round(TextRender.width(fontRendererObj, "00.00") * s));
            width = w; height = Math.max(fh, Math.round(fh * s)); ax = width / 2; ay = 0;
        } else if ("bonzo".equals(it.key)) {
            String sample = "Bonzo: READY";
            int w = Math.max(10, Math.round(TextRender.width(fontRendererObj, sample) * s));
            width = w; height = Math.max(fh, Math.round(fh * s)); ax = 0; ay = 0;
        } else if ("spirit".equals(it.key)) {
            String sample = "Spirit: READY";
            int w = Math.max(10, Math.round(TextRender.width(fontRendererObj, sample) * s));
            width = w; height = Math.max(fh, Math.round(fh * s)); ax = 0; ay = 0;
        } else if ("phoenix".equals(it.key)) {
            String sample = "Phoenix: READY";
            int w = Math.max(10, Math.round(TextRender.width(fontRendererObj, sample) * s));
            width = w; height = Math.max(fh, Math.round(fh * s)); ax = 0; ay = 0;
        } else if ("proc".equals(it.key)) {
            String sample = "Phoenix Procced";
            int w = Math.max(10, Math.round(TextRender.width(fontRendererObj, sample) * s));
            width = w; height = Math.max(fh, Math.round(fh * s)); ax = 0; ay = 0;
        } else if ("fluxflare".equals(it.key)) {
            String sample = "Plasmaflux 12s";
            int w = Math.max(10, Math.round(TextRender.width(fontRendererObj, sample) * s));
            width = w; height = Math.max(fh, Math.round(fh * s)); ax = 0; ay = 0;
        } else if ("arrowpoison".equals(it.key)) {
            String sample = "Toxic: 64";
            int w = Math.max(60, Math.round(TextRender.width(fontRendererObj, sample) * s));
            width = w; height = Math.max(fh * 2 + 4, Math.round(fh * s) * 2 + 4); ax = 0; ay = 0;
        } else if ("autofish".equals(it.key)) {
            String sample = "Hook: 3.2s";
            int w = Math.max(40, Math.round(TextRender.width(fontRendererObj, sample) * s));
            width = w; height = Math.max(fh, Math.round(fh * s)); ax = 0; ay = 0;
        } else if ("searchbar".equals(it.key)) {
            int w = Math.max(50, ModConfig.searchbarWidth);
            int h = Math.max(12, ModConfig.searchbarHeight);
            width = w; height = h; ax = 0; ay = 0;
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
            ScaledResolution sr = new ScaledResolution(mc);
            int nx = mouseX - dragOffsetX + dragging.anchorX;
            int ny = mouseY - dragOffsetY + dragging.anchorY;
            int snap = 6;
            if (Math.abs(nx - sr.getScaledWidth() / 2) <= snap) nx = sr.getScaledWidth() / 2;
            if (Math.abs(ny - sr.getScaledHeight() / 2) <= snap) ny = sr.getScaledHeight() / 2;
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
            if ("p3ticktimer".equals(it.key)) { ModConfig.p3ticktimerX = it.pos.x; ModConfig.p3ticktimerY = it.pos.y; }
            else if ("bonzo".equals(it.key)) { ModConfig.bonzoX = it.pos.x; ModConfig.bonzoY = it.pos.y; }
            else if ("spirit".equals(it.key)) { ModConfig.spiritX = it.pos.x; ModConfig.spiritY = it.pos.y; }
            else if ("phoenix".equals(it.key)) { ModConfig.phoenixX = it.pos.x; ModConfig.phoenixY = it.pos.y; }
            else if ("proc".equals(it.key)) { ModConfig.procX = it.pos.x; ModConfig.procY = it.pos.y; }
            else if ("fluxflare".equals(it.key)) { ModConfig.flarefluxX = it.pos.x; ModConfig.flarefluxY = it.pos.y; }
            else if ("arrowpoison".equals(it.key)) { ModConfig.arrowpoisonX = it.pos.x; ModConfig.arrowpoisonY = it.pos.y; }
            else if ("autofish".equals(it.key)) { ModConfig.autofishTimerX = it.pos.x; ModConfig.autofishTimerY = it.pos.y; }
            else if ("searchbar".equals(it.key)) { ModConfig.searchbarX = it.pos.x; ModConfig.searchbarY = it.pos.y; }
        }
        // Persist to disk via new config system
        ModConfigIO.save();
    }

    private void resetPositions() {
        ScaledResolution sr = new ScaledResolution(mc);
        int cx = sr.getScaledWidth() / 2;
        int cy = sr.getScaledHeight() / 2;
        for (HudItem it : items) {
            if ("p3ticktimer".equals(it.key)) { it.pos = new Pos(cx, cy); }
            else if ("bonzo".equals(it.key)) { it.pos = new Pos(20, 20); }
            else if ("spirit".equals(it.key)) { it.pos = new Pos(20, 36); }
            else if ("phoenix".equals(it.key)) { it.pos = new Pos(20, 52); }
            else if ("proc".equals(it.key)) { it.pos = new Pos(20, 68); }
            else if ("fluxflare".equals(it.key)) { it.pos = new Pos(220, 220); }
            else if ("arrowpoison".equals(it.key)) { it.pos = new Pos(200, 200); }
            else if ("autofish".equals(it.key)) { it.pos = new Pos(5, 5); }
            else if ("searchbar".equals(it.key)) { it.pos = new Pos(200, 200); }
            computeBox(it);
        }
    }
}
