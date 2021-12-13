package com.theboss.kzeaddonfabric.widgets;

import com.theboss.kzeaddonfabric.KZEAddon;
import com.theboss.kzeaddonfabric.enums.Anchor;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public class TotalAmmoWidget extends AbstractTextWidget {
    private int color;
    private short alpha;

    public TotalAmmoWidget(float scale, Offset offset, Anchor anchor, int color, int alpha) {
        super(scale, offset, anchor);
        this.color = color & 0xFFFFFF;
        this.alpha = (short) (alpha & 0xFF);
    }

    public void copy(TotalAmmoWidget other) {
        this.setOffset(other.getOffset());
        this.setScale(other.getScale());
        this.setAnchor(other.getAnchor());
        this.color = other.color;
        this.alpha = other.alpha;
    }

    @Override
    public boolean isBuiltIn() {
        return true;
    }

    @Override
    public Text getName() {
        return Text.of("Total ammo");
    }

    @Override
    public int getColor() {
        return this.color;
    }

    @Override
    public int getAlpha() {
        return this.alpha;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public void setAlpha(short alpha) {
        this.alpha = alpha;
    }

    @Override
    public void render(MatrixStack matrices, float delta) {
        if (KZEAddon.kzeInfo.getTotalAmmo() == -1) return;
        super.render(matrices, delta);
    }

    @Override
    public Text getText() {
        return Text.of(String.valueOf(KZEAddon.kzeInfo.getTotalAmmo()));
    }
}
