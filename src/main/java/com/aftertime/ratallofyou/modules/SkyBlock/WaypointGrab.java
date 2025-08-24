package com.aftertime.ratallofyou.modules.SkyBlock;

import com.aftertime.ratallofyou.UI.config.ConfigData.AllConfig;
import com.aftertime.ratallofyou.UI.config.ConfigData.ModuleInfo;
import com.aftertime.ratallofyou.utils.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.List;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WaypointGrab {
    // Regex to capture party coordinate messages in a tolerant way.
    // Accept integers or decimals for x/y/z
    private static final Pattern PARTY_COORDS = Pattern.compile(
            "(?i)^\\s*Party\\s*>\\s*(?:\\[[^]]*])?\\s*([^:]{1,32})\\s*:\\s*x\\s*[:=]\\s*(-?\\d+(?:\\.\\d+)?)\\s*,?\\s*y\\s*[:=]\\s*(-?\\d+(?:\\.\\d+)?)\\s*,?\\s*z\\s*[:=]\\s*(-?\\d+(?:\\.\\d+)?).*");
    // Extract sender name from any Party message line
    private static final Pattern PARTY_NAME = Pattern.compile("(?i)^\\s*Party\\s*>\\s*(?:\\[[^]]*])?\\s*([^:]{1,32})\\s*:");
    // Generic and key-value coord patterns (allow decimals)
    private static final Pattern GENERIC_COORDS = Pattern.compile(
            "(?i)x\\s*[:=]\\s*(-?\\d+(?:\\.\\d+)?)\\s*[ ,;]+\\s*y\\s*[:=]\\s*(-?\\d+(?:\\.\\d+)?)\\s*[ ,;]+\\s*z\\s*[:=]\\s*(-?\\d+(?:\\.\\d+)?)");
    private static final Pattern KV_COORDS = Pattern.compile(
            "(?i)(?:^|[\\s\\[,;])([xyz])\\s*[:=]\\s*(-?\\d+(?:\\.\\d+)?)(?=$|[\\s\\],;])");

    private static final long LIFETIME_MS = 20_000L; // 20 seconds
    private static final float BEAM_HEIGHT = 256f;    // world height for 1.8.9

    private static final List<Beam> beams = new CopyOnWriteArrayList<>();

    private static class Beam {
        final Vec3 pos;
        final RenderUtils.Color color;
        final float height;
        final int rx, ry, rz;      // rounded coords for uniqueness/placement
        final String sender;        // sender name (nullable)
        final String label;         // original coord label text
        volatile long expiresAt;

        Beam(Vec3 pos, RenderUtils.Color color, float height, int rx, int ry, int rz, String sender, String label, long expiresAt) {
            this.pos = pos;
            this.color = color;
            this.height = height;
            this.rx = rx;
            this.ry = ry;
            this.rz = rz;
            this.sender = sender;
            this.label = label;
            this.expiresAt = expiresAt;
        }
    }

    private static void addOrRefreshBeam(double x, double y, double z, String sender, String label) {
        // Round to nearest block for placement; center on block
        int rx = (int) Math.round(x);
        int ry = (int) Math.round(y);
        int rz = (int) Math.round(z);
        Vec3 pos = new Vec3(rx + 0.5, ry, rz + 0.5);
        long expiry = System.currentTimeMillis() + LIFETIME_MS;

        for (Beam b : beams) {
            if (b.rx == rx && b.ry == ry && b.rz == rz) {
                b.expiresAt = expiry;
                return;
            }
        }

        RenderUtils.Color color = new RenderUtils.Color(255, 200, 0, 255);
        beams.add(new Beam(pos, color, BEAM_HEIGHT, rx, ry, rz, sender, label, expiry));
    }

    private static String extractPartySender(String msg) {
        Matcher n = PARTY_NAME.matcher(msg);
        return n.find() ? n.group(1).trim() : null;
    }

    private static String fmt(double d) {
        long r = Math.round(d);
        if (Math.abs(d - r) < 1e-6) return Long.toString(r);
        String s = String.format(Locale.US, "%.2f", d);
        // strip trailing zeros and dot
        s = s.replaceAll("0+$", "").replaceAll("\\.$", "");
        return s;
    }

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        if (event == null || event.message == null || !isModuleEnabled()) return;
        String msg = event.message.getUnformattedText();
        if (msg == null || msg.isEmpty()) return;

        String sender = extractPartySender(msg);

        // 1) Order-agnostic parse: accept x/y/z in any order (decimals allowed)
        Matcher kv = KV_COORDS.matcher(msg);
        Map<Character, Double> vals = new HashMap<>();
        while (kv.find()) {
            char k = Character.toLowerCase(kv.group(1).charAt(0));
            try { vals.put(k, Double.parseDouble(kv.group(2))); } catch (NumberFormatException ignored) {}
        }
        if (vals.containsKey('x') && vals.containsKey('z')) {
            double dx = vals.get('x');
            double dz = vals.get('z');
            double dy = vals.containsKey('y') ? vals.get('y') : (Minecraft.getMinecraft().thePlayer != null ? Minecraft.getMinecraft().thePlayer.posY : 70.0);
            String label = "x:" + fmt(dx) + " y:" + fmt(dy) + " z:" + fmt(dz);
            addOrRefreshBeam(dx, dy, dz, sender, label);
            return;
        }

        // 2) Try party-formatted first (x,y,z order)
        Matcher m = PARTY_COORDS.matcher(msg);
        if (m.matches()) {
            try {
                double dx = Double.parseDouble(m.group(2));
                double dy = Double.parseDouble(m.group(3));
                double dz = Double.parseDouble(m.group(4));
                String nm = m.group(1) != null ? m.group(1).trim() : sender;
                String label = "x:" + fmt(dx) + " y:" + fmt(dy) + " z:" + fmt(dz);
                addOrRefreshBeam(dx, dy, dz, nm, label);
                return;
            } catch (NumberFormatException ignored) { }
        }

        // 3) Fallback: accept coords anywhere in the message in x,y,z order
        Matcher g = GENERIC_COORDS.matcher(msg);
        if (g.find()) {
            try {
                double dx = Double.parseDouble(g.group(1));
                double dy = Double.parseDouble(g.group(2));
                double dz = Double.parseDouble(g.group(3));
                String label = "x:" + fmt(dx) + " y:" + fmt(dy) + " z:" + fmt(dz);
                addOrRefreshBeam(dx, dy, dz, sender, label);
            } catch (NumberFormatException ignored) { }
        }
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        // Clear any active beams when switching worlds to avoid stale visuals
        beams.clear();
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
        if (!isModuleEnabled()) return;
        if (Minecraft.getMinecraft().theWorld == null) return;
        if (beams.isEmpty()) return;

        long now = System.currentTimeMillis();

        for (Beam b : beams) {
            if (now < b.expiresAt) {
                RenderUtils.renderBeaconBeam(b.pos, b.color, false, b.height, event.partialTicks);
                // Floating labels (sender and coords label)
                double x = b.pos.xCoord, y = b.pos.yCoord + 1.8, z = b.pos.zCoord;
                int white = 0xFFFFFFFF;
                if (b.sender != null && !b.sender.isEmpty()) {
                    RenderUtils.renderFloatingTextConstant(b.sender, x, y + 0.5, z, 0.03f, white, false);
                    RenderUtils.renderFloatingTextConstant(b.label, x, y - 0.2, z, 0.03f, white, false);
                } else {
                    RenderUtils.renderFloatingTextConstant(b.label, x, y, z, 0.03f, white, false);
                }
            }
        }
        beams.removeIf(b -> now >= b.expiresAt);
    }

    private static boolean isModuleEnabled() {
        ModuleInfo cfg = (ModuleInfo) AllConfig.INSTANCE.MODULES.get("skyblock_waypointgrab");
        return cfg != null && Boolean.TRUE.equals(cfg.Data);
    }
}
