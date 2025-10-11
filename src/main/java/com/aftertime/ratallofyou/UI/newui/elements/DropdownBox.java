package com.aftertime.ratallofyou.UI.newui.elements;

import net.minecraft.client.gui.Gui;
import java.util.List;
import java.util.ArrayList;

public class DropdownBox extends UIElement {
    private List<String> options;
    private int selectedIndex;
    private String title;
    private boolean expanded = false;
    private Runnable onChange;

    public DropdownBox(int x, int y, int width, int height, String title, String[] options, int initialIndex, Runnable onChange) {
        super(x, y, width, height);
        this.title = title;
        this.onChange = onChange;
        this.options = new ArrayList<>();
        for (String option : options) {
            this.options.add(option);
        }
        this.selectedIndex = Math.max(0, Math.min(initialIndex, options.length - 1));
    }

    @Override
    public void draw(int mouseX, int mouseY) {
        if (!visible) return;

        hovered = isMouseOver(mouseX, mouseY);

        // Draw title
        fontRenderer.drawString(title, x, y - 12, 0xFFFFFFFF);

        // Draw main dropdown box
        int bgColor = hovered ? 0xFF555555 : 0xFF444444;
        Gui.drawRect(x, y, x + width, y + height, bgColor);

        // Draw border
        Gui.drawRect(x, y, x + width, y + 1, 0xFF000000);
        Gui.drawRect(x, y + height - 1, x + width, y + height, 0xFF000000);
        Gui.drawRect(x, y, x + 1, y + height, 0xFF000000);
        Gui.drawRect(x + width - 1, y, x + width, y + height, 0xFF000000);

        // Draw selected option
        if (selectedIndex >= 0 && selectedIndex < options.size()) {
            String selectedText = options.get(selectedIndex);
            fontRenderer.drawString(selectedText, x + 5, y + (height - 8) / 2, 0xFFFFFFFF);
        }

        // Draw dropdown arrow
        String arrow = expanded ? "▲" : "▼";
        fontRenderer.drawString(arrow, x + width - 12, y + (height - 8) / 2, 0xFFFFFFFF);

        // Draw expanded options if open
        if (expanded) {
            int optionY = y + height;
            for (int i = 0; i < options.size(); i++) {
                boolean optionHovered = mouseX >= x && mouseX <= x + width &&
                        mouseY >= optionY && mouseY <= optionY + height;

                int optionBgColor = optionHovered ? 0xFF666666 : 0xFF555555;
                if (i == selectedIndex) {
                    optionBgColor = optionHovered ? 0xFF446644 : 0xFF335533;
                }

                Gui.drawRect(x, optionY, x + width, optionY + height, optionBgColor);
                Gui.drawRect(x, optionY, x + width, optionY + 1, 0xFF000000);

                fontRenderer.drawString(options.get(i), x + 5, optionY + (height - 8) / 2, 0xFFFFFFFF);
                optionY += height;
            }
        }
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (!visible || !isMouseOver(mouseX, mouseY)) return false;

        if (mouseButton == 0) {
            if (!expanded) {
                expanded = true;
                return true;
            } else {
                // Check if clicking on an option
                int optionY = y + height;
                for (int i = 0; i < options.size(); i++) {
                    if (mouseX >= x && mouseX <= x + width &&
                            mouseY >= optionY && mouseY <= optionY + height) {
                        selectedIndex = i;
                        if (onChange != null) onChange.run();
                        expanded = false;
                        return true;
                    }
                    optionY += height;
                }
                // Clicked outside options, close dropdown
                expanded = false;
            }
        }
        return false;
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {}

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        if (expanded && keyCode == 1) { // ESC
            expanded = false;
        }
    }

    public int getSelectedIndex() { return selectedIndex; }
    public String getSelectedOption() {
        return (selectedIndex >= 0 && selectedIndex < options.size()) ? options.get(selectedIndex) : "";
    }

    public void setSelectedIndex(int index) {
        this.selectedIndex = Math.max(0, Math.min(index, options.size() - 1));
        if (onChange != null) onChange.run();
    }

    public boolean isExpanded() { return expanded; }

    // Reserve space above for the title text
    @Override
    public int getTopPadding() {
        return 12;
    }
}