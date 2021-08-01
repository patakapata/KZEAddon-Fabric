package com.theboss.kzeaddonfabric.render.widgets;

import com.google.gson.annotations.Expose;
import com.theboss.kzeaddonfabric.Color;
import com.theboss.kzeaddonfabric.KZEAddon;
import com.theboss.kzeaddonfabric.enums.Anchor;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

@SuppressWarnings("unused")
public abstract class Widget {
    @Expose
    private Anchor widgetAnchor;
    @Expose
    private Anchor windowAnchor;
    @Expose
    private int offsetX;
    @Expose
    private int offsetY;
    @Expose
    private float scaleFactor;
    @Expose
    private boolean visibility;
    @Expose
    private short opacity;

    private float lastWidgetWidth;
    private float lastWidgetHeight;
    private float lastWindowWidth;
    private float lastWindowHeight;

    public Widget(Anchor widgetAnchor, Anchor windowAnchor, float scaleFactor, int offsetX, int offsetY, int opacity) {
        this.widgetAnchor = widgetAnchor;
        this.windowAnchor = windowAnchor;
        this.scaleFactor = scaleFactor;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.visibility = true;
        this.opacity = (short) opacity;
        this.lastWidgetWidth = 0.0F;
    }

    public Widget(Widget source) {
        this(source.widgetAnchor, source.windowAnchor, source.scaleFactor, source.offsetX, source.offsetY, source.opacity);
        this.visibility = source.visibility;
    }

    public float getAbsoluteX(Text message, Window window, TextRenderer textRenderer) {
        float msgWidth = this.getWidth(message, textRenderer);
        int scaledWidth = window.getScaledWidth();
        float windowX = scaledWidth * this.windowAnchor.getXFactor();
        float widgetX = windowX - (msgWidth * this.widgetAnchor.getXFactor());

        return (widgetX + this.offsetX) / this.scaleFactor;
    }

    public float getAbsoluteY(Text message, Window window, TextRenderer textRenderer) {
        float msgHeight = this.getHeight(textRenderer);
        int scaledHeight = window.getScaledHeight();
        float windowY = scaledHeight * this.windowAnchor.getYFactor();
        float widgetY = windowY - (msgHeight * this.widgetAnchor.getYFactor());

        return (widgetY + this.offsetY) / this.scaleFactor;
    }

    public int getColor() {
        return this.opacity & 0xFF << 24 | 0xFF_FF_FF;
    }

    public abstract void setColor(Color color);

    private float getHeight(TextRenderer textRenderer) {
        this.lastWidgetHeight = textRenderer.fontHeight * this.scaleFactor;
        return this.lastWidgetHeight;
    }

    public boolean getIsVisible() {
        return this.isVisible();
    }

    public int getOffsetX() {
        return this.offsetX;
    }

    public void setOffsetX(int offsetX) {
        this.offsetX = offsetX;
    }

    public int getOffsetY() {
        return this.offsetY;
    }

    public void setOffsetY(int offsetY) {
        this.offsetY = offsetY;
    }

    public short getOpacity() {
        return this.opacity;
    }

    public void setOpacity(short opacity) {
        this.opacity = opacity;
    }

    public float getScaleFactor() {
        return this.scaleFactor;
    }

    public void setScaleFactor(float scaleFactor) {
        this.scaleFactor = scaleFactor;
    }

    public abstract Text getText();

    public Anchor getWidgetAnchor() {
        return this.widgetAnchor;
    }

    public void setWidgetAnchor(Anchor widgetAnchor) {
        this.widgetAnchor = widgetAnchor;
    }

    private float getWidth(Text message, TextRenderer textRenderer) {
        this.lastWidgetWidth = textRenderer.getWidth(message.asString()) * this.scaleFactor;
        return this.lastWidgetWidth;
    }

    public Anchor getWindowAnchor() {
        return this.windowAnchor;
    }

    public void setWindowAnchor(Anchor windowAnchor) {
        this.windowAnchor = windowAnchor;
    }

    public boolean isVisible() {
        return this.visibility && this.opacity > 0;
    }

    public void migrateWidgetAnchor(Anchor newWidgetAnchor) {
        if (!this.widgetAnchor.equals(newWidgetAnchor)) {
            float xFactorDiff = newWidgetAnchor.getXFactor() - this.widgetAnchor.getXFactor();
            float yFactorDiff = newWidgetAnchor.getYFactor() - this.widgetAnchor.getYFactor();

            this.offsetX -= (int) (this.lastWidgetWidth * xFactorDiff);
            this.offsetY -= (int) (this.lastWidgetHeight * yFactorDiff);

            this.widgetAnchor = newWidgetAnchor;
        }
    }

    public void migrateWindowAnchor(Anchor newWindowAnchor) {
        if (!this.windowAnchor.equals(newWindowAnchor)) {
            float xFactorDiff = newWindowAnchor.getXFactor() - this.windowAnchor.getXFactor();
            float yFactorDiff = newWindowAnchor.getYFactor() - this.windowAnchor.getYFactor();

            this.offsetX -= (int) (this.lastWindowWidth * xFactorDiff);
            this.offsetY -= (int) (this.lastWindowWidth * yFactorDiff);

            this.windowAnchor = newWindowAnchor;
        }
    }

    public void render(MatrixStack matrices, Window window, TextRenderer textRenderer) {
        if (!this.isVisible()) return;

        Text message = this.getText();

        float x = this.getAbsoluteX(message, window, textRenderer);
        float y = this.getAbsoluteY(message, window, textRenderer);

        if (KZEAddon.Options.isShouldAlignWidgetPosition()) {
            x = (float) Math.floor(x);
            y = (float) Math.floor(y);
        }

        matrices.push();
        matrices.scale(this.scaleFactor, this.scaleFactor, this.scaleFactor);
        textRenderer.drawWithShadow(matrices, message, x, y, this.getColor());
        matrices.pop();
    }

    public void setVisibility(boolean visibility) {
        this.visibility = visibility;
    }
}
