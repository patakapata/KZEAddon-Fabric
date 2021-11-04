package com.theboss.kzeaddonfabric.render.widgets;

import com.google.gson.*;
import com.theboss.kzeaddonfabric.KZEAddon;
import com.theboss.kzeaddonfabric.enums.Anchor;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.lang.reflect.Type;

public interface Widget {
    void render(int scaledWidth, int scaledHeight, TextRenderer textRenderer, MatrixStack matrices, float delta);

    /**
     * ウィジェットの色を取得
     *
     * @return 0xRRGGBB
     */
    int getColor();

    /**
     * ウィジェットの不透明度を取得
     *
     * @return 0xAA
     */
    short getAlpha();

    /**
     * ウィジェットの拡大率を取得
     *
     * @return ウィジェットの拡大率
     */
    float getScale();

    /**
     * ウィジェットのテキストを取得
     *
     * @return ウィジェットのテキスト
     */
    Text getText();

    /**
     * ウィジェットのウィンドウアンカーを取得
     *
     * @return ウィジェットのウィンドウアンカー
     */
    Anchor getWindowAnchor();

    /**
     * ウィジェットのエレメントアンカーを取得
     *
     * @return ウィジェットのエレメントアンカー
     */
    Anchor getElementAnchor();

    /**
     * テキストの幅を取得
     *
     * @param textRenderer テキストレンダラー
     * @return テキストの幅
     */
    int getWidth(TextRenderer textRenderer);

    void setX(float x);

    float getX();

    void setY(float y);

    float getY();

    void setScale(float scale);

    void setWindowAnchor(Anchor anchor);

    void setElementAnchor(Anchor anchor);

    class InstanceCreator implements com.google.gson.InstanceCreator<Widget> {

        @Override
        public Widget createInstance(Type type) {
            KZEAddon.LOGGER.info("Instance creator : " + type.getTypeName());
            return new LiteralWidget();
        }
    }

    class Serializer implements JsonSerializer<Widget>, JsonDeserializer<Widget> {
        private static final String CLASS_NAME = "type";
        private static final String INSTANCE = "data";

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

            Class<?> klass;
            switch (className) {
                case "LiteralWidget":
                    klass = LiteralWidget.class;
                    break;
                case "WeaponWidget":
                    klass = WeaponWidget.class;
                    break;
                default:
                    throw new JsonParseException("Class not found. className=" + className);
            }

            return context.deserialize(jsonObj.get(INSTANCE), klass);
        }
    }
}
