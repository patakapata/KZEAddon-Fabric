package com.theboss.kzeaddonfabric.render.shader;

import com.mojang.blaze3d.systems.RenderSystem;
import com.theboss.kzeaddonfabric.KZEAddon;
import com.theboss.kzeaddonfabric.mixin.accessor.JsonEffectGlShaderAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.JsonEffectGlShader;
import net.minecraft.resource.ReloadableResourceManager;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SynchronousResourceReloader;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractShader implements SynchronousResourceReloader {
    protected JsonEffectGlShader shaderInstance;
    private final List<Integer> textures = new ArrayList<>();

    public void bind() {
        List<Integer> samplerLocs = this.getSamplerLocs();
        if (samplerLocs != null) {
            this.textures.clear();
            for (int id = 0; id < samplerLocs.size(); id++) {
                RenderSystem.activeTexture('蓀' + id);
                this.textures.add(GL20.glGetInteger(GL20.GL_TEXTURE_BINDING_2D));
            }
            RenderSystem.activeTexture(GL20.GL_TEXTURE0);
        }

        if (this.shaderInstance != null) {
            this.shaderInstance.enable();
        }
    }

    public void unbind() {
        if (this.shaderInstance != null) {
            this.shaderInstance.disable();
        }

        for (int id = 0; id < this.textures.size(); id++) {
            RenderSystem.activeTexture('蓀' + id);
            GL20.glBindTexture(GL11.GL_TEXTURE_2D, this.textures.get(id));
        }
        this.textures.clear();
        RenderSystem.activeTexture(GL20.GL_TEXTURE0);
    }

    public void close() {
        this.shaderInstance.close();
    }

    protected abstract String getShaderName();

    abstract public void handleLoad();

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

    public List<Integer> getSamplerLocs() {
        if (this.shaderInstance != null) return ((JsonEffectGlShaderAccessor) this.shaderInstance).getSamplerShaderLocs();
        return null;
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
}
