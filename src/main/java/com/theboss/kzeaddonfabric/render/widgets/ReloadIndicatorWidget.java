package com.theboss.kzeaddonfabric.render.widgets;

import com.google.gson.annotations.Expose;
import com.theboss.kzeaddonfabric.Color;
import com.theboss.kzeaddonfabric.KZEAddon;
import com.theboss.kzeaddonfabric.enums.Anchor;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public class ReloadIndicatorWidget extends Widget {
    @Expose
    private int color;

    public ReloadIndicatorWidget(Anchor widgetAnchor, Anchor windowAnchor, float scaleFactor, int offsetX, int offsetY, int opacity, int color) {
        super(widgetAnchor, windowAnchor, scaleFactor, offsetX, offsetY, opacity);
        this.color = color;
    }

    public ReloadIndicatorWidget(Widget source) {
        super(source);
    }

    @Override
    public int getColor() {
        return this.getOpacity() << 24 | this.color;
    }

    @Override
    public void setColor(Color color) {
        this.color = color.get();
    }

    protected String getReloadProgress() {
        double progress = 1 - KZEAddon.KZE_INFO.getReloadProgress();
        double seconds = KZEAddon.KZE_INFO.getReloadTimeTick() / 20.0;
        return String.format("%.2f", seconds * progress);
    }

    @Override
    public Text getText() {
        return Text.of(this.getReloadProgress());
    }

    @Override
    public void render(MatrixStack matrices, Window window, TextRenderer textRenderer) {
        if (KZEAddon.KZE_INFO.getReloadProgress() == 0.0) return;
        super.render(matrices, window, textRenderer);
    }
}
