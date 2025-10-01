package com.aftertime.ratallofyou.UI.config.OptionElements;

import com.aftertime.ratallofyou.UI.config.ConfigData.AllConfig;
import com.aftertime.ratallofyou.UI.config.ConfigData.BaseConfig;
import com.aftertime.ratallofyou.UI.config.ConfigIO;
import com.aftertime.ratallofyou.UI.config.PropertyRef;
import com.aftertime.ratallofyou.modules.dungeon.terminals.TerminalSettingsApplier;

/**
 * Factory class that creates special checkboxes for different modules.
 * This centralizes all module-specific checkbox behaviors.
 */
public class SpecialCheckboxFactory {

    /**
     * Creates a special checkbox if the given config key requires special behavior.
     * Returns null if it's a normal checkbox.
     */
    public static SpecialCheckbox createSpecialCheckbox(PropertyRef ref, String name, String description,
                                                       boolean initial, int x, int y, int width, int height) {

        // Custom Cape reload checkbox
        if (ref.ConfigType == 14 && "customcape_reloadcape".equals(ref.Key)) {
            return new CapeReloadCheckbox(ref, name, description, initial, x, y, width, height);
        }

        // Terminal settings checkbox (applies settings when toggled)
        if (ref.ConfigType == 4) {
            return new TerminalSettingsCheckbox(ref, name, description, initial, x, y, width, height);
        }

        // Add more special checkboxes here for other modules as needed
        // Example:
        // if (ref.ConfigType == 10 && "autofish_special_action".equals(ref.Key)) {
        //     return new AutoFishSpecialCheckbox(ref, name, description, initial, x, y, width, height);
        // }

        return null; // Not a special checkbox
    }

    /**
     * Cape reload checkbox implementation
     */
    private static class CapeReloadCheckbox extends SpecialCheckbox {
        public CapeReloadCheckbox(PropertyRef ref, String name, String description, boolean initial, int x, int y, int width, int height) {
            super(ref, name, description, initial, x, y, width, height);
        }

        @Override
        protected void onSpecialToggle() {
            // Only perform reload if checkbox was turned ON
            if (Data) {
                try {
                    com.aftertime.ratallofyou.modules.render.CustomCape.getInstance().reloadCape();
                } catch (Exception e) {
                    System.err.println("[SpecialCheckbox] Error reloading cape: " + e.getMessage());
                }
            }
        }

        @Override
        protected boolean shouldAutoReset() {
            return Data; // Reset to false if it was turned on
        }
    }

    /**
     * Terminal settings checkbox implementation
     */
    private static class TerminalSettingsCheckbox extends SpecialCheckbox {
        public TerminalSettingsCheckbox(PropertyRef ref, String name, String description, boolean initial, int x, int y, int width, int height) {
            super(ref, name, description, initial, x, y, width, height);
        }

        @Override
        protected void onSpecialToggle() {
            // Apply terminal settings whenever any terminal checkbox is toggled
            TerminalSettingsApplier.applyFromAllConfig();
        }

        @Override
        protected boolean shouldAutoReset() {
            return false; // Normal checkbox behavior
        }
    }

    // Add more checkbox implementations here as needed:

    /**
     * Example: Auto Fish special action checkbox
     */
    /*
    private static class AutoFishSpecialCheckbox extends SpecialCheckbox {
        public AutoFishSpecialCheckbox(PropertyRef ref, String name, String description, boolean initial, int x, int y, int width, int height) {
            super(ref, name, description, initial, x, y, width, height);
        }

        @Override
        protected void onSpecialToggle() {
            if (Data) {
                // Perform some special action for auto fish
                System.out.println("Auto Fish special action triggered!");
                // Example: reset some timer, clear cache, etc.
            }
        }

        @Override
        protected boolean shouldAutoReset() {
            return true; // Reset to false after action
        }
    }
    */
}
