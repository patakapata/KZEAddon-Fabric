package com.theboss.kzeaddonfabric.mixin;

import com.theboss.kzeaddonfabric.events.RenderingEventsListener;
import com.theboss.kzeaddonfabric.utils.VanillaUtils;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.profiler.Profiler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Inject(method = "renderWorld", at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V", args = "ldc=hand"))
    private void onPostRenderWorld(float tickDelta, long limitTime, MatrixStack matrices, CallbackInfo ci) {
        Profiler profiler = VanillaUtils.getProfiler();
        profiler.push("KZEAddon$onRenderWorld");
        RenderingEventsListener.onPostRenderWorld(matrices, tickDelta);
        profiler.pop();
    }
}
