package com.aftertime.ratallofyou.UI.config.handler;

import com.aftertime.ratallofyou.UI.config.ModSettingsGui;
import com.aftertime.ratallofyou.UI.config.commonConstant.Dimensions;
import org.lwjgl.input.Mouse;

import java.io.IOException;

public class handleMouseInput {
    private final ModSettingsGui gui;

    public handleMouseInput(ModSettingsGui gui) {
        this.gui = gui;
    }

    public void handleMouseInput() throws IOException {

        int dWheel = Mouse.getEventDWheel();
        if (dWheel != 0) {
            int mouseX = Mouse.getEventX() * gui.width / gui.mc.displayWidth;
            int mouseY = gui.height - Mouse.getEventY() * gui.height / gui.mc.displayHeight - 1;

            // Normalize scroll direction and amount
            int scrollDirection = dWheel > 0 ? -1 : 1;
            int scrollAmount = 15; // Pixels to scroll per wheel notch

            // Check if mouse is over the main module list area
            int moduleListX = gui.guiLeft + 115;
            int moduleListY = gui.guiTop + 25;
            int moduleListWidth = Dimensions.GUI_WIDTH - 120 - Dimensions.SCROLLBAR_WIDTH;
            int moduleListHeight = Dimensions.GUI_HEIGHT - 50;

            boolean overModuleList = mouseX >= moduleListX && mouseX <= moduleListX + moduleListWidth &&
                    mouseY >= moduleListY && mouseY <= moduleListY + moduleListHeight;

            // Check if mouse is over command panel area (when visible)
            boolean overCommandPanel = false;
            if (gui.showCommandSettings && gui.useSidePanelForSelected) {
                int panelX = gui.guiLeft + Dimensions.COMMAND_PANEL_X;
                int panelY = gui.guiTop + Dimensions.COMMAND_PANEL_Y;
                int panelWidth = Dimensions.COMMAND_PANEL_WIDTH;
                int panelHeight = Dimensions.GUI_HEIGHT - 60;
                overCommandPanel = mouseX >= panelX && mouseX <= panelX + panelWidth &&
                        mouseY >= panelY && mouseY <= panelY + panelHeight;
            }

            // Check if mouse is over Fast Hotkey detail panel (when visible)
            boolean overDetailPanel = false;
            if (gui.showCommandSettings && gui.optionsInline && gui.SelectedModule != null && "Fast Hotkey".equals(gui.SelectedModule.name) && gui.fhkSelectedPreset >= 0) {
                int detailX = gui.getInlineDetailX();
                int detailW = 170;
                int detailY = gui.guiTop + Dimensions.COMMAND_PANEL_Y;
                int detailH = Dimensions.GUI_HEIGHT - 60;
                overDetailPanel = mouseX >= detailX && mouseX <= detailX + detailW &&
                        mouseY >= detailY && mouseY <= detailY + detailH;
            }

            // Apply scrolling to the appropriate scroll manager
            if (overCommandPanel || overDetailPanel) {
                // Scroll the command panel
                gui.commandScroll.scroll(scrollDirection * scrollAmount);
            } else if (overModuleList) {
                // Scroll the main module list
                gui.mainScroll.scroll(scrollDirection * scrollAmount);
                gui.buildModuleButtons(); // Rebuild to update positions
            }
        }
    }
}
