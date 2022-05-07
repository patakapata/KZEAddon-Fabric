package com.theboss.kzeaddonfabric.events.impl;

import com.theboss.kzeaddonfabric.events.Event;
import net.minecraft.client.MinecraftClient;

public interface ClientEvents {
    Event<ClientEvents> TICK = Event.create(ClientEvents.class, listeners -> mc -> {for (ClientEvents listener : listeners) listener.handle(mc);});
    Event<ClientEvents> STOP = Event.create(ClientEvents.class, listeners -> mc -> {for (ClientEvents listener : listeners) listener.handle(mc);});

    void handle(MinecraftClient mc);
}
