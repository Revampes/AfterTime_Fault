package com.aftertime.ratallofyou.UI.config;

import com.aftertime.ratallofyou.UI.buildMethod.buildCategoryButtons;
import com.aftertime.ratallofyou.UI.buildMethod.buildModuleButtons;
import com.aftertime.ratallofyou.UI.config.ConfigData.*;
import com.aftertime.ratallofyou.UI.config.OptionElements.*;
import com.aftertime.ratallofyou.UI.config.commonConstant.Dimensions;
import com.aftertime.ratallofyou.UI.config.drawMethod.*;
import com.aftertime.ratallofyou.UI.config.handler.*;
import com.aftertime.ratallofyou.UI.event.computeInlineContentHeight;
import com.aftertime.ratallofyou.UI.init.initializeCommandToggles;
import com.aftertime.ratallofyou.UI.utils.InlineArea;
import com.aftertime.ratallofyou.UI.utils.isFhkKeyDuplicate;
import com.aftertime.ratallofyou.modules.dungeon.terminals.TerminalSettingsApplier;
import com.aftertime.ratallofyou.UI.config.OptionElements.Toggle;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard; // Added for key code names and capture

import java.awt.*;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.lwjgl.opengl.GL11.*;
import com.aftertime.ratallofyou.UI.config.panels.HotbarSwapPanel;

public class ModSettingsGui extends GuiScreen {
    // Fields
    public final List<GuiButton> categoryButtons = new ArrayList<>();
    public final List<ModuleButton> moduleButtons = new ArrayList<>();
    public final List<ColorInput> ColorInputs = new ArrayList<>();
    public final List<LabelledInput> labelledInputs = new ArrayList<>();
    public final List<MethodDropdown> methodDropdowns = new ArrayList<>();
    public final List<Toggle> Toggles = new ArrayList<>();

    public final ScrollManager mainScroll = new ScrollManager();
    public final ScrollManager commandScroll = new ScrollManager();

    // Fast Hotkey editor rows (right-side detail panel)
    public final List<FastRow> fastRows = new ArrayList<>();

    // New: Hotbar Swap UI extracted to its own panel
    public final HotbarSwapPanel hotbarPanel = new HotbarSwapPanel();

    public String selectedCategory = "Kuudra";
    public ModuleInfo SelectedModule = null;
    public boolean showCommandSettings = false;
    public int guiLeft;
    public int guiTop;

    // Error handling for modules without settings
    public String showNoSettingsError = null;
    public long noSettingsErrorTime = 0;

    // Layout modes
    public boolean useSidePanelForSelected = false; // Fast Hotkey only
    public boolean optionsInline = false; // inline box below module row

    // Fast Hotkey state (left panel preset list + input)
    public SimpleTextField fhkPresetNameInput = null;
    public int fhkSelectedPreset = -1; // if >=0, detail panel open
    // Fast Hotkey inline key-capture index
    public int fhkKeyCaptureIndex = -1;

    // Tracks dropdown baseline for inline mode so overlays can align exactly
    public int inlineDropdownBaseY = -1;

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
        this.fhkSelectedPreset = -1;
        this.fhkPresetNameInput = new SimpleTextField("", 0, 0, 100, 16);
        this.useSidePanelForSelected = false;
        this.optionsInline = false;

        buildCategoryButtons();
        buildModuleButtons();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        screenDrawer.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (categoryButtons.contains(button)) {
            selectedCategory = button.displayString;
            mainScroll.reset();
            showCommandSettings = false;
            SelectedModule = null;
            useSidePanelForSelected = false;
            optionsInline = false;
            buildModuleButtons();
        }
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        ConfigIO.INSTANCE.SaveProperties();
        TerminalSettingsApplier.applyFromAllConfig();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        handleInputFieldEditingState();
        handleScrollbarClicks(mouseX, mouseY);

        // Fast Hotkey side+detail panels capture clicks inside their areas
        if (showCommandSettings && useSidePanelForSelected && SelectedModule != null && "Fast Hotkey".equals(SelectedModule.name)) {
            int panelX = guiLeft + Dimensions.COMMAND_PANEL_X;
            int panelY = guiTop + Dimensions.COMMAND_PANEL_Y;
            int panelWidth = Dimensions.COMMAND_PANEL_WIDTH;
            int panelHeight = Dimensions.GUI_HEIGHT - 60;
            boolean inLeft = mouseX >= panelX && mouseX <= panelX + panelWidth && mouseY >= panelY && mouseY <= panelY + panelHeight;
            int detailX = panelX + panelWidth + 6;
            int detailW = 170;
            boolean inRight = mouseX >= detailX && mouseX <= detailX + detailW && mouseY >= panelY && mouseY <= panelY + panelHeight;
            if (inLeft || inRight) {
                handleFastHotKeyClicks(mouseX, mouseY, mouseButton);
                return;
            }
        }

        // Fast Hotkey inline mode: capture clicks in right-side detail editor only
        if (showCommandSettings && optionsInline && SelectedModule != null && "Fast Hotkey".equals(SelectedModule.name) && fhkSelectedPreset >= 0) {
            int panelX = guiLeft + Dimensions.COMMAND_PANEL_X;
            int panelY = guiTop + Dimensions.COMMAND_PANEL_Y;
            int panelWidth = Dimensions.COMMAND_PANEL_WIDTH;
            int panelHeight = Dimensions.GUI_HEIGHT - 60;
            int detailX = getInlineDetailX(); // inline mode: editor starts right after inline box
            int detailW = 170;
            boolean inRight = mouseX >= detailX && mouseX <= detailX + detailW && mouseY >= panelY && mouseY <= panelY + panelHeight;
            if (inRight) {
                handleFastHotKeyClicks(mouseX, mouseY, mouseButton);
                return;
            }
        }

        // Inline box consumes inner clicks; if a dropdown is open, allow overlay clicks even outside the box
        if (showCommandSettings && optionsInline && SelectedModule != null) {
            InlineArea ia = getInlineAreaForSelected();
            if (ia != null) {
                boolean inBox = mouseX >= ia.contentX && mouseX <= ia.contentX + ia.contentW && mouseY >= ia.boxY && mouseY <= ia.boxY + ia.boxH;
                if (inBox) { handleInlineOptionClicks(mouseX, mouseY, ia); return; }
                if (anyInlineDropdownOpen()) { handleInlineOptionClicks(mouseX, mouseY, ia); return; }
            }
        }

        handleCategoryButtonClicks(mouseX, mouseY);
        handleModuleButtonClicks(mouseX, mouseY);
        handleCommandToggleClicks(mouseX, mouseY);
    }

    public net.minecraft.client.gui.FontRenderer getFontRendererObj() {
        return this.fontRendererObj;
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
        if (showCommandSettings && optionsInline && SelectedModule != null && "Fast Hotkey".equals(SelectedModule.name)) {
            if (fhkPresetNameInput != null && fhkPresetNameInput.isEditing) { fhkPresetNameInput.handleKeyTyped(typedChar, keyCode); return; }
            handleFastHotKeyTyping(typedChar, keyCode); return;
        }
        if (showCommandSettings && useSidePanelForSelected && SelectedModule != null && "Fast Hotkey".equals(SelectedModule.name)) {
            if (fhkPresetNameInput != null && fhkPresetNameInput.isEditing) { fhkPresetNameInput.handleKeyTyped(typedChar, keyCode); return; }
            handleFastHotKeyTyping(typedChar, keyCode); return;
        }
        // New: Hotbar Swap inline typing
        if (showCommandSettings && optionsInline && SelectedModule != null && "Hotbar Swap".equals(SelectedModule.name)) {
            handleHotbarSwapTyping(typedChar, keyCode); return;
        }
        // New: Auto Fish inline typing (capture hotkey value)
        if (showCommandSettings && optionsInline && SelectedModule != null && "Auto Fish".equals(SelectedModule.name)) {
            handleAutoFishTyping(typedChar, keyCode); return;
        }
        if (showCommandSettings && optionsInline && SelectedModule != null && "Mark Location".equals(SelectedModule.name)) {
            handleMarkLocationTyping(typedChar, keyCode); return;
        }
        handleAllInputTyping(typedChar, keyCode);
    }

    private final drawBackground backgroundDrawer = new drawBackground(this);
    private final drawCategory categoryDrawer = new drawCategory(this);
    private final drawModule moduleDrawer = new drawModule(this);
    private final drawScrollbar scrollbarDrawer = new drawScrollbar(this);
    private final drawCommandPanel commandPanelDrawer = new drawCommandPanel(this);
    private final drawInlineSettingsBox InlineSettingsBoxDrawer = new drawInlineSettingsBox(this);
    private final drawFastHotKeyPanel fastHotKeyPanelDrawer = new drawFastHotKeyPanel(this);
    private final drawFastHotKeyDetailPanel fastHotKeyDetailPanelDrawer = new drawFastHotKeyDetailPanel(this);
    private final drawTooltipsAndErrors tooltipsAndErrorsDrawer = new drawTooltipsAndErrors(this);
    private final drawTooltip tooltipDrawer = new drawTooltip(this);
    private final drawDropdownOverlays dropdownOverlaysDrawer = new drawDropdownOverlays(this);
    private final drawScreen screenDrawer = new drawScreen(this);

    private final handleInlineOptionClicks inlineOptionClicksHandler = new handleInlineOptionClicks(this);
    private final handleFastHotKeyClicks fastHotKeyClicksHandler = new handleFastHotKeyClicks(this);
    private final handleFastHotKeyTyping fastHotKeyTypingHandler = new handleFastHotKeyTyping(this);
    private final handleHotbarSwapTyping hotbarSwapTypingHandler = new handleHotbarSwapTyping(this);
    private final handleAutoFishTyping autoFishTypingHandler = new handleAutoFishTyping(this);
    private final handleMarkLocationTyping markLocationTypingHandler = new handleMarkLocationTyping(this);
    private final handleInputFieldEditingState inputFieldEditingStateHandler = new handleInputFieldEditingState(this);
    private final handleScrollbarClicks scrollbarClicksHandler = new handleScrollbarClicks(this);
    private final handleCategoryButtonClicks categoryButtonClicksHandler = new handleCategoryButtonClicks(this);
    private final handleModuleButtonClicks moduleButtonClicksHandler = new handleModuleButtonClicks(this);
    private final handleModuleButtonClick moduleButtonClickHandler = new handleModuleButtonClick(this);
    private final handleCommandToggleClicks commandToggleClicksHandler = new handleCommandToggleClicks(this);
    private final handleDropdownClicks dropdownClicksHandler = new handleDropdownClicks(this);
    private final handleLabelledInputClicks labelledInputClicksHandler = new handleLabelledInputClicks(this);
    private final handleColorInputClicks colorInputClicksHandler = new handleColorInputClicks(this);
    private final handleButtonClicks buttonClicksHandler = new handleButtonClicks(this);
    private final handleScrollbarDrag scrollbarDragHandler = new handleScrollbarDrag(this);
    private final handleAllInputTyping allInputTypingHandler = new handleAllInputTyping(this);
    private final handleMouseInput mouseInputHandler = new handleMouseInput(this);

    private final buildCategoryButtons categoryButtonsBuilder = new buildCategoryButtons(this);
    private final buildModuleButtons moduleButtonsBuilder = new buildModuleButtons(this);

    private final computeInlineContentHeight inlineContentHeightComputer = new computeInlineContentHeight(this);

    private final initializeCommandToggles commandTogglesinitializer = new initializeCommandToggles(this);

    public void drawBackground() {
        backgroundDrawer.drawBackground();
    }

    public void drawCategories() {
        categoryDrawer.drawCategories();
    }

    public void drawModules(int mouseX, int mouseY) {
        moduleDrawer.drawModules(mouseX, mouseY);
    }

    public void drawScrollbars() {
        scrollbarDrawer.drawScrollbars();
    }

    public void drawCommandPanel(int mouseX, int mouseY) {
        commandPanelDrawer.drawCommandPanel(mouseX, mouseY);
    }

    public void drawInlineSettingsBox(int mouseX, int mouseY) {
        InlineSettingsBoxDrawer.drawInlineSettingsBox(mouseX, mouseY);
    }

    public void drawFastHotKeyPanel(int mouseX, int mouseY, int panelX, int panelY, int panelWidth, int panelHeight) {
        fastHotKeyPanelDrawer.drawFastHotKeyPanel(mouseX, mouseY, panelX, panelY, panelWidth, panelHeight);
    }

    // Right detail editor for commands
    public void drawFastHotkeyDetailPanel(int mouseX, int mouseY, int panelX, int panelY, int panelWidth, int panelHeight) {
        fastHotKeyDetailPanelDrawer.drawFastHotkeyDetailPanel(mouseX, mouseY, panelX, panelY, panelWidth, panelHeight);
    }

    // Draw tooltips and error messages
    public void drawTooltipsAndErrors(int mouseX, int mouseY) {
        tooltipsAndErrorsDrawer.drawTooltipsAndErrors(mouseX, mouseY);
    }

    public void drawTooltip(String text, int mouseX, int mouseY) {
        tooltipDrawer.drawTooltip(text, mouseX, mouseY);
    }

    public void drawDropdownOverlays(int mouseX, int mouseY) {
        dropdownOverlaysDrawer.drawDropdownOverlays(mouseX, mouseY);
    }

    // Helper: Title for command panel
    public String getCommandPanelTitle() {
        return SelectedModule == null ? "" : ("Settings - " + SelectedModule.name);
    }


    public int getInlineDetailX() {
        InlineArea ia = getInlineAreaForSelected();
        if (ia != null) return ia.boxX + ia.boxW + 6; // small gap after inline box
        return guiLeft + Dimensions.COMMAND_PANEL_X; // fallback
    }

    public InlineArea getInlineAreaForSelected() {
        if (!optionsInline || SelectedModule == null) return null;
        ModuleButton selBtn = null; for (ModuleButton b : moduleButtons) if (b.getModule() == SelectedModule) { selBtn = b; break; }
        if (selBtn == null) return null;
        int listX = guiLeft + 120;
        int listW = Dimensions.GUI_WIDTH - 120 - Dimensions.SCROLLBAR_WIDTH;
        int boxX = listX + 4; int boxW = listW - 8;
        int headerH = 20; int padding = 6;
        InlineArea ia = new InlineArea();
        ia.boxX = boxX; ia.boxY = selBtn.getY() + selBtn.getHeight() + 2; ia.boxW = boxW;
        ia.contentX = boxX + padding; ia.contentW = boxW - padding * 2; ia.contentY = ia.boxY + headerH;
        ia.boxH = headerH + computeInlineContentHeight() + padding;
        return ia;
    }

    public int computeInlineContentHeight() {
        return inlineContentHeightComputer.computeInlineContentHeight();
    }

    public void handleInlineOptionClicks(int mouseX, int mouseY, InlineArea ia) {
        inlineOptionClicksHandler.handleInlineOptionClicks(mouseX, mouseY, ia);
    }

    private void handleFastHotKeyClicks(int mouseX, int mouseY, int mouseButton) {
        fastHotKeyClicksHandler.handleFastHotKeyClicks(mouseX, mouseY, mouseButton);
    }

    private void handleFastHotKeyTyping(char typedChar, int keyCode) {
        fastHotKeyTypingHandler.handleFastHotKeyTyping(typedChar, keyCode);
    }

    // New: Hotbar Swap typing handler delegates to panel
    private void handleHotbarSwapTyping(char typedChar, int keyCode) {
        fastHotKeyTypingHandler.handleFastHotKeyTyping(typedChar, keyCode);
    }

    // New: Auto Fish typing handler to capture Toggle Hotkey
    private void handleAutoFishTyping(char typedChar, int keyCode) {
        autoFishTypingHandler.handleAutoFishTyping(typedChar, keyCode);
    }

    private void handleMarkLocationTyping(char typedChar, int keyCode) {
        markLocationTypingHandler.handleMarkLocationTyping(typedChar, keyCode);
    }

    private void handleInputFieldEditingState() {
        inputFieldEditingStateHandler.handleInputFieldEditingState();
    }

    private void handleScrollbarClicks(int mouseX, int mouseY) {
        scrollbarClicksHandler.handleScrollbarClicks(mouseX, mouseY);
    }

    private void handleCategoryButtonClicks(int mouseX, int mouseY) {
        categoryButtonClicksHandler.handleCategoryButtonClicks(mouseX, mouseY);
    }

    private void handleModuleButtonClicks(int mouseX, int mouseY) {
        moduleButtonClicksHandler.handleModuleButtonClicks(mouseX, mouseY);
    }

    public void handleModuleButtonClick(ModuleButton moduleBtn, int mouseX, int mouseY) {
        moduleButtonClickHandler.handleModuleButtonClick(moduleBtn, mouseX, mouseY);
    }

    private void handleCommandToggleClicks(int mouseX, int mouseY) {
        commandToggleClicksHandler.handleCommandToggleClicks(mouseX, mouseY);
    }

    public boolean handleDropdownClicks(int mouseX, int mouseY) {
        return dropdownClicksHandler.handleDropdownClicks(mouseX, mouseY);
    }


    public boolean handleLabelledInputClicks(int mouseX, int mouseY) {
        return labelledInputClicksHandler.handleLabelledInputClicks(mouseX, mouseY);
    }

    public boolean handleColorInputClicks(int mouseX, int mouseY) {
        return colorInputClicksHandler.handleColorInputClicks(mouseX, mouseY);
    }

    public boolean handleButtonClicks(int mouseX, int mouseY) {
        return buttonClicksHandler.handleButtonClicks(mouseX, mouseY);
    }

    private void handleScrollbarDrag(int mouseX, int mouseY) {
        scrollbarDragHandler.handleScrollbarDrag(mouseX, mouseY);
    }

    public void handleAllInputTyping(char typedChar, int keyCode) {
        allInputTypingHandler.handleAllInputTyping(typedChar, keyCode);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        mouseInputHandler.handleMouseInput();
    }

    private void buildCategoryButtons() {
        categoryButtonsBuilder.buildCategoryButtons();
    }

    public void buildModuleButtons() {
        moduleButtonsBuilder.buildModuleButtons();
    }

    public boolean hasSettings(ModuleInfo module) {
        if (module == null) return false;
        return module.configGroupIndex != null; // Generic: has a config group => has settings
    }

    public void initializeCommandToggles() {
        commandTogglesinitializer.initializeCommandToggles();
    }

    public void AddEntryAsOption(Map.Entry<String, BaseConfig<?>> entry, Integer y, int ConfigType) {
        PropertyRef ref = new PropertyRef(ConfigType, entry.getKey()); Type type = entry.getValue().type; Object data = entry.getValue().Data;
        int xPos, width; if (optionsInline && !useSidePanelForSelected) { int listX = guiLeft + 120; int listW = Dimensions.GUI_WIDTH - 120 - Dimensions.SCROLLBAR_WIDTH; int boxX = listX + 4; int boxW = listW - 8; int padding = 6; xPos = boxX + padding; width = (listW - 8) - padding * 2; } else { xPos = guiLeft + Dimensions.COMMAND_PANEL_X + 5; width = Dimensions.COMMAND_PANEL_WIDTH - 10; }
        // Titles above inputs for Terminal, FastHotkey, and Auto Fish
        boolean isVerticalAbove = true;
        if (type.equals(String.class)) labelledInputs.add(new LabelledInput(ref, entry.getValue().name, String.valueOf(data), xPos, y, width, 16, isVerticalAbove));
        else if (type.equals(Boolean.class)) {
            // Try to create a special checkbox first
            SpecialCheckbox specialCheckbox = SpecialCheckboxFactory.createSpecialCheckbox(ref, entry.getValue().name, entry.getValue().description, (Boolean) data, xPos, y, width, 16);
            if (specialCheckbox != null) {
                Toggles.add(specialCheckbox);
            } else {
                // Use normal toggle
                Toggles.add(new Toggle(ref, entry.getValue().name, entry.getValue().description, (Boolean) data, xPos, y, width, 16));
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
            labelledInputs.add(new LabelledInput(ref, entry.getValue().name, display, xPos, y, width, 16, isVerticalAbove));
        }
        else if (type.equals(Float.class)) labelledInputs.add(new LabelledInput(ref, entry.getValue().name, String.valueOf(data), xPos, y, width, 16, isVerticalAbove));
        else if (type.equals(DataType_DropDown.class)) { DataType_DropDown dd = (DataType_DropDown) data; methodDropdowns.add(new MethodDropdown(ref, entry.getValue().name, dd.selectedIndex, xPos, y, width, 16, dd.options)); }
        else if (type.equals(Color.class)) ColorInputs.add(new ColorInput(ref, entry.getValue().name, (Color) data, xPos, y, width, 18));
        else System.err.println("Unsupported config type: " + type);
    }

    public void rebuildFastHotkeyRowsForDetail() {
        fastRows.clear(); if (!("Fast Hotkey".equals(SelectedModule != null ? SelectedModule.name : null))) return;
        int detailBaseX = useSidePanelForSelected ? (guiLeft + Dimensions.COMMAND_PANEL_X + Dimensions.COMMAND_PANEL_WIDTH + 6 + 5) : (getInlineDetailX() + 5);
        int detailInputW = 170 - 10;
        for (FastHotkeyEntry e : AllConfig.INSTANCE.FAST_HOTKEY_ENTRIES) fastRows.add(new FastRow(detailBaseX, detailInputW, e));
    }

    private final isFhkKeyDuplicate checkFhkDuplicate = new isFhkKeyDuplicate(this);

    public boolean isFhkKeyDuplicate(int keyCode, int exceptIndex) {
        return checkFhkDuplicate.isFhkKeyDuplicate(keyCode, exceptIndex);
    }

    public void unfocusAllFastInputs() {
        for (FastRow r : fastRows) { r.labelInput.isEditing = false; r.commandInput.isEditing = false; }
        if (fhkPresetNameInput != null) fhkPresetNameInput.isEditing = false;
    }

    // New: unfocus Hotbar Swap inputs -> delegate to panel
    public void unfocusAllHotbarInputs() { hotbarPanel.unfocusAllInputs(); }

    private boolean anyInlineDropdownOpen() {
        for (MethodDropdown dd : methodDropdowns) if (dd.isOpen) return true;
        return false;
    }
}
