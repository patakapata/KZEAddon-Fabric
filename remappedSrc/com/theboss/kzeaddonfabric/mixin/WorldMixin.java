package com.theboss.kzeaddonfabric.mixin;

import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(World.class)
public abstract class WorldMixin implements WorldAccess, AutoCloseable {

    @Shadow
    public abstract boolean isClient();

    @Inject(at = @At("RETURN"), method = "tickBlockEntities")
    protected void tickWorldAfterBlockEntities(CallbackInfo ci) {}
}
