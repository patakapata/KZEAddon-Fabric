package com.theboss.kzeaddonfabric.render;

import net.minecraft.client.util.math.MatrixStack;

public interface HUD {
    void render(MatrixStack matrices);

    void tick();

    void init();
}
