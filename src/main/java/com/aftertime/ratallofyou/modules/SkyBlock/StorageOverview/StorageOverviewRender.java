package com.aftertime.ratallofyou.modules.SkyBlock.StorageOverview;

import com.aftertime.ratallofyou.UI.config.ConfigData.AllConfig;
import com.aftertime.ratallofyou.UI.config.ConfigData.BaseConfig;
import com.aftertime.ratallofyou.UI.config.ConfigData.ModuleInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class StorageOverviewRender {
    private static final ResourceLocation CHEST_GUI_TEXTURE = new ResourceLocation("textures/gui/container/generic_54.png");

    private final StorageOverviewData storageData;
    private final Minecraft mc;

    // Independent scroll state for each side
    private int scrollOffsetLeft = 0;
    private int scrollOffsetRight = 0;
    private final int STORAGE_HEIGHT = 120;
    private int maxScrollLeft = 0;
    private int maxScrollRight = 0;
    private ItemStack hoveredItem = null;
    private boolean isVisible = false;
    private boolean shouldClose = false;

    public StorageOverviewRender(StorageOverviewData storageData) {
        this.storageData = storageData;
        this.mc = Minecraft.getMinecraft();
    }

    private boolean isEnabled() {
        ModuleInfo cfg = (ModuleInfo) AllConfig.INSTANCE.MODULES.get("skyblock_storageoverview");
        return cfg != null && Boolean.TRUE.equals(cfg.Data);
    }

    private boolean showInInventory() {
        BaseConfig<?> cfg = AllConfig.INSTANCE.STORAGEOVERVIEW_CONFIGS.get("storageoverview_show_in_inventory");
        Object v = cfg != null ? cfg.Data : null;
        return !(v instanceof Boolean) || (Boolean) v;
    }

    public void show() { isVisible = true; calculateMaxScroll(); }
    public void hide() { isVisible = false; shouldClose = false; }

    private static class PanelDims {
        int x, y, width, height; // panel rect
        int titleY; // title baseline y
        PanelDims(int x, int y, int width, int height, int titleY) { this.x=x; this.y=y; this.width=width; this.height=height; this.titleY=titleY; }
    }

    private void calculateMaxScroll() {
        ScaledResolution sr = new ScaledResolution(mc);
        int screenWidth = sr.getScaledWidth();
        int screenHeight = sr.getScaledHeight();
        int margin = 10;
        // Cap each panel width similarly to legacy left panel
        int panelWidth = Math.min((screenWidth - margin * 3) / 2, 375);
        int availableHeight = screenHeight - 40; // title + margin

        // Partition storages
        List<StorageOverviewData.Storage> ec = new ArrayList<>();
        List<StorageOverviewData.Storage> bp = new ArrayList<>();
        for (StorageOverviewData.Storage s : storageData.storages) {
            if (s.IsEnderChest) ec.add(s); else bp.add(s);
        }

        // Rows calculation per panel
        int perRow = Math.max(1, Math.min(2, panelWidth / 150));
        int rowsLeft = (ec.size() + perRow - 1) / perRow;
        int rowsRight = (bp.size() + perRow - 1) / perRow;
        int totalHLeft = rowsLeft * (STORAGE_HEIGHT + 15);
        int totalHRight = rowsRight * (STORAGE_HEIGHT + 15);
        maxScrollLeft = Math.max(0, totalHLeft - availableHeight);
        maxScrollRight = Math.max(0, totalHRight - availableHeight);
        // Clamp offsets if list changed
        scrollOffsetLeft = Math.max(0, Math.min(scrollOffsetLeft, maxScrollLeft));
        scrollOffsetRight = Math.max(0, Math.min(scrollOffsetRight, maxScrollRight));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onDrawScreen(GuiScreenEvent.DrawScreenEvent.Post event) {
        if (!isEnabled()) return;

        // Auto-show when allowed and a container is open
        if (!isVisible && event.gui instanceof GuiContainer) {
            boolean allowInInv = showInInventory();
            boolean isStorageLike = false;
            GuiContainer cont = (GuiContainer) event.gui;
            try {
                if (cont.inventorySlots != null && !cont.inventorySlots.inventorySlots.isEmpty()) {
                    String name0 = cont.inventorySlots.getSlot(0).inventory.getName();
                    isStorageLike = name0 != null && ("Storage".equals(name0) || name0.startsWith("Ender Chest") || name0.contains("Backpack"));
                }
            } catch (Throwable ignored) {}
            if (allowInInv || isStorageLike) show();
        }

        if (!isVisible) return;
        if (!(mc.currentScreen instanceof GuiContainer)) { hide(); return; }
        if (shouldClose) { hide(); return; }

        GlStateManager.pushMatrix();
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        renderStorageOverlay();
        GlStateManager.disableBlend();
        GlStateManager.enableDepth();
        GlStateManager.popMatrix();
    }

    private static class HoverTarget {
        StorageOverviewData.Storage storage; int slotId; boolean leftSide;
        HoverTarget(StorageOverviewData.Storage s, int id, boolean left) { storage = s; slotId = id; leftSide = left; }
    }

    private boolean isActiveContainerSlot(HoverTarget target) {
        if (!(mc.currentScreen instanceof GuiContainer)) return false;
        if (target == null || target.storage == null) return false;
        GuiContainer container = (GuiContainer) mc.currentScreen;
        if (container.inventorySlots == null || container.inventorySlots.inventorySlots.isEmpty()) return false;
        String invName = container.inventorySlots.getSlot(0).inventory.getName();
        boolean openIsEnder; int openNum;
        try {
            if (invName.startsWith("Ender Chest")) {
                openIsEnder = true;
                openNum = Integer.parseInt(invName.substring(invName.indexOf("(") + 1, invName.indexOf("/")));
            } else if (invName.contains("Backpack")) {
                openIsEnder = false;
                openNum = Integer.parseInt(invName.substring(invName.indexOf("#") + 1, invName.length() - 1));
            } else { return false; }
        } catch (Exception e) { return false; }
        if (openIsEnder != target.storage.IsEnderChest || openNum != target.storage.StorageNum) return false;
        int totalSlots = container.inventorySlots.inventorySlots.size();
        int containerSlots = Math.max(0, totalSlots - 36);
        return target.slotId >= 0 && target.slotId < containerSlots;
    }

    // Compute panel rectangles used for draw and input
    private PanelDims[] computePanels(ScaledResolution sr) {
        int screenWidth = sr.getScaledWidth();
        int screenHeight = sr.getScaledHeight();
        int margin = 10;
        int titleHeight = 20;
        int y = 0;
        int panelWidth = Math.min((screenWidth - margin * 3) / 2, 375);
        int leftX = margin;
        int rightX = screenWidth - margin - panelWidth;
        int height = screenHeight;
        return new PanelDims[] {
            new PanelDims(leftX, y, panelWidth, height, 5),
            new PanelDims(rightX, y, panelWidth, height, 5)
        };
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onMouseInput(GuiScreenEvent.MouseInputEvent.Pre event) {
        if (!isEnabled()) return;
        if (!isVisible) return;
        if (!(event.gui instanceof GuiContainer)) return;

        int mouseX = Mouse.getEventX() * event.gui.width / mc.displayWidth;
        int mouseY = event.gui.height - Mouse.getEventY() * event.gui.height / mc.displayHeight - 1;

        ScaledResolution sr = new ScaledResolution(mc);
        PanelDims[] panels = computePanels(sr);
        PanelDims left = panels[0];
        PanelDims right = panels[1];

        // Scroll wheel per panel
        int wheel = Mouse.getEventDWheel();
        if (wheel != 0) {
            if (mouseX >= left.x && mouseX <= left.x + left.width && mouseY >= left.y && mouseY <= left.y + left.height) {
                handleScrolling(true, wheel);
                event.setCanceled(true);
                return;
            }
            if (mouseX >= right.x && mouseX <= right.x + right.width && mouseY >= right.y && mouseY <= right.y + right.height) {
                handleScrolling(false, wheel);
                event.setCanceled(true);
                return;
            }
        }

        // Mouse clicks per panel
        int button = Mouse.getEventButton();
        boolean pressed = Mouse.getEventButtonState();
        if (!pressed) return;

        HoverTarget target = findOverlaySlotAt(mouseX, mouseY, panels);
        if (target != null) {
            if(!isActiveContainerSlot(target)) {
                StorageOverviewData.Storage hoveredStorage = target.storage;
                String command = hoveredStorage.IsEnderChest ? "/ec " + hoveredStorage.StorageNum : "/backpack " + hoveredStorage.StorageNum;
                playClick();
                mc.thePlayer.sendChatMessage(command);
                event.setCanceled(true);
                return;
            } else {
                int windowId = ((GuiContainer) event.gui).inventorySlots.windowId;
                mc.playerController.windowClick(windowId, target.slotId, 1, 1, mc.thePlayer);
                playClick();
                event.setCanceled(true);
                return;
            }
        } else {
            // Click inside overlay but not a slot? swallow if in either panel
            if ((mouseX >= left.x && mouseX <= left.x + left.width && mouseY >= left.y && mouseY <= left.y + left.height) ||
                (mouseX >= right.x && mouseX <= right.x + right.width && mouseY >= right.y && mouseY <= right.y + right.height)) {
                event.setCanceled(true);
                return;
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onKeyboardInput(GuiScreenEvent.KeyboardInputEvent.Pre event) {
        if (!isEnabled()) return;
        if (!isVisible) return;
        if (!(event.gui instanceof GuiContainer)) return;

        int mouseX = Mouse.getX() * event.gui.width / mc.displayWidth;
        int mouseY = event.gui.height - Mouse.getY() * event.gui.height / mc.displayHeight - 1;

        ScaledResolution sr = new ScaledResolution(mc);
        PanelDims[] panels = computePanels(sr);

        HoverTarget target = findOverlaySlotAt(mouseX, mouseY, panels);
        if (target == null || !isActiveContainerSlot(target)) return;

        int key = Keyboard.getEventKey();
        if (key == Keyboard.KEY_NONE) return;

        int windowId = ((GuiContainer) event.gui).inventorySlots.windowId;
        if (key >= Keyboard.KEY_1 && key <= Keyboard.KEY_9) {
            int hotbarIndex = key - Keyboard.KEY_1;
            mc.playerController.windowClick(windowId, target.slotId, hotbarIndex, 2, mc.thePlayer);
            playClick();
            event.setCanceled(true);
            return;
        }
        if (key == Keyboard.KEY_Q) {
            boolean ctrl = GuiScreen.isCtrlKeyDown();
            int mouseParam = ctrl ? 1 : 0;
            mc.playerController.windowClick(windowId, target.slotId, mouseParam, 4, mc.thePlayer);
            playClick();
            event.setCanceled(true);
        }
    }

    private void playClick() {
        mc.getSoundHandler().playSound(
            net.minecraft.client.audio.PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F)
        );
    }

    private void handleScrolling(boolean leftPanel, int wheel) {
        int scrollAmount = 20;
        if (leftPanel) {
            if (wheel > 0) scrollOffsetLeft = Math.max(0, scrollOffsetLeft - scrollAmount);
            else if (wheel < 0) scrollOffsetLeft = Math.min(maxScrollLeft, scrollOffsetLeft + scrollAmount);
        } else {
            if (wheel > 0) scrollOffsetRight = Math.max(0, scrollOffsetRight - scrollAmount);
            else if (wheel < 0) scrollOffsetRight = Math.min(maxScrollRight, scrollOffsetRight + scrollAmount);
        }
    }

    private void renderStorageOverlay() {
        ScaledResolution sr = new ScaledResolution(mc);
        int screenWidth = sr.getScaledWidth();
        int screenHeight = sr.getScaledHeight();
        calculateMaxScroll();
        hoveredItem = null;

        PanelDims[] panels = computePanels(sr);
        PanelDims left = panels[0];
        PanelDims right = panels[1];

        // Partition storages by type
        List<StorageOverviewData.Storage> ec = new ArrayList<>();
        List<StorageOverviewData.Storage> bp = new ArrayList<>();
        for (StorageOverviewData.Storage s : storageData.storages) {
            if (s.IsEnderChest) ec.add(s); else bp.add(s);
        }

        // Titles
        String leftTitle = "Ender Chests (" + ec.size() + ")";
        String rightTitle = "Storage (" + bp.size() + ")";

        // Backgrounds
        drawRect(left.x, left.y, left.x + left.width, left.y + left.height, 0x30000000);
        drawRect(right.x, right.y, right.x + right.width, right.y + right.height, 0x30000000);

        // Draw titles
        int lTitleW = mc.fontRendererObj.getStringWidth(leftTitle);
        mc.fontRendererObj.drawStringWithShadow(leftTitle, left.x + (left.width - lTitleW) / 2.0f, left.titleY, 0xFFFFFF);
        int rTitleW = mc.fontRendererObj.getStringWidth(rightTitle);
        mc.fontRendererObj.drawStringWithShadow(rightTitle, right.x + (right.width - rTitleW) / 2.0f, right.titleY, 0xFFFFFF);

        int titleHeight = 20;
        int scrollableAreaHeight = screenHeight - titleHeight - 20;
        int scrollableAreaY = titleHeight;

        // Left panel scissor and draw
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        int scaleFactor = sr.getScaleFactor();
        // Left scissor
        GL11.glScissor(left.x * scaleFactor,
                (sr.getScaledHeight() - scrollableAreaY - scrollableAreaHeight) * scaleFactor,
                left.width * scaleFactor,
                scrollableAreaHeight * scaleFactor);
        GlStateManager.pushMatrix();
        GlStateManager.translate(0, -scrollOffsetLeft, 0);
        int mouseX = Mouse.getX() * screenWidth / mc.displayWidth;
        int mouseY = screenHeight - Mouse.getY() * screenHeight / mc.displayHeight - 1;
        drawStorages(ec, left.x, left.width, scrollableAreaY, mouseX, mouseY + scrollOffsetLeft);
        GlStateManager.popMatrix();
        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        // Right panel scissor and draw
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(right.x * scaleFactor,
                (sr.getScaledHeight() - scrollableAreaY - scrollableAreaHeight) * scaleFactor,
                right.width * scaleFactor,
                scrollableAreaHeight * scaleFactor);
        GlStateManager.pushMatrix();
        GlStateManager.translate(0, -scrollOffsetRight, 0);
        drawStorages(bp, right.x, right.width, scrollableAreaY, mouseX, mouseY + scrollOffsetRight);
        GlStateManager.popMatrix();
        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        // Scrollbars
        if (maxScrollLeft > 0) {
            int barX = left.x + left.width + 5; // in the center gap
            drawScrollBar(barX, scrollableAreaY, scrollableAreaHeight, scrollOffsetLeft, maxScrollLeft);
        }
        if (maxScrollRight > 0) {
            int barX = right.x - 8; // before the right panel
            drawScrollBar(barX, scrollableAreaY, scrollableAreaHeight, scrollOffsetRight, maxScrollRight);
        }

        // Tooltip on top
        if (hoveredItem != null) {
            drawHoveringText(mouseX, mouseY);
        }
    }

    private void drawStorages(List<StorageOverviewData.Storage> list, int panelX, int panelWidth, int startY, int mouseX, int mouseY) {
        int perRow = Math.max(1, Math.min(2, panelWidth / 150));
        int storageWidth = (panelWidth - 30) / perRow;
        int currentRow = 0; int currentCol = 0;
        for (int i = 0; i < list.size(); i++) {
            StorageOverviewData.Storage storage = list.get(i);
            int storageX = panelX + 15 + currentCol * (storageWidth + 10);
            int storageY = startY + currentRow * (STORAGE_HEIGHT + 15);
            drawStorage(storage, storageX, storageY, storageWidth, mouseX, mouseY);
            currentCol++; if (currentCol >= perRow) { currentCol = 0; currentRow++; }
        }
    }

    private void drawStorage(StorageOverviewData.Storage storage, int x, int y, int width, int mouseX, int mouseY) {
        mc.getTextureManager().bindTexture(CHEST_GUI_TEXTURE);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        final int ITEMS_PER_ROW = 9; final int SLOT_SIZE = 18;
        int itemsInStorage = storage.contents.length;
        int rows = Math.max(1, (itemsInStorage + ITEMS_PER_ROW - 1) / ITEMS_PER_ROW);
        int itemAreaWidth = Math.min(ITEMS_PER_ROW, itemsInStorage) * SLOT_SIZE + 10;
        int itemAreaHeight = rows * SLOT_SIZE + 10;
        int containerX = x + (width - itemAreaWidth) / 2;
        int containerY = y + 20;
        String storageTitle = (storage.IsEnderChest ? "Ender Chest " : "Backpack ") + storage.StorageNum;
        int titleWidth = mc.fontRendererObj.getStringWidth(storageTitle);
        int titleX = x + (width - titleWidth) / 2;
        mc.fontRendererObj.drawStringWithShadow(storageTitle, titleX, y + 5, storage.IsEnderChest ? 0x800080 : 0x404040);
        drawRect(containerX, containerY, containerX + itemAreaWidth, containerY + itemAreaHeight, 0x88000000);
        drawRect(containerX + 1, containerY + 1, containerX + itemAreaWidth - 1, containerY + itemAreaHeight - 1, 0x44FFFFFF);

        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.enableRescaleNormal();
        int startX = containerX + 5; int startY = containerY + 5;
        for (int i = 9; i < storage.contents.length; i++) {
            int slotX = startX + ((i-9) % ITEMS_PER_ROW) * SLOT_SIZE;
            int slotY = startY + ((i-9) / ITEMS_PER_ROW) * SLOT_SIZE;
            drawRect(slotX, slotY, slotX + 16, slotY + 16, 0x88888888);
            Slot slot = storage.contents[i];
            if (slot != null && slot.getStack() != null) {
                ItemStack stack = slot.getStack();
                if (mouseX >= slotX && mouseX <= slotX + 16 && mouseY >= slotY && mouseY <= slotY + 16) {
                    hoveredItem = stack; drawRect(slotX, slotY, slotX + 16, slotY + 16, 0x80FFFFFF);
                }
                mc.getRenderItem().renderItemAndEffectIntoGUI(stack, slotX, slotY);
                mc.getRenderItem().renderItemOverlayIntoGUI(mc.fontRendererObj, stack, slotX, slotY, null);
            }
        }
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
    }

    private void drawScrollBar(int x, int y, int height, int offset, int max) {
        drawRect(x, y, x + 5, y + height, 0x88000000);
        if (max > 0) {
            int handleHeight = Math.max(10, (height * height) / (height + max));
            int handleY = y + (int)((height - handleHeight) * ((float)offset / max));
            drawRect(x + 1, handleY, x + 4, handleY + handleHeight, 0xFFAAAAAA);
        }
    }

    private void drawHoveringText(int mouseX, int mouseY) {
        if (hoveredItem == null) return;
        List<String> tooltip = hoveredItem.getTooltip(mc.thePlayer, mc.gameSettings.advancedItemTooltips);
        if (tooltip.isEmpty()) return;
        int tooltipWidth = 0; for (String line : tooltip) tooltipWidth = Math.max(tooltipWidth, mc.fontRendererObj.getStringWidth(line));
        int tooltipHeight = tooltip.size() * 10;
        ScaledResolution sr = new ScaledResolution(mc);
        int tooltipX = mouseX + 12; int tooltipY = mouseY - 12;
        if (tooltipX + tooltipWidth + 6 > sr.getScaledWidth()) tooltipX = mouseX - tooltipWidth - 12;
        if (tooltipY + tooltipHeight + 6 > sr.getScaledHeight()) tooltipY = mouseY - tooltipHeight - 12;
        tooltipX = Math.max(4, Math.min(tooltipX, sr.getScaledWidth() - tooltipWidth - 4));
        tooltipY = Math.max(4, Math.min(tooltipY, sr.getScaledHeight() - tooltipHeight - 4));
        GlStateManager.disableRescaleNormal();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableDepth();
        drawRect(tooltipX - 3, tooltipY - 3, tooltipX + tooltipWidth + 3, tooltipY + tooltipHeight + 3, 0xF0100010);
        drawRect(tooltipX - 2, tooltipY - 2, tooltipX + tooltipWidth + 2, tooltipY + tooltipHeight + 2, 0x11111111);
        for (int i = 0; i < tooltip.size(); i++) {
            mc.fontRendererObj.drawStringWithShadow(tooltip.get(i), tooltipX, tooltipY + i * 10, 0xFFFFFF);
        }
        GlStateManager.enableDepth();
        RenderHelper.enableStandardItemLighting();
        GlStateManager.enableRescaleNormal();
    }

    private void drawRect(int left, int top, int right, int bottom, int color) {
        GuiScreen.drawRect(left, top, right, bottom, color);
    }

    private HoverTarget findOverlaySlotAt(int mouseX, int mouseY, PanelDims[] panels) {
        ScaledResolution sr = new ScaledResolution(mc);
        int screenWidth = sr.getScaledWidth();
        int screenHeight = sr.getScaledHeight();
        int titleHeight = 20;
        int startY = titleHeight;
        final int ITEMS_PER_ROW = 9; final int SLOT_SIZE = 18;

        // Partition lists for consistent indexing
        List<StorageOverviewData.Storage> ec = new ArrayList<>();
        List<StorageOverviewData.Storage> bp = new ArrayList<>();
        for (StorageOverviewData.Storage s : storageData.storages) {
            if (s.IsEnderChest) ec.add(s); else bp.add(s);
        }
        // Left side
        PanelDims left = panels[0];
        if (mouseX >= left.x && mouseX <= left.x + left.width && mouseY >= left.y && mouseY <= left.y + left.height) {
            int perRow = Math.max(1, Math.min(2, left.width / 150));
            int storageWidth = (left.width - 30) / perRow;
            int adjMouseY = mouseY + scrollOffsetLeft;
            int currentRow = 0, currentCol = 0;
            for (int i = 0; i < ec.size(); i++) {
                StorageOverviewData.Storage s = ec.get(i);
                int storageX = left.x + 15 + currentCol * (storageWidth + 10);
                int storageY = startY + currentRow * (STORAGE_HEIGHT + 15);
                int itemsInStorage = s.contents.length;
                int rows = Math.max(1, (itemsInStorage + ITEMS_PER_ROW - 1) / ITEMS_PER_ROW);
                int itemAreaWidth = Math.min(ITEMS_PER_ROW, itemsInStorage) * SLOT_SIZE + 10;
                int itemAreaHeight = rows * SLOT_SIZE + 10;
                int containerX = storageX + (storageWidth - itemAreaWidth) / 2;
                int containerY = storageY + 20;
                int startX = containerX + 5; int startYSlots = containerY + 5;
                for (int idx = 9; idx < s.contents.length; idx++) {
                    int slotX = startX + ((idx - 9) % ITEMS_PER_ROW) * SLOT_SIZE;
                    int slotY = startYSlots + ((idx - 9) / ITEMS_PER_ROW) * SLOT_SIZE;
                    if (mouseX >= slotX && mouseX <= slotX + 16 && adjMouseY >= slotY && adjMouseY <= slotY + 16) {
                        return new HoverTarget(s, idx, true);
                    }
                }
                currentCol++; if (currentCol >= perRow) { currentCol = 0; currentRow++; }
            }
        }
        // Right side
        PanelDims right = panels[1];
        if (mouseX >= right.x && mouseX <= right.x + right.width && mouseY >= right.y && mouseY <= right.y + right.height) {
            int perRow = Math.max(1, Math.min(2, right.width / 150));
            int storageWidth = (right.width - 30) / perRow;
            int adjMouseY = mouseY + scrollOffsetRight;
            int currentRow = 0, currentCol = 0;
            for (int i = 0; i < bp.size(); i++) {
                StorageOverviewData.Storage s = bp.get(i);
                int storageX = right.x + 15 + currentCol * (storageWidth + 10);
                int storageY = startY + currentRow * (STORAGE_HEIGHT + 15);
                int itemsInStorage = s.contents.length;
                int rows = Math.max(1, (itemsInStorage + ITEMS_PER_ROW - 1) / ITEMS_PER_ROW);
                int itemAreaWidth = Math.min(ITEMS_PER_ROW, itemsInStorage) * SLOT_SIZE + 10;
                int itemAreaHeight = rows * SLOT_SIZE + 10;
                int containerX = storageX + (storageWidth - itemAreaWidth) / 2;
                int containerY = storageY + 20;
                int startX = containerX + 5; int startYSlots = containerY + 5;
                for (int idx = 9; idx < s.contents.length; idx++) {
                    int slotX = startX + ((idx - 9) % ITEMS_PER_ROW) * SLOT_SIZE;
                    int slotY = startYSlots + ((idx - 9) / ITEMS_PER_ROW) * SLOT_SIZE;
                    if (mouseX >= slotX && mouseX <= slotX + 16 && adjMouseY >= slotY && adjMouseY <= slotY + 16) {
                        return new HoverTarget(s, idx, false);
                    }
                }
                currentCol++; if (currentCol >= perRow) { currentCol = 0; currentRow++; }
            }
        }
        return null;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onGuiOpen(GuiOpenEvent event) {
        if (!isEnabled()) return;
        if (event.gui instanceof GuiContainer) {
            GuiContainer container = (GuiContainer) event.gui;
            boolean allowInInv = showInInventory();
            boolean isStorageLike = false;
            try {
                if (container.inventorySlots != null && !container.inventorySlots.inventorySlots.isEmpty()) {
                    String name0 = container.inventorySlots.getSlot(0).inventory.getName();
                    isStorageLike = name0 != null && ("Storage".equals(name0) || name0.startsWith("Ender Chest") || name0.contains("Backpack"));
                }
            } catch (Throwable ignored) {}
            if (allowInInv || isStorageLike) show();
        }
    }

    // Static instance management so we can re-register cleanly
    private static StorageOverviewRender instance;
    public static void registerEvents(StorageOverviewData data) {
        if (instance != null) {
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.unregister(instance);
        }
        instance = new StorageOverviewRender(data);
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(instance);
    }
}

