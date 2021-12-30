package com.theboss.kzeaddonfabric.render.shader;

import com.theboss.kzeaddonfabric.KZEAddon;
import com.theboss.kzeaddonfabric.utils.Color;
import net.minecraft.client.gl.Uniform;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;

public class BarrierShader extends AbstractShader {
    public static final BarrierShader INSTANCE = new BarrierShader();

    private Uniform color;
    private Uniform MVP;
    private Uniform center;
    private Uniform fadeRadius;
    private Uniform useFade;

    private BarrierShader() {}

    @Override
    protected String getShaderName() {
        return KZEAddon.MOD_ID + ":barrier";
    }

    @Override
    public void handleLoad() {
        this.color = this.shaderInstance.getUniformByNameOrDummy("color");
        this.MVP = this.shaderInstance.getUniformByNameOrDummy("MVP");
        this.center = this.shaderInstance.getUniformByNameOrDummy("center");
        this.fadeRadius = this.shaderInstance.getUniformByNameOrDummy("fadeRadius");
        this.useFade = this.shaderInstance.getUniformByNameOrDummy("useFade");
    }

    public void setCenter(float x, float y, float z) {
        this.center.set(x, y, z);
    }

    public void setCenter(Vec3f pos) {
        this.setCenter(pos.getX(), pos.getY(), pos.getZ());
    }

    public void setCenter(double x, double y, double z) {
        this.center.set((float) x, (float) y, (float) z);
    }

    public void setCenter(Vec3d pos) {
        this.setCenter(pos.getX(), pos.getY(), pos.getZ());
    }

    public void setColor(float red, float green, float blue, float alpha) {
        this.color.set(red, green, blue, alpha);
    }

    public void setColor(Color color) {
        this.color.set(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F, 1F);
    }

    public void setFadeRadius(float fadeRadius) {
        this.fadeRadius.set(fadeRadius);
    }

    public void setMVP(Matrix4f matrix) {
        this.MVP.set(matrix);
    }

    public void setUseFade(boolean isUseFade) {
        this.useFade.set(isUseFade ? 1F : 0F);
    }
}
