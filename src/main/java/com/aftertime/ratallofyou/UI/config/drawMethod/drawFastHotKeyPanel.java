package com.aftertime.ratallofyou.UI.config.drawMethod;

import com.aftertime.ratallofyou.UI.config.ConfigData.AllConfig;
import com.aftertime.ratallofyou.UI.config.ModSettingsGui;
import com.aftertime.ratallofyou.UI.config.OptionElements.ColorInput;
import com.aftertime.ratallofyou.UI.config.OptionElements.LabelledInput;
import com.aftertime.ratallofyou.UI.config.OptionElements.Toggle;
import com.aftertime.ratallofyou.UI.config.commonConstant.Colors;
import com.aftertime.ratallofyou.UI.config.commonConstant.Dimensions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_SCISSOR_TEST;
import static org.lwjgl.opengl.GL11.glDisable;

public class drawFastHotKeyPanel {
    private final ModSettingsGui gui;

    public drawFastHotKeyPanel(ModSettingsGui gui) {
        this.gui = gui;
    }

    public void drawFastHotKeyPanel(int mouseX, int mouseY, int panelX, int panelY, int panelWidth, int panelHeight) {
        FontRenderer fontRenderer = gui.getFontRendererObj();
        int scale = new ScaledResolution(Minecraft.getMinecraft()).getScaleFactor();
        glEnable(GL_SCISSOR_TEST);
        glScissor(panelX * scale, (gui.height - (panelY + panelHeight)) * scale, panelWidth * scale, (panelHeight - 25) * scale);
        int y = panelY + 25 - gui.commandScroll.getOffset(); int x = panelX + 5; int w = panelWidth - 10;
        fontRenderer.drawStringWithShadow("Create settings:", x, y, Colors.COMMAND_TEXT); y += 12;
        if (gui.fhkPresetNameInput != null) {
            gui.fhkPresetNameInput.setBounds(x, y, Math.max(60, w - 65), 16); gui.fhkPresetNameInput.draw(mouseX, mouseY);
            int btnX = x + w - 60; gui.drawRect(btnX, y, btnX + 60, y + 16, Colors.BUTTON_GREEN); gui.drawCenteredString(fontRenderer, "Confirm", btnX + 30, y + 4, Colors.BUTTON_TEXT); y += 22;
        }
        fontRenderer.drawStringWithShadow("Saved settings:", x, y, Colors.COMMAND_TEXT); y += 12;
        int presetBtnH = 16;
        for (int i = 0; i < AllConfig.INSTANCE.FHK_PRESETS.size(); i++) {
            int openW = Math.max(60, w - 70);
            int openX = x, openY = y;
            boolean isActive = (i == AllConfig.INSTANCE.FHK_ACTIVE_PRESET);
            gui.drawRect(openX, openY, openX + openW, openY + presetBtnH, isActive ? Colors.BUTTON_GREEN : Colors.BUTTON_RED);
            gui.drawCenteredString(fontRenderer, AllConfig.INSTANCE.FHK_PRESETS.get(i).name + (isActive ? "  (Active)" : ""), openX + openW / 2, openY + 4, Colors.BUTTON_TEXT);
            int rmX = x + w - 60; gui.drawRect(rmX, openY, rmX + 60, openY + presetBtnH, Colors.BUTTON_RED); gui.drawCenteredString(fontRenderer, "Remove", rmX + 30, openY + 4, Colors.BUTTON_TEXT);
            y += presetBtnH + 4;
        }
        gui.drawRect(x, y, x + w, y + 1, 0x33000000); y += 6;
        // Appearance options
        for (Toggle t : gui.Toggles) { t.draw(mouseX, mouseY, y, fontRenderer); y += 22; }
        for (LabelledInput t : gui.labelledInputs) { t.draw(mouseX, mouseY, y, fontRenderer); y += t.getVerticalSpace(); }
        for (ColorInput t : gui.ColorInputs) { t.draw(mouseX, mouseY, y, fontRenderer); y += 50; }
        glDisable(GL_SCISSOR_TEST);
        // Scrollbar sizing: include detail rows height to share one scroll
        int optionsHeight = (12 + 22) + (12 + AllConfig.INSTANCE.FHK_PRESETS.size() * (presetBtnH + 4)) + (gui.Toggles.size() * 22);
        for (LabelledInput li : gui.labelledInputs) optionsHeight += li.getVerticalSpace(); optionsHeight += gui.ColorInputs.size() * 50;
        int rowsHeight = (gui.fhkSelectedPreset >= 0 ? (gui.fastRows.size() * Dimensions.FH_ROW_HEIGHT + 8 + Dimensions.FH_ADD_HEIGHT) : 0);
        int totalHeight = Math.max(optionsHeight, rowsHeight);
        gui.commandScroll.update(totalHeight, panelHeight - 25);
        if (gui.commandScroll.shouldRenderScrollbar()) gui.commandScroll.updateScrollbarPosition(panelX + panelWidth - Dimensions.SCROLLBAR_WIDTH - 2, panelY + 25, panelHeight - 25);
    }
}
