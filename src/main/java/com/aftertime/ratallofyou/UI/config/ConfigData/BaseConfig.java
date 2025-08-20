package com.aftertime.ratallofyou.UI.config.ConfigData;

import java.lang.reflect.Type;

public class BaseConfig<T> {
    public final String name;
    public final String description;
    public T Data;
    public Type type;
    public BaseConfig(String name, String description, T Data) {
        this.name = name;
        this.description = description;
        this.Data = Data;
        type = Data.getClass();
    }
}
