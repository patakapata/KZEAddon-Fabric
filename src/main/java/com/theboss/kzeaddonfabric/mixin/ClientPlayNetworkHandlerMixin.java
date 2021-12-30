package com.theboss.kzeaddonfabric.mixin;

import com.theboss.kzeaddonfabric.KZEAddon;
import com.theboss.kzeaddonfabric.events.PacketListener;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.text.LiteralText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin {
    @Inject(method = "onBlockUpdate", at = @At("RETURN"))
    private void onBlockUpdate(BlockUpdateS2CPacket packet, CallbackInfo ci) {
        PacketListener.onBlockUpdate(packet);
    }

    @Inject(method = "onChunkDeltaUpdate", at = @At("RETURN"))
    private void onChunkDeltaUpdate(ChunkDeltaUpdateS2CPacket packet, CallbackInfo ci) {
        PacketListener.onChunkDeltaUpdate(packet);
    }

    @Inject(method = "onEquipmentUpdate", at = @At("RETURN"))
    private void onEquipmentUpdate(EntityEquipmentUpdateS2CPacket packet, CallbackInfo ci) {
        PacketListener.onEquipmentUpdate(packet);
    }

    @Inject(method = "onGameJoin", at = @At("RETURN"))
    private void onGameJoin(GameJoinS2CPacket packet, CallbackInfo ci) {
        PacketListener.onGameJoin(packet);
    }

    @Inject(method = "onCustomPayload", at = @At("RETURN"))
    private void onCustomPayload(CustomPayloadS2CPacket packet, CallbackInfo ci) {
        PacketListener.onCustomPayload(packet);
    }

    @Inject(method = "onBossBar", at = @At("RETURN"))
    private void onBossBar(BossBarS2CPacket packet, CallbackInfo ci) {
        // TODO Handle Boss bar
        BossBarS2CPacket.Type type = packet.getType();
        if (type == BossBarS2CPacket.Type.ADD || type == BossBarS2CPacket.Type.UPDATE_NAME) {
            LiteralText body = new LiteralText("BossBar > [");
            body.append(packet.getName()).append("]");
            KZEAddon.info(body);
        }
    }

    @Inject(method = "onTeam", at = @At("RETURN"))
    private void onTeam(TeamS2CPacket packet, CallbackInfo ci) {
        PacketListener.onTeam(packet);
    }
}
