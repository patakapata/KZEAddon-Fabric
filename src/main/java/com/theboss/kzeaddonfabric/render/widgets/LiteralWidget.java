package com.theboss.kzeaddonfabric.render.widgets;

import com.theboss.kzeaddonfabric.enums.Anchor;
import net.minecraft.text.Text;

public class LiteralWidget extends AbstractWidget {
    private Text text;
    private int color;
    private short alpha;

    public LiteralWidget(Text text, float x, float y, float scale, int color, int alpha, Anchor windowAnchor, Anchor elementAnchor) {
        super(x, y, scale, windowAnchor, elementAnchor);
        this.text = text;
        this.color = color & 0xFFFFFF;
        this.alpha = (short) (alpha & 0xFF);
    }

    public LiteralWidget(Text text) {
        this(text, 0F, 0F, 1F, 0xFFFFFF, 0xFF, Anchor.MIDDLE_MIDDLE, Anchor.MIDDLE_MIDDLE);
    }

    /**
     * シリアライズ / デシリアライズ 用
     */
    public LiteralWidget() {
        super(0, 0, 0, null, null);
    }

    @Override
    public Text getText() {
        return this.text;
    }

    public void setText(Text text) {
        this.text = text;
    }

    @Override
    public int getColor() {
        return this.color & 0xFFFFFF;
    }

    public void setColor(int color) {
        this.color = color;
    }

    @Override
    public short getAlpha() {
        return this.alpha <= 20 ? 0 : this.alpha;
    }

    public void setAlpha(int alpha) {
        this.alpha = (byte) (alpha & 0xFF);
    }
}
