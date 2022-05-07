package com.theboss.kzeaddonfabric.render.shader.impl;

import com.theboss.kzeaddonfabric.KZEAddon;
import com.theboss.kzeaddonfabric.render.shader.api.AbstractShader;
import com.theboss.kzeaddonfabric.render.shader.api.FloatUniform;
import com.theboss.kzeaddonfabric.render.shader.api.ImmediateUniform;
import net.minecraft.client.gl.Uniform;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;

public class OldBarrierShader extends AbstractShader {
    private static final OldBarrierShader INSTANCE = new OldBarrierShader();
    private Uniform mvp;
    private Uniform fadeRadius;
    private Uniform useFade;
    private Uniform center;
    private Uniform color;
    private ImmediateUniform<Float> colorImmediate;

    public static OldBarrierShader getInstance() {
        return INSTANCE;
    }

    @Override
    protected String getShaderName() {
        return KZEAddon.MOD_ID + ":old_barrier";
    }

    @Override
    public void handleLoad() {
        this.mvp = this.shaderInstance.getUniformByNameOrDummy("mvp");
        this.fadeRadius = this.shaderInstance.getUniformByNameOrDummy("fadeRadius");
        this.useFade = this.shaderInstance.getUniformByNameOrDummy("useFade");
        this.center = this.shaderInstance.getUniformByNameOrDummy("center");
        this.color = this.shaderInstance.getUniformByNameOrDummy("color");
        this.colorImmediate = new FloatUniform(this.getProgram(), "color");
    }

    public void setCenter(Vec3d pos) {
        this.setCenter((float) pos.x, (float) pos.y, (float) pos.z);
    }

    public void setCenter(float x, float y, float z) {
        this.center.set(x, y, z);
    }

    public void setColor(int color) {
        this.setColor((color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF);
    }

    public void setColor(int red, int green, int blue) {
        this.setColor(red / 255F, green / 255F, blue / 255F);
    }

    public void setColor(float red, float green, float blue) {
        this.color.set(red, green, blue);
    }

    public void setFadeRadius(float radius) {
        this.fadeRadius.set(radius);
    }

    public void setMVP(Matrix4f mvp) {
        this.mvp.set(mvp);
    }

    public void setUseFade(boolean isFade) {
        this.useFade.set(isFade ? 1 : 0);
    }

    public void setColorTemporary(int color) {
        this.colorImmediate.set(((color >> 16) & 0xFF) / 255F, ((color >> 8) & 0xFF) / 255F, (color & 0xFF) / 255F);
    }
}
