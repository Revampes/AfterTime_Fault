package com.aftertime.ratallofyou.UI;

import com.aftertime.ratallofyou.UI.ModConfig.ModuleInfo;
import com.aftertime.ratallofyou.modules.SkyBlock.ChatCommands;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Mouse;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import static org.lwjgl.opengl.GL11.*;

public class ModSettingsGui extends GuiScreen {
    // Colors
    private static final int BACKGROUND_COLOR = new Color(0, 0, 0).getRGB(); // Pure black
    private static final int CATEGORY_COLOR = 0xFF111111; // Dark grey
    private static final int MODULE_INACTIVE_COLOR = 0xFF333333; // Medium grey
    private static final int MODULE_ACTIVE_COLOR = 0xFF006400; // Dark green
    private static final int TEXT_COLOR = 0xFFFFFFFF; // White text
    private static final int HIGHLIGHT_COLOR = 0xFF444444; // Grey highlight
    private static final int PANEL_COLOR = 0xFF000000; // Black panels
    private static final int CATEGORY_BUTTON_COLOR = 0xFF222222; // Dark grey buttons
    private static final int SELECTED_CATEGORY_COLOR = 0xFF555555; // Grey for selected category
    private static final int VERSION_COLOR = 0xFF888888; // Grey version text
    private static final int SCROLLBAR_COLOR = 0xFF333333; // Dark grey scrollbar
    private static final int SCROLLBAR_HANDLE_COLOR = 0xFF555555; // Medium grey handle
    private static final int MOVE_GUI_COLOR = 0xFF333333; // Dark grey for move button
    private static final int COMMAND_PANEL_COLOR = 0xFF111111; // Dark grey command panel
    private static final int COMMAND_CHECKBOX_COLOR = 0xFF333333; // Dark grey checkbox
    private static final int COMMAND_CHECKBOX_SELECTED_COLOR = 0xFF006400; // Dark green selected

    // Layout
    private static final int GUI_WIDTH = 400;
    private static final int GUI_HEIGHT = 300;
    private int guiLeft, guiTop;

    // Scrollbar
    private int scrollOffset = 0;
    private int maxScrollOffset = 0;
    private boolean isScrolling = false;
    private int scrollBarWidth = 6;
    private int scrollBarHeight = 30;
    private int scrollBarX, scrollBarY;
    private static final int COMMAND_SCROLLBAR_COLOR = 0xFF555577;
    private static final int COMMAND_SCROLLBAR_HANDLE_COLOR = 0xFF8888AA;
    private int commandScrollOffset = 0;
    private int commandMaxScrollOffset = 0;
    private boolean isCommandScrolling = false;
    private int commandScrollBarX, commandScrollBarY;
    private int commandScrollBarWidth = 6;
    private int commandScrollBarHeight = 30;


    // State
    private String selectedCategory = "Kuudra";
    private List<GuiButton> categoryButtons = new ArrayList<GuiButton>();
    private List<ModuleButton> moduleButtons = new ArrayList<ModuleButton>();

    private boolean showCommandSettings = false;
    private ModuleInfo selectedCommandModule = null;
    private List<CommandToggle> commandToggles = new ArrayList<CommandToggle>();

    @Override
    public void initGui() {
        this.guiLeft = (this.width - GUI_WIDTH) / 2;
        this.guiTop = (this.height - GUI_HEIGHT) / 2;
        this.buttonList.clear();
        this.categoryButtons.clear();
        this.moduleButtons.clear();
        this.scrollOffset = 0;

        // Create category buttons
        int categoryY = guiTop + 30;
        for (String category : getUniqueCategories()) {
            // In initGui(), modify the category button creation:
            GuiButton btn = new GuiButton(-1, guiLeft + 10, categoryY, 100, 20, category) {
                @Override
                public void drawButton(Minecraft mc, int mouseX, int mouseY) {
                    if (this.visible) {
                        int bgColor = selectedCategory.equals(displayString) ? SELECTED_CATEGORY_COLOR : CATEGORY_BUTTON_COLOR;
                        drawRect(xPosition, yPosition, xPosition + width, yPosition + height, bgColor);
                        int textColor = selectedCategory.equals(displayString) ? 0xFFFFFFFF : 0xCCCCCC;
                        this.drawCenteredString(mc.fontRendererObj, displayString, xPosition + width / 2, yPosition + (height - 8) / 2, textColor);
                    }
                }
            };
            categoryButtons.add(btn);
            this.buttonList.add(btn);
            categoryY += 25;
        }
        createModuleButtons();
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
        maxScrollOffset = 0;

        int moduleX = guiLeft + 120;
        int moduleY = guiTop + 30;
        int moduleWidth = GUI_WIDTH - 140 - scrollBarWidth;
        int moduleHeight = 50;
        int modulesAreaHeight = GUI_HEIGHT - 60;

        // Count modules in current category
        int moduleCount = 0;
        for (ModuleInfo module : ModConfig.MODULES) {
            if (module.category.equals(selectedCategory)) moduleCount++;
        }

        // Calculate max scroll offset
        int totalModulesHeight = moduleCount * (moduleHeight + 10);
        maxScrollOffset = Math.max(0, totalModulesHeight - modulesAreaHeight);
        scrollOffset = Math.min(scrollOffset, maxScrollOffset);
        scrollOffset = Math.max(0, scrollOffset);

        // Create buttons for visible modules
        int currentY = moduleY - scrollOffset;
        for (ModuleInfo module : ModConfig.MODULES) {
            if (module.category.equals(selectedCategory)) {
                if (currentY + moduleHeight > guiTop + 30 && currentY < guiTop + GUI_HEIGHT - 30) {
                    ModuleButton btn = new ModuleButton(
                            module,
                            moduleX,
                            currentY,
                            moduleWidth,
                            moduleHeight,
                            module.enabled ? MODULE_ACTIVE_COLOR : MODULE_INACTIVE_COLOR
                    );
                    moduleButtons.add(btn);
                }
                currentY += moduleHeight + 10;
            }
        }

        // Update scrollbar
        scrollBarX = guiLeft + GUI_WIDTH - scrollBarWidth - 10;
        scrollBarY = guiTop + 30;
        scrollBarHeight = (int)((modulesAreaHeight / (float)Math.max(totalModulesHeight, modulesAreaHeight)) * modulesAreaHeight);
        scrollBarHeight = Math.max(20, Math.min(scrollBarHeight, modulesAreaHeight));
    }

    private void handleModuleToggle(ModuleInfo module) {
        module.enabled = !module.enabled;
        ModConfig.saveConfig();
        createModuleButtons();

        if (module.name.equals("Party Commands")) {
            selectedCommandModule = module;
            initCommandToggles();
        } else {
            showCommandSettings = false;
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (categoryButtons.contains(button)) {
            selectedCategory = button.displayString;
            scrollOffset = 0;
            showCommandSettings = false;
            createModuleButtons();
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        try {
            super.mouseClicked(mouseX, mouseY, mouseButton);

            // Check command scrollbar click
            if (showCommandSettings && commandMaxScrollOffset > 0 &&
                    mouseX >= commandScrollBarX && mouseX <= commandScrollBarX + commandScrollBarWidth &&
                    mouseY >= commandScrollBarY && mouseY <= commandScrollBarY + (GUI_HEIGHT - 60)) {
                isCommandScrolling = true;
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
                    if (moduleBtn.module.name.equals("Move GUI Position")) {
                        UIHighlighter.enterMoveMode(this);
                        return;
                    } else {
                        handleModuleToggle(moduleBtn.module);
                        return;
                    }
                }
            }

            // Check command toggles
            if (showCommandSettings) {
                for (CommandToggle toggle : commandToggles) {
                    if (toggle.isMouseOver(mouseX, mouseY)) {
                        toggle.enabled = !toggle.enabled;
                        ChatCommands.setCommandEnabled(toggle.getConfigKey(), toggle.enabled);
                        return;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        isScrolling = false;
        isCommandScrolling = false;
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        if (isCommandScrolling && commandMaxScrollOffset > 0) {
            float scrollableArea = GUI_HEIGHT - 60 - commandScrollBarHeight;
            float scrollPos = Math.min(Math.max(mouseY - commandScrollBarY - (commandScrollBarHeight / 2), 0), scrollableArea);
            commandScrollOffset = (int)((scrollPos / scrollableArea) * commandMaxScrollOffset);
        }
        if (isScrolling && maxScrollOffset > 0) {
            float scrollableArea = GUI_HEIGHT - 60 - scrollBarHeight;
            float scrollPos = Math.min(Math.max(mouseY - scrollBarY - (scrollBarHeight / 2), 0), scrollableArea);
            scrollOffset = (int)((scrollPos / scrollableArea) * maxScrollOffset);
            createModuleButtons();
        } else if (isCommandScrolling && commandMaxScrollOffset > 0) {
            float scrollableArea = GUI_HEIGHT - 60 - commandScrollBarHeight;
            float scrollPos = Math.min(Math.max(mouseY - commandScrollBarY - (commandScrollBarHeight / 2), 0), scrollableArea);
            commandScrollOffset = (int)((scrollPos / scrollableArea) * commandMaxScrollOffset);
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int scroll = Mouse.getEventDWheel();

        if (showCommandSettings && commandMaxScrollOffset > 0) {
            if (scroll != 0) {
                commandScrollOffset -= scroll > 0 ? 20 : -20; //Inverted for natural scrolling
                commandScrollOffset = Math.max(0, Math.min(commandScrollOffset, commandMaxScrollOffset));
                return;
            }
        }

        if (scroll != 0) {
            scrollOffset += scroll > 0 ? -20 : 20;
            scrollOffset = Math.max(0, Math.min(scrollOffset, maxScrollOffset));
            createModuleButtons();
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // Get scale factor once at the start of the method
        int scale = new ScaledResolution(Minecraft.getMinecraft()).getScaleFactor();

        // Draw background
        drawRect(guiLeft, guiTop, guiLeft + GUI_WIDTH, guiTop + GUI_HEIGHT, PANEL_COLOR);
        drawCenteredString(fontRendererObj, "§l§nRat All Of You", width / 2, guiTop + 10, TEXT_COLOR);
        drawRect(guiLeft + 5, guiTop + 25, guiLeft + 115, guiTop + GUI_HEIGHT - 5, CATEGORY_COLOR);
        drawRect(guiLeft + 115, guiTop + 25, guiLeft + GUI_WIDTH - 5, guiTop + GUI_HEIGHT - 5, CATEGORY_COLOR);

        // Draw version info
        drawCenteredString(fontRendererObj, "§7Version v1.0 §8| §7Created by AfterTime",
                width / 2, guiTop + GUI_HEIGHT - 20, VERSION_COLOR);

        // Draw category buttons
        for (GuiButton btn : categoryButtons) {
            drawRect(btn.xPosition - 2, btn.yPosition - 2,
                    btn.xPosition + btn.width + 2, btn.yPosition + btn.height + 2,
                    CATEGORY_BUTTON_COLOR);
        }

        // Setup scissor for modules area (using the scale variable we already declared)
        int scissorX = guiLeft + 115;
        int scissorY = guiTop + 25;
        int scissorWidth = GUI_WIDTH - 120 - scrollBarWidth;
        int scissorHeight = GUI_HEIGHT - 50;

        glEnable(GL_SCISSOR_TEST);
        glScissor(scissorX * scale, (height - (scissorY + scissorHeight)) * scale,
                scissorWidth * scale, scissorHeight * scale);

        // Draw modules
        for (ModuleButton moduleBtn : moduleButtons) {
            moduleBtn.draw(mouseX, mouseY);
        }

        glDisable(GL_SCISSOR_TEST);

        // Draw scrollbar if needed
        if (maxScrollOffset > 0) {
            // Scrollbar track
            drawRect(scrollBarX, scrollBarY,
                    scrollBarX + scrollBarWidth, scrollBarY + (GUI_HEIGHT - 60),
                    SCROLLBAR_COLOR);

            // Scrollbar handle
            int handleY = scrollBarY + (int)(((float)scrollOffset / maxScrollOffset) *
                    ((GUI_HEIGHT - 60) - scrollBarHeight));
            drawRect(scrollBarX, handleY,
                    scrollBarX + scrollBarWidth, handleY + scrollBarHeight,
                    SCROLLBAR_HANDLE_COLOR);
        }

        // Draw command settings panel if enabled
        if (showCommandSettings && selectedCommandModule != null) {
            int panelX = guiLeft + GUI_WIDTH;
            int panelWidth = 170;
            int panelHeight = GUI_HEIGHT - 60;

            // Draw panel background
            drawRect(panelX, guiTop, panelX + panelWidth, guiTop + GUI_HEIGHT, COMMAND_PANEL_COLOR);

            // Draw title
            drawCenteredString(fontRendererObj, "Command Settings", panelX + panelWidth / 2, guiTop + 10, TEXT_COLOR);

            // Setup scissor for command toggles area (reusing the existing scale variable)
            glEnable(GL_SCISSOR_TEST);
            glScissor(panelX * scale, (height - (guiTop + 30 + panelHeight)) * scale,
                    (panelWidth - commandScrollBarWidth - 5) * scale, panelHeight * scale);

            // Draw toggles with scroll offset
            for (CommandToggle toggle : commandToggles) {
                toggle.draw(mouseX, mouseY + commandScrollOffset);
            }

            glDisable(GL_SCISSOR_TEST);

            // Draw scrollbar if needed
            if (commandMaxScrollOffset > 0) {
                // Scrollbar track
                drawRect(commandScrollBarX, commandScrollBarY,
                        commandScrollBarX + commandScrollBarWidth, commandScrollBarY + panelHeight,
                        COMMAND_SCROLLBAR_COLOR);

                // Scrollbar handle
                int handleY = commandScrollBarY + (int)(((float)commandScrollOffset / commandMaxScrollOffset) *
                        (panelHeight - commandScrollBarHeight));
                drawRect(commandScrollBarX, handleY,
                        commandScrollBarX + commandScrollBarWidth, handleY + commandScrollBarHeight,
                        COMMAND_SCROLLBAR_HANDLE_COLOR);
            }
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private class ModuleButton {
        public final ModuleInfo module;
        public final int x, y, width, height;
        public int color;
        private final int textPadding = 10;
        private final int lineHeight = 9;

        public ModuleButton(ModuleInfo module, int x, int y, int width, int height, int color) {
            this.module = module;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.color = color;
        }

        public void draw(int mouseX, int mouseY) {
            // Draw background
            int bgColor;
            if (module.name.equals("Move GUI Position")) {
                bgColor = MOVE_GUI_COLOR;
            } else {
                bgColor = module.enabled ? MODULE_ACTIVE_COLOR : MODULE_INACTIVE_COLOR;
            }

            drawRect(x, y, x + width, y + height, bgColor);

            // Draw module name
            fontRendererObj.drawStringWithShadow(module.name, x + textPadding, y + textPadding, TEXT_COLOR);

            // Draw description (grey text)
            List<String> descLines = fontRendererObj.listFormattedStringToWidth(module.description, width - textPadding * 2);
            for (int i = 0; i < descLines.size(); i++) {
                fontRendererObj.drawString(
                        descLines.get(i),
                        x + textPadding,
                        y + textPadding + 12 + (i * lineHeight),
                        0xAAAAAA
                );
            }

            // Hover effect
            if (isMouseOver(mouseX, mouseY)) {
                drawRect(x, y, x + width, y + height, 0x40FFFFFF);
            }
        }

        public boolean isMouseOver(int mouseX, int mouseY) {
            return mouseX >= x && mouseX <= x + width &&
                    mouseY >= y && mouseY <= y + height;
        }
    }

    private class CommandToggle {
        private final String name;
        private final String description;
        private boolean enabled;
        private final int x, y, width, height;

        public CommandToggle(String name, String description, boolean enabled, int x, int y, int width, int height) {
            this.name = name;
            this.description = description;
            this.enabled = enabled;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        public void draw(int mouseX, int mouseY) {
            int drawY = y - commandScrollOffset;

            if (drawY + height > guiTop + 30 && drawY < guiTop + GUI_HEIGHT - 30) {
                // Draw background
                drawRect(x, drawY, x + width, drawY + height,
                        enabled ? COMMAND_CHECKBOX_SELECTED_COLOR : COMMAND_CHECKBOX_COLOR);

                // Draw checkbox
                drawRect(x + 5, drawY + 5, x + 15, drawY + 15, 0xFF000000);
                if (enabled) {
                    drawRect(x + 7, drawY + 7, x + 13, drawY + 13, 0xFFFFFFFF);
                }

                // Draw text (white)
                fontRendererObj.drawStringWithShadow(name, x + 20, drawY + (height - 8) / 2, TEXT_COLOR);

                // Hover effect
                if (isMouseOver(mouseX, mouseY)) {
                    drawRect(x, drawY, x + width, drawY + height, 0x40FFFFFF);
                }
            }
        }

        public boolean isMouseOver(int mouseX, int mouseY) {
            int drawY = y - commandScrollOffset;
            return mouseX >= x && mouseX <= x + width &&
                    mouseY >= drawY && mouseY <= drawY + height;
        }

        public String getConfigKey() {
            return name.toLowerCase().replace(" ", "");
        }
    }

    private void initCommandToggles() {
        commandToggles.clear();

        if (selectedCommandModule == null || !selectedCommandModule.name.equals("Party Commands")) {
            showCommandSettings = false;
            return;
        }

        showCommandSettings = true;
        int commandX = guiLeft + GUI_WIDTH + 10;
        int commandY = guiTop + 30 - commandScrollOffset; // Apply scroll offset
        int commandWidth = 150;
        int commandHeight = 20;
        int panelHeight = GUI_HEIGHT - 60;

        // Add all command toggles with current states
        commandToggles.add(new CommandToggle("Warp", "Enable !warp command",
                ChatCommands.isCommandEnabled("warp"), commandX, commandY, commandWidth, commandHeight));
        commandY += 25;
        commandToggles.add(new CommandToggle("Warp Transfer", "Enable !warptransfer command",
                ChatCommands.isCommandEnabled("warptransfer"), commandX, commandY, commandWidth, commandHeight));
        commandY += 25;
        commandToggles.add(new CommandToggle("Coords", "Enable !coords command",
                ChatCommands.isCommandEnabled("coords"), commandX, commandY, commandWidth, commandHeight));
        commandY += 25;
        commandToggles.add(new CommandToggle("All Invite", "Enable !allinvite command",
                ChatCommands.isCommandEnabled("allinvite"), commandX, commandY, commandWidth, commandHeight));
        commandY += 25;
        commandToggles.add(new CommandToggle("Boop", "Enable !boop command",
                ChatCommands.isCommandEnabled("boop"), commandX, commandY, commandWidth, commandHeight));
        commandY += 25;
        commandToggles.add(new CommandToggle("Coin Flip", "Enable !cf command",
                ChatCommands.isCommandEnabled("cf"), commandX, commandY, commandWidth, commandHeight));
        commandY += 25;
        commandToggles.add(new CommandToggle("8Ball", "Enable !8ball command",
                ChatCommands.isCommandEnabled("8ball"), commandX, commandY, commandWidth, commandHeight));
        commandY += 25;
        commandToggles.add(new CommandToggle("Dice", "Enable !dice command",
                ChatCommands.isCommandEnabled("dice"), commandX, commandY, commandWidth, commandHeight));
        commandY += 25;
        commandToggles.add(new CommandToggle("Party Transfer", "Enable !pt command",
                ChatCommands.isCommandEnabled("pt"), commandX, commandY, commandWidth, commandHeight));
        commandY += 25;
        commandToggles.add(new CommandToggle("TPS", "Enable !tps command",
                ChatCommands.isCommandEnabled("tps"), commandX, commandY, commandWidth, commandHeight));
        commandY += 25;
        commandToggles.add(new CommandToggle("Downtime", "Enable !dt command",
                ChatCommands.isCommandEnabled("dt"), commandX, commandY, commandWidth, commandHeight));
        commandY += 25;
        commandToggles.add(new CommandToggle("Queue Instance", "Enable dungeon queue commands",
                ChatCommands.isCommandEnabled("queinstance"), commandX, commandY, commandWidth, commandHeight));
        commandY += 25;
        commandToggles.add(new CommandToggle("Demote", "Enable !demote command",
                ChatCommands.isCommandEnabled("demote"), commandX, commandY, commandWidth, commandHeight));
        commandY += 25;
        commandToggles.add(new CommandToggle("Promote", "Enable !promote command",
                ChatCommands.isCommandEnabled("promote"), commandX, commandY, commandWidth, commandHeight));


        int totalHeight = commandToggles.size() * 25;
        commandMaxScrollOffset = Math.max(0, totalHeight - panelHeight);
        commandScrollOffset = Math.min(commandScrollOffset, commandMaxScrollOffset);
        commandScrollOffset = Math.max(0, commandScrollOffset);

        // Position scrollbar
        commandScrollBarX = commandX + commandWidth + 5;
        commandScrollBarY = guiTop + 30;
        commandScrollBarHeight = (int)((panelHeight / (float)Math.max(totalHeight, panelHeight)) * panelHeight);
        commandScrollBarHeight = Math.max(20, Math.min(commandScrollBarHeight, panelHeight));
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}