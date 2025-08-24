package com.aftertime.ratallofyou.UI.config.OptionElements;

import com.aftertime.ratallofyou.UI.config.PropertyRef;
import net.minecraft.client.gui.Gui;

public class Toggle extends Option<Boolean> {

    public Toggle(PropertyRef ref, String name, String description, boolean initial, int x, int y, int width, int height) {
        super(ref,name, description, initial, x, y, width, height);
    }

    @Override
    public void draw(int mouseX, int mouseY, int yPos, net.minecraft.client.gui.FontRenderer fr) {
        // Draw the toggle button
        int box = 10;
        int cx = x;
        int cy = yPos + 3;
        Gui.drawRect(cx, cy, cx + box, cy + box, Data ? 0xFF00FF00 : 0xFFFF0000); // Green for true, Red for false
        fr.drawStringWithShadow(name, x + 14, yPos + 3, 0xFFFFFFFF); // White text
    }



    public void toggle() {
        Data = !Data; // Toggle the boolean value
        OnValueChange();
    }
}
