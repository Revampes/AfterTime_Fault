package com.aftertime.ratallofyou.UI.utils;

import com.aftertime.ratallofyou.UI.config.ConfigData.FastHotkeyPreset;
import com.aftertime.ratallofyou.UI.config.ModSettingsGui;
import com.aftertime.ratallofyou.modules.SkyBlock.FastHotKey.FastHotkeyStore;

import java.util.List;

public class isFhkKeyDuplicate {
    private final ModSettingsGui gui;

    public isFhkKeyDuplicate(ModSettingsGui gui) {
        this.gui = gui;
    }

    public boolean isFhkKeyDuplicate(int keyCode, int exceptIndex) {
        if (keyCode <= 0) return false;
        List<FastHotkeyPreset> list = FastHotkeyStore.getInstance().getPresetsView();
        for (int i = 0; i < list.size(); i++) {
            if (i == exceptIndex) continue;
            FastHotkeyPreset p = list.get(i);
            if (p.keyCode == keyCode) return true;
        }
        return false;
    }
}
