package com.aftertime.ratallofyou.modules.Performance;

import com.aftertime.ratallofyou.UI.config.ConfigData.AllConfig;
import com.aftertime.ratallofyou.UI.config.ConfigData.ModuleInfo;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.regex.Pattern;

public class HideUselessMessage {
    // Ported regex list from ChatTriggers BlockUselessMessages.js
    private static final String[] REGEX_STRINGS = new String[]{
            "Your .+ hit .+ for [\\d,.]+ damage\\.",
            "There are blocks in the way!",
            "You do not have enough mana to do this!",
            "\\+\\d+ Kill Combo.+",
            "Thunderstorm is ready to use! Press DROP to activate it!",
            ".+ healed you for .+ health!",
            "You earned .+ GEXP from playing .+!",
            ".+ unlocked .+ Essence!",
            ".+ unlocked .+ Essence x\\d+!",
            "This ability is on cooldown for 1s\\.",
            "You do not have the key for this door!",
            "The Stormy .+ struck you for .+ damage!",
            "Please wait a few seconds between refreshing!",
            "You cannot move the silverfish in that direction!",
            "You cannot hit the silverfish while it's moving!",
            "Your Kill Combo has expired! You reached a .+ Kill Combo!",
            "Your active Potion Effects have been paused and stored\\. They will be restored when you leave Dungeons! You are not allowed to use existing Potion Effects while in Dungeons\\.",
            ".+ has obtained Blood Key!",
            "The Flamethrower hit you for .+ damage!",
            ".+ found a Wither Essence! Everyone gains an extra essence!",
            "Ragnarok is ready to use! Press DROP to activate it!",
            "This creature is immune to this kind of magic!"
    };

    private static final Pattern[] PATTERNS;
    static {
        PATTERNS = new Pattern[REGEX_STRINGS.length];
        for (int i = 0; i < REGEX_STRINGS.length; i++) {
            // Anchor to full line to mimic ChatTriggers criteria behavior
            PATTERNS[i] = Pattern.compile("^" + REGEX_STRINGS[i] + "$");
        }
    }

    private static boolean isEnabled() {
        ModuleInfo cfg = (ModuleInfo) AllConfig.INSTANCE.MODULES.get("performance_hideuselessmsg");
        return cfg != null && Boolean.TRUE.equals(cfg.Data);
    }

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        if (!isEnabled()) return;
        if (event == null || event.message == null) return;
        String msg = event.message.getUnformattedText();
        if (msg == null || msg.isEmpty()) return;

        for (Pattern p : PATTERNS) {
            if (p.matcher(msg).matches()) {
                event.setCanceled(true);
                return;
            }
        }
    }
}
