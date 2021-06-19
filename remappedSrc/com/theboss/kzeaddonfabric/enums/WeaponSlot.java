package com.theboss.kzeaddonfabric.enums;

public enum WeaponSlot {
    PRIMARY(0), SECONDARY(1), MELEE(2);

    private final int id;

    WeaponSlot(int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }

    public static WeaponSlot valueOf(int id) {
        switch (id) {
            case 0:
                return PRIMARY;
            case 1:
                return SECONDARY;
            case 2:
                return MELEE;
            default:
                return null;
        }
    }
}
