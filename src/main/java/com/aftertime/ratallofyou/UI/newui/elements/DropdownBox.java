package com.aftertime.ratallofyou.UI.newui.elements;

import net.minecraft.client.gui.Gui;
import java.util.List;
import java.util.ArrayList;
import com.aftertime.ratallofyou.UI.newui.util.TextRender;

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
        int th = TextRender.height(fontRenderer);
        TextRender.draw(fontRenderer, title, x, y - (th + 4), 0xFFFFFFFF);

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
            TextRender.draw(fontRenderer, selectedText, x + 5, y + (height - th) / 2, 0xFFFFFFFF);
        }

        // Draw dropdown arrow
        String arrow = expanded ? "" : ""; // Fallback simple arrow chars might not render; keep original if needed
        // Use a simple 'v'/'^' if special chars problematic
        arrow = expanded ? "^" : "v";
        int aw = TextRender.width(fontRenderer, arrow);
        TextRender.draw(fontRenderer, arrow, x + width - 5 - aw, y + (height - th) / 2, 0xFFFFFFFF);

        // Note: options are drawn in drawOverlay() so they appear on top of other elements
    }

    @Override
    public void drawOverlay(int mouseX, int mouseY) {
        if (!visible || !expanded) return;

        int th = TextRender.height(fontRenderer);
        // Draw expanded options as overlay so it sits above other elements
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

            TextRender.draw(fontRenderer, options.get(i), x + 5, optionY + (height - th) / 2, 0xFFFFFFFF);
            optionY += height;
        }
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (!visible) return false;

        // Only toggle expansion when clicking the main box
        if (mouseButton == 0 && isMouseOver(mouseX, mouseY)) {
            expanded = !expanded;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseClickedOverlay(int mouseX, int mouseY, int mouseButton) {
        if (!visible || !expanded) return false;

        // Handle clicks on options
        if (mouseButton == 0) {
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
            // Clicked outside options/main: close and consume to prevent misclicks
            if (!(mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height)) {
                expanded = false;
                return true;
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

    @Override
    public boolean hasOverlayOpen() { return expanded; }

    @Override
    public void closeOverlay() { expanded = false; }

    public int getSelectedIndex() { return selectedIndex; }
    public String getSelectedOption() {
        return (selectedIndex >= 0 && selectedIndex < options.size()) ? options.get(selectedIndex) : "";
    }

    public void setSelectedIndex(int index) {
        this.selectedIndex = Math.max(0, Math.min(index, options.size() - 1));
        if (onChange != null) onChange.run();
    }

    public void setOnChange(Runnable onChange) { this.onChange = onChange; }

    public boolean isExpanded() { return expanded; }

    // Reserve space above for the title text
    @Override
    public int getTopPadding() {
        return 12;
    }
}