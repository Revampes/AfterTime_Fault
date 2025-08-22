package com.aftertime.ratallofyou.modules.SkyBlock;

import com.aftertime.ratallofyou.UI.config.ConfigData.AllConfig;
import com.aftertime.ratallofyou.UI.config.ConfigData.ModuleInfo;
import com.aftertime.ratallofyou.utils.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WaypointGrab {
    // Regex to capture: Party > [RANK] Name: x: <int>, y: <int>, z: <int>
    private static final Pattern PARTY_COORDS = Pattern.compile(
            "^Party > (?:\\[[^]]*?])? ?(\\w{1,16})(?: [ቾ⚒])?: x: (-?\\d+), y: (-?\\d+), z: (-?\\d+).*");

    private static final long LIFETIME_MS = 20_000L; // 20 seconds
    private static final float BEAM_HEIGHT = 256f;    // world height for 1.8.9

    private static final List<Beam> beams = new CopyOnWriteArrayList<>();

    private static class Beam {
        final Vec3 pos;
        final RenderUtils.Color color;
        final float height;
        volatile long expiresAt;

        Beam(Vec3 pos, RenderUtils.Color color, float height, long expiresAt) {
            this.pos = pos;
            this.color = color;
            this.height = height;
            this.expiresAt = expiresAt;
        }
    }

    private static void addOrRefreshBeam(double x, double y, double z) {
        // Center to block middle for nicer visuals
        Vec3 pos = new Vec3(x + 0.5, y, z + 0.5);
        long expiry = System.currentTimeMillis() + LIFETIME_MS;

        // If a beam already exists at same integer coords, refresh it
        for (Beam b : beams) {
            if (Math.floor(b.pos.xCoord) == Math.floor(x)
                    && Math.floor(b.pos.yCoord) == Math.floor(y)
                    && Math.floor(b.pos.zCoord) == Math.floor(z)) {
                b.expiresAt = expiry;
                return;
            }
        }

        // Default color: gold-yellow
        RenderUtils.Color color = new RenderUtils.Color(255, 200, 0, 255);
        beams.add(new Beam(pos, color, BEAM_HEIGHT, expiry));
    }

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        if (event == null || event.message == null) return;
        String msg = event.message.getUnformattedText();
        if (msg == null || msg.isEmpty()) return;

        Matcher m = PARTY_COORDS.matcher(msg);
        if (!m.matches()) return;

        // String name = m.group(1); // currently unused, but kept in case of future labeling
        try {
            int x = Integer.parseInt(m.group(2));
            int y = Integer.parseInt(m.group(3));
            int z = Integer.parseInt(m.group(4));
            addOrRefreshBeam(x, y, z);
        } catch (NumberFormatException ignored) {
        }
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
        if (Minecraft.getMinecraft().theWorld == null || !isModuleEnabled()) return;
        if (beams.isEmpty()) return;

        long now = System.currentTimeMillis();

        // Render active beams
        for (Beam b : beams) {
            if (now < b.expiresAt) {
                // depthCheck=false makes it visible through walls
                RenderUtils.renderBeaconBeam(b.pos, b.color, false, b.height, event.partialTicks);
            }
        }
        // Then clean up expired beams safely
        beams.removeIf(b -> now >= b.expiresAt);
    }

    private static boolean isModuleEnabled() {
        ModuleInfo cfg = (ModuleInfo) AllConfig.INSTANCE.MODULES.get("skyblock-waypointgrab");
        return cfg != null && Boolean.TRUE.equals(cfg.Data);
    }
}
