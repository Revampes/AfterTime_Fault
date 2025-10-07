package com.aftertime.ratallofyou.UI.config.drawMethod;

import com.aftertime.ratallofyou.UI.config.ModSettingsGui;
import com.aftertime.ratallofyou.UI.config.OptionElements.ColorInput;
import com.aftertime.ratallofyou.UI.config.OptionElements.LabelledInput;
import com.aftertime.ratallofyou.UI.config.OptionElements.MethodDropdown;
import com.aftertime.ratallofyou.UI.config.OptionElements.Toggle;
import com.aftertime.ratallofyou.UI.config.commonConstant.Dimensions;

public class drawDropdownOverlays {
    private final ModSettingsGui gui;

    public drawDropdownOverlays(ModSettingsGui gui) {
        this.gui = gui;
    }

    public void drawDropdownOverlays(int mouseX, int mouseY) {
        if (!gui.showCommandSettings || gui.SelectedModule == null) return;
        net.minecraft.client.gui.FontRenderer fr = gui.getFontRendererObj();
        // Side panel overlays: compute baseline same as panel
        if (gui.useSidePanelForSelected && !gui.optionsInline) {
            int y = gui.guiTop + Dimensions.COMMAND_PANEL_Y + 30 - gui.commandScroll.getOffset();
            for (Toggle ignored : gui.Toggles) y += 22;
            for (LabelledInput li : gui.labelledInputs) y += li.getVerticalSpace();
            for (ColorInput ignored : gui.ColorInputs) y += 50;
            for (MethodDropdown dd : gui.methodDropdowns) {
                if (dd.isOpen) dd.drawExpandedOptions(mouseX, mouseY, y, fr);
                y += 22;
            }
        }
        // Inline overlays: use the recorded baseline to ensure exact alignment
        if (gui.optionsInline) {
            int baseY = gui.inlineDropdownBaseY;
            if (baseY >= 0) {
                int y = baseY;
                for (MethodDropdown dd : gui.methodDropdowns) {
                    if (dd.isOpen) dd.drawExpandedOptions(mouseX, mouseY, y, fr);
                    y += 22;
                }
            }
        }
    }
}
