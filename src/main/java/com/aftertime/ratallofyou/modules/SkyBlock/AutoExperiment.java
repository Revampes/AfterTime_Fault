package com.aftertime.ratallofyou.modules.SkyBlock;

import com.aftertime.ratallofyou.UI.config.ConfigData.AllConfig;
import com.aftertime.ratallofyou.UI.config.ConfigData.BaseConfig;
import com.aftertime.ratallofyou.UI.config.ConfigData.ModuleInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import java.util.*;

/**
 * AutoExperiment: ports Chronomatron and Ultrasequencer helper from temp code
 * and wires it into this mod's config system.
 */
public class AutoExperiment {
    private final Minecraft mc = Minecraft.getMinecraft();

    private ExperimentType current = ExperimentType.NONE;
    private boolean hasAdded = false;
    private int clicks = 0;
    private long lastClickTime = 0L;

    private final List<Map.Entry<Integer, String>> chronomatronOrder = new ArrayList<>(28);
    private int lastAddedSlot = -1;

    private final HashMap<Integer, Integer> ultrasequencerOrder = new HashMap<>();
    // New: UltraSequencer state-change gating
    private int ultraLastClickedSlot = -1;
    private ItemStack ultraLastStack = null;
    private boolean ultraWaitingForChange = false;

    // Chronomatron per-step gating
    private int chronoLastClickedSlot = -1;
    private ItemStack chronoLastStack = null;
    private boolean chronoWaitingForChange = false;
    private boolean chronoPlaybackArmed = false; // new: arm playback start and wait full delay
    private long chronoNextAllowedAt = 0L;
    private long ultraNextAllowedAt = 0L;

    private static final long WAIT_TIMEOUT_MS = 2000; // 2 seconds timeout for waiting state
    private long chronoWaitStart = 0L;
    private long ultraWaitStart = 0L;

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        resetState();
        if (!(event.gui instanceof GuiChest)) return;
        Container container = ((GuiChest) event.gui).inventorySlots;
        if (!(container instanceof ContainerChest)) return;
        String chestName = ((ContainerChest) container).getLowerChestInventory().getDisplayName().getUnformattedText();
        if (chestName == null) return;
        if (chestName.contains("Chronomatron") && !chestName.contains("Sta")) {
            current = ExperimentType.CHRONOMATRON;
        } else if (chestName.contains("Ultrasequencer") && !chestName.contains("Sta")) {
            current = ExperimentType.ULTRASEQUENCER;
        } else if (chestName.startsWith("Superpairs (")) {
            current = ExperimentType.SUPERPAIRS;
        }
    }

    @SubscribeEvent
    public void onGuiDraw(GuiScreenEvent.BackgroundDrawnEvent event) {
        if (!(event.gui instanceof GuiChest)) return;
        if (!isEnabled()) return;
        Container container = ((GuiChest) event.gui).inventorySlots;
        if (!(container instanceof ContainerChest)) return;
        List<Slot> invSlots = container.inventorySlots;

        switch (current) {
            case CHRONOMATRON:
                handleChronomatron(invSlots);
                break;
            case ULTRASEQUENCER:
                handleUltrasequencer(invSlots);
                break;
            default:
                break;
        }
    }

    private void handleChronomatron(List<Slot> invSlots) {
        debugSlots(invSlots, "Chrono");
        // Reset record gate between rounds: when indicator shows glass and last recorded slot is no longer enchanted
        if (isGlassOn49(invSlots) && lastAddedSlot >= 0 && lastAddedSlot < invSlots.size()) {
            Slot last = invSlots.get(lastAddedSlot);
            if (last == null || !isEnchanted(last)) {
                hasAdded = false;
                chronoPlaybackArmed = false; // memorizing again
                if (chronomatronOrder.size() > 11 && getAutoExit()) {
                    debug("Chrono auto-exit: size=" + chronomatronOrder.size());
                    closeScreen();
                }
            }
        }

        // Record one new highlighted item per memorize step when indicator shows clock
        if (!hasAdded && isClockOn49(invSlots)) {
            Optional<Slot> opt = invSlots.stream()
                    .filter(it -> it.slotNumber >= 10 && it.slotNumber <= 43)
                    .filter(this::isEnchanted)
                    .findFirst();
            if (opt.isPresent()) {
                Slot s = opt.get();
                chronomatronOrder.add(new AbstractMap.SimpleEntry<>(s.slotNumber, s.getStack().getDisplayName()));
                lastAddedSlot = s.slotNumber;
                hasAdded = true;
                clicks = 0; // prepare to replay from the beginning of the sequence
                // Reset playback arming; we'll arm when memorize visuals are gone
                chronoWaitingForChange = false;
                chronoPlaybackArmed = false;
                debug("Chrono record: slot=" + s.slotNumber + ", total=" + chronomatronOrder.size());
            }
        }

        // If waiting for a state change on the previously clicked slot, hold until it changes
        if (chronoWaitingForChange && chronoLastClickedSlot >= 0 && chronoLastClickedSlot < invSlots.size()) {
            Slot s = invSlots.get(chronoLastClickedSlot);
            ItemStack cur = (s != null) ? s.getStack() : null;
            boolean changed = !itemStacksEqual(chronoLastStack, cur);
            debug("Chrono waiting: slot=" + chronoLastClickedSlot + ", changed=" + changed + ", elapsed=" + (now() - chronoWaitStart));
            if (changed) {
                chronoWaitingForChange = false;
            } else if (now() - chronoWaitStart > WAIT_TIMEOUT_MS) {
                debug("Chrono wait timeout, forcing next click");
                chronoWaitingForChange = false;
            } else {
                return; // do not click again until GUI updates the slot or timeout
            }
        }

        // Enter playback: indicator shows clock, recorded exists, and no enchanted items are visible
        boolean readyForPlayback = isClockOn49(invSlots) && hasAdded && !anyEnchantedInRange(invSlots, 10, 43);
        if (readyForPlayback && clicks < chronomatronOrder.size()) {
            // Arm playback the first time we see ready state, then wait full delay
            if (!chronoPlaybackArmed) {
                chronoPlaybackArmed = true;
                int d = getDelayMs();
                chronoNextAllowedAt = now() + Math.max(0, d);
                lastClickTime = now();
                debug("Chrono armed; waiting delay=" + d + "ms");
                return; // wait until delay passes before first click
            }
            long now = now();
            if (now >= chronoNextAllowedAt) {
                int slotId = chronomatronOrder.get(clicks).getKey();
                // Capture pre-click state to wait for change
                if (slotId >= 0 && slotId < invSlots.size()) {
                    Slot target = invSlots.get(slotId);
                    chronoLastStack = (target != null) ? copyItemStackSafe(target.getStack()) : null;
                } else {
                    chronoLastStack = null;
                }
                chronoLastClickedSlot = slotId;
                debug("Chrono click: index=" + clicks + ", slot=" + slotId + ", waitOk");
                clickSlot(slotId);
                chronoWaitingForChange = true;
                chronoWaitStart = now;
                lastClickTime = now;
                chronoNextAllowedAt = now + Math.max(0, getDelayMs());
                clicks++;
            }
        } else if (!readyForPlayback) {
            // Lost playback readiness, disarm so we re-wait full delay next time
            chronoPlaybackArmed = false;
        }
    }

    private void handleUltrasequencer(List<Slot> invSlots) {
        debugSlots(invSlots, "Ultra");
        // If click phase indicator shows (clock), allow clicks and ensure memorize flag resets
        if (isClockOn49(invSlots)) {
            // Ensure we can rebuild mapping next time the memorize phase returns
            hasAdded = false;
        }

        // Build order during memorize when the indicator is a stained glass pane
        if (!hasAdded && isGlassOn49(invSlots)) {
            if (!getHasStackSafe(invSlots, 44)) return; // ensure grid present
            ultrasequencerOrder.clear();
            invSlots.stream()
                    .filter(it -> it.slotNumber >= 9 && it.slotNumber <= 44)
                    .forEach(this::setUltraSequencerOrder);
            hasAdded = true;
            clicks = 0;
            ultraLastClickedSlot = -1;
            ultraLastStack = null;
            ultraWaitingForChange = false;
            debug("Ultra record complete; size=" + ultrasequencerOrder.size());
            if (ultrasequencerOrder.size() > 9 && getAutoExit()) {
                debug("Ultra auto-exit: size=" + ultrasequencerOrder.size());
                closeScreen();
            }
            return;
        }

        // If waiting for a state change on the previously clicked slot, hold until it changes
        if (ultraWaitingForChange && ultraLastClickedSlot >= 0 && ultraLastClickedSlot < invSlots.size()) {
            Slot s = invSlots.get(ultraLastClickedSlot);
            ItemStack cur = (s != null) ? s.getStack() : null;
            boolean changed = !itemStacksEqual(ultraLastStack, cur);
            debug("Ultra waiting: slot=" + ultraLastClickedSlot + ", changed=" + changed + ", elapsed=" + (now() - ultraWaitStart));
            if (changed) {
                ultraWaitingForChange = false; // allow next click after the GUI updates
            } else if (now() - ultraWaitStart > WAIT_TIMEOUT_MS) {
                debug("Ultra wait timeout, forcing next click");
                ultraWaitingForChange = false;
            } else {
                return; // still same state, do not spam clicks
            }
        }

        // Playback: only click when the phase indicator is a clock
        if (isClockOn49(invSlots) && ultrasequencerOrder.containsKey(clicks)) {
            long now = now();
            if (now >= ultraNextAllowedAt) {
                Integer slotId = ultrasequencerOrder.get(clicks);
                if (slotId != null) {
                    if (slotId >= 0 && slotId < invSlots.size()) {
                        Slot target = invSlots.get(slotId);
                        ultraLastStack = (target != null) ? copyItemStackSafe(target.getStack()) : null;
                    } else {
                        ultraLastStack = null;
                    }
                    ultraLastClickedSlot = slotId;
                    debug("Ultra click: index=" + clicks + ", slot=" + slotId);
                    clickSlot(slotId);
                    ultraWaitingForChange = true;
                    ultraWaitStart = now;
                    lastClickTime = now;
                    ultraNextAllowedAt = now + Math.max(0, getDelayMs());
                    clicks++;
                }
            }
        }
    }

    private void setUltraSequencerOrder(Slot slot) {
        ItemStack st = slot.getStack();
        if (st == null) return;
        // Paper with stack size as index (1..N)
        if (st.getItem() == Items.paper && st.stackSize > 0) {
            ultrasequencerOrder.put(st.stackSize - 1, slot.slotNumber);
        }
    }

    // Click helpers and utilities
    private void clickSlot(int slotId) {
        if (mc.thePlayer == null || mc.theWorld == null) return;
        mc.playerController.windowClick(mc.thePlayer.openContainer.windowId, slotId, 0, 0, mc.thePlayer);
    }

    private void closeScreen() {
        if (mc.thePlayer != null) mc.thePlayer.closeScreen();
    }

    private boolean isClockOn49(List<Slot> list) {
        if (list == null || list.size() <= 49) return false;
        Slot s = list.get(49);
        ItemStack st = (s != null) ? s.getStack() : null;
        return st != null && st.getItem() == Items.clock;
    }

    private boolean isGlassOn49(List<Slot> list) {
        if (list == null || list.size() <= 49) return false;
        Slot s = list.get(49);
        ItemStack st = (s != null) ? s.getStack() : null;
        return st != null && st.getItem() == net.minecraft.item.Item.getItemFromBlock(net.minecraft.init.Blocks.stained_glass_pane);
    }

    private boolean getHasStackSafe(List<Slot> list, int idx) {
        if (idx < 0 || idx >= list.size()) return false;
        Slot s = list.get(idx);
        return s != null && s.getHasStack();
    }

    private boolean isEnchanted(Slot s) {
        return s != null && s.getStack() != null && s.getStack().isItemEnchanted();
    }

    private boolean anyEnchantedInRange(List<Slot> invSlots, int from, int to) {
        for (Slot s : invSlots) {
            if (s.slotNumber >= from && s.slotNumber <= to && isEnchanted(s)) return true;
        }
        return false;
    }

    // Safe shallow copy for comparison purposes
    private ItemStack copyItemStackSafe(ItemStack st) {
        if (st == null) return null;
        return st.copy();
    }

    private boolean itemStacksEqual(ItemStack a, ItemStack b) {
        if (a == b) return true;
        if (a == null || b == null) return false;
        boolean basic = a.getItem() == b.getItem() && a.getItemDamage() == b.getItemDamage() && a.stackSize == b.stackSize;
        boolean name = Objects.equals(a.getDisplayName(), b.getDisplayName());
        boolean nbt = Objects.equals(a.getTagCompound(), b.getTagCompound());
        debug("Comparing ItemStacks: basic=" + basic + ", name=" + name + ", nbt=" + nbt + ", a=" + (a != null ? a.getDisplayName() : "null") + ", b=" + (b != null ? b.getDisplayName() : "null"));
        return basic && name && nbt;
    }

    private void debugSlots(List<Slot> slots, String phase) {
        if (!getDebug()) return;
        StringBuilder sb = new StringBuilder("[" + phase + "] Slots: ");
        for (int i = 0; i < slots.size(); i++) {
            Slot s = slots.get(i);
            ItemStack st = (s != null) ? s.getStack() : null;
            if (st != null) {
                sb.append(i).append(":").append(st.getDisplayName()).append(",");
            }
        }
        debug(sb.toString());
    }

    private void resetState() {
        current = ExperimentType.NONE;
        hasAdded = false;
        chronomatronOrder.clear();
        lastAddedSlot = -1;
        ultrasequencerOrder.clear();
        // Do NOT reset clicks or lastClickTime here; GUI can reopen between steps and we must honor delay
        ultraLastClickedSlot = -1;
        ultraLastStack = null;
        ultraWaitingForChange = false;
        // Reset Chronomatron waiting state as well
        chronoLastClickedSlot = -1;
        chronoLastStack = null;
        chronoWaitingForChange = false;
        chronoPlaybackArmed = false;
        // Reset next-allowed timestamps
        int d = getDelayMs();
        long now = now();
        chronoNextAllowedAt = now + Math.max(0, d);
        ultraNextAllowedAt = now + Math.max(0, d);
    }

    private boolean isEnabled() {
        ModuleInfo cfg = (ModuleInfo) AllConfig.INSTANCE.MODULES.get("skyblock_autoexperiment");
        return cfg != null && Boolean.TRUE.equals(cfg.Data);
    }

    private boolean getAutoExit() {
        BaseConfig<?> cfg = AllConfig.INSTANCE.AUTOEXPERIMENT_CONFIGS.get("autoexperiment_auto_exit");
        if (cfg == null || cfg.Data == null) return false;
        Object v = cfg.Data;
        if (v instanceof Boolean) return (Boolean) v;
        if (v instanceof String) return Boolean.parseBoolean(((String) v).trim());
        return false;
    }

    private boolean getDebug() {
        BaseConfig<?> cfg = AllConfig.INSTANCE.AUTOEXPERIMENT_CONFIGS.get("autoexperiment_debug");
        if (cfg == null || cfg.Data == null) return false;
        Object v = cfg.Data;
        if (v instanceof Boolean) return (Boolean) v;
        if (v instanceof String) return Boolean.parseBoolean(((String) v).trim());
        return false;
    }

    private void debug(String msg) {
        if (!getDebug()) return;
        try {
            if (mc != null && mc.thePlayer != null) {
                mc.thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.DARK_AQUA + "[AutoExperiment] " + EnumChatFormatting.GRAY + msg));
            }
        } catch (Exception ignored) {}
    }

    private int getDelayMs() {
        BaseConfig<?> cfg = AllConfig.INSTANCE.AUTOEXPERIMENT_CONFIGS.get("autoexperiment_delay_ms");
        if (cfg == null || cfg.Data == null) return 120;
        Object v = cfg.Data;
        if (v instanceof Integer) return (Integer) v;
        if (v instanceof Number) return ((Number) v).intValue();
        if (v instanceof String) {
            String s = ((String) v).trim().replace(",", "");
            try { return Integer.parseInt(s); } catch (Exception ignored) { return 120; }
        }
        return 120;
    }

    private long now() { return System.currentTimeMillis(); }

    enum ExperimentType { CHRONOMATRON, ULTRASEQUENCER, SUPERPAIRS, NONE }
}
