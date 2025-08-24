package com.aftertime.ratallofyou.modules.kuudra.PhaseFourAndFive;

import com.aftertime.ratallofyou.UI.config.ConfigData.AllConfig;
import com.aftertime.ratallofyou.UI.config.ConfigData.ModuleInfo;
import com.aftertime.ratallofyou.utils.KuudraUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.entity.monster.EntityMagmaCube;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/**
 * Shows a direction title on Kuudra spawn based on Kuudra's X/Z location when HP enters ~25k window.
 * No settings gate: assumed enabled by default. Area gate: only in Kuudra Hollow.
 */
public class Direction {
    private static final Minecraft mc = Minecraft.getMinecraft();

    private boolean shownInWindow = false;

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!KuudraUtils.isInKuudraHollow() || !isModuleEnabled()) return;

        EntityMagmaCube kuudra = KuudraUtils.findKuudraBoss();
        if (kuudra == null) { shownInWindow = false; return; }

        float hp = kuudra.getHealth();
        boolean inWindow = (hp <= 25000f && hp > 24900f);
        if (!inWindow) { shownInWindow = false; return; }
        if (shownInWindow) return;

        double x = kuudra.posX;
        double z = kuudra.posZ;
        String title;
        if (x < -128) title = "§c§lRIGHT!";
        else if (z > -84) title = "§2§lFRONT!";
        else if (x > -72) title = "§a§lLEFT!";
        else if (z < -132) title = "§4§lBACK!";
        else title = "§6§l?";

        GuiIngame gui = mc.ingameGUI;
        if (gui != null) {
            // fadeIn=0, stay=25, fadeOut=5 (ticks)
            gui.displayTitle(title, "", 0, 25, 5);
        }
        shownInWindow = true;
    }

    private boolean isModuleEnabled() {
        ModuleInfo cfg = (ModuleInfo) AllConfig.INSTANCE.MODULES.get("kuudra_kuudradirection");
        return cfg != null && Boolean.TRUE.equals(cfg.Data);
    }
}
