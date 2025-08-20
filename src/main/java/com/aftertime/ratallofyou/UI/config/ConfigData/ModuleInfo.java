package com.aftertime.ratallofyou.UI.config.ConfigData;

public class ModuleInfo extends BaseConfig<Boolean>{
    public final String category;

    public ModuleInfo(String name, String description, String category, Boolean defaultState) {
        super(name, description, defaultState);
        this.category = category;
    }
}
