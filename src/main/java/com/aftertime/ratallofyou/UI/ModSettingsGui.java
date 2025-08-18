package com.aftertime.ratallofyou.UI;

import com.aftertime.ratallofyou.UI.config.ConfigStorage;
import com.aftertime.ratallofyou.modules.render.EtherwarpOverlay;
import com.aftertime.ratallofyou.modules.render.NoDebuff;
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
        handleDeleteKey(keyCode);
        handleColorInputTyping(typedChar, keyCode);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        handleContinuousDelete();
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        handleMouseWheelScroll();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

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
    // Input Handling Methods
    // =============================================
    private void handleInputFieldEditingState() {
        for (CommandToggle toggle : commandToggles) {
            if (toggle instanceof ColorInput) {
                ((ColorInput)toggle).isEditing = false;
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

    private void handleCommandToggleClicks(int mouseX, int mouseY) {
        if (!showCommandSettings) return;

        // Handle dropdown clicks
        if (handleDropdownClicks(mouseX, mouseY)) return;

        // Handle color input clicks
        if (handleColorInputClicks(mouseX, mouseY)) return;

        // Handle regular toggle clicks
        handleRegularToggleClicks(mouseX, mouseY);
    }

    private void handleScrollbarDrag(int mouseX, int mouseY) {
        mainScroll.handleDrag(mouseX, mouseY, this::createModuleButtons);
        commandScroll.handleDrag(mouseX, mouseY, null);
    }

    private void handleDeleteKey(int keyCode) {
        if (keyCode == Keyboard.KEY_BACK) {
            if (!deleteKeyHeld) {
                deleteKeyHeld = true;
                lastDeleteTime = System.currentTimeMillis();
                handleDeleteKeyAction();
            }
        } else {
            deleteKeyHeld = false;
        }
    }

    private void handleColorInputTyping(char typedChar, int keyCode) {
        if (showCommandSettings && selectedCommandModule != null &&
                selectedCommandModule.name.equals("Etherwarp Overlay")) {
            for (CommandToggle toggle : commandToggles) {
                if (toggle instanceof ColorInput && ((ColorInput)toggle).isEditing) {
                    ((ColorInput)toggle).handleKeyTyped(typedChar, keyCode);
                    return;
                }
            }
        }
    }

    private void handleContinuousDelete() {
        if (deleteKeyHeld) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastDeleteTime > 500) {
                if (currentTime - lastDeleteTime > 50) {
                    handleDeleteKeyAction();
                    lastDeleteTime = currentTime;
                }
            }
        }
    }

    private void handleMouseWheelScroll() {
        int scroll = Mouse.getEventDWheel();
        if (showCommandSettings) {
            commandScroll.handleWheelScroll(scroll);
        } else {
            mainScroll.handleWheelScroll(scroll, this::createModuleButtons);
        }
    }

    // =============================================
    // Module Management Methods
    // =============================================
    private void createCategoryButtons() {
        int categoryY = guiTop + 30;
        for (String category : getUniqueCategories()) {
            CategoryButton btn = new CategoryButton(category, guiLeft + 10, categoryY);
            categoryButtons.add(btn);
            this.buttonList.add(btn);
            categoryY += 25;
        }
    }

    private List<String> getUniqueCategories() {
        List<String> categories = new ArrayList<String>();
        for (ModuleInfo module : ConfigStorage.MODULES) {
            if (!categories.contains(module.category)) {
                categories.add(module.category);
            }
        }
        return categories;
    }

    private void createModuleButtons() {
        moduleButtons.clear();
        int moduleX = guiLeft + Dimensions.MODULE_LIST_X;
        int moduleY = guiTop + 30;
        int moduleWidth = Dimensions.MODULE_LIST_WIDTH;
        int moduleHeight = 50;
        int modulesAreaHeight = Dimensions.GUI_HEIGHT - 60;

        List<ModuleInfo> filteredModules = getFilteredModules();
        int totalModulesHeight = filteredModules.size() * (moduleHeight + 10);
        mainScroll.update(totalModulesHeight, modulesAreaHeight);

        int currentY = moduleY - mainScroll.getOffset();
        for (ModuleInfo module : filteredModules) {
            if (currentY + moduleHeight > guiTop + 30 && currentY < guiTop + Dimensions.GUI_HEIGHT - 30) {
                moduleButtons.add(new ModuleButton(module, moduleX, currentY, moduleWidth, moduleHeight));
            }
            currentY += moduleHeight + 10;
        }

        mainScroll.updateScrollbarPosition(
                guiLeft + Dimensions.MODULE_LIST_X + Dimensions.MODULE_LIST_WIDTH + 5,
                guiTop + 30,
                modulesAreaHeight
        );
    }

    private List<ModuleInfo> getFilteredModules() {
        List<ModuleInfo> filteredModules = new ArrayList<ModuleInfo>();
        for (ModuleInfo module : ConfigStorage.MODULES) {
            if (module.category.equals(selectedCategory)) {
                filteredModules.add(module);
                if (module.enabled && (module.name.equals("Party Commands") || module.name.equals("No Debuff") || module.name.equals("Etherwarp Overlay") || module.name.equals("Fast Hotkey"))) {
                    selectedCommandModule = module;
                }
            }
        }
        return filteredModules;
    }

    private void handleModuleToggle(ModuleInfo module) {
        module.enabled = !module.enabled;
        ConfigStorage.saveMainConfig();

        if (module.name.equals("No Debuff")) {
            NoDebuff.setEnabled(module.enabled);
            if (!module.enabled) {
                NoDebuff.loadConfig();
            } else {
                showCommandSettings = false;
                selectedCommandModule = null;
            }
        } else if (module.name.equals("Party Commands")) {
            if (!module.enabled) {
                showCommandSettings = false;
                selectedCommandModule = null;
            }
        } else {
            showCommandSettings = false;
        }

        createModuleButtons();
    }

    private void handleModuleButtonClick(ModuleButton moduleBtn, int mouseX, int mouseY) {
        if (moduleBtn.isMoveGuiButton()) {
            UIHighlighter.enterMoveMode(this);
        } else if (moduleBtn.isDropdownClicked(mouseX, mouseY)) {
            handleDropdownClick(moduleBtn);
        } else {
            handleModuleToggle(moduleBtn.getModule());
        }
    }

    private void handleDropdownClick(ModuleButton moduleBtn) {
        if (selectedCommandModule == moduleBtn.getModule() && showCommandSettings) {
            showCommandSettings = false;
        } else {
            selectedCommandModule = moduleBtn.getModule();
            initializeCommandToggles();
            showCommandSettings = true;
        }
    }

    // =============================================
    // Command Panel Methods
    // =============================================
    private void initializeCommandToggles() {
        if (selectedCommandModule == null) {
            showCommandSettings = false;
            return;
        }

        commandToggles.clear();
        fastRows.clear();

        switch (selectedCommandModule.name) {
            case "Party Commands":
                initCommandToggles();
                break;
            case "No Debuff":
                initNoDebuffToggles();
                break;
            case "Etherwarp Overlay":
                initEtherwarpToggles();
                break;
            case "Fast Hotkey":
                initFastHotkeyRows();
                break;
            default:
                showCommandSettings = false;
        }
    }

    private void initCommandToggles() {
        int panelX = guiLeft + Dimensions.COMMAND_PANEL_X;
        int panelY = guiTop + Dimensions.COMMAND_PANEL_Y;
        int panelWidth = Dimensions.COMMAND_PANEL_WIDTH;
        int panelHeight = Dimensions.GUI_HEIGHT - 60;

        int commandX = panelX + 5;
        int commandY = panelY + 25;
        int commandWidth = panelWidth - 10;

        for (ConfigStorage.CommandConfig config : ConfigStorage.getCommandConfigs()) {
            commandToggles.add(new CommandToggle(
                    config.name,
                    config.description,
                    config.enabled,
                    commandX,
                    commandY,
                    commandWidth,
                    18
            ));
            commandY += 22;
        }

        int totalHeight = commandToggles.size() * 25;
        commandScroll.update(totalHeight, panelHeight - 25);
    }

    private void initNoDebuffToggles() {
        int panelX = guiLeft + Dimensions.COMMAND_PANEL_X;
        int panelY = guiTop + Dimensions.COMMAND_PANEL_Y;
        int panelWidth = Dimensions.COMMAND_PANEL_WIDTH;
        int panelHeight = Dimensions.GUI_HEIGHT - 60;

        int commandX = panelX + 5;
        int commandY = panelY + 25;
        int commandWidth = panelWidth - 10;

        for (ConfigStorage.NoDebuffConfig config : ConfigStorage.getNoDebuffConfigs()) {
            commandToggles.add(new CommandToggle(
                    config.name,
                    config.description,
                    config.enabled,
                    commandX,
                    commandY,
                    commandWidth,
                    18
            ));
            commandY += 22;
        }

        int totalHeight = commandToggles.size() * 25;
        commandScroll.update(totalHeight, panelHeight - 25);
    }

    private void initEtherwarpToggles() {
        int panelX = guiLeft + Dimensions.COMMAND_PANEL_X;
        int panelY = guiTop + Dimensions.COMMAND_PANEL_Y;
        int panelWidth = Dimensions.COMMAND_PANEL_WIDTH;
        int panelHeight = Dimensions.GUI_HEIGHT - 60;

        int commandX = panelX + 5;
        int commandY = panelY + 45;
        int commandWidth = panelWidth - 10;

        for (ConfigStorage.EtherwarpConfig config : ConfigStorage.getEtherwarpConfigs()) {
            if (config.name.equals("Render Method")) {
                commandToggles.add(new MethodDropdown(commandX, commandY, commandWidth, 18));
            } else {
                commandToggles.add(new CommandToggle(
                        config.name,
                        config.description,
                        config.enabled,
                        commandX,
                        commandY,
                        commandWidth,
                        18
                ));
            }
            commandY += 22;
        }

        commandY += 10;
        commandToggles.add(new ColorInput(
                "Overlay Color",
                EtherwarpOverlay.etherwarpOverlayColor,
                commandX,
                commandY,
                commandWidth,
                18
        ));
        commandY += 50;

        commandToggles.add(new ColorInput(
                "Fail Color",
                EtherwarpOverlay.etherwarpOverlayFailColor,
                commandX,
                commandY,
                commandWidth,
                18
        ));

        int totalHeight = commandToggles.size() * 25 + 60;
        commandScroll.update(totalHeight, panelHeight - 25);
    }

    private void initFastHotkeyRows() {
        // Build editor rows from stored entries
        List<ConfigStorage.FastHotKeyEntry> entries = ConfigStorage.getFastHotKeyEntries();
        for (ConfigStorage.FastHotKeyEntry e : entries) {
            fastRows.add(new FastRow(e));
        }
        // Update scroll metrics based on current rows
        int panelHeight = Dimensions.GUI_HEIGHT - 60;
        int totalHeight = fastRows.size() * Dimensions.FH_ROW_HEIGHT + Dimensions.FH_ADD_HEIGHT + 10;
        commandScroll.update(totalHeight, panelHeight - 25);
    }

    private void drawCommandPanelContent(int mouseX, int mouseY, int panelX, int panelY, int panelWidth, int panelHeight) {
        glEnable(GL_SCISSOR_TEST);
        glScissor(panelX * new ScaledResolution(Minecraft.getMinecraft()).getScaleFactor(),
                (height - (panelY + panelHeight)) * new ScaledResolution(Minecraft.getMinecraft()).getScaleFactor(),
                panelWidth * new ScaledResolution(Minecraft.getMinecraft()).getScaleFactor(),
                (panelHeight - 25) * new ScaledResolution(Minecraft.getMinecraft()).getScaleFactor());

        drawCommandToggles(mouseX, mouseY, panelY);
        drawOpenDropdowns(mouseX, mouseY, panelY);

        glDisable(GL_SCISSOR_TEST);

        if (commandScroll.shouldRenderScrollbar()) {
            commandScroll.updateScrollbarPosition(
                    panelX + panelWidth - Dimensions.SCROLLBAR_WIDTH - 2,
                    panelY + 25,
                    panelHeight - 25
            );
            commandScroll.drawScrollbar(Colors.COMMAND_SCROLLBAR, Colors.COMMAND_SCROLLBAR_HANDLE);
        }
    }

    private String getCommandPanelTitle() {
        if (selectedCommandModule.name.equals("Party Commands")) {
            return "Command Settings";
        } else if (selectedCommandModule.name.equals("No Debuff")) {
            return "NoDebuff Settings";
        } else if (selectedCommandModule.name.equals("Fast Hotkey")) {
            return "Fast Hotkey Settings";
        } else {
            return "Etherwarp Settings";
        }
    }

    private boolean handleDropdownClicks(int mouseX, int mouseY) {
        int panelY = guiTop + Dimensions.COMMAND_PANEL_Y;
        int contentY = panelY + 25 - commandScroll.getOffset();

        for (CommandToggle toggle : commandToggles) {
            if (toggle instanceof MethodDropdown) {
                MethodDropdown dropdown = (MethodDropdown)toggle;
                if (dropdown.isMouseOver(mouseX, mouseY, contentY)) {
                    handleDropdownInteraction(dropdown, mouseX, mouseY, contentY);
                    return true;
                }
            }
            contentY += toggle instanceof ColorInput ? 50 : 22;
        }
        return false;
    }

    private boolean handleColorInputClicks(int mouseX, int mouseY) {
        int panelY = guiTop + Dimensions.COMMAND_PANEL_Y;
        int contentY = panelY + 25 - commandScroll.getOffset();

        for (CommandToggle toggle : commandToggles) {
            if (toggle instanceof ColorInput) {
                ColorInput colorInput = (ColorInput)toggle;
                int inputY = contentY + toggle.height + 8;
                if (mouseX >= colorInput.x + 40 && mouseX <= colorInput.x + colorInput.width &&
                        mouseY >= inputY - 2 && mouseY <= inputY + 15) {
                    handleColorInputClick(colorInput, mouseX);
                    return true;
                }
            }
            contentY += toggle instanceof ColorInput ? 50 : 22;
        }
        return false;
    }

    private void handleRegularToggleClicks(int mouseX, int mouseY) {
        int panelY = guiTop + Dimensions.COMMAND_PANEL_Y;
        int contentY = panelY + 25 - commandScroll.getOffset();

        for (CommandToggle toggle : commandToggles) {
            if (!(toggle instanceof ColorInput) && !(toggle instanceof MethodDropdown)) {
                if (toggle.isMouseOver(mouseX, mouseY, contentY)) {
                    toggle.toggle();
                    updateEtherwarpConfig();
                    return;
                }
            }
            contentY += toggle instanceof ColorInput ? 50 : 22;
        }
    }

    private void drawCommandToggles(int mouseX, int mouseY, int panelY) {
        int contentY = panelY + 25 - commandScroll.getOffset();
        for (CommandToggle toggle : commandToggles) {
            if (!(toggle instanceof MethodDropdown && ((MethodDropdown)toggle).isOpen)) {
                toggle.draw(mouseX, mouseY, contentY, fontRendererObj);
            }
            contentY += toggle instanceof ColorInput ? 50 : 22;
        }
    }

    private void drawOpenDropdowns(int mouseX, int mouseY, int panelY) {
        int contentY = panelY + 25 - commandScroll.getOffset();
        for (CommandToggle toggle : commandToggles) {
            if (toggle instanceof MethodDropdown && ((MethodDropdown)toggle).isOpen) {
                toggle.draw(mouseX, mouseY, contentY, fontRendererObj);
            }
            contentY += toggle instanceof ColorInput ? 50 : 22;
        }
    }

    private void updateEtherwarpConfig() {
        if (selectedCommandModule != null && selectedCommandModule.name.equals("Etherwarp Overlay")) {
            for (ConfigStorage.EtherwarpConfig config : ConfigStorage.getEtherwarpConfigs()) {
                for (CommandToggle toggle : commandToggles) {
                    if (toggle.name.equals(config.name)) {
                        config.enabled = toggle.isEnabled();
                        break;
                    }
                }
            }
            ConfigStorage.saveEtherwarpConfig();

            EtherwarpOverlay.etherwarpSyncWithServer = ConfigStorage.getEtherwarpConfigs().get(0).enabled;
            EtherwarpOverlay.etherwarpOverlayOnlySneak = ConfigStorage.getEtherwarpConfigs().get(1).enabled;
            EtherwarpOverlay.etherwarpShowFailLocation = ConfigStorage.getEtherwarpConfigs().get(2).enabled;
        }
    }

    private void handleDeleteKeyAction() {
        if (showCommandSettings && selectedCommandModule != null && selectedCommandModule.name.equals("Fast Hotkey")) {
            for (FastRow row : fastRows) {
                if (row.labelInput.isEditing) {
                    row.labelInput.handleKeyTyped((char)0, Keyboard.KEY_BACK);
                    return;
                }
                if (row.commandInput.isEditing) {
                    row.commandInput.handleKeyTyped((char)0, Keyboard.KEY_BACK);
                    return;
                }
            }
        }
        for (CommandToggle toggle : commandToggles) {
            if (toggle instanceof ColorInput && ((ColorInput)toggle).isEditing) {
                ((ColorInput)toggle).handleKeyTyped((char)0, Keyboard.KEY_BACK);
                break;
            }
        }
    }

    private void handleDropdownInteraction(MethodDropdown dropdown, int mouseX, int mouseY, int contentY) {
        if (dropdown.isOpen) {
            for (int i = 0; i < dropdown.methods.length; i++) {
                int optionY = contentY + dropdown.height + (i * dropdown.height);
                if (mouseY >= optionY && mouseY <= optionY + dropdown.height) {
                    dropdown.selectMethod(i);
                    dropdown.isOpen = false;
                    return;
                }
            }
            dropdown.isOpen = false;
        } else {
            dropdown.isOpen = true;
        }
    }

    private void handleColorInputClick(ColorInput colorInput, int mouseX) {
        colorInput.isEditing = true;
        colorInput.cursorBlinkTimer = 0;
        colorInput.cursorVisible = true;

        int textX = colorInput.x + 45;
        String text = colorInput.currentValue;
        int relativeX = mouseX - textX;
        int pos = 0;
        int textWidth = 0;

        while (pos < text.length()) {
            char c = text.charAt(pos);
            int charWidth = fontRendererObj.getCharWidth(c);
            if (relativeX < (textWidth + charWidth/2)) {
                break;
            }
            textWidth += charWidth;
            pos++;
        }

        colorInput.cursorPosition = pos;
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

    private class CategoryButton extends GuiButton {
        CategoryButton(String category, int x, int y) {
            super(-1, x, y, 100, 20, category);
        }

        @Override
        public void drawButton(Minecraft mc, int mouseX, int mouseY) {
            if (this.visible) {
                int bgColor = selectedCategory.equals(displayString) ? Colors.SELECTED_CATEGORY : Colors.CATEGORY_BUTTON;
                drawRect(xPosition, yPosition, xPosition + width, yPosition + height, bgColor);
                int textColor = selectedCategory.equals(displayString) ? 0xFFFFFFFF : 0xCCCCCC;
                this.drawCenteredString(mc.fontRendererObj, displayString, xPosition + width / 2, yPosition + (height - 8) / 2, textColor);
            }
        }
    }

    private class ModuleButton {
        private final ModuleInfo module;
        private final int x, y, width, height;
        private boolean showDropdown = false;
        private final int dropdownSize = 10;

        ModuleButton(ModuleInfo module, int x, int y, int width, int height) {
            this.module = module;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        void draw(int mouseX, int mouseY, FontRenderer fontRenderer) {
            int bgColor = isMoveGuiButton() ? Colors.MOVE_GUI :
                    (module.enabled ? Colors.MODULE_ACTIVE : Colors.MODULE_INACTIVE);

            drawRect(x, y, x + width, y + height, bgColor);
            fontRenderer.drawStringWithShadow(module.name, x + Dimensions.TEXT_PADDING, y + Dimensions.TEXT_PADDING, Colors.TEXT);

            List<String> descLines = fontRenderer.listFormattedStringToWidth(module.description, width - Dimensions.TEXT_PADDING * 2);
            for (int i = 0; i < descLines.size(); i++) {
                fontRenderer.drawString(
                        descLines.get(i),
                        x + Dimensions.TEXT_PADDING,
                        y + Dimensions.TEXT_PADDING + 12 + (i * Dimensions.LINE_HEIGHT),
                        0xAAAAAA
                );
            }

            if (module.enabled && (module.name.equals("Party Commands") ||
                    module.name.equals("No Debuff") ||
                    module.name.equals("Etherwarp Overlay") ||
                    module.name.equals("Fast Hotkey"))) {
                String settingsText = "Settings";
                int textWidth = fontRenderer.getStringWidth(settingsText);
                int textX = x + width - textWidth - Dimensions.TEXT_PADDING;
                int textY = y + Dimensions.TEXT_PADDING;

                drawRect(textX - 2, textY - 2,
                        textX + textWidth + 2, textY + 10,
                        0x80000000);

                fontRenderer.drawStringWithShadow(settingsText, textX, textY, 0xFFFFFF);
            }

            if (isMouseOver(mouseX, mouseY)) {
                drawRect(x, y, x + width, y + height, 0x40FFFFFF);
            }
        }

        boolean isDropdownClicked(int mouseX, int mouseY) {
            if (!module.enabled || !(module.name.equals("Party Commands") ||
                    module.name.equals("No Debuff") ||
                    module.name.equals("Etherwarp Overlay") ||
                    module.name.equals("Fast Hotkey"))) {
                return false;
            }
            String settingsText = "Settings";
            int textWidth = fontRendererObj.getStringWidth(settingsText);
            int textX = x + width - textWidth - Dimensions.TEXT_PADDING;
            int textY = y + Dimensions.TEXT_PADDING;
            return mouseX >= textX - 2 && mouseX <= textX + textWidth + 2 &&
                    mouseY >= textY - 2 && mouseY <= textY + 10;
        }

        boolean isMouseOver(int mouseX, int mouseY) {
            return mouseX >= x && mouseX <= x + width &&
                    mouseY >= y && mouseY <= y + height;
        }

        boolean isMoveGuiButton() {
            return module.name.equals("Move GUI Position");
        }

        ModuleInfo getModule() {
            return module;
        }
    }

    private class CommandToggle {
        protected final String name;
        protected final String description;
        protected boolean enabled;
        protected final int x, y, width, height;

        CommandToggle(String name, String description, boolean enabled, int x, int y, int width, int height) {
            this.name = name;
            this.description = description;
            this.enabled = enabled;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        void toggle() {
            enabled = !enabled;
        }

        boolean isEnabled() {
            return enabled;
        }

        String getConfigKey() {
            return name.toLowerCase().replace(" ", "");
        }

        void draw(int mouseX, int mouseY, int yPos, FontRenderer fontRenderer) {
            if (yPos + height > guiTop + 30 && yPos < guiTop + Dimensions.GUI_HEIGHT - 30) {
                drawRect(x, yPos, x + width, yPos + height,
                        enabled ? Colors.COMMAND_CHECKBOX_SELECTED : Colors.COMMAND_CHECKBOX);

                drawRect(x + 3, yPos + 4, x + 13, yPos + 14, 0xFF000000);
                if (enabled) {
                    drawRect(x + 5, yPos + 6, x + 11, yPos + 12, 0xFFFFFFFF);
                }

                fontRenderer.drawStringWithShadow(name, x + 18, yPos + 5, Colors.TEXT);

                if (isMouseOver(mouseX, mouseY, yPos)) {
                    drawRect(x, yPos, x + width, yPos + 18, 0x40FFFFFF);
                }
            }
        }

        boolean isMouseOver(int mouseX, int mouseY, int yPos) {
            return mouseX >= x && mouseX <= x + width &&
                    mouseY >= yPos && mouseY <= yPos + height;
        }
    }

    private class ScrollManager {
        private int offset = 0;
        private int maxOffset = 0;
        private boolean isScrolling = false;
        private int scrollBarX, scrollBarY;
        private int scrollBarHeight = Dimensions.MIN_SCROLLBAR_HEIGHT;
        private int visibleHeight;

        void reset() {
            offset = 0;
            maxOffset = 0;
            isScrolling = false;
        }

        void update(int totalHeight, int visibleHeight) {
            this.visibleHeight = visibleHeight;
            maxOffset = Math.max(0, totalHeight - visibleHeight);
            offset = Math.min(offset, maxOffset);
            offset = Math.max(0, offset);
        }

        void updateScrollbarPosition(int x, int y, int visibleHeight) {
            scrollBarX = x;
            scrollBarY = y;
            scrollBarHeight = (int)((visibleHeight / (float)Math.max(maxOffset + visibleHeight, visibleHeight)) * visibleHeight);
            scrollBarHeight = Math.max(Dimensions.MIN_SCROLLBAR_HEIGHT, Math.min(scrollBarHeight, visibleHeight));
        }

        boolean checkScrollbarClick(int mouseX, int mouseY) {
            if (maxOffset <= 0) return false;
            return mouseX >= scrollBarX && mouseX <= scrollBarX + Dimensions.SCROLLBAR_WIDTH &&
                    mouseY >= scrollBarY && mouseY <= scrollBarY + scrollBarHeight;
        }

        void handleDrag(int mouseX, int mouseY, Runnable callback) {
            if (isScrolling && maxOffset > 0) {
                float scrollableArea = scrollBarY + scrollBarHeight - Dimensions.MIN_SCROLLBAR_HEIGHT;
                float scrollPos = Math.min(Math.max(mouseY - scrollBarY - (scrollBarHeight / 2), 0), scrollableArea);
                offset = (int)((scrollPos / scrollableArea) * maxOffset);
                if (callback != null) callback.run();
            }
        }

        void handleWheelScroll(int scroll, Runnable callback) {
            if (scroll != 0) {
                offset += scroll > 0 ? -20 : 20;
                offset = Math.max(0, Math.min(offset, maxOffset));
                if (callback != null) callback.run();
            }
        }

        void handleWheelScroll(int scroll) {
            handleWheelScroll(scroll, null);
        }

        void endScroll() {
            isScrolling = false;
        }

        boolean shouldRenderScrollbar() {
            return maxOffset > 0;
        }

        void drawScrollbar(int trackColor, int handleColor) {
            if (!shouldRenderScrollbar()) return;

            drawRect(scrollBarX, scrollBarY,
                    scrollBarX + Dimensions.SCROLLBAR_WIDTH,
                    scrollBarY + visibleHeight,
                    trackColor);

            float contentRatio = visibleHeight / (float)(maxOffset + visibleHeight);
            int handleHeight = Math.max(Dimensions.MIN_SCROLLBAR_HEIGHT,
                    (int)(visibleHeight * contentRatio));

            int handleY = scrollBarY + (int)(((float)offset / maxOffset) * (visibleHeight - handleHeight));
            drawRect(scrollBarX, handleY,
                    scrollBarX + Dimensions.SCROLLBAR_WIDTH,
                    handleY + handleHeight,
                    handleColor);
        }

        int getOffset() {
            return offset;
        }
    }

    private class MethodDropdown extends CommandToggle {
        private final String[] methods = {"Edges", "Filled", "Both"};
        private int selectedMethod;
        private boolean isOpen = false;

        MethodDropdown(int x, int y, int width, int height) {
            super("Render Method", "", true, x, y, width, height);
            this.selectedMethod = EtherwarpOverlay.etherwarpHighlightType / 2;
        }

        @Override
        void draw(int mouseX, int mouseY, int yPos, FontRenderer fontRenderer) {
            fontRenderer.drawStringWithShadow(name, x, yPos + 5, Colors.TEXT);

            int dropdownWidth = width - 100;
            drawRect(x + 100, yPos, x + 100 + dropdownWidth, yPos + height, 0xFF333333);
            fontRenderer.drawStringWithShadow(methods[selectedMethod],
                    x + 105, yPos + 5, Colors.TEXT);

            if (isOpen) {
                GlStateManager.pushMatrix();
                GlStateManager.translate(0, 0, 300);
                for (int i = 0; i < methods.length; i++) {
                    int optionY = yPos + height + (i * height);
                    drawRect(x + 100, optionY, x + 100 + dropdownWidth, optionY + height, 0xFF444444);
                    fontRenderer.drawStringWithShadow(methods[i],
                            x + 105, optionY + 5, Colors.TEXT);
                }
                GlStateManager.popMatrix();
            }
        }

        @Override
        boolean isMouseOver(int mouseX, int mouseY, int yPos) {
            if (mouseX >= x + 100 && mouseX <= x + width &&
                    mouseY >= yPos && mouseY <= yPos + height) {
                return true;
            }

            if (isOpen) {
                for (int i = 0; i < methods.length; i++) {
                    int optionY = yPos + height + (i * height);
                    if (mouseX >= x + 100 && mouseX <= x + width &&
                            mouseY >= optionY && mouseY <= optionY + height) {
                        return true;
                    }
                }
            }
            return false;
        }

        void selectMethod(int methodIndex) {
            selectedMethod = methodIndex;
            EtherwarpOverlay.etherwarpHighlightType = methodIndex * 2;
            ConfigStorage.saveEtherwarpConfig();
        }
    }

    private class ColorInput extends CommandToggle {
        private Color color;
        private String currentValue = "";
        private boolean isEditing = false;
        private final String title;
        private long lastKeyPressTime = 0;
        private int cursorPosition = 0;
        private long cursorBlinkTimer = 0;
        private boolean cursorVisible = false;

        ColorInput(String title, Color color, int x, int y, int width, int height) {
            super(title, "", false, x, y, width, height);
            this.color = color;
            this.title = title;
            this.currentValue = color.getRed() + "," + color.getGreen() + "," + color.getBlue() + "," + color.getAlpha();
            this.cursorPosition = this.currentValue.length();
        }

        @Override
        boolean isMouseOver(int mouseX, int mouseY, int yPos) {
            int inputY = yPos + height + 8;
            return (mouseX >= x + 40 && mouseX <= x + width &&
                    mouseY >= inputY - 2 && mouseY <= inputY + 15);
        }

        @Override
        void draw(int mouseX, int mouseY, int yPos, FontRenderer fontRenderer) {
            fontRenderer.drawStringWithShadow(title, x, yPos + 5, Colors.TEXT);
            drawRect(x + fontRenderer.getStringWidth(title) + 10, yPos + 3,
                    x + fontRenderer.getStringWidth(title) + 30, yPos + height - 3, color.getRGB());

            int inputY = yPos + height + 8;
            fontRenderer.drawStringWithShadow("RGBA:", x, inputY, Colors.TEXT);
            drawRect(x + 40, inputY - 2, x + width, inputY + 15, 0xFF222222);

            String displayedText = currentValue;
            fontRenderer.drawStringWithShadow(displayedText, x + 45, inputY, isEditing ? Colors.TEXT : 0xFFAAAAAA);

            if (isEditing) {
                cursorBlinkTimer += 10;
                if (cursorBlinkTimer >= 1000) {
                    cursorBlinkTimer = 0;
                    cursorVisible = !cursorVisible;
                }

                if (cursorVisible) {
                    int cursorX = x + 45 + fontRenderer.getStringWidth(displayedText.substring(0, cursorPosition));
                    drawRect(cursorX, inputY, cursorX + 1, inputY + 10, Colors.TEXT);
                }
            }
        }

        void handleKeyTyped(char typedChar, int keyCode) {
            if (!isEditing) return;

            long currentTime = System.currentTimeMillis();
            boolean repeatedPress = (currentTime - lastKeyPressTime < 200);
            lastKeyPressTime = currentTime;

            if (keyCode == Keyboard.KEY_RETURN) {
                isEditing = false;
                updateColor();
            } else if (keyCode == Keyboard.KEY_BACK) {
                if (cursorPosition > 0) {
                    currentValue = currentValue.substring(0, cursorPosition - 1) +
                            currentValue.substring(cursorPosition);
                    cursorPosition--;
                    updateColor();
                }
            } else if (keyCode == Keyboard.KEY_LEFT) {
                cursorPosition = Math.max(0, cursorPosition - 1);
            } else if (keyCode == Keyboard.KEY_RIGHT) {
                cursorPosition = Math.min(currentValue.length(), cursorPosition + 1);
            } else if (Character.isDigit(typedChar) || typedChar == ',') {
                currentValue = currentValue.substring(0, cursorPosition) + typedChar +
                        currentValue.substring(cursorPosition);
                cursorPosition++;
                updateColor();
            }

            cursorBlinkTimer = 0;
            cursorVisible = true;
        }

        private void updateColor() {
            String[] parts = currentValue.split(",");
            if (parts.length == 4) {
                try {
                    int r = Math.min(255, Math.max(0, Integer.parseInt(parts[0])));
                    int g = Math.min(255, Math.max(0, Integer.parseInt(parts[1])));
                    int b = Math.min(255, Math.max(0, Integer.parseInt(parts[2])));
                    int a = Math.min(255, Math.max(0, Integer.parseInt(parts[3])));

                    this.color = new Color(r, g, b, a);

                    if (title.equals("Overlay Color")) {
                        EtherwarpOverlay.etherwarpOverlayColor = this.color;
                    } else {
                        EtherwarpOverlay.etherwarpOverlayFailColor = this.color;
                    }

                    ConfigStorage.saveEtherwarpConfig();
                } catch (NumberFormatException e) {
                    currentValue = color.getRed() + "," + color.getGreen() + "," +
                            color.getBlue() + "," + color.getAlpha();
                    cursorPosition = currentValue.length();
                }
            }
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
}

