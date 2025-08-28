package com.aftertime.ratallofyou.modules.kuudra.PhaseOne;

import com.aftertime.ratallofyou.UI.config.ConfigData.AllConfig;
import com.aftertime.ratallofyou.UI.config.ConfigData.ModuleInfo;
import com.aftertime.ratallofyou.utils.KuudraUtils;
import com.aftertime.ratallofyou.utils.RenderUtils;
import com.aftertime.ratallofyou.utils.RenderUtils.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.monster.EntityGiantZombie;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * CrateBeaconBeam: renders beacon beams for Kuudra crates and missing supplies during Phase 1.
 * - Draws cyan beams above Kuudra crates (derived from Giant Zombie position and yaw offset).
 * - Draws white beams at supply locations; turns red for the supply called out as missing in party chat.
 */
public class CrateBeaconBeam {
    private static final Minecraft mc = Minecraft.getMinecraft();

    // Supply coordinates order must match missing-name mapping
    // Indices: 0 Shop, 1 Equals, 2 X Cannon, 3 X, 4 Triangle, 5 Slash
    private static final int[][] SUPPLY_COORDS = new int[][]{
            {-98, -112},
            {-98, -99},
            {-110, -106},
            {-106, -112},
            {-94, -106},
            {-106, -99}
    };

    // Beam parameters
    private static final float SUPPLY_BEAM_Y = 79f;
    private static final float SUPPLY_BEAM_HEIGHT = 100f;
    private static final float CRATE_BEAM_Y = 75f;
    private static final float CRATE_BEAM_HEIGHT = 100f;

    // State
    private String missingSupplyName = ""; // e.g., "Shop", "X Cannon", etc.
    private final List<Vec3> cratePositions = new ArrayList<>();
    private long lastCrateUpdateMs = 0L;

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        try {
            if (!isPhase1InKuudra() || !isModuleEnabled()) return;
            String msg = event.message.getUnformattedText();
            // Expect formats like: "Party > name: No Shop!" or "Party > name: No X Cannon!"
            String lower = msg.toLowerCase(Locale.ROOT);
            if (lower.contains("party >") && lower.contains(": no ") && msg.endsWith("!")) {
                int idx = lower.indexOf(": no ");
                if (idx != -1) {
                    String extract = msg.substring(idx + 5).trim(); // after ": no "
                    if (extract.endsWith("!")) extract = extract.substring(0, extract.length() - 1);
                    // Normalize common names to our canonical set
                    String norm = KuudraUtils.normalizeSupplyName(extract);
                    if (!norm.isEmpty()) missingSupplyName = norm;
                }
            }
        } catch (Exception ignored) { }
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        missingSupplyName = "";
        cratePositions.clear();
        lastCrateUpdateMs = 0L;
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
        if (!isPhase1InKuudra()) return;
        if (mc.theWorld == null || mc.thePlayer == null) return;

        // 1) Update crate positions at most every 100ms to reduce cost
        long now = System.currentTimeMillis();
        if (now - lastCrateUpdateMs > 100) {
            updateCratePositions(event.partialTicks);
            lastCrateUpdateMs = now;
        }

        // 2) Render supply beams (white or red if matches chat-missing)
        for (int i = 0; i < SUPPLY_COORDS.length; i++) {
            int x = SUPPLY_COORDS[i][0];
            int z = SUPPLY_COORDS[i][1];
            if (!KuudraUtils.suppliesReceivedNear(x + 0.5, z + 0.5, 3.0)) {
                Color color = getSupplyColorForIndex(i);
                RenderUtils.renderBeaconBeam(new Vec3(x + 0.5, SUPPLY_BEAM_Y, z + 0.5), color, true, SUPPLY_BEAM_HEIGHT, event.partialTicks);
            }
        }

        // 3) Render crate beams (cyan)
        Color cyan = new Color(0, 0, 0, 204);
        for (Vec3 pos : cratePositions) {
            RenderUtils.renderBeaconBeam(new Vec3(pos.xCoord, CRATE_BEAM_Y, pos.zCoord), cyan, true, CRATE_BEAM_HEIGHT, event.partialTicks);
        }
    }

    private void updateCratePositions(float partialTicks) {
        cratePositions.clear();
        if (mc.theWorld == null) return;

        for (Object o : mc.theWorld.loadedEntityList) {
            if (!(o instanceof EntityGiantZombie)) continue;
            EntityGiantZombie giant = (EntityGiantZombie) o;
            if (!KuudraUtils.isKuudraCrateGiant(giant)) continue;
            Vec3 pos = KuudraUtils.cratePosInterpolated(giant, partialTicks);
            cratePositions.add(pos);
        }
    }

    private Color getSupplyColorForIndex(int index) {
        String name = nameForSupplyIndex(index);
        boolean highlightRed = !missingSupplyName.isEmpty() && KuudraUtils.normalizeSupplyName(missingSupplyName).equals(KuudraUtils.normalizeSupplyName(name));
        return highlightRed ? new Color(255, 0, 0, 255) : new Color(255, 255, 255, 204);
    }

    private static String nameForSupplyIndex(int idx) {
        switch (idx) {
            case 0: return "Shop";
            case 1: return "Equals";
            case 2: return "X Cannon";
            case 3: return "X";
            case 4: return "Triangle";
            case 5: return "Slash";
            default: return "";
        }
    }

    private boolean isPhase1InKuudra() {
        return KuudraUtils.isPhase(1) && KuudraUtils.isInKuudraHollow();
    }

    private boolean isModuleEnabled() {
        ModuleInfo cfg = (ModuleInfo) AllConfig.INSTANCE.MODULES.get("kuudra_cratebeam");
        return cfg != null && Boolean.TRUE.equals(cfg.Data);
    }
}
