package com.theboss.kzeaddonfabric.render.widgets;

import com.theboss.kzeaddonfabric.KZEAddon;
import com.theboss.kzeaddonfabric.enums.Anchor;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public class TotalAmmoWidget extends AbstractWidget {
    private int color;
    private short alpha;

    public TotalAmmoWidget(float x, float y, float scale, Anchor windowAnchor, Anchor elementAnchor, int color, int alpha) {
        super(x, y, scale, windowAnchor, elementAnchor);
        this.color = color & 0xFFFFFF;
        this.alpha = (short) (alpha & 0xFF);
    }

    public void copy(TotalAmmoWidget other) {
        this.setX(other.getX());
        this.setY(other.getY());
        this.setScale(other.getScale());
        this.setWindowAnchor(other.getWindowAnchor());
        this.setElementAnchor(other.getElementAnchor());
        this.color = other.color;
        this.alpha = other.alpha;
    }

    @Override
    public int getColor() {
        return this.color;
    }

    @Override
    public short getAlpha() {
        return this.alpha;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public void setAlpha(short alpha) {
        this.alpha = alpha;
    }

    @Override
    public void render(int scaledWidth, int scaledHeight, TextRenderer textRenderer, MatrixStack matrices, float delta) {
        if (KZEAddon.kzeInfo.getTotalAmmo() == -1) return;
        super.render(scaledWidth, scaledHeight, textRenderer, matrices, delta);
    }

    @Override
    public Text getText() {
        return Text.of(String.valueOf(KZEAddon.kzeInfo.getTotalAmmo()));
    }
}
