package com.aftertime.ratallofyou.UI.config.OptionElements;

import com.aftertime.ratallofyou.UI.config.ConfigData.FastHotkeyEntry;
import com.aftertime.ratallofyou.UI.config.commonConstant.Dimensions;
import net.minecraft.client.Minecraft;

public class FastRow {
    public final TextInput labelInput;
    public final TextInput commandInput;
    public FastHotkeyEntry entry;

    // Convenience: left panel defaults (not used for drawing now)
    public FastRow(int guiLeft, FastHotkeyEntry entry) {
        this(guiLeft + Dimensions.COMMAND_PANEL_X + 5, Math.max(Dimensions.FH_INPUT_MIN_WIDTH, Dimensions.COMMAND_PANEL_WIDTH - 10), entry);
    }

    // Explicit constructor for placing in the detail panel
    public FastRow(int baseX, int inputWidth, FastHotkeyEntry entry) {
        this.entry = entry;
        this.labelInput = new LabelledInput(entry.Labelref, "Key Name", entry.label, baseX, 0, inputWidth, Dimensions.FH_INPUT_HEIGHT);
        this.commandInput = new LabelledInput(entry.Commandref, "Command Run", entry.command, baseX, 0, inputWidth, Dimensions.FH_INPUT_HEIGHT);
    }

    public void DrawElements(int MouseX,int MouseY,int labely,int commandy)
    {
        labelInput.draw(MouseX,MouseY, labely, Minecraft.getMinecraft().fontRendererObj);
        commandInput.draw(MouseX,MouseY, commandy, Minecraft.getMinecraft().fontRendererObj);

    }
}
