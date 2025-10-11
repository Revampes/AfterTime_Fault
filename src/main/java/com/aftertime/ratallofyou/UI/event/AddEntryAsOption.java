package com.aftertime.ratallofyou.UI.event;

import com.aftertime.ratallofyou.UI.config.ConfigData.BaseConfig;
import com.aftertime.ratallofyou.UI.config.ConfigData.DataType_DropDown;
import com.aftertime.ratallofyou.UI.config.ModSettingsGui;
import com.aftertime.ratallofyou.UI.config.OptionElements.*;
import com.aftertime.ratallofyou.UI.config.PropertyRef;
import com.aftertime.ratallofyou.UI.config.commonConstant.Dimensions;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.lang.reflect.Type;
import java.util.Map;

public class AddEntryAsOption {
    private final ModSettingsGui gui;

    public AddEntryAsOption(ModSettingsGui gui) {
        this.gui = gui;
    }

    public void AddEntryAsOption(Map.Entry<String, BaseConfig<?>> entry, Integer y, int ConfigType) {
        PropertyRef ref = new PropertyRef(ConfigType, entry.getKey()); Type type = entry.getValue().type; Object data = entry.getValue().Data;
        int xPos, width; if (gui.optionsInline && !gui.useSidePanelForSelected) { int listX = gui.guiLeft + 120; int listW = Dimensions.GUI_WIDTH - 120 - Dimensions.SCROLLBAR_WIDTH; int boxX = listX + 4; int boxW = listW - 8; int padding = 6; xPos = boxX + padding; width = (listW - 8) - padding * 2; } else { xPos = gui.guiLeft + Dimensions.COMMAND_PANEL_X + 5; width = Dimensions.COMMAND_PANEL_WIDTH - 10; }
        // Titles above inputs for Terminal, FastHotkey, and Auto Fish
        boolean isVerticalAbove = true;
        if (type.equals(String.class)) gui.labelledInputs.add(new LabelledInput(ref, entry.getValue().name, String.valueOf(data), xPos, y, width, 16, isVerticalAbove));
        else if (type.equals(Boolean.class)) {
            // Try to create a special checkbox first
            SpecialCheckbox specialCheckbox = SpecialCheckboxFactory.createSpecialCheckbox(ref, entry.getValue().name, entry.getValue().description, (Boolean) data, xPos, y, width, 16);
            if (specialCheckbox != null) {
                gui.Toggles.add(specialCheckbox);
            } else {
                // Use normal toggle
                gui.Toggles.add(new Toggle(ref, entry.getValue().name, entry.getValue().description, (Boolean) data, xPos, y, width, 16));
            }
        }
        else if (type.equals(Integer.class)) {
            String display = String.valueOf(data);
            // Special-case: show key name for Auto Fish hotkey input
            if (ConfigType == 10 && "autofish_hotkey".equals(entry.getKey())) {
                int code = 0; try { code = (data instanceof Integer) ? (Integer) data : Integer.parseInt(String.valueOf(data)); } catch (Exception ignored) {}
                String name = (code <= 0) ? "Unbound" : Keyboard.getKeyName(code);
                if (name == null || name.trim().isEmpty() || "NONE".equalsIgnoreCase(name)) name = "Unbound";
                display = name;
            }
            gui.labelledInputs.add(new LabelledInput(ref, entry.getValue().name, display, xPos, y, width, 16, isVerticalAbove));
        }
        else if (type.equals(Float.class)) gui.labelledInputs.add(new LabelledInput(ref, entry.getValue().name, String.valueOf(data), xPos, y, width, 16, isVerticalAbove));
        else if (type.equals(DataType_DropDown.class)) { DataType_DropDown dd = (DataType_DropDown) data; gui.methodDropdowns.add(new MethodDropdown(ref, entry.getValue().name, dd.selectedIndex, xPos, y, width, 16, dd.options)); }
        else if (type.equals(Color.class)) gui.ColorInputs.add(new ColorInput(ref, entry.getValue().name, (Color) data, xPos, y, width, 18));
        else System.err.println("Unsupported config type: " + type);
    }
}
