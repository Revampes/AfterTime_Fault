package com.aftertime.ratallofyou.UI.buildMethod;

import com.aftertime.ratallofyou.UI.config.ConfigData.AllConfig;
import com.aftertime.ratallofyou.UI.config.ModSettingsGui;
import net.minecraft.client.gui.GuiButton;
import net.minecraftforge.fml.common.Mod;

public class buildCategoryButtons {
    private final ModSettingsGui gui;

    public buildCategoryButtons(ModSettingsGui gui) {
        this.gui = gui;
    }

    public void buildCategoryButtons() {
        gui.categoryButtons.clear();
        int y = gui.guiTop + 30; int x = gui.guiLeft + 10;
        for (int i = 0; i < AllConfig.INSTANCE.Categories.size(); i++) {
            GuiButton b = new GuiButton(1000 + i, x, y, 95, 18, AllConfig.INSTANCE.Categories.get(i));
            gui.categoryButtons.add(b);
            y += 20;
        }
    }
}
