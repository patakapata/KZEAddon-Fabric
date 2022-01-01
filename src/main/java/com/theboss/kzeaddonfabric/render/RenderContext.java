package com.theboss.kzeaddonfabric.render;

import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.util.math.MatrixStack;

public class RenderContext {
    private final float delta;
    private final boolean renderBlockOutline;
    private final Camera camera;
    private final MatrixStack matrices;
    private final LightmapTextureManager lightmapTextureManager;
    private final GameRenderer gameRenderer;

    public RenderContext(float delta, boolean renderBlockOutline, Camera camera, MatrixStack matrices, LightmapTextureManager lightmapTextureManager, GameRenderer gameRenderer) {
        this.delta = delta;
        this.renderBlockOutline = renderBlockOutline;
        this.camera = camera;
        this.matrices = matrices;
        this.lightmapTextureManager = lightmapTextureManager;
        this.gameRenderer = gameRenderer;
    }

    public Camera getCamera() {
        return this.camera;
    }

    public float getDelta() {
        return this.delta;
    }

    public GameRenderer getGameRenderer() {
        return this.gameRenderer;
    }

    public LightmapTextureManager getLightmapTextureManager() {
        return this.lightmapTextureManager;
    }

    public MatrixStack getMatrices() {
        return this.matrices;
    }

    public boolean isRenderBlockOutline() {
        return this.renderBlockOutline;
    }
}
