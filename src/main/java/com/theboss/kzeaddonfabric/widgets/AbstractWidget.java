package com.theboss.kzeaddonfabric.widgets;

import com.theboss.kzeaddonfabric.enums.Anchor;
import com.theboss.kzeaddonfabric.screen.WidgetArrangementScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;

public abstract class AbstractWidget implements Widget {
    private float x;
    private float y;
    private float scale;
    private Anchor windowAnchor;
    private Anchor elementAnchor;

    public AbstractWidget(float x, float y, float scale, Anchor windowAnchor, Anchor elementAnchor) {
        this.x = x;
        this.y = y;
        this.scale = scale;
        this.windowAnchor = windowAnchor;
        this.elementAnchor = elementAnchor;
    }

    @Override
    public float getX() {
        return this.x;
    }

    @Override
    public void setX(float x) {
        this.x = x;
    }

    @Override
    public float getY() {
        return this.y;
    }

    @Override
    public void setY(float y) {
        this.y = y;
    }

    @Override
    public float getScale() {
        return this.scale;
    }

    @Override
    public void setScale(float scale) {
        this.scale = scale;
    }

    @Override
    public Anchor getWindowAnchor() {
        return this.windowAnchor;
    }

    @Override
    public void setWindowAnchor(Anchor windowAnchor) {
        this.windowAnchor = windowAnchor;
    }

    @Override
    public Anchor getElementAnchor() {
        return this.elementAnchor;
    }

    @Override
    public void setElementAnchor(Anchor elementAnchor) {
        this.elementAnchor = elementAnchor;
    }

    @Override
    public void render(int scaledWidth, int scaledHeight, TextRenderer textRenderer, MatrixStack matrices, float delta) {
        float scale = this.getScale();
        matrices.push();
        matrices.translate((scaledWidth * this.getWindowAnchor().getXFactor()) + this.getX(), (scaledHeight * this.getWindowAnchor().getYFactor()) + this.getY(), 0);
        matrices.scale(scale, scale, scale);
        textRenderer.drawWithShadow(matrices, this.getText(), -(this.getWidth(textRenderer) * this.getElementAnchor().getXFactor()), -(textRenderer.fontHeight * this.getElementAnchor().getYFactor()), this.getColor() | this.getAlpha() << 24);
        matrices.pop();
    }

    public void openArrangementScreen() {
        WidgetArrangementScreen screen = new WidgetArrangementScreen(this);
        MinecraftClient mc = MinecraftClient.getInstance();

        screen.setParent(mc.currentScreen);
        screen.open(mc);
    }

    @Override
    public int getWidth(TextRenderer textRenderer) {
        return textRenderer.getWidth(this.getText());
    }
}
