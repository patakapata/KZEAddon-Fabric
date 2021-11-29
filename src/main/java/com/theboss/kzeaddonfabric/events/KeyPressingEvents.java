package com.theboss.kzeaddonfabric.events;

import com.theboss.kzeaddonfabric.KZEAddon;
import com.theboss.kzeaddonfabric.render.shader.HoloWallShader;
import com.theboss.kzeaddonfabric.utils.ModUtils;
import com.theboss.kzeaddonfabric.utils.VanillaUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

public class KeyPressingEvents {
    public static void onPressAddObsessionTarget(KeyBinding keyBinding) {
        MinecraftClient mc = MinecraftClient.getInstance();
        List<UUID> obsessions = KZEAddon.getObsessions();
        if (mc.player == null) return;
        HitResult result = VanillaUtils.raycastIgnoreBlock(mc.player, 100.0, entity -> !entity.isSpectator() && entity.collides());
        LiteralText body = new LiteralText("Obsession > ");

        if (result.getType() == HitResult.Type.ENTITY) {
            Entity entity = ((EntityHitResult) result).getEntity();
            HoloWallShader.INSTANCE.setCenter(entity.getPos());
            UUID uuid = entity.getUuid();
            if (!obsessions.contains(uuid)) {
                obsessions.add(uuid);
                body.append("Target added (");
            } else {
                obsessions.remove(uuid);
                body.append("Target removed (");
            }
            body.append(entity.getDisplayName()).append(")");
        } else {
            body.append("Target not found");
        }

        KZEAddon.getModLog().info(body);
    }

    public static void onPressDebug(KeyBinding keyBinding) {
        KZEAddon.getModLog().info("Key feature implementing pending");
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
