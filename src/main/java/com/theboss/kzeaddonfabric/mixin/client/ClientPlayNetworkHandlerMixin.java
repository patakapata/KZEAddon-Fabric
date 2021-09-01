package com.theboss.kzeaddonfabric.mixin.client;

import com.theboss.kzeaddonfabric.events.PacketListener;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.*;
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

    @Inject(method = "onEquipmentUpdate", at = @At("RETURN"))
    private void onEquipmentUpdate(EntityEquipmentUpdateS2CPacket packet, CallbackInfo ci) {
        PacketListener.onEquipmentUpdate(packet);
    }

    @Inject(method = "onGameJoin", at = @At("RETURN"))
    private void onGameJoin(GameJoinS2CPacket packet, CallbackInfo ci) {
        PacketListener.onGameJoin(packet);
    }

    @Inject(method = "onCustomPayload", at = @At("HEAD"))
    private void onCustomPayload(CustomPayloadS2CPacket packet, CallbackInfo ci) {
        PacketListener.onCustomPayload(packet);
    }
}
