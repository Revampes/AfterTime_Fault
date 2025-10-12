package com.aftertime.ratallofyou.UI.newui.util;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;

public final class TextRender {
    private TextRender() {}

    // Global font scaling factor for the new UI (1.0 avoids blur from fractional scaling)
    public static float SCALE = 1.0f;

    // Toggle for using custom TTF renderer. Default false per user request to use vanilla FontRenderer.
    public static boolean USE_TTF = false;

    // Optional TrueType renderer (uses src/main/resources/font.ttf). If unavailable or disabled, we fall back to MC font.
    private static final TTFFontRenderer TTF = TTFFontRenderer.tryCreate();

    public static int width(FontRenderer fr, String s) {
        if (s == null) return 0;
        if (USE_TTF && TTF != null) {
            return Math.round(TTF.getStringWidth(s) * SCALE);
        }
        if (fr == null) return 0;
        return Math.round(fr.getStringWidth(s) * SCALE);
    }

    public static int height(FontRenderer fr) {
        if (USE_TTF && TTF != null) {
            return Math.max(1, Math.round(TTF.getFontHeight() * SCALE));
        }
        if (fr == null) return 0;
        // FontRenderer.FONT_HEIGHT is 9 in 1.8.9; scale the actual height for accuracy.
        return Math.max(1, Math.round(fr.FONT_HEIGHT * SCALE));
    }

    public static void draw(FontRenderer fr, String s, int x, int y, int color) {
        if (s == null) return;
        GlStateManager.pushMatrix();
        GlStateManager.translate((float)x, (float)y, 0.0F);
        GlStateManager.scale(SCALE, SCALE, 1.0F);
        if (USE_TTF && TTF != null) {
            // Draw at origin under the scaling transform
            TTF.drawString(s, 0, 0, color, false);
        } else if (fr != null) {
            fr.drawString(s, 0, 0, color);
        }
        GlStateManager.popMatrix();
    }

    public static void drawWithShadow(FontRenderer fr, String s, int x, int y, int color) {
        if (s == null) return;
        GlStateManager.pushMatrix();
        GlStateManager.translate((float)x, (float)y, 0.0F);
        GlStateManager.scale(SCALE, SCALE, 1.0F);
        if (USE_TTF && TTF != null) {
            TTF.drawString(s, 0, 0, color, true);
        } else if (fr != null) {
            fr.drawStringWithShadow(s, 0, 0, color);
        }
        GlStateManager.popMatrix();
    }
}
