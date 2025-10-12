package com.aftertime.ratallofyou.modules.Fishing;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundCategory;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

import com.aftertime.ratallofyou.config.ModConfig;
import com.aftertime.ratallofyou.UI.newui.config.ModConfigIO;

import java.util.List;
import java.util.Random;

public class AutoFish {
    // State
    private static final Minecraft mc = Minecraft.getMinecraft();

    private boolean autoFishActive = false; // whether we currently have a hook out
    private int hookTick = -1;              // ticks since hook thrown; -1 if no hook
    private int hookThrownCooldown = 0;     // cool down between auto throws
    private boolean nextTickThrow = false;  // after reeling, throw again next tick

    private float oldPlayersVolume = 1.0f;  // restore sound category on disable

    // Sneak handling
    private boolean holdSneakActive = false; // tracking if we are holding sneak due to cfgSneak

    // AutoShift feature
    private final Random rng = new Random();
    private int autoshiftCountdownTicks = -1;    // ticks until next auto shift tap
    private int autoshiftPressTicks = 0;         // remaining ticks to keep sneak pressed for tap

    // Cached config helpers
    private boolean isModuleEnabled() {
        return ModConfig.enabledAutoFish;
    }

    private boolean cfgSneak() { return ModConfig.autofishSneakHold; }
    private boolean cfgThrowIfNoHook() { return ModConfig.autofishThrowIfNoHook; }
    private int cfgThrowCooldownTicks() { return Math.max(0, ModConfig.autofishThrowCooldownS) * 20; }
    private boolean cfgRethrow() { return ModConfig.autofishRethrow; }
    private int cfgRethrowCooldownTicks() { return Math.max(1, ModConfig.autofishRethrowTimeoutS) * 20; }
    private boolean cfgSlug() { return ModConfig.autofishSlugMode; }
    private boolean cfgMessage() { return ModConfig.autofishMessages; }
    private boolean cfgShowTimer() { return ModConfig.autofishShowTimer; }
    private int cfgTimerX() { return ModConfig.autofishTimerX; }
    private int cfgTimerY() { return ModConfig.autofishTimerY; }
    private boolean cfgAutoShift() { return ModConfig.enabledAutoShift; }
    private int cfgAutoShiftIntervalS() { return Math.max(1, ModConfig.autofishAutoShiftIntervalS); }
    private int cfgHotkey() {
        try {
            String name = ModConfig.autofishHotkeyName;
            if (name == null) return 0;
            name = name.trim();
            if (name.isEmpty() || name.equalsIgnoreCase("none")) return 0;
            int code = Keyboard.getKeyIndex(name);
            return Math.max(0, code);
        } catch (Throwable ignored) { return 0; }
    }

    // Lifecycle hooks
    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load e) {
        resetState();
        // Auto disable (sound restore) if the module was on while changing worlds
        if (isModuleEnabled()) {
            restoreSound();
        }
    }

    private void resetState() {
        autoFishActive = false;
        hookTick = -1;
        hookThrownCooldown = 0;
        nextTickThrow = false;
        // release sneak we held, but don't override user input
        if (holdSneakActive) releaseSneakIfNotPhysicallyDown();
        holdSneakActive = false;
        autoshiftCountdownTicks = -1;
        autoshiftPressTicks = 0;
    }

    // New: toggle hotkey handler
    @net.minecraftforge.fml.common.eventhandler.SubscribeEvent
    public void onKeyInput(net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent event) {
        try {
            if (mc == null || mc.thePlayer == null) return;
            if (mc.currentScreen != null) return; // avoid toggling while typing in GUIs
            int bound = cfgHotkey();
            if (bound <= 0) return; // not bound
            if (!org.lwjgl.input.Keyboard.getEventKeyState()) return; // only on key down
            int key = org.lwjgl.input.Keyboard.getEventKey();
            if (key != bound) return;
            boolean newState = !ModConfig.enabledAutoFish;
            ModConfig.enabledAutoFish = newState;
            say("Toggle: " + (newState ? "Enabled" : "Disabled"));
            ModConfigIO.save();
            if (!newState) {
                // ensure we cleanup on manual disable
                restoreSound();
                if (autoshiftPressTicks > 0) releaseSneakIfNotPhysicallyDown();
                autoshiftPressTicks = 0;
            }
        } catch (Throwable ignored) {}
    }

    // Tick logic for throw, rethrow, and sneak holding
    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        if (mc.theWorld == null || mc.thePlayer == null) { resetState(); return; }

        boolean enabled = isModuleEnabled();

        // Maintain hookTick progression
        if (hookTick >= 0) hookTick++;

        if (!enabled) {
            // If disabled, make sure sound restored and state cleared
            restoreSound();
            // also finish any autoshift press without overriding user input
            if (autoshiftPressTicks > 0) releaseSneakIfNotPhysicallyDown();
            autoshiftPressTicks = 0;
            return;
        }

        // Sneak-hold (only if enabled via config). Do NOT force false when disabled to avoid blocking user's shift.
        if (cfgSneak()) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), true);
            holdSneakActive = true;
        } else if (holdSneakActive) {
            // Was previously holding due to cfg; stop now but preserve user's physical key if down
            releaseSneakIfNotPhysicallyDown();
            holdSneakActive = false;
        }

        // Ensure we only operate when holding a fishing rod
        if (!isHoldingRod()) {
            // If we were tapping sneak for AutoShift, stop it cleanly
            if (autoshiftPressTicks > 0) {
                releaseSneakIfNotPhysicallyDown();
                autoshiftPressTicks = 0;
            }
            autoshiftCountdownTicks = -1; // pause/reset countdown until rod held again
            return;
        }

        // AutoShift: only when enabled AND holding fishing rod; periodically tap sneak for a couple ticks with +/-3s jitter
        if (cfgAutoShift()) {
            if (autoshiftCountdownTicks < 0) autoshiftCountdownTicks = computeNextAutoShiftDelayTicks();
            if (autoshiftPressTicks > 0) {
                // keep pressed during tap
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), true);
                autoshiftPressTicks--;
                if (autoshiftPressTicks == 0) {
                    releaseSneakIfNotPhysicallyDown();
                }
            } else if (autoshiftCountdownTicks-- <= 0) {
                // start a new tap (2 ticks)
                autoshiftPressTicks = 2;
                autoshiftCountdownTicks = computeNextAutoShiftDelayTicks();
            }
        } else {
            // not enabled: ensure we are not doing taps
            if (autoshiftPressTicks > 0) {
                releaseSneakIfNotPhysicallyDown();
                autoshiftPressTicks = 0;
            }
            autoshiftCountdownTicks = -1;
        }


        // Auto throw if no hook out
        if (cfgThrowIfNoHook()) {
            if (!isHookThrown()) {
                if (hookThrownCooldown >= cfgThrowCooldownTicks()) {
                    // Maximize player sounds to hear splash reliably while active
                    boostSoundTemporarily();
                    useRod();
                    autoFishActive = true;
                    hookTick = 0;
                    hookThrownCooldown = 0;
                    if (cfgMessage()) say("Auto Fish: Threw hook");
                } else {
                    hookThrownCooldown++;
                }
            } else {
                hookThrownCooldown = 0;
                autoFishActive = true;
            }
        }

        // Rethrow on timeout
        if (cfgRethrow() && isHookThrown() && hookTick > cfgRethrowCooldownTicks()) {
            // Reel now, then throw next tick
            useRod(); // reel
            nextTickThrow = true;
            autoFishActive = false;
            hookTick = -1;
            if (cfgMessage()) say("Auto Fish: Rethrow (timeout)");
        }

        // If requested, do the throw this tick after a reel
        if (nextTickThrow && isHoldingRod()) {
            useRod(); // throw again
            autoFishActive = true;
            hookTick = 0;
            nextTickThrow = false;
            if (cfgMessage()) say("Auto Fish: Re-thrown");
        }
    }

    private int computeNextAutoShiftDelayTicks() {
        int base = cfgAutoShiftIntervalS();
        int min = Math.max(1, base - 3);
        int max = base + 3;
        int seconds = rng.nextInt(max - min + 1) + min; // inclusive range
        return seconds * 20;
    }

    private void releaseSneakIfNotPhysicallyDown() {
        try {
            int code = mc.gameSettings.keyBindSneak.getKeyCode();
            boolean physicallyDown = Keyboard.isKeyDown(code);
            if (!physicallyDown) {
                KeyBinding.setKeyBindState(code, false);
            }
        } catch (Throwable ignored) { }
    }

    // Splash sound detection to reel in
    @SubscribeEvent
    public void onSound(PlaySoundEvent event) {
        if (mc.theWorld == null || mc.thePlayer == null) return;
        if (!isModuleEnabled() || cfgSlug()) return; // slug mode ignores splash
        try {
            if (event == null || event.result == null) return;
            if (!"game.player.swim.splash".equalsIgnoreCase(event.name)) return;

            float sx = event.result.getXPosF();
            float sy = event.result.getYPosF();
            float sz = event.result.getZPosF();

            // Look for our fish hook near the splash
            AxisAlignedBB around = new AxisAlignedBB(sx - 0.25, sy - 0.25, sz - 0.25, sx + 0.25, sy + 0.25, sz + 0.25);
            List<EntityFishHook> entities = mc.theWorld.getEntitiesWithinAABB(EntityFishHook.class, around);
            for (EntityFishHook hook : entities) {
                if (hook.angler == mc.thePlayer) {
                    if (autoFishActive) {
                        // Reel and schedule re-throw
                        useRod();
                        nextTickThrow = true;
                        autoFishActive = false;
                        hookTick = -1;
                        if (cfgMessage()) say("Auto Fish: Reel on splash");
                    } else {
                        // If we weren't active, mark active and start timer
                        autoFishActive = true;
                        hookTick = 0;
                        if (cfgMessage()) say("Auto Fish: Hook detected");
                    }
                    break;
                }
            }
        } catch (Throwable ignored) { }
    }

    // Optional: if user manually right-clicks while active, reset active flag
    @SubscribeEvent
    public void onInteract(PlayerInteractEvent event) {
        try {
            if (event.action == PlayerInteractEvent.Action.RIGHT_CLICK_AIR || event.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {
                if (isHoldingRod() && autoFishActive) {
                    autoFishActive = false;
                    if (cfgMessage()) say("Auto Fish: Manual interaction");
                }
            }
        } catch (Throwable ignored) { }
    }

    // Draw simple timer HUD
    @SubscribeEvent
    public void onRender(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.TEXT) return;
        if (!isModuleEnabled() || !cfgShowTimer()) return;
        if (mc.thePlayer == null || mc.theWorld == null) return;
        int x = cfgTimerX();
        int y = cfgTimerY();
        String label = "Hook: " + (hookTick < 0 ? "--" : String.format("%.1fs", hookTick / 20.0));
        mc.fontRendererObj.drawStringWithShadow(label, x, y, 0xFFFFFFFF);
    }

    // Helpers
    private boolean isHoldingRod() {
        ItemStack held = mc.thePlayer.getHeldItem();
        return held != null && held.getItem() == Items.fishing_rod;
    }

    private boolean isHookThrown() {
        // Search within a reasonable radius for our hook
        AxisAlignedBB box = new AxisAlignedBB(
                mc.thePlayer.posX - 100, mc.thePlayer.posY - 100, mc.thePlayer.posZ - 100,
                mc.thePlayer.posX + 100, mc.thePlayer.posY + 100, mc.thePlayer.posZ + 100
        );
        List<EntityFishHook> hooks = mc.theWorld.getEntitiesWithinAABB(EntityFishHook.class, box);
        for (EntityFishHook h : hooks) if (h.angler == mc.thePlayer) return true;
        return false;
    }

    private void useRod() {
        try {
            mc.playerController.sendUseItem(mc.thePlayer, mc.theWorld, mc.thePlayer.getHeldItem());
        } catch (Throwable ignored) {}
    }

    private void say(String msg) {
        try { mc.thePlayer.addChatMessage(new ChatComponentText("§6[Auto Fish] §7" + msg)); } catch (Throwable ignored) {}
    }

    private void boostSoundTemporarily() {
        try {
            float cur = mc.gameSettings.getSoundLevel(SoundCategory.PLAYERS);
            if (cur < 0.99f) {
                oldPlayersVolume = cur;
                mc.gameSettings.setSoundLevel(SoundCategory.PLAYERS, 1.0f);
            }
        } catch (Throwable ignored) {}
    }

    private void restoreSound() {
        try {
            mc.gameSettings.setSoundLevel(SoundCategory.PLAYERS, oldPlayersVolume);
        } catch (Throwable ignored) {}
    }
}
