package com.aftertime.ratallofyou.modules.kuudra;


import com.aftertime.ratallofyou.UI.config.ConfigData.AllConfig;
import com.aftertime.ratallofyou.UI.config.ConfigData.ModuleInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class PearlCancel {
    private static final Minecraft mc = Minecraft.getMinecraft();

    @SubscribeEvent
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!isModuleEnabled()) return;

        if (event.action != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) return;

        ItemStack heldItem = mc.thePlayer.getHeldItem();
        if (heldItem == null) return;

        if (heldItem.getItem() == Items.ender_pearl) {
            event.setCanceled(true);
        }
    }

    private boolean isModuleEnabled() {
        ModuleInfo cfg = (ModuleInfo) AllConfig.INSTANCE.MODULES.get("kuudra_pearlcancel");
        return cfg != null && Boolean.TRUE.equals(cfg.Data);
    }
}