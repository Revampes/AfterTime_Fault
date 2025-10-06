package com.aftertime.ratallofyou.UI.config.drawMethod;

import com.aftertime.ratallofyou.UI.config.ConfigData.ModuleInfo;
import com.aftertime.ratallofyou.UI.config.ModSettingsGui;
import com.aftertime.ratallofyou.UI.config.OptionElements.ModuleButton;
import com.aftertime.ratallofyou.UI.config.commonConstant.Dimensions;
import net.minecraft.client.gui.FontRenderer;

public class drawTooltipsAndErrors {
    private final ModSettingsGui gui;

    public drawTooltipsAndErrors(ModSettingsGui gui) {
        this.gui = gui;
    }

    // Draw tooltips and error messages
    public void drawTooltipsAndErrors(int mouseX, int mouseY) {
        FontRenderer fontRendererObj = gui.getFontRendererObj();
        // Show error message for modules without settings
        if (gui.showNoSettingsError != null && System.currentTimeMillis() - gui.noSettingsErrorTime < 3000) {
            String errorMsg = "§c" + gui.showNoSettingsError + " does not have any sub-settings";
            int msgWidth = fontRendererObj.getStringWidth(errorMsg);
            int msgX = gui.width / 2 - msgWidth / 2;
            int msgY = gui.guiTop + Dimensions.GUI_HEIGHT - 45;

            // Draw background
            gui.drawRect(msgX - 4, msgY - 2, msgX + msgWidth + 4, msgY + 10, 0x99000000);
            gui.drawRect(msgX - 5, msgY - 3, msgX + msgWidth + 5, msgY + 11, 0xFFCC0000);

            // Draw text
            fontRendererObj.drawStringWithShadow(errorMsg, msgX, msgY, 0xFFFFFFFF);
        } else if (System.currentTimeMillis() - gui.noSettingsErrorTime >= 3000) {
            gui.showNoSettingsError = null; // Clear expired error
        }

        // Show tooltips for module buttons on hover
        for (ModuleButton moduleBtn : gui.moduleButtons) {
            if (moduleBtn.isMouseOver(mouseX, mouseY)) {
                ModuleInfo module = moduleBtn.getModule();
                if (module.description != null && !module.description.isEmpty()) {
                    gui.drawTooltip(module.description, mouseX, mouseY);
                }
                break; // Only show one tooltip at a time
            }
        }
    }
}
