package com.aftertime.ratallofyou.modules.render.FastHotKey;

import com.aftertime.ratallofyou.settings.BooleanSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;

public class FastHotKey {
    private static final BooleanSetting MODULE_ENABLED = new BooleanSetting("Fast Hotkey");
    private final KeyBinding hotkey;
    private boolean wasKeyDown = false;

    public FastHotKey() {
        hotkey = new KeyBinding("Fast Hotkey", Keyboard.KEY_G, "Fast Hotkey");
        ClientRegistry.registerKeyBinding(hotkey);
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (!isModuleEnabled()) return;

        boolean isKeyDown = Keyboard.isKeyDown(hotkey.getKeyCode());

        if (isKeyDown && !wasKeyDown) { // Only trigger on key press, not hold
            if (Minecraft.getMinecraft().currentScreen instanceof FastHotKeyGui) {
                Minecraft.getMinecraft().displayGuiScreen(null);
            } else {
                Minecraft.getMinecraft().displayGuiScreen(new FastHotKeyGui());
            }
        }
        wasKeyDown = isKeyDown;
    }

    private static boolean isModuleEnabled() {
        return MODULE_ENABLED.isEnabled();
    }
}