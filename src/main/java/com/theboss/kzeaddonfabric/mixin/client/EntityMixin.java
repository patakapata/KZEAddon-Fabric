package com.theboss.kzeaddonfabric.mixin.client;

import com.theboss.kzeaddonfabric.events.GetTeamColorValueEvent;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.util.Nameable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(Entity.class)
public abstract class EntityMixin implements Nameable, CommandOutput {
    @Shadow
    protected UUID uuid;

    @Inject(method = "getTeamColorValue", at = @At("RETURN"), cancellable = true)
    private void onGetTeamColorValue(CallbackInfoReturnable<Integer> cir) {
        GetTeamColorValueEvent.EVENT.invoker().onGetTeamColorValue((Entity) (Object) this, cir);
    }
}
