package com.theboss.kzeaddonfabric.widgets;

import com.theboss.kzeaddonfabric.enums.Anchor;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;

public class Offset {
    public Anchor anchor;
    public float x;
    public float y;

    public Offset() {
        this(Anchor.LEFT_UP, 0.0F, 0.0F);
    }

    public Offset(Anchor anchor, float x, float y) {
        this.anchor = anchor;
        this.x = x;
        this.y = y;
    }

    public void apply(MatrixStack matrices, Window window) {
        matrices.translate(
                MathHelper.floor(window.getScaledWidth() * this.anchor.getX() + this.x),
                MathHelper.floor(window.getScaledHeight() * this.anchor.getY() + this.y),
                0
        );
    }

    public Offset copy() {
        return new Offset(this.anchor, this.x, this.y);
    }
}
