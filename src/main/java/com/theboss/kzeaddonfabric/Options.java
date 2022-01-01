package com.theboss.kzeaddonfabric;

import com.google.gson.*;
import com.theboss.kzeaddonfabric.render.ChunkInstancedBarrierVisualizer;
import com.theboss.kzeaddonfabric.render.shader.BarrierShader;
import com.theboss.kzeaddonfabric.utils.Color;
import com.theboss.kzeaddonfabric.utils.Exclude;
import com.theboss.kzeaddonfabric.utils.ExcludeWithAnnotateStrategy;
import com.theboss.kzeaddonfabric.utils.ModUtils;
import net.minecraft.util.Util;
import net.minecraft.util.math.Vec3d;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Options {
    @Exclude
    private static final Gson GSON = Util.make(() -> new GsonBuilder()
            .setPrettyPrinting()
            .setExclusionStrategies(new ExcludeWithAnnotateStrategy())
            .registerTypeAdapter(Options.class, new OptionsTypeAdapter())
            .create()
    );
    @Exclude
    private static final Options DEFAULT = Util.make(() -> {
        Options options = new Options();
        options.restoreDefaultValues();
        return options;
    });

    @Exclude
    private final File optionFile;

    // 機能の 有効/無効
    // キルログ
    public boolean isShowKillLog;
    public boolean isHighlightMyKill;
    // 銃声の音量
    public boolean isIgnoreResourcePack;
    public boolean isChangeGunfireVolume;
    // バリア可視化
    public boolean isCrosshairVisualizeOrigin;
    public boolean isShowFriendlyInvisibles;
    public boolean isVisualizeBarriers;
    public boolean isBarrierFade;
    public boolean isShowChunkState;
    // その他
    public boolean isShowModLog;
    public boolean isHideAllies;
    public boolean isShowDebugConfig;

    // 各機能で使用される値
    // キルログ
    public float killLogScrollMultiplier;
    // バリア可視化
    public int barrierVisualizeRadius;
    public float barrierFadeRadius;
    public float barrierVisualizeRaycastDistance;
    public float barrierLineWidth;
    public Color barrierColor;
    // 銃声の音量
    public float gunfireVolumeMultiplier;
    // 発光色
    public Color obsessionGlowColor;
    public Color humanGlowColor;
    public Color zombieGlowColor;
    // その他
    public Vec3d cameraOffset;

    @SuppressWarnings("unchecked")
    public static <T> T getDefaultValue(String name) throws NoSuchFieldException, IllegalAccessException {
        return (T) Options.class.getDeclaredField(name).get(DEFAULT);
    }

    public static Class<?> getValueType(String name) throws NoSuchFieldException {
        return Options.class.getDeclaredField(name).getType();
    }

    private Options() {
        this.optionFile = null;
    }

    /**
     * @param configDir {@link net.minecraft.client.MinecraftClient#runDirectory} を渡せばok
     */
    public Options(File configDir) {
        this.optionFile = new File(configDir, "config.json").getAbsoluteFile();
        this.load();

        ChunkInstancedBarrierVisualizer.recordRenderCall(() -> BarrierShader.INSTANCE.setColor(this.barrierColor));
    }

    /**
     * 他の {@link Options} インスタンスから設定をコピーする
     */
    private void copy(Options other) {
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

    /**
     * ファイルから設定を読み込む
     *
     * @see #optionFile
     */
    public void load() {
        if (ModUtils.prepareIO(this.optionFile)) {
            Options loaded;
            try (FileReader reader = new FileReader(this.optionFile)) {
                loaded = GSON.fromJson(reader, Options.class);
            } catch (Exception ex) {
                loaded = new Options();
                loaded.restoreDefaultValues();
                KZEAddon.LOGGER.warn("Config is missing. restore default values");
            }
            if (loaded != null) {
                this.copy(loaded);
            }
        } else {
            KZEAddon.LOGGER.error("Can't create or read options file!");
            this.restoreDefaultValues();
        }
    }

    /**
     * 規定値の設定
     */
    public void restoreDefaultValues() {
        this.isShowKillLog = false;
        this.isShowModLog = false;
        this.isHighlightMyKill = true;
        this.isIgnoreResourcePack = true;
        this.isChangeGunfireVolume = true;
        this.isVisualizeBarriers = false;
        this.isCrosshairVisualizeOrigin = false;
        this.isHideAllies = false;
        this.isShowFriendlyInvisibles = false;
        this.isBarrierFade = false;
        this.isShowChunkState = false;

        this.gunfireVolumeMultiplier = 0.5F;
        this.obsessionGlowColor = new Color(0xFF0000);
        this.humanGlowColor = new Color(0x00AAAA);
        this.zombieGlowColor = new Color(0x00AA00);
        this.barrierVisualizeRadius = 1;
        this.barrierFadeRadius = 16F;
        this.barrierVisualizeRaycastDistance = 40.0F;
        this.barrierLineWidth = 2F;
        this.barrierColor = new Color(0xAA0000);
        this.cameraOffset = new Vec3d(0, 0, 0);
        this.killLogScrollMultiplier = 1.0F;
    }

    /**
     * ファイルへ設定を保存する
     *
     * @see #optionFile
     */
    public void save() {
        if (ModUtils.prepareIO(this.optionFile)) {
            try (FileWriter writer = new FileWriter(this.optionFile, false)) {
                writer.write(GSON.toJson(this));
            } catch (Exception ex) {
                KZEAddon.LOGGER.warn("Options file isn't exists");
            }
        } else {
            KZEAddon.LOGGER.error("Can't create or read options file!");
        }
    }


    private static class OptionsTypeAdapter implements JsonDeserializer<Options> {
        @SuppressWarnings("SameParameterValue")
        private List<String> declaredFields(Class<?> clazz, Predicate<Field> predicate) {
            List<String> list = new ArrayList<>();
            for (Field field : clazz.getDeclaredFields()) {
                if (predicate.test(field)) {
                    list.add(field.getName());
                }
            }

            return list;
        }

        @Override
        public Options deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            Options options = new Options();
            JsonObject jsonObj = json.getAsJsonObject();
            List<String> declaredFields = this.declaredFields(Options.class, it -> !this.shouldExclude(it));
            List<String> jsonEntries = this.jsonEntries(jsonObj);
            List<String> unknownFields = this.difference(jsonEntries, declaredFields);
            List<String> missingFields = this.difference(declaredFields, jsonEntries);

            if (!unknownFields.isEmpty())
                KZEAddon.LOGGER.error("Unknown fields: " + this.toString(unknownFields) + " is ignored");
            if (!missingFields.isEmpty())
                KZEAddon.LOGGER.error("Missing fields: " + this.toString(missingFields) + " is restored default values");

            try {
                for (String name : declaredFields) {
                    Object value;
                    if (missingFields.contains(name)) {
                        value = Options.getDefaultValue(name);
                    } else {
                        value = context.deserialize(jsonObj.get(name), Options.getValueType(name));
                    }
                    this.set(options, name, value);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                KZEAddon.LOGGER.error("Error while deserialize. config restore default values");
                options.restoreDefaultValues();
            }

            return options;
        }

        private List<String> difference(List<String> list1, List<String> list2) {
            return list1.stream().filter(it -> !list2.contains(it)).collect(Collectors.toList());
        }

        private List<String> jsonEntries(JsonObject jsonObj) {
            List<String> list = new ArrayList<>();
            for (Map.Entry<String, JsonElement> entry : jsonObj.entrySet()) {
                list.add(entry.getKey());
            }

            return list;
        }

        private void set(Options to, String name, Object value) throws NoSuchFieldException, IllegalAccessException {
            Field field = Options.class.getDeclaredField(name);
            field.set(to, value);
        }

        private boolean shouldExclude(Field field) {
            return field.getAnnotation(Exclude.class) != null;
        }

        public String toString(List<String> list) {
            StringBuilder builder = new StringBuilder();
            Iterator<String> itr = list.listIterator();
            while (itr.hasNext()) {
                builder.append(itr.next());
                if (itr.hasNext()) builder.append(", ");
            }

            return builder.toString();
        }
    }
}
