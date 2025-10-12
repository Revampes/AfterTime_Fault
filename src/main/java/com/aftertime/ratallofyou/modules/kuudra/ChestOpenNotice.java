package com.aftertime.ratallofyou.modules.kuudra;

import com.aftertime.ratallofyou.config.ModConfig;
import com.aftertime.ratallofyou.utils.KuudraUtils;
import com.aftertime.ratallofyou.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChestOpenNotice {
    public static boolean ChestOpened = false;
    public static int playOpened = 0;

    // Auto-open state
    private long paidChestSeenAtMs = 0L;
    private boolean pendingAutoOpen = false;
    private long lastAnnounceMs = 0L;
    // Ensure we only announce once per seen Paid Chest
    private boolean announcedForThisChest = false;

    private static final Minecraft mc = Minecraft.getMinecraft();

    // Dynamic pattern for party announcement, depends on user tag
    private Pattern partyLootedPattern = null;
    private String patternForTag = null;

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        if (!isModuleEnabled()) return;
        ChestOpened = false;
        playOpened = 0;
        pendingAutoOpen = false;
        paidChestSeenAtMs = 0L;
        lastAnnounceMs = 0L;
        announcedForThisChest = false;
        partyLootedPattern = null; patternForTag = null;
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        if (!isModuleEnabled() || !isInKuudraHollow()) return;
        ChestOpened = false;
        playOpened = 0;
        pendingAutoOpen = false;
        paidChestSeenAtMs = 0L;
        lastAnnounceMs = 0L;
        announcedForThisChest = false;
        partyLootedPattern = null; patternForTag = null;
    }

    private boolean isInKuudraHollow() {
        // Prefer KuudraUtils helper if available
        try {
            return KuudraUtils.isInKuudraHollow();
        } catch (Throwable t) {
            List<String> lines = Utils.getSidebarLines();
            if (lines == null || lines.isEmpty()) return false;
            for (String line : lines) {
                String l = line.toLowerCase(Locale.ROOT);
                if (l.contains("kuudra") && (l.contains("hollow") || l.contains("kuudra's"))) {
                    return true;
                }
                if (Utils.containedByCharSequence(l, "kuudra hollow")) return true;
            }
            return false;
        }
    }

    public boolean isModuleEnabled() {
        return ModConfig.enableKuudraChestOpenNotice;
    }

    private boolean isAutoOpenEnabled() {
        return ModConfig.kuudraAutoOpenChest;
    }

    private boolean isAutoRequeueEnabled() {
        return ModConfig.kuudraAutoRequeue;
    }

    private String getChestTag() {
        String t = ModConfig.kuudraChestTag;
        if (t == null) return "IQ";
        String trimmed = t.trim();
        return trimmed.isEmpty() ? "IQ" : trimmed;
    }

    private String buildAnnounceMessage() {
        return "/pc [" + getChestTag() + "] Chest Looted";
    }

    private Pattern getPartyLootedPattern() {
        String tag = getChestTag();
        if (partyLootedPattern == null || patternForTag == null || !patternForTag.equals(tag)) {
            String regex = "^Party > .*? ?([^:]+): \\[" + Pattern.quote(tag) + "\\] Chest Looted$";
            partyLootedPattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
            patternForTag = tag;
        }
        return partyLootedPattern;
    }

    private void tryAnnounceLooted() {
        if (!isModuleEnabled() || !isInKuudraHollow()) return;
        long now = System.currentTimeMillis();
        if (announcedForThisChest) return;
        // Minimal dedupe as extra safety
        if (now - lastAnnounceMs < 500) return;
        announcedForThisChest = true;
        lastAnnounceMs = now;
        sendChatCommand(buildAnnounceMessage());
    }

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        if (!isModuleEnabled() || !isInKuudraHollow()) return;
        GuiScreen gui = event.gui;
        if (!(gui instanceof GuiChest)) return;

        try {
            Container container = ((GuiChest) gui).inventorySlots;
            if (container instanceof ContainerChest) {
                String name = ((ContainerChest) container).getLowerChestInventory().getDisplayName().getUnformattedText();
                if ("Paid Chest".equalsIgnoreCase(name)) {
                    paidChestSeenAtMs = System.currentTimeMillis();
                    pendingAutoOpen = true;
                    // New chest seen, allow one announcement
                    announcedForThisChest = false;
                }
            }
        } catch (Throwable ignored) { }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!isModuleEnabled() || !isInKuudraHollow()) return;
        if (!pendingAutoOpen || !isAutoOpenEnabled()) return;
        if (!(mc.currentScreen instanceof GuiChest)) return;

        // Wait ~250ms then click slot 31 and close
        long now = System.currentTimeMillis();
        if (now - paidChestSeenAtMs < 250) return;

        try {
            GuiChest gui = (GuiChest) mc.currentScreen;
            Container container = gui.inventorySlots;
            if (!(container instanceof ContainerChest)) {
                pendingAutoOpen = false;
                return;
            }
            String name = ((ContainerChest) container).getLowerChestInventory().getDisplayName().getUnformattedText();
            if (!"Paid Chest".equalsIgnoreCase(name)) {
                // Different chest, cancel
                pendingAutoOpen = false;
                return;
            }

            int windowId = container.windowId;
            int slot = 31;
            // Left click, pickup mode
            mc.playerController.windowClick(windowId, slot, 0, 0, mc.thePlayer);
            // Mark that we opened the chest
            ChestOpened = true;
            // Close GUI
            mc.thePlayer.closeScreen();

            // Optional echo to party slightly after, but only once per chest
            mc.addScheduledTask(this::tryAnnounceLooted);
        } catch (Throwable t) {
            // Fail safe: cancel pending to avoid spamming
        } finally {
            pendingAutoOpen = false;
        }
    }

    @SubscribeEvent
    public void onSound(PlaySoundEvent event) {
        if (!isModuleEnabled() || !isInKuudraHollow()) return;
        try {
            if (event == null || event.result == null) return;
            String name = event.name; // In 1.8.9 this is the sound name key
            float vol = event.result.getVolume();
            if ("fireworks.blast".equalsIgnoreCase(name) && Math.abs(vol - 20.0f) < 0.01f) {
                long now = System.currentTimeMillis();
                // Only announce if we saw the Paid Chest recently (covers both auto and manual opens)
                if (now - paidChestSeenAtMs < 10_000L) {
                    tryAnnounceLooted();
                }
            }
        } catch (Throwable ignored) {}
    }

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        if (!isModuleEnabled() || !isInKuudraHollow()) return;
        String msg = event.message.getUnformattedText();
        if (msg == null) return;

        Matcher m = getPartyLootedPattern().matcher(msg);
        if (!m.find()) return;

        String user = m.group(1);
        playOpened++;

        if (playOpened < 4) {
            sendClientMessage("\u00a75" + user + " \u00a78(\u00a77" + playOpened + "\u00a78/\u00a774\u00a78)");
        } else if (playOpened == 4) {
            sendClientMessage("\u00a75" + user + " \u00a78(\u00a7a" + playOpened + "\u00a78/\u00a7a4\u00a78)");
            if (isAutoRequeueEnabled()) sendChatCommand("/instancerequeue");
        }
    }

    private void sendChatCommand(String cmdWithSlash) {
        if (mc.thePlayer == null) return;
        mc.thePlayer.sendChatMessage(cmdWithSlash);
    }

    private void sendClientMessage(String text) {
        if (mc.thePlayer == null) return;
        mc.thePlayer.addChatMessage(new ChatComponentText(text));
    }
}
