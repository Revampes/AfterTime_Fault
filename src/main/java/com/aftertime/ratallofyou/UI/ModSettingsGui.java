package com.aftertime.ratallofyou.UI;

import com.aftertime.ratallofyou.UI.ModConfig.ModuleInfo;
import com.aftertime.ratallofyou.modules.SkyBlock.ChatCommands;
import com.aftertime.ratallofyou.modules.render.NoDebuff;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
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
        static final int COMMAND_PANEL = 0xFF111111;
        static final int COMMAND_CHECKBOX = 0xFF333333;
        static final int COMMAND_CHECKBOX_SELECTED = 0xFF006400;
        static final int COMMAND_SCROLLBAR = 0xFF555577;
        static final int COMMAND_SCROLLBAR_HANDLE = 0xFF8888AA;
        static final int NO_DEBUFF_PANEL = 0xFF111133;
        static final int NO_DEBUFF_CHECKBOX = 0xFF333355;
        static final int NO_DEBUFF_CHECKBOX_SELECTED = 0xFF006466;
    }

    private static final class Dimensions {
        static final int GUI_WIDTH = 400;
        static final int GUI_HEIGHT = 300;
        static final int SCROLLBAR_WIDTH = 6;
        static final int MIN_SCROLLBAR_HEIGHT = 20;
        static final int TEXT_PADDING = 10;
        static final int LINE_HEIGHT = 9;
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
    private ResourceLocation ratLogo = null;
    private boolean logoLoaded = false;
    private int logoWidth = 20;
    private int logoHeight = 20;

    @Override
    public void initGui() {
        // Load the logo image (only once)
        if (!logoLoaded) {
            try {
                // Load the image from resources
                InputStream imageStream = Minecraft.getMinecraft().getResourceManager().getResource(
                        new ResourceLocation("ratallofyou", "textures/AfterTimeCutieRat.png")
                ).getInputStream();
                BufferedImage image = ImageIO.read(imageStream);
                ratLogo = Minecraft.getMinecraft().getTextureManager().getDynamicTextureLocation(
                        "rat_logo",
                        new DynamicTexture(image)
                );
                logoWidth = image.getWidth();
                logoHeight = image.getHeight();
                imageStream.close();
                logoLoaded = true;
            } catch (Exception e) {
                System.err.println("Failed to load logo image:");
                e.printStackTrace();
                logoLoaded = true; // Don't try again if it fails
            }
        }

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
        for (ModuleInfo module : ModConfig.MODULES) {
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
        for (ModuleInfo module : ModConfig.MODULES) {
            if (!categories.contains(module.category)) {
                categories.add(module.category);
            }
        }
        return categories;
    }

    private void createModuleButtons() {
        moduleButtons.clear();
        int moduleX = guiLeft + 120;
        int moduleY = guiTop + 30;
        int moduleWidth = Dimensions.GUI_WIDTH - 140 - Dimensions.SCROLLBAR_WIDTH;
        int moduleHeight = 50;
        int modulesAreaHeight = Dimensions.GUI_HEIGHT - 60;

        // Filter modules for current category
        List<ModuleInfo> filteredModules = new ArrayList<ModuleInfo>();
        for (ModuleInfo module : ModConfig.MODULES) {
            if (module.category.equals(selectedCategory)) {
                filteredModules.add(module);
                // Check if this is an enabled command module
                if (module.enabled && (module.name.equals("Party Commands") || module.name.equals("No Debuff"))) {
                    selectedCommandModule = module;
                }
            }
        }

        // Calculate scroll parameters
        int totalModulesHeight = filteredModules.size() * (moduleHeight + 10);
        mainScroll.update(totalModulesHeight, modulesAreaHeight);

        // Create visible module buttons
        int currentY = moduleY - mainScroll.getOffset();
        for (ModuleInfo module : filteredModules) {
            if (currentY + moduleHeight > guiTop + 30 && currentY < guiTop + Dimensions.GUI_HEIGHT - 30) {
                moduleButtons.add(new ModuleButton(module, moduleX, currentY, moduleWidth, moduleHeight));
            }
            currentY += moduleHeight + 10;
        }

        // Update scrollbar position
        mainScroll.updateScrollbarPosition(guiLeft + Dimensions.GUI_WIDTH - Dimensions.SCROLLBAR_WIDTH - 10,
                guiTop + 30,
                modulesAreaHeight);
    }

    private void handleModuleToggle(ModuleInfo module) {
        module.enabled = !module.enabled;
        ModConfig.saveConfig();

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
            showCommandSettings = false;
            createModuleButtons();
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        // Handle scrollbars first (re-enabled)
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
            ModuleButton activeModuleBtn = getActiveModuleButton();
            if (activeModuleBtn != null) {
                int contentY = activeModuleBtn.y + 25 - commandScroll.getOffset();
                for (CommandToggle toggle : commandToggles) {
                    if (toggle.isMouseOver(mouseX, mouseY, contentY)) {
                        toggle.toggle();
                        if (selectedCommandModule.name.equals("Party Commands")) {
                            ChatCommands.setCommandEnabled(toggle.getConfigKey(), toggle.isEnabled());
                        } else if (selectedCommandModule.name.equals("No Debuff")) {
                            // Handle NoDebuff specific toggles
                            if (toggle.name.equals("Remove Fire Overlay")) {
                                NoDebuff.setNoFire(toggle.isEnabled());
                            } else if (toggle.name.equals("Ignore Blindness")) {
                                NoDebuff.setNoBlindness(toggle.isEnabled());
                            } else if (toggle.name.equals("Clear Liquid Vision")) {
                                NoDebuff.setClearLiquidVision(toggle.isEnabled());
                            }
                        }
                        return;
                    }
                    contentY += 22;
                }
            }
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

        // Draw the rat logo if loaded (to the left of the title)
        if (ratLogo != null) {
            Minecraft.getMinecraft().getTextureManager().bindTexture(ratLogo);
            // Draw logo left of title (with 5px spacing)
            int logoX = titleX;
            int logoY = titleY + (fontRendererObj.FONT_HEIGHT - logoHeight) / 2; // Vertically centered with text
            drawModalRectWithCustomSizedTexture(logoX, logoY, 0, 0, logoWidth, logoHeight, logoWidth, logoHeight);

            // Adjust title position to be right of logo
            titleX += logoWidth + 5;
        }

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

        // Find the active module button
        ModuleButton activeModuleBtn = getActiveModuleButton();
        if (activeModuleBtn == null || !activeModuleBtn.getModule().enabled) {
            showCommandSettings = false;
            return;
        }

        // Position panel relative to the module button
        int panelX = activeModuleBtn.x + activeModuleBtn.width + 5;
        int panelY = activeModuleBtn.y;
        int panelWidth = Dimensions.COMMAND_PANEL_WIDTH;
        int panelHeight = Math.min(Dimensions.GUI_HEIGHT - 60, height - panelY - 10);
        int scale = new ScaledResolution(Minecraft.getMinecraft()).getScaleFactor();

        // Adjust position if it would go off-screen
        if (panelX + panelWidth > width) {
            panelX = activeModuleBtn.x - panelWidth - 5;
        }
        if (panelY + panelHeight > height) {
            panelY = height - panelHeight - 10;
        }

        // Draw panel background - using Colors.PANEL for black background
        drawRect(panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0xFF333333);

        // Draw border
        drawRect(panelX - 1, panelY - 1, panelX + panelWidth + 1, panelY + panelHeight + 1, 0xFF000000);

        // Draw title with proper spacing
        drawCenteredString(fontRendererObj, "Command Settings",
                panelX + panelWidth / 2, panelY + 5, Colors.TEXT);

        // Setup scissor for content with proper spacing
        glEnable(GL_SCISSOR_TEST);
        glScissor(panelX * scale, (height - (panelY + panelHeight)) * scale,
                panelWidth * scale, (panelHeight - 25) * scale);

        // Draw toggles with adjusted spacing
        int contentY = panelY + 25 - commandScroll.getOffset(); // Increased from 20
        for (CommandToggle toggle : commandToggles) {
            toggle.draw(mouseX, mouseY, contentY, fontRendererObj);
            contentY += 22; // Reduced from 25 for tighter spacing
        }

        glDisable(GL_SCISSOR_TEST);

        // Draw scrollbar if needed
        if (commandScroll.shouldRenderScrollbar()) {
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

        // Find the active module button for positioning
        ModuleButton activeModuleBtn = getActiveModuleButton();
        if (activeModuleBtn == null) return;

        // Position panel relative to the module button
        int panelX = activeModuleBtn.x + activeModuleBtn.width + 5;
        int panelY = activeModuleBtn.y;
        int panelWidth = Dimensions.COMMAND_PANEL_WIDTH;
        int panelHeight = Math.min(Dimensions.GUI_HEIGHT - 60, height - panelY - 10);

        // Adjust position if it would go off-screen
        if (panelX + panelWidth > width) {
            panelX = activeModuleBtn.x - panelWidth - 5;
        }
        if (panelY + panelHeight > height) {
            panelY = height - panelHeight - 10;
        }

        int commandX = panelX + 5;
        int commandY = panelY + 25;
        int commandWidth = panelWidth - 10;
        int commandHeight = 20;

        List<CommandConfig> commandConfigs = Arrays.asList(
                new CommandConfig("Warp", "Enable !warp command"),
                new CommandConfig("Warp Transfer", "Enable !warptransfer command"),
                new CommandConfig("Coords", "Enable !coords command"),
                new CommandConfig("All Invite", "Enable !allinvite command"),
                new CommandConfig("Boop", "Enable !boop command"),
                new CommandConfig("Coin Flip", "Enable !cf command"),
                new CommandConfig("8Ball", "Enable !8ball command"),
                new CommandConfig("Dice", "Enable !dice command"),
                new CommandConfig("Party Transfer", "Enable !pt command"),
                new CommandConfig("TPS", "Enable !tps command"),
                new CommandConfig("Downtime", "Enable !dt command"),
                new CommandConfig("Queue Instance", "Enable dungeon queue commands"),
                new CommandConfig("Demote", "Enable !demote command"),
                new CommandConfig("Promote", "Enable !promote command"),
                new CommandConfig("Disband", "Enable !disband command")
        );

        for (CommandConfig config : commandConfigs) {
            addCommandToggle(config.name, config.description, commandX, commandY, commandWidth, 18); // Height reduced to 18
            commandY += 22; // Reduced from 25
        }

        // Update command scroll parameters with correct panel position
        int totalHeight = commandToggles.size() * 25;
        commandScroll.update(totalHeight, panelHeight - 25); // Subtract title height
        commandScroll.updateScrollbarPosition(
                panelX + panelWidth - Dimensions.SCROLLBAR_WIDTH - 2,
                panelY + 25, // Start below title
                panelHeight - 25 // Subtract title height
        );
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
            if (module.enabled && (module.name.equals("Party Commands") || module.name.equals("No Debuff"))) {
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
            if (!module.enabled || !(module.name.equals("Party Commands") || module.name.equals("No Debuff"))) {
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
        private final String name;
        private final String description;
        private boolean enabled;
        private final int x, y, width, height;

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

        // Find the active module button for positioning
        ModuleButton activeModuleBtn = getActiveModuleButton();
        if (activeModuleBtn == null) return;

        // Position panel relative to the module button
        int panelX = activeModuleBtn.x + activeModuleBtn.width + 5;
        int panelY = activeModuleBtn.y;
        int panelWidth = Dimensions.COMMAND_PANEL_WIDTH;
        int panelHeight = Math.min(Dimensions.GUI_HEIGHT - 60, height - panelY - 10);

        // Adjust position if it would go off-screen
        if (panelX + panelWidth > width) {
            panelX = activeModuleBtn.x - panelWidth - 5;
        }
        if (panelY + panelHeight > height) {
            panelY = height - panelHeight - 10;
        }

        int commandX = panelX + 5;
        int commandY = panelY + 25;
        int commandWidth = panelWidth - 10;
        int commandHeight = 20;

        List<CommandConfig> commandConfigs = Arrays.asList(
                new CommandConfig("Remove Fire Overlay", "Disables the fire overlay effect"),
                new CommandConfig("Ignore Blindness", "Removes blindness effect"),
                new CommandConfig("Clear Liquid Vision", "Clears water/lava fog")
        );

        for (CommandConfig config : commandConfigs) {
            addNoDebuffToggle(config.name, config.description, commandX, commandY, commandWidth, 18); // Height reduced to 18
            commandY += 22; // Reduced from 25
        }

        // Update command scroll parameters with correct panel position
        int totalHeight = commandToggles.size() * 25;
        commandScroll.update(totalHeight, panelHeight - 25); // Subtract title height
        commandScroll.updateScrollbarPosition(
                panelX + panelWidth - Dimensions.SCROLLBAR_WIDTH - 2,
                panelY + 25, // Start below title
                panelHeight - 25 // Subtract title height
        );
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
}