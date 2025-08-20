package com.aftertime.ratallofyou.UI.config;

/**
 * Read This Before you Understand This IO System:
 * Loading: File -> Property -> AllConfig (For Better Access)
 * Saving: Property -> File
 * AllConfig Convert Property Key -> BaseConfig (Config Information)
 */
public class PropertyRef{
    public final int ConfigType;
    public final String Key;
    public PropertyRef(int ConfigType, String Key) {
        this.ConfigType = ConfigType;
        this.Key = Key;
    }
}
