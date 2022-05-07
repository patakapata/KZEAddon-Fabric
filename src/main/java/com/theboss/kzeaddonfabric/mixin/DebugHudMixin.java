package com.theboss.kzeaddonfabric.mixin;

import com.theboss.kzeaddonfabric.events.listeners.EventsListener;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.hud.DebugHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Environment(EnvType.CLIENT)
@Mixin(DebugHud.class)
public abstract class DebugHudMixin {
    @Inject(method = "getLeftText", at = @At("RETURN"))
    private void onGetLeftTextTail(CallbackInfoReturnable<List<String>> cir) {
        EventsListener.onGetLeftTextTail(cir.getReturnValue());
    }

    @Inject(method = "getRightText", at = @At("RETURN"))
    private void onGetRightTextTail(CallbackInfoReturnable<List<String>> cir) {
        EventsListener.onGetRightTextTail(cir.getReturnValue());
    }
}
