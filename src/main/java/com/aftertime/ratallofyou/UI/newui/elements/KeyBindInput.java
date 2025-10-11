package com.aftertime.ratallofyou.UI.newui.elements;

import net.minecraft.client.gui.Gui;
import org.lwjgl.input.Keyboard;

public class KeyBindInput extends UIElement {
    private int keyCode;
    private String keyName;
    private String title;
    private boolean listening = false;
    private Runnable onChange;

    public KeyBindInput(int x, int y, int width, int height, String title, String initialKey, Runnable onChange) {
        super(x, y, width, height);
        this.title = title;
        this.onChange = onChange;
        setKeyByName(initialKey);
    }

    public KeyBindInput(int x, int y, int width, int height, String title, int initialKeyCode, Runnable onChange) {
        super(x, y, width, height);
        this.title = title;
        this.onChange = onChange;
        setKeyCode(initialKeyCode);
    }

    @Override
    public void draw(int mouseX, int mouseY) {
        if (!visible) return;

        hovered = isMouseOver(mouseX, mouseY);

        // Draw title ABOVE the keybind box
        fontRenderer.drawString(title, x, y - 10, 0xFFFFFFFF);

        // Draw button background
        int bgColor;
        if (listening) {
            bgColor = 0xFF00FF00;
        } else if (hovered) {
            bgColor = 0xFF555555;
        } else {
            bgColor = 0xFF444444;
        }

        Gui.drawRect(x, y, x + width, y + height, bgColor);

        // Draw border
        int borderColor = listening ? 0xFFFFFF00 : 0xFF000000;
        Gui.drawRect(x, y, x + width, y + 1, borderColor);
        Gui.drawRect(x, y + height - 1, x + width, y + height, borderColor);
        Gui.drawRect(x, y, x + 1, y + height, borderColor);
        Gui.drawRect(x + width - 1, y, x + width, y + height, borderColor);

        // Draw key name
        String displayText = listening ? "Press a key..." : keyName;
        int textColor = listening ? 0xFF000000 : 0xFFFFFFFF;
        int textX = x + (width - fontRenderer.getStringWidth(displayText)) / 2;
        int textY = y + (height - 8) / 2;
        fontRenderer.drawString(displayText, textX, textY, textColor);
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (!visible || !isMouseOver(mouseX, mouseY)) return false;

        listening = !listening;
        return true;
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {}

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        if (!listening || !visible) return;

        if (keyCode == Keyboard.KEY_ESCAPE) {
            // Clear the keybind
            setKeyCode(0);
        } else if (keyCode != Keyboard.KEY_RETURN) {
            setKeyCode(keyCode);
        }

        listening = false;
    }

    private void setKeyByName(String keyName) {
        this.keyName = keyName;
        this.keyCode = Keyboard.getKeyIndex(keyName);
    }

    private void setKeyCode(int keyCode) {
        this.keyCode = keyCode;
        this.keyName = keyCode == 0 ? "None" : Keyboard.getKeyName(keyCode);
        if (onChange != null) onChange.run();
    }

    public int getKeyCode() { return keyCode; }
    public String getKeyName() { return keyName; }

    public boolean isListening() { return listening; }
    public void setListening(boolean listening) { this.listening = listening; }

    // New: allow attaching or changing the onChange callback after creation
    public void setOnChange(Runnable onChange) {
        this.onChange = onChange;
    }

    // Reserve space above for the title text
    @Override
    public int getTopPadding() {
        return 12;
    }
}