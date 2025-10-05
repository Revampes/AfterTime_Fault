package com.aftertime.ratallofyou.UI.config.drawMethod;

import com.aftertime.ratallofyou.UI.config.ModSettingsGui;
import com.aftertime.ratallofyou.UI.config.OptionElements.ModuleButton;
import com.aftertime.ratallofyou.UI.config.commonConstant.Colors;
import com.aftertime.ratallofyou.UI.config.commonConstant.Dimensions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_SCISSOR_TEST;
import static org.lwjgl.opengl.GL11.glDisable;

public class drawModule {
    private final ModSettingsGui gui;

    public drawModule(ModSettingsGui gui) {
        this.gui = gui;
    }

    public void drawModules(int mouseX, int mouseY) {
        FontRenderer fontRenderer = gui.getFontRendererObj();
        int scissorX = gui.guiLeft + 115;
        int scissorY = gui.guiTop + 25;
        int scissorWidth = Dimensions.GUI_WIDTH - 120 - Dimensions.SCROLLBAR_WIDTH;
        int scissorHeight = Dimensions.GUI_HEIGHT - 70;
        int scale = new ScaledResolution(Minecraft.getMinecraft()).getScaleFactor();
        glEnable(GL_SCISSOR_TEST);
        glScissor(scissorX * scale, (gui.height - (scissorY + scissorHeight)) * scale, scissorWidth * scale, scissorHeight * scale);

        // Draw category tag at the top for each category
        int listY = gui.guiTop + 28;
        int categoryTagY = listY - gui.mainScroll.getOffset();
        String categoryTag = "------ " + gui.selectedCategory + " ------";
        int tagWidth = fontRenderer.getStringWidth(categoryTag);
        int centerX = scissorX + scissorWidth / 2;
        fontRenderer.drawStringWithShadow(categoryTag, centerX - tagWidth / 2, categoryTagY, Colors.VERSION);

        for (ModuleButton moduleBtn :gui.moduleButtons) {
            moduleBtn.draw(mouseX, mouseY, 0, fontRenderer);
            if (gui.showCommandSettings && gui.optionsInline && gui.SelectedModule != null && moduleBtn.getModule() == gui.SelectedModule) {
                gui.drawInlineSettingsBox(mouseX, mouseY);
            }
        }
        glDisable(GL_SCISSOR_TEST);
    }
}
