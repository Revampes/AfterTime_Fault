package com.aftertime.ratallofyou.UI.config;

import com.aftertime.ratallofyou.UI.UIHighlighter;
import com.aftertime.ratallofyou.UI.config.ConfigData.*;
import com.aftertime.ratallofyou.UI.config.OptionElements.*;
import com.aftertime.ratallofyou.UI.config.commonConstant.Colors;
import com.aftertime.ratallofyou.UI.config.commonConstant.Dimensions;
import com.aftertime.ratallofyou.modules.dungeon.terminals.TerminalSettingsApplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.lwjgl.opengl.GL11.*;

public class ModSettingsGui extends GuiScreen {
    // =============================================
    // Constants
    // =============================================


    // =============================================
    // Fields
    // =============================================
    private final List<GuiButton> categoryButtons = new ArrayList<>();
    private final List<ModuleButton> moduleButtons = new ArrayList<>();
    private final List<ColorInput> ColorInputs = new ArrayList<>();
    private final List<LabelledInput> labelledInputs = new ArrayList<>();
    private final List<MethodDropdown> methodDropdowns = new ArrayList<>();
    private final List<Toggle> Toggles = new ArrayList<>();

    private final ScrollManager mainScroll = new ScrollManager();
    private final ScrollManager commandScroll = new ScrollManager();
    // Fast Hotkey editor rows
    private final List<FastRow> fastRows = new ArrayList<>();
    private String selectedCategory = "Kuudra";
    private ModuleInfo SelectedModule = null;
    private boolean showCommandSettings = false;
    private int guiLeft, guiTop;

    // =============================================
    // Core GUI Methods
    // =============================================
    @Override
    public void initGui() {
        this.guiLeft = (this.width - Dimensions.GUI_WIDTH) / 2;
        this.guiTop = (this.height - Dimensions.GUI_HEIGHT) / 2;
        this.buttonList.clear();
        this.categoryButtons.clear();
        this.moduleButtons.clear();
        this.ColorInputs.clear();
        this.labelledInputs.clear();
        this.methodDropdowns.clear();
        this.mainScroll.reset();
        this.commandScroll.reset();
        this.showCommandSettings = false;
        this.SelectedModule = null;
        this.fastRows.clear();

        buildCategoryButtons();
        buildModuleButtons();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawBackground();
        drawCategories();
        drawModules(mouseX, mouseY);
        drawScrollbars();
        drawCommandPanel(mouseX, mouseY);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (categoryButtons.contains(button)) {
            selectedCategory = button.displayString;
            mainScroll.reset();
            showCommandSettings = false;
            SelectedModule = null;
            buildModuleButtons();
        }
    }
    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        ConfigIO.INSTANCE.SaveProperties();
        // Apply terminal settings after saving so runtime reflects panel changes
        TerminalSettingsApplier.applyFromAllConfig();
    }
    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        handleInputFieldEditingState();
        handleScrollbarClicks(mouseX, mouseY);

        // Remove broken special-case; rely on general handlers below

        // If Fast Hotkey panel is open, only consume clicks inside its panel area
        if (showCommandSettings && SelectedModule != null && SelectedModule.name.equals("Fast Hotkey")) {
            int panelX = guiLeft + Dimensions.COMMAND_PANEL_X;
            int panelY = guiTop + Dimensions.COMMAND_PANEL_Y;
            int panelWidth = Dimensions.COMMAND_PANEL_WIDTH;
            int panelHeight = Dimensions.GUI_HEIGHT - 60;
            boolean insidePanel = mouseX >= panelX && mouseX <= panelX + panelWidth && mouseY >= panelY && mouseY <= panelY + panelHeight;
            if (insidePanel) {
                handleFastHotKeyClicks(mouseX, mouseY, mouseButton);
                return; // do not propagate to module/category clicks when interacting inside panel
            }
        }

        // Allow clicking categories/modules even when a settings panel is open
        handleCategoryButtonClicks();
        handleModuleButtonClicks(mouseX, mouseY);
        handleCommandToggleClicks(mouseX, mouseY);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        mainScroll.endScroll();
        commandScroll.endScroll();
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        handleScrollbarDrag(mouseX, mouseY);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);
        if (showCommandSettings && SelectedModule != null && SelectedModule.name.equals("Fast Hotkey")) {
            handleFastHotKeyTyping(typedChar, keyCode);
            return;
        }

        handleAllInputTyping(typedChar, keyCode);

    }

    // =============================================
    // Drawing Methods
    // =============================================
    private void drawBackground() {
        drawRect(guiLeft, guiTop, guiLeft + Dimensions.GUI_WIDTH, guiTop + Dimensions.GUI_HEIGHT, Colors.PANEL);

        String title = "§l§nRat All Of You";
        int titleX = guiLeft + 15;
        int titleY = guiTop + 10;
        fontRendererObj.drawStringWithShadow(title, titleX, titleY, Colors.TEXT);

        drawRect(guiLeft + 5, guiTop + 25, guiLeft + 115, guiTop + Dimensions.GUI_HEIGHT - 5, Colors.CATEGORY);
        drawRect(guiLeft + 115, guiTop + 25, guiLeft + Dimensions.GUI_WIDTH - 5, guiTop + Dimensions.GUI_HEIGHT - 5, Colors.CATEGORY);

        drawCenteredString(fontRendererObj, "§7Version v1.0 §8| §7Created by AfterTime", width / 2, guiTop + Dimensions.GUI_HEIGHT - 20, Colors.VERSION);
    }

    private void drawCategories() {
        for (GuiButton btn : categoryButtons) {
            drawRect(btn.xPosition - 2, btn.yPosition - 2, btn.xPosition + btn.width + 2, btn.yPosition + btn.height + 2, Colors.CATEGORY_BUTTON);
        }
    }

    private void drawModules(int mouseX, int mouseY) {
        int scissorX = guiLeft + 115;
        int scissorY = guiTop + 25;
        int scissorWidth = Dimensions.GUI_WIDTH - 120 - Dimensions.SCROLLBAR_WIDTH;
        int scissorHeight = Dimensions.GUI_HEIGHT - 50;
        int scale = new ScaledResolution(Minecraft.getMinecraft()).getScaleFactor();

        glEnable(GL_SCISSOR_TEST);
        glScissor(scissorX * scale, (height - (scissorY + scissorHeight)) * scale, scissorWidth * scale, scissorHeight * scale);

        for (ModuleButton moduleBtn : moduleButtons) {
            moduleBtn.draw(mouseX, mouseY, 0, fontRendererObj);
        }

        glDisable(GL_SCISSOR_TEST);
    }

    private void drawScrollbars() {
        if (mainScroll.shouldRenderScrollbar()) {
            mainScroll.drawScrollbar(Colors.SCROLLBAR, Colors.SCROLLBAR_HANDLE);
        }

        if (showCommandSettings && commandScroll.shouldRenderScrollbar()) {
            commandScroll.drawScrollbar(Colors.COMMAND_SCROLLBAR, Colors.COMMAND_SCROLLBAR_HANDLE);
        }
    }

    private void drawCommandPanel(int mouseX, int mouseY) {
        if (!showCommandSettings || SelectedModule == null) return;

        int panelX = guiLeft + Dimensions.COMMAND_PANEL_X;
        int panelY = guiTop + Dimensions.COMMAND_PANEL_Y;
        int panelWidth = Dimensions.COMMAND_PANEL_WIDTH;
        int panelHeight = Dimensions.GUI_HEIGHT - 60;

        drawRect(panelX, panelY, panelX + panelWidth, panelY + panelHeight, Colors.COMMAND_PANEL);
        drawRect(panelX - 1, panelY - 1, panelX + panelWidth + 1, panelY + panelHeight + 1, Colors.COMMAND_BORDER);

        String title = getCommandPanelTitle();
        drawCenteredString(fontRendererObj, title, panelX + panelWidth / 2, panelY + 5, Colors.COMMAND_TEXT);

        if (SelectedModule.name.equals("Fast Hotkey")) {
            drawFastHotKeyPanel(mouseX, mouseY, panelX, panelY, panelWidth, panelHeight);
            return;
        }

        drawCommandPanelContent(mouseX, mouseY, panelX, panelY, panelWidth, panelHeight);
    }

    // =============================================
    // Fast Hotkey editor drawing and input
    // =============================================
    private void drawFastHotKeyPanel(int mouseX, int mouseY, int panelX, int panelY, int panelWidth, int panelHeight) {
        int scale = new ScaledResolution(Minecraft.getMinecraft()).getScaleFactor();
        glEnable(GL_SCISSOR_TEST);
        glScissor(panelX * scale, (height - (panelY + panelHeight)) * scale, panelWidth * scale, (panelHeight - 25) * scale);

        int contentY = panelY + 25 - commandScroll.getOffset();
        int x = panelX + 5;
        int w = panelWidth - 10;

        List<FastHotkeyEntry> entries = AllConfig.INSTANCE.FAST_HOTKEY_ENTRIES;

        // Ensure rows match entries count
        if (fastRows.size() != entries.size()) {
            fastRows.clear();
            for (FastHotkeyEntry e : entries) fastRows.add(new FastRow(guiLeft, e));
        }

        for (int i = 0; i < fastRows.size(); i++) {
            FastRow row = fastRows.get(i);
            int rowTop = contentY + i * Dimensions.FH_ROW_HEIGHT;
            if (rowTop + Dimensions.FH_ROW_HEIGHT < panelY + 25 || rowTop > panelY + panelHeight) continue;

            // Background line separator
            drawRect(x, rowTop - 2, x + w, rowTop - 1, 0x33000000);

            // Titles and inputs (vertical layout)
            String labelTitle = "Command " + (i + 1) + " Label:";
            String cmdTitle = "Command " + (i + 1) + " Command:";
            int title1Y = rowTop + 2;
            int labelInputY = title1Y + 12;
            int title2Y = labelInputY + Dimensions.FH_INPUT_HEIGHT + Dimensions.FH_GAP_Y + 4;
            int commandInputY = title2Y + 12;

            fontRendererObj.drawStringWithShadow(labelTitle, x, title1Y, Colors.COMMAND_TEXT);
            fontRendererObj.drawStringWithShadow(cmdTitle, x, title2Y, Colors.COMMAND_TEXT);

            // Inputs full width
            row.DrawElements(mouseX, mouseY, labelInputY, commandInputY);
            // Remove button (below inputs, left-aligned)
            int removeX = x;
            int removeY = commandInputY + Dimensions.FH_INPUT_HEIGHT + Dimensions.FH_GAP_Y;
            boolean hoverRemove = mouseX >= removeX && mouseX <= removeX + Dimensions.FH_REMOVE_WIDTH && mouseY >= removeY && mouseY <= removeY + Dimensions.FH_REMOVE_HEIGHT;
            drawRect(removeX, removeY, removeX + Dimensions.FH_REMOVE_WIDTH, removeY + Dimensions.FH_REMOVE_HEIGHT, hoverRemove ? Colors.BUTTON_RED_HOVER : Colors.BUTTON_RED);
            drawCenteredString(fontRendererObj, "Remove", removeX + Dimensions.FH_REMOVE_WIDTH / 2, removeY + 5, Colors.BUTTON_TEXT);
        }

        // Add button
        int addY = contentY + fastRows.size() * Dimensions.FH_ROW_HEIGHT + 8;
        int addW = 60;
        boolean canAdd = entries.size() < 12;
        boolean hoverAdd = mouseX >= x && mouseX <= x + addW && mouseY >= addY && mouseY <= addY + Dimensions.FH_ADD_HEIGHT;
        drawRect(x, addY, x + addW, addY + Dimensions.FH_ADD_HEIGHT, (hoverAdd && canAdd) ? Colors.BUTTON_GREEN_HOVER : Colors.BUTTON_GREEN);
        int addTextColor = canAdd ? Colors.BUTTON_TEXT : 0xFFDDDDDD;
        drawCenteredString(fontRendererObj, "Add", x + addW / 2, addY + 6, addTextColor);

        glDisable(GL_SCISSOR_TEST);

        // Scrollbar
        int totalHeight = fastRows.size() * Dimensions.FH_ROW_HEIGHT + Dimensions.FH_ADD_HEIGHT + 10;
        commandScroll.update(totalHeight, panelHeight - 25);
        if (commandScroll.shouldRenderScrollbar()) {
            commandScroll.updateScrollbarPosition(panelX + panelWidth - Dimensions.SCROLLBAR_WIDTH - 2, panelY + 25, panelHeight - 25);
            commandScroll.drawScrollbar(Colors.COMMAND_SCROLLBAR, Colors.COMMAND_SCROLLBAR_HANDLE);
        }
    }

    private void handleFastHotKeyClicks(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton != 0) return;
        int panelX = guiLeft + Dimensions.COMMAND_PANEL_X;
        int panelY = guiTop + Dimensions.COMMAND_PANEL_Y;
        int panelWidth = Dimensions.COMMAND_PANEL_WIDTH;
        int panelHeight = Dimensions.GUI_HEIGHT - 60;

        int x = panelX + 5;
        int contentY = panelY + 25 - commandScroll.getOffset();

        List<FastHotkeyEntry> entries = AllConfig.INSTANCE.FAST_HOTKEY_ENTRIES;

        // Add button
        int addY = contentY + fastRows.size() * Dimensions.FH_ROW_HEIGHT + 8;
        int addW = 60;
        if (entries.size() < 12 && mouseX >= x && mouseX <= x + addW && mouseY >= addY && mouseY <= addY + Dimensions.FH_ADD_HEIGHT) {
            FastHotkeyEntry e = new FastHotkeyEntry("", "", entries.size());
            entries.add(e);
            fastRows.add(new FastRow(guiLeft, e));
            e.SetProperty();
            int totalHeight = fastRows.size() * Dimensions.FH_ROW_HEIGHT + Dimensions.FH_ADD_HEIGHT + 10;
            commandScroll.update(totalHeight, panelHeight - 25);
            return;
        }

        // Rows
        for (int i = 0; i < fastRows.size(); i++) {
            FastRow row = fastRows.get(i);
            int rowTop = contentY + i * Dimensions.FH_ROW_HEIGHT;

            // Titles and inputs to compute remove button position
            int title1Y = rowTop + 2;
            int labelInputY = title1Y + 12;
            int title2Y = labelInputY + Dimensions.FH_INPUT_HEIGHT + Dimensions.FH_GAP_Y + 4;
            int commandInputY = title2Y + 12;

            // Remove button click (below inputs, left-aligned)
            int removeX = x;
            int removeY = commandInputY + Dimensions.FH_INPUT_HEIGHT + Dimensions.FH_GAP_Y;
            if (mouseX >= removeX && mouseX <= removeX + Dimensions.FH_REMOVE_WIDTH && mouseY >= removeY && mouseY <= removeY + Dimensions.FH_REMOVE_HEIGHT) {
                row.entry.RemoveProperty();
                entries.remove(row.entry);
                fastRows.remove(i);

                int totalHeight = fastRows.size() * Dimensions.FH_ROW_HEIGHT + Dimensions.FH_ADD_HEIGHT + 10;
                commandScroll.update(totalHeight, panelHeight - 25);
                return;
            }


            if (row.labelInput.isMouseOver(mouseX, mouseY, labelInputY)) {
                unfocusAllFastInputs();
                row.labelInput.beginEditing(mouseX);
                return;
            }
            if (row.commandInput.isMouseOver(mouseX, mouseY, commandInputY)) {
                unfocusAllFastInputs();
                row.commandInput.beginEditing(mouseX);
                return;
            }
        }
    }

    private void handleFastHotKeyTyping(char typedChar, int keyCode) {
        for (FastRow row : fastRows) {
            if (row.labelInput.isEditing) {
                row.labelInput.handleKeyTyped(typedChar, keyCode);
                return;
            }
            if (row.commandInput.isEditing) {
                row.commandInput.handleKeyTyped(typedChar, keyCode);
                return;
            }
        }
    }

    private void unfocusAllFastInputs() {
        for (FastRow r : fastRows) {
            r.labelInput.isEditing = false;
            r.commandInput.isEditing = false;
        }
    }

    // =============================================
    // Input Handling Methods
    // =============================================
    private void handleInputFieldEditingState() {
        for (ColorInput option : ColorInputs) {
            option.unfocus();

        }

        if (showCommandSettings && SelectedModule != null && SelectedModule.name.equals("Fast Hotkey")) {
            for (FastRow row : fastRows) {
                row.labelInput.isEditing = false;
                row.commandInput.isEditing = false;
            }
        }
    }

    private void handleScrollbarClicks(int mouseX, int mouseY) {
        // Start dragging if clicking inside scrollbar track; don't reset dragStart after check
        if (showCommandSettings && commandScroll.checkScrollbarClick(mouseX, mouseY)) {
            return;
        }
        if (mainScroll.checkScrollbarClick(mouseX, mouseY)) {
            return;
        }
    }

    private void handleCategoryButtonClicks() {
        for (GuiButton btn : categoryButtons) {
            if (btn.isMouseOver()) {
                actionPerformed(btn);
                return;
            }
        }
    }

    private void handleModuleButtonClicks(int mouseX, int mouseY) {
        for (ModuleButton moduleBtn : moduleButtons) {
            if (moduleBtn.isMouseOver(mouseX, mouseY)) {
                handleModuleButtonClick(moduleBtn, mouseX, mouseY);
                return;
            }
        }
    }

    // Add missing handler for module button clicks: open settings or toggle module
    private void handleModuleButtonClick(ModuleButton moduleBtn, int mouseX, int mouseY) {
        ModuleInfo module = moduleBtn.getModule();
        if ("Move GUI Position".equals(moduleBtn.getModule().name)) {
            UIHighlighter.enterMoveMode(Minecraft.getMinecraft().currentScreen);
        }
        if (moduleBtn.isDropdownClicked(mouseX, mouseY)) {
            // Open settings panel; ensure module is enabled
            if (!module.Data) {
                module.Data = true;
            }
            SelectedModule = module;
            showCommandSettings = true;
            initializeCommandToggles();
            return;
        }
        // Toggle on main row click
        boolean wasEnabled = module.Data;
        module.Data = !module.Data;
        if (wasEnabled && !module.Data && SelectedModule == module) {
            // Closing settings if disabling the module currently being configured
            showCommandSettings = false;
            SelectedModule = null;
        }

    }

    private void handleCommandToggleClicks(int mouseX, int mouseY) {
        if (!showCommandSettings) return;

        // Handle labelled text input clicks first
        if (handleLabelledInputClicks(mouseX, mouseY)) return;

        // Handle dropdown clicks
        if (handleDropdownClicks(mouseX, mouseY)) return;

        // Handle color/text input clicks for color pickers
        if (handleColorInputClicks(mouseX, mouseY)) return;

        // Handle regular toggle clicks (inlined to avoid missing symbol in generated sources)
        if (SelectedModule == null) return;
        int y = guiTop + Dimensions.COMMAND_PANEL_Y + 30 - commandScroll.getOffset();
        for (Toggle toggle : Toggles) {
            if (toggle.isMouseOver(mouseX, mouseY, y)) {
                toggle.toggle();
                if (toggle.ref != null && toggle.ref.ConfigType == 4) {
                    TerminalSettingsApplier.applyFromAllConfig();
                }
                return;
            }
            y += 22;
        }
    }

    // Handle clicks on dropdowns
    private boolean handleDropdownClicks(int mouseX, int mouseY) {
        if (SelectedModule == null) return false;
        int y = guiTop + Dimensions.COMMAND_PANEL_Y + 30 - commandScroll.getOffset();
        // advance past toggles and labelled inputs and color inputs (drawn before dropdowns)
        for (Toggle ignored : Toggles) y += 22;
        for (LabelledInput li : labelledInputs) y += li.getVerticalSpace();
        for (ColorInput ignored : ColorInputs) y += 50;

        for (MethodDropdown dd : methodDropdowns) {
            int yPos = y;
            int bx = dd.x + 100;
            int bw = dd.width - 100;
            int bh = dd.height;
            boolean inBase = mouseX >= bx && mouseX <= bx + bw && mouseY >= yPos && mouseY <= yPos + bh;
            if (inBase) {
                for (MethodDropdown other : methodDropdowns) other.isOpen = false;
                dd.isOpen = !dd.isOpen;
                return true;
            }
            if (dd.isOpen) {
                for (int i = 0; i < dd.methods.length; i++) {
                    int optionY = yPos + bh + (i * bh);
                    boolean inOpt = mouseX >= bx && mouseX <= bx + bw && mouseY >= optionY && mouseY <= optionY + bh;
                    if (inOpt) {
                        dd.selectMethod(i);
                        dd.isOpen = false;
                        return true;
                    }
                }
            }
            y += 22;
        }
        return false;
    }

    // New: handle clicks on labelled inputs with proper y-tracking
    private boolean handleLabelledInputClicks(int mouseX, int mouseY) {
        if (SelectedModule == null) return false;
        int y = guiTop + Dimensions.COMMAND_PANEL_Y + 30 - commandScroll.getOffset();
        // Skip toggles (they draw first)
        for (Toggle ignored : Toggles) y += 22;
        for (LabelledInput li : labelledInputs) {
            if (li.isMouseOver(mouseX, mouseY, y)) {
                // Unfocus other inputs
                for (LabelledInput other : labelledInputs) other.isEditing = false;
                li.beginEditing(mouseX);
                return true;
            }
            y += li.getVerticalSpace();
        }
        return false;
    }

    // Focus color/text inputs when clicked inside their input boxes
    private boolean handleColorInputClicks(int mouseX, int mouseY) {
        if (SelectedModule == null) return false;
        int y = guiTop + Dimensions.COMMAND_PANEL_Y + 30 - commandScroll.getOffset();
        for (Toggle ignored : Toggles) y += 22;
        for (LabelledInput li : labelledInputs) y += li.getVerticalSpace();

        for (ColorInput ci : ColorInputs) {
            int inputY = y + ci.height + 8;
            boolean hover = (mouseX >= ci.x + 40 && mouseX <= ci.x + ci.width && mouseY >= inputY - 2 && mouseY <= inputY + 15);
            if (hover) {
                ci.beginEditing(mouseX);
                return true;
            }
            y += 50;
        }
        return false;
    }

    private void AddEntryAsOption(Map.Entry<String, BaseConfig<?>> entry, Integer y, int ConfigType) {
        PropertyRef ref = new PropertyRef(ConfigType, entry.getKey());
        Type type = entry.getValue().type;
        Object data = entry.getValue().Data;
        int xPos = guiLeft + Dimensions.COMMAND_PANEL_X + 5;
        int width = Dimensions.COMMAND_PANEL_WIDTH - 10;
        boolean isTerminal = (ConfigType == 4);
        if (type.equals(String.class)) {
            labelledInputs.add(new LabelledInput(ref, entry.getValue().name, String.valueOf(data), xPos, y, width, 16, isTerminal));
        } else if (type.equals(Boolean.class)) {
            Toggles.add(new Toggle(ref, entry.getValue().name, entry.getValue().description, (Boolean) data, xPos, y, width, 16));
        } else if (type.equals(Integer.class)) {
            labelledInputs.add(new LabelledInput(ref, entry.getValue().name, String.valueOf(data), xPos, y, width, 16, isTerminal));
        } else if (type.equals(Float.class)) {
            labelledInputs.add(new LabelledInput(ref, entry.getValue().name, String.valueOf(data), xPos, y, width, 16, isTerminal));
        } else if (type.equals(DataType_DropDown.class)) {
            DataType_DropDown dropdownData = (DataType_DropDown) data;
            methodDropdowns.add(new MethodDropdown(ref, entry.getValue().name, dropdownData.selectedIndex, xPos, y, width, 16, dropdownData.options));
        } else if (type.equals(Color.class)) {
            ColorInputs.add(new ColorInput(ref, entry.getValue().name, (Color) data, xPos, y, width, 18));
        } else {
            // Unsupported type
            System.err.println("Unsupported config type: " + type);
        }

    }

    private void Add_SubSetting_Terminal(Integer y) {
        // Show per-terminal enables first in a fixed order
        addTerminalEntry("terminal_enable_numbers", y);
        addTerminalEntry("terminal_enable_starts_with", y);
        addTerminalEntry("terminal_enable_colors", y);
        addTerminalEntry("terminal_enable_red_green", y);
        addTerminalEntry("terminal_enable_rubix", y);
        addTerminalEntry("terminal_enable_melody", y);
        // Then general defaults
        addTerminalEntry("terminal_high_ping_mode", y);
        addTerminalEntry("terminal_phoenix_client_compat", y);
        addTerminalEntry("terminal_scale", y);
        addTerminalEntry("terminal_timeout_ms", y);
        addTerminalEntry("terminal_first_click_ms", y);
        addTerminalEntry("terminal_offset_x", y);
        addTerminalEntry("terminal_offset_y", y);
        addTerminalEntry("terminal_overlay_color", y);
        addTerminalEntry("terminal_background_color", y);
    }

    private void addTerminalEntry(String key, Integer y) {
        com.aftertime.ratallofyou.UI.config.ConfigData.BaseConfig<?> cfg = AllConfig.INSTANCE.TERMINAL_CONFIGS.get(key);
        if (cfg == null) return;
        java.util.Map.Entry<String, com.aftertime.ratallofyou.UI.config.ConfigData.BaseConfig<?>> entry =
                new java.util.AbstractMap.SimpleEntry<>(key, cfg);
        AddEntryAsOption(entry, y, 4);
    }
    private void Add_SubSetting_Command(Integer y)
    {
        for (Map.Entry<String, BaseConfig<?>> entry : AllConfig.INSTANCE.COMMAND_CONFIGS.entrySet()) {
            AddEntryAsOption(entry, y, 0);
        }
    }
    private void Add_SubSetting_NoDebuff(Integer y) {
        for (Map.Entry<String, BaseConfig<?>> entry : AllConfig.INSTANCE.NODEBUFF_CONFIGS.entrySet()) {
            AddEntryAsOption(entry, y, 2);
        }
    }
    private void Add_SubSetting_Etherwarp(Integer y) {
        for (Map.Entry<String, BaseConfig<?>> entry : AllConfig.INSTANCE.ETHERWARP_CONFIGS.entrySet()) {
            AddEntryAsOption(entry, y, 3);
        }
    }

    private void initializeCommandToggles() {
        // Clear all option lists to avoid duplicates when re-opening panel
        Toggles.clear();
        labelledInputs.clear();
        methodDropdowns.clear();
        ColorInputs.clear();

        if (SelectedModule == null) return;
        Integer y = guiTop + Dimensions.COMMAND_PANEL_Y + 30; // content start (without scroll)

        switch (SelectedModule.name) {
            case "Dungeon Terminals":
                Add_SubSetting_Terminal(y);
                break;
            case "Party Commands":
                Add_SubSetting_Command(y);
                break;
            case "No Debuff":
                Add_SubSetting_NoDebuff(y);
                break;
            case "Etherwarp Overlay":
                Add_SubSetting_Etherwarp(y);
                break;
            case "Fast Hotkey":
                // Fast Hotkey uses a dedicated renderer; no generic options here
                break;
        }

        // Compute content height based on elements rather than absolute coordinates
        int contentHeight = 0;
        contentHeight += Toggles.size() * 22;
        for (LabelledInput li : labelledInputs) contentHeight += li.getVerticalSpace();
        contentHeight += ColorInputs.size() * 50;
        contentHeight += methodDropdowns.size() * 22;

        int panelViewHeight = Dimensions.GUI_HEIGHT - 60 - 25; // visible content area under header
        commandScroll.update(contentHeight, panelViewHeight);
        commandScroll.updateScrollbarPosition(
                guiLeft + Dimensions.COMMAND_PANEL_X + Dimensions.COMMAND_PANEL_WIDTH - Dimensions.SCROLLBAR_WIDTH - 2,
                guiTop + Dimensions.COMMAND_PANEL_Y + 25,
                panelViewHeight
        );
    }

    private String getCommandPanelTitle() {
        return SelectedModule == null ? "" : ("Settings - " + SelectedModule.name);
    }

    private void drawCommandPanelContent(int mouseX, int mouseY, int panelX, int panelY, int panelWidth, int panelHeight) {
        int scale = new ScaledResolution(Minecraft.getMinecraft()).getScaleFactor();
        glEnable(GL_SCISSOR_TEST);
        glScissor(panelX * scale, (height - (panelY + panelHeight)) * scale, panelWidth * scale, (panelHeight - 25) * scale);

        int y = panelY + 30 - commandScroll.getOffset();
        for (Toggle t : Toggles) {
            t.draw(mouseX, mouseY, y, fontRendererObj);
            y += 22;
        }
        for (LabelledInput t : labelledInputs) {
            t.draw(mouseX, mouseY, y, fontRendererObj);
            y += t.getVerticalSpace();
        }
        for (ColorInput t : ColorInputs) {
            t.draw(mouseX, mouseY, y, fontRendererObj);
            y += 50;
        }
        for (MethodDropdown t : methodDropdowns) {
            t.draw(mouseX, mouseY, y, fontRendererObj);
            y += 22;
        }

        glDisable(GL_SCISSOR_TEST);

        // Recompute content height and update scrollbar each frame
        int contentHeight = 0;
        contentHeight += Toggles.size() * 22;
        for (LabelledInput li : labelledInputs) contentHeight += li.getVerticalSpace();
        contentHeight += ColorInputs.size() * 50;
        contentHeight += methodDropdowns.size() * 22;

        int viewH = panelHeight - 25;
        commandScroll.update(contentHeight, viewH);
        commandScroll.updateScrollbarPosition(panelX + panelWidth - Dimensions.SCROLLBAR_WIDTH - 2, panelY + 25, viewH);
    }

    private void handleScrollbarDrag(int mouseX, int mouseY) {
        if (mainScroll.isDragging) mainScroll.handleDrag(mouseX, mouseY, this::buildModuleButtons);
        if (commandScroll.isDragging) commandScroll.handleDrag(mouseX, mouseY, null);
    }

    private void handleAllInputTyping(char typedChar, int keyCode) {
        if (!showCommandSettings) return;
        for (ColorInput t : ColorInputs) {
            t.handleKeyTyped(typedChar, keyCode);
        }
        for (LabelledInput t : labelledInputs) {
            t.handleKeyTyped(typedChar, keyCode);
        }

    }

    // Re-added builders and toggle click handler
    private void buildCategoryButtons() {
        categoryButtons.clear();
        buttonList.clear();
        int y = guiTop + 30;
        int x = guiLeft + 10;
        for (int i = 0; i < AllConfig.INSTANCE.Categories.size(); i++) {
            GuiButton b = new GuiButton(1000 + i, x, y, 95, 18, AllConfig.INSTANCE.Categories.get(i));
            categoryButtons.add(b);
            buttonList.add(b);
            y += 20;
        }
    }

    private void buildModuleButtons() {
        moduleButtons.clear();
        int listX = guiLeft + 120;
        int listY = guiTop + 28;
        int listW = Dimensions.GUI_WIDTH - 120 - 10 - Dimensions.SCROLLBAR_WIDTH;
        int y = listY - mainScroll.getOffset();
        int rowH = 32; // two-line (name + description)
        int visibleCount = 0;
        for (BaseConfig<?> mi : AllConfig.INSTANCE.MODULES.values()) {
            ModuleInfo info = (ModuleInfo) mi;
            if (info.category.equals(selectedCategory)) {
                boolean hasSettings = hasSettings(info);
                moduleButtons.add(new ModuleButton(listX + 4, y, listW - 8, rowH - 2, info, hasSettings));
                y += rowH;
                visibleCount++;
            }
        }
        int totalHeight = visibleCount * rowH;
        int viewH = Dimensions.GUI_HEIGHT - 50;
        mainScroll.update(totalHeight, viewH);
        mainScroll.updateScrollbarPosition(listX + listW - 2, listY, viewH);
    }

    // Determine whether a module has extra settings (shows the ellipsis)
    private boolean hasSettings(ModuleInfo module) {
        if (module == null || module.name == null) return false;
        switch (module.name) {
            case "Dungeon Terminals":
            case "Party Commands":
            case "No Debuff":
            case "Etherwarp Overlay":
            case "Fast Hotkey":
                return true;
            default:
                return false;
        }
    }

    private void createCategoryButtons() { buildCategoryButtons(); }
    private void createModuleButtons() { buildModuleButtons(); }





    // =============================================
    // Inner UI model classes
    // =============================================

    private class ScrollManager {
        boolean isDragging = false;
        private int contentHeight;
        private int viewHeight;
        private int offset;
        private int barX, barY, barH;
        private int handleY, handleH;
        private int dragStartY, dragStartOffset;

        void reset() {
            contentHeight = 0;
            viewHeight = 0;
            offset = 0;
            isDragging = false;
        }

        int getOffset() {
            return Math.max(0, Math.min(offset, Math.max(0, contentHeight - viewHeight)));
        }

        void update(int total, int view) {
            contentHeight = Math.max(0, total);
            viewHeight = Math.max(0, view);
            int maxOffset = Math.max(0, contentHeight - viewHeight);
            if (offset > maxOffset) offset = maxOffset;
            recalcHandle();
        }

        void updateScrollbarPosition(int x, int y, int h) {
            barX = x;
            barY = y;
            barH = h;
            recalcHandle();
        }

        boolean shouldRenderScrollbar() {
            return contentHeight > viewHeight && viewHeight > 0;
        }

        void drawScrollbar(int trackColor, int handleColor) {
            if (!shouldRenderScrollbar()) return;
            drawRect(barX, barY, barX + Dimensions.SCROLLBAR_WIDTH, barY + barH, trackColor);
            drawRect(barX, handleY, barX + Dimensions.SCROLLBAR_WIDTH, handleY + handleH, handleColor);
        }

        boolean checkScrollbarClick(int mx, int my) {
            if (!shouldRenderScrollbar()) return false;
            boolean inside = mx >= barX && mx <= barX + Dimensions.SCROLLBAR_WIDTH && my >= barY && my <= barY + barH;
            if (inside) {
                isDragging = true;
                dragStartY = my;
                dragStartOffset = offset;
            }
            return inside;
        }

        void beginScroll() {
            isDragging = true;
            dragStartY = -1;
            dragStartOffset = offset;
        }

        void endScroll() {
            isDragging = false;
        }

        void handleDrag(int mx, int my, Runnable onChange) {
            if (!isDragging || !shouldRenderScrollbar()) return;
            if (handleH >= barH) {
                offset = 0;
                if (onChange != null) onChange.run();
                return;
            }
            float ratio = (float) (contentHeight - viewHeight) / (float) (barH - handleH);
            int dy = my - dragStartY;
            offset = Math.max(0, Math.min(contentHeight - viewHeight, dragStartOffset + Math.round(dy * ratio)));
            recalcHandle();
            if (onChange != null) onChange.run();
        }

        void handleWheelScroll(int dWheel) {
            handleWheelScroll(dWheel, null);
        }

        void handleWheelScroll(int dWheel, Runnable onChange) {
            int step = 20 * (dWheel < 0 ? 1 : -1);
            offset = Math.max(0, Math.min(contentHeight - viewHeight, offset + step));
            recalcHandle();
            if (onChange != null) onChange.run();
        }

        private void recalcHandle() {
            if (!shouldRenderScrollbar()) {
                handleY = barY;
                handleH = barH;
                return;
            }
            int minH = Math.max(Dimensions.MIN_SCROLLBAR_HEIGHT, barH * Math.max(1, viewHeight) / Math.max(1, contentHeight));
            handleH = Math.max(Dimensions.MIN_SCROLLBAR_HEIGHT, minH);
            float t = (contentHeight - viewHeight) == 0 ? 0f : (float) offset / (float) (contentHeight - viewHeight);
            handleY = barY + Math.round((barH - handleH) * t);
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int dWheel = Mouse.getEventDWheel();
        if (dWheel == 0) return;

        // Translate to scaled GUI coords
        int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
        int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;

        // Main modules list area
        int listX = guiLeft + 115;
        int listY = guiTop + 25;
        int listW = Dimensions.GUI_WIDTH - 120 - Dimensions.SCROLLBAR_WIDTH;
        int listH = Dimensions.GUI_HEIGHT - 50;
        if (mouseX >= listX && mouseX <= listX + listW && mouseY >= listY && mouseY <= listY + listH) {
            mainScroll.handleWheelScroll(dWheel, this::buildModuleButtons);
            return;
        }

        // Command panel content area
        if (showCommandSettings) {
            int panelX = guiLeft + Dimensions.COMMAND_PANEL_X;
            int panelY = guiTop + Dimensions.COMMAND_PANEL_Y + 25; // skip panel header
            int panelW = Dimensions.COMMAND_PANEL_WIDTH;
            int panelH = (Dimensions.GUI_HEIGHT - 60) - 25;
            if (mouseX >= panelX && mouseX <= panelX + panelW && mouseY >= panelY && mouseY <= panelY + panelH) {
                commandScroll.handleWheelScroll(dWheel, null);
            }
        }
    }
}
