package com.aftertime.ratallofyou.UI.config.OptionElements;

import com.aftertime.ratallofyou.UI.config.ConfigData.ModuleInfo;
import com.aftertime.ratallofyou.UI.config.commonConstant.Colors;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;

public class ModuleButton extends GuiElement{
    final int x, y, width, height;
    final ModuleInfo module;
    public final boolean hasSettings; // Make this public so ModSettingsGui can access it
    private static final String SETTINGS_LABEL = "[Settings]";
    // Cached bounds for the settings label to make click area accurate after draw
    private int settingsStartX;
    private int settingsEndX;

    public ModuleButton(int x, int y, int width, int height, ModuleInfo module, boolean hasSettings)
    {
        super( x, y, width, height);
        this.x=x; this.y=y; this.width=width; this.height=height; this.module=module; this.hasSettings = hasSettings;
        // Default conservative area at the far right, will be updated during draw()
        this.settingsStartX = x + width - 24;
        this.settingsEndX = x + width;
    }
    @Override
    public void draw(int mouseX, int mouseY, int PosY,FontRenderer fr) {
        int bg = module.Data ? Colors.MODULE_ACTIVE : Colors.MODULE_INACTIVE;
        Gui.drawRect(x, y, x + width, y + height, bg);
        // Name only - center vertically since no description
        int textY = y + (height - 8) / 2; // Center the text vertically in the button
        fr.drawStringWithShadow(module.name, x + 6, textY, Colors.TEXT);

        // No longer showing description or [Settings] label by default
        // These will be shown only in hover popup
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
