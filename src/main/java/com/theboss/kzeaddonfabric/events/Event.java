package com.theboss.kzeaddonfabric.events;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

public final class Event<T> {
    private final Lock lock = new ReentrantLock();
    private final Function<T[], T> invokerFactory;
    private volatile T invoker;
    private T[] listeners;

    @SuppressWarnings("unchecked")
    public static <T> Event<T> create(Class<? extends T> type, Function<T[], T> invokerFactory) {
        return new Event<T>((T[]) Array.newInstance(type, 0), invokerFactory);
    }

    private Event(T[] listeners, Function<T[], T> invokerFactory) {
        this.listeners = listeners;
        this.invokerFactory = invokerFactory;
    }

    public T invoker() {
        return this.invoker;
    }

    public void register(T listener) {
        if (listener == null) return;
        this.lock.lock();
        try {
            this.listeners = Arrays.copyOf(this.listeners, this.listeners.length + 1);
            this.listeners[this.listeners.length - 1] = listener;
            this.update();
        } finally {
            this.lock.unlock();
        }
    }

    private void update() {
        this.invoker = this.invokerFactory.apply(this.listeners);
    }
}
