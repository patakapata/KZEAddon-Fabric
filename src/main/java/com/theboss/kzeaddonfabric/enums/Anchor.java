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

    public Anchor getById(int id) {
        Anchor[] values = values();
        if (id < 0 || id >= values.length) throw new ArrayIndexOutOfBoundsException(id + " is out of bounds!");
        return values[id];
    }

    public int getId() {
        return this.id;
    }

    public float getX() {
        return this.xFactor;
    }

    public float getY() {
        return this.yFactor;
    }

    public Anchor next() {
        Anchor[] values = values();

        return values[(this.id + 1) % values.length];
    }

    public Anchor prev() {
        Anchor[] values = values();

        return values[(values.length + this.id - 1) % values.length];
    }
}
