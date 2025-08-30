package com.aftertime.ratallofyou.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.util.StringUtils;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.HashMap;
import java.util.Locale;

public class PartyUtils {
    // Regex patterns for party messages
    private static final Pattern joinedSelf = Pattern.compile("^You have joined ((?:\\[[^]]*?])? ?)?(\\w{1,16})'s? party!$");
    private static final Pattern joinedOther = Pattern.compile("^((?:\\[[^]]*?])? ?)?(\\w{1,16}) joined the party\\.$");
    private static final Pattern leftParty = Pattern.compile("^((?:\\[[^]]*?])? ?)?(\\w{1,16}) has left the party\\.$");
    private static final Pattern kickedParty = Pattern.compile("^((?:\\[[^]]*?])? ?)?(\\w{1,16}) has been removed from the party\\.$");
    private static final Pattern kickedOffline = Pattern.compile("^Kicked ((?:\\[[^]]*?])? ?)?(\\w{1,16}) because they were offline\\.$");
    private static final Pattern kickedDisconnected = Pattern.compile("^((?:\\[[^]]*?])? ?)?(\\w{1,16}) was removed from your party because they disconnected\\.$");
    private static final Pattern transferLeave = Pattern.compile("^The party was transferred to ((?:\\[[^]]*?])? ?)?(\\w{1,16}) because ((?:\\[[^]]*?])? ?)?(\\w{1,16}) left$");
    private static final Pattern transferBy = Pattern.compile("^The party was transferred to ((?:\\[[^]]*?])? ?)?(\\w{1,16}) by ((?:\\[[^]]*?])? ?)?(\\w{1,16})$");
    private static final Pattern partyChat = Pattern.compile("^Party > ((?:\\[[^]]*?])? ?)?(\\w{1,16}): (.+)$");
    private static final Pattern partyInvite = Pattern.compile("^((?:\\[[^]]*?])? ?)?(\\w{1,16}) invited ((?:\\[[^]]*?])? ?)?(\\w{1,16}) to the party! They have 60 seconds to accept.$");
    private static final Pattern leaderDisconnected = Pattern.compile("^The party leader, ((?:\\[[^]]*?])? ?)?(\\w{1,16}) has disconnected, they have 5 minutes to rejoin before the party is disbanded\\.$");
    private static final Pattern leaderRejoined = Pattern.compile("^The party leader ((?:\\[[^]]*?])? ?)?(\\w{1,16}) has rejoined\\.$");
    private static final Pattern memberFormat = Pattern.compile("^((?:\\[[^]]*?])? ?)?(\\w{1,16})$");
    private static final Pattern partyWith = Pattern.compile("^You'll be partying with: (.+)$");
    private static final Pattern queuedInFinder = Pattern.compile("^Party Finder > Your party has been queued in the dungeon finder!$");
    private static final Pattern dungeonJoin = Pattern.compile("^Party Finder > (\\w{1,16}) joined the dungeon group! \\((\\w+) Level (\\d+)\\)$");
    private static final Pattern kuudraJoin = Pattern.compile("^Party Finder > ((?:\\[[^]]*?])? ?)?(\\w{1,16}) joined the group! \\(Combat Level (\\d+)\\)$");
    private static final Pattern membersList = Pattern.compile("^Party (Leader|Moderators|Members): (.+)$");

    private static final List<Pattern> disbandPatterns = Arrays.asList(
            Pattern.compile("^((?:\\[[^]]*?])? ?)?(\\w{1,16}) has disbanded the party!$"),
            Pattern.compile("^You have been kicked from the party by ((?:\\[[^]]*?])? ?)?(\\w{1,16})$"),
            Pattern.compile("^The party was disbanded because all invites expired and the party was empty.$"),
            Pattern.compile("^The party was disbanded because the party leader disconnected.$"),
            Pattern.compile("^You left the party.$"),
            Pattern.compile("^You are not currently in a party.$")
    );

    private static final List<String> members = new ArrayList<String>();
    private static String partyLeader = null;
    private static boolean isInParty = false;
    private static final Minecraft mc = Minecraft.getMinecraft();

    // New: track class for each member (H/M/T/A/B)
    private static final HashMap<String, String> memberClass = new HashMap<String, String>();

    public static List<String> getPartyMembers() {
        return new ArrayList<String>(members);
    }

    public static String getPartyLeader() {
        return partyLeader;
    }

    public static boolean isInParty() {
        return isInParty;
    }

    // Map class name like "Mage" to letter
    private static String classToLetter(String cls) {
        if (cls == null) return null;
        String c = cls.toLowerCase(Locale.ENGLISH);
        if (c.startsWith("healer")) return "H";
        if (c.startsWith("mage")) return "M";
        if (c.startsWith("tank")) return "T";
        if (c.startsWith("archer")) return "A";
        if (c.startsWith("bers")) return "B"; // berserk
        return null;
    }

    public static String getClassLetter(String player) {
        if (player == null) return null;
        return memberClass.get(player);
    }

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        String message = StringUtils.stripControlCodes(event.message.getUnformattedText());

        Matcher matcher;
        if ((matcher = joinedOther.matcher(message)).find()) {
            addMember(matcher.group(2));
            return;
        }

        if ((matcher = joinedSelf.matcher(message)).find()) {
            addMember(matcher.group(2));
            partyLeader = matcher.group(2);
            addMember(mc.thePlayer.getName());
            return;
        }

        if ((matcher = leftParty.matcher(message)).find()) {
            removeMember(matcher.group(2));
            return;
        }

        if ((matcher = kickedParty.matcher(message)).find()) {
            removeMember(matcher.group(2));
            return;
        }

        if ((matcher = kickedOffline.matcher(message)).find()) {
            removeMember(matcher.group(2));
            return;
        }

        if ((matcher = kickedDisconnected.matcher(message)).find()) {
            removeMember(matcher.group(2));
            return;
        }

        if ((matcher = transferBy.matcher(message)).find()) {
            addMember(matcher.group(2));
            addMember(matcher.group(4));
            partyLeader = matcher.group(2);
            return;
        }

        if ((matcher = transferLeave.matcher(message)).find()) {
            addMember(matcher.group(2));
            partyLeader = matcher.group(2);
            removeMember(matcher.group(4));
            return;
        }

        if ((matcher = leaderDisconnected.matcher(message)).find()) {
            partyLeader = matcher.group(2);
            return;
        }

        if ((matcher = leaderRejoined.matcher(message)).find()) {
            partyLeader = matcher.group(2);
            return;
        }

        if ((matcher = partyChat.matcher(message)).find()) {
            addMember(matcher.group(2));
            return;
        }

        if ((matcher = partyInvite.matcher(message)).find()) {
            addMember(matcher.group(2));
            if (partyLeader == null) partyLeader = matcher.group(2);
            return;
        }

        if (queuedInFinder.matcher(message).find()) {
            addMember(mc.thePlayer.getName());
            if (partyLeader == null) partyLeader = mc.thePlayer.getName();
            return;
        }

        for (Pattern pattern : disbandPatterns) {
            if (pattern.matcher(message).find()) {
                disband();
                return;
            }
        }

        if ((matcher = membersList.matcher(message)).find()) {
            String type = matcher.group(1);
            String[] segments = matcher.group(2).split(" ●");
            for (String segment : segments) {
                Matcher memberMatcher = memberFormat.matcher(segment.trim());
                if (memberMatcher.find()) {
                    addMember(memberMatcher.group(2));
                    if ("Leader".equals(type)) partyLeader = memberMatcher.group(2);
                }
            }
            return;
        }

        if ((matcher = partyWith.matcher(message)).find()) {
            String[] playerNames = matcher.group(1).split(", ");
            for (String playerName : playerNames) {
                Matcher memberMatcher = memberFormat.matcher(playerName.trim());
                if (memberMatcher.find()) {
                    addMember(memberMatcher.group(2));
                }
            }
            return;
        }

        if ((matcher = kuudraJoin.matcher(message)).find()) {
            addMember(matcher.group(2));
            return;
        }

        if ((matcher = dungeonJoin.matcher(message)).find()) {
            addMember(matcher.group(1));
            String letter = classToLetter(matcher.group(2));
            if (letter != null) memberClass.put(matcher.group(1), letter);
            return;
        }
    }

    private static void addMember(String playerName) {
        if (!isInParty) isInParty = true;
        if (!members.contains(playerName)) {
            members.add(playerName);
        }
    }

    private static void removeMember(String playerName) {
        if (!members.contains(playerName)) return;

        members.remove(playerName);
        memberClass.remove(playerName);

        if (members.isEmpty()) {
            disband();
        }
    }

    private static void disband() {
        members.clear();
        memberClass.clear();
        partyLeader = null;
        isInParty = false;
    }

    public static boolean isLeader() {
        return mc.thePlayer != null && mc.thePlayer.getName().equals(partyLeader);
    }
}