package com.aftertime.ratallofyou.modules.dungeon;

import com.aftertime.ratallofyou.config.ModConfig;
import com.aftertime.ratallofyou.utils.DungeonUtils;
import com.aftertime.ratallofyou.utils.TabUtils;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import com.aftertime.ratallofyou.UI.config.ConfigData.AllConfig;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.Container;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import java.util.ArrayList;
import java.util.List;

public class SalvageItem {

    private final Minecraft mc = Minecraft.getMinecraft();
    private final List<Integer> highlightSalvageable = new ArrayList<>();
    private final List<Integer> highlightTopSalvageable = new ArrayList<>();

    @SubscribeEvent
    public void onInitGui(GuiScreenEvent.InitGuiEvent.Post event) {
        if (!ModConfig.enableSalvageItem || !TabUtils.isInSkyblock() || !(event.gui instanceof GuiContainer) || DungeonUtils.isopenspiritleap()) {
            return;
        }
        // Recalculate highlights when GUI opens
        recalculateHighlights();
    }

    @SubscribeEvent
    public void onDrawPost(GuiScreenEvent.DrawScreenEvent.Post event) {
        if (!ModConfig.enableSalvageItem || !TabUtils.isInSkyblock() || !(event.gui instanceof GuiContainer) || DungeonUtils.isopenspiritleap()) {
            return;
        }
        // Recalculate highlights in case items changed
        recalculateHighlights();
        // Draw highlights in both chest and player inventory
        if (!highlightSalvageable.isEmpty() || !highlightTopSalvageable.isEmpty()) {
            GuiContainer gui = (GuiContainer) event.gui;
            int guiLeft = getGuiLeft(gui);
            int guiTop = getGuiTop(gui);
            List<Slot> slots = gui.inventorySlots == null ? null : gui.inventorySlots.inventorySlots;
            if (slots != null) {
                for (int idx : highlightSalvageable) {
                    if (idx < 0 || idx >= slots.size()) continue;
                    Slot s = slots.get(idx);
                    if (s == null) continue;
                    int x = guiLeft + s.xDisplayPosition;
                    int y = guiTop + s.yDisplayPosition;
                    int color = getOverlayColorSalvageable();
                    Gui.drawRect(x, y, x + 16, y + 16, color);
                }
                for (int idx : highlightTopSalvageable) {
                    if (idx < 0 || idx >= slots.size()) continue;
                    Slot s = slots.get(idx);
                    if (s == null) continue;
                    int x = guiLeft + s.xDisplayPosition;
                    int y = guiTop + s.yDisplayPosition;
                    int color = getOverlayColorTopSalvageable();
                    Gui.drawRect(x, y, x + 16, y + 16, color);
                }
            }
        }
    }

    private void recalculateHighlights() {
        highlightSalvageable.clear();
        highlightTopSalvageable.clear();

        if (mc.thePlayer == null) return;
        if (!(mc.currentScreen instanceof GuiContainer)) return;

        Container cont = mc.thePlayer.openContainer;
        if (cont == null) return;

        int size = cont.inventorySlots == null ? 0 : cont.inventorySlots.size();
        for (int i = 0; i < size; i++) {
            Slot slot = cont.getSlot(i);
            if (slot == null || slot.getStack() == null) continue;

            NBTTagCompound extraAttributes = slot.getStack().getSubCompound("ExtraAttributes", false);
            if (extraAttributes == null) continue;

            // Check if item has baseStatBoostPercentage but no dungeon_item_level (salvageable item)
            if (extraAttributes.hasKey("baseStatBoostPercentage") && !extraAttributes.hasKey("dungeon_item_level")) {
                int baseStatBoost = extraAttributes.getInteger("baseStatBoostPercentage");

                if (baseStatBoost == 50) {
                    highlightTopSalvageable.add(i);
                } else {
                    highlightSalvageable.add(i);
                }
            }
        }
    }

    private static int getGuiLeft(GuiContainer gui) {
        try {
            Integer left = ReflectionHelper.getPrivateValue(GuiContainer.class, gui, "guiLeft", "field_147003_i");
            return left == null ? 0 : left;
        } catch (Throwable ignored) { }
        return 0;
    }

    private static int getGuiTop(GuiContainer gui) {
        try {
            Integer top = ReflectionHelper.getPrivateValue(GuiContainer.class, gui, "guiTop", "field_147009_r");
            return top == null ? 0 : top;
        } catch (Throwable ignored) { }
        return 0;
    }


    private int getOverlayColorSalvageable() {
        // Use config color for regular salvageable items, but force 80% opacity
        int base = 0xCC00FFFF;
        return (0xCC << 24) | base; // 0xCC = 80% opacity
    }
    private int getOverlayColorTopSalvageable() {
        // Use config color for top-tier salvageable items, but force 80% opacity
        int base = 0xCCFF0000;
        return (0xCC << 24) | base; // 0xCC = 80% opacity
    }
}
