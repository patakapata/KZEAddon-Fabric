package com.theboss.kzeaddonfabric.ingame;

import net.minecraft.item.ItemStack;

public class Weapon {
    private String name;
    private int maxMagazineAmmo;
    private int reloadTime;
    private int inMagazineAmmo;
    private int totalAmmo;

    public Weapon() {
        this("", -1, -1, -1, -1);
    }

    public Weapon(String name, int inMagazineAmmo, int maxMagazineAmmo, int reloadTime, int totalAmmo) {
        this.name = name;
        this.maxMagazineAmmo = maxMagazineAmmo;
        this.reloadTime = reloadTime;

        this.inMagazineAmmo = inMagazineAmmo;
        this.totalAmmo = totalAmmo;
    }

    public void init() {
        this.name = "";
        this.inMagazineAmmo = -1;
        this.maxMagazineAmmo = -1;
        this.reloadTime = -1;
        this.totalAmmo = -1;
    }

    public int getTotalAmmo() {
        return this.totalAmmo;
    }

    public String getName() {
        return this.name;
    }

    public int getInMagazineAmmo() {
        return this.inMagazineAmmo;
    }

    public int getMaxMagazineAmmo() {
        return this.maxMagazineAmmo;
    }

    public int getReloadTime() {
        return this.reloadTime;
    }

    public double inMagazineAmmoPercentage() {
        return (double) this.inMagazineAmmo / this.maxMagazineAmmo;
    }

    public boolean isValid() {
        return !this.name.equals("") && this.inMagazineAmmo != -1 && this.maxMagazineAmmo != -1 && this.reloadTime != -1 && this.totalAmmo != -1;
    }

    // TODO アイテムから武器への変換
    public void parse(ItemStack item) {
        String[] name = item.getName().asString().split(" ");

        try {
            this.name = name[0];
            this.maxMagazineAmmo = Integer.parseInt(name[1]);
            this.reloadTime = Integer.parseInt(name[2]);
            this.inMagazineAmmo = Integer.parseInt(name[3]);
            this.totalAmmo = Integer.parseInt(name[4]);
        } catch (Exception ex) {
            this.init();
        }
    }
}
