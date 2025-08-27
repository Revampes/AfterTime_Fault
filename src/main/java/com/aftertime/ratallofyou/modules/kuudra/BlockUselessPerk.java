package com.aftertime.ratallofyou.modules.kuudra;

import com.aftertime.ratallofyou.UI.config.ConfigData.AllConfig;
import com.aftertime.ratallofyou.UI.config.ConfigData.ModuleInfo;
import com.aftertime.ratallofyou.utils.KuudraUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.util.List;
import java.util.regex.Pattern;

public class BlockUselessPerk {
    private static final Minecraft mc = Minecraft.getMinecraft();

    // (Steady Hands|Bomberman|Auto Revive|Human Cannonball|Elle's Lava Rod|Elle's Pickaxe)( [IVX]+)?
    private static final Pattern PERK_PATTERN = Pattern.compile(
            "^(Steady Hands|Bomberman|Auto Revive|Human Cannonball|Elle's Lava Rod|Elle's Pickaxe)(?: [IVX]+)?$"
    );

    // Draw after the chest renders to cover matching slots
    @SubscribeEvent
    public void onDrawScreenPost(GuiScreenEvent.DrawScreenEvent.Post event) {
        if (!isEnabled()) return;
        if (!KuudraUtils.isInKuudraHollow()) return;
        if (!(event.gui instanceof GuiChest)) return;

        GuiChest gui = (GuiChest) event.gui;
        String title = getChestTitle(gui);
        if (title == null || !title.contains("Perk Menu")) return;

        int guiLeft = getGuiLeft(gui);
        int guiTop = getGuiTop(gui);
        Container cont = gui.inventorySlots;
        if (cont == null) return;
        @SuppressWarnings("unchecked")
        List<Slot> slots = cont.inventorySlots;
        if (slots == null) return;

        for (Slot slot : slots) {
            if (slot == null) continue;
            ItemStack stack = slot.getStack();
            if (stack == null) continue;
            String name = EnumChatFormatting.getTextWithoutFormattingCodes(stack.getDisplayName());
            if (name == null) continue;
            if (PERK_PATTERN.matcher(name).matches()) {
                int x = guiLeft + slot.xDisplayPosition;
                int y = guiTop + slot.yDisplayPosition;
                // Cover the slot area (16x16) with the panel background color
                Gui.drawRect(x, y, x + 16, y + 16, 0xFF1E1E1E);
            }
        }
    }

    private boolean isEnabled() {
        ModuleInfo cfg = (ModuleInfo) AllConfig.INSTANCE.MODULES.get("kuudra_blockuselessperks");
        return cfg != null && Boolean.TRUE.equals(cfg.Data);
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

    private static String getChestTitle(GuiChest chest) {
        try {
            Container cont = chest.inventorySlots;
            if (cont instanceof ContainerChest) {
                Object lowerInv = ContainerChest.class.getMethod("getLowerChestInventory").invoke(cont);
                if (lowerInv instanceof net.minecraft.inventory.IInventory) {
                    net.minecraft.util.IChatComponent comp = ((net.minecraft.inventory.IInventory) lowerInv).getDisplayName();
                    return comp == null ? null : comp.getUnformattedText();
                }
            }
        } catch (Throwable ignored) {
        }
        return null;
    }
}
