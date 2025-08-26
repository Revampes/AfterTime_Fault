package com.aftertime.ratallofyou.UI.config;

import com.aftertime.ratallofyou.UI.UIHighlighter;
import com.aftertime.ratallofyou.UI.config.ConfigData.*;
import com.aftertime.ratallofyou.UI.config.OptionElements.*;
import com.aftertime.ratallofyou.UI.config.commonConstant.Colors;
import com.aftertime.ratallofyou.UI.config.commonConstant.Dimensions;
import com.aftertime.ratallofyou.modules.dungeon.terminals.TerminalSettingsApplier;
import com.aftertime.ratallofyou.UI.config.OptionElements.Toggle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.Gui;
import org.lwjgl.input.Mouse;
import org.lwjgl.input.Keyboard; // Added for key code names and capture

import java.awt.*;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.lwjgl.opengl.GL11.*;

public class ModSettingsGui extends GuiScreen {
    // Fields
    private final List<GuiButton> categoryButtons = new ArrayList<>();
    private final List<ModuleButton> moduleButtons = new ArrayList<>();
    private final List<ColorInput> ColorInputs = new ArrayList<>();
    private final List<LabelledInput> labelledInputs = new ArrayList<>();
    private final List<MethodDropdown> methodDropdowns = new ArrayList<>();
    private final List<Toggle> Toggles = new ArrayList<>();

    private final ScrollManager mainScroll = new ScrollManager();
    private final ScrollManager commandScroll = new ScrollManager();

    // Fast Hotkey editor rows (right-side detail panel)
    private final List<FastRow> fastRows = new ArrayList<>();

    // New: Hotbar Swap inline rows
    private final List<HotbarPresetRow> hotbarRows = new ArrayList<>();

    private String selectedCategory = "Kuudra";
    private ModuleInfo SelectedModule = null;
    private boolean showCommandSettings = false;
    private int guiLeft, guiTop;

    // Layout modes
    private boolean useSidePanelForSelected = false; // Fast Hotkey only
    private boolean optionsInline = false; // inline box below module row

    // Fast Hotkey state (left panel preset list + input)
    private SimpleTextField fhkPresetNameInput = null;
    private int fhkSelectedPreset = -1; // if >=0, detail panel open
    // Fast Hotkey inline key-capture index
    private int fhkKeyCaptureIndex = -1;

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
        this.hotbarRows.clear();

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

        // Inline box consumes inner clicks
        if (showCommandSettings && optionsInline && SelectedModule != null) {
            InlineArea ia = getInlineAreaForSelected();
            if (ia != null && mouseX >= ia.contentX && mouseX <= ia.contentX + ia.contentW && mouseY >= ia.boxY && mouseY <= ia.boxY + ia.boxH) {
                handleInlineOptionClicks(mouseX, mouseY, ia);
                return;
            }
        }

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
        handleAllInputTyping(typedChar, keyCode);
    }

    // Drawing basics
    private void drawBackground() {
        drawRect(guiLeft, guiTop, guiLeft + Dimensions.GUI_WIDTH, guiTop + Dimensions.GUI_HEIGHT, Colors.PANEL);
        fontRendererObj.drawStringWithShadow("§l§nRat All Of You", guiLeft + 15, guiTop + 10, Colors.TEXT);
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
            if (showCommandSettings && optionsInline && SelectedModule != null && moduleBtn.getModule() == SelectedModule) {
                drawInlineSettingsBox(mouseX, mouseY);
            }
        }
        glDisable(GL_SCISSOR_TEST);
    }

    private void drawScrollbars() {
        if (mainScroll.shouldRenderScrollbar()) mainScroll.drawScrollbar(Colors.SCROLLBAR, Colors.SCROLLBAR_HANDLE);
        if (showCommandSettings && useSidePanelForSelected && commandScroll.shouldRenderScrollbar()) {
            commandScroll.drawScrollbar(Colors.COMMAND_SCROLLBAR, Colors.COMMAND_SCROLLBAR_HANDLE);
        }
        // Inline Fast Hotkey: draw scrollbar for right detail panel when open
        if (showCommandSettings && optionsInline && SelectedModule != null && "Fast Hotkey".equals(SelectedModule.name) && fhkSelectedPreset >= 0 && commandScroll.shouldRenderScrollbar()) {
            commandScroll.drawScrollbar(Colors.COMMAND_SCROLLBAR, Colors.COMMAND_SCROLLBAR_HANDLE);
        }
    }

    private void drawCommandPanel(int mouseX, int mouseY) {
        // If not using side panel, still allow right-only detail panel for Fast Hotkey
        if (!showCommandSettings || SelectedModule == null) return;
        if (optionsInline && "Fast Hotkey".equals(SelectedModule.name)) {
            if (fhkSelectedPreset >= 0) {
                int panelX = guiLeft + Dimensions.COMMAND_PANEL_X;
                int panelY = guiTop + Dimensions.COMMAND_PANEL_Y;
                int panelWidth = Dimensions.COMMAND_PANEL_WIDTH;
                int panelHeight = Dimensions.GUI_HEIGHT - 60;
                // Only draw the right detail panel (no left panel)
                drawFastHotkeyDetailPanel(mouseX, mouseY, panelX, panelY, panelWidth, panelHeight);
            }
            return;
        }
        if (!useSidePanelForSelected) return;
        int panelX = guiLeft + Dimensions.COMMAND_PANEL_X;
        int panelY = guiTop + Dimensions.COMMAND_PANEL_Y;
        int panelWidth = Dimensions.COMMAND_PANEL_WIDTH;
        int panelHeight = Dimensions.GUI_HEIGHT - 60;
        drawRect(panelX, panelY, panelX + panelWidth, panelY + panelHeight, Colors.COMMAND_PANEL);
        drawRect(panelX - 1, panelY - 1, panelX + panelWidth + 1, panelY + panelHeight + 1, Colors.COMMAND_BORDER);
        drawCenteredString(fontRendererObj, getCommandPanelTitle(), panelX + panelWidth / 2, panelY + 5, Colors.COMMAND_TEXT);
        if ("Fast Hotkey".equals(SelectedModule.name)) {
            drawFastHotKeyPanel(mouseX, mouseY, panelX, panelY, panelWidth, panelHeight);
            if (fhkSelectedPreset >= 0) drawFastHotkeyDetailPanel(mouseX, mouseY, panelX, panelY, panelWidth, panelHeight);
            return;
        }
        drawCommandPanelContent(mouseX, mouseY, panelX, panelY, panelWidth, panelHeight);
    }

    // Inline settings helpers
    private static class InlineArea { int boxX, boxY, boxW, boxH, contentX, contentY, contentW; }

    private int getInlineDetailX() {
        InlineArea ia = getInlineAreaForSelected();
        if (ia != null) return ia.boxX + ia.boxW + 6; // small gap after inline box
        return guiLeft + Dimensions.COMMAND_PANEL_X; // fallback
    }

    private InlineArea getInlineAreaForSelected() {
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

    private void drawInlineSettingsBox(int mouseX, int mouseY) {
        InlineArea ia = getInlineAreaForSelected(); if (ia == null) return;
        drawRect(ia.boxX, ia.boxY, ia.boxX + ia.boxW, ia.boxY + ia.boxH, Colors.COMMAND_PANEL);
        drawRect(ia.boxX - 1, ia.boxY - 1, ia.boxX + ia.boxW + 1, ia.boxY + ia.boxH + 1, Colors.COMMAND_BORDER);
        drawCenteredString(fontRendererObj, SelectedModule.name + " Settings", ia.boxX + ia.boxW / 2, ia.boxY + 6, Colors.COMMAND_TEXT);
        int y = ia.contentY;
        if ("Fast Hotkey".equals(SelectedModule.name)) {
            // Inline fast hotkey: presets list + appearance
            fontRendererObj.drawStringWithShadow("Create settings:", ia.contentX, y, Colors.COMMAND_TEXT); y += 12;
            if (fhkPresetNameInput != null) {
                fhkPresetNameInput.setBounds(ia.contentX, y, Math.max(60, ia.contentW - 65), 16);
                fhkPresetNameInput.draw(mouseX, mouseY);
                int btnX = ia.contentX + ia.contentW - 60;
                drawRect(btnX, y, btnX + 60, y + 16, Colors.BUTTON_GREEN);
                drawCenteredString(fontRendererObj, "Confirm", btnX + 30, y + 4, Colors.BUTTON_TEXT);
                y += 22;
            }
            fontRendererObj.drawStringWithShadow("Saved settings:", ia.contentX, y, Colors.COMMAND_TEXT); y += 12;
            int rowH = 16; int gap = 4;
            for (int i = 0; i < AllConfig.INSTANCE.FHK_PRESETS.size(); i++) {
                FastHotkeyPreset p = AllConfig.INSTANCE.FHK_PRESETS.get(i);
                int x = ia.contentX; int w = ia.contentW; int h = rowH; int rowY = y;
                // Toggle 14x14
                int tSize = 14; int toggleX = x; int toggleY = rowY + (h - tSize) / 2;
                int toggleColor = p.enabled ? Colors.BUTTON_GREEN : Colors.BUTTON_RED;
                drawRect(toggleX, toggleY, toggleX + tSize, toggleY + tSize, toggleColor);
                // Name area clickable to select preset
                int nameX = toggleX + tSize + 6; int nameW = Math.max(40, w - 6 - tSize - 60 - 80 - 6); // leave space for remove(60) + key(80) + gaps
                int nameCenterY = rowY + 4;
                String nm = p.name + (i == AllConfig.INSTANCE.FHK_ACTIVE_PRESET ? "  (Active)" : "");
                fontRendererObj.drawStringWithShadow(nm, nameX, nameCenterY, Colors.COMMAND_TEXT);
                // Keybind box 80px
                int keyW = 80; int keyX = x + w - 60 - 6 - keyW; int keyY = rowY;
                drawRect(keyX, keyY, keyX + keyW, keyY + h, Colors.INPUT_BG);
                String keyLabel;
                if (fhkKeyCaptureIndex == i) keyLabel = "Press a key...";
                else keyLabel = p.keyCode <= 0 ? "Unbound" : Keyboard.getKeyName(p.keyCode);
                if (keyLabel == null || keyLabel.trim().isEmpty()) keyLabel = "Unknown";
                fontRendererObj.drawStringWithShadow(keyLabel, keyX + 4, keyY + 4, Colors.INPUT_FG);
                // Remove button 60px
                int rmW = 60; int rmX = x + w - rmW; int rmY = rowY;
                drawRect(rmX, rmY, rmX + rmW, rmY + h, Colors.BUTTON_RED);
                drawCenteredString(fontRendererObj, "Remove", rmX + rmW / 2, rmY + 4, Colors.BUTTON_TEXT);
                y += h + gap;
            }
            drawRect(ia.contentX, y, ia.contentX + ia.contentW, y + 1, 0x33000000); y += 6;
        }
        // New: Hotbar Swap inline drawing
        if ("Hotbar Swap".equals(SelectedModule.name)) {
            // Render each preset row
            for (int i = 0; i < hotbarRows.size(); i++) {
                HotbarPresetRow row = hotbarRows.get(i);
                fontRendererObj.drawStringWithShadow("Preset Label:", ia.contentX, y, Colors.COMMAND_TEXT);
                row.labelInput.setBounds(ia.contentX, y + 12, ia.contentW, 16);
                row.labelInput.draw(mouseX, mouseY);
                y += 12 + 16 + 6;
                fontRendererObj.drawStringWithShadow("Preset Trigger Command:", ia.contentX, y, Colors.COMMAND_TEXT);
                row.triggerInput.setBounds(ia.contentX, y + 12, ia.contentW, 16);
                row.triggerInput.draw(mouseX, mouseY);
                y += 12 + 16 + 6;
                int rmW = 70; int rmH = 16; int rmX = ia.contentX; int rmY = y;
                drawRect(rmX, rmY, rmX + rmW, rmY + rmH, Colors.BUTTON_RED);
                drawCenteredString(fontRendererObj, "Remove", rmX + rmW / 2, rmY + 4, Colors.BUTTON_TEXT);
                row.removeBtnX = rmX; row.removeBtnY = rmY; row.removeBtnW = rmW; row.removeBtnH = rmH;
                y += rmH + 8;
                drawRect(ia.contentX, y, ia.contentX + ia.contentW, y + 1, 0x22000000); y += 6;
            }
            // Add button at bottom
            int addW = 90; int addH = 16; int addX = ia.contentX; int addY = y;
            drawRect(addX, addY, addX + addW, addY + addH, Colors.BUTTON_GREEN);
            drawCenteredString(fontRendererObj, hotbarRows.isEmpty() ? "Add Preset" : "Add Another", addX + addW / 2, addY + 4, Colors.BUTTON_TEXT);
            // store last add button bounds for click handling
            lastHotbarAddBtnX = addX; lastHotbarAddBtnY = addY; lastHotbarAddBtnW = addW; lastHotbarAddBtnH = addH;
            y += addH + 6;
            // small separator before generic options
            drawRect(ia.contentX, y, ia.contentX + ia.contentW, y + 1, 0x33000000); y += 6;
        }
        for (Toggle t : Toggles) { t.draw(mouseX, mouseY, y, fontRendererObj); y += 22; }
        for (LabelledInput t : labelledInputs) { t.draw(mouseX, mouseY, y, fontRendererObj); y += t.getVerticalSpace(); }
        for (ColorInput t : ColorInputs) { t.draw(mouseX, mouseY, y, fontRendererObj); y += 50; }
        for (MethodDropdown t : methodDropdowns) { t.draw(mouseX, mouseY, y, fontRendererObj); y += 22; }
    }

    private int computeInlineContentHeight() {
        if (SelectedModule != null && "Fast Hotkey".equals(SelectedModule.name)) {
            int h = 12 + 22; // create
            int rowH = 16; int gap = 4; h += 12 + (AllConfig.INSTANCE.FHK_PRESETS.size() * (rowH + gap));
            h += 6; // separator
            h += Toggles.size() * 22; for (LabelledInput li : labelledInputs) h += li.getVerticalSpace(); h += ColorInputs.size() * 50; h += methodDropdowns.size() * 22; return h + 6;
        }
        // New: Hotbar Swap rows height
        if (SelectedModule != null && "Hotbar Swap".equals(SelectedModule.name)) {
            int perRow = (12 + 16 + 6) + (12 + 16 + 6) + (16 + 8) + 6; // label+input, trigger+input, remove, separator
            int h = perRow * hotbarRows.size();
            h += 16 + 6; // add button and gap
            // plus generic options (toggles/inputs)
            h += Toggles.size() * 22; for (LabelledInput li : labelledInputs) h += li.getVerticalSpace(); h += ColorInputs.size() * 50; h += methodDropdowns.size() * 22;
            return h + 6;
        }
        int h = Toggles.size() * 22; for (LabelledInput li : labelledInputs) h += li.getVerticalSpace(); h += ColorInputs.size() * 50; h += methodDropdowns.size() * 22; return h + 6;
    }

    private void handleInlineOptionClicks(int mouseX, int mouseY, InlineArea ia) {
        // Focus inputs and toggle clicks (inline)
        int y = ia.contentY;
        if (SelectedModule != null && "Fast Hotkey".equals(SelectedModule.name)) {
            int rowH = 16; int gap = 4;
            y += 12; // after label
            if (fhkPresetNameInput != null) {
                fhkPresetNameInput.setBounds(ia.contentX, y, Math.max(60, ia.contentW - 65), 16);
                if (fhkPresetNameInput.isMouseOver(mouseX, mouseY)) { fhkPresetNameInput.beginEditing(mouseX); return; }
                int btnX = ia.contentX + ia.contentW - 60; if (mouseX >= btnX && mouseX <= btnX + 60 && mouseY >= y && mouseY <= y + 16) {
                    String name = fhkPresetNameInput.text.trim(); if (!name.isEmpty()) {
                        boolean exists = false; for (FastHotkeyPreset p : AllConfig.INSTANCE.FHK_PRESETS) { if (p.name.equalsIgnoreCase(name)) { exists = true; break; } }
                        if (!exists) { AllConfig.INSTANCE.FHK_PRESETS.add(new FastHotkeyPreset(name)); AllConfig.INSTANCE.setActiveFhkPreset(AllConfig.INSTANCE.FHK_PRESETS.size() - 1); fhkSelectedPreset = AllConfig.INSTANCE.FHK_ACTIVE_PRESET; fhkPresetNameInput.text = ""; ConfigIO.INSTANCE.SaveFastHotKeyPresets(AllConfig.INSTANCE.FHK_PRESETS, AllConfig.INSTANCE.FHK_ACTIVE_PRESET); rebuildFastHotkeyRowsForDetail(); }
                    }
                    return;
                }
                y += 22;
            }
            y += 12; // saved header
            for (int i = 0; i < AllConfig.INSTANCE.FHK_PRESETS.size(); i++) {
                FastHotkeyPreset p = AllConfig.INSTANCE.FHK_PRESETS.get(i);
                int x = ia.contentX; int w = ia.contentW; int rowY = y; // capture for this row
                // Toggle box
                int tSize = 14; int toggleX = x; int toggleY = rowY + (rowH - tSize) / 2;
                boolean overToggle = mouseX >= toggleX && mouseX <= toggleX + tSize && mouseY >= toggleY && mouseY <= toggleY + tSize;
                // Key box
                int keyW = 80; int keyX = x + w - 60 - 6 - keyW; int keyY = rowY;
                boolean overKey = mouseX >= keyX && mouseX <= keyX + keyW && mouseY >= keyY && mouseY <= keyY + rowH;
                // Remove
                int rmW = 60; int rmX = x + w - rmW; int rmY = rowY; boolean overRemove = mouseX >= rmX && mouseX <= rmX + rmW && mouseY >= rmY && mouseY <= rmY + rowH;
                // Name/select area
                int nameX = toggleX + tSize + 6; int nameW = Math.max(40, w - 6 - tSize - 60 - 80 - 6);
                boolean overName = mouseX >= nameX && mouseX <= nameX + nameW && mouseY >= rowY && mouseY <= rowY + rowH;

                if (overToggle) {
                    // Enforce: must have a valid, non-duplicate key to enable
                    if (!p.enabled) {
                        if (p.keyCode <= 0) { fhkKeyCaptureIndex = i; return; }
                        if (isFhkKeyDuplicate(p.keyCode, i)) { return; }
                        p.enabled = true; ConfigIO.INSTANCE.SaveFastHotKeyPresets(AllConfig.INSTANCE.FHK_PRESETS, AllConfig.INSTANCE.FHK_ACTIVE_PRESET);
                    } else {
                        p.enabled = false; ConfigIO.INSTANCE.SaveFastHotKeyPresets(AllConfig.INSTANCE.FHK_PRESETS, AllConfig.INSTANCE.FHK_ACTIVE_PRESET);
                    }
                    // Also select this preset for editing
                    AllConfig.INSTANCE.setActiveFhkPreset(i); fhkSelectedPreset = i; rebuildFastHotkeyRowsForDetail();
                    return;
                }
                if (overKey) { fhkKeyCaptureIndex = i; return; }
                if (overRemove) {
                    if (AllConfig.INSTANCE.FHK_PRESETS.size() > 1) {
                        AllConfig.INSTANCE.FHK_PRESETS.remove(i);
                        int newActive = Math.max(0, Math.min(AllConfig.INSTANCE.FHK_ACTIVE_PRESET - (i <= AllConfig.INSTANCE.FHK_ACTIVE_PRESET ? 1 : 0), AllConfig.INSTANCE.FHK_PRESETS.size() - 1));
                        AllConfig.INSTANCE.setActiveFhkPreset(newActive);
                        fhkSelectedPreset = -1; fastRows.clear();
                        ConfigIO.INSTANCE.SaveFastHotKeyPresets(AllConfig.INSTANCE.FHK_PRESETS, AllConfig.INSTANCE.FHK_ACTIVE_PRESET);
                    }
                    return;
                }
                if (overName) { AllConfig.INSTANCE.setActiveFhkPreset(i); fhkSelectedPreset = i; rebuildFastHotkeyRowsForDetail(); return; }
                y += rowH + gap; // advance to next row baseline
            }
            y += 6; // separator gap
        }
        // New: Hotbar Swap inline clicks
        if (SelectedModule != null && "Hotbar Swap".equals(SelectedModule.name)) {
            // Iterate rows for input focus and remove
            for (int i = 0; i < hotbarRows.size(); i++) {
                HotbarPresetRow row = hotbarRows.get(i);
                // Label input
                int labelY = y + 12; // after title
                row.labelInput.setBounds(ia.contentX, labelY, ia.contentW, 16);
                if (row.labelInput.isMouseOver(mouseX, mouseY)) { unfocusAllHotbarInputs(); row.labelInput.beginEditing(mouseX); return; }
                y += 12 + 16 + 6;
                // Trigger input
                int trigY = y + 12;
                row.triggerInput.setBounds(ia.contentX, trigY, ia.contentW, 16);
                if (row.triggerInput.isMouseOver(mouseX, mouseY)) { unfocusAllHotbarInputs(); row.triggerInput.beginEditing(mouseX); return; }
                y += 12 + 16 + 6;
                // Remove button
                int rmX = ia.contentX; int rmY = y; int rmW = 70; int rmH = 16;
                if (mouseX >= rmX && mouseX <= rmX + rmW && mouseY >= rmY && mouseY <= rmY + rmH) { removeHotbarPresetAtIndex(i); return; }
                y += rmH + 8;
                y += 6; // separator gap
            }
            // Add button at bottom
            int addX = ia.contentX; int addY = y; int addW = 90; int addH = 16;
            if (mouseX >= addX && mouseX <= addX + addW && mouseY >= addY && mouseY <= addY + addH) { addCurrentHotbarAsPreset(); return; }
            // Continue to other inputs after a small gap
            y += addH + 12;
        }
        // Inputs and toggles
        int yToggle = y;
        for (Toggle toggle : Toggles) { if (toggle.isMouseOver(mouseX, mouseY, yToggle)) { toggle.toggle(); if (toggle.ref != null && toggle.ref.ConfigType == 4) TerminalSettingsApplier.applyFromAllConfig(); return; } yToggle += 22; }
        int yLI = y;
        for (Toggle ignored : Toggles) yLI += 22;
        for (LabelledInput li : labelledInputs) { if (li.isMouseOver(mouseX, mouseY, yLI)) { for (LabelledInput other : labelledInputs) other.isEditing = false; li.beginEditing(mouseX); return; } yLI += li.getVerticalSpace(); }
        int yCI = y; for (Toggle ignored : Toggles) yCI += 22; for (LabelledInput li : labelledInputs) yCI += li.getVerticalSpace();
        for (ColorInput ci : ColorInputs) { int inputY = yCI + ci.height + 8; boolean hover = (mouseX >= ci.x + 40 && mouseX <= ci.x + ci.width && mouseY >= inputY - 2 && mouseY <= inputY + 15); if (hover) { ci.beginEditing(mouseX); return; } yCI += 50; }
        int yd = y; for (Toggle ignored : Toggles) yd += 22; for (LabelledInput li : labelledInputs) yd += li.getVerticalSpace(); for (ColorInput ignored : ColorInputs) yd += 50;
        for (MethodDropdown dd : methodDropdowns) { int bx = dd.x + 100; int bw = dd.width - 100; int bh = dd.height; boolean inBase = mouseX >= bx && mouseX <= bx + bw && mouseY >= yd && mouseY <= yd + bh; if (inBase) { for (MethodDropdown other : methodDropdowns) other.isOpen = false; dd.isOpen = !dd.isOpen; return; } if (dd.isOpen) { for (int i = 0; i < dd.methods.length; i++) { int optionY = yd + bh + (i * bh); boolean inOpt = mouseX >= bx && mouseX <= bx + bw && mouseY >= optionY && mouseY <= optionY + bh; if (inOpt) { dd.selectMethod(i); dd.isOpen = false; return; } } } yd += 22; }
    }

    // Fast Hotkey left panel
    private void drawFastHotKeyPanel(int mouseX, int mouseY, int panelX, int panelY, int panelWidth, int panelHeight) {
        int scale = new ScaledResolution(Minecraft.getMinecraft()).getScaleFactor();
        glEnable(GL_SCISSOR_TEST);
        glScissor(panelX * scale, (height - (panelY + panelHeight)) * scale, panelWidth * scale, (panelHeight - 25) * scale);
        int y = panelY + 25 - commandScroll.getOffset(); int x = panelX + 5; int w = panelWidth - 10;
        fontRendererObj.drawStringWithShadow("Create settings:", x, y, Colors.COMMAND_TEXT); y += 12;
        if (fhkPresetNameInput != null) {
            fhkPresetNameInput.setBounds(x, y, Math.max(60, w - 65), 16); fhkPresetNameInput.draw(mouseX, mouseY);
            int btnX = x + w - 60; drawRect(btnX, y, btnX + 60, y + 16, Colors.BUTTON_GREEN); drawCenteredString(fontRendererObj, "Confirm", btnX + 30, y + 4, Colors.BUTTON_TEXT); y += 22;
        }
        fontRendererObj.drawStringWithShadow("Saved settings:", x, y, Colors.COMMAND_TEXT); y += 12;
        int presetBtnH = 16;
        for (int i = 0; i < AllConfig.INSTANCE.FHK_PRESETS.size(); i++) {
            int openW = Math.max(60, w - 70);
            int openX = x, openY = y;
            boolean isActive = (i == AllConfig.INSTANCE.FHK_ACTIVE_PRESET);
            drawRect(openX, openY, openX + openW, openY + presetBtnH, isActive ? Colors.BUTTON_GREEN : Colors.BUTTON_RED);
            drawCenteredString(fontRendererObj, AllConfig.INSTANCE.FHK_PRESETS.get(i).name + (isActive ? "  (Active)" : ""), openX + openW / 2, openY + 4, Colors.BUTTON_TEXT);
            int rmX = x + w - 60; drawRect(rmX, openY, rmX + 60, openY + presetBtnH, Colors.BUTTON_RED); drawCenteredString(fontRendererObj, "Remove", rmX + 30, openY + 4, Colors.BUTTON_TEXT);
            y += presetBtnH + 4;
        }
        drawRect(x, y, x + w, y + 1, 0x33000000); y += 6;
        // Appearance options
        for (Toggle t : Toggles) { t.draw(mouseX, mouseY, y, fontRendererObj); y += 22; }
        for (LabelledInput t : labelledInputs) { t.draw(mouseX, mouseY, y, fontRendererObj); y += t.getVerticalSpace(); }
        for (ColorInput t : ColorInputs) { t.draw(mouseX, mouseY, y, fontRendererObj); y += 50; }
        glDisable(GL_SCISSOR_TEST);
        // Scrollbar sizing: include detail rows height to share one scroll
        int optionsHeight = (12 + 22) + (12 + AllConfig.INSTANCE.FHK_PRESETS.size() * (presetBtnH + 4)) + (Toggles.size() * 22);
        for (LabelledInput li : labelledInputs) optionsHeight += li.getVerticalSpace(); optionsHeight += ColorInputs.size() * 50;
        int rowsHeight = (fhkSelectedPreset >= 0 ? (fastRows.size() * Dimensions.FH_ROW_HEIGHT + 8 + Dimensions.FH_ADD_HEIGHT) : 0);
        int totalHeight = Math.max(optionsHeight, rowsHeight);
        commandScroll.update(totalHeight, panelHeight - 25);
        if (commandScroll.shouldRenderScrollbar()) commandScroll.updateScrollbarPosition(panelX + panelWidth - Dimensions.SCROLLBAR_WIDTH - 2, panelY + 25, panelHeight - 25);
    }

    // Right detail editor for commands
    private void drawFastHotkeyDetailPanel(int mouseX, int mouseY, int panelX, int panelY, int panelWidth, int panelHeight) {
        int detailX = useSidePanelForSelected ? (panelX + panelWidth + 6) : (optionsInline && SelectedModule != null && "Fast Hotkey".equals(SelectedModule.name) ? getInlineDetailX() : panelX);
        int detailW = 170; int detailY = panelY; int detailH = panelHeight;
        drawRect(detailX, detailY, detailX + detailW, detailY + detailH, Colors.COMMAND_PANEL);
        drawRect(detailX - 1, detailY - 1, detailX + detailW + 1, detailY + detailH + 1, Colors.COMMAND_BORDER);
        drawCenteredString(fontRendererObj, "Preset Editor", detailX + detailW / 2, detailY + 5, Colors.COMMAND_TEXT);
        int scale = new ScaledResolution(Minecraft.getMinecraft()).getScaleFactor(); glEnable(GL_SCISSOR_TEST);
        glScissor(detailX * scale, (height - (detailY + detailH)) * scale, detailW * scale, (detailH - 25) * scale);
        int x = detailX + 5; int w = detailW - 10; int contentY = panelY + 25 - commandScroll.getOffset();
        for (int i = 0; i < fastRows.size(); i++) {
            FastRow row = fastRows.get(i); int rowTop = contentY + i * Dimensions.FH_ROW_HEIGHT;
            if (rowTop + Dimensions.FH_ROW_HEIGHT < detailY + 25 || rowTop > detailY + detailH) continue;
            drawRect(x, rowTop - 2, x + w, rowTop - 1, 0x33000000);
            int title1Y = rowTop + 2; int labelInputY = title1Y + 12; int title2Y = labelInputY + Dimensions.FH_INPUT_HEIGHT + Dimensions.FH_GAP_Y + 4; int commandInputY = title2Y + 12;
            fontRendererObj.drawStringWithShadow("Command " + (i + 1) + " Label:", x, title1Y, Colors.COMMAND_TEXT);
            fontRendererObj.drawStringWithShadow("Command " + (i + 1) + " Command:", x, title2Y, Colors.COMMAND_TEXT);
            row.DrawElements(mouseX, mouseY, labelInputY, commandInputY);
            int removeX = x, removeY = commandInputY + Dimensions.FH_INPUT_HEIGHT + Dimensions.FH_GAP_Y;
            drawRect(removeX, removeY, removeX + Dimensions.FH_REMOVE_WIDTH, removeY + Dimensions.FH_REMOVE_HEIGHT, Colors.BUTTON_RED);
            drawCenteredString(fontRendererObj, "Remove", removeX + Dimensions.FH_REMOVE_WIDTH / 2, removeY + 5, Colors.BUTTON_TEXT);
        }
        int addY = contentY + fastRows.size() * Dimensions.FH_ROW_HEIGHT + 8; int addW = 60;
        drawRect(x, addY, x + addW, addY + Dimensions.FH_ADD_HEIGHT, Colors.BUTTON_GREEN);
        drawCenteredString(fontRendererObj, "Add", x + addW / 2, addY + 6, Colors.BUTTON_TEXT);
        glDisable(GL_SCISSOR_TEST);
        // When in inline mode (no left panel), maintain scrollbar for the detail region
        if (!useSidePanelForSelected) {
            int rowsHeight = (fastRows.size() * Dimensions.FH_ROW_HEIGHT + 8 + Dimensions.FH_ADD_HEIGHT);
            int viewH = panelHeight - 25;
            commandScroll.update(rowsHeight, viewH);
            commandScroll.updateScrollbarPosition(detailX + detailW - Dimensions.SCROLLBAR_WIDTH - 2, panelY + 25, viewH);
        }
    }

    private void handleFastHotKeyClicks(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton != 0) return;
        int panelX = guiLeft + Dimensions.COMMAND_PANEL_X; int panelY = guiTop + Dimensions.COMMAND_PANEL_Y; int panelWidth = Dimensions.COMMAND_PANEL_WIDTH;
        int leftX = panelX + 5; int leftW = panelWidth - 10; int leftContentY = panelY + 25 - commandScroll.getOffset();
        boolean isInline = optionsInline && SelectedModule != null && "Fast Hotkey".equals(SelectedModule.name);
        int rightPanelX = isInline ? getInlineDetailX() : (panelX + panelWidth + 6);
        int rightPanelW = 170;
        int areaTopY = panelY; int areaBotY = panelY + (Dimensions.GUI_HEIGHT - 60);
        boolean clickInRight = mouseX >= rightPanelX && mouseX <= rightPanelX + rightPanelW && mouseY >= areaTopY && mouseY <= areaBotY;
        boolean clickInLeft = !isInline && mouseX >= panelX && mouseX <= panelX + panelWidth && mouseY >= areaTopY && mouseY <= areaBotY;

        // Right-side Preset Editor clicks (both modes)
        if (clickInRight) {
            int detailX = rightPanelX; int x = detailX + 5; int contentY = panelY + 25 - commandScroll.getOffset();
            for (int i = 0; i < fastRows.size(); i++) {
                FastRow row = fastRows.get(i); int rowTop = contentY + i * Dimensions.FH_ROW_HEIGHT;
                int labelInputY = rowTop + 14; int commandInputY = labelInputY + Dimensions.FH_INPUT_HEIGHT + Dimensions.FH_GAP_Y + 16;
                if (row.labelInput.isMouseOver(mouseX, mouseY, labelInputY)) { unfocusAllFastInputs(); row.labelInput.beginEditing(mouseX, row.labelInput.x); return; }
                if (row.commandInput.isMouseOver(mouseX, mouseY, commandInputY)) { unfocusAllFastInputs(); row.commandInput.beginEditing(mouseX, row.commandInput.x); return; }
                int removeX = x; int removeY = commandInputY + Dimensions.FH_INPUT_HEIGHT + Dimensions.FH_GAP_Y;
                if (mouseX >= removeX && mouseX <= removeX + Dimensions.FH_REMOVE_WIDTH && mouseY >= removeY && mouseY <= removeY + Dimensions.FH_REMOVE_HEIGHT) {
                    if (i < AllConfig.INSTANCE.FAST_HOTKEY_ENTRIES.size()) {
                        AllConfig.INSTANCE.FAST_HOTKEY_ENTRIES.remove(i);
                        List<FastHotkeyEntry> old = new ArrayList<>(AllConfig.INSTANCE.FAST_HOTKEY_ENTRIES);
                        List<FastHotkeyEntry> rebuilt = new ArrayList<>();
                        for (int j = 0; j < old.size(); j++) rebuilt.add(new FastHotkeyEntry(old.get(j).label, old.get(j).command, j));
                        java.util.List<FastHotkeyEntry> list = AllConfig.INSTANCE.FHK_PRESETS.get(AllConfig.INSTANCE.FHK_ACTIVE_PRESET).entries;
                        list.clear(); list.addAll(rebuilt);
                        AllConfig.INSTANCE.setActiveFhkPreset(AllConfig.INSTANCE.FHK_ACTIVE_PRESET);
                        ConfigIO.INSTANCE.SaveFastHotKeyPresets(AllConfig.INSTANCE.FHK_PRESETS, AllConfig.INSTANCE.FHK_ACTIVE_PRESET);
                        rebuildFastHotkeyRowsForDetail();
                    }
                    return;
                }
            }
            int addY = contentY + fastRows.size() * Dimensions.FH_ROW_HEIGHT + 8; int addW = 60;
            if (mouseX >= x && mouseX <= x + addW && mouseY >= addY && mouseY <= addY + Dimensions.FH_ADD_HEIGHT) {
                int idx = AllConfig.INSTANCE.FAST_HOTKEY_ENTRIES.size(); AllConfig.INSTANCE.FAST_HOTKEY_ENTRIES.add(new FastHotkeyEntry("", "", idx));
                ConfigIO.INSTANCE.SaveFastHotKeyPresets(AllConfig.INSTANCE.FHK_PRESETS, AllConfig.INSTANCE.FHK_ACTIVE_PRESET);
                rebuildFastHotkeyRowsForDetail();
            }
            return;
        }

        // Left side (only in side-panel mode): presets list + appearance options
        if (clickInLeft) {
            // Preset input + confirm
            int y = leftContentY + 12; if (fhkPresetNameInput != null) {
                fhkPresetNameInput.setBounds(leftX, y, Math.max(60, leftW - 65), 16);
                if (fhkPresetNameInput.isMouseOver(mouseX, mouseY)) { unfocusAllFastInputs(); fhkPresetNameInput.beginEditing(mouseX); return; }
                int btnX = leftX + leftW - 60; if (mouseX >= btnX && mouseX <= btnX + 60 && mouseY >= y && mouseY <= y + 16) {
                    String name = fhkPresetNameInput.text.trim(); if (!name.isEmpty()) {
                        boolean exists = false; for (FastHotkeyPreset p : AllConfig.INSTANCE.FHK_PRESETS) { if (p.name.equalsIgnoreCase(name)) { exists = true; break; } }
                        if (!exists) { AllConfig.INSTANCE.FHK_PRESETS.add(new FastHotkeyPreset(name)); AllConfig.INSTANCE.setActiveFhkPreset(AllConfig.INSTANCE.FHK_PRESETS.size() - 1); fhkSelectedPreset = AllConfig.INSTANCE.FHK_ACTIVE_PRESET; fhkPresetNameInput.text = ""; ConfigIO.INSTANCE.SaveFastHotKeyPresets(AllConfig.INSTANCE.FHK_PRESETS, AllConfig.INSTANCE.FHK_ACTIVE_PRESET); rebuildFastHotkeyRowsForDetail(); }
                    }
                    return;
                }
                y += 22;
            }
            // Saved list open/remove
            y += 12; int presetBtnH = 16;
            for (int i = 0; i < AllConfig.INSTANCE.FHK_PRESETS.size(); i++) {
                int openW = Math.max(60, leftW - 70); int openX = leftX; int openY = y; int rmX = leftX + leftW - 60;
                if (mouseX >= openX && mouseX <= openX + openW && mouseY >= openY && mouseY <= openY + presetBtnH) { AllConfig.INSTANCE.setActiveFhkPreset(i); fhkSelectedPreset = i; rebuildFastHotkeyRowsForDetail(); return; }
                if (mouseX >= rmX && mouseX <= rmX + 60 && mouseY >= openY && mouseY <= openY + presetBtnH) {
                    if (AllConfig.INSTANCE.FHK_PRESETS.size() > 1) {
                        AllConfig.INSTANCE.FHK_PRESETS.remove(i);
                        int newActive = Math.max(0, Math.min(AllConfig.INSTANCE.FHK_ACTIVE_PRESET - (i <= AllConfig.INSTANCE.FHK_ACTIVE_PRESET ? 1 : 0), AllConfig.INSTANCE.FHK_PRESETS.size() - 1));
                        AllConfig.INSTANCE.setActiveFhkPreset(newActive);
                        fhkSelectedPreset = -1; fastRows.clear();
                        ConfigIO.INSTANCE.SaveFastHotKeyPresets(AllConfig.INSTANCE.FHK_PRESETS, AllConfig.INSTANCE.FHK_ACTIVE_PRESET);
                    }
                    return;
                }
                y += presetBtnH + 4;
            }
            // Appearance options (toggles/inputs/colors)
            if (handleLabelledInputClicks(mouseX, mouseY)) return;
            if (handleColorInputClicks(mouseX, mouseY)) return;
            int yToggle = leftContentY + 12 + 22 + 12 + (AllConfig.INSTANCE.FHK_PRESETS.size() * (presetBtnH + 4)) + 6;
            for (Toggle t : Toggles) { if (t.isMouseOver(mouseX, mouseY, yToggle)) { t.toggle(); return; } yToggle += 22; }
        }
    }

    private void handleFastHotKeyTyping(char typedChar, int keyCode) {
        // Handle key capture first
        if (fhkKeyCaptureIndex >= 0) {
            if (keyCode == Keyboard.KEY_ESCAPE) { fhkKeyCaptureIndex = -1; return; }
            if (keyCode > 0) {
                // Validate: no duplicates, valid name, not NONE
                String name = Keyboard.getKeyName(keyCode);
                if (name != null && !name.trim().isEmpty() && !"NONE".equalsIgnoreCase(name)) {
                    if (!isFhkKeyDuplicate(keyCode, fhkKeyCaptureIndex)) {
                        FastHotkeyPreset p = AllConfig.INSTANCE.FHK_PRESETS.get(fhkKeyCaptureIndex);
                        p.keyCode = keyCode;
                        // Auto-enable now that key is valid and unique
                        p.enabled = true;
                        ConfigIO.INSTANCE.SaveFastHotKeyPresets(AllConfig.INSTANCE.FHK_PRESETS, AllConfig.INSTANCE.FHK_ACTIVE_PRESET);
                        // Auto-select this preset as active
                        AllConfig.INSTANCE.setActiveFhkPreset(fhkKeyCaptureIndex); fhkSelectedPreset = fhkKeyCaptureIndex; rebuildFastHotkeyRowsForDetail();
                        fhkKeyCaptureIndex = -1; return;
                    } else {
                        // Duplicate: keep capturing until a unique key is pressed
                        return;
                    }
                } else {
                    // invalid key, keep capturing
                    return;
                }
            }
            return;
        }
        // Existing text inputs
        handleAllInputTyping(typedChar, keyCode);
        for (FastRow row : fastRows) {
            if (row.labelInput.isEditing) { row.labelInput.handleKeyTyped(typedChar, keyCode); return; }
            if (row.commandInput.isEditing) { row.commandInput.handleKeyTyped(typedChar, keyCode); return; }
        }
    }

    private boolean isFhkKeyDuplicate(int keyCode, int exceptIndex) {
        if (keyCode <= 0) return false;
        List<FastHotkeyPreset> list = AllConfig.INSTANCE.FHK_PRESETS;
        for (int i = 0; i < list.size(); i++) {
            if (i == exceptIndex) continue;
            FastHotkeyPreset p = list.get(i);
            if (p.keyCode == keyCode) return true;
        }
        return false;
    }

    // New: Hotbar Swap typing handler
    private void handleHotbarSwapTyping(char typedChar, int keyCode) {
        // Update any focused hotbar preset input and propagate to HotbarSwap
        handleAllInputTyping(typedChar, keyCode);
        for (int i = 0; i < hotbarRows.size(); i++) {
            HotbarPresetRow row = hotbarRows.get(i);
            boolean changed = false;
            if (row.labelInput.isEditing) { row.labelInput.handleKeyTyped(typedChar, keyCode); changed = true; }
            else if (row.triggerInput.isEditing) { row.triggerInput.handleKeyTyped(typedChar, keyCode); changed = true; }
            if (changed) {
                // Persist without rebuilding rows to preserve input focus
                com.aftertime.ratallofyou.modules.SkyBlock.HotbarSwap.INSTANCE.updatePresetMeta(i, row.labelInput.text.trim(), row.triggerInput.text.trim(), null);
                return;
            }
        }
    }

    private void unfocusAllFastInputs() {
        for (FastRow r : fastRows) { r.labelInput.isEditing = false; r.commandInput.isEditing = false; }
        if (fhkPresetNameInput != null) fhkPresetNameInput.isEditing = false;
    }

    // New: unfocus Hotbar Swap inputs
    private void unfocusAllHotbarInputs() { for (HotbarPresetRow r : hotbarRows) { r.labelInput.isEditing = false; r.triggerInput.isEditing = false; } }

    // Input and scroll helpers
    private void handleInputFieldEditingState() { for (ColorInput c : ColorInputs) c.unfocus(); if (showCommandSettings && SelectedModule != null && "Fast Hotkey".equals(SelectedModule.name)) unfocusAllFastInputs(); if (showCommandSettings && SelectedModule != null && "Hotbar Swap".equals(SelectedModule.name)) unfocusAllHotbarInputs(); }

    private void handleScrollbarClicks(int mouseX, int mouseY) { if (showCommandSettings && useSidePanelForSelected && commandScroll.checkScrollbarClick(mouseX, mouseY)) return; if (showCommandSettings && optionsInline && SelectedModule != null && "Fast Hotkey".equals(SelectedModule.name) && fhkSelectedPreset >= 0 && commandScroll.checkScrollbarClick(mouseX, mouseY)) return; if (mainScroll.checkScrollbarClick(mouseX, mouseY)) return; }

    private void handleCategoryButtonClicks() { for (GuiButton btn : categoryButtons) { if (btn.isMouseOver()) { actionPerformed(btn); return; } } }

    private void handleModuleButtonClicks(int mouseX, int mouseY) { for (ModuleButton moduleBtn : moduleButtons) { if (moduleBtn.isMouseOver(mouseX, mouseY)) { handleModuleButtonClick(moduleBtn, mouseX, mouseY); return; } } }

    private void handleModuleButtonClick(ModuleButton moduleBtn, int mouseX, int mouseY) {
        ModuleInfo module = moduleBtn.getModule();
        if ("Move GUI Position".equals(module.name)) UIHighlighter.enterMoveMode(Minecraft.getMinecraft().currentScreen);
        if (moduleBtn.isDropdownClicked(mouseX, mouseY)) {
            if (showCommandSettings && SelectedModule == module) { showCommandSettings = false; SelectedModule = null; useSidePanelForSelected = false; optionsInline = false; buildModuleButtons(); return; }
            if (!module.Data) module.Data = true;
            SelectedModule = module; showCommandSettings = true;
            if ("Fast Hotkey".equals(SelectedModule.name)) { useSidePanelForSelected = false; optionsInline = true; fhkSelectedPreset = AllConfig.INSTANCE.FHK_ACTIVE_PRESET; rebuildFastHotkeyRowsForDetail(); }
            else { useSidePanelForSelected = false; optionsInline = true; }
            initializeCommandToggles(); buildModuleButtons(); return;
        }
        boolean wasEnabled = module.Data; module.Data = !module.Data;
        if (wasEnabled && !module.Data && SelectedModule == module) { showCommandSettings = false; SelectedModule = null; useSidePanelForSelected = false; optionsInline = false; buildModuleButtons(); }
    }

    private void handleCommandToggleClicks(int mouseX, int mouseY) {
        if (!showCommandSettings) return;
        if (optionsInline && SelectedModule != null) { InlineArea ia = getInlineAreaForSelected(); if (ia != null) handleInlineOptionClicks(mouseX, mouseY, ia); return; }
        if (handleLabelledInputClicks(mouseX, mouseY)) return;
        if (handleDropdownClicks(mouseX, mouseY)) return;
        if (handleColorInputClicks(mouseX, mouseY)) return;
        if (SelectedModule == null) return;
        int y = guiTop + Dimensions.COMMAND_PANEL_Y + 30 - commandScroll.getOffset();
        for (Toggle toggle : Toggles) { if (toggle.isMouseOver(mouseX, mouseY, y)) { toggle.toggle(); if (toggle.ref != null && toggle.ref.ConfigType == 4) TerminalSettingsApplier.applyFromAllConfig(); return; } y += 22; }
    }

    private boolean handleDropdownClicks(int mouseX, int mouseY) {
        if (SelectedModule == null) return false;
        int y = guiTop + Dimensions.COMMAND_PANEL_Y + 30 - commandScroll.getOffset();
        for (Toggle ignored : Toggles) y += 22; for (LabelledInput li : labelledInputs) y += li.getVerticalSpace(); for (ColorInput ignored : ColorInputs) y += 50;
        for (MethodDropdown dd : methodDropdowns) {
            int bx = dd.x + 100, bw = dd.width - 100, bh = dd.height; boolean inBase = mouseX >= bx && mouseX <= bx + bw && mouseY >= y && mouseY <= y + bh;
            if (inBase) { for (MethodDropdown other : methodDropdowns) other.isOpen = false; dd.isOpen = !dd.isOpen; return true; }
            if (dd.isOpen) { for (int i = 0; i < dd.methods.length; i++) { int optionY = y + bh + (i * bh); boolean inOpt = mouseX >= bx && mouseX <= bx + bw && mouseY >= optionY && mouseY <= optionY + bh; if (inOpt) { dd.selectMethod(i); dd.isOpen = false; return true; } } }
            y += 22;
        }
        return false;
    }

    private boolean handleLabelledInputClicks(int mouseX, int mouseY) {
        if (SelectedModule == null) return false; int y = guiTop + Dimensions.COMMAND_PANEL_Y + 30 - commandScroll.getOffset();
        for (Toggle ignored : Toggles) y += 22;
        for (LabelledInput li : labelledInputs) { if (li.isMouseOver(mouseX, mouseY, y)) { for (LabelledInput other : labelledInputs) other.isEditing = false; li.beginEditing(mouseX); return true; } y += li.getVerticalSpace(); }
        return false;
    }

    private boolean handleColorInputClicks(int mouseX, int mouseY) {
        if (SelectedModule == null) return false; int y = guiTop + Dimensions.COMMAND_PANEL_Y + 30 - commandScroll.getOffset();
        for (Toggle ignored : Toggles) y += 22; for (LabelledInput li : labelledInputs) y += li.getVerticalSpace();
        for (ColorInput ci : ColorInputs) { int inputY = y + ci.height + 8; boolean hover = (mouseX >= ci.x + 40 && mouseX <= ci.x + ci.width && mouseY >= inputY - 2 && mouseY <= inputY + 15); if (hover) { ci.beginEditing(mouseX); return true; } y += 50; }
        return false;
    }

    private void handleScrollbarDrag(int mouseX, int mouseY) { if (mainScroll.isDragging) mainScroll.handleDrag(mouseX, mouseY, this::buildModuleButtons); if ((useSidePanelForSelected || (optionsInline && SelectedModule != null && "Fast Hotkey".equals(SelectedModule.name) && fhkSelectedPreset >= 0)) && commandScroll.isDragging) commandScroll.handleDrag(mouseX, mouseY, null); }

    private void handleAllInputTyping(char typedChar, int keyCode) { if (!showCommandSettings) return; for (ColorInput t : ColorInputs) t.handleKeyTyped(typedChar, keyCode); for (LabelledInput t : labelledInputs) t.handleKeyTyped(typedChar, keyCode); }

    // Build UI lists
    private void buildCategoryButtons() {
        categoryButtons.clear(); buttonList.clear(); int y = guiTop + 30; int x = guiLeft + 10;
        for (int i = 0; i < AllConfig.INSTANCE.Categories.size(); i++) { GuiButton b = new GuiButton(1000 + i, x, y, 95, 18, AllConfig.INSTANCE.Categories.get(i)); categoryButtons.add(b); buttonList.add(b); y += 20; }
    }

    private void buildModuleButtons() {
        moduleButtons.clear(); int listX = guiLeft + 120; int listY = guiTop + 28; int listW = Dimensions.GUI_WIDTH - 120 - 10 - Dimensions.SCROLLBAR_WIDTH; int y = listY - mainScroll.getOffset(); int rowH = 32; int usedHeight = 0;
        for (BaseConfig<?> mi : AllConfig.INSTANCE.MODULES.values()) {
            ModuleInfo info = (ModuleInfo) mi; if (!info.category.equals(selectedCategory)) continue;
            boolean hasSettings = hasSettings(info); moduleButtons.add(new ModuleButton(listX + 4, y, listW - 8, rowH - 2, info, hasSettings));
            int inc = rowH; if (showCommandSettings && optionsInline && SelectedModule == info) { inc += 20 + computeInlineContentHeight() + 8; }
            y += inc; usedHeight += inc;
        }
        int totalHeight = usedHeight; int viewH = Dimensions.GUI_HEIGHT - 50; mainScroll.update(totalHeight, viewH); mainScroll.updateScrollbarPosition(listX + listW - 2, listY, viewH);
    }

    private boolean hasSettings(ModuleInfo module) {
        if (module == null || module.name == null) return false;
        switch (module.name) { case "Dungeon Terminals": case "Party Commands": case "No Debuff": case "Etherwarp Overlay": case "Fast Hotkey": case "Chest Open Notice": case "Hotbar Swap": return true; default: return false; }
    }

    private void initializeCommandToggles() {
        Toggles.clear(); labelledInputs.clear(); methodDropdowns.clear(); ColorInputs.clear(); if (SelectedModule == null) return;
        Integer y = guiTop + Dimensions.COMMAND_PANEL_Y + 30;
        switch (SelectedModule.name) {
            case "Dungeon Terminals": Add_SubSetting_Terminal(y); break;
            case "Party Commands": Add_SubSetting_Command(y); break;
            case "No Debuff": Add_SubSetting_NoDebuff(y); break;
            case "Etherwarp Overlay": Add_SubSetting_Etherwarp(y); break;
            case "Fast Hotkey": Add_SubSetting_FastHotkey(y); break;
            case "Chest Open Notice": Add_SubSetting_ChestOpen(y); break;
            case "Hotbar Swap": Add_SubSetting_HotbarSwap(y); rebuildHotbarRows(); break;
        }
        int contentHeight = 0; if (useSidePanelForSelected && "Fast Hotkey".equals(SelectedModule.name)) contentHeight += 12 + 22 + 12 + (AllConfig.INSTANCE.FHK_PRESETS.size() * (16 + 4));
        contentHeight += Toggles.size() * 22; for (LabelledInput li : labelledInputs) contentHeight += li.getVerticalSpace(); contentHeight += ColorInputs.size() * 50; contentHeight += methodDropdowns.size() * 22;
        int panelViewHeight = Dimensions.GUI_HEIGHT - 60 - 25;
        // Only update commandScroll position/size for side panel mode; inline mode updates in draw methods as needed
        if (useSidePanelForSelected) {
            commandScroll.update(contentHeight, panelViewHeight); commandScroll.updateScrollbarPosition(guiLeft + Dimensions.COMMAND_PANEL_X + Dimensions.COMMAND_PANEL_WIDTH - Dimensions.SCROLLBAR_WIDTH - 2, guiTop + Dimensions.COMMAND_PANEL_Y + 25, panelViewHeight);
        }
    }

    private String getCommandPanelTitle() { return SelectedModule == null ? "" : ("Settings - " + SelectedModule.name); }

    private void drawCommandPanelContent(int mouseX, int mouseY, int panelX, int panelY, int panelWidth, int panelHeight) {
        int scale = new ScaledResolution(Minecraft.getMinecraft()).getScaleFactor(); glEnable(GL_SCISSOR_TEST);
        glScissor(panelX * scale, (height - (panelY + panelHeight)) * scale, panelWidth * scale, (panelHeight - 25) * scale);
        int y = panelY + 30 - commandScroll.getOffset();
        for (Toggle t : Toggles) { t.draw(mouseX, mouseY, y, fontRendererObj); y += 22; }
        for (LabelledInput t : labelledInputs) { t.draw(mouseX, mouseY, y, fontRendererObj); y += t.getVerticalSpace(); }
        for (ColorInput t : ColorInputs) { t.draw(mouseX, mouseY, y, fontRendererObj); y += 50; }
        for (MethodDropdown t : methodDropdowns) { t.draw(mouseX, mouseY, y, fontRendererObj); y += 22; }
        glDisable(GL_SCISSOR_TEST);
        int contentHeight = Toggles.size() * 22; for (LabelledInput li : labelledInputs) contentHeight += li.getVerticalSpace(); contentHeight += ColorInputs.size() * 50; contentHeight += methodDropdowns.size() * 22;
        int viewH = panelHeight - 25; commandScroll.update(contentHeight, viewH); commandScroll.updateScrollbarPosition(panelX + panelWidth - Dimensions.SCROLLBAR_WIDTH - 2, panelY + 25, viewH);
    }

    // Builders for option elements
    private void AddEntryAsOption(Map.Entry<String, BaseConfig<?>> entry, Integer y, int ConfigType) {
        PropertyRef ref = new PropertyRef(ConfigType, entry.getKey()); Type type = entry.getValue().type; Object data = entry.getValue().Data;
        int xPos, width; if (optionsInline && !useSidePanelForSelected) { int listX = guiLeft + 120; int listW = Dimensions.GUI_WIDTH - 120 - Dimensions.SCROLLBAR_WIDTH; int boxX = listX + 4; int boxW = listW - 8; int padding = 6; xPos = boxX + padding; width = (listW - 8) - padding * 2; } else { xPos = guiLeft + Dimensions.COMMAND_PANEL_X + 5; width = Dimensions.COMMAND_PANEL_WIDTH - 10; }
        boolean isVerticalAbove = (ConfigType == 4 || ConfigType == 6);
        if (type.equals(String.class)) labelledInputs.add(new LabelledInput(ref, entry.getValue().name, String.valueOf(data), xPos, y, width, 16, isVerticalAbove));
        else if (type.equals(Boolean.class)) Toggles.add(new Toggle(ref, entry.getValue().name, entry.getValue().description, (Boolean) data, xPos, y, width, 16));
        else if (type.equals(Integer.class)) labelledInputs.add(new LabelledInput(ref, entry.getValue().name, String.valueOf(data), xPos, y, width, 16, isVerticalAbove));
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

    private void Add_SubSetting_FastHotkey(Integer y) {
        for (Map.Entry<String, BaseConfig<?>> e : AllConfig.INSTANCE.FASTHOTKEY_CONFIGS.entrySet()) {
            AddEntryAsOption(e, y, 6);
        }
    }

    private void addFhkEntry(String key, Integer y) { BaseConfig<?> cfg = AllConfig.INSTANCE.FASTHOTKEY_CONFIGS.get(key); if (cfg == null) return; AddEntryAsOption(new java.util.AbstractMap.SimpleEntry<>(key, cfg), y, 6); }

    private void Add_SubSetting_Command(Integer y) { for (Map.Entry<String, BaseConfig<?>> e : AllConfig.INSTANCE.COMMAND_CONFIGS.entrySet()) AddEntryAsOption(e, y, 0); }
    private void Add_SubSetting_NoDebuff(Integer y) { for (Map.Entry<String, BaseConfig<?>> e : AllConfig.INSTANCE.NODEBUFF_CONFIGS.entrySet()) AddEntryAsOption(e, y, 2); }
    private void Add_SubSetting_Etherwarp(Integer y) { for (Map.Entry<String, BaseConfig<?>> e : AllConfig.INSTANCE.ETHERWARP_CONFIGS.entrySet()) AddEntryAsOption(e, y, 3); }

    // New: Chest Open Notice sub-settings (index 7 in AllConfig.ALLCONFIGS)
    private void Add_SubSetting_ChestOpen(Integer y) {
        for (Map.Entry<String, BaseConfig<?>> e : AllConfig.INSTANCE.KUUDRA_CHESTOPEN_CONFIGS.entrySet()) {
            AddEntryAsOption(e, y, 7);
        }
    }

    // New: Hotbar Swap sub-settings (index 8 in AllConfig.ALLCONFIGS)
    private void Add_SubSetting_HotbarSwap(Integer y) {
        for (Map.Entry<String, BaseConfig<?>> e : AllConfig.INSTANCE.HOTBARSWAP_CONFIGS.entrySet()) {
            AddEntryAsOption(e, y, 8);
        }
    }

    private void rebuildFastHotkeyRowsForDetail() {
        fastRows.clear(); if (!("Fast Hotkey".equals(SelectedModule != null ? SelectedModule.name : null))) return;
        int detailBaseX = useSidePanelForSelected ? (guiLeft + Dimensions.COMMAND_PANEL_X + Dimensions.COMMAND_PANEL_WIDTH + 6 + 5) : (getInlineDetailX() + 5);
        int detailInputW = 170 - 10;
        for (FastHotkeyEntry e : AllConfig.INSTANCE.FAST_HOTKEY_ENTRIES) fastRows.add(new FastRow(detailBaseX, detailInputW, e));
    }

    // New: rebuild Hotbar Swap rows from current presets
    private void rebuildHotbarRows() {
        hotbarRows.clear();
        java.util.List<com.aftertime.ratallofyou.modules.SkyBlock.HotbarSwap.Hotbar> view = com.aftertime.ratallofyou.modules.SkyBlock.HotbarSwap.INSTANCE != null ? com.aftertime.ratallofyou.modules.SkyBlock.HotbarSwap.INSTANCE.getPresetsView() : java.util.Collections.<com.aftertime.ratallofyou.modules.SkyBlock.HotbarSwap.Hotbar>emptyList();
        for (int i = 0; i < view.size(); i++) {
            com.aftertime.ratallofyou.modules.SkyBlock.HotbarSwap.Hotbar p = view.get(i);
            HotbarPresetRow row = new HotbarPresetRow(i, p.name == null ? "" : p.name, p.message == null ? "" : p.message);
            hotbarRows.add(row);
        }
    }

    // New: add/remove helpers for Hotbar Swap
    private void addCurrentHotbarAsPreset() {
        if (com.aftertime.ratallofyou.modules.SkyBlock.HotbarSwap.INSTANCE == null) return;
        com.aftertime.ratallofyou.modules.SkyBlock.HotbarSwap.INSTANCE.addPresetFromCurrentHotbar();
        rebuildHotbarRows();
        buildModuleButtons();
    }
    private void removeHotbarPresetAtIndex(int rowIndex) {
        if (com.aftertime.ratallofyou.modules.SkyBlock.HotbarSwap.INSTANCE == null) return;
        com.aftertime.ratallofyou.modules.SkyBlock.HotbarSwap.INSTANCE.removePreset(rowIndex);
        rebuildHotbarRows();
        buildModuleButtons();
    }

    private class ScrollManager {
        boolean isDragging = false; private int contentHeight, viewHeight, offset; private int barX, barY, barH; private int handleY, handleH; private int dragStartY, dragStartOffset;
        void reset() { contentHeight = 0; viewHeight = 0; offset = 0; isDragging = false; }
        int getOffset() { return Math.max(0, Math.min(offset, Math.max(0, contentHeight - viewHeight))); }
        void update(int total, int view) { contentHeight = Math.max(0, total); viewHeight = Math.max(0, view); int maxOffset = Math.max(0, contentHeight - viewHeight); if (offset > maxOffset) offset = maxOffset; recalcHandle(); }
        void updateScrollbarPosition(int x, int y, int h) { barX = x; barY = y; barH = h; recalcHandle(); }
        boolean shouldRenderScrollbar() { return contentHeight > viewHeight && viewHeight > 0; }
        void drawScrollbar(int trackColor, int handleColor) { if (!shouldRenderScrollbar()) return; drawRect(barX, barY, barX + Dimensions.SCROLLBAR_WIDTH, barY + barH, trackColor); drawRect(barX, handleY, barX + Dimensions.SCROLLBAR_WIDTH, handleY + handleH, handleColor); }
        boolean checkScrollbarClick(int mx, int my) { if (!shouldRenderScrollbar()) return false; boolean inside = mx >= barX && mx <= barX + Dimensions.SCROLLBAR_WIDTH && my >= barY && my <= barY + barH; if (inside) { isDragging = true; dragStartY = my; dragStartOffset = offset; } return inside; }
        void endScroll() { isDragging = false; }
        void handleDrag(int mx, int my, Runnable onChange) { if (!isDragging || !shouldRenderScrollbar()) return; if (handleH >= barH) { offset = 0; if (onChange != null) onChange.run(); return; } float ratio = (float) (contentHeight - viewHeight) / (float) (barH - handleH); int dy = my - dragStartY; offset = Math.max(0, Math.min(contentHeight - viewHeight, dragStartOffset + Math.round(dy * ratio))); recalcHandle(); if (onChange != null) onChange.run(); }
        void handleWheelScroll(int dWheel, Runnable onChange) { int step = 20 * (dWheel < 0 ? 1 : -1); offset = Math.max(0, Math.min(contentHeight - viewHeight, offset + step)); recalcHandle(); if (onChange != null) onChange.run(); }
        private void recalcHandle() { if (!shouldRenderScrollbar()) { handleY = barY; handleH = barH; return; } int minH = Math.max(Dimensions.MIN_SCROLLBAR_HEIGHT, barH * Math.max(1, viewHeight) / Math.max(1, contentHeight)); handleH = Math.max(Dimensions.MIN_SCROLLBAR_HEIGHT, minH); float t = (contentHeight - viewHeight) == 0 ? 0f : (float) offset / (float) (contentHeight - viewHeight); handleY = barY + Math.round((barH - handleH) * t); }
    }

    private static class SimpleTextField {
        String text; int x, y, w, h; boolean isEditing = false; long cursorBlinkMs = 0; boolean cursorVisible = false; int cursorPos = 0; int maxLen = 48;
        SimpleTextField(String t, int x, int y, int w, int h) { this.text = t == null ? "" : t; setBounds(x,y,w,h); cursorPos = this.text.length(); }
        void setBounds(int x, int y, int w, int h) { this.x=x; this.y=y; this.w=w; this.h=h; }
        void draw(int mx, int my) { Gui.drawRect(x, y, x + w, y + h, Colors.INPUT_BG); Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(text, x + 3, y + 4, Colors.INPUT_FG); if (isEditing) { cursorBlinkMs += 10; if (cursorBlinkMs >= 500) { cursorBlinkMs = 0; cursorVisible = !cursorVisible; } if (cursorVisible) { int cx = x + 3 + Minecraft.getMinecraft().fontRendererObj.getStringWidth(text.substring(0, Math.min(cursorPos, text.length()))); Gui.drawRect(cx, y + 3, cx + 1, y + h - 3, Colors.INPUT_FG); } } }
        boolean isMouseOver(int mx, int my) { return mx >= x && mx <= x + w && my >= y && my <= y + h; }
        void beginEditing(int mx) { isEditing = true; cursorBlinkMs = 0; cursorVisible = true; int rel = Math.max(0, mx - x); int pos = 0; String s = text; while (pos < s.length()) { int cw = Minecraft.getMinecraft().fontRendererObj.getCharWidth(s.charAt(pos)); if (rel < cw / 2) break; rel -= cw; pos++; } cursorPos = pos; }
        void handleKeyTyped(char c, int key) { if (!isEditing) return; if (key == org.lwjgl.input.Keyboard.KEY_RETURN) { isEditing = false; return; } if (key == org.lwjgl.input.Keyboard.KEY_BACK) { if (cursorPos > 0 && !text.isEmpty()) { text = text.substring(0, cursorPos - 1) + text.substring(cursorPos); cursorPos--; } } else if (key == org.lwjgl.input.Keyboard.KEY_LEFT) { cursorPos = Math.max(0, cursorPos - 1); } else if (key == org.lwjgl.input.Keyboard.KEY_RIGHT) { cursorPos = Math.min(text.length(), cursorPos + 1); } else { if (c >= 32 && c != 127) { if (text.length() >= maxLen) return; text = text.substring(0, cursorPos) + c + text.substring(cursorPos); cursorPos++; } } cursorBlinkMs = 0; cursorVisible = true; }
    }

    // New: per-row state for Hotbar Swap presets
    private static class HotbarPresetRow {
        final int index; SimpleTextField labelInput; SimpleTextField triggerInput; int removeBtnX, removeBtnY, removeBtnW, removeBtnH;
        HotbarPresetRow(int idx, String label, String trigger) { this.index = idx; this.labelInput = new SimpleTextField(label, 0,0,0,0); this.triggerInput = new SimpleTextField(trigger, 0,0,0,0); }
    }

    // Last Add button bounds for Hotbar Swap inline area
    private int lastHotbarAddBtnX = -1, lastHotbarAddBtnY = -1, lastHotbarAddBtnW = 0, lastHotbarAddBtnH = 0;

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput(); int dWheel = Mouse.getEventDWheel(); if (dWheel == 0) return;
        int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth; int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
        int listX = guiLeft + 115, listY = guiTop + 25, listW = Dimensions.GUI_WIDTH - 120 - Dimensions.SCROLLBAR_WIDTH, listH = Dimensions.GUI_HEIGHT - 50;
        if (mouseX >= listX && mouseX <= listX + listW && mouseY >= listY && mouseY <= listY + listH) { mainScroll.handleWheelScroll(dWheel, this::buildModuleButtons); return; }
        if (showCommandSettings && useSidePanelForSelected) {
            int panelX = guiLeft + Dimensions.COMMAND_PANEL_X; int panelY = guiTop + Dimensions.COMMAND_PANEL_Y + 25; int panelW = Dimensions.COMMAND_PANEL_WIDTH; int panelH = (Dimensions.GUI_HEIGHT - 60) - 25;
            if (mouseX >= panelX && mouseX <= panelX + panelW && mouseY >= panelY && mouseY <= panelY + panelH) { commandScroll.handleWheelScroll(dWheel, null); return; }
            int detailX = panelX + Dimensions.COMMAND_PANEL_WIDTH + 6; int detailW = 170;
            if (mouseX >= detailX && mouseX <= detailX + detailW && mouseY >= panelY && mouseY <= panelY + panelH) { commandScroll.handleWheelScroll(dWheel, null); return; }
        }
        // Inline Fast Hotkey: wheel on right detail panel
        if (showCommandSettings && optionsInline && SelectedModule != null && "Fast Hotkey".equals(SelectedModule.name) && fhkSelectedPreset >= 0) {
            int panelX = guiLeft + Dimensions.COMMAND_PANEL_X; int panelY = guiTop + Dimensions.COMMAND_PANEL_Y + 25; int panelH = (Dimensions.GUI_HEIGHT - 60) - 25; int detailX = getInlineDetailX(); int detailW = 170;
            if (mouseX >= detailX && mouseX <= detailX + detailW && mouseY >= panelY && mouseY <= panelY + panelH) { commandScroll.handleWheelScroll(dWheel, null); }
        }
    }
}

