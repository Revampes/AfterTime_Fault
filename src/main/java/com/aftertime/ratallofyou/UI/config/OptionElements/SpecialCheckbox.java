package com.aftertime.ratallofyou.UI.config.OptionElements;

import com.aftertime.ratallofyou.UI.config.PropertyRef;

/**
 * Special checkbox that performs custom actions when toggled.
 * This allows for module-specific behaviors without cluttering the main GUI code.
 */
public abstract class SpecialCheckbox extends Toggle {

    public SpecialCheckbox(PropertyRef ref, String name, String description, boolean initial, int x, int y, int width, int height) {
        super(ref, name, description, initial, x, y, width, height);
    }

    @Override
    public void toggle() {
        // Call the parent toggle to update the checkbox state
        super.toggle();

        // Perform the special action
        onSpecialToggle();

        // Auto-reset the checkbox to false if needed (like the cape reload)
        if (shouldAutoReset()) {
            Data = false;
            // Update the underlying config as well
            OnValueChange();
        }
    }

    /**
     * Override this method to define what happens when the checkbox is toggled.
     */
    protected abstract void onSpecialToggle();

    /**
     * Override this method to specify if the checkbox should auto-reset to false after toggle.
     * Default is false (normal checkbox behavior).
     */
    protected boolean shouldAutoReset() {
        return false;
    }
}
