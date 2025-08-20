package com.aftertime.ratallofyou.KeyBind;

import com.aftertime.ratallofyou.UI.config.ModSettingsGui;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;

public class KeybindHandler {
    private static KeyBinding configGuiKey = new KeyBinding("Open Config GUI", Keyboard.KEY_RSHIFT, "Rat All Of You");

    public static void registerKeybinds() {
        ClientRegistry.registerKeyBinding(configGuiKey);
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (configGuiKey.isPressed()) {
            Minecraft.getMinecraft().displayGuiScreen(new ModSettingsGui());
        }
    }
}