package com.theboss.kzeaddonfabric.widgets;

import com.theboss.kzeaddonfabric.enums.Anchor;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;

public class Offset {
    public Anchor windowAnchor;
    public float x;
    public float y;

    public Offset(Anchor windowAnchor, float x, float y) {
        this.windowAnchor = windowAnchor;
        this.x = x;
        this.y = y;
    }

    public void apply(MatrixStack matrices, Window window) {
        matrices.translate(
                MathHelper.floor(window.getScaledWidth() * this.windowAnchor.getX() + this.x),
                MathHelper.floor(window.getScaledHeight() * this.windowAnchor.getY() + this.y),
                0
        );
    }

    public Offset copy() {
        return new Offset(this.windowAnchor, this.x, this.y);
    }
}
