package com.aftertime.ratallofyou.modules.dungeon.CustomTerminal;

import com.aftertime.ratallofyou.modules.dungeon.terminals.Colors;
import com.aftertime.ratallofyou.modules.dungeon.terminals.melody;
import com.aftertime.ratallofyou.modules.dungeon.terminals.numbers;
import com.aftertime.ratallofyou.modules.dungeon.terminals.redgreen;
import com.aftertime.ratallofyou.modules.dungeon.terminals.rubix;
import com.aftertime.ratallofyou.modules.dungeon.terminals.startswith;

/**
 * Manager to disable original RAT terminal helpers to avoid event conflicts
 * with the Kotlin CustomTerminal BetterTerminal overlay.
 */
public final class BetterTerminal {
    private BetterTerminal() {}

    public static void init() {
        forceDisableAll();
    }

    private static void forceDisableAll() {
        try { numbers.setEnabled(false); } catch (Throwable ignored) {}
        try { startswith.setEnabled(false); } catch (Throwable ignored) {}
        try { Colors.setEnabled(false); } catch (Throwable ignored) {}
        try { redgreen.setEnabled(false); } catch (Throwable ignored) {}
        try { rubix.setEnabled(false); } catch (Throwable ignored) {}
        try { melody.setEnabled(false); } catch (Throwable ignored) {}
    }
}

