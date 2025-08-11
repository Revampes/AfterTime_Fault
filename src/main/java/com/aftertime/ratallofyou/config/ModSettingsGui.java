package com.aftertime.ratallofyou.config;

import com.aftertime.ratallofyou.config.ModConfig.ModuleInfo;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiButton;
import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ModSettingsGui extends GuiScreen {
    // Colors
    private static final int BACKGROUND_COLOR = new Color(10, 15, 40).getRGB();
    private static final int CATEGORY_COLOR = new Color(20, 25, 60).getRGB();
    private static final int MODULE_INACTIVE_COLOR = new Color(30, 35, 80).getRGB();
    private static final int MODULE_ACTIVE_COLOR = new Color(50, 200, 50).getRGB();
    private static final int TEXT_COLOR = Color.WHITE.getRGB();
    private static final int HIGHLIGHT_COLOR = new Color(100, 150, 255).getRGB();

    // Layout
    private static final int GUI_WIDTH = 400;
    private static final int GUI_HEIGHT = 300;
    private int guiLeft, guiTop;

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

        // Create category buttons
        int categoryY = guiTop + 30;
        for (String category : getUniqueCategories()) {
            GuiButton btn = new GuiButton(-1, guiLeft + 10, categoryY, 100, 20, category);
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
        moduleButtons.clear(); // Clear existing buttons first

        int moduleX = guiLeft + 120;
        int moduleY = guiTop + 30;
        int moduleWidth = GUI_WIDTH - 140;
        int moduleHeight = 60;

        // Debug print to verify category filtering
        System.out.println("Creating buttons for category: " + selectedCategory);

        for (ModuleInfo module : ModConfig.MODULES) {
            // Debug print for each module
            System.out.println("Checking module: " + module.name + " in category: " + module.category);

            if (module.category.equals(selectedCategory)) {
                ModuleButton btn = new ModuleButton(
                        module,
                        moduleX,
                        moduleY,
                        moduleWidth,
                        moduleHeight,
                        module.enabled ? MODULE_ACTIVE_COLOR : MODULE_INACTIVE_COLOR
                );
                moduleButtons.add(btn);
                moduleY += moduleHeight + 10;

                // Debug print for added module
                System.out.println("Added module: " + module.name);
            }
        }
    }

    private void handleModuleToggle(ModuleInfo module) {
        module.enabled = !module.enabled;
        ModConfig.saveConfig();
    }

    private void handleSliderAdjustment(ModuleInfo module, float value) {
        module.sliderValue = value;
        ModConfig.saveConfig();
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (categoryButtons.contains(button)) {
            selectedCategory = button.displayString;
            createModuleButtons();
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        try {
            super.mouseClicked(mouseX, mouseY, mouseButton);

            // Check module toggles
            for (ModuleButton moduleBtn : moduleButtons) {
                if (moduleBtn.isMouseOver(mouseX, mouseY)) {
                    handleModuleToggle(moduleBtn.module);
                    moduleBtn.color = moduleBtn.module.enabled ? MODULE_ACTIVE_COLOR : MODULE_INACTIVE_COLOR;
                    return;
                }
            }

            // Check slider adjustments
            for (ModuleButton moduleBtn : moduleButtons) {
                if (moduleBtn.module.hasSlider() && moduleBtn.isMouseOver(mouseX, mouseY)) {
                    int sliderY = moduleBtn.y + moduleBtn.height - 20;
                    if (mouseY >= sliderY - 5 && mouseY <= sliderY + 10) {
                        float newValue = (float)(mouseX - (moduleBtn.x + 10)) / (moduleBtn.width - 20);
                        handleSliderAdjustment(moduleBtn.module, Math.max(0, Math.min(1, newValue)));
                        return;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawRect(guiLeft, guiTop, guiLeft + GUI_WIDTH, guiTop + GUI_HEIGHT, BACKGROUND_COLOR);
        drawCenteredString(fontRendererObj, "Rat All Of You", width / 2, guiTop + 10, TEXT_COLOR);

        // Draw panels
        drawRect(guiLeft + 5, guiTop + 25, guiLeft + 115, guiTop + GUI_HEIGHT - 5, CATEGORY_COLOR);
        drawRect(guiLeft + 115, guiTop + 25, guiTop + GUI_WIDTH - 5, guiTop + GUI_HEIGHT - 5, CATEGORY_COLOR);

        // Draw modules
        for (ModuleButton moduleBtn : moduleButtons) {
            moduleBtn.draw(mouseX, mouseY);

            // Draw text
            fontRendererObj.drawStringWithShadow(moduleBtn.module.name, moduleBtn.x + 10, moduleBtn.y + 10, TEXT_COLOR);

            List<String> descLines = fontRendererObj.listFormattedStringToWidth(moduleBtn.module.description, moduleBtn.width - 20);
            for (int i = 0; i < descLines.size(); i++) {
                fontRendererObj.drawString(descLines.get(i), moduleBtn.x + 10, moduleBtn.y + 25 + (i * 9), 0xAAAAAA);
            }

            if (moduleBtn.module.hasSlider()) {
                // Draw slider
                drawRect(moduleBtn.x + 10, moduleBtn.y + moduleBtn.height - 20,
                        moduleBtn.x + moduleBtn.width - 10, moduleBtn.y + moduleBtn.height - 15, 0xFF333333);

                int sliderPos = moduleBtn.x + 10 + (int)((moduleBtn.width - 20) * moduleBtn.module.sliderValue);
                drawRect(sliderPos - 3, moduleBtn.y + moduleBtn.height - 22,
                        sliderPos + 3, moduleBtn.y + moduleBtn.height - 13,
                        moduleBtn.module.enabled ? MODULE_ACTIVE_COLOR : HIGHLIGHT_COLOR);
            }
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private class ModuleButton {
        public final ModuleInfo module;
        public final int x, y, width, height;
        public int color;

        public ModuleButton(ModuleInfo module, int x, int y, int width, int height, int color) {
            this.module = module;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.color = color;
        }

        public void draw(int mouseX, int mouseY) {
            drawRect(x, y, x + width, y + height, color);
            if (isMouseOver(mouseX, mouseY)) {
                drawRect(x, y, x + width, y + height, 0x40FFFFFF);
            }
        }

        public boolean isMouseOver(int mouseX, int mouseY) {
            return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}