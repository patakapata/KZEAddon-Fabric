package com.theboss.kzeaddonfabric.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public final class GetTeamColorValueEvent {
    private GetTeamColorValueEvent() {}

    public static final Event<OnReturn> EVENT = EventFactory.createArrayBacked(OnReturn.class, callbacks -> (entity, cir) -> {
        for (OnReturn callback : callbacks) {
            callback.onGetTeamColorValue(entity, cir);
        }
    });

    @FunctionalInterface
    public interface OnReturn {
        void onGetTeamColorValue(Entity entity, CallbackInfoReturnable<Integer> cir);
    }
}
