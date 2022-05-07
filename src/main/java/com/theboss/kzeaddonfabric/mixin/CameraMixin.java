package com.theboss.kzeaddonfabric.mixin;

import com.theboss.kzeaddonfabric.events.listeners.RenderingEventsListener;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {
    @Inject(method = "update", at = @At("RETURN"))
    private void onCameraUpdate(BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo ci) {
        RenderingEventsListener.onCameraUpdate((Camera) (Object) this, area, focusedEntity, thirdPerson, inverseView, tickDelta);
    }
}
