package com.theboss.kzeaddonfabric.events;

public class Context {
    private boolean isCancelled;

    public Context() {
        this.isCancelled = false;
    }

    public boolean isCancelled() {
        return this.isCancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.isCancelled = cancelled;
    }
}
