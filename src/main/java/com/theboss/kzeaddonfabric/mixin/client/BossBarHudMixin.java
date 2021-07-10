package com.theboss.kzeaddonfabric.mixin.client;

import com.theboss.kzeaddonfabric.KZEAddon;
import net.minecraft.client.gui.hud.BossBarHud;
import net.minecraft.network.packet.s2c.play.BossBarS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BossBarHud.class)
public abstract class BossBarHudMixin {
    @Inject(method = "handlePacket", at = @At("RETURN"))
    private void onHandlePacket(BossBarS2CPacket packet, CallbackInfo ci) {
        KZEAddon.KZE_INFO.killLog.handlePacket(packet);
    }
}
