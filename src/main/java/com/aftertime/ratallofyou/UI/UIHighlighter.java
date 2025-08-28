package com.aftertime.ratallofyou.UI;

import com.aftertime.ratallofyou.UI.config.ConfigData.AllConfig;
import com.aftertime.ratallofyou.UI.config.ConfigData.BaseConfig;
import com.aftertime.ratallofyou.UI.config.ConfigData.UIPosition;
import com.aftertime.ratallofyou.UI.config.ConfigIO;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import java.util.Map;
import java.io.IOException;

public class UIHighlighter {
    private static Boolean isInMoveMode = false;
    private static GuiScreen previousScreen = null;

    private static void renderOverlay(Minecraft mc, ScaledResolution res, int mouseX, int mouseY) {
        // Draw dim background
        drawRect(0, 0, res.getScaledWidth(), res.getScaledHeight(), 0x80000000);
        mc.fontRendererObj.drawStringWithShadow("Drag UI elements - ESC to exit", 10, 10, 0xFFFFFF);

        // Center crosshair
        int cx = res.getScaledWidth() / 2;
        int cy = res.getScaledHeight() / 2;
        int crossLen = 8;
        int crossThick = 1;
        int crossColor = 0x80FFFFFF;
        drawRect(cx - crossLen, cy - crossThick, cx + crossLen + 1, cy + crossThick + 1, crossColor);
        drawRect(cx - crossThick, cy - crossLen, cx + crossThick + 1, cy + crossLen + 1, crossColor);

        // Draw boxes
        for (Map.Entry<String, BaseConfig<?>> entry : AllConfig.INSTANCE.Pos_CONFIGS.entrySet()) {
            if (!(entry.getValue().Data instanceof UIPosition)) continue;
            String key = entry.getKey();
            int[] box = getElementBoxAndAnchor(key);
            int width = box[0];
            int height = box[1];
            int ax = box[2];
            int ay = box[3];
            UIPosition pos = (UIPosition) entry.getValue().Data;
            if (pos == null) continue;

            int left = pos.x - ax;
            int top = pos.y - ay;
            int right = left + width;
            int bottom = top + height;

            boolean hovered = mouseX >= left && mouseX <= right && mouseY >= top && mouseY <= bottom;
            int outlineColor = UIDragger.getInstance().isDragging() && UIDragger.getInstance().getDraggedElement() == pos ? 0x80FF0000 : (hovered ? 0x80FFA500 : 0x80FFFF00);
            drawRect(left - 2, top - 2, right + 2, bottom + 2, outlineColor);

            String label = entry.getValue().name != null ? entry.getValue().name : key;
            mc.fontRendererObj.drawStringWithShadow(label, left, Math.max(0, top - mc.fontRendererObj.FONT_HEIGHT - 2), 0xFFFFFF);

            // Sample text
            String sample = null;
            if ("searchbar_pos".equals(key)) sample = "Search...";
            else if ("bonzo_pos".equals(key)) sample = "Bonzo: READY";
            else if ("spirit_pos".equals(key)) sample = "Spirit: READY";
            else if ("phoenix_pos".equals(key)) sample = "Phoenix: READY";
            else if ("proc_pos".equals(key)) sample = "Phoenix Procced";
            else if ("p3ticktimer_pos".equals(key)) sample = "00.00";
            else if ("arrowpoison_pos".equals(key)) sample = "Twilight: 1356\nToxic: 134";
            else if ("flareflux_pos".equals(key)) sample = "Flux/Flare";
            if (sample != null) {
                int color = 0xFFAAAAAA;
                if ("p3ticktimer_pos".equals(key)) {
                    int w = mc.fontRendererObj.getStringWidth(sample);
                    mc.fontRendererObj.drawStringWithShadow(sample, pos.x - w / 2, top, color);
                } else if ("arrowpoison_pos".equals(key)) {
                    String[] lines = sample.split("\\n");
                    int y = top;
                    for (String s : lines) {
                        mc.fontRendererObj.drawStringWithShadow(s, left, y, color);
                        y += mc.fontRendererObj.FONT_HEIGHT + 1;
                    }
                } else {
                    mc.fontRendererObj.drawStringWithShadow(sample, left, top, color);
                }
            }

            // Dragging
            if (Mouse.isButtonDown(0)) {
                if (hovered && !UIDragger.getInstance().isDragging()) {
                    UIDragger.getInstance().tryStartDrag(pos, mouseX, mouseY, width, height, ax, ay);
                }
                if (UIDragger.getInstance().isDragging()) UIDragger.getInstance().updateDragPosition(mouseX, mouseY);
            } else if (UIDragger.getInstance().isDragging()) {
                UIDragger.getInstance().updatePositions();
            }

            // Tooltip
            if (hovered) {
                String tip = "searchbar_pos".equals(key) ? "Scroll: width, Shift+Scroll: height" : "Scroll: scale";
                mc.fontRendererObj.drawStringWithShadow(tip, left, bottom + 4, 0xFFFFFF);
            }
        }
    }

    public static boolean isInMoveMode() { return isInMoveMode; }

    public static void enterMoveMode(GuiScreen currentScreen) {
        isInMoveMode = true;
        previousScreen = currentScreen;
        Minecraft mc = Minecraft.getMinecraft();
        mc.gameSettings.hideGUI = false;
        mc.inGameHasFocus = false;
        Mouse.setGrabbed(false);
        // Open dedicated screen to capture input and unfocus game
        mc.displayGuiScreen(new MoveModeScreen());
    }

    public static void exitMoveMode() {
        isInMoveMode = false;
        Minecraft mc = Minecraft.getMinecraft();
        // Persist updated UI positions
        ConfigIO.INSTANCE.SaveProperties();
        // Restore previous screen
        mc.displayGuiScreen(previousScreen);
    }

    private static int[] getElementBoxAndAnchor(String key) {
        Minecraft mc = Minecraft.getMinecraft();
        int fh = mc.fontRendererObj.FONT_HEIGHT;
        int width = 40, height = 50, ax = 0, ay = 0;
        if ("searchbar_pos".equals(key)) {
            Object w = AllConfig.INSTANCE.Pos_CONFIGS.get("searchbar_width").Data;
            Object h = AllConfig.INSTANCE.Pos_CONFIGS.get("searchbar_height").Data;
            width = (w instanceof Integer) ? (Integer) w : 192;
            height = (h instanceof Integer) ? (Integer) h : 16;
        } else if ("bonzo_pos".equals(key) || "spirit_pos".equals(key) || "phoenix_pos".equals(key) || "proc_pos".equals(key)) {
            float s = 1.0f;
            Object sc = AllConfig.INSTANCE.Pos_CONFIGS.get("invincible_scale").Data;
            if (sc instanceof Float) s = (Float) sc; else if (sc instanceof Double) s = ((Double) sc).floatValue();
            String sample = "bonzo_pos".equals(key) ? "Bonzo: READY" : ("spirit_pos".equals(key) ? "Spirit: READY" : ("phoenix_pos".equals(key) ? "Phoenix: READY" : "Phoenix Procced"));
            int w = mc.fontRendererObj.getStringWidth(sample);
            width = Math.max(10, Math.round(w * s));
            height = Math.max(fh, Math.round(fh * s));
        } else if ("p3ticktimer_pos".equals(key)) {
            float s = 1.0f;
            Object sc = AllConfig.INSTANCE.Pos_CONFIGS.get("p3ticktimer_scale").Data;
            if (sc instanceof Float) s = (Float) sc; else if (sc instanceof Double) s = ((Double) sc).floatValue();
            int w = mc.fontRendererObj.getStringWidth("00.00");
            width = Math.max(30, Math.round(w * s));
            height = Math.max(fh, Math.round(fh * s));
            ax = width / 2;
        } else if ("arrowpoison_pos".equals(key)) {
            float s = 1.0f;
            Object sc = AllConfig.INSTANCE.Pos_CONFIGS.get("arrowpoison_scale").Data;
            if (sc instanceof Float) s = (Float) sc; else if (sc instanceof Double) s = ((Double) sc).floatValue();
            // Include label widths so the drag box matches drawn text
            int w1 = mc.fontRendererObj.getStringWidth("Twilight: 000000");
            int w2 = mc.fontRendererObj.getStringWidth("Toxic: 000000");
            int textMax = Math.max(w1, w2);
            int baseW = 16 + 4 + textMax; // icon + padding + text
            int baseH = 34; // two rows with stride
            width = Math.max(24, Math.round(baseW * s));
            height = Math.max(20, Math.round(baseH * s));
        } else if ("flareflux_pos".equals(key)) {
            float s = 1.0f;
            Object sc = AllConfig.INSTANCE.Pos_CONFIGS.get("flareflux_scale").Data;
            if (sc instanceof Float) s = (Float) sc; else if (sc instanceof Double) s = ((Double) sc).floatValue();
            int baseW = mc.fontRendererObj.getStringWidth("Flux/Flare") + 12;
            int baseH = fh + 4;
            width = Math.max(30, Math.round(baseW * s));
            height = Math.max(fh, Math.round(baseH * s));
        }
        return new int[]{width, height, ax, ay};
    }

    @SubscribeEvent
    public void onRender(RenderGameOverlayEvent.Post event) {
        if (!isInMoveMode || event.type != RenderGameOverlayEvent.ElementType.ALL) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.currentScreen != null) return; // screen draws overlay in move mode
        ScaledResolution res = new ScaledResolution(mc);
        int mouseX = Mouse.getX() * res.getScaledWidth() / mc.displayWidth;
        int mouseY = res.getScaledHeight() - Mouse.getY() * res.getScaledHeight() / mc.displayHeight - 1;
        renderOverlay(mc, res, mouseX, mouseY);
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END && isInMoveMode) {
            if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) { exitMoveMode(); return; }
            Minecraft mc = Minecraft.getMinecraft();
            ScaledResolution res = new ScaledResolution(mc);
            int mouseX = Mouse.getX() * res.getScaledWidth() / mc.displayWidth;
            int mouseY = res.getScaledHeight() - Mouse.getY() * res.getScaledHeight() / mc.displayHeight - 1;
            int wheel = Mouse.getDWheel();
            if (wheel != 0) {
                String hoveredKey = null;
                for (Map.Entry<String, BaseConfig<?>> entry : AllConfig.INSTANCE.Pos_CONFIGS.entrySet()) {
                    if (!(entry.getValue().Data instanceof UIPosition)) continue;
                    String key = entry.getKey();
                    int[] box = getElementBoxAndAnchor(key);
                    int width = box[0]; int height = box[1]; int ax = box[2]; int ay = box[3];
                    UIPosition p = (UIPosition) entry.getValue().Data;
                    if (p == null) continue;
                    int left = p.x - ax; int top = p.y - ay;
                    int right = left + width; int bottom = top + height;
                    if (mouseX >= left && mouseX <= right && mouseY >= top && mouseY <= bottom) { hoveredKey = key; break; }
                }
                if (hoveredKey != null) {
                    int dir = wheel > 0 ? 1 : -1;
                    if ("searchbar_pos".equals(hoveredKey)) {
                        @SuppressWarnings("unchecked") BaseConfig<Integer> wCfg = (BaseConfig<Integer>) AllConfig.INSTANCE.Pos_CONFIGS.get("searchbar_width");
                        @SuppressWarnings("unchecked") BaseConfig<Integer> hCfg = (BaseConfig<Integer>) AllConfig.INSTANCE.Pos_CONFIGS.get("searchbar_height");
                        int wVal = (wCfg != null && wCfg.Data != null) ? wCfg.Data : 192;
                        int hVal = (hCfg != null && hCfg.Data != null) ? hCfg.Data : 16;
                        boolean heightMode = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
                        if (heightMode) { hVal = Math.max(12, Math.min(hVal + dir * 2, 28)); if (hCfg != null) hCfg.Data = hVal; }
                        else { wVal = Math.max(96, Math.min(wVal + dir * 8, 400)); if (wCfg != null) wCfg.Data = wVal; }
                    } else if ("p3ticktimer_pos".equals(hoveredKey)) {
                        @SuppressWarnings("unchecked") BaseConfig<Float> sCfg = (BaseConfig<Float>) AllConfig.INSTANCE.Pos_CONFIGS.get("p3ticktimer_scale");
                        float s = (sCfg != null && sCfg.Data != null) ? sCfg.Data : 1.0f;
                        s = Math.max(0.5f, Math.min(s + dir * 0.05f, 3.0f));
                        if (sCfg != null) sCfg.Data = s;
                    } else if ("bonzo_pos".equals(hoveredKey) || "spirit_pos".equals(hoveredKey) || "phoenix_pos".equals(hoveredKey) || "proc_pos".equals(hoveredKey)) {
                        @SuppressWarnings("unchecked") BaseConfig<Float> sCfg = (BaseConfig<Float>) AllConfig.INSTANCE.Pos_CONFIGS.get("invincible_scale");
                        float s = (sCfg != null && sCfg.Data != null) ? sCfg.Data : 1.0f;
                        s = Math.max(0.5f, Math.min(s + dir * 0.05f, 3.0f));
                        if (sCfg != null) sCfg.Data = s;
                    } else if ("arrowpoison_pos".equals(hoveredKey)) {
                        @SuppressWarnings("unchecked") BaseConfig<Float> sCfg = (BaseConfig<Float>) AllConfig.INSTANCE.Pos_CONFIGS.get("arrowpoison_scale");
                        float s = (sCfg != null && sCfg.Data != null) ? sCfg.Data : 1.0f;
                        s = Math.max(0.5f, Math.min(s + dir * 0.05f, 3.0f));
                        if (sCfg != null) sCfg.Data = s;
                    } else if ("flareflux_pos".equals(hoveredKey)) {
                        @SuppressWarnings("unchecked") BaseConfig<Float> sCfg = (BaseConfig<Float>) AllConfig.INSTANCE.Pos_CONFIGS.get("flareflux_scale");
                        float s = (sCfg != null && sCfg.Data != null) ? sCfg.Data : 1.0f;
                        s = Math.max(0.5f, Math.min(s + dir * 0.05f, 3.0f));
                        if (sCfg != null) sCfg.Data = s;
                    }
                }
            }
        }
    }

    private static void drawRect(int left, int top, int right, int bottom, int color) {
        net.minecraft.client.gui.Gui.drawRect(left, top, right, bottom, color);
    }

    // Lightweight screen to capture focus and delegate rendering to overlay
    public static class MoveModeScreen extends GuiScreen {
        @Override public boolean doesGuiPauseGame() { return false; }
        @Override protected void keyTyped(char typedChar, int keyCode) {
            if (keyCode == Keyboard.KEY_ESCAPE) { UIHighlighter.exitMoveMode(); }
        }
        @Override public void drawScreen(int mouseX, int mouseY, float partialTicks) {
            Minecraft mc = Minecraft.getMinecraft();
            ScaledResolution res = new ScaledResolution(mc);
            renderOverlay(mc, res, mouseX, mouseY);
        }
        @Override
        public void handleMouseInput() throws IOException {
            super.handleMouseInput();
            int dWheel = Mouse.getEventDWheel();
            if (dWheel == 0) return;
            Minecraft mc = Minecraft.getMinecraft();
            ScaledResolution res = new ScaledResolution(mc);
            int mouseX = Mouse.getX() * res.getScaledWidth() / mc.displayWidth;
            int mouseY = res.getScaledHeight() - Mouse.getY() * res.getScaledHeight() / mc.displayHeight - 1;
            String hoveredKey = null;
            for (Map.Entry<String, BaseConfig<?>> entry : AllConfig.INSTANCE.Pos_CONFIGS.entrySet()) {
                if (!(entry.getValue().Data instanceof UIPosition)) continue;
                String key = entry.getKey();
                int[] box = getElementBoxAndAnchor(key);
                int width = box[0]; int height = box[1]; int ax = box[2]; int ay = box[3];
                UIPosition p = (UIPosition) entry.getValue().Data;
                if (p == null) continue;
                int left = p.x - ax; int top = p.y - ay;
                int right = left + width; int bottom = top + height;
                if (mouseX >= left && mouseX <= right && mouseY >= top && mouseY <= bottom) { hoveredKey = key; break; }
            }
            if (hoveredKey == null) return;
            int dir = dWheel > 0 ? 1 : -1;
            if ("searchbar_pos".equals(hoveredKey)) {
                @SuppressWarnings("unchecked") BaseConfig<Integer> wCfg = (BaseConfig<Integer>) AllConfig.INSTANCE.Pos_CONFIGS.get("searchbar_width");
                @SuppressWarnings("unchecked") BaseConfig<Integer> hCfg = (BaseConfig<Integer>) AllConfig.INSTANCE.Pos_CONFIGS.get("searchbar_height");
                int wVal = (wCfg != null && wCfg.Data != null) ? wCfg.Data : 192;
                int hVal = (hCfg != null && hCfg.Data != null) ? hCfg.Data : 16;
                boolean heightMode = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
                if (heightMode) {
                    hVal = Math.max(12, Math.min(hVal + dir * 2, 28));
                    if (hCfg != null) hCfg.Data = hVal;
                } else {
                    wVal = Math.max(96, Math.min(wVal + dir * 8, 400));
                    if (wCfg != null) wCfg.Data = wVal;
                }
            } else if ("p3ticktimer_pos".equals(hoveredKey)) {
                @SuppressWarnings("unchecked") BaseConfig<Float> sCfg = (BaseConfig<Float>) AllConfig.INSTANCE.Pos_CONFIGS.get("p3ticktimer_scale");
                float s = (sCfg != null && sCfg.Data != null) ? sCfg.Data : 1.0f;
                s = Math.max(0.5f, Math.min(s + dir * 0.05f, 3.0f));
                if (sCfg != null) sCfg.Data = s;
            } else if ("bonzo_pos".equals(hoveredKey) || "spirit_pos".equals(hoveredKey) || "phoenix_pos".equals(hoveredKey) || "proc_pos".equals(hoveredKey)) {
                @SuppressWarnings("unchecked") BaseConfig<Float> sCfg = (BaseConfig<Float>) AllConfig.INSTANCE.Pos_CONFIGS.get("invincible_scale");
                float s = (sCfg != null && sCfg.Data != null) ? sCfg.Data : 1.0f;
                s = Math.max(0.5f, Math.min(s + dir * 0.05f, 3.0f));
                if (sCfg != null) sCfg.Data = s;
            } else if ("arrowpoison_pos".equals(hoveredKey)) {
                @SuppressWarnings("unchecked") BaseConfig<Float> sCfg = (BaseConfig<Float>) AllConfig.INSTANCE.Pos_CONFIGS.get("arrowpoison_scale");
                float s = (sCfg != null && sCfg.Data != null) ? sCfg.Data : 1.0f;
                s = Math.max(0.5f, Math.min(s + dir * 0.05f, 3.0f));
                if (sCfg != null) sCfg.Data = s;
            } else if ("flareflux_pos".equals(hoveredKey)) {
                @SuppressWarnings("unchecked") BaseConfig<Float> sCfg = (BaseConfig<Float>) AllConfig.INSTANCE.Pos_CONFIGS.get("flareflux_scale");
                float s = (sCfg != null && sCfg.Data != null) ? sCfg.Data : 1.0f;
                s = Math.max(0.5f, Math.min(s + dir * 0.05f, 3.0f));
                if (sCfg != null) sCfg.Data = s;
            }
        }
    }
}
