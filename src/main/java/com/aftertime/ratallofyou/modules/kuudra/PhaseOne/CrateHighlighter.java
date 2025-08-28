package com.aftertime.ratallofyou.modules.kuudra.PhaseOne;


import com.aftertime.ratallofyou.UI.config.ConfigData.AllConfig;
import com.aftertime.ratallofyou.UI.config.ConfigData.ModuleInfo;
import com.aftertime.ratallofyou.utils.KuudraUtils;
import com.aftertime.ratallofyou.utils.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.monster.EntityGiantZombie;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class CrateHighlighter {
    private static final Minecraft mc = Minecraft.getMinecraft();

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        if (!isModuleEnabled() || mc.theWorld == null || mc.thePlayer == null) return;
        // Only during Kuudra Phase 1 to match crate-beam behavior
        if (!KuudraUtils.isPhase(1)) return;

        for (Object o : mc.theWorld.loadedEntityList) {
            if (!(o instanceof EntityGiantZombie)) continue;
            EntityGiantZombie giant = (EntityGiantZombie) o;
            if (!KuudraUtils.isKuudraCrateGiant(giant)) continue;

            Vec3 crate = KuudraUtils.cratePosInterpolated(giant, event.partialTicks);
            double x = crate.xCoord;
            double z = crate.zCoord;
            double y = crate.yCoord; // should be 75.0

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


    private boolean isModuleEnabled() {
        ModuleInfo cfg = (ModuleInfo) AllConfig.INSTANCE.MODULES.get("kuudra_cratehighlighter");
        return cfg != null && Boolean.TRUE.equals(cfg.Data);
    }
}