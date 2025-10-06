package com.aftertime.ratallofyou.UI.config.handler;

import com.aftertime.ratallofyou.UI.config.ModSettingsGui;
import net.minecraft.client.gui.GuiButton;

public class handleCategoryButtonClicks {
    private final ModSettingsGui gui;

    public handleCategoryButtonClicks(ModSettingsGui gui) {
        this.gui = gui;
    }

    public void handleCategoryButtonClicks(int mouseX, int mouseY) {
        for (GuiButton btn : gui.categoryButtons) {
            if (mouseX >= btn.xPosition && mouseX <= btn.xPosition + btn.width &&
                    mouseY >= btn.yPosition && mouseY <= btn.yPosition + btn.height) {
                // Manually trigger the category selection logic
                gui.selectedCategory = btn.displayString;
                gui.mainScroll.reset();
                gui.showCommandSettings = false;
                gui.SelectedModule = null;
                gui.useSidePanelForSelected = false;
                gui.optionsInline = false;
                gui.buildModuleButtons();
                return;
            }
        }
    }
}
