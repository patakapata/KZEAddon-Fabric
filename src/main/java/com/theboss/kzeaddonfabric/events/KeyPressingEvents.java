package com.theboss.kzeaddonfabric.events;

import com.theboss.kzeaddonfabric.KZEAddon;
import com.theboss.kzeaddonfabric.ModUtils;
import com.theboss.kzeaddonfabric.VanillaUtils;
import com.theboss.kzeaddonfabric.screen.KillLogScreen;
import com.theboss.kzeaddonfabric.screen.Screen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class KeyPressingEvents {
    public static void onPressAddGlowTarget(KeyBinding keyBinding) {
        MinecraftClient mc = MinecraftClient.getInstance();
        List<UUID> glowPlayers = KZEAddon.getPriorityGlowPlayers();
        if (mc.player == null) return;
        HitResult result = VanillaUtils.raycastIgnoreBlock(mc.player, 100.0);

        if (result.getType() == HitResult.Type.ENTITY) {
            UUID uuid = ((EntityHitResult) result).getEntity().getUuid();
            if (!glowPlayers.contains(uuid)) {
                glowPlayers.add(uuid);
            } else {
                glowPlayers.remove(uuid);
            }
        }
    }

    public static void onPressDebug(KeyBinding keyBinding) {
        Screen screen = new KillLogScreen();
        screen.setParent(MinecraftClient.getInstance().currentScreen);
        screen.open(MinecraftClient.getInstance());
    }

    public static void onCamX(KeyBinding keyBinding) {
        KZEAddon.options.cameraOffset = KZEAddon.options.cameraOffset.add(VanillaUtils.isSneaking() ? 1 : -1, 0, 0);
    }

    public static void onCamY(KeyBinding keyBinding) {
        KZEAddon.options.cameraOffset = KZEAddon.options.cameraOffset.add(0, VanillaUtils.isSneaking() ? 1 : -1, 0);
    }

    public static void onCamZ(KeyBinding keyBinding) {
        KZEAddon.options.cameraOffset = KZEAddon.options.cameraOffset.add(0, 0, VanillaUtils.isSneaking() ? 1 : -1);
    }

    public static void onPressHideTeammates(KeyBinding keyBinding) {
        KZEAddon.options.shouldHideTeammates = !KZEAddon.options.shouldHideTeammates;
        KZEAddon.info(new TranslatableText("info.kzeaddon." + (KZEAddon.options.shouldHideTeammates ? "hide" : "show") + "_teammates"));
    }

    public static void onPressUnStack(KeyBinding keyBinding) {
        ModUtils.sendCurrentPositionPacket();
        Optional.ofNullable(MinecraftClient.getInstance().player).ifPresent(player -> player.sendMessage(new TranslatableText("feature.key.kzeaddon.un_stack"), false));
    }
}
