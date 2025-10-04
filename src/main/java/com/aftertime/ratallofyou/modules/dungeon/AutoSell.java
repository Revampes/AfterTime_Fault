package com.aftertime.ratallofyou.modules.dungeon;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.Locale;

public class AutoSell {
    private final Minecraft mc = Minecraft.getMinecraft();
    private boolean enabled = false;
    private List<String> sellList = new ArrayList<>();
    private int delay = 100; // Default delay in milliseconds
    private int clickType = 0; // 0 = Shift, 1 = Middle, 2 = Left
    private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    private final String[] defaultItems = {
        "enchanted ice", "superboom tnt", "rotten", "skeleton master", "skeleton grunt", "cutlass",
        "skeleton lord", "skeleton soldier", "zombie soldier", "zombie knight", "zombie commander", "zombie lord",
        "skeletor", "super heavy", "heavy", "sniper helmet", "dreadlord", "earth shard", "zombie commander whip",
        "machine gun", "sniper bow", "soulstealer bow", "silent death", "training weight",
        "beating heart", "premium flesh", "mimic fragment", "enchanted rotten flesh", "sign",
        "enchanted bone", "defuse kit", "optical lens", "tripwire hook", "button", "carpet", "lever", "diamond atom",
        "healing viii splash potion", "healing 8 splash potion", "candycomb"
    };

    public AutoSell() {
        // Initialize with default items
        sellList.addAll(Arrays.asList(defaultItems));

        // Start the execution loop
        executor.scheduleWithFixedDelay(this::executeAutoSell, delay, delay, TimeUnit.MILLISECONDS);
    }

    private void executeAutoSell() {
        if (!enabled || sellList.isEmpty()) return;

        if (mc.thePlayer == null || mc.thePlayer.openContainer == null) return;

        if (!(mc.thePlayer.openContainer instanceof ContainerChest)) return;

        ContainerChest container = (ContainerChest) mc.thePlayer.openContainer;

        if (!isValidContainer(container)) return;

        // Find item to sell in inventory slots 54-90
        Integer slotIndex = findItemToSell(container);
        if (slotIndex == null) return;

        // Perform click based on click type
        switch (clickType) {
            case 0:
                windowClick(slotIndex, ClickType.SHIFT);
                break;
            case 1:
                windowClick(slotIndex, ClickType.MIDDLE);
                break;
            case 2:
                windowClick(slotIndex, ClickType.LEFT);
                break;
        }
    }

    private boolean isValidContainer(ContainerChest container) {
        try {
            IInventory inv = container.getLowerChestInventory();
            if (inv == null || inv.getDisplayName() == null) return false;

            String title = net.minecraft.util.StringUtils.stripControlCodes(
                inv.getDisplayName().getUnformattedText()
            ).toLowerCase(Locale.ENGLISH);

            return title.equals("trades") || title.equals("booster cookie") ||
                   title.equals("farm merchant") || title.equals("ophelia");
        } catch (Exception e) {
            return false;
        }
    }

    private Integer findItemToSell(ContainerChest container) {
        List<Slot> inventorySlots = container.inventorySlots;
        if (inventorySlots == null || inventorySlots.size() < 90) return null;

        for (int i = 54; i < 90 && i < inventorySlots.size(); i++) {
            Slot slot = inventorySlots.get(i);
            if (slot == null || !slot.getHasStack()) continue;

            ItemStack stack = slot.getStack();
            if (stack == null || stack.getDisplayName() == null) continue;

            String displayName = stack.getDisplayName().toLowerCase(Locale.ENGLISH);

            for (String sellItem : sellList) {
                if (displayName.contains(sellItem.toLowerCase())) {
                    return slot.slotNumber;
                }
            }
        }

        return null;
    }

    private void windowClick(int slotIndex, ClickType clickType) {
        try {
            switch (clickType) {
                case SHIFT:
                    mc.playerController.windowClick(
                        mc.thePlayer.openContainer.windowId,
                        slotIndex,
                        0,
                        1, // Shift click
                        mc.thePlayer
                    );
                    break;
                case MIDDLE:
                    mc.playerController.windowClick(
                        mc.thePlayer.openContainer.windowId,
                        slotIndex,
                        2, // Middle mouse button
                        3, // Pick block
                        mc.thePlayer
                    );
                    break;
                case LEFT:
                    mc.playerController.windowClick(
                        mc.thePlayer.openContainer.windowId,
                        slotIndex,
                        0, // Left mouse button
                        0, // Normal click
                        mc.thePlayer
                    );
                    break;
            }
        } catch (Exception e) {
            // Handle any click errors
        }
    }

    public enum ClickType {
        SHIFT, MIDDLE, LEFT
    }

    // Getters and setters
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<String> getSellList() {
        return sellList;
    }

    public void setSellList(List<String> sellList) {
        this.sellList = sellList;
    }

    public int getDelay() {
        return delay;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    public int getClickType() {
        return clickType;
    }

    public void setClickType(int clickType) {
        this.clickType = clickType;
    }
}

