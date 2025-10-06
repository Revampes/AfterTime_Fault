package com.aftertime.ratallofyou.UI.config.drawMethod;

import com.aftertime.ratallofyou.UI.config.ModSettingsGui;
import com.aftertime.ratallofyou.UI.config.commonConstant.Colors;
import com.aftertime.ratallofyou.UI.config.commonConstant.Dimensions;
import net.minecraft.client.gui.FontRenderer;

public class drawBackground {
    private final ModSettingsGui gui;

    public drawBackground(ModSettingsGui gui) {
        this.gui = gui;
    }

    public void drawBackground() {
        FontRenderer fontRenderer = gui.getFontRendererObj();
        gui.drawRect(gui.guiLeft, gui.guiTop, gui.guiLeft + Dimensions.GUI_WIDTH, gui.guiTop + Dimensions.GUI_HEIGHT, Colors.PANEL);
        fontRenderer.drawStringWithShadow("§l§nAfterTimeFault", gui.guiLeft + 15, gui.guiTop + 10, Colors.TEXT);
        gui.drawRect(gui.guiLeft + 5, gui.guiTop + 25, gui.guiLeft + 115, gui.guiTop + Dimensions.GUI_HEIGHT - 5, Colors.CATEGORY);
        gui.drawRect(gui.guiLeft + 115, gui.guiTop + 25, gui.guiLeft + Dimensions.GUI_WIDTH - 5, gui.guiTop + Dimensions.GUI_HEIGHT - 5, Colors.CATEGORY);

        // Enhanced footer with version, author, and instructions
        String versionText = "§7Version v2.2 §8| §7Created by AfterTime";
        String instructionText = "§8Left Click: Toggle | Right Click: Settings | Hover: Description";
        gui.drawCenteredString(fontRenderer, versionText, gui.width / 2, gui.guiTop + Dimensions.GUI_HEIGHT - 30, Colors.VERSION);
        gui.drawCenteredString(fontRenderer, instructionText, gui.width / 2, gui.guiTop + Dimensions.GUI_HEIGHT - 18, Colors.VERSION);
    }
}
