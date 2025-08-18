package com.aftertime.ratallofyou.UI;

import com.aftertime.ratallofyou.UI.config.ConfigStorage;
import com.aftertime.ratallofyou.modules.render.EtherwarpOverlay;
import com.aftertime.ratallofyou.modules.render.NoDebuff;
import com.aftertime.ratallofyou.modules.dungeon.terminals.startswith;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import com.aftertime.ratallofyou.UI.config.ConfigStorage.ModuleInfo;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import static org.lwjgl.opengl.GL11.*;

public class ModSettingsGui extends GuiScreen {
    // =============================================
    // Constants
    // =============================================
    private static final class Colors {
        static final int CATEGORY = 0xFF111111;
        static final int MODULE_INACTIVE = 0xFF333333;
        static final int MODULE_ACTIVE = 0xFF006400;
        static final int TEXT = 0xFFFFFFFF;
        static final int PANEL = 0xFF000000;
        static final int CATEGORY_BUTTON = 0xFF222222;
        static final int SELECTED_CATEGORY = 0xFF555555;
        static final int VERSION = 0xFF888888;
        static final int SCROLLBAR = 0xFF333333;
        static final int SCROLLBAR_HANDLE = 0xFF555555;
        static final int MOVE_GUI = 0xFF333333;
        static final int COMMAND_CHECKBOX = 0xFF333333;
        static final int COMMAND_CHECKBOX_SELECTED = 0xFF006400;
        static final int COMMAND_SCROLLBAR = 0xFF555577;
        static final int COMMAND_SCROLLBAR_HANDLE = 0xFF8888AA;
        static final int COMMAND_PANEL = 0xFF222222;
        static final int COMMAND_TEXT = 0xFFFFFFFF;
        static final int COMMAND_BORDER = 0xFF111111;
        // Fast Hotkey UI
        static final int INPUT_BG = 0xFF222222;
        static final int INPUT_FG = 0xFFFFFFFF;
        static final int INPUT_PLACEHOLDER = 0xFFAAAAAA;
        static final int BUTTON_BG = 0xFF2E2E2E;
        static final int BUTTON_BG_HOVER = 0xFF3A3A3A;
        static final int BUTTON_TEXT = 0xFFFFFFFF;
        // New colored buttons
        static final int BUTTON_GREEN = 0xFF2E7D32;
        static final int BUTTON_GREEN_HOVER = 0xFF388E3C;
        static final int BUTTON_RED = 0xFFC62828;
        static final int BUTTON_RED_HOVER = 0xFFD32F2F;
    }

    private static final class Dimensions {
        static final int GUI_WIDTH = 400;
        static final int GUI_HEIGHT = 300;
        static final int SCROLLBAR_WIDTH = 6;
        static final int MIN_SCROLLBAR_HEIGHT = 20;
        static final int TEXT_PADDING = 10;
        static final int LINE_HEIGHT = 9;
        static final int MODULE_LIST_X = 120;
        static final int MODULE_LIST_WIDTH = 270;
        static final int COMMAND_PANEL_X = MODULE_LIST_X + MODULE_LIST_WIDTH + 5;
        static final int COMMAND_PANEL_Y = 30;
        static final int COMMAND_PANEL_WIDTH = 150;
        // Fast Hotkey layout
        // Increased height to accommodate vertical layout (title + input) x2 and remove button
        static final int FH_ROW_HEIGHT = 100;
        static final int FH_INPUT_HEIGHT = 16;
        static final int FH_INPUT_MIN_WIDTH = 60;
        static final int FH_REMOVE_WIDTH = 60;
        static final int FH_REMOVE_HEIGHT = 18;
        static final int FH_ADD_HEIGHT = 20;
        static final int FH_GAP_Y = 6;
    }

    // =============================================
    // Fields
    // =============================================
    private final List<GuiButton> categoryButtons = new ArrayList<GuiButton>();
    private final List<ModuleButton> moduleButtons = new ArrayList<ModuleButton>();
    private final List<CommandToggle> commandToggles = new ArrayList<CommandToggle>();
    private final ScrollManager mainScroll = new ScrollManager();
    private final ScrollManager commandScroll = new ScrollManager();

    private String selectedCategory = "Kuudra";
    private ModuleInfo selectedCommandModule = null;
    private boolean showCommandSettings = false;
    private int guiLeft, guiTop;
    private long lastDeleteTime = 0;
    private boolean deleteKeyHeld = false;

    // Fast Hotkey editor rows
    private final List<FastRow> fastRows = new ArrayList<FastRow>();

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
        this.commandToggles.clear();
        this.mainScroll.reset();
        this.commandScroll.reset();
        this.showCommandSettings = false;
        this.selectedCommandModule = null;
        this.fastRows.clear();

        createCategoryButtons();
        createModuleButtons();
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
            selectedCommandModule = null;
            createModuleButtons();
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        handleInputFieldEditingState();
        handleScrollbarClicks(mouseX, mouseY);

        // Terminal panel input focus handling
        if (showCommandSettings && selectedCommandModule != null && selectedCommandModule.name.equals("Dungeon Terminals")) {
            int contentY = guiTop + Dimensions.COMMAND_PANEL_Y + 25 - commandScroll.getOffset();
            for (CommandToggle toggle : commandToggles) {
                if (toggle instanceof LabeledTextInput) {
                    LabeledTextInput li = (LabeledTextInput) toggle;
                    if (li.isMouseOver(mouseX, mouseY, contentY)) { li.beginEditing(mouseX); return; }
                } else if (toggle instanceof TerminalColorInput) {
                    TerminalColorInput tci = (TerminalColorInput) toggle;
                    if (tci.isMouseOver(mouseX, mouseY, contentY)) { tci.textInput.beginEditing(mouseX, tci.x + 40); return; }
                }
                contentY += (toggle instanceof TerminalColorInput) ? 50 : 22;
            }
        }

        // If Fast Hotkey panel is open, only consume clicks inside its panel area
        if (showCommandSettings && selectedCommandModule != null && selectedCommandModule.name.equals("Fast Hotkey")) {
            int panelX = guiLeft + Dimensions.COMMAND_PANEL_X;
            int panelY = guiTop + Dimensions.COMMAND_PANEL_Y;
            int panelWidth = Dimensions.COMMAND_PANEL_WIDTH;
            int panelHeight = Dimensions.GUI_HEIGHT - 60;
            boolean insidePanel = mouseX >= panelX && mouseX <= panelX + panelWidth &&
                    mouseY >= panelY && mouseY <= panelY + panelHeight;
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
        if (showCommandSettings && selectedCommandModule != null && selectedCommandModule.name.equals("Fast Hotkey")) {
            handleFastHotKeyTyping(typedChar, keyCode);
            return;
        }
        handleColorInputTyping(typedChar, keyCode);
        // Terminal panel input typing
        if (showCommandSettings && selectedCommandModule != null && selectedCommandModule.name.equals("Dungeon Terminals")) {
            int contentY = guiTop + Dimensions.COMMAND_PANEL_Y + 25 - commandScroll.getOffset();
            for (CommandToggle toggle : commandToggles) {
                if (toggle instanceof LabeledTextInput) {
                    ((LabeledTextInput) toggle).handleKeyTyped(typedChar, keyCode);
                } else if (toggle instanceof TerminalColorInput) {
                    ((TerminalColorInput) toggle).textInput.handleKeyTyped(typedChar, keyCode);
                }
                contentY += (toggle instanceof TerminalColorInput) ? 50 : 22;
            }
        }
    }

    // Remove unused global delete key helpers (kept for reference, no longer called)
    private void handleDeleteKey(int keyCode) { /* no-op */ }
    private void handleContinuousDelete() { /* no-op */ }
    private void handleDeleteKeyAction() { /* no-op */ }
    // =============================================
    // Drawing Methods
    // =============================================
    private void drawBackground() {
        int scale = new ScaledResolution(Minecraft.getMinecraft()).getScaleFactor();
        drawRect(guiLeft, guiTop, guiLeft + Dimensions.GUI_WIDTH, guiTop + Dimensions.GUI_HEIGHT, Colors.PANEL);

        String title = "§l§nRat All Of You";
        int titleX = guiLeft + 15;
        int titleY = guiTop + 10;
        fontRendererObj.drawStringWithShadow(title, titleX, titleY, Colors.TEXT);

        drawRect(guiLeft + 5, guiTop + 25, guiLeft + 115, guiTop + Dimensions.GUI_HEIGHT - 5, Colors.CATEGORY);
        drawRect(guiLeft + 115, guiTop + 25, guiLeft + Dimensions.GUI_WIDTH - 5, guiTop + Dimensions.GUI_HEIGHT - 5, Colors.CATEGORY);

        drawCenteredString(fontRendererObj, "§7Version v1.0 §8| §7Created by AfterTime",
                width / 2, guiTop + Dimensions.GUI_HEIGHT - 20, Colors.VERSION);
    }

    private void drawCategories() {
        for (GuiButton btn : categoryButtons) {
            drawRect(btn.xPosition - 2, btn.yPosition - 2,
                    btn.xPosition + btn.width + 2, btn.yPosition + btn.height + 2,
                    Colors.CATEGORY_BUTTON);
        }
    }

    private void drawModules(int mouseX, int mouseY) {
        int scissorX = guiLeft + 115;
        int scissorY = guiTop + 25;
        int scissorWidth = Dimensions.GUI_WIDTH - 120 - Dimensions.SCROLLBAR_WIDTH;
        int scissorHeight = Dimensions.GUI_HEIGHT - 50;
        int scale = new ScaledResolution(Minecraft.getMinecraft()).getScaleFactor();

        glEnable(GL_SCISSOR_TEST);
        glScissor(scissorX * scale, (height - (scissorY + scissorHeight)) * scale,
                scissorWidth * scale, scissorHeight * scale);

        for (ModuleButton moduleBtn : moduleButtons) {
            moduleBtn.draw(mouseX, mouseY, fontRendererObj);
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
        if (!showCommandSettings || selectedCommandModule == null) return;

        int panelX = guiLeft + Dimensions.COMMAND_PANEL_X;
        int panelY = guiTop + Dimensions.COMMAND_PANEL_Y;
        int panelWidth = Dimensions.COMMAND_PANEL_WIDTH;
        int panelHeight = Dimensions.GUI_HEIGHT - 60;

        drawRect(panelX, panelY, panelX + panelWidth, panelY + panelHeight, Colors.COMMAND_PANEL);
        drawRect(panelX - 1, panelY - 1, panelX + panelWidth + 1, panelY + panelHeight + 1, Colors.COMMAND_BORDER);

        String title = getCommandPanelTitle();
        drawCenteredString(fontRendererObj, title,
                panelX + panelWidth / 2, panelY + 5, Colors.COMMAND_TEXT);

        if (selectedCommandModule.name.equals("Fast Hotkey")) {
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
        glScissor(panelX * scale,
                (height - (panelY + panelHeight)) * scale,
                panelWidth * scale,
                (panelHeight - 25) * scale);

        int contentY = panelY + 25 - commandScroll.getOffset();
        int x = panelX + 5;
        int w = panelWidth - 10;

        List<ConfigStorage.FastHotKeyEntry> entries = ConfigStorage.getFastHotKeyEntries();

        // Ensure rows match entries count
        if (fastRows.size() != entries.size()) {
            fastRows.clear();
            for (ConfigStorage.FastHotKeyEntry e : entries) fastRows.add(new FastRow(e));
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
            int inputX = x;
            int inputW = Math.max(Dimensions.FH_INPUT_MIN_WIDTH, w);
            row.labelInput.draw(inputX, labelInputY, inputW, Dimensions.FH_INPUT_HEIGHT, fontRendererObj);
            row.commandInput.draw(inputX, commandInputY, inputW, Dimensions.FH_INPUT_HEIGHT, fontRendererObj);

            // Remove button (below inputs, left-aligned)
            int removeX = inputX;
            int removeY = commandInputY + Dimensions.FH_INPUT_HEIGHT + Dimensions.FH_GAP_Y;
            boolean hoverRemove = mouseX >= removeX && mouseX <= removeX + Dimensions.FH_REMOVE_WIDTH && mouseY >= removeY && mouseY <= removeY + Dimensions.FH_REMOVE_HEIGHT;
            drawRect(removeX, removeY, removeX + Dimensions.FH_REMOVE_WIDTH, removeY + Dimensions.FH_REMOVE_HEIGHT,
                    hoverRemove ? Colors.BUTTON_RED_HOVER : Colors.BUTTON_RED);
            drawCenteredString(fontRendererObj, "Remove", removeX + Dimensions.FH_REMOVE_WIDTH / 2, removeY + 5, Colors.BUTTON_TEXT);
        }

        // Add button
        int addY = contentY + fastRows.size() * Dimensions.FH_ROW_HEIGHT + 8;
        int addX = x;
        int addW = 60;
        boolean canAdd = entries.size() < 12;
        boolean hoverAdd = mouseX >= addX && mouseX <= addX + addW && mouseY >= addY && mouseY <= addY + Dimensions.FH_ADD_HEIGHT;
        drawRect(addX, addY, addX + addW, addY + Dimensions.FH_ADD_HEIGHT,
                (hoverAdd && canAdd) ? Colors.BUTTON_GREEN_HOVER : Colors.BUTTON_GREEN);
        int addTextColor = canAdd ? Colors.BUTTON_TEXT : 0xFFDDDDDD;
        drawCenteredString(fontRendererObj, "Add", addX + addW / 2, addY + 6, addTextColor);

        glDisable(GL_SCISSOR_TEST);

        // Scrollbar
        int totalHeight = fastRows.size() * Dimensions.FH_ROW_HEIGHT + Dimensions.FH_ADD_HEIGHT + 10;
        commandScroll.update(totalHeight, panelHeight - 25);
        if (commandScroll.shouldRenderScrollbar()) {
            commandScroll.updateScrollbarPosition(
                    panelX + panelWidth - Dimensions.SCROLLBAR_WIDTH - 2,
                    panelY + 25,
                    panelHeight - 25
            );
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
        int w = panelWidth - 10;
        int contentY = panelY + 25 - commandScroll.getOffset();

        List<ConfigStorage.FastHotKeyEntry> entries = ConfigStorage.getFastHotKeyEntries();

        // Add button
        int addY = contentY + fastRows.size() * Dimensions.FH_ROW_HEIGHT + 8;
        int addX = x;
        int addW = 60;
        if (entries.size() < 12 && mouseX >= addX && mouseX <= addX + addW && mouseY >= addY && mouseY <= addY + Dimensions.FH_ADD_HEIGHT) {
            ConfigStorage.FastHotKeyEntry e = new ConfigStorage.FastHotKeyEntry("", "");
            entries.add(e);
            fastRows.add(new FastRow(e));
            ConfigStorage.saveFastHotKeyConfig();
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
                entries.remove(row.entry);
                fastRows.remove(i);
                ConfigStorage.saveFastHotKeyConfig();
                int totalHeight = fastRows.size() * Dimensions.FH_ROW_HEIGHT + Dimensions.FH_ADD_HEIGHT + 10;
                commandScroll.update(totalHeight, panelHeight - 25);
                return;
            }

            // Input clicks
            int inputX = x;
            int inputW = Math.max(Dimensions.FH_INPUT_MIN_WIDTH, w);
            int labelY = labelInputY;
            int commandY = commandInputY;

            if (row.labelInput.isMouseOver(mouseX, mouseY, inputX, labelY, inputW, Dimensions.FH_INPUT_HEIGHT)) {
                unfocusAllFastInputs();
                row.labelInput.beginEditing(mouseX, inputX);
                return;
            }
            if (row.commandInput.isMouseOver(mouseX, mouseY, inputX, commandY, inputW, Dimensions.FH_INPUT_HEIGHT)) {
                unfocusAllFastInputs();
                row.commandInput.beginEditing(mouseX, inputX);
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
        for (CommandToggle toggle : commandToggles) {
            if (toggle instanceof ColorInput) {
                ((ColorInput)toggle).unfocus();
            }
        }
        if (showCommandSettings && selectedCommandModule != null && selectedCommandModule.name.equals("Fast Hotkey")) {
            for (FastRow row : fastRows) {
                row.labelInput.isEditing = false;
                row.commandInput.isEditing = false;
            }
        }
    }

    private void handleScrollbarClicks(int mouseX, int mouseY) {
        if (showCommandSettings && commandScroll.checkScrollbarClick(mouseX, mouseY)) {
            commandScroll.beginScroll();
            // position handle immediately to click point
            commandScroll.handleDrag(mouseX, mouseY, null);
            return;
        }
        if (mainScroll.checkScrollbarClick(mouseX, mouseY)) {
            mainScroll.beginScroll();
            // update modules position on drag
            mainScroll.handleDrag(mouseX, mouseY, this::createModuleButtons);
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
        if (module.enabled && moduleBtn.isDropdownClicked(mouseX, mouseY)) {
            selectedCommandModule = module;
            showCommandSettings = true;
            initializeCommandToggles();
        } else {
            handleModuleToggle(module);
        }
    }

    private void handleCommandToggleClicks(int mouseX, int mouseY) {
        if (!showCommandSettings) return;

        // Handle dropdown clicks
        if (handleDropdownClicks(mouseX, mouseY)) return;

        // Handle color/text input clicks for Etherwarp and Terminals
        if (handleColorInputClicks(mouseX, mouseY)) return;

        // Handle regular toggle clicks
        handleRegularToggleClicks(mouseX, mouseY);
    }

    // Handle clicks on dropdowns (currently Etherwarp render method)
    private boolean handleDropdownClicks(int mouseX, int mouseY) {
        if (selectedCommandModule == null) return false;
        if (!"Etherwarp Overlay".equals(selectedCommandModule.name)) return false;
        int contentY = guiTop + Dimensions.COMMAND_PANEL_Y + 25 - commandScroll.getOffset();
        for (CommandToggle toggle : commandToggles) {
            if (toggle instanceof MethodDropdown) {
                MethodDropdown dd = (MethodDropdown) toggle;
                // Base dropdown area
                int yPos = contentY;
                boolean inBase = mouseX >= dd.x + 100 && mouseX <= dd.x + dd.width && mouseY >= yPos && mouseY <= yPos + dd.height;
                if (inBase) {
                    dd.isOpen = !dd.isOpen;
                    return true;
                }
                if (dd.isOpen) {
                    for (int i = 0; i < dd.methods.length; i++) {
                        int optionY = yPos + dd.height + (i * dd.height);
                        if (mouseX >= dd.x + 100 && mouseX <= dd.x + dd.width && mouseY >= optionY && mouseY <= optionY + dd.height) {
                            dd.selectMethod(i);
                            dd.isOpen = false;
                            return true;
                        }
                    }
                }
            }
            contentY += 22;
        }
        return false;
    }

    // Focus color/text inputs when clicked inside their input boxes
    private boolean handleColorInputClicks(int mouseX, int mouseY) {
        if (selectedCommandModule == null) return false;
        int contentY = guiTop + Dimensions.COMMAND_PANEL_Y + 25 - commandScroll.getOffset();
        for (CommandToggle toggle : commandToggles) {
            if (toggle instanceof ColorInput) {
                ColorInput ci = (ColorInput) toggle;
                int inputY = contentY + ci.height + 8;
                boolean hover = (mouseX >= ci.x + 40 && mouseX <= ci.x + ci.width && mouseY >= inputY - 2 && mouseY <= inputY + 15);
                if (hover) { ci.beginEditing(mouseX); return true; }
                contentY += 50; // ColorInput consumes extra vertical space
                continue;
            }
            if (toggle instanceof LabeledTextInput) {
                LabeledTextInput li = (LabeledTextInput) toggle;
                if (li.isMouseOver(mouseX, mouseY, contentY)) { li.beginEditing(mouseX); return true; }
                contentY += 22;
                continue;
            }
            if (toggle instanceof TerminalColorInput) {
                TerminalColorInput tci = (TerminalColorInput) toggle;
                if (tci.isMouseOver(mouseX, mouseY, contentY)) { tci.textInput.beginEditing(mouseX, tci.x + 40); return true; }
                contentY += 50; // TerminalColorInput height
                continue;
            }
            contentY += 22;
        }
        return false;
    }

    // Toggle simple checkboxes and persist settings where applicable
    private void handleRegularToggleClicks(int mouseX, int mouseY) {
        if (selectedCommandModule == null) return;
        int contentY = guiTop + Dimensions.COMMAND_PANEL_Y + 25 - commandScroll.getOffset();
        for (CommandToggle toggle : commandToggles) {
            // Skip non-simple toggles
            boolean isComplex = (toggle instanceof MethodDropdown) || (toggle instanceof ColorInput) || (toggle instanceof LabeledTextInput) || (toggle instanceof TerminalColorInput);
            if (!isComplex && toggle.isMouseOver(mouseX, mouseY, contentY)) {
                toggle.toggle();
                // Persist per panel
                if ("Dungeon Terminals".equals(selectedCommandModule.name)) {
                    updateTerminalConfig();
                } else if ("Etherwarp Overlay".equals(selectedCommandModule.name)) {
                    // Etherwarp toggles mapped directly from config list; save config
                    ConfigStorage.saveEtherwarpConfig();
                } else if ("Party Commands".equals(selectedCommandModule.name)) {
                    ConfigStorage.saveCommandsConfig();
                } else if ("No Debuff".equals(selectedCommandModule.name)) {
                    ConfigStorage.saveNoDebuffConfig();
                    // Optionally apply to runtime
                    boolean fire = false, blind = false, liquid = false;
                    for (ConfigStorage.NoDebuffConfig cfg : ConfigStorage.getNoDebuffConfigs()) {
                        if ("Remove Fire Overlay".equals(cfg.name)) fire = cfg.enabled;
                        else if ("Ignore Blindness".equals(cfg.name)) blind = cfg.enabled;
                        else if ("Clear Liquid Vision".equals(cfg.name)) liquid = cfg.enabled;
                    }
                    NoDebuff.setNoFire(fire);
                    NoDebuff.setNoBlindness(blind);
                    NoDebuff.setClearLiquidVision(liquid);
                }
                return;
            }
            // Advance Y per control type
            if (toggle instanceof ColorInput || toggle instanceof TerminalColorInput) contentY += 50; else contentY += 22;
        }
    }

    // =============================
    // Terminal settings inputs
    // =============================
    private class LabeledTextInput extends CommandToggle {
        private final TextInput textInput;
        private final ValueChange onCommit;
        LabeledTextInput(String label, String initial, int x, int y, int width, int height, ValueChange onCommit) {
            super(label, "", false, x, y, width, height);
            this.onCommit = onCommit;
            this.textInput = new TextInput(initial, true, (v) -> {});
        }
        boolean isMouseOver(int mouseX, int mouseY, int yPos) {
            int inputY = yPos; // we place input on same line for simplicity
            return mouseX >= x && mouseX <= x + width && mouseY >= inputY && mouseY <= inputY + height;
        }
        void beginEditing(int mouseX) { textInput.beginEditing(mouseX, x + 45); }
        void handleKeyTyped(char typedChar, int keyCode) {
            int beforeLen = textInput.value.length();
            textInput.handleKeyTyped(typedChar, keyCode);
            // On enter commit
            if (!textInput.isEditing || (keyCode == Keyboard.KEY_RETURN)) {
                if (onCommit != null) onCommit.onChange(textInput.value);
            } else if (textInput.value.length() != beforeLen) {
                // Also commit live on change for immediate preview
                if (onCommit != null) onCommit.onChange(textInput.value);
            }
        }
        @Override
        void draw(int mouseX, int mouseY, int yPos, FontRenderer fr) {
            fr.drawStringWithShadow(name, x, yPos + 5, Colors.COMMAND_TEXT);
            textInput.draw(x + 45, yPos + 2, width - 50, 16, fr);
        }
    }

    private class TerminalColorInput extends CommandToggle {
        private Color color;
        private final String title;
        private final boolean isOverlay;
        private final TextInput textInput;
        TerminalColorInput(String title, Color color, int x, int y, int width, int height, boolean isOverlay) {
            super(title, "", false, x, y, width, height);
            this.color = color;
            this.title = title;
            this.isOverlay = isOverlay;
            String initial = color.getRed() + "," + color.getGreen() + "," + color.getBlue() + "," + color.getAlpha();
            this.textInput = new TextInput(initial, true, (v) -> updateColor(v));
        }
        boolean isMouseOver(int mouseX, int mouseY, int yPos) {
            int inputY = yPos + height + 8;
            return (mouseX >= x + 40 && mouseX <= x + width && mouseY >= inputY - 2 && mouseY <= inputY + 15);
        }
        @Override
        void draw(int mouseX, int mouseY, int yPos, FontRenderer fr) {
            fr.drawStringWithShadow(title, x, yPos + 5, Colors.COMMAND_TEXT);
            drawRect(x + fr.getStringWidth(title) + 10, yPos + 3, x + fr.getStringWidth(title) + 30, yPos + height - 3, color.getRGB());
            int inputY = yPos + height + 8;
            fr.drawStringWithShadow("RGBA:", x, inputY, Colors.COMMAND_TEXT);
            textInput.draw(x + 40, inputY - 2, width - 40, 16, fr);
        }
        void handleKeyTyped(char typedChar, int keyCode) { textInput.handleKeyTyped(typedChar, keyCode); }
        private void updateColor(String value) {
            String[] parts = value.split(",");
            if (parts.length != 4) return;
            try {
                int r = Math.min(255, Math.max(0, Integer.parseInt(parts[0])));
                int g = Math.min(255, Math.max(0, Integer.parseInt(parts[1])));
                int b = Math.min(255, Math.max(0, Integer.parseInt(parts[2])));
                int a = Math.min(255, Math.max(0, Integer.parseInt(parts[3])));
                this.color = new Color(r, g, b, a);
                // Persist to config and apply
                ConfigStorage.TerminalSettings ts = ConfigStorage.getTerminalSettings();
                if (isOverlay) ts.overlayColor = this.color; else ts.backgroundColor = this.color;
                ConfigStorage.saveTerminalConfig();
                ConfigStorage.applyTerminalSettingsToRuntime();
            } catch (Exception ignored) {}
        }
    }

    // Generic text input for Fast Hotkey rows
    private class TextInput {
        String value;
        boolean isEditing = false;
        int cursorPosition = 0;
        long cursorBlinkTimer = 0;
        boolean cursorVisible = false;
        int maxLen = 64;
        final boolean allowSpaces;
        final ValueChange onChange;

        TextInput(String initial, boolean allowSpaces, ValueChange onChange) {
            this.value = initial == null ? "" : initial;
            this.cursorPosition = this.value.length();
            this.allowSpaces = allowSpaces;
            this.onChange = onChange;
        }

        void draw(int x, int y, int w, int h, FontRenderer fr) {
            drawRect(x, y, x + w, y + h, Colors.INPUT_BG);
            String display = value;
            fr.drawStringWithShadow(display, x + 3, y + 4, Colors.INPUT_FG);

            if (isEditing) {
                cursorBlinkTimer += 10;
                if (cursorBlinkTimer >= 1000) {
                    cursorBlinkTimer = 0;
                    cursorVisible = !cursorVisible;
                }
                if (cursorVisible) {
                    int cx = x + 3 + fr.getStringWidth(display.substring(0, Math.min(cursorPosition, display.length())));
                    drawRect(cx, y + 3, cx + 1, y + h - 3, Colors.INPUT_FG);
                }
            }
        }

        boolean isMouseOver(int mouseX, int mouseY, int x, int y, int w, int h) {
            return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
        }

        void beginEditing(int mouseX, int textStartX) {
            isEditing = true;
            cursorBlinkTimer = 0;
            cursorVisible = true;
            // place cursor based on click x
            int rel = Math.max(0, mouseX - (textStartX + 3));
            int pos = 0;
            while (pos < value.length()) {
                int cw = fontRendererObj.getCharWidth(value.charAt(pos));
                if (rel < cw / 2) break;
                rel -= cw;
                pos++;
            }
            cursorPosition = pos;
        }

        void handleKeyTyped(char typedChar, int keyCode) {
            if (!isEditing) return;
            if (keyCode == Keyboard.KEY_RETURN) {
                isEditing = false;
                return;
            }
            if (keyCode == Keyboard.KEY_BACK) {
                if (cursorPosition > 0 && value.length() > 0) {
                    value = value.substring(0, cursorPosition - 1) + value.substring(cursorPosition);
                    cursorPosition--;
                    onChange.onChange(value);
                }
            } else if (keyCode == Keyboard.KEY_LEFT) {
                cursorPosition = Math.max(0, cursorPosition - 1);
            } else if (keyCode == Keyboard.KEY_RIGHT) {
                cursorPosition = Math.min(value.length(), cursorPosition + 1);
            } else {
                // Accept printable characters
                if (typedChar >= 32 && typedChar != 127) {
                    if (!allowSpaces && typedChar == ' ') return;
                    if (value.length() >= maxLen) return;
                    value = value.substring(0, cursorPosition) + typedChar + value.substring(cursorPosition);
                    cursorPosition++;
                    onChange.onChange(value);
                }
            }
            cursorBlinkTimer = 0;
            cursorVisible = true;
        }
    }

    private interface ValueChange { void onChange(String newValue); }

    private class FastRow {
        final ConfigStorage.FastHotKeyEntry entry;
        final TextInput labelInput;
        final TextInput commandInput;
        FastRow(ConfigStorage.FastHotKeyEntry entry) {
            this.entry = entry;
            this.labelInput = new TextInput(entry.label, true, (v) -> {
                entry.label = v;
                ConfigStorage.saveFastHotKeyConfig();
            });
            this.commandInput = new TextInput(entry.command, true, (v) -> {
                entry.command = v;
                ConfigStorage.saveFastHotKeyConfig();
            });
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int dWheel = Mouse.getEventDWheel();
        if (dWheel == 0) return;

        // Translate raw mouse to scaled GUI coords
        int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
        int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;

        // If hovering the command panel content area, scroll command panel
        if (showCommandSettings && selectedCommandModule != null) {
            int panelX = guiLeft + Dimensions.COMMAND_PANEL_X;
            int panelY = guiTop + Dimensions.COMMAND_PANEL_Y + 25; // content starts below title
            int panelWidth = Dimensions.COMMAND_PANEL_WIDTH;
            int panelHeight = (Dimensions.GUI_HEIGHT - 60) - 25;
            if (mouseX >= panelX && mouseX <= panelX + panelWidth &&
                    mouseY >= panelY && mouseY <= panelY + panelHeight) {
                commandScroll.handleWheelScroll(dWheel);
                return;
            }
        }

        // Otherwise, if hovering the modules list area, scroll main list
        int listX = guiLeft + 115;
        int listY = guiTop + 25;
        int listWidth = Dimensions.GUI_WIDTH - 120 - Dimensions.SCROLLBAR_WIDTH;
        int listHeight = Dimensions.GUI_HEIGHT - 50;
        if (mouseX >= listX && mouseX <= listX + listWidth &&
                mouseY >= listY && mouseY <= listY + listHeight) {
            mainScroll.handleWheelScroll(dWheel, this::createModuleButtons);
        }
    }

    // =============================================
    // Builders and helpers we were missing
    // =============================================
    private void createCategoryButtons() {
        categoryButtons.clear();
        buttonList.clear();
        List<String> cats = ConfigStorage.getUniqueCategories();
        int y = guiTop + 30;
        int x = guiLeft + 10;
        for (int i = 0; i < cats.size(); i++) {
            String c = cats.get(i);
            GuiButton b = new GuiButton(1000 + i, x, y, 95, 18, c);
            categoryButtons.add(b);
            buttonList.add(b);
            y += 20;
        }
        if (!cats.contains(selectedCategory) && !cats.isEmpty()) selectedCategory = cats.get(0);
    }

    private void createModuleButtons() {
        moduleButtons.clear();
        List<ConfigStorage.ModuleInfo> mods = ConfigStorage.getModulesByCategory(selectedCategory);
        int listX = guiLeft + 120;
        int listY = guiTop + 28;
        int listW = Dimensions.GUI_WIDTH - 120 - 10 - Dimensions.SCROLLBAR_WIDTH;
        int y = listY - mainScroll.getOffset();
        int rowH = 22;
        for (ConfigStorage.ModuleInfo mi : mods) {
            moduleButtons.add(new ModuleButton(listX + 4, y, listW - 8, rowH - 2, mi));
            y += rowH;
        }
        int totalHeight = mods.size() * rowH;
        int viewH = Dimensions.GUI_HEIGHT - 50;
        mainScroll.update(totalHeight, viewH);
        mainScroll.updateScrollbarPosition(listX + listW - 2, listY, viewH);
    }

    private void initializeCommandToggles() {
        commandToggles.clear();
        if (selectedCommandModule == null) return;
        int panelX = guiLeft + Dimensions.COMMAND_PANEL_X + 5;
        int y = guiTop + Dimensions.COMMAND_PANEL_Y + 30; // content start (without scroll)
        int w = Dimensions.COMMAND_PANEL_WIDTH - 10;

        String name = selectedCommandModule.name;
        if ("Dungeon Terminals".equals(name)) {
            ConfigStorage.TerminalSettings ts = ConfigStorage.getTerminalSettings();
            commandToggles.add(new CommandToggle("High Ping Mode", "Queues clicks for high latency", ts.highPingMode, panelX, y, w, 16)); y += 22;
            commandToggles.add(new CommandToggle("Phoenix Client Compat", "Alternative click timing", ts.phoenixClientCompat, panelX, y, w, 16)); y += 22;
            commandToggles.add(new CommandToggle("Enable Starts With GUI", "Use custom GUI for Starts With", ts.enableStartsWith, panelX, y, w, 16)); y += 22;
            commandToggles.add(new CommandToggle("Enable Colors GUI", "Use custom GUI for Colors", ts.enableColors, panelX, y, w, 16)); y += 22;

            // Scale and timings / offsets
            commandToggles.add(new LabeledTextInput("Scale", String.valueOf(ts.scale), panelX, y, w, 16, (v) -> {
                try { ts.scale = Math.max(0.25f, Math.min(4f, Float.parseFloat(v))); } catch (Exception ignored) {}
                ConfigStorage.saveTerminalConfig();
                ConfigStorage.applyTerminalSettingsToRuntime();
            })); y += 22;
            commandToggles.add(new LabeledTextInput("Timeout", String.valueOf(ts.timeoutMs), panelX, y, w, 16, (v) -> {
                try { ts.timeoutMs = Math.max(0, Integer.parseInt(v)); } catch (Exception ignored) {}
                ConfigStorage.saveTerminalConfig();
                ConfigStorage.applyTerminalSettingsToRuntime();
            })); y += 22;
            commandToggles.add(new LabeledTextInput("FirstClick", String.valueOf(ts.firstClickMs), panelX, y, w, 16, (v) -> {
                try { ts.firstClickMs = Math.max(0, Integer.parseInt(v)); } catch (Exception ignored) {}
                ConfigStorage.saveTerminalConfig();
                ConfigStorage.applyTerminalSettingsToRuntime();
            })); y += 22;
            commandToggles.add(new LabeledTextInput("OffsetX", String.valueOf(ts.offsetX), panelX, y, w, 16, (v) -> {
                try { ts.offsetX = Integer.parseInt(v); } catch (Exception ignored) {}
                ConfigStorage.saveTerminalConfig();
                ConfigStorage.applyTerminalSettingsToRuntime();
            })); y += 22;
            commandToggles.add(new LabeledTextInput("OffsetY", String.valueOf(ts.offsetY), panelX, y, w, 16, (v) -> {
                try { ts.offsetY = Integer.parseInt(v); } catch (Exception ignored) {}
                ConfigStorage.saveTerminalConfig();
                ConfigStorage.applyTerminalSettingsToRuntime();
            })); y += 22;

            // Colors
            commandToggles.add(new TerminalColorInput("Overlay", ts.overlayColor, panelX, y, w, 18, true)); y += 50;
            commandToggles.add(new TerminalColorInput("Background", ts.backgroundColor, panelX, y, w, 18, false)); y += 50;
        } else if ("Party Commands".equals(name)) {
            for (ConfigStorage.CommandConfig cc : ConfigStorage.getCommandConfigs()) {
                commandToggles.add(new CommandToggle(cc.name, cc.description, cc.enabled, panelX, y, w, 16)); y += 22;
            }
        } else if ("No Debuff".equals(name)) {
            for (ConfigStorage.NoDebuffConfig nc : ConfigStorage.getNoDebuffConfigs()) {
                commandToggles.add(new CommandToggle(nc.name, nc.description, nc.enabled, panelX, y, w, 16)); y += 22;
            }
        } else if ("Etherwarp Overlay".equals(name)) {
            for (ConfigStorage.EtherwarpConfig ec : ConfigStorage.getEtherwarpConfigs()) {
                if ("Render Method".equals(ec.name)) {
                    commandToggles.add(new MethodDropdown("Render Method", panelX, y, w, 16)); y += 22;
                } else {
                    commandToggles.add(new CommandToggle(ec.name, ec.description, ec.enabled, panelX, y, w, 16)); y += 22;
                }
            }
            // Color editors for Etherwarp
            commandToggles.add(new ColorInput("Overlay RGBA", EtherwarpOverlay.etherwarpOverlayColor, panelX, y, w, 18, (c) -> {
                EtherwarpOverlay.etherwarpOverlayColor = c; ConfigStorage.saveEtherwarpConfig();
            })); y += 50;
            commandToggles.add(new ColorInput("Fail RGBA", EtherwarpOverlay.etherwarpOverlayFailColor, panelX, y, w, 18, (c) -> {
                EtherwarpOverlay.etherwarpOverlayFailColor = c; ConfigStorage.saveEtherwarpConfig();
            })); y += 50;
        }
        int panelHeight = Dimensions.GUI_HEIGHT - 60 - 25; // view height
        int totalHeight = (y - (guiTop + Dimensions.COMMAND_PANEL_Y + 30));
        commandScroll.update(totalHeight, panelHeight);
        commandScroll.updateScrollbarPosition(guiLeft + Dimensions.COMMAND_PANEL_X + Dimensions.COMMAND_PANEL_WIDTH - Dimensions.SCROLLBAR_WIDTH - 2,
                guiTop + Dimensions.COMMAND_PANEL_Y + 25,
                panelHeight);
    }

    private String getCommandPanelTitle() {
        return selectedCommandModule == null ? "" : ("Settings - " + selectedCommandModule.name);
    }

    private void drawCommandPanelContent(int mouseX, int mouseY, int panelX, int panelY, int panelWidth, int panelHeight) {
        int scale = new ScaledResolution(Minecraft.getMinecraft()).getScaleFactor();
        glEnable(GL_SCISSOR_TEST);
        glScissor(panelX * scale, (height - (panelY + panelHeight)) * scale, panelWidth * scale, (panelHeight - 25) * scale);

        int y = panelY + 30 - commandScroll.getOffset();
        for (CommandToggle t : commandToggles) {
            t.draw(mouseX, mouseY, y, fontRendererObj);
            y += (t instanceof ColorInput || t instanceof TerminalColorInput) ? 50 : 22;
        }
        glDisable(GL_SCISSOR_TEST);

        int totalHeight = 0;
        for (CommandToggle t : commandToggles) totalHeight += (t instanceof ColorInput || t instanceof TerminalColorInput) ? 50 : 22;
        commandScroll.update(totalHeight, panelHeight - 25);
        commandScroll.updateScrollbarPosition(panelX + panelWidth - Dimensions.SCROLLBAR_WIDTH - 2, panelY + 25, panelHeight - 25);
    }

    private void handleScrollbarDrag(int mouseX, int mouseY) {
        if (mainScroll.isDragging) mainScroll.handleDrag(mouseX, mouseY, this::createModuleButtons);
        if (commandScroll.isDragging) commandScroll.handleDrag(mouseX, mouseY, null);
    }

    private void handleColorInputTyping(char typedChar, int keyCode) {
        if (!showCommandSettings) return;
        for (CommandToggle t : commandToggles) {
            if (t instanceof ColorInput) ((ColorInput) t).handleKeyTyped(typedChar, keyCode);
            if (t instanceof TerminalColorInput) ((TerminalColorInput) t).handleKeyTyped(typedChar, keyCode);
        }
    }

    private void updateTerminalConfig() {
        if (selectedCommandModule == null || !"Dungeon Terminals".equals(selectedCommandModule.name)) return;
        ConfigStorage.TerminalSettings ts = ConfigStorage.getTerminalSettings();
        for (CommandToggle t : commandToggles) {
            if ("High Ping Mode".equals(t.name)) ts.highPingMode = t.enabled;
            else if ("Phoenix Client Compat".equals(t.name)) ts.phoenixClientCompat = t.enabled;
            else if ("Enable Starts With GUI".equals(t.name)) ts.enableStartsWith = t.enabled;
            else if ("Enable Colors GUI".equals(t.name)) ts.enableColors = t.enabled;
        }
        ConfigStorage.saveTerminalConfig();
        // Apply runtime + enable/disable modules
        ConfigStorage.applyTerminalSettingsToRuntime();
        com.aftertime.ratallofyou.modules.dungeon.terminals.startswith.setEnabled(ts.enableStartsWith);
        com.aftertime.ratallofyou.modules.dungeon.terminals.Colors.setEnabled(ts.enableColors);
    }

    private void handleModuleToggle(ConfigStorage.ModuleInfo module) {
        module.enabled = !module.enabled;
        ConfigStorage.saveMainConfig();
        if ("No Debuff".equals(module.name)) {
            NoDebuff.setEnabled(module.enabled);
        } else if ("Dungeon Terminals".equals(module.name)) {
            // When module toggled, enable/disable both helpers according to per-terminal toggles
            ConfigStorage.TerminalSettings ts = ConfigStorage.getTerminalSettings();
            com.aftertime.ratallofyou.modules.dungeon.terminals.startswith.setEnabled(module.enabled && ts.enableStartsWith);
            com.aftertime.ratallofyou.modules.dungeon.terminals.Colors.setEnabled(module.enabled && ts.enableColors);
        }
        createModuleButtons();
    }

    // =============================================
    // Inner UI model classes
    // =============================================
    private class CommandToggle {
        final String name;
        final String description;
        boolean enabled;
        final int x, y, width, height;
        CommandToggle(String name, String description, boolean enabled, int x, int y, int width, int height) {
            this.name = name; this.description = description; this.enabled = enabled;
            this.x = x; this.y = y; this.width = width; this.height = height;
        }
        void draw(int mouseX, int mouseY, int yPos, FontRenderer fr) {
            int box = 10;
            int cx = x; int cy = yPos + 3;
            drawRect(cx, cy, cx + box, cy + box, enabled ? Colors.BUTTON_GREEN : Colors.CATEGORY_BUTTON);
            fr.drawStringWithShadow(name, x + 14, yPos + 3, Colors.COMMAND_TEXT);
        }
        boolean isMouseOver(int mouseX, int mouseY, int yPos) {
            return mouseX >= x && mouseX <= x + width && mouseY >= yPos && mouseY <= yPos + height;
        }
        void toggle() { enabled = !enabled; }
    }

    private class ColorInput extends CommandToggle {
        private Color color;
        private final TextInput textInput;
        private final java.util.function.Consumer<Color> onCommit;
        ColorInput(String title, Color initial, int x, int y, int width, int height, java.util.function.Consumer<Color> onCommit) {
            super(title, "", false, x, y, width, height);
            this.color = initial;
            this.onCommit = onCommit;
            String s = color.getRed()+","+color.getGreen()+","+color.getBlue()+","+color.getAlpha();
            this.textInput = new TextInput(s, true, (v) -> updateColor(v));
        }
        void beginEditing(int mouseX) { textInput.beginEditing(mouseX, x + 40); }
        void unfocus() { textInput.isEditing = false; }
        @Override
        void draw(int mouseX, int mouseY, int yPos, FontRenderer fr) {
            fr.drawStringWithShadow(name, x, yPos + 5, Colors.COMMAND_TEXT);
            drawRect(x + fr.getStringWidth(name) + 10, yPos + 3, x + fr.getStringWidth(name) + 30, yPos + height - 3, color.getRGB());
            int inputY = yPos + height + 8;
            fr.drawStringWithShadow("RGBA:", x, inputY, Colors.COMMAND_TEXT);
            textInput.draw(x + 40, inputY - 2, width - 40, 16, fr);
        }
        void handleKeyTyped(char typedChar, int keyCode) { textInput.handleKeyTyped(typedChar, keyCode); }
        private void updateColor(String value) {
            String[] parts = value.split(",");
            if (parts.length != 4) return;
            try {
                int r = Math.min(255, Math.max(0, Integer.parseInt(parts[0])));
                int g = Math.min(255, Math.max(0, Integer.parseInt(parts[1])));
                int b = Math.min(255, Math.max(0, Integer.parseInt(parts[2])));
                int a = Math.min(255, Math.max(0, Integer.parseInt(parts[3])));
                this.color = new Color(r,g,b,a);
                if (onCommit != null) onCommit.accept(this.color);
            } catch (Exception ignored) {}
        }
    }

    private class MethodDropdown extends CommandToggle {
        final String[] methods = new String[]{"Edges", "Filled", "Both"};
        int selected = Math.max(0, Math.min(2, EtherwarpOverlay.etherwarpHighlightType / 2));
        boolean isOpen = false;
        MethodDropdown(String title, int x, int y, int width, int height) { super(title, "", false, x, y, width, height); }
        void selectMethod(int idx) {
            selected = Math.max(0, Math.min(2, idx));
            EtherwarpOverlay.etherwarpHighlightType = selected * 2;
            ConfigStorage.saveEtherwarpConfig();
        }
        @Override
        void draw(int mouseX, int mouseY, int yPos, FontRenderer fr) {
            fr.drawStringWithShadow(name + ":", x, yPos + 4, Colors.COMMAND_TEXT);
            int bx = x + 100; int by = yPos; int bw = width - 100; int bh = height;
            drawRect(bx, by, bx + bw, by + bh, Colors.CATEGORY_BUTTON);
            String label = methods[selected];
            fr.drawStringWithShadow(label, bx + 4, by + 4, Colors.COMMAND_TEXT);
            if (isOpen) {
                for (int i = 0; i < methods.length; i++) {
                    int oy = by + bh + i * bh;
                    drawRect(bx, oy, bx + bw, oy + bh, 0xFF1E1E1E);
                    fr.drawStringWithShadow(methods[i], bx + 4, oy + 4, Colors.COMMAND_TEXT);
                }
            }
        }
    }

    private class ModuleButton {
        final int x, y, width, height;
        final ConfigStorage.ModuleInfo module;
        ModuleButton(int x, int y, int width, int height, ConfigStorage.ModuleInfo module) { this.x=x; this.y=y; this.width=width; this.height=height; this.module=module; }
        void draw(int mouseX, int mouseY, FontRenderer fr) {
            int bg = module.enabled ? 0xFF2E7D32 : 0xFF333333;
            drawRect(x, y, x + width, y + height, bg);
            fr.drawStringWithShadow(module.name, x + 6, y + 7, Colors.TEXT);
            // settings dropdown area (right side ellipsis)
            fr.drawStringWithShadow("...", x + width - 12, y + 7, Colors.TEXT);
        }
        boolean isMouseOver(int mx, int my) { return mx >= x && mx <= x + width && my >= y && my <= y + height; }
        boolean isDropdownClicked(int mx, int my) { return isMouseOver(mx, my) && mx >= x + width - 18; }
        ConfigStorage.ModuleInfo getModule() { return module; }
    }

    private class ScrollManager {
        private int contentHeight;
        private int viewHeight;
        private int offset;
        private int barX, barY, barH;
        private int handleY, handleH;
        private int dragStartY, dragStartOffset;
        boolean isDragging = false;

        void reset() { contentHeight = 0; viewHeight = 0; offset = 0; isDragging = false; }
        int getOffset() { return Math.max(0, Math.min(offset, Math.max(0, contentHeight - viewHeight))); }
        void update(int total, int view) {
            contentHeight = Math.max(0, total);
            viewHeight = Math.max(0, view);
            int maxOffset = Math.max(0, contentHeight - viewHeight);
            if (offset > maxOffset) offset = maxOffset;
            recalcHandle();
        }
        void updateScrollbarPosition(int x, int y, int h) { barX = x; barY = y; barH = h; recalcHandle(); }
        boolean shouldRenderScrollbar() { return contentHeight > viewHeight && viewHeight > 0; }
        void drawScrollbar(int trackColor, int handleColor) {
            if (!shouldRenderScrollbar()) return;
            drawRect(barX, barY, barX + Dimensions.SCROLLBAR_WIDTH, barY + barH, trackColor);
            drawRect(barX, handleY, barX + Dimensions.SCROLLBAR_WIDTH, handleY + handleH, handleColor);
        }
        boolean checkScrollbarClick(int mx, int my) {
            if (!shouldRenderScrollbar()) return false;
            boolean inside = mx >= barX && mx <= barX + Dimensions.SCROLLBAR_WIDTH && my >= barY && my <= barY + barH;
            if (inside) { isDragging = true; dragStartY = my; dragStartOffset = offset; }
            return inside;
        }
        void beginScroll() { isDragging = true; dragStartY = -1; dragStartOffset = offset; }
        void endScroll() { isDragging = false; }
        void handleDrag(int mx, int my, Runnable onChange) {
            if (!isDragging || !shouldRenderScrollbar()) return;
            if (handleH >= barH) { offset = 0; if (onChange!=null) onChange.run(); return; }
            float ratio = (float)(contentHeight - viewHeight) / (float)(barH - handleH);
            int dy = my - (dragStartY == -1 ? my : dragStartY);
            offset = Math.max(0, Math.min(contentHeight - viewHeight, dragStartOffset + Math.round(dy * ratio)));
            recalcHandle();
            if (onChange != null) onChange.run();
        }
        void handleWheelScroll(int dWheel) { handleWheelScroll(dWheel, null); }
        void handleWheelScroll(int dWheel, Runnable onChange) {
            int step = 20 * (dWheel < 0 ? 1 : -1);
            offset = Math.max(0, Math.min(contentHeight - viewHeight, offset + step));
            recalcHandle();
            if (onChange != null) onChange.run();
        }
        private void recalcHandle() {
            if (!shouldRenderScrollbar()) { handleY = barY; handleH = barH; return; }
            int minH = Math.max(Dimensions.MIN_SCROLLBAR_HEIGHT, barH * Math.max(1, viewHeight) / Math.max(1, contentHeight));
            handleH = Math.max(Dimensions.MIN_SCROLLBAR_HEIGHT, minH);
            float t = (contentHeight - viewHeight) == 0 ? 0f : (float) offset / (float) (contentHeight - viewHeight);
            handleY = barY + Math.round((barH - handleH) * t);
        }
    }
}
