package com.theboss.kzeaddonfabric.mixin.client;

import com.theboss.kzeaddonfabric.KZEAddon;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {
    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(MinecraftClient client, BufferBuilderStorage bufferBuilders, CallbackInfo ci) {
        KZEAddon.onRenderInit();
    }
}