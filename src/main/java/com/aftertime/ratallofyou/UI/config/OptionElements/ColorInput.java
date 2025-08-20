package com.aftertime.ratallofyou.UI.config.OptionElements;

import com.aftertime.ratallofyou.UI.config.ConfigData.AllConfig;
import com.aftertime.ratallofyou.UI.config.ConfigIO;
import com.aftertime.ratallofyou.UI.config.ModSettingsGui;
import com.aftertime.ratallofyou.UI.config.PropertyRef;
import com.aftertime.ratallofyou.UI.config.commonConstant.Colors;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;

import java.awt.*;

public class ColorInput extends Option<Color> {
    private final TextInput textInput;

    public ColorInput(PropertyRef ref, String title,Color initial, int x, int y, int width, int height) {
        super(ref,title, "", initial, x, y, width, height);
        String s = initial.getRed() + "," + initial.getGreen() + "," + initial.getBlue() + "," + initial.getAlpha();
        // Use a dummy ref so TextInput changes don't directly write into the Color config
        this.textInput = new TextInput(new PropertyRef(99, "color_input"),"ColorInput", "Text Input For Color",x+40,y,width-40,16,s, true);
    }

    public void beginEditing(int mouseX) {
        textInput.beginEditing(mouseX, x + 40);
    }

    public void unfocus() {
        textInput.isEditing = false;
    }

    @Override
    public void draw(int mouseX, int mouseY, int yPos, FontRenderer fr) {
        fr.drawStringWithShadow(name, x, yPos + 5, Colors.COMMAND_TEXT);
        Gui.drawRect(x + fr.getStringWidth(name) + 10, yPos + 3, x + fr.getStringWidth(name) + 30, yPos + height - 3, Data.getRGB());
        int inputY = yPos + height + 8;
        fr.drawStringWithShadow("RGBA:", x, inputY, Colors.COMMAND_TEXT);
        textInput.draw(mouseX,mouseY, inputY - 2, fr);
    }



    public void handleKeyTyped(char typedChar, int keyCode) {
        textInput.handleKeyTyped(typedChar, keyCode);
        // Parse and update the actual Color config from the text
        updateColor(textInput.Data);
    }

    private void updateColor(String value) {
        String[] parts = value.split(",");
        if (parts.length != 4) return;
        try {
            int r = Math.min(255, Math.max(0, Integer.parseInt(parts[0])));
            int g = Math.min(255, Math.max(0, Integer.parseInt(parts[1])));
            int b = Math.min(255, Math.max(0, Integer.parseInt(parts[2])));
            int a = Math.min(255, Math.max(0, Integer.parseInt(parts[3])));
            Data = new Color(r, g, b, a);
            OnValueChange();
        } catch (Exception ignored) {
        }
    }
}
