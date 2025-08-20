package com.aftertime.ratallofyou.modules.SkyBlock.FastHotKey;


import com.aftertime.ratallofyou.UI.config.ConfigData.AllConfig;
import com.aftertime.ratallofyou.UI.config.ConfigData.ModuleInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

public class FastHotKey {
    // Expose a static keybinding so it can be remapped in Controls and referenced elsewhere
    public static final KeyBinding HOTKEY = new KeyBinding("Open Fast Hotkey", Keyboard.KEY_G, "Rat All Of You");

    // Track whether we opened the GUI with the hotkey and are waiting for key release
    private boolean guiOpenedByHotkey = false;

    public FastHotKey() {
        ClientRegistry.registerKeyBinding(HOTKEY);
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (!isModuleEnabled()) return;

        // Open on key press only; selection happens when key is released
        if (HOTKEY.isPressed()) {
            if (!(Minecraft.getMinecraft().currentScreen instanceof FastHotKeyGui)) {
                Minecraft.getMinecraft().displayGuiScreen(new FastHotKeyGui());
                guiOpenedByHotkey = true;
            }
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (!isModuleEnabled()) return;
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (guiOpenedByHotkey) {
            if (!(mc.currentScreen instanceof FastHotKeyGui)) {
                // GUI closed by other means (e.g., ESC or click) -> reset state
                guiOpenedByHotkey = false;
                return;
            }

            // If the hotkey is no longer physically held, confirm selection and close
            int code = HOTKEY.getKeyCode();
            boolean down = code != 0 && Keyboard.isKeyDown(code);
            if (!down) {
                FastHotKeyGui gui = (FastHotKeyGui) mc.currentScreen;
                gui.onHotkeyReleased();
                guiOpenedByHotkey = false;
            }
        }
    }

    private static boolean isModuleEnabled() {
        ModuleInfo cfg = (ModuleInfo) AllConfig.INSTANCE.MODULES.get("skyblock_fasthotkey");
        return cfg != null && Boolean.TRUE.equals(cfg.Data);
    }
}