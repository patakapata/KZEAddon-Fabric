package com.theboss.kzeaddonfabric.widgets.impl;

import com.mojang.blaze3d.platform.GlStateManager;
import com.theboss.kzeaddonfabric.enums.Anchor;
import com.theboss.kzeaddonfabric.utils.RenderUtils;
import com.theboss.kzeaddonfabric.widgets.AbstractTextureWidget;
import com.theboss.kzeaddonfabric.widgets.Offset;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public class RadialProgressTextureWidget extends AbstractTextureWidget {
    public static final Text NAME = Text.of("RadialProgressTextureWidget");
    private double progress;

    public RadialProgressTextureWidget(int texture, float scale, int color, int alpha, Offset offset, Anchor anchor, int size, float u1, float v1, float u2, float v2) {
        super(texture, scale, color, alpha, offset, anchor, size, u1, v1, u2, v2);
    }

    @Override
    public void render(MatrixStack matrices, float delta) {
        GlStateManager.bindTexture(this.texture);
        RenderUtils.drawRadialTexture(matrices, this.progress, this.size, this.u1, this.v1, this.u2, this.v2);
        GlStateManager.bindTexture(0);
    }

    @Override
    public Text getName() {
        return NAME;
    }

    @Override
    public boolean isBuiltIn() {
        return false;
    }
}
