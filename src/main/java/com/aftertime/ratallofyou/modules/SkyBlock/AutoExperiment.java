package com.aftertime.ratallofyou.modules.SkyBlock;

import com.aftertime.ratallofyou.config.ModConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import java.util.*;

public class AutoExperiment {
    private final Minecraft mc = Minecraft.getMinecraft();

    private ExperimentType current = ExperimentType.NONE;
    private boolean hasAdded = false;
    private int clicks = 0;
    private long lastClickTime = 0L;

    private final List<Map.Entry<Integer, String>> chronomatronOrder = new ArrayList<>(28);
    private int lastAddedSlot = -1;

    private final HashMap<Integer, Integer> ultrasequencerOrder = new HashMap<>();

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        // Reset state
        current = ExperimentType.NONE;
        hasAdded = false;
        chronomatronOrder.clear();
        lastAddedSlot = -1;
        ultrasequencerOrder.clear();
        clicks = 0;

        if (!(event.gui instanceof GuiChest)) return;
        Container container = ((GuiChest) event.gui).inventorySlots;
        if (!(container instanceof ContainerChest)) return;

        String chestName = ((ContainerChest) container).getLowerChestInventory().getDisplayName().getUnformattedText();
        if (chestName == null) return;

        if (chestName.startsWith("Chronomatron (")) {
            current = ExperimentType.CHRONOMATRON;
            debug("Started Chronomatron experiment");
        } else if (chestName.startsWith("Ultrasequencer (")) {
            current = ExperimentType.ULTRTRASEQUENCER; // will be corrected below
        } else if (chestName.startsWith("Superpairs (")) {
            current = ExperimentType.SUPERPAIRS;
            debug("Started Superpairs experiment");
        }
        if (current == ExperimentType.ULTRTRASEQUENCER) {
            current = ExperimentType.ULTRASEQUENCER;
            debug("Started Ultrasequencer experiment");
        }
    }

    @SubscribeEvent
    public void onGuiDraw(GuiScreenEvent.BackgroundDrawnEvent event) {
        if (!(event.gui instanceof GuiChest)) return;
        if (!isEnabled()) return;

        Container container = ((GuiChest) event.gui).inventorySlots;
        if (!(container instanceof ContainerChest)) return;

        List<Slot> invSlots = container.inventorySlots;
        if (invSlots.size() <= 49) return;

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
        // Check for reset condition: glowstone in slot 49 and last added slot is no longer enchanted
        if (isGlowstoneOn49(invSlots) && lastAddedSlot >= 0 && lastAddedSlot < invSlots.size()) {
            Slot lastSlot = invSlots.get(lastAddedSlot);
            if (lastSlot == null || !isEnchanted(lastSlot)) {
                hasAdded = false;
                clicks = 0;
                debug("Chronomatron reset detected, sequence size: " + chronomatronOrder.size());

                if (chronomatronOrder.size() > 10 && getAutoExit()) {
                    debug("Auto-exiting Chronomatron");
                    closeScreen();
                    return;
                }
            }
        }

        // Record phase: clock in slot 49, look for enchanted items
        if (!hasAdded && isClockOn49(invSlots)) {
            for (Slot slot : invSlots) {
                if (slot.slotNumber >= 10 && slot.slotNumber <= 43 && isEnchanted(slot)) {
                    chronomatronOrder.add(new AbstractMap.SimpleEntry<>(slot.slotNumber, slot.getStack().getDisplayName()));
                    lastAddedSlot = slot.slotNumber;
                    hasAdded = true;
                    clicks = 0;
                    debug("Chronomatron recorded: slot=" + slot.slotNumber + ", total=" + chronomatronOrder.size());
                    break;
                }
            }
        }

        // Playback phase: clock in slot 49, no enchanted items visible, and we have sequence to play
        if (hasAdded && isClockOn49(invSlots) && !anyEnchantedInRange(invSlots, 10, 43) &&
            clicks < chronomatronOrder.size() && canClick()) {

            int slotToClick = chronomatronOrder.get(clicks).getKey();
            debug("Chronomatron clicking: index=" + clicks + ", slot=" + slotToClick);
            clickSlot(slotToClick);
            clicks++;
        }
    }

    private void handleUltrasequencer(List<Slot> invSlots) {
        // Reset flag when clock appears (playback phase)
        if (isClockOn49(invSlots)) {
            hasAdded = false;
        }

        // Record phase: glowstone in slot 49, build the sequence
        if (!hasAdded && isGlowstoneOn49(invSlots)) {
            if (!invSlots.get(44).getHasStack()) return; // Ensure grid is present

            ultrasequencerOrder.clear();
            for (Slot slot : invSlots) {
                if (slot.slotNumber >= 9 && slot.slotNumber <= 44 && slot.getHasStack()) {
                    ItemStack stack = slot.getStack();
                    // Look for dye items (not paper as in the broken version)
                    if (stack.getItem() == Items.dye) {
                        // Stack size indicates the order (1-based)
                        ultrasequencerOrder.put(stack.stackSize - 1, slot.slotNumber);
                    }
                }
            }

            hasAdded = true;
            clicks = 0;
            debug("Ultrasequencer recorded: size=" + ultrasequencerOrder.size());

            if (ultrasequencerOrder.size() > 6 && getAutoExit()) {
                debug("Auto-exiting Ultrasequencer");
                closeScreen();
                return;
            }
        }

        // Playback phase: clock in slot 49 and we have the sequence
        if (isClockOn49(invSlots) && ultrasequencerOrder.containsKey(clicks) && canClick()) {
            Integer slotToClick = ultrasequencerOrder.get(clicks);
            if (slotToClick != null) {
                debug("Ultrasequencer clicking: index=" + clicks + ", slot=" + slotToClick);
                clickSlot(slotToClick);
                clicks++;
            }
        }
    }

    // Helper methods
    private void clickSlot(int slotId) {
        if (mc.thePlayer == null || mc.theWorld == null) return;
        mc.playerController.windowClick(mc.thePlayer.openContainer.windowId, slotId, 0, 0, mc.thePlayer);
        lastClickTime = System.currentTimeMillis();
    }

    private void closeScreen() {
        if (mc.thePlayer != null) mc.thePlayer.closeScreen();
    }

    private boolean isClockOn49(List<Slot> slots) {
        if (slots.size() <= 49) return false;
        Slot slot = slots.get(49);
        return slot != null && slot.getHasStack() && slot.getStack().getItem() == Items.clock;
    }

    private boolean isGlowstoneOn49(List<Slot> slots) {
        if (slots.size() <= 49) return false;
        Slot slot = slots.get(49);
        return slot != null && slot.getHasStack() &&
               slot.getStack().getItem() == Item.getItemFromBlock(Blocks.glowstone);
    }

    private boolean isEnchanted(Slot slot) {
        return slot != null && slot.getHasStack() && slot.getStack().isItemEnchanted();
    }

    private boolean anyEnchantedInRange(List<Slot> slots, int from, int to) {
        for (Slot slot : slots) {
            if (slot.slotNumber >= from && slot.slotNumber <= to && isEnchanted(slot)) {
                return true;
            }
        }
        return false;
    }

    private boolean canClick() {
        return System.currentTimeMillis() - lastClickTime >= getDelayMs();
    }

    private boolean isEnabled() {
        return ModConfig.enableAutoExperiment;
    }

    private boolean getAutoExit() {
        return ModConfig.autoExperimentAutoExit;
    }

    private boolean getDebug() {
        return ModConfig.autoExperimentDebug;
    }

    private void debug(String msg) {
        if (!getDebug()) return;
        try {
            if (mc != null && mc.thePlayer != null) {
                mc.thePlayer.addChatMessage(new ChatComponentText(
                    EnumChatFormatting.DARK_AQUA + "[AutoExperiment] " +
                    EnumChatFormatting.GRAY + msg));
            }
        } catch (Exception ignored) {}
    }

    private int getDelayMs() {
        try {
            int v = ModConfig.autoExperimentDelayMs;
            return Math.max(60, Math.min(1000, v));
        } catch (Throwable ignored) {
            return 300;
        }
    }

    enum ExperimentType {
        CHRONOMATRON,
        ULTRASEQUENCER,
        SUPERPAIRS,
        ULTRTRASEQUENCER,
        NONE
    }
}
