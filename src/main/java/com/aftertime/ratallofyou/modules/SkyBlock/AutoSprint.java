package com.aftertime.ratallofyou.modules.SkyBlock;


import com.aftertime.ratallofyou.UI.config.ConfigData.AllConfig;
import com.aftertime.ratallofyou.UI.config.ConfigData.ModuleInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

public class AutoSprint {
    private boolean wasEnabled = false;
    private boolean messageSent = false;
    private boolean temporaryDisabled = false;
    private int cooldown = 0;

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || mc.theWorld == null) {
            wasEnabled = false;
            messageSent = false;
            temporaryDisabled = false;
            cooldown = 0;
            return;
        }

        if (mc.currentScreen != null) {
            return;
        }

        // Handle cooldown
        if (cooldown > 0) {
            cooldown--;
            return;
        }

        // Check for sprint key press to toggle
        if (Keyboard.isKeyDown(mc.gameSettings.keyBindSprint.getKeyCode())) {
            if (isModuleEnabled() && !temporaryDisabled) {
                temporaryDisabled = true;
                mc.thePlayer.addChatMessage(
                        new ChatComponentText(EnumChatFormatting.GOLD + "[RatAllOfYou] " +
                                EnumChatFormatting.RED + "Auto Sprint Disabled")
                );
                cooldown = 10; // 0.5 second cooldown (20 ticks = 1 second)
            } else if (isModuleEnabled() && temporaryDisabled) {
                temporaryDisabled = false;
                mc.thePlayer.addChatMessage(
                        new ChatComponentText(EnumChatFormatting.GOLD + "[RatAllOfYou] " +
                                EnumChatFormatting.GREEN + "Auto Sprint Enabled")
                );
                cooldown = 10;
            }
        }

        if (isModuleEnabled() && !temporaryDisabled) {
            KeyBinding.setKeyBindState(
                    mc.gameSettings.keyBindSprint.getKeyCode(),
                    true
            );

            // Send initial enable message
            if (!messageSent) {
                mc.thePlayer.addChatMessage(
                        new ChatComponentText(EnumChatFormatting.GOLD + "[RatAllOfYou] " +
                                EnumChatFormatting.GREEN + "Auto Sprint Enabled")
                );
                messageSent = true;
                wasEnabled = true;
            }
        } else {
            // Reset state when disabled
            if (wasEnabled && !messageSent) {
                mc.thePlayer.addChatMessage(
                        new ChatComponentText(EnumChatFormatting.GOLD + "[RatAllOfYou] " +
                                EnumChatFormatting.RED + "Auto Sprint Disabled")
                );
                messageSent = true;
            }
            wasEnabled = false;
        }
    }

    private boolean isModuleEnabled() {
        ModuleInfo cfg = (ModuleInfo) AllConfig.INSTANCE.MODULES.get("skyblock_autosprint");
        return cfg != null && Boolean.TRUE.equals(cfg.Data);
    }
}
