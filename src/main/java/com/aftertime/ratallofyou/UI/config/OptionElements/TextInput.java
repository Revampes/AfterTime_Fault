package com.aftertime.ratallofyou.UI.config.OptionElements;

import com.aftertime.ratallofyou.UI.config.ConfigIO;
import com.aftertime.ratallofyou.UI.config.PropertyRef;
import com.aftertime.ratallofyou.UI.config.commonConstant.Colors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import org.lwjgl.input.Keyboard;

public class TextInput extends Option<String> {
    final boolean allowSpaces;
    public boolean isEditing = false;
    int cursorPosition = 0;
    long cursorBlinkTimer = 0;
    boolean cursorVisible = false;
    int maxLen = 64;

    public TextInput(PropertyRef ref, String name, String description, int x, int y, int width, int height, String initial, boolean allowSpaces) {
        super(ref, name, description, initial, x, y, width, height);
        this.cursorPosition = initial.length();
        this.allowSpaces = allowSpaces;
    }

    // New: allow external code to update the visible text without triggering parsing or persistence
    public void setDisplayText(String text) {
        this.Data = text == null ? "" : text;
        // do not call OnValueChange() here; this is for UI display only
    }

    @Override
    public void draw(int mouseX, int mouseY, int yPos, FontRenderer fr) {
        Gui.drawRect(x, yPos, x + width, yPos + height, Colors.INPUT_BG);
        fr.drawStringWithShadow(Data, x + 3, yPos + 4, Colors.INPUT_FG);

        if (isEditing) {
            cursorBlinkTimer += 10;
            if (cursorBlinkTimer >= 1000) {
                cursorBlinkTimer = 0;
                cursorVisible = !cursorVisible;
            }
            if (cursorVisible) {
                int cx = x + 3 + fr.getStringWidth(Data.substring(0, Math.min(cursorPosition, Data.length())));
                Gui.drawRect(cx, yPos + 3, cx + 1, yPos + height - 3, Colors.INPUT_FG);
            }
        }
    }




    public void beginEditing(int mouseX) {
        beginEditing(mouseX, this.x);
    }

    public void beginEditing(int mouseX, int leftX) {
        isEditing = true;
        cursorBlinkTimer = 0;
        cursorVisible = true;
        // place cursor based on click x relative to the input's left edge
        int rel = Math.max(0, mouseX - leftX);
        int pos = 0;
        while (pos < Data.length()) {
            int cw = Minecraft.getMinecraft().fontRendererObj.getCharWidth(Data.charAt(pos));
            if (rel < cw / 2) break;
            rel -= cw;
            pos++;
        }
        cursorPosition = pos;
    }

    public void handleKeyTyped(char typedChar, int keyCode) {
        if (!isEditing) return;
        if (keyCode == Keyboard.KEY_RETURN) {
            isEditing = false;
            return;
        }
        if (keyCode == Keyboard.KEY_BACK) {
            if (cursorPosition > 0 && Data.length() > 0) {
                Data = Data.substring(0, cursorPosition - 1) + Data.substring(cursorPosition);
                cursorPosition--;
                OnValueChange();
            }
        } else if (keyCode == Keyboard.KEY_LEFT) {
            cursorPosition = Math.max(0, cursorPosition - 1);
        } else if (keyCode == Keyboard.KEY_RIGHT) {
            cursorPosition = Math.min(Data.length(), cursorPosition + 1);
        } else {
            // Accept printable characters
            if (typedChar >= 32 && typedChar != 127) {
                if (!allowSpaces && typedChar == ' ') return;
                if (Data.length() >= maxLen) return;
                Data = Data.substring(0, cursorPosition) + typedChar + Data.substring(cursorPosition);
                cursorPosition++;
                OnValueChange();
            }
        }
        cursorBlinkTimer = 0;
        cursorVisible = true;
    }


}
