package com.aftertime.ratallofyou.modules.SkyBlock;

import com.aftertime.ratallofyou.UI.ModConfig;
import com.aftertime.ratallofyou.utils.PartyUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StringUtils;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class ChatCommands {
    private static final Minecraft mc = Minecraft.getMinecraft();
    private static final String MODULE_NAME = "Party Commands";
    private static final Pattern PARTY_MSG_REGEX = Pattern.compile("^Party > (\\[[^]]*?])? ?(\\w{1,16})(?: [ቾ⚒])?: ?!(\\w+)(?: (.+))?$");

    // Command toggles
    private boolean warp = isCommandEnabled("warp");
    private boolean warptransfer = isCommandEnabled("warptransfer");
    private boolean coords = isCommandEnabled("coords");
    private boolean allinvite = isCommandEnabled("allinvite");
    private boolean boop = isCommandEnabled("boop");
    private boolean kick = isCommandEnabled("kick");
    private boolean cf = isCommandEnabled("cf");
    private boolean eightball = isCommandEnabled("8ball");
    private boolean dice = isCommandEnabled("dice");
    private boolean pt = isCommandEnabled("pt");
    private boolean tps = isCommandEnabled("tps");
    private boolean dt = isCommandEnabled("dt");
    private boolean queInstance = isCommandEnabled("queInstance");
    private boolean demote = isCommandEnabled("demote");
    private boolean promote = isCommandEnabled("promote");
    private boolean disband = isCommandEnabled("disband");

    private final List<Pair> dtReason = new ArrayList<Pair>();
    private boolean isEnabled = false;

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        if (!isEnabled("Party Commands")) {
            return;
        }

        final String message = StringUtils.stripControlCodes(event.message.getUnformattedText());

        // Handle DT messages
        if (message.contains("> EXTRA STATS <") || message.contains("[NPC] Elle: Good job everyone")) {
            if (!dt || dtReason.isEmpty()) return;

            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    for (Pair pair : dtReason) {
                        if (pair.getFirst().equals(mc.thePlayer.getName())) {
                            partyMessage("Downtime needed: " + pair.getSecond());
                        }
                    }

                    // Group reasons
                    Map<String, List<String>> reasonMap = new HashMap<String, List<String>>();
                    for (Pair pair : dtReason) {
                        String reason = (String) pair.getSecond();
                        if (!reasonMap.containsKey(reason)) {
                            reasonMap.put(reason, new ArrayList<String>());
                        }
                        reasonMap.get(reason).add((String) pair.getFirst());
                    }

                    // Build message
                    StringBuilder sb = new StringBuilder("DT Reasons: ");
                    for (Map.Entry<String, List<String>> entry : reasonMap.entrySet()) {
                        sb.append(join(entry.getValue(), ", "))
                                .append(": ")
                                .append(entry.getKey())
                                .append(", ");
                    }

                    dtReason.clear();
                }
            }, 30000);
            return;
        }

        Matcher matcher = PARTY_MSG_REGEX.matcher(message);
        if (!matcher.find()) return;

        final String ign = matcher.group(2);
        final String command = matcher.group(3).toLowerCase();
        final String args = matcher.group(4);

        if (ign == null || command == null) return;

        handleCommand(command, args, ign);
    }

    private void handleCommand(String command, String args, String sender) {
        warp = isCommandEnabled("warp");
        warptransfer = isCommandEnabled("warptransfer");
        coords = isCommandEnabled("coords");
        allinvite = isCommandEnabled("allinvite");
        boop = isCommandEnabled("boop");
        kick = isCommandEnabled("kick");
        cf = isCommandEnabled("cf");
        eightball = isCommandEnabled("8ball");
        dice = isCommandEnabled("dice");
        pt = isCommandEnabled("pt");
        tps = isCommandEnabled("tps");
        dt = isCommandEnabled("dt");
        queInstance = isCommandEnabled("queInstance");
        demote = isCommandEnabled("demote");
        promote = isCommandEnabled("promote");
        disband = isCommandEnabled("disband");

        if (command.equals("help") || command.equals("h")) {
            List<String> availableCommands = new ArrayList<String>();
            if (coords) availableCommands.add("coords/co");
            if (boop) availableCommands.add("boop [player]");
            if (cf) availableCommands.add("cf");
            if (eightball) availableCommands.add("8ball");
            if (dice) availableCommands.add("dice");
            if (tps) availableCommands.add("tps");
            if (warp && PartyUtils.isLeader()) availableCommands.add("warp/w");
            if (warptransfer && PartyUtils.isLeader()) availableCommands.add("warptransfer/wt");
            if (allinvite && PartyUtils.isLeader()) availableCommands.add("allinvite/allinv");
            if (pt && PartyUtils.isLeader()) availableCommands.add("pt/ptme/transfer [player]");
            if (dt) availableCommands.add("dt/downtime [reason]");
            if (queInstance && PartyUtils.isLeader()) availableCommands.add("m1-m7/f1-f7/t1-t5");
            if (demote && PartyUtils.isLeader()) availableCommands.add("demote [player]");
            if (promote && PartyUtils.isLeader()) availableCommands.add("promote [player]");
            if (disband && PartyUtils.isLeader()) availableCommands.add("disband");

            partyMessage("Available commands: " + join(availableCommands, ", "));
        }
        else if (command.equals("coords") || command.equals("co")) {
            if (coords) {
                partyMessage(getPositionString());
            }
        }
        else if (command.equals("boop")) {
            if (boop && args != null) {
                sendCommand("boop " + args);
            }
        }
        else if (command.equals("cf")) {
            if (cf) {
                partyMessage(Math.random() < 0.5 ? "heads" : "tails");
            }
        }
        else if (command.equals("8ball")) {
            if (eightball) {
                partyMessage(getRandomResponse());
            }
        }
        else if (command.equals("dice")) {
            if (dice) {
                partyMessage(String.valueOf(new Random().nextInt(6) + 1));
            }
        }
        else if (command.equals("tps")) {
            if (tps) {
                partyMessage("TPS: " + String.format("%.1f", getAverageTps()));
            }
        }
        else if (command.equals("warp") || command.equals("w")) {
            if (warp && PartyUtils.isLeader()) {
                sendCommand("p warp");
            }
        }
        else if (command.equals("warptransfer") || command.equals("wt")) {
            if (warptransfer && PartyUtils.isLeader()) {
                sendCommand("p warp");
                final String finalSender = sender;
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        sendCommand("p transfer " + finalSender);
                    }
                }, 12000);
            }
        }
        else if (command.equals("allinvite") || command.equals("allinv")) {
            if (allinvite && PartyUtils.isLeader()) {
                sendCommand("p settings allinvite");
            }
        }
        else if (command.equals("pt") || command.equals("ptme") || command.equals("transfer")) {
            if (pt && PartyUtils.isLeader()) {
                String target = args != null ? findPartyMember(args) : sender;
                if (target == null) target = sender;
                sendCommand("p transfer " + target);
            }
        }
        else if (command.equals("downtime") || command.equals("dt")) {
            if (dt) {
                String reason = args != null ? args : "No reason given";
                for (Pair pair : dtReason) {
                    if (pair.getFirst().equals(sender)) {
                        modMessage(EnumChatFormatting.GOLD + sender + EnumChatFormatting.RED + " already has a reminder!");
                        return;
                    }
                }
                modMessage(EnumChatFormatting.GREEN + "Reminder set for the end of the run!");
                dtReason.add(new Pair(sender, reason));
            }
        }
        else if (command.equals("undowntime") || command.equals("undt")) {
            if (dt) {
                boolean removed = false;
                Iterator<Pair> iterator = dtReason.iterator();
                while (iterator.hasNext()) {
                    Pair pair = iterator.next();
                    if (pair.getFirst().equals(sender)) {
                        iterator.remove();
                        removed = true;
                    }
                }
                if (removed) {
                    modMessage(EnumChatFormatting.GREEN + "Reminder removed!");
                } else {
                    modMessage(EnumChatFormatting.GOLD + sender + EnumChatFormatting.RED + " has no reminder set!");
                }
            }
        }
        else if (command.matches("^[mf][1-7]$")) {
            if (queInstance && PartyUtils.isLeader()) {
                String floorType = command.substring(0, 1);
                String floorNum = command.substring(1);

                String[] numberWords = {"", "one", "two", "three", "four",
                        "five", "six", "seven"};
                String floorWord = numberWords[Integer.parseInt(floorNum)];

                String dungeonCommand;
                if (floorType.equals("m")) {
                    dungeonCommand = "joindungeon master_catacombs_floor_" + floorWord;
                } else {
                    dungeonCommand = "joindungeon catacombs_floor_" + floorWord;
                }

                sendCommand(dungeonCommand);
            }
        }
        else if (command.matches("^t[1-5]$")) {
            if (queInstance && PartyUtils.isLeader()) {
                String[] kuudraTiers = {
                        "kuudra_normal",
                        "kuudra_hot",
                        "kuudra_burning",
                        "kuudra_fiery",
                        "kuudra_infernal"
                };

                int tierIndex = Integer.parseInt(command.substring(1)) - 1;
                sendCommand("joininstance " + kuudraTiers[tierIndex]);
            }
        }
        else if (command.equals("demote")) {
            if (demote && PartyUtils.isLeader() && args != null) {
                String target = findPartyMember(args);
                if (target != null) {
                    sendCommand("p demote " + target);
                }
            }
        }
        else if (command.equals("promote")) {
            if (promote && PartyUtils.isLeader() && args != null) {
                String target = findPartyMember(args);
                if (target != null) {
                    sendCommand("p promote " + target);
                }
            }
        }
        else if (command.equals("disband")) {
            if (disband && PartyUtils.isLeader()) {
                sendCommand("p disband");
            }
        }
        else {
            partyMessage("Unknown command. Use !help for available commands.");
        }
    }

    private String findPartyMember(String partialName) {
        for (String member : PartyUtils.getPartyMembers()) {
            if (member.toLowerCase().startsWith(partialName.toLowerCase())) {
                return member;
            }
        }
        return null;
    }

    private void partyMessage(String message) {
        sendCommand("pc " + message);
    }

    private void modMessage(String message) {
        mc.thePlayer.addChatMessage(new ChatComponentText(message));
    }

    private void sendCommand(String command) {
        mc.thePlayer.sendChatMessage("/" + command);
    }

    private String getPositionString() {
        return String.format("X: %.1f, Y: %.1f, Z: %.1f",
                mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
    }

    private String getRandomResponse() {
        String[] responses = {
                "It is certain", "It is decidedly so", "Without a doubt", "Yes definitely",
                "You may rely on it", "As I see it, yes", "Most likely", "Outlook good",
                "Yes", "Signs point to yes", "Reply hazy try again", "Ask again later",
                "Better not tell you now", "Cannot predict now", "Concentrate and ask again",
                "Don't count on it", "My reply is no", "My sources say no", "Outlook not so good",
                "Very doubtful"
        };
        return responses[new Random().nextInt(responses.length)];
    }

    private float getAverageTps() {
        return 20.0f;
    }

    private String join(List<String> list, String delimiter) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            sb.append(list.get(i));
            if (i < list.size() - 1) {
                sb.append(delimiter);
            }
        }
        return sb.toString();
    }

    public static class Pair {
        private final Object first;
        private final Object second;

        public Pair(Object first, Object second) {
            this.first = first;
            this.second = second;
        }

        public Object getFirst() {
            return first;
        }

        public Object getSecond() {
            return second;
        }
    }

    public static boolean isEnabled(String moduleName) {
        for (ModConfig.ModuleInfo module : ModConfig.MODULES) {
            if (module.name.equals(MODULE_NAME)) {
                return module.enabled;
            }
        }
        return false;
    }

    public static void setCommandEnabled(String commandName, boolean enabled) {
        Properties props = new Properties();
        File configFile = new File("config/ratallofyou_commands.cfg");

        try {
            if (configFile.exists()) {
                props.load(new FileInputStream(configFile));
            }

            props.setProperty(commandName, String.valueOf(enabled));
            props.store(new FileOutputStream(configFile), "Rat All Of You Command Settings");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean isCommandEnabled(String commandName) {
        Properties props = new Properties();
        File configFile = new File("config/ratallofyou_commands.cfg");

        try {
            if (configFile.exists()) {
                props.load(new FileInputStream(configFile));
                return Boolean.parseBoolean(props.getProperty(commandName, "true"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if ("pt".equals(commandName)) {
            return false;
        } else if ("demote".equals(commandName)) {
            return false;
        } else if ("promote".equals(commandName)) {
            return false;
        } else if ("disband".equals(commandName)) {
            return false;
        } else {
            return true;
        }
    }
}