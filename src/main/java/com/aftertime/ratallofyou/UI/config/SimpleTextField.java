package com.aftertime.ratallofyou.UI.config;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.FontRenderer;
import org.lwjgl.input.Keyboard;

public class SimpleTextField {
    public String text;
    public boolean isEditing = false;
    private int x, y, width, height;
    private int cursorPos = 0;
    private long lastCursorBlink = 0;

    public SimpleTextField(String text, int x, int y, int width, int height) {
        this.text = text != null ? text : "";
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.cursorPos = this.text.length();
    }

    public void setBounds(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void draw(int mouseX, int mouseY) {
        // Draw background
        int bgColor = isEditing ? 0xFF444444 : 0xFF222222;
        Gui.drawRect(x, y, x + width, y + height, bgColor);

        // Draw border
        int borderColor = isEditing ? 0xFF666666 : 0xFF444444;
        Gui.drawRect(x - 1, y - 1, x + width + 1, y + height + 1, borderColor);

        // Note: Text drawing needs to be handled by the caller with FontRenderer
        // This method just draws the background and border
    }

    public void draw(int mouseX, int mouseY, FontRenderer fontRenderer) {
        draw(mouseX, mouseY);

        // Draw text
        if (!text.isEmpty()) {
            fontRenderer.drawString(text, x + 2, y + 2, 0xFFFFFFFF);
        }

        // Draw cursor if editing
        if (isEditing && (System.currentTimeMillis() - lastCursorBlink) % 1000 < 500) {
            int cursorX = x + 2 + fontRenderer.getStringWidth(text.substring(0, Math.min(cursorPos, text.length())));
            Gui.drawRect(cursorX, y + 2, cursorX + 1, y + height - 2, 0xFFFFFFFF);
        }
    }

    public boolean isMouseOver(int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    public void beginEditing(int mouseX) {
        isEditing = true;
        lastCursorBlink = System.currentTimeMillis();
        // Position cursor at click location (simplified)
        cursorPos = text.length();
    }

    public void beginEditing(int mouseX, int baseX) {
        beginEditing(mouseX);
    }

    public void handleKeyTyped(char typedChar, int keyCode) {
        if (!isEditing) return;

        if (keyCode == Keyboard.KEY_ESCAPE || keyCode == Keyboard.KEY_RETURN) {
            isEditing = false;
            return;
        }

        if (keyCode == Keyboard.KEY_BACK && cursorPos > 0) {
            text = text.substring(0, cursorPos - 1) + text.substring(cursorPos);
            cursorPos--;
        } else if (keyCode == Keyboard.KEY_DELETE && cursorPos < text.length()) {
            text = text.substring(0, cursorPos) + text.substring(cursorPos + 1);
        } else if (keyCode == Keyboard.KEY_LEFT && cursorPos > 0) {
            cursorPos--;
        } else if (keyCode == Keyboard.KEY_RIGHT && cursorPos < text.length()) {
            cursorPos++;
        } else if (keyCode == Keyboard.KEY_HOME) {
            cursorPos = 0;
        } else if (keyCode == Keyboard.KEY_END) {
            cursorPos = text.length();
        } else if (isPrintableChar(typedChar)) {
            text = text.substring(0, cursorPos) + typedChar + text.substring(cursorPos);
            cursorPos++;
        }

        lastCursorBlink = System.currentTimeMillis();
    }

    private boolean isPrintableChar(char c) {
        return c >= 32 && c < 127; // Basic ASCII printable characters
    }

    public void unfocus() {
        isEditing = false;
    }
}
