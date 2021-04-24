package com.theboss.kzeaddonfabric.screen;

import net.minecraft.text.Text;

public abstract class Screen extends net.minecraft.client.gui.screen.Screen {
    private final ParentWrapper parent;

    protected Screen(Text title) {
        super(title);

        this.parent = new ParentWrapper(null);
    }

    public void setParent(Object screen) {
        this.parent.setParent(screen);
    }

    @Override
    public void onClose() {
        this.parent.openParent(this.client);
    }
}
