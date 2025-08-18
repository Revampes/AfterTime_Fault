package com.aftertime.ratallofyou.UI.config;

public class Singleconfig {
    public final String name;
    public final String description;
    public boolean enabled;

    public Singleconfig(String name, String description, boolean defaultState) {
        this.name = name;
        this.description = description;
        this.enabled = defaultState;
    }
}
