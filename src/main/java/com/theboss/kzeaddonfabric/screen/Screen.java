package com.theboss.kzeaddonfabric.screen;

import net.minecraft.text.Text;

public abstract class Screen extends net.minecraft.client.gui.screen.Screen {
    private Screen parent;

    protected Screen(Text title) {
        super(title);
    }

    public void setParent(Screen screen) {
        this.parent = screen;
    }

    @Override
    public void onClose() {
        this.client.openScreen(this.parent);
    }
}
