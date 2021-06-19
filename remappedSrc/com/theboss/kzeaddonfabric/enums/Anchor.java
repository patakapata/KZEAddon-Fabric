package com.theboss.kzeaddonfabric.enums;

public enum Anchor {
    LEFT_UP(0, 0.0F, 0.0F), MIDDLE_UP(1, 0.5F, 0.0F), RIGHT_UP(2, 1.0F, 0.0F),
    LEFT_MIDDLE(3, 0.0F, 0.5F), MIDDLE_MIDDLE(4, 0.5F, 0.5F), RIGHT_MIDDLE(5, 1.0F, 0.5F),
    LEFT_DOWN(6, 0.0F, 1.0F), MIDDLE_DOWN(7, 0.5F, 1.0F), RIGHT_DOWN(8, 1.0F, 1.0F);

    private final int id;
    private final float xFactor;
    private final float yFactor;

    Anchor(int id, float xFactor, float yFactor) {
        this.id = id;
        this.xFactor = xFactor;
        this.yFactor = yFactor;
    }

    public int getId() {
        return this.id;
    }

    public float getXFactor() {
        return this.xFactor;
    }

    public float getYFactor() {
        return this.yFactor;
    }
}
