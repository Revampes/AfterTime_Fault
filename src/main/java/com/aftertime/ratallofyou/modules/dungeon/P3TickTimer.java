package com.aftertime.ratallofyou.modules.dungeon;

import com.aftertime.ratallofyou.config.ModConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import com.aftertime.ratallofyou.config.ModConfig.ModuleInfo;

public class P3TickTimer {
    private int barrierTicks = 0;
    private boolean isTimerActive = false;
    private final Minecraft mc = Minecraft.getMinecraft();

    @SubscribeEvent
    public void onChat(net.minecraftforge.client.event.ClientChatReceivedEvent event) {
        if (!isModuleEnabled("Phase 3 Tick Timer")) return;

        String message = event.message.getUnformattedText();

        // Goldor messages
        if (message.equals("[BOSS] Goldor: Who dares trespass into my domain?") ||
                message.equals("[BOSS] Goldor: What do you think you are doing there!")) {
            startTimer();
        }
        // Party proc messages
        else if (message.matches("Party > (?:\\[.+\\])? ?(?:.+)?[ቾ⚒]?: (Bonzo|Phoenix) Procced!?(?: \\(3s\\))?")) {
            startTimer();
        }
        // Core opening
        else if (message.equals("The Core entrance is opening!")) {
            resetTimer();
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (!isModuleEnabled("Phase 3 Tick Timer") || !isTimerActive || event.phase != TickEvent.Phase.START) return;

        barrierTicks--;
        if (barrierTicks <= 0) {
            barrierTicks = 60; // Reset to 3 seconds (60 ticks)
        }
    }

    public void onRender(RenderGameOverlayEvent.Post event) {
        if (!isModuleEnabled("Phase 3 Tick Timer") || !isTimerActive || event.type != RenderGameOverlayEvent.ElementType.ALL) return;

        String time = String.format("%.2f", barrierTicks / 20.0f);
        String formattedTime;

        if (barrierTicks >= 40) { // 2+ seconds
            formattedTime = EnumChatFormatting.GREEN + time;
        } else if (barrierTicks >= 20) { // 1-2 seconds
            formattedTime = EnumChatFormatting.YELLOW + time;
        } else { // <1 second
            formattedTime = EnumChatFormatting.RED + time;
        }

        // Get position from module's slider value
        ModuleInfo timerModule = getModule("Phase 3 Tick Timer");
        if (timerModule != null) {
            int x = (int)(timerModule.sliderValue * mc.displayWidth);
            int y = (int)(0.75f * mc.displayHeight); // Fixed Y position or use another slider if needed

            mc.fontRendererObj.drawStringWithShadow(
                    formattedTime,
                    x,
                    y,
                    0xFFFFFF
            );
        }
    }

    private ModuleInfo getModule(String name) {
        for (ModConfig.ModuleInfo module : ModConfig.MODULES) {
            if (module.name.equals(name)) {
                return module;
            }
        }
        return null;
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        resetTimer();
    }

    private void startTimer() {
        barrierTicks = 60; // 3 seconds (60 ticks)
        isTimerActive = true;
        mc.thePlayer.addChatMessage(new ChatComponentText(
                EnumChatFormatting.GOLD + "[RatAllOfYou] " +
                        EnumChatFormatting.GREEN + "Phase 3 Timer Started"
        ));
    }

    private void resetTimer() {
        barrierTicks = 0;
        isTimerActive = false;
    }

    public static void register() {
        MinecraftForge.EVENT_BUS.register(new P3TickTimer());
    }

    private boolean isModuleEnabled(String moduleName) {
        for (ModConfig.ModuleInfo module : ModConfig.MODULES) {
            if (module.name.equals(moduleName)) {
                return module.enabled;
            }
        }
        return false;
    }
}