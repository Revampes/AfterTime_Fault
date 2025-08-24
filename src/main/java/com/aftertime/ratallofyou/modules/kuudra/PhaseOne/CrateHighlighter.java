package com.aftertime.ratallofyou.modules.kuudra.PhaseOne;


import com.aftertime.ratallofyou.UI.config.ConfigData.AllConfig;
import com.aftertime.ratallofyou.UI.config.ConfigData.ModuleInfo;
import com.aftertime.ratallofyou.utils.KuudraUtils;
import com.aftertime.ratallofyou.utils.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityGiantZombie;
import net.minecraft.item.Item;
import net.minecraft.item.ItemSkull;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Locale;

public class CrateHighlighter {
    private static final Minecraft mc = Minecraft.getMinecraft();

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        if (!isModuleEnabled() || mc.theWorld == null || mc.thePlayer == null) return;
        // Only during Kuudra Phase 1 to match crate-beam behavior
        if (!KuudraUtils.isPhase(1)) return;

        for (Entity e : mc.theWorld.loadedEntityList) {
            if (!(e instanceof EntityGiantZombie)) continue;
            EntityGiantZombie giant = (EntityGiantZombie) e;
            if (!isKuudraCrateGiant(giant)) continue;

            // Interpolate render position like CrateBeaconBeam
            double gx = giant.prevPosX + (giant.posX - giant.prevPosX) * event.partialTicks;
            double gz = giant.prevPosZ + (giant.posZ - giant.prevPosZ) * event.partialTicks;
            float yaw = giant.rotationYaw;
            double rad = Math.toRadians(yaw + 130.0);
            double x = gx + 3.7 * Math.cos(rad);
            double z = gz + 3.7 * Math.sin(rad);
            double y = 75.0; // matches CrateBeaconBeam's ground Y for crates

            double distance = mc.thePlayer.getDistance(x, mc.thePlayer.posY, z);
            float lineWidth = (float) Math.max(1.5, 3.0 - (distance / 50.0));
            float alpha = (float) Math.max(0.4, 1.0 - (distance / 100.0));

            AxisAlignedBB box = new AxisAlignedBB(
                    x - 0.5, y, z - 0.5,
                    x + 0.5, y + 1.0, z + 0.5
            );

            // Cyan to match beacon color
            float red = 0.0f, green = 1.0f, blue = 1.0f;
            RenderUtils.drawEspBox(box, red, green, blue, alpha, lineWidth);
        }
    }

    private boolean isKuudraCrateGiant(EntityGiantZombie giant) {
        try {
            ItemStack held = giant.getHeldItem();
            if (held == null) return false;
            Item item = held.getItem();
            if (item == null) return false;
            if (item instanceof ItemSkull) return true;
            String name = item.getUnlocalizedName();
            return name != null && name.toLowerCase(Locale.ROOT).contains("skull");
        } catch (Exception ignored) {
            return false;
        }
    }

    private boolean isModuleEnabled() {
        ModuleInfo cfg = (ModuleInfo) AllConfig.INSTANCE.MODULES.get("kuudra_cratehighlighter");
        return cfg != null && Boolean.TRUE.equals(cfg.Data);
    }
}