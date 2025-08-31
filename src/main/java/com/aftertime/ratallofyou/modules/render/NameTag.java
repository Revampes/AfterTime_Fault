package com.aftertime.ratallofyou.modules.render;

import com.aftertime.ratallofyou.UI.config.ConfigData.AllConfig;
import com.aftertime.ratallofyou.utils.DungeonUtils;
import com.aftertime.ratallofyou.utils.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class NameTag {
    private static final Minecraft mc = Minecraft.getMinecraft();
    // Refresh renderer every 3 minutes like PlayerESP
    private static final long CLEAR_INTERVAL_MS = 3L * 60 * 1000;
    private long lastClearMs = System.currentTimeMillis();

    // Basic keywords to filter NPCs masquerading as players
    private static final String[] NAME_FILTER = new String[]{
            "Goblin","Ice Walker","Weakling","Frozen Steve","Kalhuiki",
            "CreeperTam","Pitfighter","Sentry","Scarecrow"
    };

    public NameTag() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    private boolean isEnabled() {
        // New toggle key under Render category
        return DungeonUtils.isModuleEnabled("render_nametag");
    }

    private float getScale() {
        try {
            Object val = AllConfig.INSTANCE.NAMETAG_CONFIGS.get("nametag_scale").Data;
            if (val instanceof Float) {
                float s = (Float) val;
                if (Float.isNaN(s) || s <= 0f) return 0.002f;
                // allow ultra small nametags; clamp to [0.0005, 1.0]
                return Math.max(0.0005f, Math.min(s, 1.0f));
            }
        } catch (Throwable ignored) { }
        return 0.002f;
    }

    // Heuristic NPC detection copied from PlayerESP
    private static boolean isNPC(Entity entity) {
        try {
            if (!(entity instanceof net.minecraft.client.entity.EntityOtherPlayerMP)) return false;
            EntityLivingBase elb = (EntityLivingBase) entity;
            return entity.getUniqueID().version() == 2 && elb.getHealth() == 20.0f;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private boolean shouldRenderFor(EntityPlayer player) {
        if (player == null || mc == null || mc.thePlayer == null) return false;
        if (player == mc.thePlayer) return false;
        if (isNPC(player)) return false; // do not render npc nametag

        try {
            String disp = player.getDisplayName() != null ? player.getDisplayName().getUnformattedText() : "";
            String name = player.getName() != null ? player.getName() : "";
            String dispLower = disp.toLowerCase();
            String nameLower = name.toLowerCase();
            for (String kw : NAME_FILTER) {
                String k = kw.toLowerCase();
                if (dispLower.contains(k) || nameLower.contains(k)) {
                    return false;
                }
            }
            if (dispLower.contains("[lv]") || nameLower.contains("[lv]")) return false;
        } catch (Throwable ignored) { }

        return true;
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
        if (!isEnabled() || mc.theWorld == null || mc.thePlayer == null) return;

        long now = System.currentTimeMillis();
        if (now - lastClearMs >= CLEAR_INTERVAL_MS) {
            try { if (mc.renderGlobal != null) mc.renderGlobal.loadRenderers(); } catch (Throwable ignored) {}
            lastClearMs = now;
        }

        float partial = event.partialTicks;
        float scale = getScale();
        // strong reduction multiplier to ensure tiny on-screen text even if user scale is large
        float effectiveScale = scale * 0.05f;
        for (Object obj : mc.theWorld.playerEntities) {
            if (!(obj instanceof EntityPlayer)) continue;
            EntityPlayer p = (EntityPlayer) obj;
            if (!shouldRenderFor(p)) continue;

            double x = p.lastTickPosX + (p.posX - p.lastTickPosX) * partial;
            double y = p.lastTickPosY + (p.posY - p.lastTickPosY) * partial + p.height + 0.4; // slightly lower constant offset
            double z = p.lastTickPosZ + (p.posZ - p.lastTickPosZ) * partial;

            String text;
            try {
                text = p.getDisplayName() != null ? p.getDisplayName().getFormattedText() : p.getName();
            } catch (Throwable t) {
                text = p.getName();
            }

            RenderUtils.renderFloatingTextConstant(text, x, y, z, effectiveScale, 0xFFFFFFFF, false);
        }
    }
}
