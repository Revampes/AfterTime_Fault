package com.aftertime.ratallofyou.modules.kuudra;


import com.aftertime.ratallofyou.UI.config.ConfigData.AllConfig;
import com.aftertime.ratallofyou.UI.config.ConfigData.ModuleInfo;
import com.aftertime.ratallofyou.utils.KuudraUtils;
import com.aftertime.ratallofyou.utils.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class CrateHighlighter {
    private static final Minecraft mc = Minecraft.getMinecraft();
    private static final String MODULE_NAME = "Crate Highlighter";

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        if (!isModuleEnabled() || mc.theWorld == null) return;

        for (Entity entity : mc.theWorld.loadedEntityList) {
            if (KuudraUtils.isValidSupply(entity) && !entity.getDisplayName().getUnformattedText().contains("SUPPLY RECEIVED")) {
                double distance = mc.thePlayer.getDistanceToEntity(entity);
                float lineWidth = (float) Math.max(1.5, 3.0 - (distance / 50.0));
                float alpha = (float) Math.max(0.4, 1.0 - (distance / 100.0));

                AxisAlignedBB box = new AxisAlignedBB(
                        entity.posX - 0.5, entity.posY, entity.posZ - 0.5,
                        entity.posX + 0.5, entity.posY + 1.0, entity.posZ + 0.5
                );

                boolean interactable = KuudraUtils.isInteractable(entity);
                float red = interactable ? 1.0f : 0.0f;
                float green = 0.0f;
                float blue = interactable ? 0.0f : 1.0f;

                RenderUtils.drawEspBox(
                        box,
                        red, green, blue,
                        alpha,
                        lineWidth
                );
            }
        }
    }

    private boolean isModuleEnabled() {
        ModuleInfo cfg = (ModuleInfo) AllConfig.INSTANCE.MODULES.get("kuudra_cratehighlighter");
        return cfg != null && Boolean.TRUE.equals(cfg.Data);
    }
}