package com.theboss.kzeaddonfabric.events;

import com.theboss.kzeaddonfabric.ingame.KZEInformation;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public interface ReloadEvents {
    Event<ReloadEvents> BEGIN = EventFactory.createArrayBacked(ReloadEvents.class, listeners -> kzeInfo -> {
        for (ReloadEvents listener : listeners) listener.apply(kzeInfo);
    });

    Event<ReloadEvents> REFUSE = EventFactory.createArrayBacked(ReloadEvents.class, listeners -> kzeInfo -> {
        for (ReloadEvents listener : listeners) listener.apply(kzeInfo);
    });

    Event<ReloadEvents> COMPLETE = EventFactory.createArrayBacked(ReloadEvents.class, listeners -> kzeInfo -> {
        for (ReloadEvents listener : listeners) listener.apply(kzeInfo);
    });

    void apply(KZEInformation kzeInfo);
}
