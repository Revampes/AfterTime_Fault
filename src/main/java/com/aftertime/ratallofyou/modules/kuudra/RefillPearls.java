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

public class RefillPearls {
    private boolean allowPearlRefill = true;
    private long lastRefillTime = 0;
    private long lastInteractTime = 0;
    private long lastTransferTime = 0;
    private int tickCounter = 0;
    private boolean hasShownNoPearlsWarning = false;
    // Alternate between underscore and space command variants for broader compatibility
    private boolean useUnderscoreCmd = true;

    // Track counts to differentiate Spirit Leap vs Ender Pearl consumption
    private int prevPearlCount = -1;       // counts only true Ender Pearls
    private int prevSpiritLeapCount = -1;  // counts Spirit Leaps (same id, different name)

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        tickCounter++;

        if (Minecraft.getMinecraft().thePlayer == null || !isModuleEnabled()) {
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
        // Don't early-return when there is no pearl stack; we'll try to pull from sacks
        if (pearlStack == null) {
            if (!hasShownNoPearlsWarning) {
                hasShownNoPearlsWarning = true;
            }
        } else {
            hasShownNoPearlsWarning = false;
        }

        int stackSize = pearlStack == null ? 0 : pearlStack.stackSize;
        long currentTime = System.currentTimeMillis();

        // Auto-unlock if inventory increased or after timeout (avoid getting stuck if chat pattern changes)
        if (!allowPearlRefill) {
            if (pearlCount > prevPearlCount || currentTime - lastRefillTime > 4000) {
                allowPearlRefill = true;
            }
        }

        // If a Spirit Leap was used, we still want to top up Ender Pearls (models overlap in-game)
        boolean anyTeleportConsumed = pearlConsumed || spiritLeapConsumed;

        boolean shouldRefill = false;
        int toGive = 0;

        // Emergency refill when nearly out or none in inventory
        if (stackSize < 2) {
            shouldRefill = true;
            toGive = Math.max(1, 16 - stackSize);
        } else if (anyTeleportConsumed) {
            // Top off after any teleport-type consumption
            if (stackSize < 16 &&
                currentTime - lastRefillTime > 5000 &&
                currentTime - lastInteractTime > 5000 &&
                currentTime - lastTransferTime > 3000) {
                shouldRefill = true;
                toGive = 16 - stackSize;
            }
        }

        if (shouldRefill && toGive > 0 && allowPearlRefill) {
            allowPearlRefill = false;
            if (toGive < 15) {
                lastInteractTime = currentTime;
            }
            lastRefillTime = currentTime;
            String itemArg = useUnderscoreCmd ? "ender_pearl" : "ender pearl";
            useUnderscoreCmd = !useUnderscoreCmd; // alternate next time
            Minecraft.getMinecraft().thePlayer.sendChatMessage("/gfs " + itemArg + " " + toGive);
        }

        // Update baselines after decision
        prevPearlCount = pearlCount;
        prevSpiritLeapCount = spiritLeapCount;
    }

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        if (event.message == null) return;

        String messageRaw = event.message.getUnformattedText();
        if (messageRaw == null) return;
        String message = EnumChatFormatting.getTextWithoutFormattingCodes(messageRaw).toLowerCase();
        if (message.contains("moved") && message.contains("ender pearl") &&
            (message.contains("from your sacks") || message.contains("from your sack"))) {
            lastTransferTime = System.currentTimeMillis();
            allowPearlRefill = true;
        }
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
