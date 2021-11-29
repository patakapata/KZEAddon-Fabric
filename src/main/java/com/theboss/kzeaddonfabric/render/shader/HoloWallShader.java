package com.theboss.kzeaddonfabric.render.shader;

import net.minecraft.client.gl.Uniform;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL33;

public class HoloWallShader extends AbstractShader {
    public static final HoloWallShader INSTANCE = new HoloWallShader();

    private Uniform position;
    private Uniform center;
    private Uniform invProj;
    private Uniform invView;
    private Uniform visibleDistance;

    private int resolutionLoc;
    private Vec2f resolution;

    private HoloWallShader() {
        this.resolution = new Vec2f(1F, 1F);
    }

    @Override
    protected String getShaderName() {
        return "holo_wall";
    }

    @Override
    public void handleLoad() {
        this.position = this.shaderInstance.getUniformByNameOrDummy("position");
        this.center = this.shaderInstance.getUniformByNameOrDummy("center");
        this.invProj = this.shaderInstance.getUniformByNameOrDummy("invProjMat");
        this.invView = this.shaderInstance.getUniformByNameOrDummy("invViewMat");
        this.resolutionLoc = GL33.glGetUniformLocation(this.shaderInstance.getProgramRef(), "resolution");
        this.visibleDistance = this.shaderInstance.getUniformByNameOrDummy("visibleDistance");
    }

    @Override
    public void bind() {
        super.bind();

        GL33.glUniform2f(this.resolutionLoc, this.resolution.x, this.resolution.y);
    }

    public void setPosition(Vec3d pos) {
        this.position.set((float) pos.x, (float) pos.y, (float) pos.z);
    }

    public void setCenter(Vec3d pos) {
        this.center.set((float) pos.x, (float) pos.y, (float) pos.z);
    }

    public void setInverseProjection(Matrix4f invProj) {
        this.invProj.set(invProj);
    }

    public void setInverseView(Matrix4f invView) {
        this.invView.set(invView);
    }

    public void setDepthTexture(int id) {
        if (this.shaderInstance != null) {
            this.shaderInstance.bindSampler("depthTexture", () -> id);
        }
    }

    public void setColorTexture(int id) {
        if (this.shaderInstance != null) {
            this.shaderInstance.bindSampler("colorTexture", () -> id);
        }
    }

    public void setResolution(float width, float height) {
        this.resolution = new Vec2f(width, height);
    }

    public void setVisibleDistance(float distance) {
        this.visibleDistance.set(distance);
    }
}
