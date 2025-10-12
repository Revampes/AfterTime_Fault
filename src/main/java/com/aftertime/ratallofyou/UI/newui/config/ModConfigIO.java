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

    private static void ensureDir() {
        if (!CONFIG_DIR.exists()) CONFIG_DIR.mkdirs();
    }
}
