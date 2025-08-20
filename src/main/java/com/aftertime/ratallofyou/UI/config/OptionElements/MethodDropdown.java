package com.aftertime.ratallofyou.UI.config.OptionElements;

import com.aftertime.ratallofyou.UI.config.ConfigData.AllConfig;
import com.aftertime.ratallofyou.UI.config.ConfigData.DataType_DropDown;
import com.aftertime.ratallofyou.UI.config.ConfigIO;
import com.aftertime.ratallofyou.UI.config.PropertyRef;
import com.aftertime.ratallofyou.UI.config.commonConstant.Colors;
import com.aftertime.ratallofyou.modules.render.EtherwarpOverlay;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;

public class MethodDropdown extends Option<String[]> {
    public final String[] methods;//
    int selected;
    public boolean isOpen = false;
    public MethodDropdown(PropertyRef ref, String title, int Initial,int x, int y, int width, int height, String[] methods)
    {
        super(ref,title, "",methods, x, y, width, height);
        this.methods = methods;
        this.selected = Initial;
    }
    public void selectMethod(int idx) {
        selected = Math.max(0, Math.min(methods.length - 1, idx));
        // Persist into the underlying dropdown config value
        Object cfg = AllConfig.INSTANCE.ALLCONFIGS.get(ref.ConfigType).get(ref.Key).Data;
        if (cfg instanceof DataType_DropDown) {
            ((DataType_DropDown) cfg).selectedIndex = selected;
        }
        // Save to properties immediately so it survives reopen
        String compositeKey = ref.ConfigType + "," + ref.Key;
        ConfigIO.INSTANCE.SetDropDownSelect(compositeKey, selected);
    }
    @Override
    public void draw(int mouseX, int mouseY, int yPos, FontRenderer fr) {
        fr.drawStringWithShadow(name + ":", x, yPos + 4,Colors.COMMAND_TEXT);
        int bx = x + 100; int by = yPos; int bw = width - 100; int bh = height;
        Gui.drawRect(bx, by, bx + bw, by + bh, Colors.CATEGORY_BUTTON);
        String label = methods[Math.max(0, Math.min(methods.length - 1, selected))];
        fr.drawStringWithShadow(label, bx + 4, by + 4, Colors.COMMAND_TEXT);
        if (isOpen) {
            for (int i = 0; i < methods.length; i++) {
                int oy = by + bh + i * bh;
                Gui.drawRect(bx, oy, bx + bw, oy + bh, 0xFF1E1E1E);
                fr.drawStringWithShadow(methods[i], bx + 4, oy + 4, Colors.COMMAND_TEXT);
            }
        }
    }


}
