package com.aftertime.ratallofyou.UI.Settings;

import com.aftertime.ratallofyou.UI.config.ConfigData.AllConfig;
import com.aftertime.ratallofyou.UI.config.ConfigData.ModuleInfo;

public class BooleanSettings {

    public static boolean isEnabled(String moduleKey) {
        try {
            ModuleInfo cfg = (ModuleInfo) AllConfig.INSTANCE.MODULES.get(moduleKey);
            return cfg != null && Boolean.TRUE.equals(cfg.Data);
        } catch (Throwable ignored) {
            return false;
        }
    }
}
