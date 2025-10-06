package com.aftertime.ratallofyou.UI.config.drawMethod;

import com.aftertime.ratallofyou.UI.config.ModSettingsGui;
import net.minecraft.client.gui.FontRenderer;

import java.util.ArrayList;
import java.util.List;

public class drawTooltip {
    private final ModSettingsGui gui;

    public drawTooltip(ModSettingsGui gui) {
        this.gui = gui;
    }

    public void drawTooltip(String text, int mouseX, int mouseY) {
        FontRenderer fontRendererObj = gui.getFontRendererObj();
        if (text == null || text.isEmpty()) return;

        List<String> lines = new ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();
        int maxWidth = 200; // Max tooltip width

        // Word wrap the tooltip text
        for (String word : words) {
            String testLine = currentLine.length() == 0 ? word : currentLine + " " + word;
            if (fontRendererObj.getStringWidth(testLine) <= maxWidth) {
                currentLine = new StringBuilder(testLine);
            } else {
                if (currentLine.length() > 0) {
                    lines.add(currentLine.toString());
                    currentLine = new StringBuilder(word);
                } else {
                    lines.add(word); // Single word longer than max width
                }
            }
        }
        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }

        // Calculate tooltip dimensions
        int tooltipWidth = 0;
        for (String line : lines) {
            tooltipWidth = Math.max(tooltipWidth, fontRendererObj.getStringWidth(line));
        }
        int tooltipHeight = lines.size() * 10 + 4;

        // Position tooltip to avoid screen edges
        int tooltipX = mouseX + 12;
        int tooltipY = mouseY - 12;

        if (tooltipX + tooltipWidth + 8 > gui.width) {
            tooltipX = mouseX - tooltipWidth - 12;
        }
        if (tooltipY + tooltipHeight + 8 > gui.height) {
            tooltipY = mouseY - tooltipHeight - 12;
        }
        if (tooltipY < 0) {
            tooltipY = mouseY + 12;
        }

        // Draw tooltip background
        gui.drawRect(tooltipX - 3, tooltipY - 3, tooltipX + tooltipWidth + 3, tooltipY + tooltipHeight + 3, 0xF0100010);
        gui.drawRect(tooltipX - 2, tooltipY - 2, tooltipX + tooltipWidth + 2, tooltipY + tooltipHeight + 2, 0x505000FF);

        // Draw tooltip text
        for (int i = 0; i < lines.size(); i++) {
            fontRendererObj.drawStringWithShadow(lines.get(i), tooltipX, tooltipY + i * 10, 0xFFFFFFFF);
        }
    }
}
