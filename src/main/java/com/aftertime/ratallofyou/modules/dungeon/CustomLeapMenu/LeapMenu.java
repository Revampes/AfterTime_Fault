package com.aftertime.ratallofyou.modules.dungeon.CustomLeapMenu;

import com.aftertime.ratallofyou.UI.Settings.BooleanSettings;
import com.aftertime.ratallofyou.utils.DungeonUtils;
import com.aftertime.ratallofyou.utils.PartyUtils;
import com.aftertime.ratallofyou.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.renderer.GlStateManager;
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
import org.lwjgl.opengl.GL11;

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

    // Radial layout
    private int centerX, centerY;
    private int innerRadius = 70;
    private int outerRadius = 180;
    private static final float GAP_PX = 6f;
    private static final double ANGLE_OFFSET = Math.PI / 4; // rotate so X lines are the separators

    private static final Pattern MC_USERNAME = Pattern.compile("^[A-Za-z0-9_]{1,16}$");

    public LeapMenu() { MinecraftForge.EVENT_BUS.register(this); }

    private static boolean isEnabled() { return BooleanSettings.isEnabled("dungeons_customleapmenu"); }

    private static boolean isSpiritLeapOpen() {
        if (!isEnabled()) return false;
        Minecraft m = Minecraft.getMinecraft();
        if (m == null || !(m.currentScreen instanceof GuiChest) || m.thePlayer == null) return false;
        if (!(m.thePlayer.openContainer instanceof ContainerChest)) return false;
        try {
            ContainerChest cc = (ContainerChest) m.thePlayer.openContainer;
            IInventory inv = cc.getLowerChestInventory();
            if (inv == null || inv.getDisplayName() == null) return false;
            String title = net.minecraft.util.StringUtils.stripControlCodes(inv.getDisplayName().getUnformattedText()).toLowerCase(Locale.ENGLISH);
            return title.contains("spirit") && title.contains("leap");
        } catch (Throwable ignored) { return false; }
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
        centerX = w / 2; centerY = h / 2;
        // Fixed 4-slot layout
        int count = 4;
        innerRadius = 70;
        outerRadius = 200;

        // Dim background
        Gui.drawRect(0, 0, w, h, 0xB0000000);

        // Draw ring sectors (always 4 slots)
        drawRingSectors(e.mouseX, e.mouseY, count);

        // Header
        FontRenderer fr = mc.fontRendererObj;
        String header = "Spirit Leap";
        fr.drawString(header, centerX - fr.getStringWidth(header) / 2, centerY - outerRadius - 18, 0xFFFFFF, false);

        // Footer hint (limit to 1-4)
        String hint = orderedNames.isEmpty() ? "No targets" : ("Left click or press 1-" + Math.min(4, orderedNames.size()));
        fr.drawString(hint, centerX - fr.getStringWidth(hint) / 2, centerY + outerRadius + 6, 0xAAAAAA, false);
    }

    @SubscribeEvent
    public void onMouse(GuiScreenEvent.MouseInputEvent.Pre e) {
        if (!isActive()) return;
        if (!Mouse.getEventButtonState()) return;
        int btn = Mouse.getEventButton();
        if (btn != 0 && btn != 1) return;
        int mx = Mouse.getEventX() * e.gui.width / mc.displayWidth;
        int my = e.gui.height - Mouse.getEventY() * e.gui.height / mc.displayHeight - 1;
        int idx = getHoveredRegion(mx, my, 4); // fixed 4 regions
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

    private void drawRingSectors(int mouseX, int mouseY, int count) {
        if (count <= 0) return;
        double sector = (Math.PI * 2.0) / count;
        double gapAngleInner = GAP_PX / Math.max(1.0, innerRadius);
        double gapAngleOuter = GAP_PX / Math.max(1.0, outerRadius);

        // Setup GL
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        GlStateManager.disableTexture2D();
        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);
        GlStateManager.disableCull();

        for (int i = 0; i < count; i++) {
            double start = ANGLE_OFFSET + i * sector;
            double end = ANGLE_OFFSET + (i + 1) * sector;
            double iStart = start + gapAngleInner * 0.5;
            double iEnd = end - gapAngleInner * 0.5;
            double oStart = start + gapAngleOuter * 0.5;
            double oEnd = end - gapAngleOuter * 0.5;
            if (iEnd <= iStart || oEnd <= oStart) continue;

            boolean hovered = (getHoveredRegion(mouseX, mouseY, count) == i);
            int base = hovered ? 0x40FFFFFF : 0x3020A0FF; // light / bluish
            float a = (base >> 24 & 255) / 255f;
            float r = (base >> 16 & 255) / 255f;
            float g = (base >> 8 & 255) / 255f;
            float b = (base & 255) / 255f;
            GL11.glColor4f(r, g, b, a);

            // Fill sector as triangle strip
            GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
            int steps = 48;
            for (int s = 0; s <= steps; s++) {
                double t = (double) s / steps;
                double oa = oStart + t * (oEnd - oStart);
                double ia = iStart + t * (iEnd - iStart);
                float ox = (float) (centerX + Math.cos(oa) * outerRadius);
                float oy = (float) (centerY + Math.sin(oa) * outerRadius);
                float ix = (float) (centerX + Math.cos(ia) * innerRadius);
                float iy = (float) (centerY + Math.sin(ia) * innerRadius);
                GL11.glVertex2f(ox, oy);
                GL11.glVertex2f(ix, iy);
            }
            GL11.glEnd();

            // Draw label at mid-angle (need textures ON for font rendering)
            double mid = (start + end) * 0.5;
            int rx = (int) (centerX + Math.cos(mid) * (innerRadius + (outerRadius - innerRadius) * 0.65));
            int ry = (int) (centerY + Math.sin(mid) * (innerRadius + (outerRadius - innerRadius) * 0.65));
            String label = (i < orderedNames.size()) ? formatLabel(orderedNames.get(i)) : "";
            FontRenderer fr = mc.fontRendererObj;
            GlStateManager.enableTexture2D();
            GL11.glColor4f(1f, 1f, 1f, 1f);
            if (!label.isEmpty()) {
                fr.drawString((i + 1) + ". " + label, rx - fr.getStringWidth((i + 1) + ". " + label) / 2, ry - fr.FONT_HEIGHT / 2, 0xFFFFFF, false);
            } else {
                String idxStr = String.valueOf(i + 1);
                fr.drawString(idxStr, rx - fr.getStringWidth(idxStr) / 2, ry - fr.FONT_HEIGHT / 2, 0x666666, false);
            }
            GlStateManager.disableTexture2D();
        }

        // Draw X separator lines across the ring (at 45° and 135°)
        GL11.glLineWidth(2.5f);
        GL11.glColor4f(1f, 1f, 1f, 0.5f);
        GL11.glBegin(GL11.GL_LINES);
        drawRadialLine(Math.PI / 4);           // 45°
        drawRadialLine(Math.PI / 4 + Math.PI); // 225°
        drawRadialLine(3 * Math.PI / 4);       // 135°
        drawRadialLine(3 * Math.PI / 4 + Math.PI); // 315°
        GL11.glEnd();

        // Outlines
        GL11.glLineWidth(2f);
        GL11.glColor4f(1f, 1f, 1f, 0.33f);
        GL11.glBegin(GL11.GL_LINE_LOOP);
        for (int s = 0; s < 144; s++) {
            double a = (Math.PI * 2.0) * s / 144.0;
            GL11.glVertex2f((float)(centerX + Math.cos(a) * outerRadius), (float)(centerY + Math.sin(a) * outerRadius));
        }
        GL11.glEnd();
        GL11.glBegin(GL11.GL_LINE_LOOP);
        for (int s = 0; s < 144; s++) {
            double a = (Math.PI * 2.0) * s / 144.0;
            GL11.glVertex2f((float)(centerX + Math.cos(a) * innerRadius), (float)(centerY + Math.sin(a) * innerRadius));
        }
        GL11.glEnd();

        // restore GL
        GlStateManager.enableCull();
        GlStateManager.depthMask(true);
        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    private void drawRadialLine(double angle) {
        float ix = (float) (centerX + Math.cos(angle) * innerRadius);
        float iy = (float) (centerY + Math.sin(angle) * innerRadius);
        float ox = (float) (centerX + Math.cos(angle) * outerRadius);
        float oy = (float) (centerY + Math.sin(angle) * outerRadius);
        GL11.glVertex2f(ix, iy);
        GL11.glVertex2f(ox, oy);
    }

    private int getHoveredRegion(int mouseX, int mouseY, int count) {
        if (count <= 0) return -1;
        double dx = mouseX - centerX;
        double dy = mouseY - centerY;
        double dist = Math.hypot(dx, dy);
        if (dist < innerRadius || dist > outerRadius) return -1;
        double ang = Math.atan2(dy, dx);
        if (ang < 0) ang += Math.PI * 2.0;
        // rotate so separators align with X lines
        ang -= ANGLE_OFFSET;
        if (ang < 0) ang += Math.PI * 2.0;
        double sector = (Math.PI * 2.0) / count;
        int idx = (int) Math.floor(ang / sector);
        // Respect gap near borders at current radius
        double localGap = GAP_PX / Math.max(1.0, dist);
        double within = ang - idx * sector;
        if (within < localGap * 0.5 || within > sector - localGap * 0.5) return -1;
        return Math.max(0, Math.min(count - 1, idx));
    }

    private String formatLabel(String name) {
        String cls = nameToClass.get(name);
        if (cls == null) cls = "?";
        return "[" + cls + "] " + name;
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
        // refresh sidebar classes first
        scanSidebarClasses();
        if (mc.thePlayer == null) return;
        if (!(mc.thePlayer.openContainer instanceof ContainerChest)) return;
        ContainerChest chest = (ContainerChest) mc.thePlayer.openContainer;
        List<Slot> slots = chest.inventorySlots;
        int chestInvSize = 27;
        try { IInventory inv = chest.getLowerChestInventory(); if (inv != null) chestInvSize = Math.min(inv.getSizeInventory(), slots.size()); } catch (Throwable ignored) {}
        List<String> party = PartyUtils.getPartyMembers();
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
                // Prefer sidebar class, then PartyUtils, then lore
                String cls = classesFromSidebar.get(mapped.toLowerCase(Locale.ENGLISH));
                if (cls == null) cls = PartyUtils.getClassLetter(mapped);
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

    private void clear() { windowId = -1; nameToSlot.clear(); orderedNames.clear(); nameToClass.clear(); parsedTick = -1; }
}
