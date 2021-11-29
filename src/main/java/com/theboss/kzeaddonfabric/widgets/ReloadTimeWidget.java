package com.theboss.kzeaddonfabric.widgets;

import com.theboss.kzeaddonfabric.KZEAddon;
import com.theboss.kzeaddonfabric.ingame.KZEInformation;
import com.theboss.kzeaddonfabric.enums.Anchor;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

public class ReloadTimeWidget extends AbstractWidget {
    private int color;
    private short alpha;
    private float lastDelta;
    private double lastProgress;

    public ReloadTimeWidget(float x, float y, float scale, Anchor windowAnchor, Anchor elementAnchor, int color, int alpha) {
        super(x, y, scale, windowAnchor, elementAnchor);
        this.color = color;
        this.alpha = (short) (alpha & 0xFF);
    }

    public void copy(ReloadTimeWidget other) {
        this.setX(other.getX());
        this.setY(other.getY());
        this.setScale(other.getScale());
        this.setWindowAnchor(other.getWindowAnchor());
        this.setElementAnchor(other.getElementAnchor());
        this.alpha = other.alpha;
        this.color = other.color;
    }

    @Override
    public int getColor() {
        return this.color;
    }

    @Override
    public short getAlpha() {
        return this.alpha;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public void setAlpha(short alpha) {
        this.alpha = alpha;
    }

    @Override
    public Text getText() {
        // Old method
        // -------------------------------------------------- //
        // KZEInformation kzeInfo = KZEAddon.kzeInfo;
        // double progress = kzeInfo.getReloadProgress();
        // Text text = Text.of(
        //         String.format("%.2f",
        //                 kzeInfo.getReloadTimeTick() * (1 - MathHelper.lerp(this.lastDelta, this.lastProgress, progress)) / 20.0
        //         )
        // );
        // this.lastProgress = progress;
        // return text;

        KZEInformation kzeInfo = KZEAddon.kzeInfo;
        double progress = kzeInfo.getReloadTimeTick() * (1.0 - kzeInfo.getReloadProgress());
        if (progress > this.lastProgress) this.lastProgress = progress;
        progress = MathHelper.lerp(this.lastDelta, this.lastProgress, progress);
        this.lastProgress = progress;

        return Text.of(String.format("%.2f", progress / 20));
    }

    @Override
    public void render(int scaledWidth, int scaledHeight, TextRenderer textRenderer, MatrixStack matrices, float delta) {
        if (!KZEAddon.kzeInfo.isReloading()) return;
        this.lastDelta = delta;
        super.render(scaledWidth, scaledHeight, textRenderer, matrices, delta);
    }
}
