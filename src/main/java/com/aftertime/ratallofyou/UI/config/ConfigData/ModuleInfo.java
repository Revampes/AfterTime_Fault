package com.aftertime.ratallofyou.UI.config.ConfigData;

public class ModuleInfo extends BaseConfig<Boolean>{
    public final String category;
    public final Integer configGroupIndex; // null if no sub-settings group

    public ModuleInfo(String name, String description, String category, Boolean defaultState) {
        super(name, description, defaultState);
        this.category = category;
        this.configGroupIndex = null;
    }

    public ModuleInfo(String name, String description, String category, Boolean defaultState, Integer configGroupIndex) {
        super(name, description, defaultState);
        this.category = category;
        this.configGroupIndex = configGroupIndex;
    }
}
