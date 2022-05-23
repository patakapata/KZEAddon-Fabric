package me.patakapata.kzeaddon;

import me.patakapata.kzeaddon.weapon.InGameWeapon;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.profiler.Profiler;

public class KzeData {
    private static final KzeData INSTANCE = new KzeData();

    private static final int MAIN_WEAPON_SLOT = 0;
    private static final int SECONDARY_WEAPON_SLOT = 1;
    private static final int MELEE_WEAPON_SLOT = 2;

    private final InGameWeapon main = new InGameWeapon();
    private final InGameWeapon secondary = new InGameWeapon();
    private final InGameWeapon melee = new InGameWeapon();

    public static KzeData getInstance() {
        return INSTANCE;
    }

    private KzeData() {
    }

    public void onClientTick(MinecraftClient mc) {
        ClientPlayerEntity player = mc.player;
        Profiler profiler = mc.getProfiler();

        profiler.push("Parsing weapon data");
        if (player != null) {
            PlayerInventory inv = player.getInventory();
            this.main.loadFrom(inv.getStack(MAIN_WEAPON_SLOT));
            this.secondary.loadFrom(inv.getStack(SECONDARY_WEAPON_SLOT));
            this.melee.loadFrom(inv.getStack(MELEE_WEAPON_SLOT));
        }
        profiler.pop();
    }

    public boolean isMainWeaponValid() {
        MinecraftClient mc = MinecraftClient.getInstance();
        ClientPlayerEntity player = mc.player;
        return player != null && KzeUtils.isWeapon(player.getInventory().getStack(MAIN_WEAPON_SLOT));
    }

    public boolean isHoldMainWeapon() {
        MinecraftClient mc = MinecraftClient.getInstance();
        ClientPlayerEntity player = mc.player;

        return this.isMainWeaponValid() && player != null && player.getInventory().selectedSlot == MAIN_WEAPON_SLOT;
    }

    public InGameWeapon getMainWeapon() {
        return this.main;
    }

    public boolean isSecondaryWeaponValid() {
        MinecraftClient mc = MinecraftClient.getInstance();
        ClientPlayerEntity player = mc.player;
        return player != null && KzeUtils.isWeapon(player.getInventory().getStack(SECONDARY_WEAPON_SLOT));
    }

    public boolean isHoldSecondaryWeapon() {
        MinecraftClient mc = MinecraftClient.getInstance();
        ClientPlayerEntity player = mc.player;

        return this.isSecondaryWeaponValid() && player != null && player.getInventory().selectedSlot == SECONDARY_WEAPON_SLOT;
    }

    public InGameWeapon getSecondaryWeapon() {
        return this.secondary;
    }

    public boolean isMeleeWeaponValid() {
        MinecraftClient mc = MinecraftClient.getInstance();
        ClientPlayerEntity player = mc.player;
        return player != null && KzeUtils.isWeapon(player.getInventory().getStack(MELEE_WEAPON_SLOT));
    }

    public boolean isHoldMeleeWeapon() {
        MinecraftClient mc = MinecraftClient.getInstance();
        ClientPlayerEntity player = mc.player;

        return this.isMeleeWeaponValid() && player != null && player.getInventory().selectedSlot == MELEE_WEAPON_SLOT;
    }

    public InGameWeapon getMeleeWeapon() {
        return this.melee;
    }
}
