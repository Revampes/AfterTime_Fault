package com.aftertime.ratallofyou.modules.render;

import com.aftertime.ratallofyou.config.ModConfig;
import com.aftertime.ratallofyou.utils.RenderUtils;
import com.aftertime.ratallofyou.utils.PartyUtils; // New: party filter
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
        return ModConfig.enableNameTag;
    }

    private float getScale() {
        try {
            int percent = ModConfig.nametagScale;
            // Convert percent to a small scale factor; baseline at 0.02 for 20%
            float s = Math.max(1, Math.min(100, percent)) / 1000f;
            if (Float.isNaN(s) || s <= 0f) return 0.002f;
            return s;
        } catch (Throwable ignored) { }
        return 0.002f;
    }

    // Only-party toggle from ModConfig
    private boolean onlyParty() {
        try {
            return ModConfig.nametagOnlyParty;
        } catch (Throwable ignored) { return false; }
    }

    private boolean isPartyMember(EntityPlayer p) {
        try {
            String name = p.getName();
            for (String s : PartyUtils.getPartyMembers()) {
                if (s != null && name != null && s.equalsIgnoreCase(name)) return true;
            }
        } catch (Throwable ignored) { }
        return false;
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

        // Optional: only party members
        if (onlyParty()) {
            if (!PartyUtils.isInParty() || !isPartyMember(player)) return false;
        }

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
        float effectiveScale = scale * 0.05f; // strong reduction multiplier for small on-screen text
        for (Object obj : mc.theWorld.playerEntities) {
            if (!(obj instanceof EntityPlayer)) continue;
            EntityPlayer p = (EntityPlayer) obj;
            if (!shouldRenderFor(p)) continue;

            double x = p.lastTickPosX + (p.posX - p.lastTickPosX) * partial;
            double y = p.lastTickPosY + (p.posY - p.lastTickPosY) * partial + p.height + 0.4;
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
