//package com.aftertime.ratallofyou.modules.kuudra;
//
//import com.aftertime.ratallofyou.config.ModConfig;
//import net.minecraft.client.Minecraft;
//import net.minecraft.entity.Entity;
//import net.minecraft.entity.monster.EntityZombie;
//import net.minecraft.network.play.client.C02PacketUseEntity;
//import net.minecraft.util.Vec3;
//import net.minecraftforge.common.MinecraftForge;
//import net.minecraftforge.event.world.WorldEvent;
//import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
//import net.minecraftforge.fml.common.gameevent.TickEvent;
//import net.minecraftforge.client.event.ClientChatReceivedEvent;
//
//import java.util.Arrays;
//import java.util.List;
//
//public class CrateAura {
//    private int pickingUp = 0;
//    private boolean cratesSpawned = false;
//
//    private static final List<String> crateMessages = Arrays.asList(
//            "[NPC] Elle: Not again!",
//            "[NPC] Elle: Head over to the main platform, I will join you when I get a bite!"
//    );
//
//    public CrateAura() {
//        MinecraftForge.EVENT_BUS.register(this);
//    }
//
//    @SubscribeEvent
//    public void onWorldUnload(WorldEvent.Unload event) {
//        cratesSpawned = false;
//        pickingUp = 0;
//    }
//
//    @SubscribeEvent
//    public void onTick(TickEvent.ClientTickEvent event) {
//        if (event.phase != TickEvent.Phase.END) return;
//        if (!cratesSpawned || pickingUp > 0 || !ModConfig.crateAuraToggle) return;
//
//        // Simple cooldown decrement if we can't detect packets
//        if (pickingUp > 0) {
//            pickingUp--;
//            return;
//        }
//
//        for (Entity entity : Minecraft.getMinecraft().theWorld.loadedEntityList) {
//            if (!(entity instanceof EntityZombie)) continue;
//
//            EntityZombie zombie = (EntityZombie) entity;
//            if (zombie.getDistanceToEntity(Minecraft.getMinecraft().thePlayer) > 4) continue;
//
//            sendUseEntity(zombie, null);
//            pickingUp = 20; // Set cooldown
//            break;
//        }
//    }
//
//    @SubscribeEvent
//    public void onChat(ClientChatReceivedEvent event) {
//        String message = event.message.getUnformattedText();
//        if (crateMessages.contains(message)) {
//            cratesSpawned = true;
//        } else if (message.equals("[NPC] Elle: Phew! The Ballista is finally ready! It should be strong enough to tank Kuudra's blows now!")) {
//            cratesSpawned = false;
//        }
//    }
//
//    private void sendUseEntity(Entity entity, Vec3 hitVec) {
//        C02PacketUseEntity packet;
//        if (hitVec == null) {
//            packet = new C02PacketUseEntity(entity, C02PacketUseEntity.Action.INTERACT_AT);
//        } else {
//            packet = new C02PacketUseEntity(entity, new Vec3(0, 0, 0));
//        }
//        Minecraft.getMinecraft().getNetHandler().addToSendQueue(packet);
//    }
//}