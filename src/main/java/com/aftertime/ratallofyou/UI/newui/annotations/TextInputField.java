package com.aftertime.ratallofyou.UI.newui.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface TextInputField {
    String key();
    String title();
    int maxLength() default 32;
}