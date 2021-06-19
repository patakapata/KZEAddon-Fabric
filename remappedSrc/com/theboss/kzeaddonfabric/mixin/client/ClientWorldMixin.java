package com.theboss.kzeaddonfabric.mixin.client;

import com.theboss.kzeaddonfabric.KZEAddon;
import com.theboss.kzeaddonfabric.mixin.WorldMixin;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(ClientWorld.class)
public abstract class ClientWorldMixin extends WorldMixin {

    @Override
    protected void tickWorldAfterBlockEntities(CallbackInfo ci) {
        KZEAddon.onTick();
    }
}
