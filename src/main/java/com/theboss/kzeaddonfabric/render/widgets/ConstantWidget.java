package com.theboss.kzeaddonfabric.render.widgets;

import com.theboss.kzeaddonfabric.Color;
import com.theboss.kzeaddonfabric.enums.Anchor;
import net.minecraft.text.Text;

public class ConstantWidget extends Widget {
    private Text text;
    private int color;

    public ConstantWidget(Anchor widgetAnchor, Anchor windowAnchor, float scaleFactor, int offsetX, int offsetY, int opacity) {
        super(widgetAnchor, windowAnchor, scaleFactor, offsetX, offsetY, opacity);
    }

    public ConstantWidget(ConstantWidget source) {
        super(source);

        this.text = source.text;
        this.color = source.color;
    }

    @Override
    public int getColor() {
        return this.getOpacity() << 24 | this.color;
    }

    @Override
    public void setColor(Color color) {
        this.color = color.get();
    }

    @Override
    public Text getText() {
        return this.text;
    }

    public void setText(Text text) {
        this.text = text;
    }
}
