package me.patakapata.kzeaddon.overlay;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.util.function.Supplier;

class SlideEntry extends OverlayEntry {
    private final float x1;
    private final float y1;
    private final float x2;
    private final float y2;
    private float lastX;
    private float lastY;
    private int slideTime = 2;
    private int showTime;

    SlideEntry(
            Supplier<Boolean> shouldRender,
            Supplier<Text> textSupplier,
            Supplier<Integer> colorSupplier,
            Anchor windowAnchor,
            Anchor elementAnchor,
            float x1,
            float y1,
            float x2,
            float y2
    ) {
        super(shouldRender, textSupplier, colorSupplier, windowAnchor, elementAnchor, x1, y1);
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }

    @Override
    public void tick(MinecraftClient mc) {
        this.lastX = this.getX(1);
        this.lastY = this.getY(1);

        boolean show = super.getShouldRender();
        if (show) {
            if (this.showTime < this.slideTime) {
                this.showTime++;
            }
        } else {
            if (this.showTime > 0)
                this.showTime--;
        }

        float progress = this.getProgress();

        this.setX(show ? this.x2 : this.x1);
        this.setY(MathHelper.lerp(progress, this.y1, this.y2));
    }

    public float getProgress() {
        return this.showTime / (float) this.slideTime;
    }

    @Override
    public Boolean getShouldRender() {
        return super.getShouldRender() || this.showTime > 0;
    }

    @Override
    public float getX(float delta) {
        return MathHelper.lerp(delta, this.lastX, super.getX(1));
    }

    @Override
    public float getY(float delta) {
        return MathHelper.lerp(delta, this.lastY, super.getY(1));
    }

    @Override
    public Integer getColor() {
        return ((int) (this.getProgress() * 255) << 24) | 0xFFFFFF;
    }

    public int getSlideTime() {
        return this.slideTime;
    }

    public void setSlideTime(int slideTime) {
        this.slideTime = slideTime;
    }
}
