package com.aftertime.ratallofyou.UI.config.handler;

import com.aftertime.ratallofyou.UI.config.ModSettingsGui;
import com.aftertime.ratallofyou.UI.config.OptionElements.ModuleButton;
import com.aftertime.ratallofyou.UI.config.commonConstant.Dimensions;

public class handleModuleButtonClicks {
    private final ModSettingsGui gui;

    public handleModuleButtonClicks(ModSettingsGui gui) {
        this.gui = gui;
    }

    public void handleModuleButtonClicks(int mouseX, int mouseY) {
        int scissorX = gui.guiLeft + 115;
        int scissorY = gui.guiTop + 25;
        int scissorWidth = Dimensions.GUI_WIDTH - 120 - Dimensions.SCROLLBAR_WIDTH;
        int scissorHeight = Dimensions.GUI_HEIGHT - 70;
        boolean inVisibleArea = mouseX >= scissorX && mouseX <= scissorX + scissorWidth &&
                mouseY >= scissorY && mouseY <= scissorY + scissorHeight;

        for (ModuleButton moduleBtn : gui.moduleButtons) {
            if (inVisibleArea && moduleBtn.isMouseOver(mouseX, mouseY)) {
                gui.drawTooltip(moduleBtn.getModule().description, mouseX, mouseY);
            }
        }
        for (ModuleButton moduleBtn : gui.moduleButtons) {
            if (inVisibleArea && moduleBtn.isMouseOver(mouseX, mouseY)) {
                gui.handleModuleButtonClick(moduleBtn, mouseX, mouseY);
                return;
            }
        }
    }
}
