package com.theboss.kzeaddonfabric.render.widgets;

import com.theboss.kzeaddonfabric.KZEAddon;
import com.theboss.kzeaddonfabric.enums.Anchor;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public class ReloadIndicatorWidget extends Widget {
    public ReloadIndicatorWidget(Anchor widgetAnchor, Anchor windowAnchor, float scaleFactor, int offsetX, int offsetY, int opacity) {
        super(widgetAnchor, windowAnchor, scaleFactor, offsetX, offsetY, opacity);
    }

    public ReloadIndicatorWidget(Widget source) {
        super(source);
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
