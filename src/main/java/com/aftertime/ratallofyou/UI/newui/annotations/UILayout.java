package com.aftertime.ratallofyou.UI.newui.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface UILayout {
    // Module grouping key (must match the ToggleButton key of the module)
    String key();
    // Display title for this layout section in the UI
    String title();
    // Default layout values
    int posx() default 0;
    int posy() default 0;
    float scale() default 1.0f;
}
