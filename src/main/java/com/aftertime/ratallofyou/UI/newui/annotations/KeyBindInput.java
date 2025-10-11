package com.aftertime.ratallofyou.UI.newui.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface KeyBindInput {
    String key();
    String title();
}