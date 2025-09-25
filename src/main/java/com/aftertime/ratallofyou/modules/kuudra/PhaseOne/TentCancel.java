package com.aftertime.ratallofyou.modules.kuudra.PhaseOne;

import com.aftertime.ratallofyou.UI.config.ConfigData.AllConfig;
import com.aftertime.ratallofyou.UI.config.ConfigData.BaseConfig;
import com.aftertime.ratallofyou.UI.config.ConfigData.ModuleInfo;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.network.play.server.S1BPacketEntityAttach;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * TentCancel - Prevents being pulled by Kuudra tentacles
 * Java implementation based on the original JavaScript version
 */
public class TentCancel {
    private final Minecraft mc = Minecraft.getMinecraft();

    // Store position history
    private final List<Position> positions = new ArrayList<>();
    private boolean isSneaking = false;
    private int attempts = 0;
    private boolean handlerAdded = false;
    private boolean moduleActive = true; // Tracks if module is active (like JS register/unregister)
    private static final String HANDLER_NAME = "tentcancel_handler";
    private boolean debugMode = true; // Set to true for debugging

    /**
     * Simple position class to match the JavaScript version's structure
     */
    private static class Position {
        double x, y, z;

        Position(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    /**
     * Check if the module is enabled in config
     */
    private boolean isEnabled() {
        ModuleInfo info = (ModuleInfo) AllConfig.INSTANCE.MODULES.get("kuudra_tentcancel");
        return info != null && Boolean.TRUE.equals(info.Data) && moduleActive;
    }

    /**
     * Get the max attempts from config, default to 5
     */
    private int getMaxAttempts() {
        BaseConfig<?> config = AllConfig.INSTANCE.TENTCANCEL_CONFIGS.get("tentcancel_max_attempts");
        return config != null && config.Data instanceof Integer ? (Integer) config.Data : 5;
    }

    /**
     * Send a chat message to the player
     */
    private void chat(String message) {
        if (mc.thePlayer != null) {
            mc.thePlayer.addChatMessage(new ChatComponentText(message));
        }
    }

    /**
     * Send a debug message if debug mode is enabled
     */
    private void debug(String message) {
        if (debugMode && mc.thePlayer != null) {
            chat("[TentCancel] " + message);
        }
    }

    /**
     * Show failsafe message
     */
    private void showFailSafe(boolean mounted) {
        if (mounted) {
            chat("[HateBM] Tentacle Cancel turned off (mounted cannon)");
        } else {
            chat("[HateBM] Tentacle Cancel turned off (too many attempts)");
        }
        moduleActive = false; // Disable the module like JS unregister
    }

    /**
     * Reset state when world changes or phase changes
     */
    private void resetState() {
        attempts = 0;
        positions.clear();
        handlerAdded = false;
        moduleActive = true; // Re-enable the module like JS register
    }

    /**
     * Main tick handler to record player positions
     */
    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !isEnabled() || mc.thePlayer == null) return;

        // Ensure packet handlers are registered
        ensurePacketHandlers();

        // Record positions similar to JavaScript version
        Position pos = new Position(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
        positions.add(0, pos); // Add to beginning like JavaScript's push

        // Keep only most recent positions (JS uses slice(1) which keeps last n-1)
        while (positions.size() > 3) {
            positions.remove(positions.size() - 1);
        }
    }

    /**
     * Chat event handler to detect phase changes
     */
    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        String message = event.message.getUnformattedText();

        if (message.contains(" mounted a Cannon!")) {
            showFailSafe(true);
            resetState();
        } else if (message.contains("[NPC] Elle: It's time to build the Ballista again! Cover me!") ||
                   message.contains("build the Ballista")) {
            debug("Ballista message - resetting state");
            resetState();
        } else if (message.contains("[NPC] Elle: Okay adventurers, I will go and fish up Kuudra!") ||
                   message.contains("fish up Kuudra")) {
            debug("Phase start message - resetting state");
            resetState();
            moduleActive = true;
        }
    }

    /**
     * World unload handler
     */
    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        debug("World unload - resetting state");
        resetState();
    }

    /**
     * Ensure packet handlers are registered
     */
    private void ensurePacketHandlers() {
        if (handlerAdded) return;
        if (mc.getNetHandler() == null || mc.getNetHandler().getNetworkManager() == null) return;

        NetworkManager netManager = mc.getNetHandler().getNetworkManager();
        if (netManager.channel() == null) return;

        ChannelPipeline pipeline = netManager.channel().pipeline();
        if (pipeline.get(HANDLER_NAME) != null) {
            handlerAdded = true;
            return;
        }

        try {
            // Register inbound packet handler (S1B and S12 packets)
            pipeline.addBefore("packet_handler", HANDLER_NAME, new ChannelInboundHandlerAdapter() {
                @Override
                public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                    if (isEnabled() && msg instanceof Packet) {
                        handleIncomingPacket((Packet<?>) msg);
                    }
                    super.channelRead(ctx, msg);
                }
            });

            // Register outbound packet handler (C0B packets)
            pipeline.addBefore("packet_handler", HANDLER_NAME + "_out", new ChannelOutboundHandlerAdapter() {
                @Override
                public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                    if (isEnabled() && msg instanceof Packet) {
                        handleOutgoingPacket((Packet<?>) msg);
                    }
                    super.write(ctx, msg, promise);
                }
            });

            handlerAdded = true;
            debug("Packet handlers registered successfully");
        } catch (Exception e) {
            debug("Failed to inject packet handlers: " + e.getMessage());
        }
    }

    /**
     * Handle incoming packets (S1B, S12)
     */
    private void handleIncomingPacket(Packet<?> packet) {
        if (!isEnabled() || mc.thePlayer == null) return;

        // Handle S1BPacketEntityAttach (tentacle attach/detach)
        if (packet instanceof S1BPacketEntityAttach) {
            S1BPacketEntityAttach attachPacket = (S1BPacketEntityAttach) packet;
            int entityId = getEntityId(attachPacket);
            int vehicleId = getVehicleId(attachPacket);

            // Exact check like in JS: packet.func_149403_d() !== Player.getPlayer().func_145782_y()
            if (entityId != mc.thePlayer.getEntityId()) return;

            // Check !isSneaking like in JS
            if (!isSneaking) {
                debug("Skipping detach - not sneaking");
                return;
            }

            // Check attempts against max like in JS
            if (attempts >= getMaxAttempts()) {
                debug("Exceeded max attempts");
                return;
            }

            // Exact check like in JS: packet.func_149402_e() !== -1
            if (vehicleId != -1) {
                debug("Not a detach packet");
                return;
            }

            debug("Player detach packet detected");

            if (positions.isEmpty() || positions.get(0) == null) {
                debug("No position history available");
                return;
            }

            Position pos = positions.get(0);

            // The exact countermeasure sequence from JS
            mc.thePlayer.setPosition(pos.x, pos.y + 0.05, pos.z);

            mc.addScheduledTask(() -> {
                debug("Sending teleport packet");
                mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer.C06PacketPlayerPosLook(
                    pos.x, pos.y + 0.05, pos.z,
                    mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch,
                    mc.thePlayer.onGround
                ));
                mc.thePlayer.setPosition(pos.x, pos.y, pos.z);
            });

            attempts++;
            debug("Detach packet handled (attempts=" + attempts + ")");

            if (attempts == getMaxAttempts()) {
                showFailSafe(false);
            }
        }
        // Handle S12PacketEntityVelocity (tentacle pull velocity)
        else if (packet instanceof S12PacketEntityVelocity) {
            S12PacketEntityVelocity velocityPacket = (S12PacketEntityVelocity) packet;

            // Exact checks like in JS
            if (velocityPacket.getEntityID() != mc.thePlayer.getEntityId()) return;

            // Check JS: packet.func_149410_e() !== 10400
            if (velocityPacket.getMotionY() != 10400) return;

            debug("Neutralizing velocity Y=10400");

            // Cancel velocity by setting motion to 0
            mc.addScheduledTask(() -> {
                if (mc.thePlayer != null) {
                    mc.thePlayer.motionX = 0;
                    mc.thePlayer.motionY = 0;
                    mc.thePlayer.motionZ = 0;
                }
            });
        }
    }

    /**
     * Handle outgoing packets (C0B)
     */
    private void handleOutgoingPacket(Packet<?> packet) {
        if (!isEnabled() || mc.thePlayer == null) return;

        // Track sneaking state through C0B packets
        if (packet instanceof C0BPacketEntityAction) {
            C0BPacketEntityAction actionPacket = (C0BPacketEntityAction) packet;
            C0BPacketEntityAction.Action action = actionPacket.getAction();

            if (action == C0BPacketEntityAction.Action.START_SNEAKING) {
                isSneaking = true;
                debug("Player started sneaking");
            }
            else if (action == C0BPacketEntityAction.Action.STOP_SNEAKING) {
                isSneaking = false;
                debug("Player stopped sneaking");
            }
        }
    }

    /**
     * Get entity ID from S1BPacketEntityAttach
     */
    private int getEntityId(S1BPacketEntityAttach packet) {
        try {
            // Match exact JS function name: packet.func_149403_d()
            Method method = S1BPacketEntityAttach.class.getMethod("func_149403_d");
            return (int) method.invoke(packet);
        } catch (Exception e) {
            try {
                Method method = S1BPacketEntityAttach.class.getMethod("getEntityId");
                return (int) method.invoke(packet);
            } catch (Exception e2) {
                try {
                    Field field = S1BPacketEntityAttach.class.getDeclaredField("entityId");
                    field.setAccessible(true);
                    return (int) field.get(packet);
                } catch (Exception e3) {
                    debug("Failed to get entity ID: " + e3.getMessage());
                    return -1;
                }
            }
        }
    }

    /**
     * Get vehicle ID from S1BPacketEntityAttach
     */
    private int getVehicleId(S1BPacketEntityAttach packet) {
        try {
            // Match exact JS function name: packet.func_149402_e()
            Method method = S1BPacketEntityAttach.class.getMethod("func_149402_e");
            return (int) method.invoke(packet);
        } catch (Exception e) {
            try {
                Method method = S1BPacketEntityAttach.class.getMethod("getVehicleEntityId");
                return (int) method.invoke(packet);
            } catch (Exception e2) {
                try {
                    Field field = S1BPacketEntityAttach.class.getDeclaredField("vehicleEntityId");
                    field.setAccessible(true);
                    return (int) field.get(packet);
                } catch (Exception e3) {
                    try {
                        Field field = S1BPacketEntityAttach.class.getDeclaredField("field_149406_e");
                        field.setAccessible(true);
                        return (int) field.get(packet);
                    } catch (Exception e4) {
                        debug("Failed to get vehicle ID: " + e4.getMessage());
                        return -2;
                    }
                }
            }
        }
    }
}
