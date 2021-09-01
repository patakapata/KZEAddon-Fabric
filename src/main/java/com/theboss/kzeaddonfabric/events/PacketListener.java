package com.theboss.kzeaddonfabric.events;

import com.theboss.kzeaddonfabric.Color;
import com.theboss.kzeaddonfabric.wip.ChunkInstancedBarrierVisualizer;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityEquipmentUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.text.Text;

import java.util.Random;

import static com.theboss.kzeaddonfabric.KZEAddon.*;

public class PacketListener {

    public static void onBlockUpdate(BlockUpdateS2CPacket packet) {
    }

    public static void onCustomPayload(CustomPayloadS2CPacket packet) {
        warn(Text.of("CustomPayload Packet : " + packet.getChannel().toString()));
        StringBuilder builder = new StringBuilder();
        PacketByteBuf buf = packet.getData();
        for (int i = 0; i < buf.capacity(); i++) {
            byte b = buf.readByte();
            if (b == 0x00) {
                String content = builder.toString();
                builder.setLength(0);
                error("Content " + i + " : " + content);
            }
            builder.append((char) b);
        }

    }

    public static void onEquipmentUpdate(EntityEquipmentUpdateS2CPacket packet) {
    }

    public static void onGameJoin(GameJoinS2CPacket packet) {
        ChunkInstancedBarrierVisualizer.INSTANCE.setShouldRebuild(true);
    }

    public static Color randomColor() {
        Random rand = new Random();

        return new Color(rand.nextInt(255), rand.nextInt(255), rand.nextInt(255));
    }
}
