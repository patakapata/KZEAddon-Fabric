package com.theboss.kzeaddonfabric.widgets.impl;

import com.theboss.kzeaddonfabric.enums.Anchor;
import com.theboss.kzeaddonfabric.widgets.AbstractTextWidget;
import com.theboss.kzeaddonfabric.widgets.Offset;
import net.minecraft.text.Text;

public class TextWidget extends AbstractTextWidget {
    private Text text;
    private int color;
    private int alpha;

    public TextWidget(float scale, Text text, Offset offset, Anchor anchor, int color, int alpha) {
        super(scale, offset, anchor);
        this.text = text;
        this.color = color & 0xFFFFFF;
        this.alpha = alpha & 0xFF;
    }

    @Override
    public boolean isBuiltIn() {
        return false;
    }

    public void setText(Text text) {
        this.text = text;
    }

    @Override
    public Text getText() {
        return this.text;
    }

    @Override
    public int getColor() {
        return this.color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    @Override
    public int getAlpha() {
        return this.alpha;
    }

    public void setAlpha(int alpha) {
        this.alpha = alpha;
    }
}
