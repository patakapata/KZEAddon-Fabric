package com.theboss.kzeaddonfabric;

import com.theboss.kzeaddonfabric.enums.WeaponSlot;
import com.theboss.kzeaddonfabric.ingame.KillLog;
import com.theboss.kzeaddonfabric.ingame.Weapon;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

public class KZEInformation {
    private final Weapon primary;
    private final Weapon secondary;
    private final Weapon melee;
    public KillLog killLog;
    private boolean isHuman;
    private boolean isReloading;
    private int reloadProgressTick;
    private int reloadTimeTick;
    private double reloadProgress;
    private double lastReloadProgress;

    public static int lerp(int color1, int color2, double progress) {
        int[] array1 = parse(color1);
        int[] array2 = parse(color2);
        int[] diff = new int[]{array2[0] - array1[0], array2[1] - array1[1], array2[2] - array1[2]};

        array1[0] += diff[0] * progress;
        array1[1] += diff[1] * progress;
        array1[2] += diff[2] * progress;

        return parse(array1[0], array1[1], array1[2]);
    }

    public static int[] parse(int color) {
        return new int[]{
                color >> 16 & 0xFF,
                color >> 8 & 0xFF,
                color & 0xFF
        };
    }

    public static int parse(int red, int green, int blue) {
        return red >> 16 | green >> 8 | blue;
    }

    public KZEInformation() {
        this.killLog = new KillLog(100);

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
        this.lastReloadProgress = 0.0;
    }

    public void cancelReload() {
        this.isReloading = false;
        this.reloadTimeTick = 0;
        this.reloadProgressTick = 0;
        this.reloadProgress = 0.0;
        this.lastReloadProgress = 0.0;
    }

    public KillLog getKillLog() {
        return this.killLog;
    }

    public Weapon getMainHandWeapon() {
        WeaponSlot slot = WeaponSlot.valueOf(MinecraftClient.getInstance().player.inventory.selectedSlot);
        if (slot == null) return null;

        switch (slot) {
            case PRIMARY:
                return this.primary;
            case SECONDARY:
                return this.secondary;
            case MELEE:
                return this.melee;
            default:
                return null;
        }
    }

    public double getReloadProgress() {
        return MathHelper.lerp(MinecraftClient.getInstance().getTickDelta(), this.lastReloadProgress, this.reloadProgress);
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
            this.lastReloadProgress = 0;
        } else {
            this.lastReloadProgress = this.reloadProgress;
            this.reloadProgress = (double) this.reloadProgressTick / this.reloadTimeTick;
        }
    }

    @Deprecated
    protected void setReloadFromMainhandWeapon() {
        WeaponSlot slot = WeaponSlot.valueOf(MinecraftClient.getInstance().player.inventory.selectedSlot);
        if (slot == null) {
            KZEAddon.addChatLog(Text.of("§c§lInvalid weapon or slot!§r"));
            return;
        }

        this.isReloading = true;
        this.reloadProgressTick = 0;
        this.reloadTimeTick = this.getWeapon(slot).getReloadTime();
    }

    public void tick() {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;

        if (player == null) return;

        this.isHuman = (client.player.getScoreboardTeam() != null && client.player.getScoreboardTeam().getName().equals("e"));
        PlayerInventory inventory = player.inventory;

        this.primary.parse(inventory.getStack(WeaponSlot.PRIMARY.getId()));
        this.secondary.parse(inventory.getStack(WeaponSlot.SECONDARY.getId()));
        this.melee.parse(inventory.getStack(WeaponSlot.MELEE.getId()));

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
    }
}
