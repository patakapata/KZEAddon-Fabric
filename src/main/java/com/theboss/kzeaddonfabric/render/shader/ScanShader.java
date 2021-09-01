package com.theboss.kzeaddonfabric.render.shader;

import net.minecraft.client.gl.Uniform;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3f;

public final class ScanShader extends AbstractShader {
    public static final ScanShader INSTANCE = new ScanShader();

    private Uniform invViewMat;
    private Uniform invProjMat;
    private Uniform pos;
    private Uniform center;
    private Uniform radius;
    private Uniform width;
    private Uniform directDepthDisplay;

    private ScanShader() {}

    @Override
    protected String getShaderName() {
        return "scan";
    }

    @Override
    public void handleLoad() {
        this.invViewMat = this.shaderInstance.getUniformByNameOrDummy("invViewMat");
        this.invProjMat = this.shaderInstance.getUniformByNameOrDummy("invProjMat");
        this.pos = this.shaderInstance.getUniformByNameOrDummy("pos");
        this.center = this.shaderInstance.getUniformByNameOrDummy("center");
        this.radius = this.shaderInstance.getUniformByNameOrDummy("radius");
        this.width = this.shaderInstance.getUniformByNameOrDummy("width");
        this.directDepthDisplay = this.shaderInstance.getUniformByNameOrDummy("directDepthDisplay");
    }

    public void isDirectDepthDisplay(boolean bool) {
        this.directDepthDisplay.set(bool ? 1.0F : 0.0F);
    }

    public void setCenter(Vec3f center) {
        this.center.set(center.getX(), center.getY(), center.getZ());
    }

    public void setDepthTex(int textureId) {
        this.shaderInstance.bindSampler("depthTex", () -> textureId);
    }

    public void setInvProjMat(Matrix4f matrix) {
        this.invProjMat.set(matrix);
    }

    public void setInvViewMat(Matrix4f matrix) {
        this.invViewMat.set(matrix);
    }

    public void setPosition(Vec3f position) {
        this.pos.set(position.getX(), position.getY(), position.getZ());
    }

    public void setRadius(float radius) {
        this.radius.set(radius);
    }

    public void setWidth(float width) {
        this.width.set(width);
    }
}
