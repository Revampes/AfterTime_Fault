package com.aftertime.ratallofyou.UI;

import com.aftertime.ratallofyou.UI.config.ConfigData.AllConfig;
import com.aftertime.ratallofyou.UI.config.ConfigData.BaseConfig;
import com.aftertime.ratallofyou.UI.config.ConfigData.UIPosition;
import com.aftertime.ratallofyou.UI.config.ConfigIO;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import java.util.Map;

public class UIHighlighter {
    private static Boolean isInMoveMode = false;
    private static GuiScreen previousScreen = null;
    private final UIDragger uiDragger = UIDragger.getInstance();

    public static boolean isInMoveMode() {
        return isInMoveMode;
    }

    public static void enterMoveMode(GuiScreen currentScreen) {
        isInMoveMode = true;
        previousScreen = currentScreen;
        Minecraft mc = Minecraft.getMinecraft();

        // Show GUI and release mouse
        mc.gameSettings.hideGUI = false;
        mc.mouseHelper.ungrabMouseCursor();
        mc.inGameHasFocus = false;
        Mouse.setGrabbed(false);

        // Close any open GUI screen
        mc.displayGuiScreen(null);
    }

    public static void exitMoveMode() {
        isInMoveMode = false;
        Minecraft mc = Minecraft.getMinecraft();

        // Restore mouse control
        mc.mouseHelper.grabMouseCursor();
        mc.inGameHasFocus = true;
        Mouse.setGrabbed(true);

        // Persist updated UI positions
        ConfigIO.INSTANCE.SaveProperties();

        // Restore previous screen if exists
        if (previousScreen != null) {
            mc.displayGuiScreen(previousScreen);
        }
    }

    @SubscribeEvent
    public void onRender(RenderGameOverlayEvent.Post event) {
        if (!isInMoveMode || event.type != RenderGameOverlayEvent.ElementType.ALL) return;

        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution res = new ScaledResolution(mc);

        // Draw overlay
        drawRect(0, 0, res.getScaledWidth(), res.getScaledHeight(), 0x80000000);
        mc.fontRendererObj.drawStringWithShadow("Drag UI elements - ESC to exit", 10, 10, 0xFFFFFF);

        // Handle dragging
        int mouseX = Mouse.getX() * res.getScaledWidth() / mc.displayWidth;
        int mouseY = res.getScaledHeight() - Mouse.getY() * res.getScaledHeight() / mc.displayHeight - 1;
        for (Map.Entry<String, BaseConfig<?>> entry : AllConfig.INSTANCE.Pos_CONFIGS.entrySet()) {

            int width = 40;
            int height = 50;
            UIPosition pos = (UIPosition) entry.getValue().Data;
            drawRect(
                    pos.x - 2, pos.y - 2,
                    pos.x + width + 2, pos.y + height + 2,
                    uiDragger.isDragging() && uiDragger.getDraggedElement() == pos ?
                            0x80FF0000 : 0x80FFFF00
            );
            if (Mouse.isButtonDown(0)) {


                if (mouseX >= pos.x && mouseX <= pos.x + width &&
                        mouseY >= pos.y && mouseY <= pos.y + height) {

                    if (!uiDragger.isDragging()) {
                        uiDragger.tryStartDrag((UIPosition) entry.getValue().Data, mouseX, mouseY);
                    }
                }


                if (uiDragger.isDragging()) {
                    uiDragger.updateDragPosition(mouseX, mouseY);
                }
            } else if (uiDragger.isDragging()) {
                uiDragger.updatePositions();
            }
        }


    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END && isInMoveMode) {
            // Exit on ESC
            if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
                exitMoveMode();
                return;
            }

            // Block movement by preventing key presses from being processed
            Minecraft mc = Minecraft.getMinecraft();
            if (mc.currentScreen == null) {
                mc.displayGuiScreen(new GuiScreen() {
                    @Override
                    public boolean doesGuiPauseGame() {
                        return false;
                    }
                });
            }
        }
    }

    private void drawRect(int left, int top, int right, int bottom, int color) {
        net.minecraft.client.gui.Gui.drawRect(left, top, right, bottom, color);
    }
}