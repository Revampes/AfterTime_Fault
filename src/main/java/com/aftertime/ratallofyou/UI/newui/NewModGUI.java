package com.aftertime.ratallofyou.UI.newui;

import com.aftertime.ratallofyou.UI.newui.categories.CategoryPanel;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.Minecraft;
import com.aftertime.ratallofyou.UI.newui.config.UIConfigManager;
import com.aftertime.ratallofyou.UI.newui.config.ModConfigIO;

import java.io.IOException;
import java.util.*;

public class NewModGUI extends GuiScreen {
    private List<CategoryPanel> categories = new ArrayList<>();
    private int categoryWidth = 80; // Reduced to fit more categories per row
    private int padding = 5;
    private Minecraft mc = Minecraft.getMinecraft();

    @Override
    public void initGui() {
        categories.clear();

        // Build categories dynamically from annotated ModConfig
        Map<String, CategoryPanel> categoryMap = UIConfigManager.createUICategories();

        // Deterministic order: sort by name
        List<String> names = new ArrayList<>(categoryMap.keySet());
        Collections.sort(names);

        // Layout categories horizontally
        for (int i = 0; i < names.size(); i++) {
            String name = names.get(i);
            CategoryPanel panel = categoryMap.get(name);
            int x = padding + i * (categoryWidth + padding);
            int y = padding;
            int height = this.height - padding * 2;
            panel.setBounds(x, y, categoryWidth, height);
            categories.add(panel);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();

        for (CategoryPanel category : categories) {
            category.draw(mouseX, mouseY);
        }

        // Draw overlays on top after all base content
        for (CategoryPanel category : categories) {
            category.drawOverlays(mouseX, mouseY);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        // Route overlay clicks first so popups capture input and close properly
        for (CategoryPanel category : categories) {
            if (category.mouseClickedOverlay(mouseX, mouseY, mouseButton)) {
                return;
            }
        }

        for (CategoryPanel category : categories) {
            if (category.mouseClicked(mouseX, mouseY, mouseButton)) {
                return;
            }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        for (CategoryPanel category : categories) {
            category.mouseReleased(mouseX, mouseY, mouseButton);
        }
        super.mouseReleased(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        for (CategoryPanel category : categories) {
            category.keyTyped(typedChar, keyCode);
        }

        if (keyCode == 1) {
            this.mc.displayGuiScreen(null);
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();

        // If any overlay is open, avoid scrolling to prevent UI from jumping while interacting
        boolean anyOverlayOpen = false;
        for (CategoryPanel category : categories) {
            if (category.hasAnyOverlayOpen()) { anyOverlayOpen = true; break; }
        }
        if (anyOverlayOpen) return;

        int scroll = org.lwjgl.input.Mouse.getEventDWheel();
        if (scroll != 0) {
            int mouseX = org.lwjgl.input.Mouse.getEventX() * this.width / this.mc.displayWidth;
            int mouseY = this.height - org.lwjgl.input.Mouse.getEventY() * this.height / this.mc.displayHeight - 1;

            for (CategoryPanel category : categories) {
                if (category.isMouseOver(mouseX, mouseY)) {
                    category.handleMouseScroll(scroll > 0 ? -1 : 1);
                    break;
                }
            }
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void onGuiClosed() {
        // Persist ModConfig when leaving the new UI
        ModConfigIO.save();
        super.onGuiClosed();
    }
}