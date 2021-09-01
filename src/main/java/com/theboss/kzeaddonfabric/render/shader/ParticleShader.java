package com.theboss.kzeaddonfabric.render.shader;

import net.minecraft.client.gl.Uniform;
import net.minecraft.util.math.Matrix4f;

public class ParticleShader extends AbstractShader {
    public static final ParticleShader INSTANCE = new ParticleShader();

    private Uniform MVP;
    private Uniform color;
    private Uniform cam_right_ws;
    private Uniform cam_up_ws;

    private ParticleShader() {}

    @Override
    protected String getShaderName() {
        return "particle";
    }

    @Override
    public void handleLoad() {
        this.MVP = this.shaderInstance.getUniformByNameOrDummy("MVP");
        this.color = this.shaderInstance.getUniformByNameOrDummy("color");
        this.cam_right_ws = this.shaderInstance.getUniformByNameOrDummy("cam_right_ws");
        this.cam_up_ws = this.shaderInstance.getUniformByNameOrDummy("cam_up_ws");
    }

    public void setCamRightWS(float v1, float v2, float v3) {
        this.cam_right_ws.set(v1, v2, v3);
    }

    public void setCamUpWS(float v1, float v2, float v3) {
        this.cam_up_ws.set(v1, v2, v3);
    }

    public void setColor(float red, float green, float blue, float alpha) {
        this.color.set(red, green, blue, alpha);
    }

    public void setMVP(Matrix4f matrix) {
        this.MVP.set(matrix);
    }
}
