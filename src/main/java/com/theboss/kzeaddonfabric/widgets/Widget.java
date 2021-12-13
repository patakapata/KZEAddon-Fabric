package com.theboss.kzeaddonfabric.widgets;

import com.google.gson.*;
import com.theboss.kzeaddonfabric.enums.Anchor;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public interface Widget {
    default void transform(MatrixStack matrices, Window window) {
        float scale = this.getScale();
        this.getOffset().apply(matrices, window);
        matrices.scale(scale, scale, scale);
        matrices.translate(
                -this.getWidth() * this.getAnchor().getX(),
                -this.getHeight() * this.getAnchor().getY(),
                0
        );
    }

    void render(MatrixStack matrices, float delta);

    Text getName();

    void setScale(float scale);

    float getScale();

    void setOffset(Offset offset);

    Offset getOffset();

    void setAnchor(Anchor anchor);

    Anchor getAnchor();

    float getWidth();

    default float getScaledWidth() {
        return this.getWidth() * this.getScale();
    }

    float getHeight();

    default float getScaledHeight() {
        return this.getHeight() * this.getScale();
    }

    int getColor();

    int getAlpha();

    boolean isBuiltIn();

    class Serializer implements JsonSerializer<Widget>, JsonDeserializer<Widget> {
        private static final Map<String, Class<?>> REGISTERED_TYPES = new HashMap<>();
        private static final String CLASS_NAME = "type";
        private static final String INSTANCE = "data";

        public static void registerType(Class<?> type) {
            String name = type.getSimpleName();
            if (REGISTERED_TYPES.containsKey(name)) return;
            REGISTERED_TYPES.put(name, type);
        }

        @Override
        public JsonElement serialize(Widget src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject result = new JsonObject();
            String className = src.getClass().getSimpleName();
            result.addProperty(CLASS_NAME, className);
            JsonElement element = context.serialize(src);
            result.add(INSTANCE, element);
            return result;
        }

        @Override
        public Widget deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObj = json.getAsJsonObject();
            JsonPrimitive prim = (JsonPrimitive) jsonObj.get(CLASS_NAME);
            String className = prim.getAsString();

            if (!REGISTERED_TYPES.containsKey(className))
                throw new JsonParseException("Class not found. className=" + className);

            return context.deserialize(jsonObj.get(INSTANCE), REGISTERED_TYPES.get(className));
        }
    }
}
