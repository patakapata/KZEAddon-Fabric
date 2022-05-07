package com.theboss.kzeaddonfabric.render.shader.api;

import org.lwjgl.opengl.GL20;

public class IntegerUniform extends AbstractUniform<Integer> {
    public IntegerUniform(int program, CharSequence name) {
        super(program, name);
    }

    @Override
    public void set(Integer v1) {
        if (this.location != -1) {
            GL20.glUniform1i(this.location, v1);
        }
    }

    @Override
    public void set(Integer v1, Integer v2) {
        if (this.location != -1) {
            GL20.glUniform2i(this.location, v1, v2);
        }
    }

    @Override
    public void set(Integer v1, Integer v2, Integer v3) {
        if (this.location != -1) {
            GL20.glUniform3i(this.location, v1, v2, v3);
        }
    }

    @Override
    public void set(Integer v1, Integer v2, Integer v3, Integer v4) {
        if (this.location != -1) {
            GL20.glUniform4i(this.location, v1, v2, v3, v4);
        }
    }
}
