package com.aftertime.ratallofyou.modules.kuudra;


import com.aftertime.ratallofyou.UI.config.ConfigData.AllConfig;
import com.aftertime.ratallofyou.UI.config.ConfigData.ModuleInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.event.world.WorldEvent;

public class RefillPearls {
    private boolean allowPearlRefill = true;
    private long lastRefillTime = 0;
    private long lastInteractTime = 0;
    private long lastTransferTime = 0;
    private int tickCounter = 0;
    private boolean hasShownNoPearlsWarning = false;

    // Track counts to differentiate Spirit Leap vs Ender Pearl consumption
    private int prevPearlCount = -1;       // counts only true Ender Pearls
    private int prevSpiritLeapCount = -1;  // counts Spirit Leaps (same id, different name)

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        tickCounter++;

        if (Minecraft.getMinecraft().thePlayer == null || !isModuleEnabled() || !allowPearlRefill) {
            return;
        }

        // Every 20 ticks (~1s)
        if (tickCounter % 20 != 0) return;

        int pearlCount = countEnderPearls();
        int spiritLeapCount = countSpiritLeaps();

        if (prevPearlCount < 0 || prevSpiritLeapCount < 0) {
            prevPearlCount = pearlCount;
            prevSpiritLeapCount = spiritLeapCount;
        }

        boolean spiritLeapConsumed = prevSpiritLeapCount > spiritLeapCount;
        boolean pearlConsumed = prevPearlCount > pearlCount;

        ItemStack pearlStack = findPearlStack();
        if (pearlStack == null) {
            if (!hasShownNoPearlsWarning) {
                hasShownNoPearlsWarning = true;
            }
            prevPearlCount = pearlCount;
            prevSpiritLeapCount = spiritLeapCount;
            return;
        } else {
            hasShownNoPearlsWarning = false;
        }

        int stackSize = pearlStack.stackSize;
        long currentTime = System.currentTimeMillis();

        // If only a Spirit Leap was used, skip
        if (spiritLeapConsumed && !pearlConsumed) {
            prevPearlCount = pearlCount;
            prevSpiritLeapCount = spiritLeapCount;
            return;
        }

        boolean shouldRefill = false;
        int toGive = 0;

        // Emergency refill when nearly out
        if (stackSize < 2) {
            shouldRefill = true;
            toGive = Math.max(1, 16 - stackSize);
        } else if (pearlConsumed) {
            // Top off after actual pearl consumption (not after Spirit Leap)
            if (stackSize < 16 &&
                currentTime - lastRefillTime > 5000 &&
                currentTime - lastInteractTime > 5000 &&
                currentTime - lastTransferTime > 3000) {
                shouldRefill = true;
                toGive = 16 - stackSize;
            }
        }

        if (shouldRefill && toGive > 0) {
            allowPearlRefill = false;
            if (toGive < 15) {
                lastInteractTime = currentTime;
            }
            lastRefillTime = currentTime;
            Minecraft.getMinecraft().thePlayer.sendChatMessage("/gfs ender_pearl " + toGive);
        }

        // Update baselines after decision
        prevPearlCount = pearlCount;
        prevSpiritLeapCount = spiritLeapCount;
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

    // Re-enable pearl refill and reset state on world load (client-side)
    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        // Only act on client worlds
        // event.world.isRemote is true on client; guard via Minecraft instance just in case
        if (Minecraft.getMinecraft() == null) return;
        allowPearlRefill = true;
        hasShownNoPearlsWarning = false;
        prevPearlCount = -1;
        prevSpiritLeapCount = -1;
        lastRefillTime = 0;
        lastInteractTime = 0;
        lastTransferTime = 0;
        tickCounter = 0;
    }

    private boolean isSpiritLeap(ItemStack stack) {
        if (stack == null) return false;
        if (stack.getItem() != Item.getItemById(368)) return false;
        String name = stack.getDisplayName();
        if (name == null) return false;
        name = EnumChatFormatting.getTextWithoutFormattingCodes(name).toLowerCase();
        return name.contains("spirit leap");
    }

    private boolean isEnderPearl(ItemStack stack) {
        if (stack == null) return false;
        if (stack.getItem() != Item.getItemById(368)) return false;
        return !isSpiritLeap(stack);
    }

    private int countEnderPearls() {
        int total = 0;
        for (int i = 0; i < 36; i++) {
            ItemStack stack = Minecraft.getMinecraft().thePlayer.inventory.getStackInSlot(i);
            if (isEnderPearl(stack)) total += stack.stackSize;
        }
        return total;
    }

    private int countSpiritLeaps() {
        int total = 0;
        for (int i = 0; i < 36; i++) {
            ItemStack stack = Minecraft.getMinecraft().thePlayer.inventory.getStackInSlot(i);
            if (isSpiritLeap(stack)) total += stack.stackSize;
        }
        return total;
    }

    private ItemStack findPearlStack() {
        for (int i = 0; i < 36; i++) {
            ItemStack stack = Minecraft.getMinecraft().thePlayer.inventory.getStackInSlot(i);
            if (isEnderPearl(stack)) return stack;
        }
        return null;
    }

    public void onPlayerInteract() {
        ItemStack pearlStack = findPearlStack();
        if (pearlStack != null && pearlStack.stackSize >= 16) {
            lastInteractTime = System.currentTimeMillis();
        }
    }

    private boolean isModuleEnabled() {
        ModuleInfo cfg = (ModuleInfo) AllConfig.INSTANCE.MODULES.get("kuudra_pearlrefill");
        return cfg != null && Boolean.TRUE.equals(cfg.Data);
    }
}
