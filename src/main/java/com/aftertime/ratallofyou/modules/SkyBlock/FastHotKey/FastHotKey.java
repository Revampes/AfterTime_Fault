package com.aftertime.ratallofyou.modules.SkyBlock.FastHotKey;


import com.aftertime.ratallofyou.UI.config.ConfigData.AllConfig;
import com.aftertime.ratallofyou.UI.config.ConfigData.ModuleInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;

public class FastHotKey {
    // Expose a static keybinding so it can be remapped in Controls and referenced elsewhere
    public static final KeyBinding HOTKEY = new KeyBinding("Open Fast Hotkey", Keyboard.KEY_G, "Rat All Of You");

    public FastHotKey() {
        ClientRegistry.registerKeyBinding(HOTKEY);
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (!isModuleEnabled()) return;

        // Trigger only on key press events, respects user remapping
        if (HOTKEY.isPressed()) {
            if (Minecraft.getMinecraft().currentScreen instanceof FastHotKeyGui) {
                Minecraft.getMinecraft().displayGuiScreen(null);
            } else {
                Minecraft.getMinecraft().displayGuiScreen(new FastHotKeyGui());
            }
        }
    }

    private static boolean isModuleEnabled() {
        ModuleInfo cfg = (ModuleInfo) AllConfig.INSTANCE.MODULES.get("skyblock_fasthotkey");
        return cfg != null && Boolean.TRUE.equals(cfg.Data);
    }
}