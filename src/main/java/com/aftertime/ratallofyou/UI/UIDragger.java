package com.aftertime.ratallofyou.UI;

import com.aftertime.ratallofyou.UI.config.ConfigData.UIPosition;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;

public class UIDragger {
    private static final UIDragger INSTANCE = new UIDragger();
    private boolean isDragging = false;
    private UIPosition currentlyDragging = null;
    private int dragOffsetX, dragOffsetY;
    // Track element dimensions for proper clamping
    private int currentElementWidth = 30;
    private int currentElementHeight = 50;
    // Anchor offsets allow elements whose logical position isn't the top-left (e.g., centered x)
    private int anchorOffsetX = 0;
    private int anchorOffsetY = 0;

    public static UIDragger getInstance() {
        return INSTANCE;
    }

    public UIPosition getDraggedElement() {
        return currentlyDragging;
    }

    // Backward-compatible: default element size
    public boolean tryStartDrag(UIPosition pos, int mouseX, int mouseY) {
        return tryStartDrag(pos, mouseX, mouseY, 30, 50, 0, 0);
    }

    // Backward-compatible: element size with top-left anchoring
    public boolean tryStartDrag(UIPosition pos, int mouseX, int mouseY, int elementWidth, int elementHeight) {
        return tryStartDrag(pos, mouseX, mouseY, elementWidth, elementHeight, 0, 0);
    }

    // New: start drag with explicit element size and anchor offsets
    // anchorOffsetX/anchorOffsetY indicate how far the element's logical pos is from its top-left hitbox
    public boolean tryStartDrag(UIPosition pos, int mouseX, int mouseY, int elementWidth, int elementHeight, int anchorOffsetX, int anchorOffsetY) {
        if (pos == null) return false;

        // Compute top-left corner from logical position and anchor offsets
        int left = pos.x - anchorOffsetX;
        int top = pos.y - anchorOffsetY;

        // Hit test using provided mouse coords and dimensions
        if (mouseX >= left && mouseX <= left + elementWidth &&
                mouseY >= top && mouseY <= top + elementHeight) {
            currentlyDragging = pos;
            // Store drag offsets relative to the hitbox top-left
            dragOffsetX = mouseX - left;
            dragOffsetY = mouseY - top;
            currentElementWidth = elementWidth;
            currentElementHeight = elementHeight;
            this.anchorOffsetX = anchorOffsetX;
            this.anchorOffsetY = anchorOffsetY;
            isDragging = true;
            return true;
        }
        return false;
    }

    public void updateDragPosition(int mouseX, int mouseY) {
        if (isDragging && currentlyDragging != null) {
            // Compute new top-left by applying the drag offset
            int newLeft = mouseX - dragOffsetX;
            int newTop = mouseY - dragOffsetY;

            // Clamp within screen using current element size
            Minecraft mc = Minecraft.getMinecraft();
            ScaledResolution res = new ScaledResolution(mc);
            int maxX = Math.max(0, res.getScaledWidth() - currentElementWidth);
            int maxY = Math.max(0, res.getScaledHeight() - currentElementHeight);
            newLeft = Math.max(0, Math.min(newLeft, maxX));
            newTop = Math.max(0, Math.min(newTop, maxY));

            // Apply anchor offsets back to logical position
            currentlyDragging.x = newLeft + anchorOffsetX;
            currentlyDragging.y = newTop + anchorOffsetY;
        }
    }

    public void updatePositions() {
        isDragging = false;
        currentlyDragging = null;
        anchorOffsetX = 0;
        anchorOffsetY = 0;
    }

    public boolean isDragging() {
        return isDragging;
    }


}