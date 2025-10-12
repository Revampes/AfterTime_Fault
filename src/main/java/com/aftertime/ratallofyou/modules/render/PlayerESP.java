package com.aftertime.ratallofyou.modules.render;

import com.aftertime.ratallofyou.config.ModConfig;
import com.aftertime.ratallofyou.utils.RenderUtils;
import com.aftertime.ratallofyou.utils.PartyUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.Color;
import java.util.List;

public class PlayerESP {
    private static final Minecraft mc = Minecraft.getMinecraft();
    private static final long CLEAR_INTERVAL_MS = 3L * 60 * 1000;
    private long lastClearMs = System.currentTimeMillis();

    private static final String[] NAME_FILTER = new String[]{
            "Goblin","Ice Walker","Weakling","Frozen Steve","Kalhuiki",
            "CreeperTam","Pitfighter","Sentry","Scarecrow"
    };

    public PlayerESP() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    private boolean isEnabled() {
        return ModConfig.enablePlayerESP;
    }

    private int getMode() {
        return ModConfig.playerESPMode;
    }

    private Color getColor() {
        return new Color(ModConfig.playerESPColor, true);
    }

    private boolean onlyParty() {
        return ModConfig.playerESPOnlyParty;
    }

    private boolean isPartyMember(EntityPlayer p) {
        try {
            String name = p.getName();
            List<String> list = PartyUtils.getPartyMembers();
            for (String s : list) {
                if (s != null && name != null && s.equalsIgnoreCase(name)) return true;
            }
        } catch (Throwable ignored) { }
        return false;
    }

    private static boolean isNPC(Entity entity) {
        try {
            if (!(entity instanceof net.minecraft.client.entity.EntityOtherPlayerMP)) return false;
            EntityLivingBase elb = (EntityLivingBase) entity;
            return entity.getUniqueID().version() == 2 && elb.getHealth() == 20.0f;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private boolean isRenderablePlayer(EntityPlayer player) {
        if (player == null || mc == null) return false;
        if (player == mc.thePlayer) return false;
        if (isNPC(player)) return false;
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
        try {
            if (player.isInvisible()) {
                boolean hasEquip = false;
                for (int i = 0; i <= 4; i++) {
                    if (player.getEquipmentInSlot(i) != null) { hasEquip = true; break; }
                }
                if (!hasEquip) return false;
            }
        } catch (Throwable ignored) { }
        return true;
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
        if (!isEnabled() || mc.theWorld == null || mc.thePlayer == null) return;
        long now = System.currentTimeMillis();
        if (now - lastClearMs >= CLEAR_INTERVAL_MS) {
            try {
                if (mc.renderGlobal != null) mc.renderGlobal.loadRenderers();
            } catch (Throwable ignored) {}
            lastClearMs = now;
        }
        int mode = getMode();
        Color c = getColor();
        float r = c.getRed() / 255f;
        float g = c.getGreen() / 255f;
        float b = c.getBlue() / 255f;
        float a = c.getAlpha() / 255f;
        for (Object obj : mc.theWorld.playerEntities) {
            if (!(obj instanceof EntityPlayer)) continue;
            EntityPlayer p = (EntityPlayer) obj;
            if (!isRenderablePlayer(p)) continue;
            switch (mode) {
                case 1:
                    net.minecraft.util.AxisAlignedBB box = p.getEntityBoundingBox().expand(0.05, 0.15, 0.05);
                    RenderUtils.renderBoxFromCorners(
                            box.minX, box.minY, box.minZ,
                            box.maxX, box.maxY, box.maxZ,
                            r, g, b, a, true, 1.0f, true
                    );
                    RenderUtils.drawEspBox(box, r, g, b, 1.0f, 1.0f);
                    break;
                case 0:
                default:
                    RenderUtils.drawEntityBox(p, r, g, b, 1.0f, 2.0f, event.partialTicks);
                    break;
            }
        }
        GlStateManager.color(1f, 1f, 1f, 1f);
    }
}
