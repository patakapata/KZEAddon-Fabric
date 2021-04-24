package com.theboss.kzeaddonfabric.mixin.client;

import com.theboss.kzeaddonfabric.KZEAddon;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.scoreboard.AbstractTeam;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.client.render.entity.PlayerEntityRenderer.class)
public abstract class PlayerEntityRendererMixin {
    @Inject(method = "render", at = @At("INVOKE"), cancellable = true)
    private void onRenderInvoke(AbstractClientPlayerEntity abstractClientPlayerEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        AbstractTeam team = MinecraftClient.getInstance().player.getScoreboardTeam();
        if (!abstractClientPlayerEntity.isMainPlayer() && KZEAddon.OPTIONS.isCompletelyInvisible() && KZEAddon.isHidePlayersEnabled && abstractClientPlayerEntity.getScoreboardTeam() == team) {
            ci.cancel();
        }
    }
}
