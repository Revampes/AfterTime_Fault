package com.aftertime.ratallofyou.UI.config.drawMethod;

import com.aftertime.ratallofyou.UI.config.ConfigData.AllConfig;
import com.aftertime.ratallofyou.UI.config.ModSettingsGui;
import com.aftertime.ratallofyou.UI.config.OptionElements.ColorInput;
import com.aftertime.ratallofyou.UI.config.OptionElements.LabelledInput;
import com.aftertime.ratallofyou.UI.config.OptionElements.Toggle;
import com.aftertime.ratallofyou.UI.config.commonConstant.Colors;
import com.aftertime.ratallofyou.UI.config.commonConstant.Dimensions;
import net.minecraft.client.gui.FontRenderer;

public class drawCommandPanel {
    private final ModSettingsGui gui;

    public drawCommandPanel(ModSettingsGui gui) {
        this.gui = gui;
    }

    public void drawCommandPanel(int mouseX, int mouseY) {
        FontRenderer fontRenderer = gui.getFontRendererObj();
        // If not using side panel, still allow right-only detail panel for Fast Hotkey
        if (!gui.showCommandSettings || gui.SelectedModule == null) return;
        if (gui.optionsInline && "Fast Hotkey".equals(gui.SelectedModule.name)) {
            if (gui.fhkSelectedPreset >= 0) {
                int panelX = gui.guiLeft + Dimensions.COMMAND_PANEL_X;
                int panelY = gui.guiTop + Dimensions.COMMAND_PANEL_Y;
                int panelWidth = Dimensions.COMMAND_PANEL_WIDTH;
                int panelHeight = Dimensions.GUI_HEIGHT - 60;
                // Only draw the right detail panel (no left panel)
                gui.drawFastHotkeyDetailPanel(mouseX, mouseY, panelX, panelY, panelWidth, panelHeight);
            }
            return;
        }
        if (!gui.useSidePanelForSelected) return;
        int panelX = gui.guiLeft + Dimensions.COMMAND_PANEL_X;
        int panelY = gui.guiTop + Dimensions.COMMAND_PANEL_Y;
        int panelWidth = Dimensions.COMMAND_PANEL_WIDTH;
        int panelHeight = Dimensions.GUI_HEIGHT - 60;
        gui.drawRect(panelX, panelY, panelX + panelWidth, panelY + panelHeight, Colors.COMMAND_PANEL);
        gui.drawRect(panelX - 1, panelY - 1, panelX + panelWidth + 1, panelY + panelHeight + 1, Colors.COMMAND_BORDER);
        gui.drawCenteredString(fontRenderer, gui.getCommandPanelTitle(), panelX + panelWidth / 2, panelY + 5, Colors.COMMAND_TEXT);
        if ("Fast Hotkey".equals(gui.SelectedModule.name)) {
            gui.drawFastHotKeyPanel(mouseX, mouseY, panelX, panelY, panelWidth, panelHeight);
            if (gui.fhkSelectedPreset >= 0) gui.drawFastHotkeyDetailPanel(mouseX, mouseY, panelX, panelY, panelWidth, panelHeight);
            return;
        }
        // Inject module-specific sub-settings
        int y = panelY + 30 - gui.commandScroll.getOffset();
        switch (gui.SelectedModule.name) {
            case "Party Commands": gui.Add_SubSetting_Command(y); break;
            case "No Debuff": gui.Add_SubSetting_NoDebuff(y); break;
            case "Etherwarp Overlay": gui.Add_SubSetting_Etherwarp(y); break;
            case "Fast Hotkey": gui.Add_SubSetting_FastHotkey(y); break;
            case "Chest Open Notice": gui.Add_SubSetting_ChestOpen(y); break;
            case "Hotbar Swap": gui.Add_SubSetting_HotbarSwap(y); gui.hotbarPanel.rebuildRows(); break;
            case "Auto Fish": gui.Add_SubSetting_AutoFish(y); break;
            case "Auto Sell": gui.Add_SubSetting_AutoSell(y); break;
            case "Auto Experiment": gui.Add_SubSetting_AutoExperiment(y); break;
            case "NameTag": gui.Add_SubSetting_NameTag(y); break; // New
            case "Player ESP": gui.Add_SubSetting_PlayerESP(y); break; //
            case "DarkMode": gui.Add_SubSetting_DarkMode(y); break;
            case "Custom Cape": gui.Add_SubSetting_CustomCape(y); break;
            case "Mark Location": gui.Add_SubSetting_MarkLocation(y); break;
        }
        // Draw all UI elements in order
        y = panelY + 30 - gui.commandScroll.getOffset();
        for (Toggle t : gui.Toggles) {
            t.draw(mouseX, mouseY, y, fontRenderer);
            y += 22;
        }
        for (LabelledInput li : gui.labelledInputs) {
            li.draw(mouseX, mouseY, y, fontRenderer);
            y += li.getVerticalSpace();
        }
        for (ColorInput ci : gui.ColorInputs) {
            ci.draw(mouseX, mouseY, y, fontRenderer);
            y += 50;
        }
        for (com.aftertime.ratallofyou.UI.config.OptionElements.MethodDropdown dd : gui.methodDropdowns) {
            dd.drawBase(mouseX, mouseY, y, fontRenderer);
            if (dd.isOpen) {
                dd.drawExpandedOptions(mouseX, mouseY, y, fontRenderer);
            }
            y += 22;
        }
        int contentHeight = 0; if (gui.useSidePanelForSelected && "Fast Hotkey".equals(gui.SelectedModule.name)) contentHeight += 12 + 22 + 12 + (AllConfig.INSTANCE.FHK_PRESETS.size() * (16 + 4));
        contentHeight += gui.Toggles.size() * 22; for (LabelledInput li : gui.labelledInputs) contentHeight += li.getVerticalSpace(); contentHeight += gui.ColorInputs.size() * 50; contentHeight += gui.methodDropdowns.size() * 22;
        int panelViewHeight = Dimensions.GUI_HEIGHT - 60 - 25;
        if (gui.useSidePanelForSelected) {
            gui.commandScroll.update(contentHeight, panelViewHeight); gui.commandScroll.updateScrollbarPosition(gui.guiLeft + Dimensions.COMMAND_PANEL_X + Dimensions.COMMAND_PANEL_WIDTH - Dimensions.SCROLLBAR_WIDTH - 2, gui.guiTop + Dimensions.COMMAND_PANEL_Y + 25, panelViewHeight);
        }
    }
}
