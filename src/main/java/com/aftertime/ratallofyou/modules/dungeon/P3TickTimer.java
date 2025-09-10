package com.aftertime.ratallofyou.modules.dungeon;

import com.aftertime.ratallofyou.UI.UIDragger;
import com.aftertime.ratallofyou.UI.UIHighlighter;

import com.aftertime.ratallofyou.UI.config.ConfigData.AllConfig;
import com.aftertime.ratallofyou.UI.config.ConfigData.UIPosition;
import com.aftertime.ratallofyou.UI.Settings.BooleanSettings;
import com.aftertime.ratallofyou.utils.DungeonUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Mouse;

public class P3TickTimer {
    private int barrierTicks = 0;
    private boolean isTimerActive = false;
    private final Minecraft mc = Minecraft.getMinecraft();
    private boolean isMouseOver = false;

    @SubscribeEvent
    public void onChat(net.minecraftforge.client.event.ClientChatReceivedEvent event) {
        if (!isModuleEnabled() || !DungeonUtils.isInDungeon()) return;

        String message = event.message.getUnformattedText();

        // Trigger messages
        if (DungeonUtils.isInP3()) {
            startTimer();
        } else if (!DungeonUtils.isInP3()) {
            resetTimer();
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (!isModuleEnabled() || !isTimerActive || event.phase != TickEvent.Phase.START || DungeonUtils.isInDungeon()) return;

        barrierTicks--;
        if (barrierTicks <= 0) {
            barrierTicks = 60; // Reset to 3 seconds (60 ticks)
        }
    }
    UIPosition pos = (UIPosition) AllConfig.INSTANCE.Pos_CONFIGS.get("p3ticktimer_pos").Data;
    @SubscribeEvent
    public void onRender(RenderGameOverlayEvent.Post event) {
        if (!isModuleEnabled() || !isTimerActive ||
                event.type != RenderGameOverlayEvent.ElementType.ALL) return;

        String time = String.format("%.2f", barrierTicks / 20.0f);
        String formattedTime = getFormattedTime(time);


        if (pos == null) {
            ScaledResolution res = new ScaledResolution(mc);
            pos = new UIPosition(res.getScaledWidth() / 2, res.getScaledHeight() / 2);
        }

        float scale = 1.0f;
        Object sc = AllConfig.INSTANCE.Pos_CONFIGS.get("p3ticktimer_scale").Data;
        if (sc instanceof Float) scale = (Float) sc; else if (sc instanceof Double) scale = ((Double) sc).floatValue();
        int textWidth = mc.fontRendererObj.getStringWidth(formattedTime);
        int renderX = pos.x - Math.round((textWidth * scale) / 2f);
        int renderY = pos.y;

        // Check if mouse is over (unscaled hitbox in move mode is handled by UIHighlighter)
        isMouseOver = isMouseOver(renderX, renderY, Math.round(textWidth * scale), Math.round(mc.fontRendererObj.FONT_HEIGHT * scale));

        // Draw the timer with scaling
        GlStateManager.pushMatrix();
        GlStateManager.scale(scale, scale, 1.0f);
        mc.fontRendererObj.drawStringWithShadow(formattedTime, (int) (renderX / scale), (int) (renderY / scale), 0xFFFFFF);
        GlStateManager.popMatrix();

        // Show drag handle when in move mode
        if (UIHighlighter.isInMoveMode()) {
            mc.fontRendererObj.drawStringWithShadow("≡", renderX + Math.round(textWidth * scale) + 2, renderY, 0xAAAAAA);
            drawRect(renderX - 2, renderY - 2, renderX + Math.round(textWidth * scale) + 2, renderY + Math.round(10 * scale), 0x60FFFF00);
        }
    }

    private String getFormattedTime(String time) {
        if (barrierTicks >= 40) return EnumChatFormatting.GREEN + time;
        if (barrierTicks >= 20) return EnumChatFormatting.YELLOW + time;
        return EnumChatFormatting.RED + time;
    }

    private boolean isMouseOver(int x, int y, int width, int height) {
        if (UIHighlighter.isInMoveMode()) {
            ScaledResolution res = new ScaledResolution(mc);
            int mouseX = Mouse.getX() * res.getScaledWidth() / mc.displayWidth;
            int mouseY = res.getScaledHeight() - Mouse.getY() * res.getScaledHeight() / mc.displayHeight - 1;
            return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
        }
        return false;
    }

    @SubscribeEvent
    public void onMouseInput(net.minecraftforge.client.event.MouseEvent event) {
        if (!isModuleEnabled() || !isTimerActive || !UIHighlighter.isInMoveMode()) return;

        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution res = new ScaledResolution(mc);

        // Get absolute mouse coordinates
        int mouseX = Mouse.getX() * res.getScaledWidth() / mc.displayWidth;
        int mouseY = res.getScaledHeight() - Mouse.getY() * res.getScaledHeight() / mc.displayHeight - 1;

        UIPosition pos = (UIPosition) AllConfig.INSTANCE.Pos_CONFIGS.get("p3ticktimer_pos").Data;
        if (pos == null) return;

        if (Mouse.getEventButton() == 0) { // Left mouse button
            if (Mouse.getEventButtonState()) { // Mouse pressed
                if (isMouseOver(pos.x, pos.y, getElementWidth(), getElementHeight())) {
                    UIDragger.getInstance().tryStartDrag(pos, mouseX, mouseY);
                }
            } else { // Mouse released
                UIDragger.getInstance().updatePositions();
            }
        }

        // Continue dragging if mouse is moving while button is down
        if (Mouse.isButtonDown(0) && UIDragger.getInstance().isDragging()) {
            UIDragger.getInstance().updateDragPosition(mouseX, mouseY);
        }
    }

    private int getElementWidth() {
        return 50; // Width of your timer display
    }

    private int getElementHeight() {
        return mc.fontRendererObj.FONT_HEIGHT;
    }

    private void drawRect(int left, int top, int right, int bottom, int color) {
        net.minecraft.client.gui.Gui.drawRect(left, top, right, bottom, color);
    }

    private void startTimer() {
        barrierTicks = 60;
        isTimerActive = true;
        mc.thePlayer.addChatMessage(new ChatComponentText(
                EnumChatFormatting.GOLD + "[RatAllOfYou] " +
                        EnumChatFormatting.GREEN + "Phase 3 Timer Started"
        ));
    }

    private void resetTimer() {
        barrierTicks = 0;
        isTimerActive = false;
    }

    private boolean isModuleEnabled() {
        return BooleanSettings.isEnabled("dungeons_phase3ticktimer");
    }
}
