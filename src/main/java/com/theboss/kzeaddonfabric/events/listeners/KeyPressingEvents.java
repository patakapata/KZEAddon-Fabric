package com.theboss.kzeaddonfabric.events.listeners;

import com.theboss.kzeaddonfabric.KZEAddon;
import com.theboss.kzeaddonfabric.Options;
import com.theboss.kzeaddonfabric.screen.DebugScreen;
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

        KZEAddon.info(body);
    }

    public static void onPressDebug(KeyBinding keyBinding) {
        new DebugScreen().open(MinecraftClient.getInstance());
    }

    public static void onPressHideTeammates(KeyBinding keyBinding) {
        Options options = KZEAddon.getOptions();
        options.isHideAllies = !options.isHideAllies;
        KZEAddon.info(new TranslatableText("feature.kzeaddon." + (options.isHideAllies ? "hide" : "show") + "_ally"));
    }

    public static void onPressUnStack(KeyBinding keyBinding) {
        ModUtils.sendCurrentPositionPacket();
        Optional.ofNullable(MinecraftClient.getInstance().player).ifPresent(player -> player.sendMessage(new TranslatableText("feature.kzeaddon.un_stack"), false));
    }
}
