package com.aftertime.ratallofyou.modules.render;

import com.aftertime.ratallofyou.UI.Settings.BooleanSettings;
import com.aftertime.ratallofyou.UI.config.ConfigData.AllConfig;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.opengl.GL11;

public class DarkMode {
    private static final Minecraft mc = Minecraft.getMinecraft();

    private boolean isEnabled() {
        return BooleanSettings.isEnabled("render_darkmode");
    }

    private int getOpacity() {
        Object value = AllConfig.INSTANCE.DARKMODE_CONFIGS.get("darkmode_getopacity").Data;
        if (value instanceof Integer) return (Integer) value;
        try { return Integer.parseInt(String.valueOf(value)); } catch (Exception ignored) {}
        return 128;
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (!isEnabled()) return;
        if (event.type != RenderGameOverlayEvent.ElementType.ALL) return;
        ScaledResolution sr = new ScaledResolution(mc);
        int width = sr.getScaledWidth();
        int height = sr.getScaledHeight();
        int opacity = getOpacity();
        GL11.glPushMatrix();
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(0f, 0f, 0f, opacity / 255f);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(0, 0);
        GL11.glVertex2f(0, height);
        GL11.glVertex2f(width, height);
        GL11.glVertex2f(width, 0);
        GL11.glEnd();
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glPopMatrix();
    }
}
