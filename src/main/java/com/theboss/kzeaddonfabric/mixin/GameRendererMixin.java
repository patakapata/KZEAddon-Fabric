package com.theboss.kzeaddonfabric.mixin;

import com.theboss.kzeaddonfabric.events.listeners.RenderingEventsListener;
import com.theboss.kzeaddonfabric.render.RenderContext;
import com.theboss.kzeaddonfabric.utils.VanillaUtils;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Shadow
    @Final
    private Camera camera;

    @Shadow
    protected abstract boolean shouldRenderBlockOutline();

    @Shadow
    public abstract LightmapTextureManager getLightmapTextureManager();

    @Inject(method = "renderWorld", at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V", args = "ldc=hand"))
    private void onPostRenderWorld(float tickDelta, long limitTime, MatrixStack matrix, CallbackInfo ci) {
        VanillaUtils.getProfiler().push("KZEAddon$onPostRenderWorld");
        RenderContext context = new RenderContext(tickDelta, this.shouldRenderBlockOutline(), this.camera, matrix, this.getLightmapTextureManager(), (GameRenderer) (Object) this);
        RenderingEventsListener.onPostRenderWorld(context);
        VanillaUtils.getProfiler().pop();
    }
}
