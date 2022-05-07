package com.theboss.kzeaddonfabric.widgets;

import com.theboss.kzeaddonfabric.enums.Anchor;
import com.theboss.kzeaddonfabric.utils.Exclude;
import com.theboss.kzeaddonfabric.widgets.api.Widget;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public abstract class AbstractTextWidget implements Widget {
    @Exclude
    public static TextRenderer textRenderer;

    private float scale;
    private Offset offset;
    private Anchor anchor;

    public AbstractTextWidget() {
        this(1.0F, new Offset(Anchor.LEFT_UP, 0.0F, 0.0F), Anchor.LEFT_UP);
    }

    public AbstractTextWidget(float scale, Offset offset, Anchor anchor) {
        this.scale = scale;
        this.offset = offset;
        this.anchor = anchor;
    }

    @Override
    public void render(MatrixStack matrices, float delta) {
        textRenderer.drawWithShadow(matrices,
                                    this.getText(),
                                    0,
                                    0,
                                    this.getColor() | this.getAlpha() << 24
        );
    }

    @Override
    public Offset getOffset() {
        return this.offset;
    }

    @Override
    public void setOffset(Offset offset) {
        this.offset = offset;
    }

    public abstract Text getText();

    @Override
    public Text getName() {
        return this.getText();
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
    public Anchor getAnchor() {
        return this.anchor;
    }

    @Override
    public void setAnchor(Anchor anchor) {
        this.anchor = anchor;
    }

    @Override
    public float getWidth() {
        return textRenderer.getWidth(this.getText());
    }

    @Override
    public float getHeight() {
        return textRenderer.fontHeight;
    }

    public int getColor() {
        return 0xFFFFFF;
    }

    public int getAlpha() {
        return 0xFF;
    }
}
