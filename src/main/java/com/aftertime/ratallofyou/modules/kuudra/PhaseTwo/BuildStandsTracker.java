package com.aftertime.ratallofyou.modules.kuudra.PhaseTwo;

import com.aftertime.ratallofyou.utils.KuudraUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.List;

public class BuildStandsTracker {
    private final Minecraft mc = Minecraft.getMinecraft();

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (mc.theWorld == null) return;

        int newBuild = KuudraUtils.getBuild();
        int newBuilders = KuudraUtils.getBuilders();

        List<KuudraUtils.StandInfo> piles = new ArrayList<>();
        boolean foundProgress = false;

        for (Entity e : mc.theWorld.loadedEntityList) {
            String name = KuudraUtils.getArmorStandName(e);
            if (name == null) continue;

            String lower = name.toLowerCase();
            if (!lower.contains("progress")) continue;

            // Building Progress line
            if (name.contains("Building Progress")) {
                foundProgress = true;
                // Example: "Building Progress 2% (0 Players Helping)"
                int pctIdx = name.indexOf('%');
                if (pctIdx > 0) {
                    int i = pctIdx - 1;
                    while (i >= 0 && Character.isDigit(name.charAt(i))) i--;
                    String digits = name.substring(i + 1, pctIdx);
                    try { newBuild = Integer.parseInt(digits); } catch (Exception ignored) {}
                }
                int l = name.indexOf('(');
                int r = name.indexOf(')');
                if (l >= 0 && r > l) {
                    String inside = name.substring(l + 1, r);
                    int space = inside.indexOf(' ');
                    if (space > 0) {
                        String num = inside.substring(0, space).replaceAll("\\D", "");
                        if (!num.isEmpty()) {
                            try { newBuilders = Integer.parseInt(num); } catch (Exception ignored) {}
                        }
                    }
                }
            }

            // Progress stands for piles
            if (name.contains("PROGRESS: ") && name.contains("%")) {
                piles.add(new KuudraUtils.StandInfo(e.posX, e.posY, e.posZ, name));
            }
        }

        if (foundProgress) {
            // If we detect the building progress stand, we can safely assume Phase 2
            KuudraUtils.setPhase(2);
        }

        KuudraUtils.setBuild(newBuild);
        KuudraUtils.setBuilders(newBuilders);
        KuudraUtils.setBuildPilesStands(piles);
        // freshLeft gets updated centrally in KuudraUtils tick
    }
}
