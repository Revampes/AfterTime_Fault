package com.aftertime.ratallofyou.Command;

import com.aftertime.ratallofyou.UI.config.ModSettingsGui;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;

import java.util.Collections;
import java.util.List;

public class OpenConfigGuiCommandRat extends CommandBase {
    @Override
    public String getCommandName() { return "rat"; }

    @Override
    public String getCommandUsage(ICommandSender sender) { return "/rat"; }

    @Override
    public List<String> getCommandAliases() { return Collections.singletonList("raoy"); }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        final Minecraft mc = Minecraft.getMinecraft(); if (mc == null) return;
        if (mc.thePlayer != null) mc.thePlayer.addChatMessage(new ChatComponentText("Opening Rat All Of You config..."));
        // Defer opening until next tick to avoid chat GUI closing over it
        GuiOpenScheduler.openConfigNextTick();
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) { return true; }

    @Override
    public int getRequiredPermissionLevel() { return 0; }
}
