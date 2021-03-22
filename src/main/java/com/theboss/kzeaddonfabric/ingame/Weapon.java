package com.theboss.kzeaddonfabric.ingame;

public class Weapon {
    private String name;
    private int inMagazineAmmo;
    private int maxMagazineAmmo;
    private int reloadTime;

    public Weapon(String name, int inMagazineAmmo, int maxMagazineAmmo, int reloadTime) {
        this.name = name;
        this.inMagazineAmmo = inMagazineAmmo;
        this.maxMagazineAmmo = maxMagazineAmmo;
        this.reloadTime = reloadTime;
    }

    public String getName() {
        return name;
    }

    public int getInMagazineAmmo() {
        return inMagazineAmmo;
    }

    public int getMaxMagazineAmmo() {
        return maxMagazineAmmo;
    }

    public int getReloadTime() {
        return reloadTime;
    }
}
