package com.aftertime.ratallofyou.UI.config.OptionElements;

import com.aftertime.ratallofyou.UI.config.ConfigData.ModuleInfo;
import com.aftertime.ratallofyou.UI.config.commonConstant.Colors;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;

public class ModuleButton extends GuiElement{
    final int x, y, width, height;
    final ModuleInfo module;
    final boolean hasSettings;
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
        // Name top line
        fr.drawStringWithShadow(module.name, x + 6, y + 5, Colors.TEXT);
        // Description below (dim)
        if (module.description != null && !module.description.isEmpty()) {
            fr.drawStringWithShadow(module.description, x + 6, y + 16, Colors.VERSION);
        }
        // settings label area (right side) only if has settings
        if (hasSettings) {
            int padRight = 6;
            int labelW = fr.getStringWidth(SETTINGS_LABEL);
            int labelX = Math.max(x + 6, x + width - labelW - padRight);
            int labelY = y + 7;
            fr.drawStringWithShadow(SETTINGS_LABEL, labelX, labelY, Colors.TEXT);
            // Update clickable bounds for this frame (a bit of padding around text)
            this.settingsStartX = labelX - 2;
            this.settingsEndX = Math.min(x + width, labelX + labelW + 2);
        }
    }
    public boolean isMouseOver(int mx, int my) { return mx >= x && mx <= x + width && my >= y && my <= y + height; }
    public boolean isDropdownClicked(int mx, int my) { return hasSettings && isMouseOver(mx, my) && mx >= settingsStartX && mx <= settingsEndX; }
    public ModuleInfo getModule() { return module; }
    // New safe accessors
    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
}
