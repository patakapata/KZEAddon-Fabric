package com.theboss.kzeaddonfabric.events;

import com.theboss.kzeaddonfabric.KillLog;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public interface KillLogEvents {
    Event<KillLogEvents> ADD_ENTRY = EventFactory.createArrayBacked(KillLogEvents.class, listeners -> (killLog, entry) -> {
        for (KillLogEvents listener : listeners) listener.apply(killLog, entry);
    });

    void apply(KillLog killLog, KillLog.Entry entry);
}
