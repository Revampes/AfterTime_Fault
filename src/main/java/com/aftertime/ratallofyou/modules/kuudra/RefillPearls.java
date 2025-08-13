package com.aftertime.ratallofyou.modules.kuudra;

import com.aftertime.ratallofyou.UI.ModConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class RefillPearls {
    private boolean allowPearlRefill = true;
    private long lastRefillTime = 0;
    private long lastInteractTime = 0;
    private long lastTransferTime = 0;
    private int tickCounter = 0;
    private boolean hasShownNoPearlsWarning = false;

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        tickCounter++;

        // Skip if player doesn't exist or mod is disabled
        if (Minecraft.getMinecraft().thePlayer == null || !isModuleEnabled("Pearl Refill (Use at your own risk") || !allowPearlRefill) {
            return;
        }

        // Only check every 10 ticks (0.5 seconds)
        if (tickCounter % 10 != 0) return;

        ItemStack pearlStack = findPearlStack();
        if (pearlStack == null) {
            if (!hasShownNoPearlsWarning) {
                Minecraft.getMinecraft().ingameGUI.displayTitle("§cNo Ender Pearls in your sack!", "", 5, 20, 5);
                hasShownNoPearlsWarning = true;
            }
            return;
        } else {
            hasShownNoPearlsWarning = false;
        }

        int stackSize = pearlStack.stackSize;
        long currentTime = System.currentTimeMillis();

        if (stackSize < 2 || (stackSize < 16 &&
                currentTime - lastRefillTime > 5000 &&
                currentTime - lastInteractTime > 5000 &&
                currentTime - lastTransferTime > 3000)) {

            allowPearlRefill = false;
            int toGive = 16 - stackSize;

            if (toGive < 15) {
                lastInteractTime = currentTime;
            }

            lastRefillTime = currentTime;
            Minecraft.getMinecraft().thePlayer.sendChatMessage("/gfs ender_pearl " + toGive);
        }
    }

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        if (event.message == null) return;

        String message = event.message.getUnformattedText();
        if (message.contains("Moved") && message.contains("Ender Pearl") && message.contains("from your Sacks")) {
            lastTransferTime = System.currentTimeMillis();
            allowPearlRefill = true;
        }
    }

    private ItemStack findPearlStack() {
        for (int i = 0; i < 36; i++) {
            ItemStack stack = Minecraft.getMinecraft().thePlayer.inventory.getStackInSlot(i);
            if (stack != null && stack.getItem() == Item.getItemById(368)) {
                return stack;
            }
        }
        return null;
    }

    public void onPlayerInteract() {
        ItemStack pearlStack = findPearlStack();
        if (pearlStack != null && pearlStack.stackSize >= 16) {
            lastInteractTime = System.currentTimeMillis();
        }
    }

    private boolean isModuleEnabled(String moduleName) {
        for (ModConfig.ModuleInfo module : ModConfig.MODULES) {
            if (module.name.equals(moduleName)) {
                return module.enabled;
            }
        }
        return false;
    }
}



