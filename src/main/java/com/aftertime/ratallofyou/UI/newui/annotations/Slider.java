package com.aftertime.ratallofyou.UI.newui.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Slider {
    String key();
    float min();
    float max();
    // Optional display title shown above the slider bar; falls back to prettified field name when empty
    String title() default "";
}