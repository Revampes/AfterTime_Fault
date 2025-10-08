package com.aftertime.ratallofyou.UI.buildMethod;

import com.aftertime.ratallofyou.UI.config.ConfigData.AllConfig;
import com.aftertime.ratallofyou.UI.config.ConfigData.BaseConfig;
import com.aftertime.ratallofyou.UI.config.ConfigData.ModuleInfo;
import com.aftertime.ratallofyou.UI.config.ModSettingsGui;
import com.aftertime.ratallofyou.UI.config.OptionElements.ModuleButton;
import com.aftertime.ratallofyou.UI.config.commonConstant.Dimensions;

public class buildModuleButtons {
    private final ModSettingsGui gui;

    public buildModuleButtons(ModSettingsGui gui) {
        this.gui = gui;
    }

    public void buildModuleButtons() {
        gui.moduleButtons.clear(); int listX = gui.guiLeft + 120; int listY = gui.guiTop + 28; int listW = Dimensions.GUI_WIDTH - 120 - 10 - Dimensions.SCROLLBAR_WIDTH; int y = listY - gui.mainScroll.getOffset(); int rowH = 20; int usedHeight = 0;
        for (BaseConfig<?> mi : AllConfig.INSTANCE.MODULES.values()) {
            ModuleInfo info = (ModuleInfo) mi; if (!info.category.equals(gui.selectedCategory)) continue;
            boolean hasSettings = gui.hasSettings(info); gui.moduleButtons.add(new ModuleButton(listX + 4, y, listW - 8, rowH - 2, info, hasSettings));
            int inc = rowH; if (gui.showCommandSettings && gui.optionsInline && gui.SelectedModule == info) { inc += 20 + gui.computeInlineContentHeight() + 8; }
            y += inc; usedHeight += inc;
        }
        int totalHeight = usedHeight; int viewH = Dimensions.GUI_HEIGHT - 70; gui.mainScroll.update(totalHeight, viewH); gui.mainScroll.updateScrollbarPosition(listX + listW - 2, listY, viewH);
    }
}
