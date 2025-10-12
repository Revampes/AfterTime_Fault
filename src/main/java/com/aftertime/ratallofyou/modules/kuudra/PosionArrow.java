package com.aftertime.ratallofyou.modules.kuudra;

import com.aftertime.ratallofyou.config.ModConfig;
import com.aftertime.ratallofyou.utils.KuudraUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class PosionArrow {
    private static final Minecraft mc = Minecraft.getMinecraft();

    private int twilight = 0;
    private int toxic = 0;
    private long lastAlertMs = 0L;

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!isEnabled()) return;
        if (mc.thePlayer == null) return;

        // Scan player inventory for arrow poisons
        twilight = 0;
        toxic = 0;
        ItemStack[] inv = mc.thePlayer.inventory.mainInventory;
        if (inv != null) {
            for (ItemStack stack : inv) {
                if (stack == null) continue;
                String name = stack.getDisplayName();
                if (name == null) continue;
                String plain = net.minecraft.util.EnumChatFormatting.getTextWithoutFormattingCodes(name);
                if (plain == null) continue;
                if (plain.contains("Twilight Arrow Poison")) twilight += stack.stackSize;
                if (plain.contains("Toxic Arrow Poison")) toxic += stack.stackSize;
            }
        }

        // Phase 1 alert if no Toxic Arrow Poison
        if (KuudraUtils.isPhase(1) && toxic <= 0) {
            long now = System.currentTimeMillis();
            if (now - lastAlertMs > 5000) { // throttle 5s
                String title = EnumChatFormatting.RED + "No Toxic Arrow Poison!";
                mc.ingameGUI.displayTitle(title, "", 5, 40, 5);
                lastAlertMs = now;
            }
        }
    }

    @SubscribeEvent
    public void onRender(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.TEXT) return;
        if (!isEnabled()) return;
        if (mc.thePlayer == null) return;

        float scale = getScale();

        GlStateManager.pushMatrix();
        try {
            ScaledResolution sr = new ScaledResolution(mc);
            // Clamp position lightly to screen to avoid off-screen draw
            int x = Math.max(0, Math.min(ModConfig.arrowpoisonX, sr.getScaledWidth() - 4));
            int y = Math.max(0, Math.min(ModConfig.arrowpoisonY, sr.getScaledHeight() - 4));

            GlStateManager.translate(x, y, 0);
            GlStateManager.scale(scale, scale, 1.0f);

            // Layout: two rows, each 18px apart
            int rowStride = 18;
            int iconY1 = 0;
            int iconY2 = rowStride;
            int textX = 20; // 16px icon + 4px padding
            int fontH = mc.fontRendererObj.FONT_HEIGHT;
            int textOffsetY = Math.max(0, (16 - fontH) / 2);
            int textY1 = iconY1 + textOffsetY;
            int textY2 = iconY2 + textOffsetY;

            // Prepare dye icons (metadata 5 and 10)
            ItemStack twilightIcon = new ItemStack(Items.dye, 1, 5);   // purple-ish for Twilight
            ItemStack toxicIcon = new ItemStack(Items.dye, 1, 10);      // lime for Toxic

            GlStateManager.disableLighting();
            RenderHelper.enableGUIStandardItemLighting();
            mc.getRenderItem().renderItemAndEffectIntoGUI(twilightIcon, 0, iconY1);
            mc.getRenderItem().renderItemAndEffectIntoGUI(toxicIcon, 0, iconY2);
            RenderHelper.disableStandardItemLighting();

            // Labeled counts so mapping is unambiguous
            int twilightColor = 0xCC88FF; // soft purple
            int toxicColor = 0x66FF66;    // lime green
            mc.fontRendererObj.drawString("Twilight Arrow Posion: " + twilight, textX, textY1, twilightColor, true);
            mc.fontRendererObj.drawString("Toxic Arrow Posion: " + toxic, textX, textY2, toxicColor, true);
        } finally {
            GlStateManager.popMatrix();
        }
    }

    private boolean isEnabled() {
        return ModConfig.enableKuudraArrowPoison;
    }

    private float getScale() {
        float v = ModConfig.arrowpoisonScale;
        return v <= 0 ? 1.0f : v;
    }
}
