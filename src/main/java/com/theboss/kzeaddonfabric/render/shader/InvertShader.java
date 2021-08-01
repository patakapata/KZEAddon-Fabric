package com.theboss.kzeaddonfabric.render.shader;

import net.minecraft.client.gl.Uniform;
import net.minecraft.util.math.Matrix4f;

public final class InvertShader extends AbstractShader {
    public static final InvertShader INSTANCE = new InvertShader();

    private Uniform MVP;

    @Override
    protected String getShaderName() {
        return "particle_vs";
    }

    @Override
    public void handleLoad() {
        super.handleLoad();
        this.MVP = this.shaderInstance.getUniformByNameOrDummy("MVP");
    }

    public void setMVP(Matrix4f matrix) {
        this.MVP.set(matrix);
    }
}
