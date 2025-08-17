package com.aftertime.ratallofyou.modules.render;

import com.aftertime.ratallofyou.settings.BooleanSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import java.lang.reflect.Field;

public class ArmorHide {
    private static final BooleanSetting MODULE_ENABLED = new BooleanSetting("Armor Hide");
    private static boolean reflectionInitialized = false;
    private static Field mainModelField;

    static {
        try {
            mainModelField = RenderPlayer.class.getDeclaredField("mainModel");
            mainModelField.setAccessible(true);
            reflectionInitialized = true;
        } catch (Exception e) {
            System.err.println("Failed to initialize ArmorHide reflection: " + e.getMessage());
            reflectionInitialized = false;
        }
    }

    @SubscribeEvent
    public void onPlayerRender(RenderPlayerEvent.Pre event) {
        if (!isModuleEnabled() || !reflectionInitialized) return;

        if (event.entityPlayer == Minecraft.getMinecraft().thePlayer) {
            try {
                ModelBiped model = (ModelBiped) mainModelField.get(event.renderer);
                model.bipedHeadwear.showModel = false;    // Helmet
                model.bipedBody.showModel = false;        // Chestplate
                model.bipedLeftArm.showModel = false;    // Left arm (part of chestplate)
                model.bipedRightArm.showModel = false;    // Right arm (part of chestplate)
                model.bipedLeftLeg.showModel = false;     // Leggings (left leg)
                model.bipedRightLeg.showModel = false;    // Leggings (right leg)
            } catch (Exception e) {
                System.err.println("ArmorHide error: " + e.getMessage());
            }
        }
    }

    @SubscribeEvent
    public void onPlayerRenderPost(RenderPlayerEvent.Post event) {
        if (!isModuleEnabled() || !reflectionInitialized) return;

        if (event.entityPlayer == Minecraft.getMinecraft().thePlayer) {
            try {
                ModelBiped model = (ModelBiped) mainModelField.get(event.renderer);
                model.bipedHeadwear.showModel = true;
                model.bipedBody.showModel = true;
                model.bipedLeftArm.showModel = true;
                model.bipedRightArm.showModel = true;
                model.bipedLeftLeg.showModel = true;
                model.bipedRightLeg.showModel = true;
            } catch (Exception e) {
                System.err.println("ArmorHide error: " + e.getMessage());
            }
        }
    }

    private static boolean isModuleEnabled() {
        return MODULE_ENABLED.isEnabled();
    }
}