package com.aftertime.ratallofyou.settings;

import com.aftertime.ratallofyou.UI.config.ConfigStorage;

public class BooleanSetting {
    private final String moduleName;

    public BooleanSetting(String moduleName) {
        this.moduleName = moduleName;
    }

    public boolean isEnabled() {
        for (ConfigStorage.ModuleInfo module : ConfigStorage.MODULES) {
            if (module.name.equals(moduleName)) {
                return module.enabled;
            }
        }
        return false;
    }

    public String getModuleName() {
        return moduleName;
    }
}