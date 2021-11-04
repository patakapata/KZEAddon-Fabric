package com.theboss.kzeaddonfabric.mixin;

import com.theboss.kzeaddonfabric.VanillaUtils;
import com.theboss.kzeaddonfabric.events.RenderingEventsListener;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {
    @Inject(method = "render", at = @At(value = "INVOKE", ordinal = 0, target = "Lnet/minecraft/client/render/WorldRenderer;renderLayer(Lnet/minecraft/client/render/RenderLayer;Lnet/minecraft/client/util/math/MatrixStack;DDD)V"))
    private void onPreRenderSolid(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, CallbackInfo ci) {
        VanillaUtils.getProfiler().push("KZEAddon$onPreRenderSolid");
        RenderingEventsListener.onPreRenderSolid(matrices, tickDelta, limitTime, renderBlockOutline, camera, gameRenderer, lightmapTextureManager, matrix4f);
        VanillaUtils.getProfiler().pop();
    }

    @Inject(method = "render", at = @At(value = "INVOKE", ordinal = 1, target = "Lnet/minecraft/client/render/WorldRenderer;renderLayer(Lnet/minecraft/client/render/RenderLayer;Lnet/minecraft/client/util/math/MatrixStack;DDD)V"))
    private void onPreRenderCutoutMipped(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, CallbackInfo ci) {
        VanillaUtils.getProfiler().push("KZEAddon$onPreRenderCutoutMipped");
        VanillaUtils.getProfiler().pop();
    }


    @Inject(method = "render", at = @At(value = "INVOKE", ordinal = 2, target = "Lnet/minecraft/client/render/WorldRenderer;renderLayer(Lnet/minecraft/client/render/RenderLayer;Lnet/minecraft/client/util/math/MatrixStack;DDD)V"))
    private void onPreRenderCutout(MatrixStack matrices, float delta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, CallbackInfo ci) {
        VanillaUtils.getProfiler().push("KZEAddon$onPreRenderCutout");
        RenderingEventsListener.onPreRenderCutout(matrices, delta, limitTime, renderBlockOutline, camera, gameRenderer, lightmapTextureManager, matrix4f);
        VanillaUtils.getProfiler().pop();
    }

    @Inject(method = "render", at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V", args = "ldc=entities"))
    private void onPreRenderEntities(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, CallbackInfo ci) {
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onRenderInit(MinecraftClient client, BufferBuilderStorage bufferBuilders, CallbackInfo ci) {
        RenderingEventsListener.onInit();
    }
}
