package com.theboss.kzeaddonfabric.events;

import com.mojang.datafixers.util.Pair;
import com.theboss.kzeaddonfabric.utils.Color;
import com.theboss.kzeaddonfabric.KZEAddon;
import com.theboss.kzeaddonfabric.Options;
import com.theboss.kzeaddonfabric.utils.VanillaUtils;
import com.theboss.kzeaddonfabric.ingame.Weapon;
import com.theboss.kzeaddonfabric.render.ChunkInstancedBarrierVisualizer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;

public class PacketListener {
    public static final String HUMAN_TEAM_NAME = "e";
    public static final String ZOMBIE_TEAM_NAME = "z";

    @SuppressWarnings("unused")
    public static void onBlockUpdate(BlockUpdateS2CPacket packet) {
        EventsListener.onBlockUpdate(packet.getPos(), packet.getState());
    }

    public static void onChunkDeltaUpdate(ChunkDeltaUpdateS2CPacket packet) {
        packet.visitUpdates(EventsListener::onBlockUpdate);
    }

    @SuppressWarnings("unused")
    public static void onCustomPayload(CustomPayloadS2CPacket packet) {
        Identifier channel = packet.getChannel();
        PacketByteBuf buffer = packet.getData();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < buffer.capacity(); i++) builder.append((char) buffer.readByte());

        KZEAddon.info("CustomPayload > " + channel.getPath() + ":" + channel.getNamespace() + " | " + builder.toString());
    }

    @SuppressWarnings("unused")
    public static void onEquipmentUpdate(EntityEquipmentUpdateS2CPacket packet) {
        ClientWorld world = Objects.requireNonNull(MinecraftClient.getInstance().world);
        Entity entity = world.getEntityById(packet.getId());
        List<Pair<EquipmentSlot, ItemStack>> equipmentList = packet.getEquipmentList();

        if (entity != null && entity.getType().equals(EntityType.PLAYER)) {
            Optional<Pair<EquipmentSlot, ItemStack>> optional = equipmentList.stream().filter(it -> it.getFirst() == EquipmentSlot.MAINHAND).findFirst();
            if (optional.isPresent()) {
                ItemStack item = optional.get().getSecond();
                Weapon weapon = new Weapon();
                weapon.newParser(item);

                KZEAddon.info(" - Reloading : " + (weapon.isReloading() ? "yes" : "no"));
                KZEAddon.info(VanillaUtils.textAsString(entity.getName()) + " > " + weapon.getInMagazineAmmo() + " / " + weapon.getMaxMagazineAmmo() + "(" + weapon.getName() + ")");
            }
        }
    }

    public static void onGameJoin(GameJoinS2CPacket packet) {
        ChunkInstancedBarrierVisualizer.INSTANCE.setShouldRebuild(true);
    }

    public static void onTeam(TeamS2CPacket packet) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null) return;
        Scoreboard scoreboard = mc.world.getScoreboard();
        Options options = KZEAddon.options;
        String name = packet.getTeamName();
        Team team = scoreboard.getTeam(name);

        if (team.shouldShowFriendlyInvisibles() != options.shouldShowFriendlyInvisibles) {
            team.setShowFriendlyInvisibles(options.shouldShowFriendlyInvisibles);
            KZEAddon.info(new TranslatableText("info.kzeaddon.change_visibility", new TranslatableText("info.kzeaddon." + (options.shouldShowFriendlyInvisibles ? "hide" : "show")), new TranslatableText("info.kzeaddon." + (options.shouldShowFriendlyInvisibles ? "show" : "hide"))));
        }
    }

    @SuppressWarnings("unused")
    public static Color randomColor() {
        Random rand = new Random();

        return new Color(rand.nextInt(255), rand.nextInt(255), rand.nextInt(255));
    }
}
