package com.aftertime.ratallofyou.Command;

import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;

import java.util.Collections;
import java.util.List;

public class OpenConfigGuiCommand extends CommandBase {
    @Override
    public String getCommandName() {
        return "raoy";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/raoy";
    }

    @Override
    public List<String> getCommandAliases() {
        return Collections.singletonList("rat");
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        final Minecraft mc = Minecraft.getMinecraft();
        if (mc == null) return;

        // Give quick feedback in chat so users know the command executed
        if (mc.thePlayer != null) {
            mc.thePlayer.addChatMessage(new ChatComponentText("Opening Rat All Of You config..."));
        }

        // Defer opening until next tick to avoid chat GUI closing over it
        GuiOpenScheduler.openConfigNextTick();
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true; // client-side only; always allowed
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }
}
