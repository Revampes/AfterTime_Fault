package com.aftertime.ratallofyou.UI.newui.categories;

import com.aftertime.ratallofyou.UI.newui.elements.*;
import net.minecraft.client.gui.Gui;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import com.aftertime.ratallofyou.UI.newui.util.TextRender;

public class ModulePanel {
    private String moduleName;
    private String description;
    private int x, y, width;
    private boolean expanded = false;
    private List<UIElement> elements = new ArrayList<>();
    private ToggleButton toggleButton;
    private int baseHeight = 20;
    private int verticalGap = 4; // consistent spacing between elements

    public ModulePanel(String moduleName, String description, int x, int y, int width, boolean initialToggleState) {
        this.moduleName = moduleName;
        this.description = description;
        this.x = x;
        this.y = y;
        this.width = width;

        // Create main toggle button - using aqua blue for enabled, grey for disabled
        toggleButton = new ToggleButton(x, y, width, baseHeight, moduleName, description, initialToggleState, () -> {
            System.out.println(moduleName + " toggled: " + toggleButton.isToggled());
        });
    }

    public void draw(int mouseX, int mouseY) {
        // Draw module background
        int bgColor = expanded ? 0x80444444 : 0x80333333;
        Gui.drawRect(x, y, x + width, y + getHeight(), bgColor);

        // Draw toggle button
        toggleButton.draw(mouseX, mouseY);

        // Draw description on hover
        if (toggleButton.isMouseOver(mouseX, mouseY)) {
            drawTooltip(mouseX, mouseY, description);
        }

        // Draw subsettings if expanded
        if (expanded) {
            for (int i = 0; i < elements.size(); i++) {
                UIElement element = elements.get(i);
                element.draw(mouseX, mouseY);
            }
        }
    }

    // Draw overlays after all normal elements to ensure they are on top
    public void drawOverlays(int mouseX, int mouseY) {
        if (!expanded) return;
        for (UIElement element : elements) {
            element.drawOverlay(mouseX, mouseY);
        }
    }

    private void drawTooltip(int mouseX, int mouseY, String text) {
        int th = TextRender.height(toggleButton.fontRenderer);
        int tooltipWidth = TextRender.width(toggleButton.fontRenderer, text) + 8;
        int tooltipX = Math.min(mouseX + 5, toggleButton.mc.currentScreen.width - tooltipWidth - 5);
        int tooltipY = mouseY + 5;
        int tooltipH = th + 8;

        Gui.drawRect(tooltipX, tooltipY, tooltipX + tooltipWidth, tooltipY + tooltipH, 0xE0000000);
        TextRender.draw(toggleButton.fontRenderer, text, tooltipX + 4, tooltipY + (tooltipH - th) / 2, 0xFFFFFF);
    }

    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        // Check toggle button (left click)
        if (toggleButton.mouseClicked(mouseX, mouseY, mouseButton)) {
            return true;
        }

        // Right click on toggle button to expand/collapse
        if (toggleButton.isMouseOver(mouseX, mouseY) && mouseButton == 1) {
            expanded = !expanded;
            return true;
        }

        // Check subsetting elements if expanded
        if (expanded) {
            for (UIElement element : elements) {
                if (element.mouseClicked(mouseX, mouseY, mouseButton)) {
                    return true;
                }
            }
        }
        return false;
    }

    // Handle clicks targeting overlays (e.g., dropdown lists, color popups)
    public boolean mouseClickedOverlay(int mouseX, int mouseY, int mouseButton) {
        if (!expanded) return false;
        // Prefer elements with overlays open first
        for (UIElement element : elements) {
            if (element.hasOverlayOpen()) {
                if (element.mouseClickedOverlay(mouseX, mouseY, mouseButton)) return true;
            }
        }
        // Then allow elements to open overlays if click falls into their overlay activation areas
        for (UIElement element : elements) {
            if (element.mouseClickedOverlay(mouseX, mouseY, mouseButton)) return true;
        }
        return false;
    }

    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        toggleButton.mouseReleased(mouseX, mouseY, mouseButton);

        if (expanded) {
            for (UIElement element : elements) {
                element.mouseReleased(mouseX, mouseY, mouseButton);
            }
        }
    }

    public void keyTyped(char typedChar, int keyCode) {
        if (expanded) {
            for (UIElement element : elements) {
                element.keyTyped(typedChar, keyCode);
            }
        }
    }

    public int getHeight() {
        if (!expanded) return baseHeight;
        int sum = baseHeight + 3; // padding below toggle
        for (int i = 0; i < elements.size(); i++) {
            UIElement el = elements.get(i);
            sum += el.getOuterHeight();
            if (i < elements.size() - 1) sum += verticalGap;
        }
        // Add small bottom padding
        return sum + 3;
    }

    // Methods to add different types of subsetting elements with proper spacing
    public void addCheckBox(String title, boolean initialValue, Runnable onChange) {
        CheckBox checkBox = new CheckBox(0, 0, width - 10, 16, title, initialValue, onChange);
        elements.add(checkBox);
        relayoutElements();
    }

    public void addSlider(String title, float min, float max, float initialValue, Runnable onChange) {
        Slider slider = new Slider(0, 0, width - 10, 16, title, min, max, initialValue);
        slider.setOnChange(v -> onChange.run());
        elements.add(slider);
        relayoutElements();
    }

    // New: Consumer-based overload to receive the slider's value
    public void addSlider(String title, float min, float max, float initialValue, Consumer<Float> onChange) {
        Slider slider = new Slider(0, 0, width - 10, 16, title, min, max, initialValue);
        slider.setOnChange(onChange);
        elements.add(slider);
        relayoutElements();
    }

    public void addColorPicker(String title, java.awt.Color initialColor, Runnable onChange) {
        addColorPickerReturn(title, initialColor, onChange);
    }

    public ColorPicker addColorPickerReturn(String title, java.awt.Color initialColor, Runnable onChange) {
        ColorPicker colorPicker = new ColorPicker(0, 0, width - 10, 16, title, initialColor, onChange);
        elements.add(colorPicker);
        relayoutElements();
        return colorPicker;
    }

    public void addTextInput(String placeholder, int maxLength, Runnable onChange) {
        addTextInputReturn(placeholder, maxLength, onChange);
    }

    public TextInputField addTextInputReturn(String placeholder, int maxLength, Runnable onChange) {
        TextInputField textInput = new TextInputField(0, 0, width - 10, 16, placeholder, maxLength, onChange);
        elements.add(textInput);
        relayoutElements();
        return textInput;
    }

    public void addKeyBindInput(String title, String initialKey, Runnable onChange) {
        KeyBindInput keyBind = new KeyBindInput(0, 0, width - 10, 16, title, initialKey, onChange);
        elements.add(keyBind);
        relayoutElements();
    }

    // New: return the KeyBindInput instance so callers can read the selected key in onChange
    public KeyBindInput addKeyBindInputReturn(String title, String initialKey, Runnable onChange) {
        KeyBindInput keyBind = new KeyBindInput(0, 0, width - 10, 16, title, initialKey, onChange);
        elements.add(keyBind);
        relayoutElements();
        return keyBind;
    }

    public void addKeyBindInput(String title, int initialKeyCode, Runnable onChange) {
        KeyBindInput keyBind = new KeyBindInput(0, 0, width - 10, 16, title, initialKeyCode, onChange);
        elements.add(keyBind);
        relayoutElements();
    }

    public void addNormalButton(String label, Runnable onClick) {
        NormalButton button = new NormalButton(0, 0, width - 10, 16, label, onClick);
        elements.add(button);
        relayoutElements();
    }

    public void addDropdown(String title, String[] options, int initialIndex, Runnable onChange) {
        addDropdownReturn(title, options, initialIndex, onChange);
    }

    public DropdownBox addDropdownReturn(String title, String[] options, int initialIndex, Runnable onChange) {
        DropdownBox dropdown = new DropdownBox(0, 0, width - 10, 16, title, options, initialIndex, onChange);
        elements.add(dropdown);
        relayoutElements();
        return dropdown;
    }

    private void relayoutElements() {
        int currentY = y + baseHeight + 3; // start below toggle
        for (UIElement element : elements) {
            // Reserve the element's top padding so its title (drawn above) doesn't overlap previous element
            currentY += element.getTopPadding();
            element.setPosition(x + 5, currentY);
            element.setSize(width - 10, element.getHeight());
            // Advance by the element's core height and a small gap
            currentY += element.getHeight() + verticalGap;
        }
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
        toggleButton.setPosition(x, y);
        relayoutElements();
    }

    // New: allow caller to update x,y and width in one go, and propagate sizes
    public void setBounds(int x, int y, int width) {
        this.x = x;
        this.y = y;
        this.width = width;
        toggleButton.setPosition(x, y);
        toggleButton.setSize(width, baseHeight);
        relayoutElements();
    }

    public void closeOverlays() {
        for (UIElement element : elements) {
            if (element.hasOverlayOpen()) element.closeOverlay();
        }
    }

    public boolean hasAnyOverlayOpen() {
        for (UIElement element : elements) {
            if (element.hasOverlayOpen()) return true;
        }
        return false;
    }

    // Getters
    public boolean isExpanded() { return expanded; }
    public String getModuleName() { return moduleName; }
    public ToggleButton getToggleButton() { return toggleButton; }
    public boolean isEnabled() { return toggleButton.isToggled(); }
}
