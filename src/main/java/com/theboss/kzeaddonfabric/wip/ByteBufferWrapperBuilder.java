package com.theboss.kzeaddonfabric.wip;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexConsumer;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class ByteBufferWrapperBuilder implements VertexConsumer {
    private ByteBufferWrapper wrapper;

    public ByteBufferWrapperBuilder(@Nullable  ByteBufferWrapper wrapper) {
        this.wrapper = wrapper;
    }

    public void setWrapper(@Nullable ByteBufferWrapper wrapper) {
        this.wrapper = wrapper;
    }

    protected void assertWrapper() {
        Objects.requireNonNull(this.wrapper);
    }

    @Override
    public VertexConsumer vertex(double x, double y, double z) {
        this.assertWrapper();
        this.wrapper.put(x, y, z);
        return this;
    }

    @Override
    public VertexConsumer color(int red, int green, int blue, int alpha) {
        this.assertWrapper();
        this.wrapper.put(alpha, green, blue, alpha);
        return this;
    }

    @Override
    public VertexConsumer texture(float u, float v) {
        this.assertWrapper();
        this.wrapper.put(u, v);
        return this;
    }

    @Override
    public VertexConsumer overlay(int u, int v) {
        this.assertWrapper();
        this.wrapper.put(u, v);
        return this;
    }

    @Override
    public VertexConsumer light(int u, int v) {
        this.assertWrapper();
        this.wrapper.put(u, v);
        return this;
    }

    @Override
    public VertexConsumer normal(float x, float y, float z) {
        this.assertWrapper();
        this.wrapper.put(x, y, z);
        return this;
    }

    @Override
    public void next() {
    }
}
