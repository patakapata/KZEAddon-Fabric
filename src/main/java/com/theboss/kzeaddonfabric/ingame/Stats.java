package com.theboss.kzeaddonfabric.ingame;

import com.google.gson.*;
import com.theboss.kzeaddonfabric.utils.Exclude;
import com.theboss.kzeaddonfabric.utils.ExcludeWithAnnotateStrategy;
import com.theboss.kzeaddonfabric.KZEAddon;
import net.minecraft.util.Util;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Stats {
    @Exclude
    private static final Gson GSON = Util.make(() -> new GsonBuilder()
            .setPrettyPrinting()
            .setExclusionStrategies(new ExcludeWithAnnotateStrategy())
            .registerTypeAdapter(Stats.class, new Deserializer())
            .create()
    );

    @Exclude
    private final File saveTarget;
    // -------------------------------------------------- //
    // 勝利数
    private int winAsHuman;
    private int winAsZombie;
    // -------------------------------------------------- //
    // キル数
    private int totalKillCount;
    private int killByInfect;
    private int killByWeapon;

    public Stats() {
        this.saveTarget = null;
    }

    public Stats(File configDir) {
        this.saveTarget = new File(configDir, "stats.json");
        this.load();
    }

    public void load() {
        try (FileReader reader = new FileReader(this.saveTarget)) {
            if (!this.saveTarget.exists()) {
                //noinspection ResultOfMethodCallIgnored
                this.saveTarget.getParentFile().mkdirs();
                if (!this.saveTarget.createNewFile()) throw new IOException();
            } else {
                Stats loaded = GSON.fromJson(reader, this.getClass());
                this.copy(loaded);
            }
            KZEAddon.LOGGER.info("Stats is loaded");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void save() {
        try (FileWriter writer = new FileWriter(this.saveTarget, false)) {
            if (!this.saveTarget.exists()) {
                //noinspection ResultOfMethodCallIgnored
                this.saveTarget.getParentFile().mkdirs();
                if (!this.saveTarget.createNewFile()) throw new IOException();
            }

            writer.write(GSON.toJson(this));
            KZEAddon.LOGGER.info("Stats is saved");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void copy(Stats other) {
        try {
            for (Field field : this.getClass().getDeclaredFields()) {
                if (field.getAnnotation(Exclude.class) == null) {
                    field.set(this, field.get(other));
                }
            }
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
        }
    }

    public int getWinAsHuman() {
        return this.winAsHuman;
    }

    public int getWinAsZombie() {
        return this.winAsZombie;
    }

    public int getTotalKillCount() {
        return this.totalKillCount;
    }

    public int getKillByInfect() {
        return this.killByInfect;
    }

    public int getKillByWeapon() {
        return this.killByWeapon;
    }

    private static class Deserializer implements JsonDeserializer<Stats> {
        @Override
        public Stats deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObj = json.getAsJsonObject();
            List<String> declared = Arrays.stream(Stats.class.getDeclaredFields()).filter(it -> it.getAnnotation(Exclude.class) == null).map(Field::getName).collect(Collectors.toList());
            List<String> entries = jsonObj.entrySet().stream().map(Map.Entry::getKey).collect(Collectors.toList());

            Stats result = new Stats();
            try {
                for (String key : declared) {
                    if (!entries.contains(key)) continue;
                    this.set(result, key, jsonObj.get(key).getAsInt());
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            return result;
        }

        private void set(Object instance, String name, Object value) throws IllegalAccessException, NoSuchFieldException {
            Field field = instance.getClass().getDeclaredField(name);
            field.setAccessible(true);
            field.set(instance, value);
        }
    }
}
