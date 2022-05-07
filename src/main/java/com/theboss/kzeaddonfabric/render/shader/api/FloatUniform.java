package com.theboss.kzeaddonfabric.render.shader.api;

import org.lwjgl.opengl.GL20;

public class FloatUniform extends AbstractUniform<Float> {
    public FloatUniform(int program, CharSequence name) {
        super(program, name);
    }

    @Override
    public void set(Float v1) {
        if (this.location != -1) {
            GL20.glUniform1f(this.location, v1);
        }
    }

    @Override
    public void set(Float v1, Float v2) {
        if (this.location != -1) {
            GL20.glUniform2f(this.location, v1, v2);
        }
    }

    @Override
    public void set(Float v1, Float v2, Float v3) {
        if (this.location != -1) {
            GL20.glUniform3f(this.location, v1, v2, v3);
        }
    }

    @Override
    public void set(Float v1, Float v2, Float v3, Float v4) {
        if (this.location != -1) {
            GL20.glUniform4f(this.location, v1, v2, v3, v4);
        }
    }
}
