package me.patakapata.kzeaddon.weapon;

import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.Objects;

/**
 * KZEのゲーム中アイテムから取得できるの武器情報
 */
public class InGameWeapon implements Weapon {
    private static final TextColor RELOADING_COLOR = TextColor.fromFormatting(Formatting.RED);

    private String name;
    private int ammoInMagazine;
    private int remainingAmmo;
    private boolean isReloading;

    public InGameWeapon(ItemStack item) {
        this.loadFrom(item);
    }

    public InGameWeapon() {
        this("", 0, 0, false);
    }

    public InGameWeapon(String name, int ammoInMagazine, int remainingAmmo, boolean isReloading) {
        this.name = name;
        this.ammoInMagazine = ammoInMagazine;
        this.remainingAmmo = remainingAmmo;
        this.isReloading = isReloading;
    }

    public void loadFrom(ItemStack item) {
        try {
            Text name = item.getName();
            List<Text> siblings = name.getSiblings();

            String rawName = siblings.get(0).getString();
            String rawAmmoInMagazine = siblings.get(1).getString();
            String rawRemainingAmmo = siblings.get(2).getString();

            this.name = rawName.substring(0, rawName.length() - 3);
            this.ammoInMagazine = Integer.parseInt(rawAmmoInMagazine);
            this.remainingAmmo = Integer.parseInt(rawRemainingAmmo.substring(3));
            this.isReloading = Objects.equals(name.getStyle().getColor(), RELOADING_COLOR);
            return;
        } catch (Exception ignored) {}
        this.name = "";
        this.remainingAmmo = 0;
        this.ammoInMagazine = 0;
        this.isReloading = false;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public float getAttenuation() {
        return 0;
    }

    @Override
    public float maxRange() {
        return 0;
    }

    @Override
    public int getPenetrability() {
        return 0;
    }

    @Override
    public float getPiercingPercent() {
        return 0;
    }

    @Override
    public float scopeLevel() {
        return 0;
    }

    @Override
    public int getReloadDuration() {
        return 0;
    }

    @Override
    public int getDamage() {
        return 0;
    }

    @Override
    public int getHeadshotDamage() {
        return 0;
    }

    @Override
    public float getKnockBack() {
        return 0;
    }

    @Override
    public int getCooldown() {
        return 0;
    }

    @Override
    public float getSpeed() {
        return 0;
    }

    public int getAmmoInMagazine() {
        return this.ammoInMagazine;
    }

    public int getRemainingAmmo() {
        return this.remainingAmmo;
    }

    @Override
    public int getTotalAmmo() {
        return 0;
    }

    @Override
    public float getAccuracy() {
        return 0;
    }

    public boolean isReloading() {
        return this.isReloading;
    }
}
