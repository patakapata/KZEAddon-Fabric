package com.theboss.kzeaddonfabric.mixin.client;

import com.theboss.kzeaddonfabric.KZEAddon;
import com.theboss.kzeaddonfabric.events.EventsListener;
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

    @Shadow
    public abstract UUID getUuid();

    @Inject(method = "getTeamColorValue", at = @At("RETURN"), cancellable = true)
    private void onGetTeamColorValue(CallbackInfoReturnable<Integer> cir) {
        int result = EventsListener.onGetTeamColorValue((Entity) (Object) this);
        if (result != -1) cir.setReturnValue(result);
    }

    @Inject(method = "isGlowing", at = @At("RETURN"), cancellable = true)
    private void onIsGlowing(CallbackInfoReturnable<Boolean> cir) {
        if (KZEAddon.priorityGlowPlayers.contains(this.getUuid())) {
            cir.setReturnValue(true);
        }
    }
}
