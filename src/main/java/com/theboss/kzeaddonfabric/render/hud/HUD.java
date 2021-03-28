package com.theboss.kzeaddonfabric.render.hud;

import net.minecraft.client.util.math.MatrixStack;

public interface HUD {
    void render(MatrixStack matrices);

    void tick();
}
