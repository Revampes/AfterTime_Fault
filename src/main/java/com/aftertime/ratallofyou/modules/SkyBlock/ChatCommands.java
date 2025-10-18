package com.aftertime.ratallofyou.modules.SkyBlock;


import com.aftertime.ratallofyou.config.ModConfig;
import com.aftertime.ratallofyou.utils.PartyUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StringUtils;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class ChatCommands {
    private static final Minecraft mc = Minecraft.getMinecraft();
    private static final Pattern PARTY_MSG_REGEX = Pattern.compile("^Party > (\\[[^]]*?])? ?(\\w{1,16})(?: [\u007e\u2692])?: ?!(\\w+)(?: (.+))?$");

    // Dynamic toggles (refreshed on each message)
    private boolean warp, warptransfer, coords, allinvite, boop, cf, eightball, dice, pt, tps, dt, queInstance, demote, promote, disband, ptandwarp;

    private void refreshToggles() {
        this.warp = ModConfig.chatCmdWarp;
        this.warptransfer = ModConfig.chatCmdWarpTransfer;
        this.coords = ModConfig.chatCmdCoords;
        this.allinvite = ModConfig.chatCmdAllInvite;
        this.boop = ModConfig.chatCmdBoop;
        this.cf = ModConfig.chatCmdCoinFlip;
        this.eightball = ModConfig.chatCmd8Ball;
        this.dice = ModConfig.chatCmdDice;
        this.pt = ModConfig.chatCmdPartyTransfer;
        this.tps = ModConfig.chatCmdTps;
        this.dt = ModConfig.chatCmdDowntime;
        this.queInstance = ModConfig.chatCmdQueueInstance;
        this.demote = ModConfig.chatCmdDemote;
        this.promote = ModConfig.chatCmdPromote;
        this.disband = ModConfig.chatCmdDisband;
        this.ptandwarp = ModConfig.chatCmdPtAndWarp;
    }

    private final List<Pair> dtReason = new ArrayList<Pair>();

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        if (!isModuleEnabled()) {
            return;
        }
        refreshToggles();

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
                refreshToggles();
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
                if (ModConfig.chatCmdCoords) {
                    runWithSelfDelay(sender, new Runnable() {
                        @Override
                        public void run() {
                            partyMessage(getPositionString());
                        }
                    });
                }
                break;

            case "boop":
                if (ModConfig.chatCmdBoop && args != null) {
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
                if (ModConfig.chatCmdCoinFlip) {
                    runWithSelfDelay(sender, new Runnable() {
                        @Override
                        public void run() {
                            partyMessage(Math.random() < 0.5 ? "heads" : "tails");
                        }
                    });
                }
                break;

            case "8ball":
                if (ModConfig.chatCmd8Ball) {
                    runWithSelfDelay(sender, new Runnable() {
                        @Override
                        public void run() {
                            partyMessage(getRandomResponse());
                        }
                    });
                }
                break;

            case "dice":
                if (ModConfig.chatCmdDice) {
                    runWithSelfDelay(sender, new Runnable() {
                        @Override
                        public void run() {
                            partyMessage(String.valueOf(new Random().nextInt(6) + 1));
                        }
                    });
                }
                break;

            case "tps":
                if (ModConfig.chatCmdTps) {
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
                if (ModConfig.chatCmdWarp && PartyUtils.isLeader()) {
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
                if (ModConfig.chatCmdWarpTransfer && PartyUtils.isLeader()) {
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
                if (ModConfig.chatCmdAllInvite && PartyUtils.isLeader()) {
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
                if (ModConfig.chatCmdPartyTransfer && PartyUtils.isLeader()) {
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
                if (ModConfig.chatCmdDowntime) {
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
                if (ModConfig.chatCmdDowntime) {
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
                if (ModConfig.chatCmdDemote && PartyUtils.isLeader() && args != null) {
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
                if (ModConfig.chatCmdPromote && PartyUtils.isLeader() && args != null) {
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
                if (ModConfig.chatCmdDisband && PartyUtils.isLeader()) {
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
                if (mc == null || mc.thePlayer == null || !ModConfig.chatCmdPtAndWarp) {
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
                                    modMessage(EnumChatFormatting.RED + "Couldn't find player to transfer to!");
                                }
                            }
                        } else {
                            // No args -> transfer to self then warp
                            partyMessage("!ptme");
                            new Timer().schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    if (mc != null && mc.thePlayer != null) {
                                        sendCommand("p warp");
                                    }
                                }
                            }, 2000);
                        }
                    }
                };
                runWithSelfDelay(sender, processPtw);
                break;

            // Queue Instance Commands
            case "f1":
            case "f2":
            case "f3":
            case "f4":
            case "f5":
            case "f6":
            case "f7":
                if (ModConfig.chatCmdQueueInstance && PartyUtils.isLeader()) {
                    final String floor = command.substring(1); // Extract number (1-7)
                    runWithSelfDelay(sender, new Runnable() {
                        @Override
                        public void run() {
                            sendCommand("joindungeon catacombs " + floor);
                        }
                    });
                }
                break;

            case "m1":
            case "m2":
            case "m3":
            case "m4":
            case "m5":
            case "m6":
            case "m7":
                if (ModConfig.chatCmdQueueInstance && PartyUtils.isLeader()) {
                    final String floor = command.substring(1); // Extract number (1-7)
                    runWithSelfDelay(sender, new Runnable() {
                        @Override
                        public void run() {
                            sendCommand("joindungeon master_catacombs " + floor);
                        }
                    });
                }
                break;

            case "t1":
                if (ModConfig.chatCmdQueueInstance && PartyUtils.isLeader()) {
                    runWithSelfDelay(sender, new Runnable() {
                        @Override
                        public void run() {
                            sendCommand("joindungeon kuudra_normal");
                        }
                    });
                }
                break;

            case "t2":
                if (ModConfig.chatCmdQueueInstance && PartyUtils.isLeader()) {
                    runWithSelfDelay(sender, new Runnable() {
                        @Override
                        public void run() {
                            sendCommand("joindungeon kuudra_hot");
                        }
                    });
                }
                break;

            case "t3":
                if (ModConfig.chatCmdQueueInstance && PartyUtils.isLeader()) {
                    runWithSelfDelay(sender, new Runnable() {
                        @Override
                        public void run() {
                            sendCommand("joindungeon kuudra_burning");
                        }
                    });
                }
                break;

            case "t4":
                if (ModConfig.chatCmdQueueInstance && PartyUtils.isLeader()) {
                    runWithSelfDelay(sender, new Runnable() {
                        @Override
                        public void run() {
                            sendCommand("joindungeon kuudra_fiery");
                        }
                    });
                }
                break;

            case "t5":
                if (ModConfig.chatCmdQueueInstance && PartyUtils.isLeader()) {
                    runWithSelfDelay(sender, new Runnable() {
                        @Override
                        public void run() {
                            sendCommand("joindungeon kuudra_infernal");
                        }
                    });
                }
                break;
        }
    }

    private boolean isModuleEnabled() {
        return ModConfig.enableChatCommands;
    }

    private int getAverageTps() {
        return 20;
    }

    // Helpers
    private void runWithSelfDelay(String sender, Runnable action) {
        if (mc == null || mc.thePlayer == null) return;
        long delay = sender.equalsIgnoreCase(mc.thePlayer.getName()) ? 0L : 350L;
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                try { action.run(); } catch (Throwable ignored) {}
            }
        }, delay);
    }

    private void modMessage(String msg) {
        if (mc != null && mc.thePlayer != null) {
            mc.thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.GOLD + "[RatAllOfYou] " + msg));
        }
    }

    private void partyMessage(String msg) { sendCommand("pc " + msg); }

    private void sendCommand(String cmd) {
        if (mc != null && mc.thePlayer != null) mc.thePlayer.sendChatMessage("/" + cmd);
    }

    private String getPositionString() {
        if (mc == null || mc.thePlayer == null) return "x: 0, y: 0, z: 0";
        int x = (int) Math.floor(mc.thePlayer.posX);
        int y = (int) Math.floor(mc.thePlayer.posY);
        int z = (int) Math.floor(mc.thePlayer.posZ);
        return "x: " + x + ", y: " + y + ", z: " + z;
    }

    private String join(List<String> list, String sep) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(sep);
            sb.append(list.get(i));
        }
        return sb.toString();
    }

    private String getRandomResponse() {
        String[] responses = {
                "It is certain.",
                "Ask again later.",
                "Better not tell you now.",
                "Very doubtful.",
                "Outlook good."
        };
        return responses[new Random().nextInt(responses.length)];
    }

    // Resolve a party member by exact or prefix match (case-insensitive); returns null if not found
    private String findPartyMember(String input) {
        if (input == null) return null;
        String query = input.trim();
        if (query.isEmpty()) return null;
        String q = query.toLowerCase(java.util.Locale.ROOT);
        java.util.List<String> party = PartyUtils.getPartyMembers();
        if (party == null || party.isEmpty()) return null;
        String exact = null;
        String prefix = null;
        for (String m : party) {
            if (m == null || m.isEmpty()) continue;
            String ml = m.toLowerCase(java.util.Locale.ROOT);
            if (ml.equals(q)) { exact = m; break; }
            if (prefix == null && ml.startsWith(q)) prefix = m;
        }
        if (exact != null) return exact;
        if (prefix != null) return prefix;
        return null;
    }

    // Simple pair holder
    private static class Pair {
        private final Object first; private final Object second;
        Pair(Object a, Object b) { first = a; second = b; }
        public Object getFirst() { return first; }
        public Object getSecond() { return second; }
    }
}
