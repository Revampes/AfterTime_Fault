package com.aftertime.ratallofyou.modules.kuudra.PhaseFourAndFive;

import com.aftertime.ratallofyou.UI.config.ConfigData.AllConfig;
import com.aftertime.ratallofyou.UI.config.ConfigData.ModuleInfo;
import com.aftertime.ratallofyou.utils.KuudraUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.monster.EntityMagmaCube;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/**
 * Shows a direction title on Kuudra spawn based on Kuudra's X/Z location when HP enters ~25k window.
 */
public class Direction {
    private static final Minecraft mc = Minecraft.getMinecraft();

    private boolean shownInWindow = false;
    // Track last seen HP to catch downward crossing of 25k between ticks
    private Float lastHp = null;

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!KuudraUtils.isInKuudraHollow() || !isModuleEnabled()) { shownInWindow = false; lastHp = null; return; }

        EntityMagmaCube kuudra = KuudraUtils.findKuudraBoss();
        if (kuudra == null) { shownInWindow = false; lastHp = null; return; }

        float hp = kuudra.getHealth();
        boolean narrowWindow = (hp <= 25000f && hp > 24900f);
        boolean crossedDown = (lastHp != null && lastHp > 25000f && hp <= 25000f);
        boolean inWindow = narrowWindow || crossedDown;
        if (!inWindow) { shownInWindow = false; lastHp = hp; return; }
        if (shownInWindow) { lastHp = hp; return; }

        double x = kuudra.posX;
        double z = kuudra.posZ;
        String title;
        if (x < -128) title = "§c§lRIGHT!";
        else if (z > -84) title = "§2§lFRONT!";
        else if (x > -72) title = "§a§lLEFT!";
        else if (z < -132) title = "§4§lBACK!";
        else title = "§6§l?";

        // fadeIn=0, stay=25, fadeOut=5 (ticks)
        mc.ingameGUI.displayTitle(title, "", 0, 25, 5);
        shownInWindow = true;
        lastHp = hp;
    }

    private boolean isModuleEnabled() {
        ModuleInfo cfg = (ModuleInfo) AllConfig.INSTANCE.MODULES.get("kuudra_kuudradirection");
        return cfg != null && Boolean.TRUE.equals(cfg.Data);
    }
}
