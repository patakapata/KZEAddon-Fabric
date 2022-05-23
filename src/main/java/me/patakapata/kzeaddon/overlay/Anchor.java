package me.patakapata.kzeaddon.overlay;

public enum Anchor {
    LEFT_UP(0.0F, 0.0F), MIDDLE_UP(0.5F, 0.0F), RIGHT_UP(1.0F, 0.0F),
    LEFT_MIDDLE(0.0F, 0.5F), MIDDLE_MIDDLE(0.5F, 0.5F), RIGHT_MIDDLE(1.0F, 0.5F),
    LEFT_DOWN(0.0F, 1.0F), MIDDLE_DOWN(0.5F, 1.0F), RIGHT_DOWN(1.0F, 1.0F);

    private final float x;
    private final float y;

    Anchor(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float getX() {
        return this.x;
    }

    public float getY() {
        return this.y;
    }
}
