package com.aftertime.ratallofyou.UI;

import com.aftertime.ratallofyou.UI.ModConfig.ModuleInfo;
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
    private static final int BACKGROUND_COLOR = new Color(10, 15, 40).getRGB();
    private static final int CATEGORY_COLOR = 0xDD1A2244;
    private static final int MODULE_INACTIVE_COLOR = 0xDD252D5D;
    private static final int MODULE_ACTIVE_COLOR = 0xDD32CD32;
    private static final int TEXT_COLOR = 0xFFFFFFFF;
    private static final int HIGHLIGHT_COLOR = new Color(100, 150, 255).getRGB();
    private static final int PANEL_COLOR = 0xDD111933;
    private static final int CATEGORY_BUTTON_COLOR = new Color(20, 25, 60).getRGB();
    private static final int VERSION_COLOR = 0xFFAAAAAA;
    private static final int SCROLLBAR_COLOR = 0xFF555577;
    private static final int SCROLLBAR_HANDLE_COLOR = 0xFF8888AA;
    private static final int MOVE_GUI_COLOR = 0xDD252D5D;

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

    // State
    private String selectedCategory = "Kuudra";
    private List<GuiButton> categoryButtons = new ArrayList<GuiButton>();
    private List<ModuleButton> moduleButtons = new ArrayList<ModuleButton>();

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
            GuiButton btn = new GuiButton(-1, guiLeft + 10, categoryY, 100, 20, category) {
                @Override
                public void drawButton(Minecraft mc, int mouseX, int mouseY) {
                    if (this.visible) {
                        drawRect(xPosition, yPosition, xPosition + width, yPosition + height, CATEGORY_BUTTON_COLOR);
                        int textColor = selectedCategory.equals(displayString) ? 0xFFFF00 : 0xFFFFFF;
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
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (categoryButtons.contains(button)) {
            selectedCategory = button.displayString;
            scrollOffset = 0;
            createModuleButtons();
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        try {
            super.mouseClicked(mouseX, mouseY, mouseButton);

            // Check scrollbar click
            if (maxScrollOffset > 0 && mouseX >= scrollBarX && mouseX <= scrollBarX + scrollBarWidth &&
                    mouseY >= scrollBarY && mouseY <= scrollBarY + (GUI_HEIGHT - 60)) {
                isScrolling = true;
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        isScrolling = false;
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        if (isScrolling && maxScrollOffset > 0) {
            float scrollableArea = GUI_HEIGHT - 60 - scrollBarHeight;
            float scrollPos = Math.min(Math.max(mouseY - scrollBarY - (scrollBarHeight / 2), 0), scrollableArea);
            scrollOffset = (int)((scrollPos / scrollableArea) * maxScrollOffset);
            createModuleButtons();
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int scroll = Mouse.getEventDWheel();
        if (scroll != 0) {
            scrollOffset += scroll > 0 ? -20 : 20;
            scrollOffset = Math.max(0, Math.min(scrollOffset, maxScrollOffset));
            createModuleButtons();
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
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

        // Setup scissor for modules area
        int scale = new ScaledResolution(Minecraft.getMinecraft()).getScaleFactor();
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
            int bgColor = module.name.equals("Move GUI Position")
                    ? MOVE_GUI_COLOR
                    : (module.enabled ? MODULE_ACTIVE_COLOR : MODULE_INACTIVE_COLOR);

            drawRect(x, y, x + width, y + height, bgColor);

            // Draw module name
            fontRendererObj.drawStringWithShadow(module.name, x + textPadding, y + textPadding, TEXT_COLOR);

            // Draw description
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

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}