package com.aftertime.ratallofyou.UI.newui.categories;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.Minecraft;
import com.aftertime.ratallofyou.UI.newui.util.TextRender;

import java.util.ArrayList;
import java.util.List;

public class CategoryPanel {
    private String categoryName;
    private int x, y, width, height;
    private List<ModulePanel> modules = new ArrayList<>();
    private int scrollOffset = 0;
    private Minecraft mc = Minecraft.getMinecraft();

    public CategoryPanel(String categoryName, int x, int y, int width, int height) {
        this.categoryName = categoryName;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void draw(int mouseX, int mouseY) {
        // Draw category background (opaque grey)
        Gui.drawRect(x, y, x + width, y + height, 0xFF444444);

        // Draw category title (scaled)
        int titleW = TextRender.width(mc.fontRendererObj, categoryName);
        int titleX = x + (width - titleW) / 2;
        TextRender.draw(mc.fontRendererObj, categoryName, titleX, y + 5, 0xFFFFFF);

        // Draw modules
        int moduleY = y + 25 - scrollOffset;
        for (ModulePanel module : modules) {
            if (moduleY + module.getHeight() > y && moduleY < y + height) {
                // Ensure module and its toggle fill the column width minus padding
                module.setBounds(x + 5, moduleY, width - 10);
                module.draw(mouseX, mouseY);
            }
            // Remove gap between toggle buttons/modules
            moduleY += module.getHeight();
        }

        // Draw scroll bar if needed
        drawScrollBar(mouseX, mouseY);
    }

    // New: draw overlays after normal content so they appear on top
    public void drawOverlays(int mouseX, int mouseY) {
        int moduleY = y + 25 - scrollOffset;
        for (ModulePanel module : modules) {
            if (moduleY + module.getHeight() > y && moduleY < y + height) {
                module.drawOverlays(mouseX, mouseY);
            }
            moduleY += module.getHeight();
        }
    }

    private void drawScrollBar(int mouseX, int mouseY) {
        int totalHeight = getTotalHeight();
        if (totalHeight <= height - 25) return;

        float visibleRatio = (float)(height - 25) / totalHeight;
        int scrollbarHeight = (int)((height - 25) * visibleRatio);
        int scrollbarY = y + 25 + (int)(scrollOffset * ((height - 25 - scrollbarHeight) / (float)(totalHeight - (height - 25))));

        Gui.drawRect(x + width - 5, scrollbarY, x + width - 2, scrollbarY + scrollbarHeight, 0xFF888888);
    }

    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (!isMouseOver(mouseX, mouseY)) return false;

        // Check modules
        int moduleY = y + 25 - scrollOffset;
        for (ModulePanel module : modules) {
            if (moduleY + module.getHeight() > y && moduleY < y + height) {
                if (module.mouseClicked(mouseX, mouseY, mouseButton)) {
                    return true;
                }
            }
            moduleY += module.getHeight();
        }
        return false;
    }

    // New: route overlay clicks before standard clicks, not constrained by panel bounds since overlays can extend
    public boolean mouseClickedOverlay(int mouseX, int mouseY, int mouseButton) {
        int moduleY = y + 25 - scrollOffset;
        boolean handled = false;
        for (ModulePanel module : modules) {
            if (moduleY + module.getHeight() > y && moduleY < y + height) {
                if (module.mouseClickedOverlay(mouseX, mouseY, mouseButton)) {
                    handled = true;
                    break;
                }
            }
            moduleY += module.getHeight();
        }
        return handled;
    }

    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        for (ModulePanel module : modules) {
            module.mouseReleased(mouseX, mouseY, mouseButton);
        }
    }

    public void keyTyped(char typedChar, int keyCode) {
        for (ModulePanel module : modules) {
            module.keyTyped(typedChar, keyCode);
        }
    }

    public void handleMouseScroll(int delta) {
        scrollOffset += delta * 10;
        scrollOffset = Math.max(0, Math.min(scrollOffset, getTotalHeight() - (height - 25)));
    }

    private int getTotalHeight() {
        int total = 0;
        for (ModulePanel module : modules) {
            total += module.getHeight();
        }
        return total;
    }

    public boolean isMouseOver(int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    public void addModule(ModulePanel module) {
        modules.add(module);
    }

    // Getters for layout
    public int getWidth() { return width; }
    public int getX() { return x; }

    // New: allow repositioning and resizing by the GUI screen
    public void setBounds(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    // New: check if any module has an overlay open
    public boolean hasAnyOverlayOpen() {
        for (ModulePanel module : modules) {
            if (module.hasAnyOverlayOpen()) return true;
        }
        return false;
    }

    // New: preferred height based on content (title + modules)
    public int getPreferredHeight() {
        return 25 + getTotalHeight();
    }
}