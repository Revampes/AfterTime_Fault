package com.aftertime.ratallofyou.UI.config.OptionElements;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;

public abstract class GuiElement {
        public final int x, y, width, height;
        public GuiElement(int x, int y, int width, int height) {
            this.x = x; this.y = y; this.width = width; this.height = height;
        }
        protected abstract void draw(int mouseX, int mouseY, int yPos, FontRenderer fr);
        public boolean isMouseOver(int mouseX, int mouseY, int yPos) {
            return mouseX >= x && mouseX <= x + width && mouseY >= yPos && mouseY <= yPos + height;
        }



}
