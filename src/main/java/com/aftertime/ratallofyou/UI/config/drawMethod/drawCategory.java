package com.aftertime.ratallofyou.UI.config.drawMethod;

import com.aftertime.ratallofyou.UI.config.ModSettingsGui;
import com.aftertime.ratallofyou.UI.config.commonConstant.Colors;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;

public class drawCategory {
    private final ModSettingsGui gui;

    public drawCategory(ModSettingsGui gui) {
        this.gui = gui;
    }

    public void drawCategories() {
        FontRenderer fontRenderer = gui.getFontRendererObj();
        for (GuiButton btn : gui.categoryButtons) {
            boolean isSelected = btn.displayString.equals(gui.selectedCategory);
            int bgColor = isSelected ? Colors.SELECTED_CATEGORY : Colors.CATEGORY_BUTTON;
            gui.drawRect(btn.xPosition, btn.yPosition, btn.xPosition + btn.width, btn.yPosition + btn.height, bgColor);

            int textColor = isSelected ? Colors.TEXT_BLUE : Colors.TEXT;
            gui.drawCenteredString(fontRenderer, btn.displayString,
                    btn.xPosition + btn.width / 2,
                    btn.yPosition + (btn.height - 8) / 2,
                    textColor);
        }
    }
}
