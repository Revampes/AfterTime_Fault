package com.aftertime.ratallofyou.UI.config;

import com.aftertime.ratallofyou.UI.UIHighlighter;
import com.aftertime.ratallofyou.UI.config.ConfigData.*;
import com.aftertime.ratallofyou.UI.config.OptionElements.*;
import com.aftertime.ratallofyou.UI.config.commonConstant.Colors;
import com.aftertime.ratallofyou.UI.config.commonConstant.Dimensions;
import com.aftertime.ratallofyou.UI.config.drawMethod.*;
import com.aftertime.ratallofyou.UI.config.handler.*;
import com.aftertime.ratallofyou.UI.utils.InlineArea;
import com.aftertime.ratallofyou.modules.dungeon.terminals.TerminalSettingsApplier;
import com.aftertime.ratallofyou.UI.config.OptionElements.Toggle;
import com.aftertime.ratallofyou.UI.config.ScrollManager;
import com.aftertime.ratallofyou.UI.config.SimpleTextField;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Mouse;
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
        drawBackground();

        // Call super.drawScreen() first so default button rendering happens before our custom drawing
        super.drawScreen(mouseX, mouseY, partialTicks);

        drawCategories();
        drawModules(mouseX, mouseY);
        drawScrollbars();
        drawCommandPanel(mouseX, mouseY);
        // Draw expanded dropdowns last as overlays so they aren't occluded
        drawDropdownOverlays(mouseX, mouseY);

        // Draw tooltips and error messages last (on top of everything)
        drawTooltipsAndErrors(mouseX, mouseY);
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

    private void drawBackground() {
        backgroundDrawer.drawBackground();
    }

    private void drawCategories() {
        categoryDrawer.drawCategories();
    }

    private void drawModules(int mouseX, int mouseY) {
        moduleDrawer.drawModules(mouseX, mouseY);
    }

    private void drawScrollbars() {
        scrollbarDrawer.drawScrollbars();
    }

    private void drawCommandPanel(int mouseX, int mouseY) {
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
    private void drawTooltipsAndErrors(int mouseX, int mouseY) {
        tooltipsAndErrorsDrawer.drawTooltipsAndErrors(mouseX, mouseY);
    }

    public void drawTooltip(String text, int mouseX, int mouseY) {
        tooltipDrawer.drawTooltip(text, mouseX, mouseY);
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

    private int computeInlineContentHeight() {
        if (SelectedModule != null && "Fast Hotkey".equals(SelectedModule.name)) {
            int h = 12 + 22; // create
            int rowH = 16; int gap = 4; h += 12 + (AllConfig.INSTANCE.FHK_PRESETS.size() * (rowH + gap));
            h += 6; // separator
            h += Toggles.size() * 22; for (LabelledInput li : labelledInputs) h += li.getVerticalSpace(); h += ColorInputs.size() * 50; h += methodDropdowns.size() * 22; return h + 6;
        }
        // New: Hotbar Swap rows height delegated to panel
        if (SelectedModule != null && "Hotbar Swap".equals(SelectedModule.name)) {
            // compute inline content width (same as AddEntryAsOption inline branch)
            int listW = Dimensions.GUI_WIDTH - 120 - Dimensions.SCROLLBAR_WIDTH; int contentW = (listW - 8) - 6 * 2;
            int h = hotbarPanel.computeSectionHeight(contentW);
            // plus generic options (toggles/inputs)
            h += Toggles.size() * 22; for (LabelledInput li : labelledInputs) h += li.getVerticalSpace(); h += ColorInputs.size() * 50; h += methodDropdowns.size() * 22;
            return h + 6;
        }
        int h = Toggles.size() * 22; for (LabelledInput li : labelledInputs) h += li.getVerticalSpace(); h += ColorInputs.size() * 50; h += methodDropdowns.size() * 22; return h + 6;
    }

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

    // Build UI lists
    private void buildCategoryButtons() {
        categoryButtons.clear();
        // Don't clear buttonList here or add category buttons to it - we want custom rendering only
        int y = guiTop + 30; int x = guiLeft + 10;
        for (int i = 0; i < AllConfig.INSTANCE.Categories.size(); i++) {
            GuiButton b = new GuiButton(1000 + i, x, y, 95, 18, AllConfig.INSTANCE.Categories.get(i));
            categoryButtons.add(b);
            // Don't add to buttonList to prevent default texture rendering
            y += 20;
        }
    }

    public void buildModuleButtons() {
        moduleButtons.clear(); int listX = guiLeft + 120; int listY = guiTop + 28; int listW = Dimensions.GUI_WIDTH - 120 - 10 - Dimensions.SCROLLBAR_WIDTH; int y = listY - mainScroll.getOffset(); int rowH = 20; int usedHeight = 0;
        for (BaseConfig<?> mi : AllConfig.INSTANCE.MODULES.values()) {
            ModuleInfo info = (ModuleInfo) mi; if (!info.category.equals(selectedCategory)) continue;
            boolean hasSettings = hasSettings(info); moduleButtons.add(new ModuleButton(listX + 4, y, listW - 8, rowH - 2, info, hasSettings));
            int inc = rowH; if (showCommandSettings && optionsInline && SelectedModule == info) { inc += 20 + computeInlineContentHeight() + 8; }
            y += inc; usedHeight += inc;
        }
        int totalHeight = usedHeight; int viewH = Dimensions.GUI_HEIGHT - 70; mainScroll.update(totalHeight, viewH); mainScroll.updateScrollbarPosition(listX + listW - 2, listY, viewH);
    }

    private boolean hasSettings(ModuleInfo module) {
        if (module == null || module.name == null) return false;
        switch (module.name) {
            case "Dungeon Terminals":
            case "Party Commands":
            case "No Debuff":
            case "Etherwarp Overlay":
            case "Fast Hotkey":
            case "Chest Open Notice":
            case "Hotbar Swap":
            case "Auto Fish":
            case "Auto Sell":
            case "Auto Experiment":
            case "NameTag":
            case "Player ESP":
            case "Custom Cape":
            case "DarkMode":
            case "Mark Location":
                return true;
            default:
                return false;
        }
    }

    public void initializeCommandToggles() {
        Toggles.clear(); labelledInputs.clear(); methodDropdowns.clear(); ColorInputs.clear(); if (SelectedModule == null) return;
        Integer y = guiTop + Dimensions.COMMAND_PANEL_Y + 30;
        switch (SelectedModule.name) {
            case "Dungeon Terminals": Add_SubSetting_Terminal(y); break;
            case "Party Commands": Add_SubSetting_Command(y); break;
            case "No Debuff": Add_SubSetting_NoDebuff(y); break;
            case "Etherwarp Overlay": Add_SubSetting_Etherwarp(y); break;
            case "Fast Hotkey": Add_SubSetting_FastHotkey(y); break;
            case "Chest Open Notice": Add_SubSetting_ChestOpen(y); break;
            case "Hotbar Swap": Add_SubSetting_HotbarSwap(y); hotbarPanel.rebuildRows(); break;
            case "Auto Fish": Add_SubSetting_AutoFish(y); break;
            case "Auto Sell": Add_SubSetting_AutoSell(y); break;
            case "Auto Experiment": Add_SubSetting_AutoExperiment(y); break;
            case "NameTag": Add_SubSetting_NameTag(y); break; // New
            case "Player ESP": Add_SubSetting_PlayerESP(y); break; //
            case "Custom Cape": Add_SubSetting_CustomCape(y); break;
            case "DarkMode": Add_SubSetting_DarkMode(y); break;
            case "Mark Location": Add_SubSetting_MarkLocation(y); break;
        }
        int contentHeight = 0; if (useSidePanelForSelected && "Fast Hotkey".equals(SelectedModule.name)) contentHeight += 12 + 22 + 12 + (AllConfig.INSTANCE.FHK_PRESETS.size() * (16 + 4));
        contentHeight += Toggles.size() * 22; for (LabelledInput li : labelledInputs) contentHeight += li.getVerticalSpace(); contentHeight += ColorInputs.size() * 50; contentHeight += methodDropdowns.size() * 22;
        int panelViewHeight = Dimensions.GUI_HEIGHT - 60 - 25;
        // Only update commandScroll position/size for side panel mode; inline mode updates in draw methods as needed
        if (useSidePanelForSelected) {
            commandScroll.update(contentHeight, panelViewHeight); commandScroll.updateScrollbarPosition(guiLeft + Dimensions.COMMAND_PANEL_X + Dimensions.COMMAND_PANEL_WIDTH - Dimensions.SCROLLBAR_WIDTH - 2, guiTop + Dimensions.COMMAND_PANEL_Y + 25, panelViewHeight);
        }
    }

    // New: NameTag sub-settings (index 13 in AllConfig.ALLCONFIGS)
    public void Add_SubSetting_NameTag(Integer y) {
        for (java.util.Map.Entry<String, com.aftertime.ratallofyou.UI.config.ConfigData.BaseConfig<?>> e : com.aftertime.ratallofyou.UI.config.ConfigData.AllConfig.INSTANCE.NAMETAG_CONFIGS.entrySet()) {
            AddEntryAsOption(e, y, 13);
        }
    }

    // New: Player ESP sub-settings (index 12 in AllConfig.ALLCONFIGS)
    public void Add_SubSetting_PlayerESP(Integer y) {
        for (java.util.Map.Entry<String, com.aftertime.ratallofyou.UI.config.ConfigData.BaseConfig<?>> e : com.aftertime.ratallofyou.UI.config.ConfigData.AllConfig.INSTANCE.PLAYERESP_CONFIGS.entrySet()) {
            AddEntryAsOption(e, y, 12);
        }
    }

    public void Add_SubSetting_CustomCape(Integer y) {
        for (java.util.Map.Entry<String, com.aftertime.ratallofyou.UI.config.ConfigData.BaseConfig<?>> e : AllConfig.INSTANCE.CUSTOMCAPE_CONFIGS.entrySet()) {
            AddEntryAsOption(e, y, 14);
        }
    }

    public void Add_SubSetting_DarkMode(Integer y) {
        for (java.util.Map.Entry<String, com.aftertime.ratallofyou.UI.config.ConfigData.BaseConfig<?>> e : AllConfig.INSTANCE.DARKMODE_CONFIGS.entrySet()) {
            AddEntryAsOption(e, y, 16);
        }
    }

    private void AddEntryAsOption(Map.Entry<String, BaseConfig<?>> entry, Integer y, int ConfigType) {
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

    private void Add_SubSetting_Terminal(Integer y) {
        for (Map.Entry<String, BaseConfig<?>> e : AllConfig.INSTANCE.TERMINAL_CONFIGS.entrySet()) {
            AddEntryAsOption(e, y, 4);
        }
    }

    private void addTerminalEntry(String key, Integer y) { BaseConfig<?> cfg = AllConfig.INSTANCE.TERMINAL_CONFIGS.get(key); if (cfg == null) return; AddEntryAsOption(new java.util.AbstractMap.SimpleEntry<>(key, cfg), y, 4); }

    public void Add_SubSetting_FastHotkey(Integer y) {
        for (Map.Entry<String, BaseConfig<?>> e : AllConfig.INSTANCE.FASTHOTKEY_CONFIGS.entrySet()) {
            AddEntryAsOption(e, y, 6);
        }
    }

    public void Add_SubSetting_Command(Integer y) { for (Map.Entry<String, BaseConfig<?>> e : AllConfig.INSTANCE.COMMAND_CONFIGS.entrySet()) AddEntryAsOption(e, y, 0); }
    public void Add_SubSetting_NoDebuff(Integer y) { for (Map.Entry<String, BaseConfig<?>> e : AllConfig.INSTANCE.NODEBUFF_CONFIGS.entrySet()) AddEntryAsOption(e, y, 2); }
    public void Add_SubSetting_Etherwarp(Integer y) { for (Map.Entry<String, BaseConfig<?>> e : AllConfig.INSTANCE.ETHERWARP_CONFIGS.entrySet()) AddEntryAsOption(e, y, 3); }

    // New: Chest Open Notice sub-settings (index 7 in AllConfig.ALLCONFIGS)
    public void Add_SubSetting_ChestOpen(Integer y) {
        for (Map.Entry<String, BaseConfig<?>> e : AllConfig.INSTANCE.KUUDRA_CHESTOPEN_CONFIGS.entrySet()) {
            AddEntryAsOption(e, y, 7);
        }
    }

    // New: Hotbar Swap sub-settings (index 8 in AllConfig.ALLCONFIGS)
    public void Add_SubSetting_HotbarSwap(Integer y) {
        for (Map.Entry<String, BaseConfig<?>> e : AllConfig.INSTANCE.HOTBARSWAP_CONFIGS.entrySet()) {
            AddEntryAsOption(e, y, 8);
        }
    }

    // Restore: Auto Fish sub-settings (index 10)
    public void Add_SubSetting_AutoFish(Integer y) {
        for (Map.Entry<String, BaseConfig<?>> e : AllConfig.INSTANCE.AUTOFISH_CONFIGS.entrySet()) {
            AddEntryAsOption(e, y, 10);
        }
    }

    // Restore: Auto Sell sub-settings (index 11)
    public void Add_SubSetting_AutoSell(Integer y) {
        for (Map.Entry<String, BaseConfig<?>> e : AllConfig.INSTANCE.AUTOSELL_CONFIGS.entrySet()) {
            AddEntryAsOption(e, y, 11);
        }
    }

    // Restore: Auto Experiment sub-settings (index 12)
    public void Add_SubSetting_AutoExperiment(Integer y) {
        for (Map.Entry<String, BaseConfig<?>> e : AllConfig.INSTANCE.AUTOEXPERIMENT_CONFIGS.entrySet()) {
            AddEntryAsOption(e, y, 12);
        }
    }

    public void Add_SubSetting_MarkLocation(Integer y) {
        for (Map.Entry<String, BaseConfig<?>> e : AllConfig.INSTANCE.MARKLOCATION_CONFIGS.entrySet()) {
            AddEntryAsOption(e, y, 17);
        }
    }

    public void rebuildFastHotkeyRowsForDetail() {
        fastRows.clear(); if (!("Fast Hotkey".equals(SelectedModule != null ? SelectedModule.name : null))) return;
        int detailBaseX = useSidePanelForSelected ? (guiLeft + Dimensions.COMMAND_PANEL_X + Dimensions.COMMAND_PANEL_WIDTH + 6 + 5) : (getInlineDetailX() + 5);
        int detailInputW = 170 - 10;
        for (FastHotkeyEntry e : AllConfig.INSTANCE.FAST_HOTKEY_ENTRIES) fastRows.add(new FastRow(detailBaseX, detailInputW, e));
    }

    public boolean isFhkKeyDuplicate(int keyCode, int exceptIndex) {
        if (keyCode <= 0) return false;
        List<FastHotkeyPreset> list = AllConfig.INSTANCE.FHK_PRESETS;
        for (int i = 0; i < list.size(); i++) {
            if (i == exceptIndex) continue;
            FastHotkeyPreset p = list.get(i);
            if (p.keyCode == keyCode) return true;
        }
        return false;
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

    // Draws open dropdowns on top for both side panel and inline modes
    private void drawDropdownOverlays(int mouseX, int mouseY) {
        if (!showCommandSettings || SelectedModule == null) return;
        net.minecraft.client.gui.FontRenderer fr = getFontRendererObj();
        // Side panel overlays: compute baseline same as panel
        if (useSidePanelForSelected && !optionsInline) {
            int y = guiTop + Dimensions.COMMAND_PANEL_Y + 30 - commandScroll.getOffset();
            for (Toggle ignored : Toggles) y += 22;
            for (LabelledInput li : labelledInputs) y += li.getVerticalSpace();
            for (ColorInput ignored : ColorInputs) y += 50;
            for (MethodDropdown dd : methodDropdowns) {
                if (dd.isOpen) dd.drawExpandedOptions(mouseX, mouseY, y, fr);
                y += 22;
            }
        }
        // Inline overlays: use the recorded baseline to ensure exact alignment
        if (optionsInline) {
            int baseY = inlineDropdownBaseY;
            if (baseY >= 0) {
                int y = baseY;
                for (MethodDropdown dd : methodDropdowns) {
                    if (dd.isOpen) dd.drawExpandedOptions(mouseX, mouseY, y, fr);
                    y += 22;
                }
            }
        }
    }
}
