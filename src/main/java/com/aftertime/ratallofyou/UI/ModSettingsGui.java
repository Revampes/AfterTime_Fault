package com.aftertime.ratallofyou.UI;

import com.aftertime.ratallofyou.UI.config.ConfigStorage;
import com.aftertime.ratallofyou.modules.SkyBlock.ChatCommands;
import com.aftertime.ratallofyou.modules.render.EtherwarpOverlay;
import com.aftertime.ratallofyou.modules.render.NoDebuff;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import com.aftertime.ratallofyou.UI.config.ConfigStorage.ModuleInfo;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import static org.lwjgl.opengl.GL11.*;

public class ModSettingsGui extends GuiScreen {
    // Constants
    private static final class Colors {
        static final int BACKGROUND = new Color(0, 0, 0).getRGB();
        static final int CATEGORY = 0xFF111111;
        static final int MODULE_INACTIVE = 0xFF333333;
        static final int MODULE_ACTIVE = 0xFF006400;
        static final int TEXT = 0xFFFFFFFF;
        static final int HIGHLIGHT = 0xFF444444;
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
        static final int NO_DEBUFF_PANEL = 0xFF111133;
        static final int NO_DEBUFF_CHECKBOX = 0xFF333355;
        static final int NO_DEBUFF_CHECKBOX_SELECTED = 0xFF006466;
        static final int COMMAND_PANEL = 0xFF222222; // Darker background
        static final int COMMAND_TEXT = 0xFFFFFFFF;
        static final int COMMAND_HIGHLIGHT = 0xFF444444;
        static final int COMMAND_BORDER = 0xFF111111;
    }

    private static final class Dimensions {
        static final int GUI_WIDTH = 400;
        static final int GUI_HEIGHT = 300;
        static final int SCROLLBAR_WIDTH = 6;
        static final int MIN_SCROLLBAR_HEIGHT = 20;
        static final int TEXT_PADDING = 10;
        static final int LINE_HEIGHT = 9;

        // Module list area (unchanged)
        static final int MODULE_LIST_X = 120;
        static final int MODULE_LIST_WIDTH = 270; // Reduced from original to make space for settings panel

        // Command settings panel area
        static final int COMMAND_PANEL_X = MODULE_LIST_X + MODULE_LIST_WIDTH + 5;
        static final int COMMAND_PANEL_Y = 30;
        static final int COMMAND_PANEL_WIDTH = 150;
    }

    // GUI Components
    private final List<GuiButton> categoryButtons = new ArrayList<GuiButton>();
    private final List<ModuleButton> moduleButtons = new ArrayList<ModuleButton>();
    private final List<CommandToggle> commandToggles = new ArrayList<CommandToggle>();

    // State
    private String selectedCategory = "Kuudra";
    private ModuleInfo selectedCommandModule = null;
    private boolean showCommandSettings = false;

    // Scroll Management
    private final ScrollManager mainScroll = new ScrollManager();
    private final ScrollManager commandScroll = new ScrollManager();

    // Position
    private int guiLeft, guiTop;

    //extra
    private long lastDeleteTime = 0;
    private boolean deleteKeyHeld = false;

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
        this.showCommandSettings = false; // Always start with settings hidden
        this.selectedCommandModule = null; // Reset selected module

        createCategoryButtons();
        createModuleButtons();
    }

    private void checkForEnabledCommandModules() {
        for (ModuleInfo module : ConfigStorage.MODULES) {
            if (module.enabled && (module.name.equals("Party Commands") || module.name.equals("No Debuff"))) {
                selectedCommandModule = module;
                if (module.name.equals("Party Commands")) {
                    initCommandToggles();
                } else if (module.name.equals("No Debuff")) {
                    initNoDebuffToggles();
                }
                break;
            }
        }
    }

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
        // Use original module list position (not affected by command panel)
        int moduleX = guiLeft + Dimensions.MODULE_LIST_X;
        int moduleY = guiTop + 30;
        int moduleWidth = Dimensions.MODULE_LIST_WIDTH;
        int moduleHeight = 50;
        int modulesAreaHeight = Dimensions.GUI_HEIGHT - 60;

        // Rest of the method remains the same...
        List<ModuleInfo> filteredModules = new ArrayList<ModuleInfo>();
        for (ModuleInfo module : ConfigStorage.MODULES) {
            if (module.category.equals(selectedCategory)) {
                filteredModules.add(module);
                if (module.enabled && (module.name.equals("Party Commands") || module.name.equals("No Debuff"))) {
                    selectedCommandModule = module;
                }
            }
        }

        int totalModulesHeight = filteredModules.size() * (moduleHeight + 10);
        mainScroll.update(totalModulesHeight, modulesAreaHeight);

        int currentY = moduleY - mainScroll.getOffset();
        for (ModuleInfo module : filteredModules) {
            if (currentY + moduleHeight > guiTop + 30 && currentY < guiTop + Dimensions.GUI_HEIGHT - 30) {
                moduleButtons.add(new ModuleButton(module, moduleX, currentY, moduleWidth, moduleHeight));
            }
            currentY += moduleHeight + 10;
        }

        mainScroll.updateScrollbarPosition(guiLeft + Dimensions.MODULE_LIST_X + Dimensions.MODULE_LIST_WIDTH + 5,
                guiTop + 30,
                modulesAreaHeight);
    }

    private void handleModuleToggle(ModuleInfo module) {
        module.enabled = !module.enabled;
        ConfigStorage.saveMainConfig();

        // Update module state
        if (module.name.equals("No Debuff")) {
            NoDebuff.setEnabled(module.enabled);
            // Don't automatically show settings
            if (!module.enabled) {
                NoDebuff.loadConfig();
            } else {
                showCommandSettings = false;
                selectedCommandModule = null;
            }
        } else if (module.name.equals("Party Commands")) {
            // Don't automatically show settings
            if (!module.enabled) {
                showCommandSettings = false;
                selectedCommandModule = null;
            }
        } else {
            showCommandSettings = false;
        }

        createModuleButtons();
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (categoryButtons.contains(button)) {
            selectedCategory = button.displayString;
            mainScroll.reset();
            showCommandSettings = false; // Close settings when changing categories
            selectedCommandModule = null;
            createModuleButtons();
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        // Reset all input field editing states first
        for (CommandToggle toggle : commandToggles) {
            if (toggle instanceof ColorInput) {
                ((ColorInput)toggle).isEditing = false;
            }
        }

        // Handle scrollbars first
        if (showCommandSettings && commandScroll.checkScrollbarClick(mouseX, mouseY)) {
            return;
        }
        if (mainScroll.checkScrollbarClick(mouseX, mouseY)) {
            return;
        }

        // Check category buttons
        for (GuiButton btn : categoryButtons) {
            if (btn.isMouseOver()) {
                actionPerformed(btn);
                return;
            }
        }

        // Check module buttons
        for (ModuleButton moduleBtn : moduleButtons) {
            if (moduleBtn.isMouseOver(mouseX, mouseY)) {
                if (moduleBtn.isMoveGuiButton()) {
                    UIHighlighter.enterMoveMode(this);
                } else if (moduleBtn.isDropdownClicked(mouseX, mouseY)) {
                    // Toggle command settings
                    if (selectedCommandModule == moduleBtn.getModule() && showCommandSettings) {
                        showCommandSettings = false;
                    } else {
                        selectedCommandModule = moduleBtn.getModule();
                        if (selectedCommandModule.name.equals("Party Commands")) {
                            initCommandToggles();
                        } else if (selectedCommandModule.name.equals("No Debuff")) {
                            initNoDebuffToggles();
                        } else if (selectedCommandModule.name.equals("Etherwarp Overlay")) {
                            initEtherwarpToggles();
                        }
                        showCommandSettings = true;
                    }
                } else {
                    handleModuleToggle(moduleBtn.getModule());
                }
                return;
            }
        }

        // Check command toggles
        if (showCommandSettings) {
            int panelX = guiLeft + Dimensions.COMMAND_PANEL_X;
            int panelY = guiTop + Dimensions.COMMAND_PANEL_Y;
            int contentY = panelY + 25 - commandScroll.getOffset();

            // First check for dropdown clicks (highest priority)
            for (CommandToggle toggle : commandToggles) {
                if (toggle instanceof MethodDropdown) {
                    MethodDropdown dropdown = (MethodDropdown)toggle;
                    if (dropdown.isMouseOver(mouseX, mouseY, contentY)) {
                        if (dropdown.isOpen) {
                            // Check if clicking on an option
                            for (int i = 0; i < dropdown.methods.length; i++) {
                                int optionY = contentY + dropdown.height + (i * dropdown.height);
                                if (mouseY >= optionY && mouseY <= optionY + dropdown.height) {
                                    dropdown.selectMethod(i);
                                    dropdown.isOpen = false;
                                    return;
                                }
                            }
                            // Clicked elsewhere on open dropdown - just close it
                            dropdown.isOpen = false;
                        } else {
                            // Clicked on closed dropdown - open it
                            dropdown.isOpen = true;
                        }
                        return;
                    }
                }
                contentY += toggle instanceof ColorInput ? 50 : 22;
            }

            // Then check for color input clicks (medium priority)
            contentY = panelY + 25 - commandScroll.getOffset();
            for (CommandToggle toggle : commandToggles) {
                if (toggle instanceof ColorInput) {
                    ColorInput colorInput = (ColorInput)toggle;
                    // Only activate if clicking precisely on the input field
                    int inputY = contentY + toggle.height + 8;
                    if (mouseX >= colorInput.x + 40 && mouseX <= colorInput.x + colorInput.width &&
                            mouseY >= inputY - 2 && mouseY <= inputY + 15) {

                        colorInput.isEditing = true;
                        colorInput.cursorBlinkTimer = 0;
                        colorInput.cursorVisible = true;

                        // Precise cursor positioning
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
                        return;
                    }
                }
                contentY += toggle instanceof ColorInput ? 50 : 22;
            }

            // Finally check regular toggles (lowest priority)
            contentY = panelY + 25 - commandScroll.getOffset();
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
    }

    private void updateEtherwarpConfig() {
        if (selectedCommandModule != null && selectedCommandModule.name.equals("Etherwarp Overlay")) {
            // Update etherwarp config
            for (ConfigStorage.EtherwarpConfig config : ConfigStorage.getEtherwarpConfigs()) {
                for (CommandToggle toggle : commandToggles) {
                    if (toggle.name.equals(config.name)) {
                        config.enabled = toggle.isEnabled();
                        break;
                    }
                }
            }
            ConfigStorage.saveEtherwarpConfig();

            // Update etherwarp settings
            EtherwarpOverlay.etherwarpSyncWithServer = ConfigStorage.getEtherwarpConfigs().get(0).enabled;
            EtherwarpOverlay.etherwarpOverlayOnlySneak = ConfigStorage.getEtherwarpConfigs().get(1).enabled;
            EtherwarpOverlay.etherwarpShowFailLocation = ConfigStorage.getEtherwarpConfigs().get(2).enabled;
        }
    }

    private ModuleButton getActiveModuleButton() {
        if (selectedCommandModule == null) return null;
        for (ModuleButton moduleBtn : moduleButtons) {
            if (moduleBtn.getModule() == selectedCommandModule) {
                return moduleBtn;
            }
        }
        return null;
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        mainScroll.endScroll();
        commandScroll.endScroll();
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        mainScroll.handleDrag(mouseX, mouseY, new Runnable() {
            @Override
            public void run() {
                createModuleButtons();
            }
        });
        commandScroll.handleDrag(mouseX, mouseY, null);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);

        // Handle delete key
        if (keyCode == Keyboard.KEY_BACK) {
            if (!deleteKeyHeld) {
                deleteKeyHeld = true;
                lastDeleteTime = System.currentTimeMillis();
                // Process the first delete immediately
                handleDeleteKey();
            }
        } else {
            deleteKeyHeld = false;
        }

        // Rest of the method remains the same...
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

    public void updateScreen() {
        super.updateScreen();

        // Handle continuous delete
        if (deleteKeyHeld) {
            long currentTime = System.currentTimeMillis();
            // Initial delay of 500ms before continuous deletion starts
            if (currentTime - lastDeleteTime > 500) {
                // Then delete every 50ms for fast continuous deletion
                if (currentTime - lastDeleteTime > 50) {
                    handleDeleteKey();
                    lastDeleteTime = currentTime;
                }
            }
        }
    }

    private void handleDeleteKey() {
        for (CommandToggle toggle : commandToggles) {
            if (toggle instanceof ColorInput && ((ColorInput)toggle).isEditing) {
                ((ColorInput)toggle).handleKeyTyped((char)0, Keyboard.KEY_BACK);
                break;
            }
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int scroll = Mouse.getEventDWheel();

        if (showCommandSettings) {
            commandScroll.handleWheelScroll(scroll);
        } else {
            mainScroll.handleWheelScroll(scroll, new Runnable() {
                @Override
                public void run() {
                    createModuleButtons();
                }
            });
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawBackground();
        drawCategories();
        drawModules(mouseX, mouseY);
        drawScrollbars();
        drawCommandPanel(mouseX, mouseY);  // Pass mouse coordinates here
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawBackground() {
        int scale = new ScaledResolution(Minecraft.getMinecraft()).getScaleFactor();

        // Main background
        drawRect(guiLeft, guiTop, guiLeft + Dimensions.GUI_WIDTH, guiTop + Dimensions.GUI_HEIGHT, Colors.PANEL);

        // Title and logo positioning
        String title = "§l§nRat All Of You";
        int titleX = guiLeft + 15; // Start 15 pixels from left edge
        int titleY = guiTop + 10;

        // Draw the title
        fontRendererObj.drawStringWithShadow(title, titleX, titleY, Colors.TEXT);

        // Category and module panels
        drawRect(guiLeft + 5, guiTop + 25, guiLeft + 115, guiTop + Dimensions.GUI_HEIGHT - 5, Colors.CATEGORY);
        drawRect(guiLeft + 115, guiTop + 25, guiLeft + Dimensions.GUI_WIDTH - 5, guiTop + Dimensions.GUI_HEIGHT - 5, Colors.CATEGORY);

        // Version info
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
        // Main scrollbar
        if (mainScroll.shouldRenderScrollbar()) {
            mainScroll.drawScrollbar(Colors.SCROLLBAR, Colors.SCROLLBAR_HANDLE);
        }

        // Command scrollbar
        if (showCommandSettings && commandScroll.shouldRenderScrollbar()) {
            commandScroll.drawScrollbar(Colors.COMMAND_SCROLLBAR, Colors.COMMAND_SCROLLBAR_HANDLE);
        }
    }

    private void drawCommandPanel(int mouseX, int mouseY) {
        if (!showCommandSettings || selectedCommandModule == null) return;

        // Unified panel styling
        int panelX = guiLeft + Dimensions.COMMAND_PANEL_X;
        int panelY = guiTop + Dimensions.COMMAND_PANEL_Y;
        int panelWidth = Dimensions.COMMAND_PANEL_WIDTH;
        int panelHeight = Dimensions.GUI_HEIGHT - 60;

        // Main panel background
        drawRect(panelX, panelY, panelX + panelWidth, panelY + panelHeight, Colors.COMMAND_PANEL);

        // Border
        drawRect(panelX - 1, panelY - 1, panelX + panelWidth + 1, panelY + panelHeight + 1, Colors.COMMAND_BORDER);

        // Title
        String title;
        if (selectedCommandModule.name.equals("Party Commands")) {
            title = "Command Settings";
        } else if (selectedCommandModule.name.equals("No Debuff")) {
            title = "NoDebuff Settings";
        } else {
            title = "Etherwarp Settings";
        }
        drawCenteredString(fontRendererObj, title,
                panelX + panelWidth / 2, panelY + 5, Colors.COMMAND_TEXT);

        // Setup scissor for content
        glEnable(GL_SCISSOR_TEST);
        glScissor(panelX * new ScaledResolution(Minecraft.getMinecraft()).getScaleFactor(),
                (height - (panelY + panelHeight)) * new ScaledResolution(Minecraft.getMinecraft()).getScaleFactor(),
                panelWidth * new ScaledResolution(Minecraft.getMinecraft()).getScaleFactor(),
                (panelHeight - 25) * new ScaledResolution(Minecraft.getMinecraft()).getScaleFactor());

        // Draw regular toggles first
        int contentY = panelY + 25 - commandScroll.getOffset();
        for (CommandToggle toggle : commandToggles) {
            if (!(toggle instanceof MethodDropdown && ((MethodDropdown)toggle).isOpen)) {
                toggle.draw(mouseX, mouseY, contentY, fontRendererObj);
            }
            contentY += toggle instanceof ColorInput ? 50 : 22;
        }

        // Then draw open dropdowns on top
        contentY = panelY + 25 - commandScroll.getOffset();
        for (CommandToggle toggle : commandToggles) {
            if (toggle instanceof MethodDropdown && ((MethodDropdown)toggle).isOpen) {
                toggle.draw(mouseX, mouseY, contentY, fontRendererObj);
            }
            contentY += toggle instanceof ColorInput ? 50 : 22;
        }

        glDisable(GL_SCISSOR_TEST);

        // Draw scrollbar
        if (commandScroll.shouldRenderScrollbar()) {
            commandScroll.updateScrollbarPosition(
                    panelX + panelWidth - Dimensions.SCROLLBAR_WIDTH - 2,
                    panelY + 25,
                    panelHeight - 25
            );
            commandScroll.drawScrollbar(Colors.COMMAND_SCROLLBAR, Colors.COMMAND_SCROLLBAR_HANDLE);
        }
    }

    private void initCommandToggles() {
        commandToggles.clear();

        if (selectedCommandModule == null || !selectedCommandModule.name.equals("Party Commands")) {
            showCommandSettings = false;
            return;
        }

        showCommandSettings = true;

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

        // Update scroll parameters
        int totalHeight = commandToggles.size() * 25;
        commandScroll.update(totalHeight, panelHeight - 25);
    }

    private class CommandConfig {
        final String name;
        final String description;

        CommandConfig(String name, String description) {
            this.name = name;
            this.description = description;
        }
    }

    private void addCommandToggle(String name, String description, int x, int y, int width, int height) {
        commandToggles.add(new CommandToggle(
                name,
                description,
                ChatCommands.isCommandEnabled(name.toLowerCase().replace(" ", "")),
                x,
                y,
                width,
                height
        ));
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    // Inner component classes
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

            // Draw description
            List<String> descLines = fontRenderer.listFormattedStringToWidth(module.description, width - Dimensions.TEXT_PADDING * 2);
            for (int i = 0; i < descLines.size(); i++) {
                fontRenderer.drawString(
                        descLines.get(i),
                        x + Dimensions.TEXT_PADDING,
                        y + Dimensions.TEXT_PADDING + 12 + (i * Dimensions.LINE_HEIGHT),
                        0xAAAAAA
                );
            }

            // Draw "Settings" text if module has commands
            if (module.enabled && (module.name.equals("Party Commands") ||
                    module.name.equals("No Debuff") ||
                    module.name.equals("Etherwarp Overlay"))) {
                String settingsText = "Settings";
                int textWidth = fontRenderer.getStringWidth(settingsText);
                int textX = x + width - textWidth - Dimensions.TEXT_PADDING;
                int textY = y + Dimensions.TEXT_PADDING;

                // Draw background for better visibility
                drawRect(textX - 2, textY - 2,
                        textX + textWidth + 2, textY + 10,
                        0x80000000);

                fontRenderer.drawStringWithShadow(settingsText, textX, textY, 0xFFFFFF);
            }

            if (isMouseOver(mouseX, mouseY)) {
                drawRect(x, y, x + width, y + height, 0x40FFFFFF);
            }
        }

        private void drawLine(int x1, int y1, int x2, int y2, int color) {
            // Simple line drawing implementation
            float f = (float)(color >> 24 & 255) / 255.0F;
            float f1 = (float)(color >> 16 & 255) / 255.0F;
            float f2 = (float)(color >> 8 & 255) / 255.0F;
            float f3 = (float)(color & 255) / 255.0F;
            glEnable(GL_BLEND);
            glDisable(GL_TEXTURE_2D);
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            glColor4f(f1, f2, f3, f);
            glBegin(GL_LINES);
            glVertex2i(x1, y1);
            glVertex2i(x2, y2);
            glEnd();
            glEnable(GL_TEXTURE_2D);
            glDisable(GL_BLEND);
        }

        boolean isDropdownClicked(int mouseX, int mouseY) {
            if (!module.enabled || !(module.name.equals("Party Commands") ||
                    module.name.equals("No Debuff") ||
                    module.name.equals("Etherwarp Overlay"))) {
                return false;
            }
            String settingsText = "Settings";
            int textWidth = fontRendererObj.getStringWidth(settingsText);
            int textX = x + width - textWidth - Dimensions.TEXT_PADDING;
            int textY = y + Dimensions.TEXT_PADDING;
            return mouseX >= textX - 2 && mouseX <= textX + textWidth + 2 &&
                    mouseY >= textY - 2 && mouseY <= textY + 10;
        }

        void toggleDropdown() {
            showDropdown = !showDropdown;
        }

        boolean isDropdownVisible() {
            return showDropdown;
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
                // Reduced height from 20 to 18
                drawRect(x, yPos, x + width, yPos + height,
                        enabled ? Colors.COMMAND_CHECKBOX_SELECTED : Colors.COMMAND_CHECKBOX);

                // Checkbox
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
            this.visibleHeight = visibleHeight; // Store the visible height
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

            // Check if click is within scrollbar area
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

            // Scrollbar track
            drawRect(scrollBarX, scrollBarY,
                    scrollBarX + Dimensions.SCROLLBAR_WIDTH,
                    scrollBarY + visibleHeight,
                    trackColor);

            // Calculate handle size
            float contentRatio = visibleHeight / (float)(maxOffset + visibleHeight);
            int handleHeight = Math.max(Dimensions.MIN_SCROLLBAR_HEIGHT,
                    (int)(visibleHeight * contentRatio));

            // Scrollbar handle
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

    private void initNoDebuffToggles() {
        commandToggles.clear();

        if (selectedCommandModule == null || !selectedCommandModule.name.equals("No Debuff")) {
            showCommandSettings = false;
            return;
        }

        showCommandSettings = true;

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

        // Update scroll parameters
        int totalHeight = commandToggles.size() * 25;
        commandScroll.update(totalHeight, panelHeight - 25);
    }

    private void addNoDebuffToggle(String name, String description, int x, int y, int width, int height) {
        boolean enabled = false;
        if (name.equals("Remove Fire Overlay")) {
            enabled = NoDebuff.isNoFire();
        } else if (name.equals("Ignore Blindness")) {
            enabled = NoDebuff.isNoBlindness();
        } else if (name.equals("Clear Liquid Vision")) {
            enabled = NoDebuff.isClearLiquidVision();
        }

        commandToggles.add(new CommandToggle(
                name,
                description,
                enabled,
                x,
                y,
                width,
                height
        ));
    }

    private void initEtherwarpToggles() {
        commandToggles.clear();

        if (selectedCommandModule == null || !selectedCommandModule.name.equals("Etherwarp Overlay")) {
            showCommandSettings = false;
            return;
        }

        showCommandSettings = true;

        int panelX = guiLeft + Dimensions.COMMAND_PANEL_X;
        int panelY = guiTop + Dimensions.COMMAND_PANEL_Y;
        int panelWidth = Dimensions.COMMAND_PANEL_WIDTH;
        int panelHeight = Dimensions.GUI_HEIGHT - 60;

        int commandX = panelX + 5;
        int commandY = panelY + 45;
        int commandWidth = panelWidth - 10;

        // Add standard toggles
        for (ConfigStorage.EtherwarpConfig config : ConfigStorage.getEtherwarpConfigs()) {
            if (config.name.equals("Render Method")) {
                commandToggles.add(new MethodDropdown(
                        commandX,
                        commandY,
                        commandWidth,
                        18
                ));
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

        // Add color input fields with new layout
        commandY += 10;
        commandToggles.add(new ColorInput(
                "Overlay Color",
                EtherwarpOverlay.etherwarpOverlayColor,
                commandX,
                commandY,
                commandWidth,
                18
        ));
        commandY += 50; // Increased spacing

        commandToggles.add(new ColorInput(
                "Fail Color",
                EtherwarpOverlay.etherwarpOverlayFailColor,
                commandX,
                commandY,
                commandWidth,
                18
        ));

        // Update scroll parameters with new spacing
        int totalHeight = commandToggles.size() * 25 + 60; // Extra space for inputs
        commandScroll.update(totalHeight, panelHeight - 25);
    }

    private class MethodDropdown extends CommandToggle {
        private final String[] methods = {"Edges", "Filled", "Both"};
        private int selectedMethod;
        private boolean isOpen = false;

        MethodDropdown(int x, int y, int width, int height) {
            super("Render Method", "", true, x, y, width, height);
            this.selectedMethod = EtherwarpOverlay.etherwarpHighlightType / 2; // Convert to 0-2 index
        }

        // Update the MethodDropdown's draw method:
        @Override
        void draw(int mouseX, int mouseY, int yPos, FontRenderer fontRenderer) {
            // Draw label
            fontRenderer.drawStringWithShadow(name, x, yPos + 5, Colors.TEXT);

            // Draw dropdown box
            int dropdownWidth = width - 100;
            drawRect(x + 100, yPos, x + 100 + dropdownWidth, yPos + height, 0xFF333333);
            fontRenderer.drawStringWithShadow(methods[selectedMethod],
                    x + 105, yPos + 5, Colors.TEXT);

            if (isOpen) {
                // Draw dropdown options on top of everything
                GlStateManager.pushMatrix();
                GlStateManager.translate(0, 0, 300); // Bring to front
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
            // Main dropdown box
            if (mouseX >= x + 100 && mouseX <= x + width &&
                    mouseY >= yPos && mouseY <= yPos + height) {
                return true;
            }

            // Dropdown options (when open)
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
            EtherwarpOverlay.etherwarpHighlightType = methodIndex * 2; // Convert to 0/2/4
            ConfigStorage.saveEtherwarpConfig();
        }
    }

    // Add this inner class for color input
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
            // Only the input field area should be clickable
            int inputY = yPos + height + 8;
            return (mouseX >= x + 40 && mouseX <= x + width &&
                    mouseY >= inputY - 2 && mouseY <= inputY + 15);
        }

        @Override
        void draw(int mouseX, int mouseY, int yPos, FontRenderer fontRenderer) {
            // Draw title and color preview
            fontRenderer.drawStringWithShadow(title, x, yPos + 5, Colors.TEXT);
            drawRect(x + fontRenderer.getStringWidth(title) + 10, yPos + 3,
                    x + fontRenderer.getStringWidth(title) + 30, yPos + height - 3, color.getRGB());

            // Draw RGBA input field
            int inputY = yPos + height + 8;
            fontRenderer.drawStringWithShadow("RGBA:", x, inputY, Colors.TEXT);
            drawRect(x + 40, inputY - 2, x + width, inputY + 15, 0xFF222222);

            // Draw text and cursor
            String displayedText = currentValue;
            fontRenderer.drawStringWithShadow(displayedText, x + 45, inputY, isEditing ? Colors.TEXT : 0xFFAAAAAA);

            // Draw blinking cursor when editing
            if (isEditing) {
                cursorBlinkTimer += 10; // Slower update (was 20)
                if (cursorBlinkTimer >= 1000) { // Blink every 1000ms (was 600ms)
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
            boolean repeatedPress = (currentTime - lastKeyPressTime < 200); // 200ms threshold for repeat
            lastKeyPressTime = currentTime;

            if (keyCode == Keyboard.KEY_RETURN) {
                isEditing = false;
                updateColor();
            } else if (keyCode == Keyboard.KEY_BACK) {
                if (cursorPosition > 0) {
                    currentValue = currentValue.substring(0, cursorPosition - 1) +
                            currentValue.substring(cursorPosition);
                    cursorPosition--;
                    updateColor(); // Auto-save on delete
                }
            } else if (keyCode == Keyboard.KEY_LEFT) {
                cursorPosition = Math.max(0, cursorPosition - 1);
            } else if (keyCode == Keyboard.KEY_RIGHT) {
                cursorPosition = Math.min(currentValue.length(), cursorPosition + 1);
            } else if (Character.isDigit(typedChar) || typedChar == ',') {
                currentValue = currentValue.substring(0, cursorPosition) + typedChar +
                        currentValue.substring(cursorPosition);
                cursorPosition++;
                updateColor(); // Auto-save on input
            }

            // Reset cursor blink whenever a key is pressed
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
                    // Revert to current color on invalid input
                    currentValue = color.getRed() + "," + color.getGreen() + "," +
                            color.getBlue() + "," + color.getAlpha();
                    cursorPosition = currentValue.length();
                }
            }
        }
    }
}