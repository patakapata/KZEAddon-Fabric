package com.theboss.kzeaddonfabric.widgets.impl;

import com.theboss.kzeaddonfabric.KZEAddon;
import com.theboss.kzeaddonfabric.enums.Anchor;
import com.theboss.kzeaddonfabric.ingame.KZEInformation;
import com.theboss.kzeaddonfabric.utils.Exclude;
import com.theboss.kzeaddonfabric.widgets.AbstractTextWidget;
import com.theboss.kzeaddonfabric.widgets.Offset;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

public class ReloadTimeWidget extends AbstractTextWidget {
    private int color;
    private short alpha;
    @Exclude
    private float lastDelta;
    @Exclude
    private double lastProgress;

    public ReloadTimeWidget(float scale, Offset offset, Anchor anchor, int color, int alpha) {
        super(scale, offset, anchor);
        this.color = color;
        this.alpha = (short) (alpha & 0xFF);
    }

    public void copy(ReloadTimeWidget other) {
        this.setOffset(other.getOffset());
        this.setScale(other.getScale());
        this.setAnchor(other.getAnchor());
        this.alpha = other.alpha;
        this.color = other.color;
    }

    @Override
    public boolean isBuiltIn() {
        return true;
    }

    @Override
    public Text getName() {
        return Text.of("Reload time");
    }

    @Override
    public int getColor() {
        return this.color;
    }

    @Override
    public int getAlpha() {
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

        KZEInformation kzeInfo = KZEAddon.getKZEInfo();
        double progress = kzeInfo.getReloadTimeTick() / 20.0 * (1.0 - kzeInfo.getReloadProgress());
        if (progress > this.lastProgress) this.lastProgress = progress;
        progress = MathHelper.lerp(this.lastDelta, this.lastProgress, progress);
        this.lastProgress = progress;

        return Text.of(String.format("%.2f", progress));
    }

    @Override
    public void render(MatrixStack matrices, float delta) {
        if (!KZEAddon.getKZEInfo().isReloading()) return;
        this.lastDelta = delta;
        super.render(matrices, delta);
    }
}
