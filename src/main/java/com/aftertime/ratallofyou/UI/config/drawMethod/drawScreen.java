package com.aftertime.ratallofyou.UI.config.drawMethod;

import com.aftertime.ratallofyou.UI.config.ModSettingsGui;
import net.minecraftforge.fml.common.Mod;

public class drawScreen {
    private final ModSettingsGui gui;

    public drawScreen(ModSettingsGui gui) {
        this.gui = gui;
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        gui.drawBackground();
        gui.drawCategories();
        gui.drawModules(mouseX, mouseY);
        gui.drawScrollbars();
        gui.drawCommandPanel(mouseX, mouseY);
        gui.drawDropdownOverlays(mouseX, mouseY);
        gui.drawTooltipsAndErrors(mouseX, mouseY);
    }
}
