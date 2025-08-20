package com.aftertime.ratallofyou.modules.kuudra;

import com.aftertime.ratallofyou.UI.config.ConfigData.AllConfig;
import com.aftertime.ratallofyou.UI.config.ConfigData.ModuleInfo;
import com.aftertime.ratallofyou.utils.KuudraUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.util.Vec3;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.client.event.ClientChatReceivedEvent;

public class CrateAura {
    private static final Minecraft mc = Minecraft.getMinecraft();

    // Simple state mirroring the JS version
    private int pickingUpCooldown = 0; // ticks remaining before next interact
    private boolean cratesSpawned = false; // enabled after specific Elle chat lines (used as a hint only)

    // Elle lines that indicate crates spawning
    private static final String[] CRATE_MESSAGES = new String[] {
            "Not again!",
            "Head over to the main platform, I will join you when I get a bite!"
    };

    private static final String END_MESSAGE = "Phew! The Ballista is finally ready! It should be strong enough to tank Kuudra's blows now!";

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        cratesSpawned = false;
        pickingUpCooldown = 0;
    }

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        if (event.message == null) return;
        String msg = event.message.getUnformattedText();
        if (msg == null) return;

        for (String line : CRATE_MESSAGES) {
            if (msg.contains(line)) {
                cratesSpawned = true; // hint: crates likely exist now
                return;
            }
        }

        if (msg.contains(END_MESSAGE)) {
            cratesSpawned = false;
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !isModuleEnabled()) return;
        if (mc.theWorld == null || mc.thePlayer == null) return;

        if (pickingUpCooldown > 0) {
            pickingUpCooldown--;
            return;
        }

        // We try whenever a valid candidate is nearby; chat just helps reduce spam
        // 1) ArmorStand-based supplies first (matches CrateHighlighter/CrateBeam)
        for (Entity e : mc.theWorld.loadedEntityList) {
            if (!KuudraUtils.isInteractable(e)) continue;

            // Try both packet forms to maximize compatibility
            mc.thePlayer.sendQueue.addToSendQueue(new C02PacketUseEntity(e, C02PacketUseEntity.Action.INTERACT));
            mc.thePlayer.sendQueue.addToSendQueue(new C02PacketUseEntity(e, new Vec3(0, 0, 0)));

            pickingUpCooldown = 20; // faster retry
            return;
        }

        // 2) Fallback: try nearby zombies as in the JS module
        for (Entity e : mc.theWorld.loadedEntityList) {
            if (!(e instanceof EntityZombie)) continue;
            EntityZombie z = (EntityZombie) e;
            if (mc.thePlayer.getDistanceToEntity(z) > 4.0f) continue;
            // Slot 3 is helmet in 1.8.9 getCurrentArmor indexing
            if (z.getCurrentArmor(3) != null) continue;

            mc.thePlayer.sendQueue.addToSendQueue(new C02PacketUseEntity(z, C02PacketUseEntity.Action.INTERACT));
            mc.thePlayer.sendQueue.addToSendQueue(new C02PacketUseEntity(z, new Vec3(0, 0, 0)));

            pickingUpCooldown = 20;
            return;
        }
    }

    private boolean isModuleEnabled() {
        ModuleInfo cfg = (ModuleInfo) AllConfig.INSTANCE.MODULES.get("kuudra_crateaura");
        return cfg != null && Boolean.TRUE.equals(cfg.Data);
    }
}
