package com.aftertime.ratallofyou.modules.dungeon.CustomLeapMenu;

import com.aftertime.ratallofyou.config.ModConfig;
import com.aftertime.ratallofyou.utils.DungeonUtils;
import com.aftertime.ratallofyou.utils.PartyUtils;
import com.aftertime.ratallofyou.utils.Utils;
import com.aftertime.ratallofyou.utils.PlayerUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.util.*;
import java.util.regex.Pattern;

public class LeapMenu {
    private final Minecraft mc = Minecraft.getMinecraft();

    private int windowId = -1;
    private final Map<String, Integer> nameToSlot = new HashMap<>();
    private final List<String> orderedNames = new ArrayList<>();
    private final Map<String, String> nameToClass = new HashMap<>();
    // Cache parsed from scoreboard each tick
    private final Map<String, String> classesFromSidebar = new HashMap<>();
    private int parsedTick = -1;

    private final LeapMenuGui gui = new LeapMenuGui();

    private static final Pattern MC_USERNAME = Pattern.compile("^[A-Za-z0-9_]{1,16}$");

    public LeapMenu() { MinecraftForge.EVENT_BUS.register(this); }

    private static boolean isEnabled() { return ModConfig.enableCustomLeapMenu; }

    private static boolean isSpiritLeapOpen() {
        if (!isEnabled()) return false;
        if (DungeonUtils.isopenspiritleap()) return true;
        return false;
    }

    private static boolean isActive() { return isEnabled() && isSpiritLeapOpen(); }

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent e) { if (e.gui == null) clear(); }

    @SubscribeEvent
    public void onDrawPre(GuiScreenEvent.DrawScreenEvent.Pre e) {
        if (!isActive()) return;
        e.setCanceled(true);
        if (mc.thePlayer != null && parsedTick != mc.thePlayer.ticksExisted) {
            parsedTick = mc.thePlayer.ticksExisted;
            buildMappingFromContainer();
        }
        int w = e.gui.width, h = e.gui.height;
        gui.drawMenu(w, h, e.mouseX, e.mouseY, orderedNames, nameToClass);
    }

    @SubscribeEvent
    public void onMouse(GuiScreenEvent.MouseInputEvent.Pre e) {
        if (!isActive()) return;
        if (!Mouse.getEventButtonState()) return;
        int btn = Mouse.getEventButton();
        if (btn != 0 && btn != 1) return;
        int mx = Mouse.getEventX() * e.gui.width / mc.displayWidth;
        int my = e.gui.height - Mouse.getEventY() * e.gui.height / mc.displayHeight - 1;
        int idx = gui.getHoveredRegion(mx, my, 4); // fixed 4 regions
        if (idx >= 0 && idx < orderedNames.size()) {
            String name = orderedNames.get(idx);
            Integer slot = nameToSlot.get(name);
            if (slot != null) clickSlot(slot);
        }
        e.setCanceled(true);
    }

    @SubscribeEvent
    public void onKeyboard(GuiScreenEvent.KeyboardInputEvent.Pre e) {
        if (!isActive()) return;
        if (!Keyboard.getEventKeyState()) return;
        int key = Keyboard.getEventKey();
        // Limit to keys 1..4 only
        if (key >= Keyboard.KEY_1 && key <= Keyboard.KEY_4) {
            int idx = key - Keyboard.KEY_1;
            if (idx < orderedNames.size()) {
                String name = orderedNames.get(idx);
                Integer slot = nameToSlot.get(name);
                if (slot != null) { clickSlot(slot); e.setCanceled(true); }
            }
        }
    }


    private void clickSlot(int slotId) {
        if (mc.thePlayer == null || mc.playerController == null) return;
        Container c = mc.thePlayer.openContainer;
        if (c == null) return;
        windowId = c.windowId;
        try { mc.playerController.windowClick(windowId, slotId, 0, 0, mc.thePlayer); } catch (Throwable ignored) {}
    }

    private void buildMappingFromContainer() {
        nameToSlot.clear();
        orderedNames.clear();
        nameToClass.clear();

        // Get class information from PlayerUtils (new method)
        Map<String, String> tabClasses = PlayerUtils.getDungeonClasses();
        if (tabClasses == null || tabClasses.isEmpty()) {
            // Fall back to older method if PlayerUtils returns nothing
            tabClasses = Utils.getDungeonClassesFromTab();
        }

        // Get player names from Tab list first (preferred source)
        List<String> tabPlayers = Utils.getPlayerNamesFromTab();

        // Fall back to scoreboard if needed
        scanSidebarClasses();

        if (mc.thePlayer == null) return;
        if (!(mc.thePlayer.openContainer instanceof ContainerChest)) return;

        ContainerChest chest = (ContainerChest) mc.thePlayer.openContainer;
        List<Slot> slots = chest.inventorySlots;
        int chestInvSize = 27;
        try { IInventory inv = chest.getLowerChestInventory(); if (inv != null) chestInvSize = Math.min(inv.getSizeInventory(), slots.size()); } catch (Throwable ignored) {}

        // Get party members - prefer Tab list, fall back to PartyUtils
        List<String> party = !tabPlayers.isEmpty() ? tabPlayers : PartyUtils.getPartyMembers();
        Set<String> partyLower = new HashSet<>();
        for (String p : party) partyLower.add(p.toLowerCase(Locale.ENGLISH));

        for (int i = 0; i < Math.min(chestInvSize, slots.size()); i++) {
            Slot s = slots.get(i);
            if (s == null) continue;
            ItemStack st = s.getStack();
            if (st == null) continue;
            String mapped = null;
            // SkullOwner.Name preferred
            try {
                NBTTagCompound tag = st.getTagCompound();
                if (tag != null && tag.hasKey("SkullOwner", 10)) {
                    NBTTagCompound skull = tag.getCompoundTag("SkullOwner");
                    if (skull != null && skull.hasKey("Name", 8)) {
                        String skullName = net.minecraft.util.StringUtils.stripControlCodes(skull.getString("Name")).trim();
                        if (!skullName.isEmpty() && MC_USERNAME.matcher(skullName).matches()) mapped = skullName;
                    }
                }
            } catch (Throwable ignored) {}
            // Fallback to displayName last token
            if (mapped == null) {
                String dn = safeDisplayName(st);
                if (dn != null && !dn.isEmpty()) {
                    String plain = net.minecraft.util.StringUtils.stripControlCodes(dn).trim();
                    if (!partyLower.isEmpty() && partyLower.contains(plain.toLowerCase(Locale.ENGLISH))) mapped = plain;
                    if (mapped == null && MC_USERNAME.matcher(plain).matches()) mapped = plain;
                    if (mapped == null) {
                        String[] toks = plain.split(" ");
                        if (toks.length > 0) {
                            String cand = toks[toks.length - 1];
                            if (MC_USERNAME.matcher(cand).matches()) mapped = cand;
                        }
                    }
                }
            }
            if (mapped != null && !nameToSlot.containsKey(mapped)) {
                nameToSlot.put(mapped, i);

                // Class determination order of precedence:
                // 1. Tab list class (new)
                // 2. Scoreboard sidebar class
                // 3. PartyUtils class
                // 4. Item lore
                String mappedLower = mapped.toLowerCase(Locale.ENGLISH);
                String cls = null;

                // Try Tab list first (new method)
                if (tabClasses != null && !tabClasses.isEmpty()) {
                    cls = tabClasses.get(mapped);
                    if (cls == null) {
                        // Try case-insensitive match
                        for (Map.Entry<String, String> entry : tabClasses.entrySet()) {
                            if (entry.getKey().equalsIgnoreCase(mapped)) {
                                cls = entry.getValue();
                                break;
                            }
                        }
                    }
                }

                // Fall back to scoreboard
                if (cls == null) cls = classesFromSidebar.get(mappedLower);

                // Fall back to PartyUtils
                if (cls == null) cls = PartyUtils.getClassLetter(mapped);

                // Fall back to item lore
                if (cls == null) cls = extractClassFromItem(st);

                if (cls != null) nameToClass.put(mapped, cls);
            }
        }

        String self = mc.thePlayer.getName();
        if (!party.isEmpty()) {
            for (String p : party) {
                if (p.equalsIgnoreCase(self)) continue;
                if (nameToSlot.containsKey(p)) orderedNames.add(p);
            }
        }
        for (Map.Entry<String,Integer> en : nameToSlot.entrySet()) {
            if (!containsIgnoreCase(orderedNames, en.getKey())) orderedNames.add(en.getKey());
        }
        // Ensure self is not displayed even if party list was empty
        if (self != null) {
            for (Iterator<String> it = orderedNames.iterator(); it.hasNext();) {
                String n = it.next();
                if (n.equalsIgnoreCase(self)) { it.remove(); break; }
            }
        }
    }

    private void scanSidebarClasses() {
        classesFromSidebar.clear();
        try {
            List<String> lines = Utils.getSidebarLines();
            if (lines == null) return;
            for (String raw : lines) {
                String line = net.minecraft.util.StringUtils.stripControlCodes(raw).trim();
                // Expect like: [A] PlayerName 21,189 ❤ — take the first token after ']'
                if (line.length() < 4 || line.charAt(0) != '[') continue;
                int rb = line.indexOf(']');
                if (rb <= 1 || rb + 1 >= line.length()) continue;
                char clsChar = Character.toUpperCase(line.charAt(1));
                if ("HMTAB".indexOf(clsChar) == -1) continue;
                String after = line.substring(rb + 1).trim();
                if (after.isEmpty()) continue;
                String[] parts = after.split("\\s+");
                if (parts.length == 0) continue;
                String name = parts[0].trim();
                if (MC_USERNAME.matcher(name).matches()) {
                    classesFromSidebar.put(name.toLowerCase(Locale.ENGLISH), String.valueOf(clsChar));
                }
            }
        } catch (Throwable ignored) {}
    }

    private static boolean containsIgnoreCase(List<String> arr, String s) { for (String a : arr) if (a.equalsIgnoreCase(s)) return true; return false; }
    private static String safeDisplayName(ItemStack st) { try { if (st.hasDisplayName()) return st.getDisplayName(); } catch (Throwable ignored) {} return null; }

    private static String classFromLoreLine(String line) {
        if (line == null) return null;
        String s = net.minecraft.util.StringUtils.stripControlCodes(line).toLowerCase(Locale.ENGLISH);
        if (s.contains("healer")) return "H";
        if (s.contains("mage")) return "M";
        if (s.contains("tank")) return "T";
        if (s.contains("archer")) return "A";
        if (s.contains("berserk")) return "B";
        return null;
    }

    private static String extractClassFromItem(ItemStack st) {
        try {
            NBTTagCompound tag = st.getTagCompound();
            if (tag == null) return null;
            if (!tag.hasKey("display", 10)) return null;
            NBTTagCompound display = tag.getCompoundTag("display");
            if (!display.hasKey("Lore", 9)) return null;
            NBTTagList lore = display.getTagList("Lore", 8);
            for (int i = 0; i < lore.tagCount(); i++) {
                String line = lore.getStringTagAt(i);
                String cls = classFromLoreLine(line);
                if (cls != null) return cls;
            }
        } catch (Throwable ignored) {}
        return null;
    }

    // Helper to convert single-letter class to full display name
    static String fullClassName(String letter) {
        if (letter == null) return "Unknown";
        switch (letter.toUpperCase(Locale.ENGLISH)) {
            case "H": return "Healer";
            case "M": return "Mage";
            case "T": return "Tank";
            case "A": return "Archer";
            case "B": return "Berserker";
            default:  return "Unknown";
        }
    }

    // Class colors for display
    public static final Map<String, Integer> CLASS_COLORS = new HashMap<>();
    static {
        CLASS_COLORS.put("H", 0xFFFF55FF); // Healer: Pink
        CLASS_COLORS.put("M", 0xFF55AAFF); // Mage: Blue
        CLASS_COLORS.put("T", 0xFF55FF55); // Tank: Green
        CLASS_COLORS.put("A", 0xFFFFFF55); // Archer: Yellow
        CLASS_COLORS.put("B", 0xFFFF5555); // Berserker: Red
        CLASS_COLORS.put("?", 0xFFAAAAAA); // Unknown: Gray
    }

    private void clear() {
        windowId = -1;
        nameToSlot.clear();
        orderedNames.clear();
        nameToClass.clear();
        classesFromSidebar.clear();
        parsedTick = -1;
    }
}
