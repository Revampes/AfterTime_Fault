package com.aftertime.ratallofyou.UI.config.OptionElements;

import com.aftertime.ratallofyou.UI.config.PropertyRef;
import com.aftertime.ratallofyou.UI.config.commonConstant.Colors;
import net.minecraft.client.gui.FontRenderer;

public class LabelledInput extends TextInput {
    private final boolean verticalBelow; // if true, draw input below label and use extra vertical space

    public LabelledInput(PropertyRef ref, String label, String initial, int x, int y, int width, int height) {
        super(ref, label, "", x, y, width, height, initial, true);
        this.verticalBelow = false;
    }

    public LabelledInput(PropertyRef ref, String label, String initial, int x, int y, int width, int height, boolean verticalBelow) {
        super(ref, label, "", x, y, width, height, initial, true);
        this.verticalBelow = verticalBelow;
    }

    @Override
    public void draw(int mouseX, int mouseY, int yPos, FontRenderer fr) {
        fr.drawStringWithShadow(name, x, yPos + 5, Colors.COMMAND_TEXT);
        int inputY = verticalBelow ? (yPos + 12) : yPos;
        super.draw(mouseX, mouseY, inputY, fr);
    }

    @Override
    public boolean isMouseOver(int mouseX, int mouseY, int yPos) {
        int inputY = verticalBelow ? (yPos + 12) : yPos;
        return super.isMouseOver(mouseX, mouseY, inputY);
    }

    public int getVerticalSpace() {
        return verticalBelow ? 34 : 22; // label + input vs single-line
    }
}
