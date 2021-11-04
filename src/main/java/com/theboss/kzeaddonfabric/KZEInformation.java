package com.theboss.kzeaddonfabric;

import com.theboss.kzeaddonfabric.enums.WeaponSlot;
import com.theboss.kzeaddonfabric.ingame.Weapon;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.profiler.Profiler;

import static com.theboss.kzeaddonfabric.KZEAddon.warn;

public class KZEInformation {
    private final MinecraftClient mc;
    private final Weapon primary;
    private final Weapon secondary;
    private final Weapon melee;
    private boolean isHuman;
    private boolean isReloading;
    private int reloadProgressTick;
    private int reloadTimeTick;
    private double reloadProgress;
    private int lastSelectedSlot;

    public KZEInformation(MinecraftClient mc) {
        this.mc = mc;

        this.isHuman = false;
        this.isReloading = false;
        this.reloadProgressTick = 0;
        this.reloadTimeTick = 0;
        this.reloadProgress = 0;

        this.primary = new Weapon();
        this.secondary = new Weapon();
        this.melee = new Weapon();
    }

    public void beginReload(Weapon weapon) {
        this.isReloading = true;
        this.reloadTimeTick = weapon.getReloadTime();
        this.reloadProgressTick = 0;
        this.reloadProgress = 0.0;
    }

    public void cancelReload() {
        this.isReloading = false;
        this.reloadTimeTick = 0;
        this.reloadProgressTick = 0;
        this.reloadProgress = 0.0;
    }

    public Weapon getMainHandWeapon() {
        if (this.mc.player == null) return null;
        WeaponSlot slot = WeaponSlot.valueOf(this.mc.player.inventory.selectedSlot);

        if (slot != null) {
            switch (slot) {
                case PRIMARY:
                    return this.primary;
                case SECONDARY:
                    return this.secondary;
                case MELEE:
                    return this.melee;
            }
        }

        return null;
    }

    public double getReloadProgress() {
        return this.reloadProgress;
    }

    public int getReloadTimeTick() {
        return this.reloadTimeTick;
    }

    public int getTotalAmmo() {
        int primary = this.primary.getTotalAmmo();
        int secondary = this.secondary.getTotalAmmo();
        int melee = this.melee.getTotalAmmo();
        int value = Integer.MAX_VALUE;

        if (primary != -1 && primary < value) value = primary;
        if (secondary != -1 && secondary < value) value = secondary;
        if (melee != -1 && melee < value) value = melee;
        if (value == Integer.MAX_VALUE) value = -1;

        return value;
    }

    public Weapon getWeapon(WeaponSlot slot) {
        switch (slot) {
            case PRIMARY:
                return this.primary;
            case SECONDARY:
                return this.secondary;
            case MELEE:
                return this.melee;
            default:
                throw new IllegalArgumentException("Invalid slot!");
        }
    }

    public boolean isHuman() {
        return this.isHuman;
    }

    public boolean isReloading() {
        return this.isReloading;
    }

    protected void reloadTick() {
        this.reloadProgressTick++;
        if (this.reloadProgressTick > this.reloadTimeTick) {
            this.isReloading = false;
            this.reloadTimeTick = 0;
            this.reloadProgressTick = 0;
            this.reloadProgress = 0;
        } else {
            this.reloadProgress = (double) this.reloadProgressTick / this.reloadTimeTick;
        }
    }

    @Deprecated
    protected void setReloadFromMainhandWeapon() {
        WeaponSlot slot = WeaponSlot.valueOf(this.mc.player != null ? this.mc.player.inventory.selectedSlot : 0);
        if (slot == null) {
            warn(Text.of("§lInvalid weapon or slot!§r"));
            return;
        }

        this.isReloading = true;
        this.reloadProgressTick = 0;
        this.reloadTimeTick = this.getWeapon(slot).getReloadTime();
    }

    public void tick() {
        MinecraftClient mc = this.mc;
        Profiler profiler = mc.getProfiler();
        ClientPlayerEntity player = mc.player;

        if (player == null || (mc.currentScreen != null && mc.currentScreen.isPauseScreen())) return;

        profiler.push("Check selected slot");
        this.isHuman = (mc.player.getScoreboardTeam() != null && mc.player.getScoreboardTeam().getName().equals("e"));
        PlayerInventory inventory = player.inventory;
        int selectedSlot = inventory.selectedSlot;
        if (this.lastSelectedSlot != selectedSlot) {
            this.lastSelectedSlot = selectedSlot;
            this.reloadProgressTick = 0;
            this.reloadProgress = 0.0;
        }

        profiler.swap("Parse weapons");
        profiler.push("Primary weapon");
        this.primary.newParser(inventory.getStack(WeaponSlot.PRIMARY.getId()));
        profiler.swap("Secondary weapon");
        this.secondary.newParser(inventory.getStack(WeaponSlot.SECONDARY.getId()));
        profiler.swap("Melee weapon");
        this.melee.newParser(inventory.getStack(WeaponSlot.MELEE.getId()));
        profiler.pop();

        profiler.swap("Check if reloading");
        Weapon handWeapon = this.getMainHandWeapon();
        if (handWeapon == null) {
            if (this.isReloading) this.cancelReload();
        } else {
            boolean isReloading = handWeapon.isReloading();
            if (!this.isReloading) {
                if (isReloading) this.beginReload(handWeapon);
            } else {
                if (!isReloading) this.cancelReload();
                else this.reloadTick();
            }
        }
        profiler.pop();
    }
}
