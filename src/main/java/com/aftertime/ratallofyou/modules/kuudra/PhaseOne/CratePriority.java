package com.aftertime.ratallofyou.modules.kuudra.PhaseOne;

import com.aftertime.ratallofyou.UI.config.ConfigData.AllConfig;
import com.aftertime.ratallofyou.UI.config.ConfigData.ModuleInfo;
import com.aftertime.ratallofyou.utils.KuudraUtils;
import com.aftertime.ratallofyou.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.List;
import java.util.Locale;

public class CratePriority {
    private static final Minecraft mc = Minecraft.getMinecraft();

    // Reference points copied from CheckNoPre for reliable pre-spot detection
    private static final double[] TRIANGLE = new double[]{-67.5, 77, -122.5};
    private static final double[] X = new double[]{-142.5, 77, -151};
    private static final double[] EQUALS = new double[]{-65.5, 76, -87.5};
    private static final double[] SLASH = new double[]{-113.5, 77, -68.5};

    // Debounce title spam
    private String lastShownKey = ""; // key = type:details
    private long lastShownAt = 0L;

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        try {
            if (!isEnabledPhase1()) return;
            String msg = event.message.getUnformattedText();
            if (msg == null || msg.isEmpty()) return;

            // 1) Try to extract explicit "No <spot>" first
            String missing = extractMissingFromNoCall(msg);
            if (missing.isEmpty()) {
                // 2) Try to infer missing via synonyms without "no"
                missing = extractMissingFromSynonyms(msg);
            }

            if (!missing.isEmpty() && isHandledMissing(missing)) {
                String pre = detectPreSpot();
                String keyPre = missing + ":" + (pre == null ? "unknown" : pre);
                if (keyPre.equals(lastShownKey) && System.currentTimeMillis() - lastShownAt < 3000) return;

                String instruction = mapInstruction(missing, pre);
                if (instruction != null && !instruction.isEmpty()) {
                    showTitle(instruction);
                    lastShownKey = keyPre; lastShownAt = System.currentTimeMillis();
                }
                return;
            }

            // 3) If no missing found, check action-only keywords like "square" or "xcannon"
            String action = extractActionShortcut(msg);
            if (!action.isEmpty()) {
                String key = "action:" + action;
                if (key.equals(lastShownKey) && System.currentTimeMillis() - lastShownAt < 3000) return;
                if ("Square".equals(action)) showTitle(colorize("Go Square"));
                else if ("X Cannon".equals(action)) showTitle(colorize("Go XCannon"));
                lastShownKey = key; lastShownAt = System.currentTimeMillis();
            }
        } catch (Exception ignored) { }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        // Clear debounce when leaving area or phase to avoid stale suppression across runs
        if (event.phase != TickEvent.Phase.END) return;
        if (!isEnabledPhase1()) {
            lastShownKey = ""; lastShownAt = 0L;
        }
    }

    private String detectPreSpot() {
        if (mc.thePlayer == null) return null;
        if (distanceToPlayer(TRIANGLE) < 15) return "Triangle";
        if (distanceToPlayer(X) < 30) return "X";
        if (distanceToPlayer(EQUALS) < 15) return "Equals";
        if (distanceToPlayer(SLASH) < 15) return "Slash";
        return null;
    }

    private double distanceToPlayer(double[] loc) {
        if (mc.thePlayer == null || loc == null) return Double.MAX_VALUE;
        double dx = mc.thePlayer.posX - loc[0];
        double dy = mc.thePlayer.posY - loc[1];
        double dz = mc.thePlayer.posZ - loc[2];
        return Math.sqrt(dx*dx + dy*dy + dz*dz);
    }

    private boolean isEnabledPhase1() {
        return isModuleEnabled() && KuudraUtils.isPhase(1) && KuudraUtils.isInKuudraHollow();
    }

    private boolean isModuleEnabled() {
        ModuleInfo cfg = (ModuleInfo) AllConfig.INSTANCE.MODULES.get("kuudra_cratepriority");
        return cfg != null && Boolean.TRUE.equals(cfg.Data);
    }

    private void showTitle(String title) {
        try {
            if (mc.ingameGUI != null) {
                // Show at least ~3 seconds (60 ticks)
                mc.ingameGUI.displayTitle(title, "", 5, 60, 5);
            }
        } catch (Exception ignored) { }
    }

    // Mapping logic (concise): remove pre labels
    private String mapInstruction(String missing, String pre) {
        String p = pre == null ? "" : pre.toLowerCase(Locale.ROOT);
        switch (missing) {
            case "Slash":
                if (p.equals("slash")) return colorize("Pull square, grab shop");
                if (p.equals("x")) return colorize("Go XCannon");
                if (p.equals("triangle")) return colorize("Go Square");
                if (p.equals("equals")) return colorize("Go XCannon");
                break;
            case "X":
                if (p.equals("x")) return colorize("Pull square, grab shop");
                if (p.equals("slash")) return colorize("Go Square");
                if (p.equals("triangle")) return colorize("Go XCannon");
                if (p.equals("equals")) return colorize("Go XCannon");
                break;
            case "Triangle":
                if (p.equals("triangle")) return colorize("Pull square, grab shop");
                if (p.equals("x")) return colorize("Go XCannon");
                if (p.equals("slash")) return colorize("Go Square");
                if (p.equals("equals")) return colorize("Go XCannon");
                break;
            case "Equals":
                if (p.equals("equals")) return colorize("Pull square, grab shop");
                if (p.equals("x")) return colorize("Go XCannon");
                if (p.equals("slash")) return colorize("Go Square");
                if (p.equals("triangle")) return colorize("Go XCannon");
                break;
            case "Shop":
                if (p.equals("slash")) return colorize("Go Square");
                if (p.equals("x")) return colorize("Go XCannon");
                if (p.equals("triangle")) return colorize("Go XCannon");
                if (p.equals("equals")) return colorize("Go Square");
                break;
        }
        // Fallback if pre unknown: suggest generic best action per missing
        switch (missing) {
            case "Slash":
            case "Equals":
                return colorize("Go XCannon");
            case "X":
            case "Triangle":
                return colorize("Go Square");
            case "Shop":
                return colorize("Go XCannon");
            default:
                return null;
        }
    }

    private String colorize(String s) {
        return EnumChatFormatting.GREEN + s;
    }

    // --- Parsing helpers ---

    private String extractMissingFromNoCall(String msg) {
        String lower = msg.toLowerCase(Locale.ROOT).trim();
        int idx = lower.indexOf(": no ");
        String raw = null;
        if (idx != -1) {
            raw = msg.substring(idx + 5).trim();
        } else {
            int p = lower.indexOf("no ");
            if (p != -1) raw = msg.substring(p + 3).trim();
        }
        if (raw == null) return "";
        if (raw.endsWith("!")) raw = raw.substring(0, raw.length() - 1);
        String norm = KuudraUtils.normalizeSupplyName(raw);
        return isHandledMissing(norm) ? norm : "";
    }

    private String extractMissingFromSynonyms(String msg) {
        String lower = msg.toLowerCase(Locale.ROOT);
        // Short direct tokens like "tri", "=", "/"; prefer exact or short messages
        if (containsToken(lower, "triangle") || containsToken(lower, "tri")) return "Triangle";
        if (containsToken(lower, "shop")) return "Shop";
        if (containsToken(lower, "equals") || containsToken(lower, "equal") || equalsTrim(lower, "=")) return "Equals";
        if (containsToken(lower, "slash") || equalsTrim(lower, "/")) return "Slash";
        if (containsToken(lower, "cross") || containsToken(lower, "x")) return "X";
        return "";
    }

    private String extractActionShortcut(String msg) {
        String lower = msg.toLowerCase(Locale.ROOT);
        if (containsToken(lower, "square")) return "Square";
        if (containsToken(lower, "x cannon") || containsToken(lower, "xcannon") || containsToken(lower, "xc")) return "X Cannon";
        return "";
    }

    private boolean containsToken(String msgLower, String token) {
        if (msgLower == null || token == null) return false;
        String t = token.toLowerCase(Locale.ROOT);
        if (t.equals("/") || t.equals("=")) {
            return equalsTrim(msgLower, t);
        }
        // normalize msg: keep letters and digits as words
        String norm = msgLower.replaceAll("[^a-z0-9]+", " ");
        String wrapped = " " + norm + " ";
        String tw = " " + t + " ";
        return wrapped.contains(tw);
    }

    private boolean equalsTrim(String msgLower, String token) {
        if (msgLower == null || token == null) return false;
        String trimmed = msgLower.trim();
        // allow trailing punctuation '!' or '.'
        if (trimmed.endsWith("!") || trimmed.endsWith(".")) trimmed = trimmed.substring(0, trimmed.length() - 1);
        return trimmed.equals(token);
    }

    private boolean isHandledMissing(String name) {
        if (name == null) return false;
        switch (name) {
            case "Slash":
            case "X":
            case "Triangle":
            case "Equals":
            case "Shop":
                return true;
            default:
                return false;
        }
    }
}
