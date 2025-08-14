package com.aftertime.ratallofyou.modules.dungeon;

import com.aftertime.ratallofyou.UI.ModConfig;
import com.aftertime.ratallofyou.utils.DungeonUtils;
import com.aftertime.ratallofyou.utils.BoxRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class WitherKeyHighlighter {
    private static final String MODULE_NAME = "Key Highlighter";
    private static boolean bloodOpened = false;
    private static final Minecraft mc = Minecraft.getMinecraft();

    public static void register() {
        MinecraftForge.EVENT_BUS.register(new WitherKeyHighlighter());
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        bloodOpened = false;
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
        if (!isModuleEnabled() || bloodOpened || !DungeonUtils.isInDungeon()) return;

        for (Object entity : mc.theWorld.loadedEntityList) {
            if (entity instanceof EntityArmorStand) {
                EntityArmorStand armorStand = (EntityArmorStand) entity;
                String name = armorStand.getDisplayName().getUnformattedText();

                if (name.contains("Wither Key")) {
                    BoxRenderer.drawEntityEspBox(
                            armorStand.posX, armorStand.posY, armorStand.posZ,
                            0.8, 1.0,  // Width and height
                            1, 1, 0,   // Yellow color
                            1.0f       // Y offset
                    );
                } else if (name.contains("Blood Key")) {
                    BoxRenderer.drawEntityEspBox(
                            armorStand.posX, armorStand.posY, armorStand.posZ,
                            0.8, 1.0,  // Width and height
                            1, 0, 0,   // Red color
                            1.0f       // Y offset
                    );
                }
            }
        }
    }

    private boolean isModuleEnabled() {
        for (ModConfig.ModuleInfo module : ModConfig.MODULES) {
            if (module.name.equals(MODULE_NAME)) {
                return module.enabled;
            }
        }
        return false;
    }
}