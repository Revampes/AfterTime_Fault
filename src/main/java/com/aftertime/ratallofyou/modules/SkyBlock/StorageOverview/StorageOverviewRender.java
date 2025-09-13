package com.aftertime.ratallofyou.modules.SkyBlock.StorageOverview;

import com.aftertime.ratallofyou.UI.config.ConfigData.AllConfig;
import com.aftertime.ratallofyou.UI.config.ConfigData.BaseConfig;
import com.aftertime.ratallofyou.UI.config.ConfigData.ModuleInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class StorageOverviewRender {
    private static final ResourceLocation CHEST_GUI_TEXTURE = new ResourceLocation("textures/gui/container/generic_54.png");

    private final StorageOverviewData storageData;
    private final Minecraft mc;

    // Separate scroll state for EC and Backpacks columns
    private int scrollOffsetEC = 0;
    private int scrollOffsetBP = 0;
    private final int STORAGE_HEIGHT = 120;
    private int maxScrollEC = 0;
    private int maxScrollBP = 0;
    private ItemStack hoveredItem = null;
    private boolean isVisible = false;
    private boolean shouldClose = false;

    // Compact layout constants
    private static final int PANEL_MARGIN = 2;         // distance from screen left/top
    private static final int PANEL_SIDE_PADDING = 6;   // inner horizontal padding
    private static final int COLUMN_GAP = 4;           // gap between EC and Backpack columns
    private static final int V_GAP = 10;               // vertical gap between tiles
    private static final int TILE_WIDTH = 180;         // width per storage tile (fits 9 slots width)
    private static final int MIN_TILE_WIDTH = 172;     // 9 slots * 18 + 10 padding
    private static final int TITLE_HEIGHT = 26;        // header area height (increased for extra padding)
    private static final int TILE_TITLE_Y = 8;         // pixels below tile top

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
        int screenHeight = sr.getScaledHeight();
        // Determine rows per column
        List<StorageOverviewData.Storage> ec = new ArrayList<>();
        List<StorageOverviewData.Storage> bp = new ArrayList<>();
        for (StorageOverviewData.Storage s : storageData.storages) {
            if (s.IsEnderChest) ec.add(s); else bp.add(s);
        }
        List<StorageOverviewData.Storage> ecSorted = sortForColumn(ec);
        List<StorageOverviewData.Storage> bpSorted = sortForColumn(bp);
        int rowsEC = ecSorted.size();
        int rowsBP = bpSorted.size();
        int contentHeightEC = (rowsEC == 0) ? 0 : (TITLE_HEIGHT + rowsEC * (STORAGE_HEIGHT + V_GAP) - V_GAP);
        int contentHeightBP = (rowsBP == 0) ? 0 : (TITLE_HEIGHT + rowsBP * (STORAGE_HEIGHT + V_GAP) - V_GAP);
        int availableHeight = screenHeight - PANEL_MARGIN * 2; // leave a small margin
        maxScrollEC = Math.max(0, contentHeightEC - availableHeight);
        maxScrollBP = Math.max(0, contentHeightBP - availableHeight);
        scrollOffsetEC = Math.max(0, Math.min(scrollOffsetEC, maxScrollEC));
        scrollOffsetBP = Math.max(0, Math.min(scrollOffsetBP, maxScrollBP));
    }

    private boolean isAllowedScreen(GuiScreen gui) {
        if (!(gui instanceof GuiContainer)) return false;
        if (gui instanceof GuiInventory) return true; // player inventory
        try {
            GuiContainer cont = (GuiContainer) gui;
            if (cont.inventorySlots != null && !cont.inventorySlots.inventorySlots.isEmpty()) {
                String name0 = cont.inventorySlots.getSlot(0).inventory.getName();
                return name0 != null && ("Storage".equals(name0) || name0.startsWith("Ender Chest") || name0.contains("Backpack"));
            }
        } catch (Throwable ignored) {}
        return false;
    }

    private int computeTileWidthForPanel(int panelWidth) {
        int availableForTiles = panelWidth - PANEL_SIDE_PADDING * 2 - COLUMN_GAP;
        return Math.max(MIN_TILE_WIDTH, Math.min(TILE_WIDTH, availableForTiles / 2));
    }

    private boolean hasEnoughSpace(PanelDims panel) {
        return panel.width >= (PANEL_SIDE_PADDING * 2 + MIN_TILE_WIDTH * 2 + COLUMN_GAP);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onDrawScreen(GuiScreenEvent.DrawScreenEvent.Post event) {
        if (!isEnabled()) return;
        if (!isAllowedScreen(event.gui)) { hide(); return; }

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
        StorageOverviewData.Storage storage; int slotId;
        HoverTarget(StorageOverviewData.Storage s, int id) { storage = s; slotId = id; }
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

    // Compute single compact left panel rectangle with dynamic width to avoid overlapping center container
    private PanelDims computePanel(ScaledResolution sr) {
        int screenWidth = sr.getScaledWidth();
        int screenHeight = sr.getScaledHeight();
        int approxGuiWidth = 176; // vanilla container width
        int containerLeftApprox = screenWidth / 2 - approxGuiWidth / 2;
        int preferredWidth = PANEL_SIDE_PADDING * 2 + TILE_WIDTH * 2 + COLUMN_GAP;
        int maxAllowedWidth = Math.max(0, containerLeftApprox - PANEL_MARGIN - 4);
        int width = Math.min(preferredWidth, maxAllowedWidth);
        int x = PANEL_MARGIN;
        int y = PANEL_MARGIN;
        int height = screenHeight - PANEL_MARGIN * 2;
        return new PanelDims(x, y, width, height, y + 6); // push title baseline down a bit
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onMouseInput(GuiScreenEvent.MouseInputEvent.Pre event) {
        if (!isEnabled()) return;
        if (!isVisible) return;
        if (!isAllowedScreen(event.gui)) return;

        int mouseX = Mouse.getEventX() * event.gui.width / mc.displayWidth;
        int mouseY = event.gui.height - Mouse.getEventY() * event.gui.height / mc.displayHeight - 1;

        ScaledResolution sr = new ScaledResolution(mc);
        PanelDims panel = computePanel(sr);
        if (!hasEnoughSpace(panel)) return; // not enough room, do nothing

        int tileWidth = computeTileWidthForPanel(panel.width);
        int leftX = panel.x + PANEL_SIDE_PADDING;
        int rightX = leftX + tileWidth + COLUMN_GAP;
        int scrollableAreaY = panel.y + TITLE_HEIGHT;
        int scrollableAreaHeight = panel.height - TITLE_HEIGHT;

        // Scroll wheel inside specific column
        int wheel = Mouse.getEventDWheel();
        if (wheel != 0) {
            if (mouseX >= panel.x && mouseX <= panel.x + panel.width && mouseY >= panel.y && mouseY <= panel.y + panel.height) {
                boolean inScrollableY = mouseY >= scrollableAreaY && mouseY <= scrollableAreaY + scrollableAreaHeight;
                if (inScrollableY) {
                    if (mouseX >= leftX && mouseX <= leftX + tileWidth) {
                        handleScrolling(wheel, true);
                        event.setCanceled(true);
                        return;
                    } else if (mouseX >= rightX && mouseX <= rightX + tileWidth) {
                        handleScrolling(wheel, false);
                        event.setCanceled(true);
                        return;
                    }
                }
            }
        }

        // Mouse clicks inside panel
        boolean pressed = Mouse.getEventButtonState();
        if (!pressed) return;

        // Click on scrollbars to jump
        boolean inScrollableY = mouseY >= scrollableAreaY && mouseY <= scrollableAreaY + scrollableAreaHeight;
        if (inScrollableY) {
            // EC scrollbar region
            int barXLeft = leftX + tileWidth - 5;
            if (maxScrollEC > 0 && mouseX >= barXLeft && mouseX <= barXLeft + 5) {
                scrollToPosition(true, mouseY, scrollableAreaY, scrollableAreaHeight);
                event.setCanceled(true);
                return;
            }
            // BP scrollbar region
            int barXRight = rightX + tileWidth - 5;
            if (maxScrollBP > 0 && mouseX >= barXRight && mouseX <= barXRight + 5) {
                scrollToPosition(false, mouseY, scrollableAreaY, scrollableAreaHeight);
                event.setCanceled(true);
                return;
            }
        }

        // First, check if clicking on a favorite toggle
        StorageOverviewData.Storage favTarget = findFavoriteToggleAt(mouseX, mouseY, panel);
        if (favTarget != null) {
            boolean newFav = !favTarget.IsFavorite;
            storageData.setFavorite(favTarget.IsEnderChest, favTarget.StorageNum, newFav);
            playClick();
            event.setCanceled(true);
            return;
        }

        HoverTarget target = findOverlaySlotAt(mouseX, mouseY, panel);
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
            // Click inside overlay but not a slot? swallow if in panel
            if (mouseX >= panel.x && mouseX <= panel.x + panel.width && mouseY >= panel.y && mouseY <= panel.y + panel.height) {
                event.setCanceled(true);
            }
        }
    }

    private void scrollToPosition(boolean isEC, int clickY, int areaY, int areaHeight) {
        int max = isEC ? maxScrollEC : maxScrollBP;
        if (max <= 0) return;
        int handleHeight = Math.max(10, (areaHeight * areaHeight) / (areaHeight + max));
        float rel = (clickY - areaY - handleHeight / 2.0f) / Math.max(1, (areaHeight - handleHeight));
        int newOffset = (int) (rel * max);
        newOffset = Math.max(0, Math.min(max, newOffset));
        if (isEC) scrollOffsetEC = newOffset; else scrollOffsetBP = newOffset;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onKeyboardInput(GuiScreenEvent.KeyboardInputEvent.Pre event) {
        if (!isEnabled()) return;
        if (!isVisible) return;
        if (!isAllowedScreen(event.gui)) return;

        int mouseX = Mouse.getX() * event.gui.width / mc.displayWidth;
        int mouseY = event.gui.height - Mouse.getY() * event.gui.height / mc.displayHeight - 1;

        ScaledResolution sr = new ScaledResolution(mc);
        PanelDims panel = computePanel(sr);
        if (!hasEnoughSpace(panel)) return; // not enough room, do nothing

        HoverTarget target = findOverlaySlotAt(mouseX, mouseY, panel);
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

    private void handleScrolling(int wheel, boolean isEC) {
        int scrollAmount = 20;
        if (wheel > 0) {
            if (isEC) scrollOffsetEC = Math.max(0, scrollOffsetEC - scrollAmount);
            else scrollOffsetBP = Math.max(0, scrollOffsetBP - scrollAmount);
        } else if (wheel < 0) {
            if (isEC) scrollOffsetEC = Math.min(maxScrollEC, scrollOffsetEC + scrollAmount);
            else scrollOffsetBP = Math.min(maxScrollBP, scrollOffsetBP + scrollAmount);
        }
    }

    private void renderStorageOverlay() {
        ScaledResolution sr = new ScaledResolution(mc);
        int screenWidth = sr.getScaledWidth();
        int screenHeight = sr.getScaledHeight();
        calculateMaxScroll();
        hoveredItem = null;

        PanelDims panel = computePanel(sr);
        if (!hasEnoughSpace(panel)) return; // avoid overlap/clipping on very small screens
        int tileWidth = computeTileWidthForPanel(panel.width);

        // Partition storages by type and sort for display
        List<StorageOverviewData.Storage> ec = new ArrayList<>();
        List<StorageOverviewData.Storage> bp = new ArrayList<>();
        for (StorageOverviewData.Storage s : storageData.storages) {
            if (s.IsEnderChest) ec.add(s); else bp.add(s);
        }
        List<StorageOverviewData.Storage> ecSorted = sortForColumn(ec);
        List<StorageOverviewData.Storage> bpSorted = sortForColumn(bp);

        // Background and headings
        drawRect(panel.x, panel.y, panel.x + panel.width, panel.y + panel.height, 0x30000000);
        String leftTitle = "Ender Chests (" + ecSorted.size() + ")";
        String rightTitle = "Backpacks (" + bpSorted.size() + ")";
        int leftTitleX = panel.x + PANEL_SIDE_PADDING + 2;
        int rightTitleX = panel.x + PANEL_SIDE_PADDING + tileWidth + COLUMN_GAP + 2;
        mc.fontRendererObj.drawStringWithShadow(leftTitle, leftTitleX, panel.titleY, 0xFFFFFF);
        mc.fontRendererObj.drawStringWithShadow(rightTitle, rightTitleX, panel.titleY, 0xFFFFFF);

        int scrollableAreaY = panel.y + TITLE_HEIGHT;
        int scrollableAreaHeight = panel.height - TITLE_HEIGHT;

        // Determine column x positions
        int leftX = panel.x + PANEL_SIDE_PADDING;
        int rightX = leftX + tileWidth + COLUMN_GAP;

        // Scissor to panel content area
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        int scaleFactor = sr.getScaleFactor();
        GL11.glScissor(panel.x * scaleFactor,
                (sr.getScaledHeight() - scrollableAreaY - scrollableAreaHeight) * scaleFactor,
                panel.width * scaleFactor,
                scrollableAreaHeight * scaleFactor);

        int mouseX = Mouse.getX() * screenWidth / mc.displayWidth;
        int mouseY = screenHeight - Mouse.getY() * screenHeight / mc.displayHeight - 1;

        // Draw EC column with its own scroll
        GlStateManager.pushMatrix();
        GlStateManager.translate(0, -scrollOffsetEC, 0);
        drawTwoColumnStorages(ecSorted, Collections.<StorageOverviewData.Storage>emptyList(), panel.x + PANEL_SIDE_PADDING, scrollableAreaY, mouseX, mouseY + scrollOffsetEC, tileWidth);
        GlStateManager.popMatrix();

        // Draw BP column with its own scroll
        GlStateManager.pushMatrix();
        GlStateManager.translate(0, -scrollOffsetBP, 0);
        drawTwoColumnStorages(Collections.<StorageOverviewData.Storage>emptyList(), bpSorted, panel.x + PANEL_SIDE_PADDING, scrollableAreaY, mouseX, mouseY + scrollOffsetBP, tileWidth);
        GlStateManager.popMatrix();

        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        // Scrollbars for each column
        if (maxScrollEC > 0) {
            int barXLeft = leftX + tileWidth - 5;
            drawScrollBar(barXLeft, scrollableAreaY, scrollableAreaHeight, scrollOffsetEC, maxScrollEC);
        }
        if (maxScrollBP > 0) {
            int barXRight = rightX + tileWidth - 5;
            drawScrollBar(barXRight, scrollableAreaY, scrollableAreaHeight, scrollOffsetBP, maxScrollBP);
        }

        // Tooltip on top
        if (hoveredItem != null) {
            drawHoveringText(mouseX, mouseY);
        }
    }

    // Use a named static comparator to avoid anonymous inner class ($1) runtime issues
    private static final Comparator<StorageOverviewData.Storage> STORAGE_COMPARATOR = new StorageComparator();
    private static final class StorageComparator implements Comparator<StorageOverviewData.Storage> {
        @Override
        public int compare(StorageOverviewData.Storage a, StorageOverviewData.Storage b) {
            boolean af = a.IsFavorite, bf = b.IsFavorite;
            if (af != bf) return af ? -1 : 1; // favorites first
            if (af) {
                // Newest favorites first
                if (a.FavoriteTime != b.FavoriteTime) return Long.compare(b.FavoriteTime, a.FavoriteTime);
            }
            // Non-favorites sorted by page number asc
            return Integer.compare(a.StorageNum, b.StorageNum);
        }
    }

    private List<StorageOverviewData.Storage> sortForColumn(List<StorageOverviewData.Storage> list) {
        List<StorageOverviewData.Storage> copy = new ArrayList<>(list);
        Collections.sort(copy, STORAGE_COMPARATOR);
        return copy;
    }

    private void drawTwoColumnStorages(List<StorageOverviewData.Storage> ec, List<StorageOverviewData.Storage> bp, int startX, int startY, int mouseX, int mouseY, int tileWidth) {
        int leftX = startX;
        int rightX = startX + tileWidth + COLUMN_GAP;
        int rows = Math.max(ec.size(), bp.size());
        for (int row = 0; row < rows; row++) {
            if (row < ec.size()) {
                int y = startY + row * (STORAGE_HEIGHT + V_GAP);
                drawStorage(ec.get(row), leftX, y, tileWidth, mouseX, mouseY);
            }
            if (row < bp.size()) {
                int y = startY + row * (STORAGE_HEIGHT + V_GAP);
                drawStorage(bp.get(row), rightX, y, tileWidth, mouseX, mouseY);
            }
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
        int titleX = x + 4; // align left for space
        mc.fontRendererObj.drawStringWithShadow(storageTitle, titleX, y + TILE_TITLE_Y, storage.IsEnderChest ? 0xC080FF : 0xC0C0C0);
        // Favorite toggle box in top-right (aligned with title baseline area)
        int favX1 = x + width - 12, favY1 = y + TILE_TITLE_Y, favX2 = favX1 + 8, favY2 = favY1 + 8;
        int favColor = storage.IsFavorite ? 0xFFE6C200 : 0x66FFFFFF;
        drawRect(favX1, favY1, favX2, favY2, favColor);
        drawRect(favX1, favY1, favX1+1, favY2, 0xFF000000);
        drawRect(favX2-1, favY1, favX2, favY2, 0xFF000000);
        drawRect(favX1, favY1, favX2, favY1+1, 0xFF000000);
        drawRect(favX1, favY2-1, favX2, favY2, 0xFF000000);

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

    private HoverTarget findOverlaySlotAt(int mouseX, int mouseY, PanelDims panel) {
        final int ITEMS_PER_ROW = 9; final int SLOT_SIZE = 18;
        int startY = panel.y + TITLE_HEIGHT;

        // Partition and sort
        List<StorageOverviewData.Storage> ec = new ArrayList<>();
        List<StorageOverviewData.Storage> bp = new ArrayList<>();
        for (StorageOverviewData.Storage s : storageData.storages) {
            if (s.IsEnderChest) ec.add(s); else bp.add(s);
        }
        List<StorageOverviewData.Storage> ecSorted = sortForColumn(ec);
        List<StorageOverviewData.Storage> bpSorted = sortForColumn(bp);

        // Check if inside panel at all
        if (!(mouseX >= panel.x && mouseX <= panel.x + panel.width && mouseY >= panel.y && mouseY <= panel.y + panel.height)) return null;

        // Column geometry
        int tileWidth = computeTileWidthForPanel(panel.width);
        int leftX = panel.x + PANEL_SIDE_PADDING;
        int rightX = leftX + tileWidth + COLUMN_GAP;

        int adjMouseYEC = mouseY + scrollOffsetEC;
        int adjMouseYBP = mouseY + scrollOffsetBP;

        int rows = Math.max(ecSorted.size(), bpSorted.size());
        for (int row = 0; row < rows; row++) {
            int yBase = startY + row * (STORAGE_HEIGHT + V_GAP);
            // Left column EC
            if (row < ecSorted.size()) {
                StorageOverviewData.Storage s = ecSorted.get(row);
                int containerX = leftX + (tileWidth - (Math.min(ITEMS_PER_ROW, s.contents.length) * SLOT_SIZE + 10)) / 2;
                int containerY = yBase + 20;
                int startXSlots = containerX + 5; int startYSlots = containerY + 5;
                for (int idx = 9; idx < s.contents.length; idx++) {
                    int slotX = startXSlots + ((idx - 9) % ITEMS_PER_ROW) * SLOT_SIZE;
                    int slotY = startYSlots + ((idx - 9) / ITEMS_PER_ROW) * SLOT_SIZE;
                    if (mouseX >= slotX && mouseX <= slotX + 16 && adjMouseYEC >= slotY && adjMouseYEC <= slotY + 16) {
                        return new HoverTarget(s, idx);
                    }
                }
            }
            // Right column BP
            if (row < bpSorted.size()) {
                StorageOverviewData.Storage s = bpSorted.get(row);
                int containerX = rightX + (tileWidth - (Math.min(ITEMS_PER_ROW, s.contents.length) * SLOT_SIZE + 10)) / 2;
                int containerY = yBase + 20;
                int startXSlots = containerX + 5; int startYSlots = containerY + 5;
                for (int idx = 9; idx < s.contents.length; idx++) {
                    int slotX = startXSlots + ((idx - 9) % ITEMS_PER_ROW) * SLOT_SIZE;
                    int slotY = startYSlots + ((idx - 9) / ITEMS_PER_ROW) * SLOT_SIZE;
                    if (mouseX >= slotX && mouseX <= slotX + 16 && adjMouseYBP >= slotY && adjMouseYBP <= slotY + 16) {
                        return new HoverTarget(s, idx);
                    }
                }
            }
        }
        return null;
    }

    // Detect if favorite toggle box is clicked; returns the storage item or null
    private StorageOverviewData.Storage findFavoriteToggleAt(int mouseX, int mouseY, PanelDims panel) {
        int startY = panel.y + TITLE_HEIGHT;
        if (!(mouseX >= panel.x && mouseX <= panel.x + panel.width && mouseY >= panel.y && mouseY <= panel.y + panel.height)) return null;
        int adjMouseYEC = mouseY + scrollOffsetEC;
        int adjMouseYBP = mouseY + scrollOffsetBP;
        List<StorageOverviewData.Storage> ec = new ArrayList<>();
        List<StorageOverviewData.Storage> bp = new ArrayList<>();
        for (StorageOverviewData.Storage s : storageData.storages) {
            if (s.IsEnderChest) ec.add(s); else bp.add(s);
        }
        List<StorageOverviewData.Storage> ecSorted = sortForColumn(ec);
        List<StorageOverviewData.Storage> bpSorted = sortForColumn(bp);
        int tileWidth = computeTileWidthForPanel(panel.width);
        int leftX = panel.x + PANEL_SIDE_PADDING;
        int rightX = leftX + tileWidth + COLUMN_GAP;
        int rows = Math.max(ecSorted.size(), bpSorted.size());
        for (int row = 0; row < rows; row++) {
            int yBase = startY + row * (STORAGE_HEIGHT + V_GAP);
            // EC column toggle
            if (row < ecSorted.size()) {
                StorageOverviewData.Storage s = ecSorted.get(row);
                int favX1 = leftX + tileWidth - 12, favY1 = yBase + TILE_TITLE_Y, favX2 = favX1 + 8, favY2 = favY1 + 8;
                if (mouseX >= favX1 && mouseX <= favX2 && adjMouseYEC >= favY1 && adjMouseYEC <= favY2) return s;
            }
            // BP column toggle
            if (row < bpSorted.size()) {
                StorageOverviewData.Storage s = bpSorted.get(row);
                int favX1 = rightX + tileWidth - 12, favY1 = yBase + TILE_TITLE_Y, favX2 = favX1 + 8, favY2 = favY1 + 8;
                if (mouseX >= favX1 && mouseX <= favX2 && adjMouseYBP >= favY1 && adjMouseYBP <= favY2) return s;
            }
        }
        return null;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onGuiOpen(GuiOpenEvent event) {
        if (!isEnabled()) return;
        if (isAllowedScreen(event.gui)) show();
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
