package com.theboss.kzeaddonfabric.events.impl;

import com.theboss.kzeaddonfabric.KillLog;
import com.theboss.kzeaddonfabric.events.Event;

@FunctionalInterface
public interface KillLogEvents {
    Event<KillLogEvents> ADD = Event.create(KillLogEvents.class, listeners -> (log, entry) -> {for (KillLogEvents listener : listeners) listener.handle(log, entry);});

    void handle(KillLog log, KillLog.Entry entry);
}
