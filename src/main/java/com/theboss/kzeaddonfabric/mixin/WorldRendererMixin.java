package com.theboss.kzeaddonfabric.mixin;

import com.theboss.kzeaddonfabric.events.listeners.RenderingEventsListener;
import com.theboss.kzeaddonfabric.render.RenderContext;
import com.theboss.kzeaddonfabric.utils.VanillaUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {
    @Inject(method = "render", at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V", args = "ldc=translucent", shift = At.Shift.AFTER))
    private void onPostRenderTranslucent(MatrixStack matrices, float delta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, CallbackInfo ci) {
        VanillaUtils.getProfiler().push("KZEAddon$onPostRenderTranslucent");
        RenderContext renderContext = new RenderContext(delta, renderBlockOutline, camera, matrices, lightmapTextureManager, gameRenderer);
        VanillaUtils.getProfiler().pop();
    }

    @Inject(method = "render", at = @At(value = "INVOKE", ordinal = 2, target = "Lnet/minecraft/client/render/WorldRenderer;renderLayer(Lnet/minecraft/client/render/RenderLayer;Lnet/minecraft/client/util/math/MatrixStack;DDD)V"))
    private void onPreRenderCutout(MatrixStack matrices, float delta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, CallbackInfo ci) {
        VanillaUtils.getProfiler().push("KZEAddon$onPreRenderCutout");
        RenderContext renderContext = new RenderContext(delta, renderBlockOutline, camera, matrices, lightmapTextureManager, gameRenderer);
        VanillaUtils.getProfiler().pop();
    }

    @Inject(method = "render", at = @At(value = "INVOKE", ordinal = 1, target = "Lnet/minecraft/client/render/WorldRenderer;renderLayer(Lnet/minecraft/client/render/RenderLayer;Lnet/minecraft/client/util/math/MatrixStack;DDD)V"))
    private void onPreRenderCutoutMipped(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, CallbackInfo ci) {
        VanillaUtils.getProfiler().push("KZEAddon$onPreRenderCutoutMipped");
        RenderContext renderContext = new RenderContext(tickDelta, renderBlockOutline, camera, matrices, lightmapTextureManager, gameRenderer);
        VanillaUtils.getProfiler().pop();
    }

    @Inject(method = "render", at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V", args = "ldc=entities"))
    private void onPreRenderEntities(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, CallbackInfo ci) {
        VanillaUtils.getProfiler().push("KZEAddon$onPreRenderEntities");
        RenderContext renderContext = new RenderContext(tickDelta, renderBlockOutline, camera, matrices, lightmapTextureManager, gameRenderer);
        VanillaUtils.getProfiler().pop();
    }

    @Inject(method = "render", at = @At(value = "INVOKE", ordinal = 0, target = "Lnet/minecraft/client/render/WorldRenderer;renderLayer(Lnet/minecraft/client/render/RenderLayer;Lnet/minecraft/client/util/math/MatrixStack;DDD)V"))
    private void onPreRenderSolid(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, CallbackInfo ci) {
        VanillaUtils.getProfiler().push("KZEAddon$onPreRenderSolid");
        RenderContext renderContext = new RenderContext(tickDelta, renderBlockOutline, camera, matrices, lightmapTextureManager, gameRenderer);
        VanillaUtils.getProfiler().pop();
    }

    @Inject(method = "render", at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V", args = "ldc=translucent"))
    private void onPreRenderTranslucent(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, CallbackInfo ci) {
        VanillaUtils.getProfiler().push("KZEAddon$onPreRenderTranslucent");
        RenderContext renderContext = new RenderContext(tickDelta, renderBlockOutline, camera, matrices, lightmapTextureManager, gameRenderer);
        RenderingEventsListener.onPreRenderTranslucent(renderContext);
        VanillaUtils.getProfiler().pop();
    }
}
