package com.aftertime.ratallofyou.modules.dungeon;

import com.aftertime.ratallofyou.UI.ModConfig;
import com.aftertime.ratallofyou.utils.DungeonUtils;
import com.aftertime.ratallofyou.utils.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;

public class KeyHighlighter {
    private static final String MODULE_NAME = "Key Highlighter";
    private static boolean bloodOpened = false;
    private static final Minecraft mc = Minecraft.getMinecraft();
    private static final Map<String, Boolean> keyTracking = new HashMap<String, Boolean>();

    public static void register() {
        MinecraftForge.EVENT_BUS.register(new KeyHighlighter());
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        bloodOpened = false;
        keyTracking.clear();
    }

    @SubscribeEvent
    public void onEntitySpawn(EntityJoinWorldEvent event) {
        if (!isModuleEnabled() || bloodOpened || !DungeonUtils.isInDungeon()) return;

        if (event.entity instanceof EntityArmorStand) {
            EntityArmorStand armorStand = (EntityArmorStand) event.entity;
            String name = armorStand.getDisplayName().getUnformattedText();
            String keyId = armorStand.getUniqueID().toString();

            if (name.contains("Wither Key") && !keyTracking.containsKey(keyId)) {
                keyTracking.put(keyId, true);
                showTitle(EnumChatFormatting.GOLD + "Wither Key Dropped!", "", 10, 40, 10);
                mc.thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.GOLD + "Wither Key Dropped!"));
            }
            else if (name.contains("Blood Key") && !keyTracking.containsKey(keyId)) {
                keyTracking.put(keyId, true);
                showTitle(EnumChatFormatting.RED + "Blood Key Dropped!", "", 10, 40, 10);
                mc.thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Blood Key Dropped!"));
            }
        }
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
        if (!isModuleEnabled() || bloodOpened || !DungeonUtils.isInDungeon()) return;

        for (Object entity : mc.theWorld.loadedEntityList) {
            if (entity instanceof EntityArmorStand) {
                EntityArmorStand armorStand = (EntityArmorStand) entity;
                String name = armorStand.getDisplayName().getUnformattedText();

                if (name.contains("Wither Key")) {
                    RenderUtils.drawEntityEspBox(
                            armorStand.posX, armorStand.posY, armorStand.posZ,
                            0.8, 1.0,  // Width and height
                            0.0f, 0.0f, 0.0f,   // Yellow color (R,G,B)
                            1.0f       // Y offset
                    );
                } else if (name.contains("Blood Key")) {
                    RenderUtils.drawEntityEspBox(
                            armorStand.posX, armorStand.posY, armorStand.posZ,
                            0.8, 1.0,  // Width and height
                            1.0f, 0.0f, 0.0f,   // Red color (R,G,B)
                            1.0f       // Y offset
                    );
                }
            }
        }
    }

    private void showTitle(String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        if (mc.thePlayer != null) {
            // Clear any existing titles first
            mc.ingameGUI.displayTitle(title, subtitle, fadeIn, stay, fadeOut);
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