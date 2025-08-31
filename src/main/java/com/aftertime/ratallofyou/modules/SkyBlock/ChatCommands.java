package com.aftertime.ratallofyou.modules.SkyBlock;


import com.aftertime.ratallofyou.UI.config.ConfigData.AllConfig;
import com.aftertime.ratallofyou.UI.config.ConfigData.ModuleInfo;
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
    private static final Pattern PARTY_MSG_REGEX = Pattern.compile("^Party > (\\[[^]]*?])? ?(\\w{1,16})(?: [ቾ⚒])?: ?!(\\w+)(?: (.+))?$");

    // Command toggles
    private final Boolean warp = (Boolean) AllConfig.INSTANCE.COMMAND_CONFIGS.get("command_warp").Data;
    private final Boolean warptransfer = (Boolean) AllConfig.INSTANCE.COMMAND_CONFIGS.get("command_warp_transfer").Data;
    private final Boolean coords = (Boolean) AllConfig.INSTANCE.COMMAND_CONFIGS.get("command_coords").Data;
    private final Boolean allinvite = (Boolean) AllConfig.INSTANCE.COMMAND_CONFIGS.get("command_all_invite").Data;
    private final Boolean boop = (Boolean) AllConfig.INSTANCE.COMMAND_CONFIGS.get("command_boop").Data;
    private final Boolean cf = (Boolean) AllConfig.INSTANCE.COMMAND_CONFIGS.get("command_coin_flip").Data;
    private final Boolean eightball = (Boolean) AllConfig.INSTANCE.COMMAND_CONFIGS.get("command_8ball").Data;
    private final Boolean dice = (Boolean) AllConfig.INSTANCE.COMMAND_CONFIGS.get("command_dice").Data;
    private final Boolean pt = (Boolean) AllConfig.INSTANCE.COMMAND_CONFIGS.get("command_party_transfer").Data;
    private final Boolean tps = (Boolean) AllConfig.INSTANCE.COMMAND_CONFIGS.get("command_tps").Data;
    private final Boolean dt = (Boolean) AllConfig.INSTANCE.COMMAND_CONFIGS.get("command_downtime").Data;
    private final Boolean queInstance = (Boolean) AllConfig.INSTANCE.COMMAND_CONFIGS.get("command_queue_instance").Data;
    private final Boolean demote = (Boolean) AllConfig.INSTANCE.COMMAND_CONFIGS.get("command_demote").Data;
    private final Boolean promote = (Boolean) AllConfig.INSTANCE.COMMAND_CONFIGS.get("command_promote").Data;
    private final Boolean disband = (Boolean) AllConfig.INSTANCE.COMMAND_CONFIGS.get("command_disband").Data;
    private final Boolean ptandwarp = (Boolean) AllConfig.INSTANCE.COMMAND_CONFIGS.get("command_pt_warp").Data;

    private final List<Pair> dtReason = new ArrayList<Pair>();

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        if (!isModuleEnabled()) {
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
        switch (command) {
            case "help":
            case "h":
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
                if (ptandwarp) availableCommands.add("ptandwarp");

                runWithSelfDelay(sender, new Runnable() {
                    @Override
                    public void run() {
                        partyMessage("Available commands: " + join(availableCommands, ", "));
                    }
                });
                break;

            case "coords":
            case "co":
                if (coords) {
                    runWithSelfDelay(sender, new Runnable() {
                        @Override
                        public void run() {
                            partyMessage(getPositionString());
                        }
                    });
                }
                break;

            case "boop":
                if (boop && args != null) {
                    final String finalArgsBoop = args;
                    runWithSelfDelay(sender, new Runnable() {
                        @Override
                        public void run() {
                            sendCommand("boop " + finalArgsBoop);
                        }
                    });
                }
                break;

            case "cf":
                if (cf) {
                    runWithSelfDelay(sender, new Runnable() {
                        @Override
                        public void run() {
                            partyMessage(Math.random() < 0.5 ? "heads" : "tails");
                        }
                    });
                }
                break;

            case "8ball":
                if (eightball) {
                    runWithSelfDelay(sender, new Runnable() {
                        @Override
                        public void run() {
                            partyMessage(getRandomResponse());
                        }
                    });
                }
                break;

            case "dice":
                if (dice) {
                    runWithSelfDelay(sender, new Runnable() {
                        @Override
                        public void run() {
                            partyMessage(String.valueOf(new Random().nextInt(6) + 1));
                        }
                    });
                }
                break;

            case "tps":
                if (tps) {
                    runWithSelfDelay(sender, new Runnable() {
                        @Override
                        public void run() {
                            partyMessage("TPS: " + String.format("%.1f", getAverageTps()));
                        }
                    });
                }
                break;

            case "warp":
            case "w":
                if (warp && PartyUtils.isLeader()) {
                    runWithSelfDelay(sender, new Runnable() {
                        @Override
                        public void run() {
                            sendCommand("p warp");
                        }
                    });
                }
                break; // prevent fall-through into warptransfer

            case "warptransfer":
            case "wt":
                if (warptransfer && PartyUtils.isLeader()) {
                    final String finalSenderWT = sender;
                    runWithSelfDelay(sender, new Runnable() {
                        @Override
                        public void run() {
                            sendCommand("p warp");
                            new Timer().schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    sendCommand("p transfer " + finalSenderWT);
                                }
                            }, 12000);
                        }
                    });
                }
                break;

            case "allinvite":
            case "allinv":
                if (allinvite && PartyUtils.isLeader()) {
                    runWithSelfDelay(sender, new Runnable() {
                        @Override
                        public void run() {
                            sendCommand("p settings allinvite");
                        }
                    });
                }
                break;

            case "pt":
            case "ptme":
            case "transfer":
                if (pt && PartyUtils.isLeader()) {
                    String target = args != null ? findPartyMember(args) : sender;
                    if (target == null) target = sender;
                    final String finalTargetPt = target;
                    runWithSelfDelay(sender, new Runnable() {
                        @Override
                        public void run() {
                            sendCommand("p transfer " + finalTargetPt);
                        }
                    });
                }
                break;

            case "downtime":
            case "dt":
                if (dt) {
                    final String finalArgsDt = args;
                    runWithSelfDelay(sender, new Runnable() {
                        @Override
                        public void run() {
                            String reason = finalArgsDt != null ? finalArgsDt : "No reason given";
                            for (Pair pair : dtReason) {
                                if (pair.getFirst().equals(sender)) {
                                    modMessage(EnumChatFormatting.GOLD + sender + EnumChatFormatting.RED + " already has a reminder!");
                                    return;
                                }
                            }
                            modMessage(EnumChatFormatting.GREEN + "Reminder set for the end of the run!");
                            dtReason.add(new Pair(sender, reason));
                        }
                    });
                }
                break;

            case "undowntime":
            case "undt":
                if (dt) {
                    runWithSelfDelay(sender, new Runnable() {
                        @Override
                        public void run() {
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
                    });
                }
                break;

            case "demote":
                if (demote && PartyUtils.isLeader() && args != null) {
                    String target = findPartyMember(args);
                    if (target != null) {
                        final String finalTargetDemote = target;
                        runWithSelfDelay(sender, new Runnable() {
                            @Override
                            public void run() {
                                sendCommand("p demote " + finalTargetDemote);
                            }
                        });
                    }
                }
                break;

            case "promote":
                if (promote && PartyUtils.isLeader() && args != null) {
                    String target = findPartyMember(args);
                    if (target != null) {
                        final String finalTargetPromote = target;
                        runWithSelfDelay(sender, new Runnable() {
                            @Override
                            public void run() {
                                sendCommand("p promote " + finalTargetPromote);
                            }
                        });
                    }
                }
                break;

            case "disband":
                if (disband && PartyUtils.isLeader()) {
                    runWithSelfDelay(sender, new Runnable() {
                        @Override
                        public void run() {
                            sendCommand("p disband");
                        }
                    });
                }
                break;

            case "ptw":
            case "tw":
                // Null check Minecraft and player
                if (mc == null || mc.thePlayer == null || !ptandwarp) {
                    return;
                }

                // Only ignore if someone else sends without args
                if (args == null && !sender.equalsIgnoreCase(mc.thePlayer.getName())) {
                    return;
                }

                final String finalArgsPtw = args;
                Runnable processPtw = new Runnable() {
                    @Override
                    public void run() {
                        if (finalArgsPtw != null) {
                            String targetInput = finalArgsPtw.toLowerCase();
                            String myName = mc.thePlayer.getName().toLowerCase();

                            if (targetInput.equals(myName)) {
                                partyMessage("!ptme");

                                new Timer().schedule(new TimerTask() {
                                    @Override
                                    public void run() {
                                        if (mc != null && mc.thePlayer != null) {
                                            sendCommand("p warp");
                                        }
                                    }
                                }, 2000);
                            } else {
                                List<String> partyMembers = PartyUtils.getPartyMembers();
                                String foundMember = null;

                                if (partyMembers != null) {
                                    for (String member : partyMembers) {
                                        if (member != null && (member.toLowerCase().equals(targetInput) ||
                                                member.toLowerCase().startsWith(targetInput))) {
                                            foundMember = member;
                                            break;
                                        }
                                    }
                                }

                                if (foundMember != null && PartyUtils.isLeader()) {
                                    sendCommand("p transfer " + foundMember);

                                    new Timer().schedule(new TimerTask() {
                                        @Override
                                        public void run() {
                                            if (mc != null && mc.thePlayer != null) {
                                                partyMessage("!warp");
                                            }
                                        }
                                    }, 2000);
                                } else if (foundMember == null) {
                                    partyMessage("Player not found in party: " + finalArgsPtw);
                                }
                            }
                        } else {
                            if (PartyUtils.isLeader()) {
                                sendCommand("p warp");
                            }
                        }
                    }
                };

                runWithSelfDelay(sender, processPtw);
                break;

            default:
                // Handle regex patterns for floor commands
                if (command.matches("^[mf][1-7]$")) {
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

                        final String finalDungeonCommand = dungeonCommand;
                        runWithSelfDelay(sender, new Runnable() {
                            @Override
                            public void run() {
                                sendCommand(finalDungeonCommand);
                            }
                        });
                    }
                } else if (command.matches("^t[1-5]$")) {
                    if (queInstance && PartyUtils.isLeader()) {
                        String[] kuudraTiers = {
                                "kuudra_normal",
                                "kuudra_hot",
                                "kuudra_burning",
                                "kuudra_fiery",
                                "kuudra_infernal"
                        };

                        int tierIndex = Integer.parseInt(command.substring(1)) - 1;
                        final String finalJoinInstance = "joininstance " + kuudraTiers[tierIndex];
                        runWithSelfDelay(sender, new Runnable() {
                            @Override
                            public void run() {
                                sendCommand(finalJoinInstance);
                            }
                        });
                    }
                }
                break;
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
        return String.format("x: %.1f, y: %.1f, z: %.1f",
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

    private boolean isModuleEnabled() {
        ModuleInfo cfg = (ModuleInfo) AllConfig.INSTANCE.MODULES.get("skyblock_partycommands");
        return cfg != null && Boolean.TRUE.equals(cfg.Data);
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

    // Add helpers to conditionally delay when the sender is the local player
    private boolean isSelf(String sender) {
        return mc != null && mc.thePlayer != null && sender != null && sender.equalsIgnoreCase(mc.thePlayer.getName());
    }

    private void runWithSelfDelay(String sender, Runnable action) {
        if (isSelf(sender)) {
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    action.run();
                }
            }, 1000);
        } else {
            action.run();
        }
    }
}
