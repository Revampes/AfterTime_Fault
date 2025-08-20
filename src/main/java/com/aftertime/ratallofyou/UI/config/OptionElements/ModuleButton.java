package com.aftertime.ratallofyou.UI.config.OptionElements;

import com.aftertime.ratallofyou.UI.config.ConfigData.ModuleInfo;
import com.aftertime.ratallofyou.UI.config.commonConstant.Colors;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;

public class ModuleButton extends GuiElement{
    final int x, y, width, height;
    final ModuleInfo module;
    final boolean hasSettings;
    public ModuleButton(int x, int y, int width, int height, ModuleInfo module, boolean hasSettings)
    {
        super( x, y, width, height);
        this.x=x; this.y=y; this.width=width; this.height=height; this.module=module; this.hasSettings = hasSettings;
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
        // settings dropdown area (right side ellipsis) only if has settings
        if (hasSettings) {
            fr.drawStringWithShadow("...", x + width - 12, y + 7, Colors.TEXT);
        }
    }
    public boolean isMouseOver(int mx, int my) { return mx >= x && mx <= x + width && my >= y && my <= y + height; }
    public boolean isDropdownClicked(int mx, int my) { return hasSettings && isMouseOver(mx, my) && mx >= x + width - 18; }
    public ModuleInfo getModule() { return module; }
}
