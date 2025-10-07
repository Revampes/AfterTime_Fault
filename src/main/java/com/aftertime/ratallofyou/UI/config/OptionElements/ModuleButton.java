package com.aftertime.ratallofyou.UI.config.OptionElements;

import com.aftertime.ratallofyou.UI.config.ConfigData.ModuleInfo;
import com.aftertime.ratallofyou.UI.config.commonConstant.Colors;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;

public class ModuleButton extends GuiElement{
    final int x, y, width, height;
    final ModuleInfo module;
    public final boolean hasSettings; // Make this public so ModSettingsGui can access it
    private static final int HAMBURGER_W = 10;
    private static final int HAMBURGER_H = 6;

    public ModuleButton(int x, int y, int width, int height, ModuleInfo module, boolean hasSettings)
    {
        super( x, y, width, height);
        this.x=x; this.y=y; this.width=width; this.height=height; this.module=module; this.hasSettings = hasSettings;
    }
    @Override
    public void draw(int mouseX, int mouseY, int PosY,FontRenderer fr) {
        int bg = module.Data ? Colors.MODULE_ACTIVE : Colors.MODULE_INACTIVE;
        Gui.drawRect(x, y, x + width, y + height, bg);
        // Name only - center vertically since no description
        int textY = y + (height - 8) / 2; // Center the text vertically in the button
        fr.drawStringWithShadow(module.name, x + 6, textY, Colors.TEXT);

        // Draw triple-bar (hamburger) indicator when settings exist
        if (hasSettings) {
            int pad = 8;
            int iconX = x + width - pad - HAMBURGER_W;
            int iconY = y + (height - HAMBURGER_H) / 2;
            boolean hover = mouseX >= iconX && mouseX <= iconX + HAMBURGER_W && mouseY >= iconY && mouseY <= iconY + HAMBURGER_H;
            int col = hover ? 0xFFFFFFFF : 0xFFCCCCCC;
            // three 1px lines with 2px spacing
            Gui.drawRect(iconX, iconY, iconX + HAMBURGER_W, iconY + 1, col);
            Gui.drawRect(iconX, iconY + 2, iconX + HAMBURGER_W, iconY + 3, col);
            Gui.drawRect(iconX, iconY + 4, iconX + HAMBURGER_W, iconY + 5, col);
        }
    }
    public boolean isMouseOver(int mx, int my) { return mx >= x && mx <= x + width && my >= y && my <= y + height; }
    public boolean isDropdownClicked(int mx, int my) {
        // Right click anywhere on the button opens settings if available
        return hasSettings && isMouseOver(mx, my);
    }
    public ModuleInfo getModule() { return module; }
    // New safe accessors
    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
}
