package com.aftertime.ratallofyou.UI.config.handler;

import com.aftertime.ratallofyou.UI.config.ModSettingsGui;
import com.aftertime.ratallofyou.UI.config.OptionElements.ColorInput;
import com.aftertime.ratallofyou.UI.config.OptionElements.LabelledInput;
import com.aftertime.ratallofyou.UI.config.OptionElements.MethodDropdown;
import com.aftertime.ratallofyou.UI.config.OptionElements.Toggle;
import com.aftertime.ratallofyou.UI.config.commonConstant.Dimensions;

public class handleDropdownClicks {
    private final ModSettingsGui gui;

    public handleDropdownClicks(ModSettingsGui gui) {
        this.gui = gui;
    }

    public boolean handleDropdownClicks(int mouseX, int mouseY) {
        if (gui.SelectedModule == null) return false;
        int y = gui.guiTop + com.aftertime.ratallofyou.UI.config.commonConstant.Dimensions.COMMAND_PANEL_Y + 30 - gui.commandScroll.getOffset();
        for (Toggle ignored : gui.Toggles) y += 22;
        for (LabelledInput li : gui.labelledInputs) y += li.getVerticalSpace();
        for (ColorInput ignored : gui.ColorInputs) y += 50;
        for (MethodDropdown dd : gui.methodDropdowns) {
            int bx = dd.x + 100, bw = dd.width - 100, bh = dd.height;
            int by = y; // Use calculated yPos for dropdown
            // Debug output for troubleshooting
            System.out.println("Dropdown: " + dd.name + " at x=" + bx + ", y=" + by + ", w=" + bw + ", h=" + bh + ", mouseX=" + mouseX + ", mouseY=" + mouseY);
            boolean inBase = mouseX >= bx && mouseX <= bx + bw && mouseY >= by && mouseY <= by + bh;
            if (inBase) {
                for (MethodDropdown other : gui.methodDropdowns) other.isOpen = false;
                dd.isOpen = !dd.isOpen;
                System.out.println("Dropdown base clicked: " + dd.name + ", now isOpen=" + dd.isOpen);
                return true;
            }
            if (dd.isOpen) {
                for (int i = 0; i < dd.methods.length; i++) {
                    int optionY = by + bh + (i * bh);
                    boolean inOpt = mouseX >= bx && mouseX <= bx + bw && mouseY >= optionY && mouseY <= optionY + bh;
                    if (inOpt) {
                        dd.selectMethod(i);
                        dd.isOpen = false;
                        System.out.println("Dropdown option clicked: " + dd.name + ", selected=" + dd.methods[i]);
                        return true;
                    }
                }
            }
            y += 22;
        }
        return false;
    }
}
