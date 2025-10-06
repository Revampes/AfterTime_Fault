package com.aftertime.ratallofyou.UI.config.drawMethod;

import com.aftertime.ratallofyou.UI.config.ModSettingsGui;
import com.aftertime.ratallofyou.UI.config.OptionElements.FastRow;
import com.aftertime.ratallofyou.UI.config.commonConstant.Colors;
import com.aftertime.ratallofyou.UI.config.commonConstant.Dimensions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_SCISSOR_TEST;
import static org.lwjgl.opengl.GL11.glDisable;

public class drawFastHotKeyDetailPanel {
    private final ModSettingsGui gui;

    public drawFastHotKeyDetailPanel(ModSettingsGui gui) {
        this.gui = gui;
    }

    public void drawFastHotkeyDetailPanel(int mouseX, int mouseY, int panelX, int panelY, int panelWidth, int panelHeight) {
        FontRenderer fontRendererObj = gui.getFontRendererObj();
        int detailX = gui.useSidePanelForSelected ? (panelX + panelWidth + 6) : (gui.optionsInline && gui.SelectedModule != null && "Fast Hotkey".equals(gui.SelectedModule.name) ? gui.getInlineDetailX() : panelX);
        int detailW = 170; int detailY = panelY; int detailH = panelHeight;
        gui.drawRect(detailX, detailY, detailX + detailW, detailY + detailH, Colors.COMMAND_PANEL);
        gui.drawRect(detailX - 1, detailY - 1, detailX + detailW + 1, detailY + detailH + 1, Colors.COMMAND_BORDER);
        gui.drawCenteredString(fontRendererObj, "Preset Editor", detailX + detailW / 2, detailY + 5, Colors.COMMAND_TEXT);
        int scale = new ScaledResolution(Minecraft.getMinecraft()).getScaleFactor(); glEnable(GL_SCISSOR_TEST);
        glScissor(detailX * scale, (gui.height - (detailY + detailH)) * scale, detailW * scale, (detailH - 25) * scale);
        int x = detailX + 5; int w = detailW - 10; int contentY = panelY + 25 - gui.commandScroll.getOffset();
        for (int i = 0; i < gui.fastRows.size(); i++) {
            FastRow row = gui.fastRows.get(i); int rowTop = contentY + i * Dimensions.FH_ROW_HEIGHT;
            if (rowTop + Dimensions.FH_ROW_HEIGHT < detailY + 25 || rowTop > detailY + detailH) continue;
            gui.drawRect(x, rowTop - 2, x + w, rowTop - 1, 0x33000000);
            int title1Y = rowTop + 2; int labelInputY = title1Y + 12; int title2Y = labelInputY + Dimensions.FH_INPUT_HEIGHT + Dimensions.FH_GAP_Y + 4; int commandInputY = title2Y + 12;
            fontRendererObj.drawStringWithShadow("Command " + (i + 1) + " Label:", x, title1Y, Colors.COMMAND_TEXT);
            fontRendererObj.drawStringWithShadow("Command " + (i + 1) + " Command:", x, title2Y, Colors.COMMAND_TEXT);
            row.DrawElements(mouseX, mouseY, labelInputY, commandInputY);
            int removeX = x, removeY = commandInputY + Dimensions.FH_INPUT_HEIGHT + Dimensions.FH_GAP_Y;
            gui.drawRect(removeX, removeY, removeX + Dimensions.FH_REMOVE_WIDTH, removeY + Dimensions.FH_REMOVE_HEIGHT, Colors.BUTTON_RED);
            gui.drawCenteredString(fontRendererObj, "Remove", removeX + Dimensions.FH_REMOVE_WIDTH / 2, removeY + 5, Colors.BUTTON_TEXT);
        }
        int addY = contentY + gui.fastRows.size() * Dimensions.FH_ROW_HEIGHT + 8; int addW = 60;
        gui.drawRect(x, addY, x + addW, addY + Dimensions.FH_ADD_HEIGHT, Colors.BUTTON_GREEN);
        gui.drawCenteredString(fontRendererObj, "Add", x + addW / 2, addY + 6, Colors.BUTTON_TEXT);
        glDisable(GL_SCISSOR_TEST);
        // When in inline mode (no left panel), maintain scrollbar for the detail region
        if (!gui.useSidePanelForSelected) {
            int rowsHeight = (gui.fastRows.size() * Dimensions.FH_ROW_HEIGHT + 8 + Dimensions.FH_ADD_HEIGHT);
            int viewH = panelHeight - 25;
            gui.commandScroll.update(rowsHeight, viewH);
            gui.commandScroll.updateScrollbarPosition(detailX + detailW - Dimensions.SCROLLBAR_WIDTH - 2, panelY + 25, viewH);
        }
    }
}
