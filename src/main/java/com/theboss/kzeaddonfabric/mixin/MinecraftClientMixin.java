package com.theboss.kzeaddonfabric.mixin;

import com.theboss.kzeaddonfabric.KZEAddon;
import com.theboss.kzeaddonfabric.events.impl.ClientEvents;
import com.theboss.kzeaddonfabric.events.listeners.RenderingEventsListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import net.minecraft.client.WindowEventHandler;
import net.minecraft.client.util.Window;
import net.minecraft.util.snooper.SnooperListener;
import net.minecraft.util.thread.ReentrantThreadExecutor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin extends ReentrantThreadExecutor<Runnable> implements SnooperListener, WindowEventHandler {
    @Shadow
    @Final
    private Window window;

    public MinecraftClientMixin(String string) {
        super(string);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(RunArgs args, CallbackInfo ci) {
        KZEAddon.postClientInitialize((MinecraftClient) (Object) this);
    }

    @Inject(method = "onResolutionChanged", at = @At("RETURN"))
    private void onResolutionChanged(CallbackInfo ci) {
        RenderingEventsListener.onWindowResized(this.window);
    }

    @Inject(method = "stop", at = @At(value = "INVOKE", target = "Lorg/apache/logging/log4j/Logger;info(Ljava/lang/String;)V", shift = At.Shift.AFTER))
    private void onStopping(CallbackInfo ci) {
        ClientEvents.STOP.invoker().handle((MinecraftClient) (Object) this);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        ClientEvents.TICK.invoker().handle((MinecraftClient) (Object) this);
    }
}
