package com.aftertime.ratallofyou.modules.SkyBlock;

import com.aftertime.ratallofyou.utils.HotbarSwapUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

// New imports
import org.lwjgl.input.Keyboard;
import com.aftertime.ratallofyou.UI.config.ConfigData.AllConfig;
// Register client-side commands for preset triggers like "/kuudra1"
import net.minecraftforge.client.ClientCommandHandler;

/**
 * HotbarSwap: Port of the ChatTriggers HotbarSwapper.js to Forge 1.8.9 Java.
 * Provides preset save/load of hotbar, chat triggers, and movement-safe inventory swaps.
 */
public class HotbarSwap {
    private static final Minecraft mc = Minecraft.getMinecraft();

    // Singleton instance for UI access
    public static HotbarSwap INSTANCE;

    private static final int INVENTORY_HOTBAR_START = 0;     // InventoryPlayer indices for hotbar 0..8
    private static final int INVENTORY_HOTBAR_END = 8;
    private static final int INVENTORY_MAIN_START = 10;      // Match JS behavior: start at 10, reserving 9
    private static final int INVENTORY_MAIN_END = 35;

    // Data & state
    private final List<Hotbar> presets = new ArrayList<Hotbar>();
    private final Map<String, Hotbar> msgTriggers = new HashMap<String, Hotbar>();
    // Map of keyCode -> preset
    private final Map<Integer, Hotbar> keyTriggers = new HashMap<Integer, Hotbar>();
    private final Set<Integer> recentlyInteracted = new HashSet<Integer>(); // InventoryPlayer slot indices 0..35
    // Track client commands registered for "/..." triggers to avoid duplicates
    private final Set<String> registeredClientCommands = new HashSet<String>();

    private int moveCD = 0; // ticks left to block movement

    // Persistence
    private final File presetsFile = new File(new File(mc.mcDataDir, "config"), "ratallofyou/hotbar_presets.json");

    public HotbarSwap() {
        INSTANCE = this;
        loadFromDisk();
        indexTriggers();
        // Commands are deprecated for this feature; do not register.
        // registerCommands();
    }

    /* ===================== Config helpers ===================== */
    private boolean isModuleEnabled() {
        try {
            com.aftertime.ratallofyou.UI.config.ConfigData.BaseConfig<?> cfg = AllConfig.INSTANCE.MODULES.get("skyblock_hotbarswap");
            if (cfg instanceof com.aftertime.ratallofyou.UI.config.ConfigData.ModuleInfo) {
                return (Boolean) ((com.aftertime.ratallofyou.UI.config.ConfigData.ModuleInfo) cfg).Data;
            }
        } catch (Throwable ignored) {}
        return false;
    }

    private boolean isKeybindsEnabled() {
        try {
            com.aftertime.ratallofyou.UI.config.ConfigData.BaseConfig<?> cfg = AllConfig.INSTANCE.HOTBARSWAP_CONFIGS.get("hotbarswap_enable_keybinds");
            if (cfg != null && cfg.Data instanceof Boolean) return (Boolean) cfg.Data;
        } catch (Throwable ignored) {}
        return true;
    }

    private boolean isChatTriggersEnabled() {
        try {
            com.aftertime.ratallofyou.UI.config.ConfigData.BaseConfig<?> cfg = AllConfig.INSTANCE.HOTBARSWAP_CONFIGS.get("hotbarswap_enable_chat_triggers");
            if (cfg != null && cfg.Data instanceof Boolean) return (Boolean) cfg.Data;
        } catch (Throwable ignored) {}
        return true;
    }

    private int getBlockTicks() {
        try {
            com.aftertime.ratallofyou.UI.config.ConfigData.BaseConfig<?> cfg = AllConfig.INSTANCE.HOTBARSWAP_CONFIGS.get("hotbarswap_block_ticks");
            if (cfg != null && cfg.Data instanceof Integer) {
                int v = (Integer) cfg.Data;
                // Clamp to a small, safe maximum to avoid long movement locks
                return Math.max(0, Math.min(20, v));
            }
        } catch (Throwable ignored) {}
        return 10;
    }

    /* ===================== Commands (deprecated) ===================== */
    // Keeping classes for possible future use/migration, but not registered anymore.
    private abstract class BaseCmd extends CommandBase {
        @Override public boolean canCommandSenderUseCommand(ICommandSender sender) { return true; }
        @Override public List getCommandAliases() { return Collections.emptyList(); }
        @Override public int getRequiredPermissionLevel() { return 0; }
        @Override public List addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) { return Collections.emptyList(); }
    }
    // /presetmsg <preset> <full message>
    private class CmdPresetMsg extends BaseCmd {
        @Override public String getCommandName() { return "presetmsg"; }
        @Override public List getCommandAliases() { return Arrays.asList("premsg"); }
        @Override public String getCommandUsage(ICommandSender sender) { return "/presetmsg <preset> <message>"; }
        @Override public void processCommand(ICommandSender sender, String[] args) {
            if (args.length < 2) { chat("Invalid usage. /presetmsg <preset> <message>"); return; }
            String name = args[0];
            Hotbar preset = findPreset(name, true);
            if (preset == null) { chat("Invalid preset!"); return; }
            String full = joinArgs(args, 1);
            preset.message = full;
            saveToDisk();
            indexTriggers();
            chat("§aPreset §e" + preset.name + " §awill now trigger on: §c" + full);
        }
    }

    // /hotbar <name>
    private class CmdSaveHotbar extends BaseCmd {
        @Override public String getCommandName() { return "hotbar"; }
        @Override public List getCommandAliases() { return Arrays.asList("preset", "ps"); }
        @Override public String getCommandUsage(ICommandSender sender) { return "/hotbar <name>"; }
        @Override public void processCommand(ICommandSender sender, String[] args) {
            if (args.length < 1) { chat("Invalid name assignment!"); return; }
            if (findPreset(args[0], false) != null) { chat("Preset of " + args[0].toLowerCase() + " already exists!"); return; }
            saveCurrentHotbar(args[0]);
            chat("Saved hotbar preset " + args[0]);
        }
    }

    // /doslots <preset>
    private class CmdLoadSlots extends BaseCmd {
        @Override public String getCommandName() { return "doslots"; }
        @Override public String getCommandUsage(ICommandSender sender) { return "/doslots <preset>"; }
        @Override public void processCommand(ICommandSender sender, String[] args) {
            if (args.length < 1) { chat("Usage: /doslots <preset>"); return; }
            loadPreset(args[0]);
        }
    }

    // /listpresets
    private class CmdListPresets extends BaseCmd {
        @Override public String getCommandName() { return "listpresets"; }
        @Override public List getCommandAliases() { return Arrays.asList("lp"); }
        @Override public String getCommandUsage(ICommandSender sender) { return "/listpresets"; }
        @Override public void processCommand(ICommandSender sender, String[] args) {
            if (presets.isEmpty()) { chat("No hotbar presets to display :("); return; }
            for (Hotbar p : presets) printPreset(p);
        }
    }

    // /delpreset <preset>
    private class CmdDeletePreset extends BaseCmd {
        @Override public String getCommandName() { return "delpreset"; }
        @Override public List getCommandAliases() { return Arrays.asList("dp"); }
        @Override public String getCommandUsage(ICommandSender sender) { return "/delpreset <preset>"; }
        @Override public void processCommand(ICommandSender sender, String[] args) {
            if (args.length < 1) { chat("Usage: /delpreset <preset>"); return; }
            deletePreset(args[0]);
        }
    }

    // Client-side command tied to a specific preset (e.g., "/kuudra1")
    private class PresetClientCmd extends CommandBase {
        private final String cmd;
        private final String presetName; // kept for backward compatibility, but resolution now uses current mapping
        PresetClientCmd(String cmd, String presetName) { this.cmd = cmd; this.presetName = presetName; }
        @Override public String getCommandName() { return cmd; }
        @Override public String getCommandUsage(ICommandSender sender) { return "/" + cmd; }
        @Override public void processCommand(ICommandSender sender, String[] args) {
            if (!isModuleEnabled() || !isChatTriggersEnabled()) return;
            // Resolve using the latest mapping so edits take effect immediately
            Hotbar mapped = msgTriggers.get("/" + cmd);
            if (mapped != null) {
                loadPreset(mapped.name);
            } else if (presetName != null) {
                // Fallback: trigger the originally registered preset even if message text changed
                loadPreset(presetName);
            }
        }
        @Override public boolean canCommandSenderUseCommand(ICommandSender sender) { return true; }
        @Override public int getRequiredPermissionLevel() { return 0; }
    }

    /* ===================== Events ===================== */

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent e) {
        if (moveCD > 0) HotbarSwapUtils.stopInputs();
        try {
            if (mc == null || mc.thePlayer == null) return;
            // Only respond when module and keybind triggers are enabled and no GUI is open
            if (!isModuleEnabled()) return;
            if (!isKeybindsEnabled()) return;
            if (mc.currentScreen != null) return;

            int key = Keyboard.getEventKey();
            boolean down = Keyboard.getEventKeyState();
            if (!down) return; // only on key press
            if (key == Keyboard.KEY_NONE) return;
            Hotbar p = keyTriggers.get(key);
            if (p != null) loadPreset(p.name);
        } catch (Throwable ignored) { }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        if (moveCD > 0) {
            moveCD--;
            if (moveCD == 0) HotbarSwapUtils.restartMovement();
        }
    }

    // Listen for chat messages to trigger presets (incoming from server)
    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        try {
            if (!isModuleEnabled()) return;
            if (!isChatTriggersEnabled()) return;
            String msg = event.message.getUnformattedText();
            if (msg == null) return;
            Hotbar preset = msgTriggers.get(msg);
            if (preset == null) return;
            loadPreset(preset.name);
        } catch (Throwable ignored) {}
    }

    /* ===================== Core logic ===================== */

    // Public: allow other UIs (e.g., FastHotKey) to trigger swaps from a command/message string
    public boolean tryTriggerLocal(String text) {
        try {
            if (!isModuleEnabled()) return false;
            if (text == null) return false;
            String s = text.trim();
            if (s.isEmpty()) return false;
            // Exact match first
            Hotbar p = msgTriggers.get(s);
            if (p != null) { loadPreset(p.name); return true; }
            // If it looks like a command, try resolving by command name prefix (e.g., "/kuudra1 ...")
            if (s.startsWith("/")) {
                String nameOnly = s.substring(1).trim();
                int sp = nameOnly.indexOf(' ');
                if (sp > 0) nameOnly = nameOnly.substring(0, sp);
                // Try exact "/name" key
                p = msgTriggers.get("/" + nameOnly);
                if (p != null) { loadPreset(p.name); return true; }
            }
        } catch (Throwable ignored) {}
        return false;
    }

    private void loadPreset(String presetName) {
        if (presetName == null || mc.thePlayer == null) return;
        if (moveCD > 0) return;

        // Avoid interfering when a container/chat is open; retry shortly
        if (mc.currentScreen instanceof GuiContainer || mc.currentScreen instanceof GuiChat) {
            schedule(() -> loadPreset(presetName), 2000);
            return;
        }

        Hotbar target = findPreset(presetName, true);
        if (target == null) { chat("Preset of " + presetName + " doesn't exist!"); return; }

        recentlyInteracted.clear();
        // Pass 1: hotbar-to-hotbar only
        for (int i = INVENTORY_HOTBAR_START; i <= INVENTORY_HOTBAR_END; i++) setSlot(i, target.slots.get(i), true);

        // Pass 2: allow from main inventory after 50ms
        schedule(() -> {
            recentlyInteracted.clear();
            for (int i = INVENTORY_HOTBAR_START; i <= INVENTORY_HOTBAR_END; i++) setSlot(i, target.slots.get(i), false);
        }, 50);

        // Pass 3: finalize hotbar-to-hotbar after 100ms
        schedule(() -> {
            recentlyInteracted.clear();
            for (int i = INVENTORY_HOTBAR_START; i <= INVENTORY_HOTBAR_END; i++) setSlot(i, target.slots.get(i), true);
        }, 100);
    }

    private Hotbar findPreset(String name, boolean fuzzy) {
        if (name == null) return null;
        String q = name.toLowerCase(Locale.ROOT);
        for (Hotbar p : presets) {
            String pn = p.name.toLowerCase(Locale.ROOT);
            if (!fuzzy && pn.equals(q)) return p;
            if (fuzzy && pn.contains(q)) return p;
        }
        return null;
    }

    private ItemStack getStackInInventorySlot(int invIndex) {
        if (mc.thePlayer == null) return null;
        if (invIndex < 0 || invIndex > INVENTORY_MAIN_END) return null;
        return mc.thePlayer.inventory.getStackInSlot(invIndex);
    }

    private boolean compareUUID(HotbarItem hotbarItem, ItemStack stack) {
        String uuid = HotbarSwapUtils.getUUID(stack);
        if (uuid == null || hotbarItem.uuid == null) return false;
        String name = stack.getDisplayName();
        return hotbarItem.uuid.equals(uuid) && eqIgnoreCase(hotbarItem.name, name);
    }

    private boolean compareSkyblockID(HotbarItem hotbarItem, ItemStack stack) {
        String id = HotbarSwapUtils.getSkyblockID(stack);
        if (id == null || hotbarItem.id == null) return false;
        return hotbarItem.id.equalsIgnoreCase(id);
    }

    private int findEmptyMainSlot() {
        for (int i = INVENTORY_MAIN_START; i <= INVENTORY_MAIN_END; i++) {
            if (recentlyInteracted.contains(i)) continue;
            if (getStackInInventorySlot(i) == null) return i;
        }
        return HotbarSwapUtils.NOT_FOUND;
    }

    private int findMatchingItem(HotbarItem hotbarItem, int targetHotbarSlot, boolean includeHotbar) {
        if (hotbarItem == null || (hotbarItem.id == null && hotbarItem.uuid == null)) return HotbarSwapUtils.NOT_FOUND;

        // If target already matches, skip
        ItemStack inTarget = getStackInInventorySlot(targetHotbarSlot);
        if (inTarget != null && (compareUUID(hotbarItem, inTarget) || compareSkyblockID(hotbarItem, inTarget))) {
            return HotbarSwapUtils.NOT_FOUND;
        }

        int start = includeHotbar ? INVENTORY_HOTBAR_START : INVENTORY_MAIN_START;
        for (int i = start; i <= INVENTORY_MAIN_END; i++) {
            if (i == targetHotbarSlot) continue;
            if (recentlyInteracted.contains(i)) continue;
            ItemStack s = getStackInInventorySlot(i);
            if (s == null) continue;
            boolean match = compareUUID(hotbarItem, s) || compareSkyblockID(hotbarItem, s);
            if (match) return i;
        }
        return HotbarSwapUtils.NOT_FOUND;
    }

    private void setSlot(int hotbarSlot, HotbarItem item, boolean hotbarSort) {
        if (hotbarSlot < INVENTORY_HOTBAR_START || hotbarSlot > INVENTORY_HOTBAR_END) return;

        // Clearing requested
        if (item == null || (item.id == null && item.uuid == null)) {
            clearSlot(hotbarSlot);
            return;
        }

        int itemSlot = findMatchingItem(item, hotbarSlot, hotbarSort);
        if (itemSlot == HotbarSwapUtils.NOT_FOUND) return;
        if (recentlyInteracted.contains(hotbarSlot)) return;
        if (hotbarSort && !(itemSlot >= INVENTORY_HOTBAR_START && itemSlot <= INVENTORY_HOTBAR_END)) return; // pass 1 only moves inside hotbar

        recentlyInteracted.add(itemSlot);

        // Single click: hotbar swap from source slot to target hotbar index
        int containerSlotId = getContainerSlotIdForInvIndex(itemSlot);
        if (containerSlotId == HotbarSwapUtils.NOT_FOUND) return;
        performHotbarSwapClick(containerSlotId, hotbarSlot);
    }

    private void clearSlot(int hotbarSlot) {
        if (hotbarSlot < INVENTORY_HOTBAR_START || hotbarSlot > INVENTORY_HOTBAR_END) return;
        ItemStack cur = getStackInInventorySlot(hotbarSlot);
        if (cur == null) return;

        int emptyMain = findEmptyMainSlot();
        if (emptyMain == HotbarSwapUtils.NOT_FOUND) return;
        recentlyInteracted.add(emptyMain);

        // Click on empty main slot with hotbar-swap to move item from hotbarSlot into it
        int containerSlotId = getContainerSlotIdForInvIndex(emptyMain);
        if (containerSlotId == HotbarSwapUtils.NOT_FOUND) return;
        performHotbarSwapClick(containerSlotId, hotbarSlot);
    }

    private void performHotbarSwapClick(int containerSlotId, int targetHotbarIndex) {
        try {
            Container c = mc.thePlayer.openContainer;
            int windowId = c.windowId;
            // type=2 => SWAP_WITH_HOTBAR, mouseButton = hotbar index
            mc.playerController.windowClick(windowId, containerSlotId, targetHotbarIndex, 2, mc.thePlayer);
            // Only trigger movement suppression if not already active, to avoid extending the lock with multiple clicks
            int blockTicks = getBlockTicks();
            if (blockTicks > 0 && moveCD <= 0) {
                HotbarSwapUtils.stopInputs();
                moveCD = blockTicks;
            }
        } catch (Throwable ignored) { }
    }

    private int getContainerSlotIdForInvIndex(int invIndex) {
        try {
            Container c = mc.thePlayer.openContainer;
            List slots = c.inventorySlots;
            for (int i = 0; i < slots.size(); i++) {
                Object o = slots.get(i);
                if (!(o instanceof Slot)) continue;
                Slot s = (Slot) o;
                if (s.inventory == mc.thePlayer.inventory && s.getSlotIndex() == invIndex) return i;
            }
        } catch (Throwable ignored) {}
        return HotbarSwapUtils.NOT_FOUND;
    }

    /* ===================== Preset IO and helpers ===================== */

    private void indexTriggers() {
        msgTriggers.clear();
        keyTriggers.clear();
        // Keep existing client commands; add new ones as needed
        for (Hotbar p : presets) {
            // Message trigger: exact incoming chat match
            if (p.message != null && !p.message.trim().isEmpty()) {
                String msg = p.message.trim();
                msgTriggers.put(msg, p);
                // Backward compatibility: if message itself starts with a slash, also treat it as a command trigger
                if (msg.startsWith("/")) {
                    String nameOnly = msg.substring(1).trim();
                    int sp = nameOnly.indexOf(' ');
                    if (sp > 0) nameOnly = nameOnly.substring(0, sp);
                    if (!nameOnly.isEmpty()) {
                        msgTriggers.put("/" + nameOnly, p);
                        if (!registeredClientCommands.contains(nameOnly)) {
                            try { ClientCommandHandler.instance.registerCommand(new PresetClientCmd(nameOnly, p.name)); registeredClientCommands.add(nameOnly); } catch (Throwable ignored) { }
                        }
                    }
                }
            }
            // Command trigger: user-entered command name, with or without leading '/'
            if (p.command != null && !p.command.trim().isEmpty()) {
                String cmd = p.command.trim();
                String nameOnly = cmd.startsWith("/") ? cmd.substring(1).trim() : cmd;
                int sp = nameOnly.indexOf(' ');
                if (sp > 0) nameOnly = nameOnly.substring(0, sp);
                if (!nameOnly.isEmpty()) {
                    // Map "/name" to preset so tryTriggerLocal works
                    msgTriggers.put("/" + nameOnly, p);
                    if (!registeredClientCommands.contains(nameOnly)) {
                        try { ClientCommandHandler.instance.registerCommand(new PresetClientCmd(nameOnly, p.name)); registeredClientCommands.add(nameOnly); } catch (Throwable ignored) { }
                    }
                }
            }
            if (p.keyCode != null && p.keyCode > 0) keyTriggers.put(p.keyCode, p);
        }
    }

    // Backward-compatible: if no name exists, generate one
    private String generateDefaultName() {
        int i = 1;
        while (true) {
            String cand = "Preset " + i;
            if (findPreset(cand, false) == null) return cand;
            i++;
        }
    }

    public synchronized int addPresetFromCurrentHotbar() {
        String name = generateDefaultName();
        List<HotbarItem> list = new ArrayList<HotbarItem>();
        for (int i = INVENTORY_HOTBAR_START; i <= INVENTORY_HOTBAR_END; i++) {
            ItemStack s = getStackInInventorySlot(i);
            list.add(new HotbarItem(s));
        }
        Hotbar hb = new Hotbar(name, list, null, null, -1);
        presets.add(hb);
        saveToDisk();
        indexTriggers();
        return presets.size() - 1;
    }

    public synchronized List<Hotbar> getPresetsView() {
        return Collections.unmodifiableList(presets);
    }

    public synchronized void updatePresetMeta(int index, String name, String message, Integer keyCode) {
        // Backward-compatible method: no command provided
        updatePresetMeta(index, name, message, null, keyCode);
    }

    // New: accepts both message and command updates
    public synchronized void updatePresetMeta(int index, String name, String message, String command, Integer keyCode) {
        if (index < 0 || index >= presets.size()) return;
        Hotbar p = presets.get(index);
        if (name != null) p.name = name;
        if (message != null) p.message = message;
        if (command != null) p.command = command;
        if (keyCode != null) p.keyCode = keyCode;
        saveToDisk();
        indexTriggers();
    }

    // New: remove by index for UI
    public synchronized void removePreset(int index) {
        if (index < 0 || index >= presets.size()) return;
        presets.remove(index);
        saveToDisk();
        indexTriggers();
    }

    private void saveCurrentHotbar(String name) {
        List<HotbarItem> list = new ArrayList<HotbarItem>();
        for (int i = INVENTORY_HOTBAR_START; i <= INVENTORY_HOTBAR_END; i++) {
            ItemStack s = getStackInInventorySlot(i);
            list.add(new HotbarItem(s));
        }
        Hotbar hb = new Hotbar(name, list);
        presets.add(hb);
        saveToDisk();
        indexTriggers();
    }

    private void deletePreset(String name) {
        Hotbar target = findPreset(name, true);
        if (target == null) { chat("Couldn't find a preset matching your search :("); return; }
        presets.remove(target);
        saveToDisk();
        indexTriggers();
        chat("Removed preset " + target.name);
    }

    private void printPreset(Hotbar p) {
        chatClient("§c------------------------------");
        chatClient("§a§lPreset Name:§r " + p.name);
        chatClient("§a§lTrigger Message:§r " + (p.message != null ? p.message : "None"));
        chatClient("§a§lTrigger Command:§r " + (p.command != null ? p.command : "None"));
        // Show key if present
        String keyStr = (p.keyCode != null && p.keyCode > 0) ? Keyboard.getKeyName(p.keyCode) : "None";
        chatClient("§a§lKeybind:§r " + keyStr);
        for (int i = 0; i < p.slots.size(); i++) {
            String nm = p.slots.get(i).name != null ? p.slots.get(i).name : "None";
            chatClient("§bItem in slot §a" + (i + 1) + ":§r " + nm);
        }
        chatClient("§c------------------------------");
    }

    private void chat(String msg) { if (mc.thePlayer != null) mc.thePlayer.addChatMessage(new ChatComponentText(msg)); }
    private void chatClient(String msg) { chat(msg); }

    private String joinArgs(String[] args, int from) {
        StringBuilder sb = new StringBuilder();
        for (int i = from; i < args.length; i++) {
            if (i > from) sb.append(' ');
            sb.append(args[i]);
        }
        return sb.toString();
    }

    private boolean eqIgnoreCase(String a, String b) {
        if (a == null || b == null) return false;
        return a.equalsIgnoreCase(b);
    }

    private void schedule(Runnable r, long delayMs) {
        try {
            new Timer().schedule(new TimerTask() {
                @Override public void run() { mc.addScheduledTask(r); }
            }, Math.max(0, delayMs));
        } catch (Throwable ignored) { }
    }

    /* ===================== Persistence ===================== */

    private void ensureFile() {
        try {
            File parent = presetsFile.getParentFile();
            if (!parent.exists()) parent.mkdirs();
            if (!presetsFile.exists()) presetsFile.createNewFile();
        } catch (IOException ignored) { }
    }

    private void saveToDisk() {
        ensureFile();
        try {
            String json = serializePresets();
            try (Writer w = new OutputStreamWriter(new FileOutputStream(presetsFile), StandardCharsets.UTF_8)) {
                w.write(json);
            }
        } catch (Exception ignored) { }
    }

    private void loadFromDisk() {
        presets.clear();
        ensureFile();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(presetsFile), StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line).append('\n');
            String json = sb.toString().trim();
            if (json.isEmpty()) return;
            deserializePresets(json);
        } catch (Exception ignored) { }
    }

    // Minimal JSON (no external deps assumed). Format:
    // {"presets":[{"name":"...","message":"...","key":123,"slots":[{"uuid":"..","id":"..","name":".."},...]}...]}
    private String serializePresets() {
        StringBuilder sb = new StringBuilder();
        sb.append('{').append("\"presets\":[");
        for (int i = 0; i < presets.size(); i++) {
            Hotbar p = presets.get(i);
            if (i > 0) sb.append(',');
            sb.append('{');
            sb.append("\"name\":\"").append(escape(p.name)).append("\",");
            sb.append("\"message\":").append(p.message == null ? "null" : ("\"" + escape(p.message) + "\""));
            sb.append(',').append("\"command\":").append(p.command == null ? "null" : ("\"" + escape(p.command) + "\""));
            // keyCode persisted (use -1 for none)
            sb.append(',').append("\"key\":").append(p.keyCode == null ? -1 : p.keyCode.intValue());
            sb.append(',').append("\"slots\":[");
            for (int j = 0; j < p.slots.size(); j++) {
                HotbarItem it = p.slots.get(j);
                if (j > 0) sb.append(',');
                sb.append('{')
                        .append("\"uuid\":").append(it.uuid == null ? "null" : ("\"" + escape(it.uuid) + "\""))
                        .append(',')
                        .append("\"id\":").append(it.id == null ? "null" : ("\"" + escape(it.id) + "\""))
                        .append(',')
                        .append("\"name\":").append(it.name == null ? "null" : ("\"" + escape(it.name) + "\""))
                        .append('}');
            }
            sb.append(']');
            sb.append('}');
        }
        sb.append(']');
        sb.append('}');
        return sb.toString();
    }

    private void deserializePresets(String json) {
        try {
            // Very small hand-rolled parser tolerant to our format
            // Split by top-level objects under presets array
            presets.clear();
            int arrStart = json.indexOf("\"presets\"");
            if (arrStart < 0) return;
            int bracket = json.indexOf('[', arrStart);
            int end = json.lastIndexOf(']');
            if (bracket < 0 || end < 0 || end <= bracket) return;
            String arr = json.substring(bracket + 1, end).trim();
            if (arr.isEmpty()) return;
            // Split objects by '},{' at top level (no nested braces inside slots arrays for our simple values)
            List<String> objs = splitTopLevel(arr);
            for (String obj : objs) {
                String name = extractNullableString(obj, "name");
                String message = extractNullableString(obj, "message");
                String command = extractNullableString(obj, "command");
                Integer key = extractNullableInt(obj, "key");
                if (key == null) key = -1;
                List<HotbarItem> slots = new ArrayList<HotbarItem>();
                String slotsArr = extractArray(obj, "slots");
                if (slotsArr != null && !slotsArr.isEmpty()) {
                    List<String> slotObjs = splitTopLevel(slotsArr);
                    for (String so : slotObjs) {
                        String uuid = extractNullableString(so, "uuid");
                        String id = extractNullableString(so, "id");
                        String nm = extractNullableString(so, "name");
                        HotbarItem hi = new HotbarItem(uuid, id, nm);
                        slots.add(hi);
                    }
                }
                if (name != null && slots.size() == 9) presets.add(new Hotbar(name, slots, message, command, key));
            }
        } catch (Throwable ignored) { }
        indexTriggers();
    }

    private Integer extractNullableInt(String obj, String key) {
        try {
            String needle = "\"" + key + "\":";
            int idx = obj.indexOf(needle);
            if (idx < 0) return null;
            int valStart = idx + needle.length();
            // handle null
            if (obj.startsWith("null", valStart)) return null;
            // read until comma or closing brace
            int i = valStart;
            StringBuilder sb = new StringBuilder();
            while (i < obj.length()) {
                char c = obj.charAt(i);
                if ((c >= '0' && c <= '9') || c == '-' ) { sb.append(c); i++; continue; }
                break;
            }
            if (sb.length() == 0) return null;
            try { return Integer.parseInt(sb.toString()); } catch (NumberFormatException nfe) { return null; }
        } catch (Throwable ignored) { return null; }
    }

    // JSON helpers for simple parsing
    private String extractString(String obj, String key) {
        String v = extractNullableString(obj, key);
        return v == null ? "" : v;
    }

    private String extractNullableString(String obj, String key) {
        try {
            String needle = "\"" + key + "\":";
            int idx = obj.indexOf(needle);
            if (idx < 0) return null;
            int valStart = idx + needle.length();
            if (obj.startsWith("null", valStart)) return null;
            int q1 = obj.indexOf('"', valStart);
            if (q1 < 0) return null;
            int q2 = obj.indexOf('"', q1 + 1);
            if (q2 < 0) return null;
            return unescape(obj.substring(q1 + 1, q2));
        } catch (Throwable ignored) { return null; }
    }

    private String extractArray(String obj, String key) {
        try {
            String needle = "\"" + key + "\":";
            int idx = obj.indexOf(needle);
            if (idx < 0) return null;
            int valStart = obj.indexOf('[', idx + needle.length());
            if (valStart < 0) return null;
            int depth = 0;
            for (int i = valStart; i < obj.length(); i++) {
                char c = obj.charAt(i);
                if (c == '[') depth++;
                else if (c == ']') { depth--; if (depth == 0) return obj.substring(valStart + 1, i); }
            }
            return null;
        } catch (Throwable ignored) { return null; }
    }

    private List<String> splitTopLevel(String arr) {
        List<String> out = new ArrayList<String>();
        int depth = 0; int start = 0;
        for (int i = 0; i < arr.length(); i++) {
            char c = arr.charAt(i);
            if (c == '{' || c == '[') depth++;
            else if (c == '}' || c == ']') depth--;
            else if (c == ',' && depth == 0) {
                out.add(arr.substring(start, i).trim());
                start = i + 1;
            }
        }
        if (start < arr.length()) out.add(arr.substring(start).trim());
        return out;
    }

    private String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
    private String unescape(String s) {
        return s.replace("\\\"", "\"").replace("\\\\", "\\");
    }

    /* ===================== Data classes ===================== */
    public static class Hotbar {
        public String name;
        public List<HotbarItem> slots;
        public String message;
        // New: separate command trigger (with or without leading '/')
        public String command;
        // New: keybind code (LWJGL key code), -1 or null means none
        public Integer keyCode;
        public Hotbar() {}
        public Hotbar(String name, List<HotbarItem> slots) { this.name = name; this.slots = slots; this.message = null; this.command = null; this.keyCode = -1; }
        public Hotbar(String name, List<HotbarItem> slots, String message) { this.name = name; this.slots = slots; this.message = message; this.command = null; this.keyCode = -1; }
        public Hotbar(String name, List<HotbarItem> slots, String message, Integer keyCode) { this.name = name; this.slots = slots; this.message = message; this.command = null; this.keyCode = keyCode == null ? -1 : keyCode; }
        public Hotbar(String name, List<HotbarItem> slots, String message, String command, Integer keyCode) { this.name = name; this.slots = slots; this.message = message; this.command = command; this.keyCode = keyCode == null ? -1 : keyCode; }
    }

    public static class HotbarItem {
        public String uuid;
        public String id;
        public String name;
        public HotbarItem() {}
        public HotbarItem(ItemStack stack) {
            this.uuid = HotbarSwapUtils.getUUID(stack);
            this.id = HotbarSwapUtils.getSkyblockID(stack);
            this.name = stack != null ? stack.getDisplayName() : "None";
        }
        public HotbarItem(String uuid, String id, String name) { this.uuid = uuid; this.id = id; this.name = name; }
    }
}
