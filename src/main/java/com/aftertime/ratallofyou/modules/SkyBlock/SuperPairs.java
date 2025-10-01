package com.aftertime.ratallofyou.modules.SkyBlock;

import com.aftertime.ratallofyou.UI.config.ConfigData.AllConfig;
import com.aftertime.ratallofyou.UI.config.ConfigData.ModuleInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.*;

public class SuperPairs {
    private final Minecraft mc = Minecraft.getMinecraft();

    private final List<Integer> slots = new ArrayList<>();
    private final Map<Integer, String> names = new HashMap<>();
    private final Map<String, List<Integer>> matches = new HashMap<>();
    private final Map<Integer, ItemStack> displayedItems = new HashMap<>(); // Store revealed items
    private boolean inExperimentation = false;
    private GuiChest lastGui;

    // Color array for highlighting matched pairs
    private final int[][] colors = {
        {255, 0, 0},     // Red
        {0, 0, 255},     // Blue
        {60, 179, 113},  // Medium Sea Green
        {238, 130, 238}, // Violet
        {255, 165, 0},   // Orange
        {106, 90, 205},  // Slate Blue
        {132, 222, 2},   // Lawn Green
        {229, 43, 80},   // Crimson
        {178, 132, 190}, // Plum
        {18, 97, 128},   // Dark Cyan
        {194, 113, 38},  // Saddle Brown
        {255, 255, 255}, // White
        {38, 168, 212},  // Dodger Blue
        {248, 203, 203}  // Misty Rose
    };

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        if (!(event.gui instanceof GuiChest)) return;

        Container container = ((GuiChest) event.gui).inventorySlots;
        if (!(container instanceof ContainerChest)) return;

        String chestName = ((ContainerChest) container).getLowerChestInventory().getDisplayName().getUnformattedText();
        if (chestName != null && (chestName.contains("Superpairs (") && !chestName.contains("Stakes"))) {
            lastGui = (GuiChest) event.gui;
            displayedItems.clear(); // Clear revealed items when opening new GUI
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!isEnabled()) return;

        if (mc.thePlayer != null && mc.thePlayer.openContainer instanceof ContainerChest) {
            String name = ((ContainerChest) mc.thePlayer.openContainer).getLowerChestInventory().getDisplayName().getUnformattedText();

            if (!inExperimentation) {
                if (name != null && (name.contains("Superpairs (") && !name.contains("Stakes"))) {
                    // Start a thread to initialize slots after a short delay
                    new Thread(() -> {
                        try {
                            Thread.sleep(40);
                            initializeSlots();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }).start();

                    inExperimentation = true;
                }
            } else {
                if (name == null || (!name.contains("Superpairs (") || name.contains("Stakes"))) {
                    exited();
                    return;
                }

                updateItemNames();
            }
        } else if (inExperimentation) {
            exited();
        }
    }

    @SubscribeEvent
    public void onGuiDraw(GuiScreenEvent.BackgroundDrawnEvent event) {
        if (!inExperimentation) return;
        if (!isEnabled()) return;
        if (!(event.gui instanceof GuiChest)) return;

        renderHighlights();
        renderRevealedItems((GuiContainer) event.gui);
    }

    @SubscribeEvent
    public void onItemTooltip(ItemTooltipEvent event) {
        if (!isEnabled()) return;
        if (!inExperimentation) return;
        if (lastGui == null) return;
        if (event.itemStack == null || event.itemStack.getItem() == null) return;

        // Check if this is a stained glass block (ID 95 in 1.8.9)
        if (getItemId(event.itemStack) == 95) {
            Slot slotUnderMouse = lastGui.getSlotUnderMouse();
            if (slotUnderMouse != null) {
                int slot = slotUnderMouse.slotNumber; // Use slotNumber instead of slotIndex
                String name = names.get(slot);

                if (name != null) {
                    String displayName = event.itemStack.getDisplayName();
                    if (displayName.contains("Click any button") || displayName.contains("Click a second button") ||
                        displayName.contains("Next button is instantly rewarded") || displayName.contains("Stained Glass")) {

                        // Check if tooltip already contains our name
                        boolean alreadyHasName = event.toolTip.stream()
                            .anyMatch(line -> EnumChatFormatting.getTextWithoutFormattingCodes(line).equals(
                                EnumChatFormatting.getTextWithoutFormattingCodes(name)));

                        if (!alreadyHasName) {
                            event.toolTip.add(EnumChatFormatting.YELLOW + name);
                        }
                    }
                }
            }
        }
    }

    private void initializeSlots() {
        if (mc.thePlayer == null || !(mc.thePlayer.openContainer instanceof ContainerChest)) return;

        ContainerChest container = (ContainerChest) mc.thePlayer.openContainer;
        List<Slot> inventorySlots = container.inventorySlots;

        slots.clear();
        for (int i = 0; i < 45 && i < inventorySlots.size(); i++) {
            Slot slot = inventorySlots.get(i);
            if (slot != null && slot.getHasStack()) {
                ItemStack stack = slot.getStack();
                // Check for stained glass (ID 95 in 1.8.9)
                if (stack != null && getItemId(stack) == 95) {
                    slots.add(i);
                }
            }
        }

        if (slots.isEmpty()) {
            inExperimentation = false;
        }
    }

    private void updateItemNames() {
        if (mc.thePlayer == null || !(mc.thePlayer.openContainer instanceof ContainerChest)) return;

        ContainerChest container = (ContainerChest) mc.thePlayer.openContainer;
        List<Slot> inventorySlots = container.inventorySlots;

        for (int slotIndex : slots) {
            if (slotIndex >= inventorySlots.size()) continue;

            Slot slot = inventorySlots.get(slotIndex);
            if (slot == null || !slot.getHasStack()) continue;

            ItemStack stack = slot.getStack();
            if (stack == null) continue;

            int itemId = getItemId(stack);

            // Store revealed items (not stained glass and not bookshelf)
            if (itemId != 95 && itemId != getItemId(new ItemStack(net.minecraft.init.Blocks.bookshelf))) {
                if (!displayedItems.containsKey(slotIndex)) {
                    displayedItems.put(slotIndex, stack.copy());
                }
            }

            // Skip if we already have a name for this slot or if it's still stained glass
            if (itemId == 95 || itemId == -1 || names.containsKey(slotIndex)) continue;

            String name = stack.getDisplayName();
            int amount = stack.stackSize;

            // Handle enchanted books
            if (name.contains("Enchanted Book") && stack.hasTagCompound()) {
                List<String> lore = getLore(stack);
                if (lore.size() > 3) {
                    name = lore.get(3);
                }
            }

            // Add stack size if greater than 1
            if (amount > 1) {
                name = name + " x" + amount;
            }

            names.put(slotIndex, name);

            // Skip special items that shouldn't be matched
            if (!name.contains("Instant Find") && !name.contains("Gained +")) {
                matches.computeIfAbsent(name, k -> new ArrayList<>()).add(slotIndex);
            }
        }
    }

    private void renderHighlights() {
        if (matches.isEmpty()) return;

        GlStateManager.pushMatrix();

        int colorIndex = 0;
        for (Map.Entry<String, List<Integer>> entry : matches.entrySet()) {
            List<Integer> slotList = entry.getValue();
            if (slotList.size() <= 1) continue; // Only highlight pairs

            int[] color = colors[colorIndex % colors.length];

            for (int slotIndex : slotList) {
                int x = slotIndex % 9;
                int y = slotIndex / 9;

                // Fixed positioning calculation to match the GUI properly
                int guiLeft = (mc.displayWidth / mc.gameSettings.guiScale - 176) / 2;
                int guiTop = (mc.displayHeight / mc.gameSettings.guiScale - 222) / 2;

                int renderX = guiLeft + 8 + (x * 18);
                int renderY = guiTop + 18 + (y * 18); // Fixed: removed the offset that was causing issues

                // Draw colored rectangle overlay
                drawRect(renderX - 1, renderY - 1, renderX + 17, renderY + 17,
                    (160 << 24) | (color[0] << 16) | (color[1] << 8) | color[2]);
            }

            colorIndex++;
        }

        GlStateManager.popMatrix();
    }

    private void renderRevealedItems(GuiContainer container) {
        if (displayedItems.isEmpty()) return;

        for (Map.Entry<Integer, ItemStack> entry : displayedItems.entrySet()) {
            int slotIndex = entry.getKey();
            ItemStack revealedItem = entry.getValue();

            if (revealedItem == null) continue;

            // Get the slot position
            if (slotIndex >= container.inventorySlots.inventorySlots.size()) continue;
            Slot slot = container.inventorySlots.inventorySlots.get(slotIndex);

            // Calculate render position
            int x = (container.width - 176) / 2 + slot.xDisplayPosition;
            int y = (container.height - 222) / 2 + slot.yDisplayPosition;

            GlStateManager.pushMatrix();
            GlStateManager.translate(0, 0, 200); // Bring to front

            RenderHelper.enableGUIStandardItemLighting();
            mc.getRenderItem().renderItemAndEffectIntoGUI(revealedItem, x, y);
            RenderHelper.disableStandardItemLighting();

            GlStateManager.popMatrix();
        }
    }

    private void drawRect(int left, int top, int right, int bottom, int color) {
        net.minecraft.client.gui.Gui.drawRect(left, top, right, bottom, color);
    }

    private List<String> getLore(ItemStack stack) {
        List<String> lore = new ArrayList<>();
        if (stack.hasTagCompound() && stack.getTagCompound().hasKey("display")) {
            NBTTagList loreList = stack.getTagCompound().getCompoundTag("display").getTagList("Lore", 8);
            for (int i = 0; i < loreList.tagCount(); i++) {
                lore.add(loreList.getStringTagAt(i));
            }
        }
        return lore;
    }

    private int getItemId(ItemStack stack) {
        if (stack == null || stack.getItem() == null) return -1;
        return stack.getItem().getIdFromItem(stack.getItem());
    }

    private void exited() {
        slots.clear();
        names.clear();
        matches.clear();
        displayedItems.clear();
        inExperimentation = false;
        lastGui = null;
    }

    private boolean isEnabled() {
        ModuleInfo cfg = (ModuleInfo) AllConfig.INSTANCE.MODULES.get("skyblock_superpairs");
        return cfg != null && (Boolean) cfg.Data;
    }
}
