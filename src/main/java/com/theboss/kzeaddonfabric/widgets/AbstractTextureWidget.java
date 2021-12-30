package com.theboss.kzeaddonfabric.widgets;

import com.mojang.blaze3d.platform.GlStateManager;
import com.theboss.kzeaddonfabric.enums.Anchor;

public abstract class AbstractTextureWidget implements Widget {
    protected int texture;
    protected float scale;
    protected int color;
    protected int alpha;
    protected Offset offset;
    protected Anchor anchor;
    protected float size;
    protected float u1;
    protected float v1;
    protected float u2;
    protected float v2;

    public AbstractTextureWidget(int texture, float scale, int color, int alpha, Offset offset, Anchor anchor, int size, float u1, float v1, float u2, float v2) {
        this.texture = texture;
        this.scale = scale;
        this.color = color & 0xFFFFFF;
        this.alpha = alpha & 0xFF;
        this.offset = offset;
        this.anchor = anchor;
        this.size = size;
        this.u1 = u1;
        this.v1 = v1;
        this.u2 = u2;
        this.v2 = v2;
    }

    protected void bindTexture() {
        GlStateManager.bindTexture(this.texture);
    }

    public int getTexture() {
        return this.texture;
    }

    public void setTexture(int texture) {
        this.texture = texture;
    }

    @Override
    public void setScale(float scale) {
        this.scale = scale;
    }

    @Override
    public float getScale() {
        return this.scale;
    }

    @Override
    public void setOffset(Offset offset) {
        this.offset = offset;
    }

    @Override
    public Offset getOffset() {
        return this.offset;
    }

    @Override
    public void setAnchor(Anchor anchor) {
        this.anchor = anchor;
    }

    @Override
    public Anchor getAnchor() {
        return this.anchor;
    }

    @Override
    public int getColor() {
        return this.color;
    }

    public void setColor(int color) {
        this.color = color & 0xFFFFFF;
    }

    @Override
    public int getAlpha() {
        return this.alpha;
    }

    @Override
    public float getWidth() {
        return this.size;
    }

    @Override
    public float getScaledWidth() {
        return this.getWidth();
    }

    @Override
    public float getHeight() {
        return this.size;
    }

    @Override
    public float getScaledHeight() {
        return this.getHeight();
    }

    public void setAlpha(int alpha) {
        this.alpha = alpha;
    }
}
