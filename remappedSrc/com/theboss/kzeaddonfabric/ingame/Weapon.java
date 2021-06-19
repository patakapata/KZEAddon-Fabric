package com.theboss.kzeaddonfabric.ingame;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.Text;

public class Weapon {
    private String name;
    private int maxMagazineAmmo;
    private int reloadTime;
    private int inMagazineAmmo;
    private int totalAmmo;
    private boolean isReloading;

    public Weapon() {
        this("", -1, -1, -1, -1, false);
    }

    public Weapon(String name, int inMagazineAmmo, int maxMagazineAmmo, int reloadTime, int totalAmmo, boolean isReloading) {
        this.name = name;
        this.maxMagazineAmmo = maxMagazineAmmo;
        this.reloadTime = reloadTime;

        this.inMagazineAmmo = inMagazineAmmo;
        this.totalAmmo = totalAmmo;
        this.isReloading = isReloading;
    }

    public void init() {
        this.name = "";
        this.inMagazineAmmo = -1;
        this.maxMagazineAmmo = -1;
        this.reloadTime = -1;
        this.totalAmmo = -1;
        this.isReloading = false;
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

    public boolean isReloading() {
        return this.isReloading;
    }

    public void setReloading(boolean reloading) {
        this.isReloading = reloading;
    }

    public double inMagazineAmmoPercentage() {
        return (double) this.inMagazineAmmo / this.maxMagazineAmmo;
    }

    public boolean isValid() {
        return !this.name.equals("") && this.inMagazineAmmo != -1 && this.maxMagazineAmmo != -1 && this.reloadTime != -1 && this.totalAmmo != -1;
    }

    // TODO アイテムから武器への変換
    public void parse(ItemStack item) {
        if (item.getItem().equals(Items.AIR)) {
            this.init();
            return;
        }

        String[] nameArray = item.getName().asString().split(" ");
        String[] lore = this.getLore(item);

        try {
            this.isReloading = nameArray[0].startsWith("§c");

            this.name = nameArray[0].substring(2);
            this.inMagazineAmmo = Integer.parseInt(nameArray[3].substring(4, nameArray[3].length() - 4));
            this.totalAmmo = Integer.parseInt(nameArray[6]);

            this.reloadTime = Integer.parseInt(lore[1].substring(22));
            this.maxMagazineAmmo = Integer.parseInt(lore[2].substring(20));
        } catch (Exception ex) {
            this.init();
        }
    }

    private String[] getLore(ItemStack item) {
        NbtCompound display = item.getSubTag("display");
        if (display == null) return null;
        NbtList loreTag = display.getList("Lore", 8);

        String[] lore = new String[loreTag.size()];
        for (int i = 0; i < loreTag.size(); i++) {
            lore[i] = Text.Serializer.fromJson(loreTag.getString(i)).asString();
        }

        return lore;
    }

    @Override
    public String toString() {
        return String.format("{Name: \"%s\", InMagazineAmmo: %d, MaxMagazineAmmo: %d, TotalAmmo: %d, ReloadTime: %d}", this.name, this.inMagazineAmmo, this.maxMagazineAmmo, this.totalAmmo, this.reloadTime);
    }
}
