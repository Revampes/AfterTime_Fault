package com.aftertime.ratallofyou.modules.kuudra.PhaseFourAndFive;

import com.aftertime.ratallofyou.UI.config.ConfigData.AllConfig;
import com.aftertime.ratallofyou.UI.config.ConfigData.ModuleInfo;
import com.aftertime.ratallofyou.utils.KuudraUtils;
import com.aftertime.ratallofyou.utils.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.monster.EntityMagmaCube;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/**
 * Tracks Kuudra HP and renders both a HUD % and floating HP text above the boss.
 * No settings gate; active while in Kuudra Hollow.
 */
public class KuudraHP {
    private static final Minecraft mc = Minecraft.getMinecraft();

    // HUD percent text and placement
    private String percentText = null;
    private int percentX = 0;

    // World floating text data
    private String worldHpText = "";
    private double worldX = 0, worldY = 0, worldZ = 0;

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!KuudraUtils.isInKuudraHollow()) { clear(); return; }
        if (!isModuleEnabled()) return;

        EntityMagmaCube kuudra = KuudraUtils.findKuudraBoss();
        if (kuudra == null) { clear(); return; }

        int hp = Math.max(0, Math.round(kuudra.getHealth()));

        // Build HUD percent text and center X
        String pct = String.format("§l%.2f%%", (hp / 100000.0) * 100.0);
        ScaledResolution sr = new ScaledResolution(mc);
        int w = mc.fontRendererObj.getStringWidth(pct);
        percentText = pct;
        percentX = (sr.getScaledWidth() / 2) - (w / 2);

        // Build world HP text and position
        worldHpText = getHealthString(hp);
        worldX = kuudra.posX;
        worldY = kuudra.posY + 10.0;
        worldZ = kuudra.posZ;
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.TEXT) return;
        if (percentText == null) return;
        mc.fontRendererObj.drawString(percentText, percentX, 11, 0xFFFFFF, true);
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
        if (worldHpText == null || worldHpText.isEmpty()) return;
        RenderUtils.renderFloatingText(worldHpText, worldX, worldY, worldZ, 0.25f, 0xA7171A, true);
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load e) {
        clear();
    }

    private void clear() {
        percentText = null;
        worldHpText = "";
        worldX = worldY = worldZ = 0.0;
    }

    private String getHealthString(int hp) {
        int phase = KuudraUtils.getPhase();

        // Use legacy formatting codes to match JS color logic
        String colorCode =
                (hp > 99_000) ? "§a" :
                (hp > 75_000) ? "§2" :
                (hp > 50_000) ? "§e" :
                (hp > 25_000) ? "§6" :
                (hp > 10_000) ? "§c" : "§4";

        if (phase >= 5) {
            double scaledHp = hp * 4.0;
            long numM = Math.round((scaledHp * 2400.0) / 1_000_000.0);
            return colorCode + numM + "M §c❤";
        } else {
            double pct = (hp / 100000.0) * 100.0;
            return colorCode + String.format("%.2f%%", pct);
        }
    }

    private boolean isModuleEnabled() {
        ModuleInfo cfg = (ModuleInfo) AllConfig.INSTANCE.MODULES.get("kuudra_kuudrahp");
        return cfg != null && Boolean.TRUE.equals(cfg.Data);
    }
}
