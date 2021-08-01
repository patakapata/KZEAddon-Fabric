package com.theboss.kzeaddonfabric.render.shader;

import com.mojang.blaze3d.systems.RenderSystem;
import com.theboss.kzeaddonfabric.KZEAddon;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.JsonEffectGlShader;
import net.minecraft.client.gl.Uniform;
import net.minecraft.resource.ReloadableResourceManager;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SynchronousResourceReloader;

import java.io.IOException;

public abstract class AbstractShader implements SynchronousResourceReloader {
    private static final long START_TIME = System.currentTimeMillis();
    protected JsonEffectGlShader shaderInstance;
    private Uniform timeUniform;

    public void bind() {
        if (this.shaderInstance != null) {
            this.timeUniform.set((System.currentTimeMillis() - AbstractShader.START_TIME) / 1000F);
            this.shaderInstance.enable();
        }
    }

    public void close() {
        this.shaderInstance.close();
    }

    protected abstract String getShaderName();

    public void handleLoad() {
        this.timeUniform = this.shaderInstance.getUniformByNameOrDummy("time");
    }

    public void initialize() {
        final ResourceManager manager = MinecraftClient.getInstance().getResourceManager();
        RenderSystem.recordRenderCall(() -> this.reloadShaders(manager));
        if (manager instanceof ReloadableResourceManager) {
            ((ReloadableResourceManager) manager).registerReloader(this);
        }
    }

    @Override
    public void reload(ResourceManager manager) {
        RenderSystem.recordRenderCall(() -> this.reloadShaders(manager));
    }

    public void reloadShaders(ResourceManager manager) {
        if (this.shaderInstance != null) {
            this.shaderInstance.close();
            this.shaderInstance = null;
        }

        try {
            this.shaderInstance = new JsonEffectGlShader(manager, this.getShaderName());
            this.handleLoad();
        } catch (IOException ex) {
            KZEAddon.LOGGER.error(ex);
        }
    }

    public void unbind() {
        if (this.shaderInstance != null) {
            this.shaderInstance.disable();
        }
    }
}
