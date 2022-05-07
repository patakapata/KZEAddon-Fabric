package com.theboss.kzeaddonfabric.render.shader.api;

public interface ImmediateUniform<E> {
    void set(E v1);

    void set(E v1, E v2);

    void set(E v1, E v2, E v3);

    void set(E v1, E v2, E v3, E v4);
}
