package com.aftertime.ratallofyou.modules.SkyBlock.FastHotKey;


import com.aftertime.ratallofyou.UI.config.ConfigData.AllConfig;
import com.aftertime.ratallofyou.UI.config.ConfigData.ModuleInfo;
import com.aftertime.ratallofyou.UI.config.ConfigData.FastHotkeyPreset;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

public class FastHotKey {
    // Track whether we opened the GUI with a custom preset key and are waiting for key release
    private boolean guiOpenedByHotkey = false;
    private int openKeyCode = 0;
    private int openPresetIndex = -1;

    public FastHotKey() {
        // No global keybinding; per-preset key codes are captured in settings UI
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (!isModuleEnabled()) return;
        // Ignore if GUI already open via FastHotKey
        if (guiOpenedByHotkey) return;
        if (!Keyboard.getEventKeyState()) return; // only handle key down
        int key = Keyboard.getEventKey();
        if (key <= 0) return;
        // Find enabled preset with this key
        java.util.List<FastHotkeyPreset> presets = AllConfig.INSTANCE.FHK_PRESETS;
        for (int i = 0; i < presets.size(); i++) {
            FastHotkeyPreset p = presets.get(i);
            if (!p.enabled) continue;
            if (p.keyCode == key) {
                // Activate this preset and open GUI
                if (!(Minecraft.getMinecraft().currentScreen instanceof FastHotKeyGui)) {
                    AllConfig.INSTANCE.setActiveFhkPreset(i);
                    Minecraft.getMinecraft().displayGuiScreen(new FastHotKeyGui());
                    guiOpenedByHotkey = true;
                    openKeyCode = key;
                    openPresetIndex = i;
                }
                break; // handle only one
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
                resetOpenState();
                return;
            }
            // If the trigger key is no longer physically held, confirm selection and close
            boolean down = openKeyCode != 0 && Keyboard.isKeyDown(openKeyCode);
            if (!down) {
                FastHotKeyGui gui = (FastHotKeyGui) mc.currentScreen;
                gui.onHotkeyReleased();
                resetOpenState();
            }
        }
    }

    private void resetOpenState() {
        guiOpenedByHotkey = false; openKeyCode = 0; openPresetIndex = -1;
    }

    private static boolean isModuleEnabled() {
        ModuleInfo cfg = (ModuleInfo) AllConfig.INSTANCE.MODULES.get("skyblock_fasthotkey");
        return cfg != null && Boolean.TRUE.equals(cfg.Data);
    }
}