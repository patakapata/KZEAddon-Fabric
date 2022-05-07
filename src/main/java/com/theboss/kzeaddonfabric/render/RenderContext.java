package com.theboss.kzeaddonfabric.render;

import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;

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

    public void applyCamera(MatrixStack matrices) {
        Vec3d pos = this.camera.getPos();
        matrices.translate(-pos.x, -pos.y, -pos.z);
    }

    public Matrix4f pushCamera(MatrixStack matrices) {
        matrices.push();
        this.applyCamera(matrices);
        return matrices.peek().getModel();
    }
}
