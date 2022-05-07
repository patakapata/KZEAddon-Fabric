package com.theboss.kzeaddonfabric.events.impl;

import com.theboss.kzeaddonfabric.events.Event;
import com.theboss.kzeaddonfabric.ingame.KZEInformation;

@FunctionalInterface
public interface ReloadEvents {
    Event<ReloadEvents> START = Event.create(ReloadEvents.class, listeners -> kzeInfo -> {for (ReloadEvents listener : listeners) listener.handle(kzeInfo);});
    Event<ReloadEvents> COMPLETE = Event.create(ReloadEvents.class, listeners -> kzeInfo -> {for (ReloadEvents listener : listeners) listener.handle(kzeInfo);});
    Event<ReloadEvents> REFUSE = Event.create(ReloadEvents.class, listeners -> kzeInfo -> {for (ReloadEvents listener : listeners) listener.handle(kzeInfo);});

    void handle(KZEInformation kzeInfo);
}
