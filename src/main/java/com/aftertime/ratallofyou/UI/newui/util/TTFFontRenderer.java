package com.aftertime.ratallofyou.UI.newui.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.CRC32;

/**
 * Minimal TrueType font renderer for UI text. It pre-renders entire strings to a DynamicTexture
 * and caches them for reuse. This keeps changes local to TextRender and avoids touching other UI code.
 */
final class TTFFontRenderer {
    private static final String RESOURCE_PATH = "font.ttf"; // at JAR root (src/main/resources/font.ttf)
    private static final float BASE_PIXEL_SIZE = 9f; // match vanilla 1.8.9 height; overall UI scale applied in TextRender
    private static final int CACHE_LIMIT = 256; // max cached strings
    private static final int PADDING = 2; // 1px border on each side to avoid clipping after AA

    private final Minecraft mc = Minecraft.getMinecraft();
    private final TextureManager textureManager = mc.getTextureManager();

    private Font awtFont;

    private static final class CacheEntry {
        final int width;
        final int height;
        final int ascent;
        final DynamicTexture texture;
        final ResourceLocation location;
        CacheEntry(int width, int height, int ascent, DynamicTexture texture, ResourceLocation location) {
            this.width = width;
            this.height = height;
            this.ascent = ascent;
            this.texture = texture;
            this.location = location;
        }
    }

    private final Map<String, CacheEntry> cache = new LinkedHashMap<String, CacheEntry>(CACHE_LIMIT + 1, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, CacheEntry> eldest) {
            if (size() > CACHE_LIMIT) {
                // Free GL texture when evicted
                try {
                    eldest.getValue().texture.deleteGlTexture();
                } catch (Throwable ignored) {}
                return true;
            }
            return false;
        }
    };

    static TTFFontRenderer tryCreate() {
        TTFFontRenderer r = new TTFFontRenderer();
        return r.init() ? r : null;
    }

    private boolean init() {
        // Load TTF from classpath root
        InputStream is = null;
        try {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            if (cl != null) {
                is = cl.getResourceAsStream(RESOURCE_PATH);
            }
            if (is == null) {
                // Fallback try using this class' loader
                is = TTFFontRenderer.class.getClassLoader().getResourceAsStream(RESOURCE_PATH);
            }
            if (is == null) return false;

            Font base = Font.createFont(Font.TRUETYPE_FONT, is);
            awtFont = base.deriveFont(Font.PLAIN, BASE_PIXEL_SIZE);
            return true;
        } catch (Throwable t) {
            return false;
        } finally {
            if (is != null) try { is.close(); } catch (IOException ignored) {}
        }
    }

    int getStringWidth(String s) {
        if (s == null || s.isEmpty()) return 0;
        String clean = sanitize(s);
        CacheEntry ce = cache.get(clean);
        if (ce != null) return ce.width;
        // Measure using FontMetrics
        BufferedImage tmp = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = tmp.createGraphics();
        applyHints(g);
        g.setFont(awtFont);
        FontMetrics fm = g.getFontMetrics();
        int w = fm.stringWidth(clean) + PADDING;
        g.dispose();
        return Math.max(0, w);
    }

    int getFontHeight() {
        // Use metrics ascent+descent + small slack as line height
        BufferedImage tmp = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = tmp.createGraphics();
        applyHints(g);
        g.setFont(awtFont);
        FontMetrics fm = g.getFontMetrics();
        int h = fm.getAscent() + fm.getDescent() + 1 + PADDING / 2;
        g.dispose();
        return Math.max(1, h);
    }

    void drawString(String s, float x, float y, int color, boolean shadow) {
        if (s == null || s.isEmpty()) return;
        String clean = sanitize(s);
        CacheEntry ce = cache.computeIfAbsent(clean, this::renderStringToTexture);
        if (ce == null) return;

        // Ensure blending for proper font alpha
        GlStateManager.enableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

        // Draw shadow if requested
        if (shadow) {
            GlStateManager.color(0f, 0f, 0f, extractAlpha(color) * 0.5f);
            mc.getTextureManager().bindTexture(ce.location);
            // Set texture filtering to nearest for text crispness
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
            Gui.drawModalRectWithCustomSizedTexture((int)(x + 1), (int)(y + 1), 0, 0, ce.width, ce.height, ce.width, ce.height);
        }

        // Draw main text tinted to color
        float a = extractAlpha(color);
        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;
        GlStateManager.color(r, g, b, a);
        mc.getTextureManager().bindTexture(ce.location);
        // Ensure nearest filtering again in case state changed
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        Gui.drawModalRectWithCustomSizedTexture((int)x, (int)y, 0, 0, ce.width, ce.height, ce.width, ce.height);

        // Reset color to white to not affect subsequent renders
        GlStateManager.color(1f, 1f, 1f, 1f);
    }

    private CacheEntry renderStringToTexture(String s) {
        // Prepare graphics to measure
        BufferedImage tmp = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = tmp.createGraphics();
        applyHints(g);
        g.setFont(awtFont);
        FontMetrics fm = g.getFontMetrics();
        int ascent = fm.getAscent();
        int descent = fm.getDescent();
        int w = Math.max(1, fm.stringWidth(s)) + PADDING;
        int h = Math.max(1, ascent + descent + 1 + PADDING / 2);
        g.dispose();

        // Render onto ARGB image with AA and padding
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        applyHints(g2);
        g2.setFont(awtFont);
        g2.setColor(Color.WHITE);
        // Draw at (1, ascent+1) so we have a 1px border around glyphs
        g2.drawString(s, 1, 1 + ascent);
        g2.dispose();

        DynamicTexture dyn = new DynamicTexture(img);
        String key = dynamicKey(s);
        ResourceLocation rl = textureManager.getDynamicTextureLocation(key, dyn);
        return new CacheEntry(w, h, ascent, dyn, rl);
    }

    private static void applyHints(Graphics2D g) {
        // Prefer crisp, pixel-aligned text over smoothed AA which can blur at small sizes
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
        // Optional: leave shape AA disabled as well
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
    }

    private static float extractAlpha(int colorARGB) {
        int a = (colorARGB >>> 24) & 0xFF;
        if (a == 0) return 1.0f; // match MC behavior: treat 0 alpha as fully opaque if no explicit alpha given
        return a / 255.0f;
    }

    private static String sanitize(String s) {
        // Strip Minecraft formatting codes like §a, §l, etc.
        int len = s.length();
        StringBuilder out = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            char c = s.charAt(i);
            if (c == '\u00A7') { // '§'
                i++; // skip next formatting char if present
                continue;
            }
            // Skip other control chars except normal printable ones
            if (c < 0x20) continue;
            out.append(c);
        }
        return out.toString();
    }

    private static String dynamicKey(String s) {
        CRC32 crc = new CRC32();
        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
        crc.update(bytes, 0, bytes.length);
        long v = crc.getValue();
        return "ratui/font/" + Long.toHexString(v) + "_" + s.length();
    }
}
