package com.theboss.kzeaddonfabric.mixin;

import com.theboss.kzeaddonfabric.events.listeners.RenderingEventsListener;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(PlayerEntityRenderer.class)
public class PlayerEntityRendererMixin {
    @Inject(method = "getArmPose", at = @At("RETURN"), cancellable = true)
    private static void onGetArmPose(AbstractClientPlayerEntity player, Hand hand, CallbackInfoReturnable<BipedEntityModel.ArmPose> cir) {
        Optional<BipedEntityModel.ArmPose> ret = RenderingEventsListener.onGetArmPose(player, hand);

        ret.ifPresent(cir::setReturnValue);
    }
}
