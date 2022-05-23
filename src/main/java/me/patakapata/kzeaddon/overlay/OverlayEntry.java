package me.patakapata.kzeaddon.overlay;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.util.function.Supplier;

public class OverlayEntry {
    private final Supplier<Boolean> shouldRender;
    private final Supplier<Text> textSupplier;
    private final Supplier<Integer> colorSupplier;
    private final Anchor windowAnchor;
    private final Anchor elementAnchor;
    private float x;
    private float y;

    public OverlayEntry(Supplier<Boolean> shouldRender, Supplier<Text> textSupplier, Supplier<Integer> colorSupplier, Anchor windowAnchor, Anchor elementAnchor, float x, float y) {
        this.shouldRender = shouldRender;
        this.textSupplier = textSupplier;
        this.colorSupplier = colorSupplier;
        this.windowAnchor = windowAnchor;
        this.elementAnchor = elementAnchor;
        this.x = x;
        this.y = y;
    }

    public Boolean getShouldRender() {
        return this.shouldRender.get();
    }

    public Text getText() {
        return this.textSupplier.get();
    }

    public Integer getColor() {
        return this.colorSupplier.get();
    }

    public Anchor getWindowAnchor() {
        return this.windowAnchor;
    }

    public Anchor getElementAnchor() {
        return this.elementAnchor;
    }

    public float getX(float delta) {
        return this.x;
    }

    public float getY(float delta) {
        return this.y;
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public void tick(MinecraftClient mc) {
    }
}
