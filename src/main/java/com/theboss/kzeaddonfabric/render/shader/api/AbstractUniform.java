package com.theboss.kzeaddonfabric.render.shader.api;

import org.lwjgl.opengl.GL20;

public abstract class AbstractUniform<E> implements ImmediateUniform<E> {
    protected final int location;

    public AbstractUniform(int program, CharSequence name) {
        this.location = GL20.glGetUniformLocation(program, name);
    }
}
