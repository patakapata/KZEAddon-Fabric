package com.theboss.kzeaddonfabric.mixin;

import com.theboss.kzeaddonfabric.KZEAddonFabric;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
    @Inject(at = @At(value = "INVOKE", target = "Lorg/apache/logging/log4j/Logger;info(Ljava/lang/String;)V", shift = At.Shift.AFTER), method = "stop")
    private void onClientStop(CallbackInfo ci) {
        KZEAddonFabric.onClientStop();
    }
}
