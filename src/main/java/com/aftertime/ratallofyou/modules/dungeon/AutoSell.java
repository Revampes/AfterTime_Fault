package com.aftertime.ratallofyou.modules.dungeon;

import net.minecraft.client.Minecraft;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import com.aftertime.ratallofyou.config.ModConfig;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.Locale;

public class AutoSell {
    private final Minecraft mc = Minecraft.getMinecraft();
    private ScheduledExecutorService executor;

    private final String[] defaultItems = {
        "enchanted ice", "superboom tnt", "rotten", "skeleton master", "skeleton grunt", "cutlass",
        "skeleton lord", "skeleton soldier", "zombie soldier", "zombie knight", "zombie commander", "zombie lord",
        "skeletor", "super heavy", "heavy", "sniper helmet", "dreadlord", "earth shard", "zombie commander whip",
        "machine gun", "sniper bow", "soulstealer bow", "silent death", "training weight",
        "beating heart", "premium flesh", "mimic fragment", "enchanted rotten flesh", "sign",
        "enchanted bone", "defuse kit", "optical lens", "tripwire hook", "button", "carpet", "lever", "diamond atom",
        "healing viii splash potion", "healing 8 splash potion", "candycomb"
    };

    private static AutoSell instance;
    public static AutoSell getInstance() {
        return instance;
    }

    public AutoSell() {
        instance = this;
        // Initialize executor but don't start scheduled task yet
        executor = Executors.newSingleThreadScheduledExecutor();
        startAutoSellLoop();
    }

    private void startAutoSellLoop() {
        // Always stop previous executor
        if (executor != null && !executor.isShutdown()) {
            executor.shutdownNow();
        }
        if (!ModConfig.enableAutoSell) {
            return; // Disabled: do not schedule
        }
        executor = Executors.newSingleThreadScheduledExecutor();

        // Use config delay
        int delay = ModConfig.autoSellDelayMs;
        executor.scheduleWithFixedDelay(this::executeAutoSell, delay, delay, TimeUnit.MILLISECONDS);
    }

    private void executeAutoSell() {
        // Check if module is enabled
        if (!ModConfig.enableAutoSell) return;

        // Get sell list from config
        List<String> sellList = getSellListFromConfig();
        if (sellList.isEmpty()) return;

        if (mc.thePlayer == null || mc.thePlayer.openContainer == null) return;

        if (!(mc.thePlayer.openContainer instanceof ContainerChest)) return;

        ContainerChest container = (ContainerChest) mc.thePlayer.openContainer;

        if (!isValidContainer(container)) return;

        // Find item to sell in inventory slots 54-90
        Integer slotIndex = findItemToSell(container, sellList);
        if (slotIndex == null) return;

        // Perform click based on config click type
        int clickType = getClickTypeFromConfig();
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

    private int getDelayFromConfig() {
        return ModConfig.autoSellDelayMs;
    }

    private int getClickTypeFromConfig() {
        return ModConfig.autoSellClickType;
    }

    private List<String> getSellListFromConfig() {
        List<String> result = new ArrayList<>();
        if (ModConfig.autoSellUseDefaultItems) {
            result.addAll(Arrays.asList(defaultItems));
        }
        String customItems = ModConfig.autoSellCustomItems;
        if (customItems != null && !customItems.trim().isEmpty()) {
            String[] items = customItems.split(",");
            for (String item : items) {
                String trimmed = item.trim();
                if (!trimmed.isEmpty()) {
                    result.add(trimmed.toLowerCase());
                }
            }
        }
        return result;
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

    private Integer findItemToSell(ContainerChest container, List<String> sellList) {
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

    // Method to restart the auto-sell loop when settings change
    public void onConfigChanged() {
        startAutoSellLoop();
    }

    // Clean up method
    public void shutdown() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdownNow();
        }
    }
}
