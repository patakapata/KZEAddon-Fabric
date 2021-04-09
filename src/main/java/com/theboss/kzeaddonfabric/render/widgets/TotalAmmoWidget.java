package com.theboss.kzeaddonfabric.render.widgets;

import com.theboss.kzeaddonfabric.KZEAddonFabric;
import com.theboss.kzeaddonfabric.enums.Anchor;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public class TotalAmmoWidget extends Widget {
    public TotalAmmoWidget(Anchor widgetAnchor, Anchor windowAnchor, float scaleFactor, int offsetX, int offsetY, int opacity) {
        super(widgetAnchor, windowAnchor, scaleFactor, offsetX, offsetY, opacity);
    }

    public TotalAmmoWidget(Widget source) {
        super(source);
    }

    @Override
    public Text getText() {
        return Text.of(Integer.toString(KZEAddonFabric.KZE_INFO.getTotalAmmo()));
    }

    @Override
    public void render(MatrixStack matrices, Window window, TextRenderer textRenderer) {
        if (KZEAddonFabric.KZE_INFO.getTotalAmmo() == -1) return;
        super.render(matrices, window, textRenderer);
    }
}
