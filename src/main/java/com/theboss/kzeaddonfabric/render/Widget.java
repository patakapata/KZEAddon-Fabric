package com.theboss.kzeaddonfabric.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.util.function.Supplier;

@SuppressWarnings("unused")
public class Widget {
    private Supplier<Text> supplier;
    private Anchor widgetAnchor;
    private Anchor windowAnchor;
    private int offsetX;
    private int offsetY;
    private float scaleFactor;
    private Supplier<Integer> color;

    public Widget(Supplier<Text> supplier, Anchor widgetAnchor, Anchor windowAnchor, float scaleFactor, Supplier<Integer> color, int offsetX, int offsetY) {
        this.supplier = supplier;
        this.widgetAnchor = widgetAnchor;
        this.windowAnchor = windowAnchor;
        this.scaleFactor = scaleFactor;
        this.color = color;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

    public Widget(Widget source) {
        this(source.supplier, source.widgetAnchor, source.windowAnchor, source.scaleFactor, source.color, source.offsetX, source.offsetY);
    }

    public Text getMessage() {
        return this.supplier.get();
    }

    public float getWidth(Text message, TextRenderer textRenderer) {
        return textRenderer.getWidth(message.asString()) * this.scaleFactor;
    }

    public float getHeight(TextRenderer textRenderer) {
        return textRenderer.fontHeight * this.scaleFactor;
    }

    public float getAbsoluteX(Text message, Window window, TextRenderer textRenderer) {
        float msgWidth = this.getWidth(message, textRenderer);
        int scaledWidth = window.getScaledWidth();
        float windowX = scaledWidth * this.windowAnchor.getXFactor();
        float widgetX = windowX - (msgWidth * this.widgetAnchor.getXFactor());

        return widgetX + this.offsetX;
    }

    public float getAbsoluteY(Text message, Window window, TextRenderer textRenderer) {
        float msgHeight = this.getHeight(textRenderer);
        int scaledHeight = window.getScaledHeight();
        float windowY = scaledHeight * this.windowAnchor.getYFactor();
        float widgetY = windowY - (msgHeight * this.widgetAnchor.getYFactor());

        return widgetY + this.offsetY;
    }

    public void render(MatrixStack matrices, Window window, TextRenderer textRenderer) {
        Text message = this.getMessage();

        float x = this.getAbsoluteX(message, window, textRenderer);
        float y = this.getAbsoluteY(message, window, textRenderer);

        RenderSystem.scalef(this.scaleFactor, this.scaleFactor, this.scaleFactor);
        textRenderer.drawWithShadow(matrices, message, x, y, this.color.get());
        RenderSystem.scalef(1F, 1F, 1F);
    }

    public double getScaleFactor() {
        return scaleFactor;
    }

    public void setScaleFactor(float scaleFactor) {
        this.scaleFactor = scaleFactor;
    }

    public Supplier<Text> getSupplier() {
        return this.supplier;
    }

    public void setSupplier(Supplier<Text> supplier) {
        this.supplier = supplier;
    }

    public Anchor getWidgetAnchor() {
        return widgetAnchor;
    }

    public void setWidgetAnchor(Anchor widgetAnchor) {
        this.widgetAnchor = widgetAnchor;
    }

    public Anchor getWindowAnchor() {
        return windowAnchor;
    }

    public void setWindowAnchor(Anchor windowAnchor) {
        this.windowAnchor = windowAnchor;
    }

    public int getOffsetX() {
        return offsetX;
    }

    public void setOffsetX(int offsetX) {
        this.offsetX = offsetX;
    }

    public int getOffsetY() {
        return offsetY;
    }

    public void setOffsetY(int offsetY) {
        this.offsetY = offsetY;
    }
}
