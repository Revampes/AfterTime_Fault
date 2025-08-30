package com.aftertime.ratallofyou.modules.SkyBlock.StorageOverview;

import com.aftertime.ratallofyou.UI.config.ConfigData.AllConfig;
import com.aftertime.ratallofyou.UI.config.ConfigData.ModuleInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class StorageOverviewData {
    private static final String STORAGE_FILE_NAME = "storage_data.dat";
    public final List<Storage> storages = new ArrayList<Storage>();

    private boolean isEnabled() {
        ModuleInfo cfg = (ModuleInfo) AllConfig.INSTANCE.MODULES.get("skyblock_storageoverview");
        return cfg != null && Boolean.TRUE.equals(cfg.Data);
    }

    public static class Storage{
        public boolean IsEnderChest;
        public int StorageNum;
        public Slot[] contents;
        // Added: favorite state and timestamp for sorting (newest first)
        public boolean IsFavorite = false;
        public long FavoriteTime = 0L;
        public Storage(boolean isEnderChest, int storageNum, Slot[] contents) {
            IsEnderChest = isEnderChest;
            StorageNum = storageNum;
            this.contents = contents;
        }
    }

    private int findStorage(int storageNum, boolean isEnderChest) {
        for (int i = 0; i < storages.size(); i++) {
            Storage storage = storages.get(i);
            if (storage.StorageNum == storageNum && storage.IsEnderChest == isEnderChest) {
                return i;
            }
        }
        return -1; // Not found
    }

    public void addStorage(GuiContainer container, boolean isEnderChest, int storageNum) {
        int totalSlots = container.inventorySlots.inventorySlots.size();
        int containerSlots = totalSlots - 36; // Player inventory is always 36 slots

        Slot[] contents = new Slot[containerSlots];
        for (int i = 0; i < containerSlots; i++) {
            if (i < totalSlots) {
                contents[i] = container.inventorySlots.getSlot(i);
            }
        }

        storages.add(new Storage(isEnderChest, storageNum, contents));
        saveStorages();
    }

    public void updateStorage(GuiContainer container, int storageIndex) {
        int totalSlots = container.inventorySlots.inventorySlots.size();
        int containerSlots = totalSlots - 36;

        Slot[] storageContents = new Slot[containerSlots];
        for (int i = 0; i < containerSlots; i++) {
            if (i < totalSlots) {
                storageContents[i] = container.inventorySlots.getSlot(i);
            }
        }

        storages.get(storageIndex).contents = storageContents;
        saveStorages();
    }

    // Toggle or set favorite for a storage and persist
    public void setFavorite(boolean isEnderChest, int storageNum, boolean favorite) {
        int idx = findStorage(storageNum, isEnderChest);
        if (idx >= 0) {
            Storage s = storages.get(idx);
            if (favorite && !s.IsFavorite) {
                s.IsFavorite = true;
                s.FavoriteTime = System.currentTimeMillis();
            } else if (!favorite && s.IsFavorite) {
                s.IsFavorite = false;
                s.FavoriteTime = 0L;
            }
            saveStorages();
        }
    }

    private boolean currentIsEnderChest;
    private int currentStorageNum;

    private void updateStorageData(GuiContainer container) {
        String invName = container.inventorySlots.getSlot(0).inventory.getName();
        if (invName == null) return;
        try {
            if (invName.startsWith("Ender Chest")) {
                currentIsEnderChest = true;
                String substring = invName.substring(invName.indexOf("(") + 1, invName.indexOf("/"));
                currentStorageNum = Integer.parseInt(substring);
                int idx = findStorage(currentStorageNum, true);
                if (idx == -1) addStorage(container, true, currentStorageNum); else updateStorage(container, idx);
            } else if (invName.contains("Backpack")) {
                currentIsEnderChest = false;
                String substring = invName.substring(invName.indexOf("#") + 1, invName.length() - 1);
                currentStorageNum = Integer.parseInt(substring);
                int idx = findStorage(currentStorageNum, false);
                if (idx == -1) addStorage(container, false, currentStorageNum); else updateStorage(container, idx);
            }
        } catch (Exception ignored) { }
    }

    @SubscribeEvent
    public void onStorageGUIOpen(GuiOpenEvent event) {
        if (!isEnabled()) return;
        if (event.gui instanceof GuiContainer) {
            GuiContainer container = (GuiContainer) event.gui;
            if (container.inventorySlots != null && !container.inventorySlots.inventorySlots.isEmpty()) {
                String name = container.inventorySlots.getSlot(0).inventory.getName();
                if (name != null && (name.startsWith("Ender Chest") || name.contains("Backpack"))) {
                    updateStorageData(container);
                }
            }
        }
    }

    public void saveStorages() {
        try {
            File storageFile = new File(Minecraft.getMinecraft().mcDataDir, STORAGE_FILE_NAME);
            NBTTagCompound rootTag = new NBTTagCompound();
            NBTTagList storagesList = new NBTTagList();

            for (Storage storage : storages) {
                NBTTagCompound storageTag = new NBTTagCompound();
                storageTag.setBoolean("IsEnderChest", storage.IsEnderChest);
                storageTag.setInteger("StorageNum", storage.StorageNum);
                // Persist favorite state
                storageTag.setBoolean("IsFavorite", storage.IsFavorite);
                storageTag.setLong("FavoriteTime", storage.FavoriteTime);

                NBTTagList itemsList = new NBTTagList();
                for (int i = 9; i < storage.contents.length; i++) {
                    NBTTagCompound itemTag = new NBTTagCompound();
                    Slot slot = storage.contents[i];
                    if (slot != null && slot.getStack() != null) {
                        slot.getStack().writeToNBT(itemTag);
                    }
                    itemsList.appendTag(itemTag);
                }

                storageTag.setTag("Items", itemsList);
                storagesList.appendTag(storageTag);
            }

            rootTag.setTag("StorageList", storagesList);

            FileOutputStream fileOutput = new FileOutputStream(storageFile);
            CompressedStreamTools.writeCompressed(rootTag, fileOutput);
            fileOutput.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadStorages() {
        try {
            File storageFile = new File(Minecraft.getMinecraft().mcDataDir, STORAGE_FILE_NAME);
            if (!storageFile.exists()) {
                return;
            }

            FileInputStream fileInput = new FileInputStream(storageFile);
            NBTTagCompound rootTag = CompressedStreamTools.readCompressed(fileInput);
            fileInput.close();

            storages.clear();

            if (rootTag.hasKey("StorageList")) {
                NBTTagList storagesList = rootTag.getTagList("StorageList", 10);

                for (int i = 0; i < storagesList.tagCount(); i++) {
                    NBTTagCompound storageTag = storagesList.getCompoundTagAt(i);

                    boolean isEnderChest = storageTag.getBoolean("IsEnderChest");
                    int storageNum = storageTag.getInteger("StorageNum");

                    if (storageTag.hasKey("Items")) {
                        NBTTagList itemsList = storageTag.getTagList("Items", 10);
                        Slot[] contents = new Slot[9 + itemsList.tagCount()];
                        for (int j = 0; j < 9; j++) contents[j] = null;
                        for (int j = 0; j < itemsList.tagCount(); j++) {
                            NBTTagCompound itemTag = itemsList.getCompoundTagAt(j);
                            if (!itemTag.hasNoTags()) {
                                ItemStack stack = ItemStack.loadItemStackFromNBT(itemTag);
                                contents[9 + j] = new TemporarySlot(stack);
                            } else {
                                contents[9 + j] = null;
                            }
                        }
                        Storage storage = new Storage(isEnderChest, storageNum, contents);
                        // Restore favorites if present (backward compatible)
                        if (storageTag.hasKey("IsFavorite")) storage.IsFavorite = storageTag.getBoolean("IsFavorite");
                        if (storageTag.hasKey("FavoriteTime")) storage.FavoriteTime = storageTag.getLong("FavoriteTime");
                        storages.add(storage);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class TemporarySlot extends Slot {
        private ItemStack stack;
        public TemporarySlot(ItemStack stack) {
            super(null, 0, 0, 0);
            this.stack = stack;
        }
        @Override public ItemStack getStack() { return stack; }
        @Override public void putStack(ItemStack stack) { this.stack = stack; }
        @Override public boolean getHasStack() { return stack != null; }
    }
}
