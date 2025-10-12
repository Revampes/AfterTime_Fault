package com.aftertime.ratallofyou.UI.newui.config;

import com.aftertime.ratallofyou.config.ModConfig;
import com.aftertime.ratallofyou.modules.dungeon.AutoSell;
import com.google.gson.*;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class ModConfigIO {
    private static final File CONFIG_DIR = new File("config/ratallofyou");
    private static final File CONFIG_FILE = new File(CONFIG_DIR, "newui-config.json");

    // Keys for Fast Hotkey JSON section
    private static final String FHK_PRESETS_KEY = "fhk_presets";
    private static final String FHK_ACTIVE_KEY = "fhk_active_index";

    public static void load() {
        ensureDir();
        if (!CONFIG_FILE.exists()) {
            // Nothing saved yet
            return;
        }
        try (Reader reader = new InputStreamReader(new FileInputStream(CONFIG_FILE), StandardCharsets.UTF_8)) {
            JsonElement rootEl = new JsonParser().parse(reader);
            if (!rootEl.isJsonObject()) return;
            JsonObject obj = rootEl.getAsJsonObject();
            // Iterate all fields in ModConfig and populate from json when present
            for (Field f : ModConfig.class.getDeclaredFields()) {
                String name = f.getName();
                if (!obj.has(name)) continue;
                try {
                    f.setAccessible(true);
                    Class<?> t = f.getType();
                    JsonElement v = obj.get(name);
                    if (t == boolean.class || t == Boolean.class) {
                        f.setBoolean(null, v.getAsBoolean());
                    } else if (t == int.class || t == Integer.class) {
                        f.setInt(null, v.getAsInt());
                    } else if (t == float.class || t == Float.class) {
                        f.setFloat(null, v.getAsFloat());
                    } else if (t == double.class || t == Double.class) {
                        f.setDouble(null, v.getAsDouble());
                    } else if (t == long.class || t == Long.class) {
                        f.setLong(null, v.getAsLong());
                    } else if (t == String.class) {
                        f.set(null, v.isJsonNull() ? null : v.getAsString());
                    }
                } catch (Throwable ignored) {}
            }
            // Load Fast Hotkey presets if present
            try {
                if (obj.has(FHK_PRESETS_KEY)) {
                    java.util.List<com.aftertime.ratallofyou.UI.config.ConfigData.FastHotkeyPreset> list = new java.util.ArrayList<>();
                    JsonArray arr = obj.getAsJsonArray(FHK_PRESETS_KEY);
                    for (JsonElement el : arr) {
                        if (!el.isJsonObject()) continue;
                        JsonObject pObj = el.getAsJsonObject();
                        String name = pObj.has("name") ? pObj.get("name").getAsString() : "Preset";
                        com.aftertime.ratallofyou.UI.config.ConfigData.FastHotkeyPreset p = new com.aftertime.ratallofyou.UI.config.ConfigData.FastHotkeyPreset(name);
                        p.enabled = pObj.has("enabled") && pObj.get("enabled").getAsBoolean();
                        p.keyCode = pObj.has("keyCode") ? pObj.get("keyCode").getAsInt() : 0;
                        if (pObj.has("entries") && pObj.get("entries").isJsonArray()) {
                            JsonArray eArr = pObj.getAsJsonArray("entries");
                            int idx = 0;
                            for (JsonElement ee : eArr) {
                                if (!ee.isJsonObject()) continue;
                                JsonObject eObj = ee.getAsJsonObject();
                                String label = eObj.has("label") ? eObj.get("label").getAsString() : "";
                                String cmd = eObj.has("command") ? eObj.get("command").getAsString() : "";
                                p.entries.add(new com.aftertime.ratallofyou.UI.config.ConfigData.FastHotkeyEntry(label, cmd, idx++));
                            }
                        }
                        list.add(p);
                    }
                    int active = obj.has(FHK_ACTIVE_KEY) ? obj.get(FHK_ACTIVE_KEY).getAsInt() : 0;
                    com.aftertime.ratallofyou.UI.config.ConfigData.AllConfig.INSTANCE.FHK_PRESETS = list;
                    com.aftertime.ratallofyou.UI.config.ConfigData.AllConfig.INSTANCE.FHK_ACTIVE_PRESET = Math.max(0, Math.min(active, Math.max(0, list.size() - 1)));
                    if (!list.isEmpty()) {
                        com.aftertime.ratallofyou.UI.config.ConfigData.AllConfig.INSTANCE.FAST_HOTKEY_ENTRIES = list.get(com.aftertime.ratallofyou.UI.config.ConfigData.AllConfig.INSTANCE.FHK_ACTIVE_PRESET).entries;
                    } else {
                        com.aftertime.ratallofyou.UI.config.ConfigData.AllConfig.INSTANCE.FAST_HOTKEY_ENTRIES = new java.util.ArrayList<>();
                    }
                }
            } catch (Throwable ignored) {}
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Notify AutoSell after loading config
        if (AutoSell.getInstance() != null) {
            AutoSell.getInstance().onConfigChanged();
        }
    }

    public static void save() {
        ensureDir();
        JsonObject obj = new JsonObject();
        for (Field f : ModConfig.class.getDeclaredFields()) {
            try {
                f.setAccessible(true);
                Object val = f.get(null);
                if (val == null) {
                    obj.add(f.getName(), JsonNull.INSTANCE);
                } else if (val instanceof Boolean) {
                    obj.addProperty(f.getName(), (Boolean) val);
                } else if (val instanceof Number) {
                    obj.addProperty(f.getName(), (Number) val);
                } else if (val instanceof String) {
                    obj.addProperty(f.getName(), (String) val);
                } else {
                    // Fallback: store toString
                    obj.addProperty(f.getName(), String.valueOf(val));
                }
            } catch (Throwable ignored) {}
        }
        // Persist Fast Hotkey presets as part of main JSON
        try {
            JsonArray arr = new JsonArray();
            java.util.List<com.aftertime.ratallofyou.UI.config.ConfigData.FastHotkeyPreset> list = com.aftertime.ratallofyou.UI.config.ConfigData.AllConfig.INSTANCE.FHK_PRESETS;
            if (list != null) {
                for (com.aftertime.ratallofyou.UI.config.ConfigData.FastHotkeyPreset p : list) {
                    JsonObject pObj = new JsonObject();
                    pObj.addProperty("name", p.name == null ? "" : p.name);
                    pObj.addProperty("enabled", p.enabled);
                    pObj.addProperty("keyCode", p.keyCode);
                    JsonArray eArr = new JsonArray();
                    for (com.aftertime.ratallofyou.UI.config.ConfigData.FastHotkeyEntry e : p.entries) {
                        JsonObject eObj = new JsonObject();
                        eObj.addProperty("label", e.label == null ? "" : e.label);
                        eObj.addProperty("command", e.command == null ? "" : e.command);
                        eArr.add(eObj);
                    }
                    pObj.add("entries", eArr);
                    arr.add(pObj);
                }
            }
            obj.add(FHK_PRESETS_KEY, arr);
            obj.addProperty(FHK_ACTIVE_KEY, com.aftertime.ratallofyou.UI.config.ConfigData.AllConfig.INSTANCE.FHK_ACTIVE_PRESET);
        } catch (Throwable ignored) {}

        try (Writer writer = new OutputStreamWriter(new FileOutputStream(CONFIG_FILE), StandardCharsets.UTF_8)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(obj, writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Notify AutoSell after saving config
        if (AutoSell.getInstance() != null) {
            AutoSell.getInstance().onConfigChanged();
        }
    }

    // Direct helpers to read/write FHK without touching ModConfig fields
    public static java.util.List<com.aftertime.ratallofyou.UI.config.ConfigData.FastHotkeyPreset> loadFhkPresets() {
        ensureDir();
        java.util.List<com.aftertime.ratallofyou.UI.config.ConfigData.FastHotkeyPreset> out = new java.util.ArrayList<>();
        if (!CONFIG_FILE.exists()) return out;
        try (Reader reader = new InputStreamReader(new FileInputStream(CONFIG_FILE), StandardCharsets.UTF_8)) {
            JsonElement rootEl = new JsonParser().parse(reader);
            if (!rootEl.isJsonObject()) return out;
            JsonObject obj = rootEl.getAsJsonObject();
            if (!obj.has(FHK_PRESETS_KEY)) return out;
            JsonArray arr = obj.getAsJsonArray(FHK_PRESETS_KEY);
            for (JsonElement el : arr) {
                if (!el.isJsonObject()) continue;
                JsonObject pObj = el.getAsJsonObject();
                String name = pObj.has("name") ? pObj.get("name").getAsString() : "Preset";
                com.aftertime.ratallofyou.UI.config.ConfigData.FastHotkeyPreset p = new com.aftertime.ratallofyou.UI.config.ConfigData.FastHotkeyPreset(name);
                p.enabled = pObj.has("enabled") && pObj.get("enabled").getAsBoolean();
                p.keyCode = pObj.has("keyCode") ? pObj.get("keyCode").getAsInt() : 0;
                if (pObj.has("entries") && pObj.get("entries").isJsonArray()) {
                    JsonArray eArr = pObj.getAsJsonArray("entries");
                    int idx = 0;
                    for (JsonElement ee : eArr) {
                        if (!ee.isJsonObject()) continue;
                        JsonObject eObj = ee.getAsJsonObject();
                        String label = eObj.has("label") ? eObj.get("label").getAsString() : "";
                        String cmd = eObj.has("command") ? eObj.get("command").getAsString() : "";
                        p.entries.add(new com.aftertime.ratallofyou.UI.config.ConfigData.FastHotkeyEntry(label, cmd, idx++));
                    }
                }
                out.add(p);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return out;
    }

    public static int loadFhkActiveIndex() {
        ensureDir();
        if (!CONFIG_FILE.exists()) return 0;
        try (Reader reader = new InputStreamReader(new FileInputStream(CONFIG_FILE), StandardCharsets.UTF_8)) {
            JsonElement rootEl = new JsonParser().parse(reader);
            if (!rootEl.isJsonObject()) return 0;
            JsonObject obj = rootEl.getAsJsonObject();
            if (obj.has(FHK_ACTIVE_KEY)) return obj.get(FHK_ACTIVE_KEY).getAsInt();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static void saveFhkPresets(java.util.List<com.aftertime.ratallofyou.UI.config.ConfigData.FastHotkeyPreset> presets, int activeIndex) {
        ensureDir();
        JsonObject obj;
        // Try to merge with existing file content
        try (Reader reader = new InputStreamReader(new FileInputStream(CONFIG_FILE), StandardCharsets.UTF_8)) {
            JsonElement rootEl = new JsonParser().parse(reader);
            obj = rootEl != null && rootEl.isJsonObject() ? rootEl.getAsJsonObject() : new JsonObject();
        } catch (Exception ignored) { obj = new JsonObject(); }

        // Write/update FHK section
        JsonArray arr = new JsonArray();
        if (presets != null) {
            for (com.aftertime.ratallofyou.UI.config.ConfigData.FastHotkeyPreset p : presets) {
                JsonObject pObj = new JsonObject();
                pObj.addProperty("name", p.name == null ? "" : p.name);
                pObj.addProperty("enabled", p.enabled);
                pObj.addProperty("keyCode", p.keyCode);
                JsonArray eArr = new JsonArray();
                for (com.aftertime.ratallofyou.UI.config.ConfigData.FastHotkeyEntry e : p.entries) {
                    JsonObject eObj = new JsonObject();
                    eObj.addProperty("label", e.label == null ? "" : e.label);
                    eObj.addProperty("command", e.command == null ? "" : e.command);
                    eArr.add(eObj);
                }
                pObj.add("entries", eArr);
                arr.add(pObj);
            }
        }
        obj.add(FHK_PRESETS_KEY, arr);
        obj.addProperty(FHK_ACTIVE_KEY, Math.max(0, activeIndex));

        // Save back to disk
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(CONFIG_FILE), StandardCharsets.UTF_8)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(obj, writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void ensureDir() {
        if (!CONFIG_DIR.exists()) CONFIG_DIR.mkdirs();
    }
}
