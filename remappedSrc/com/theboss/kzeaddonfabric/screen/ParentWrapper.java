package com.theboss.kzeaddonfabric.screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;

public class ParentWrapper {
    private Screen parent;

    public ParentWrapper() {
        this(null);
    }

    public ParentWrapper(Object parent) {
        this.setParent(parent);
    }

    public Screen getParent() {
        return this.parent;
    }

    public void setParent(Object parent) {
        if (parent == null) {
            this.parent = null;
        } else if (!(parent instanceof Screen)) {
            throw new IllegalArgumentException("Invalid parent argument!");
        } else {
            this.parent = (Screen) parent;
        }
    }

    public void open(MinecraftClient mc) {
        mc.openScreen(this.parent);
    }
}
