package com.aftertime.ratallofyou.UI.config;

import net.minecraft.client.gui.Gui;

public class ScrollManager {
    private int offset = 0;
    private int maxOffset = 0;
    private int scrollbarX, scrollbarY, scrollbarHeight;
    private int contentHeight, viewHeight;
    public boolean isDragging = false;
    private int dragStartY = 0;
    private int dragStartOffset = 0;
    private Runnable onScrollCallback;

    public ScrollManager() {
        reset();
    }

    public void reset() {
        this.offset = 0;
        this.maxOffset = 0;
        this.isDragging = false;
    }

    public void update(int contentHeight, int viewHeight) {
        this.contentHeight = contentHeight;
        this.viewHeight = viewHeight;
        this.maxOffset = Math.max(0, contentHeight - viewHeight);
        this.offset = Math.max(0, Math.min(this.offset, this.maxOffset));
    }

    public void updateScrollbarPosition(int x, int y, int height) {
        this.scrollbarX = x;
        this.scrollbarY = y;
        this.scrollbarHeight = height;
    }

    public int getOffset() {
        return offset;
    }

    public boolean shouldRenderScrollbar() {
        return maxOffset > 0;
    }

    public void drawScrollbar(int backgroundColor, int handleColor) {
        if (!shouldRenderScrollbar()) return;

        // Draw scrollbar background
        Gui.drawRect(scrollbarX, scrollbarY, scrollbarX + 8, scrollbarY + scrollbarHeight, backgroundColor);

        // Calculate handle position and size
        float handleRatio = (float) viewHeight / contentHeight;
        int handleHeight = Math.max(10, (int) (scrollbarHeight * handleRatio));

        float offsetRatio = (float) offset / maxOffset;
        int handleY = scrollbarY + (int) ((scrollbarHeight - handleHeight) * offsetRatio);

        // Draw handle
        Gui.drawRect(scrollbarX + 1, handleY, scrollbarX + 7, handleY + handleHeight, handleColor);
    }

    public boolean checkScrollbarClick(int mouseX, int mouseY) {
        if (!shouldRenderScrollbar()) return false;

        boolean inScrollbar = mouseX >= scrollbarX && mouseX <= scrollbarX + 8 &&
                             mouseY >= scrollbarY && mouseY <= scrollbarY + scrollbarHeight;

        if (inScrollbar) {
            isDragging = true;
            dragStartY = mouseY;
            dragStartOffset = offset;
            return true;
        }
        return false;
    }

    public void handleDrag(int mouseX, int mouseY, Runnable callback) {
        if (!isDragging) return;

        this.onScrollCallback = callback;
        int deltaY = mouseY - dragStartY;
        float scrollRatio = (float) deltaY / scrollbarHeight;
        int newOffset = dragStartOffset + (int) (scrollRatio * maxOffset);

        offset = Math.max(0, Math.min(newOffset, maxOffset));

        if (callback != null) {
            callback.run();
        }
    }

    public void endScroll() {
        isDragging = false;
        onScrollCallback = null;
    }

    public void scroll(int delta) {
        offset = Math.max(0, Math.min(offset + delta, maxOffset));
    }
}
