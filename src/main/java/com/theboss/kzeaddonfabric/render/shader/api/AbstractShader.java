package com.theboss.kzeaddonfabric.render.shader.api;

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
import org.lwjgl.opengl.GL33;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractShader implements SynchronousResourceReloader {
    private final List<Integer> textures = new ArrayList<>();
    protected JsonEffectGlShader shaderInstance;

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

    public void close() {
        this.shaderInstance.close();
    }

    public int getAttribLocByName(String name) {
        int index = ((JsonEffectGlShaderAccessor) this.shaderInstance).getAttribNames().indexOf(name);
        return index == -1 ? index : ((JsonEffectGlShaderAccessor) this.shaderInstance).getAttribLocs().get(index);
    }

    public int getProgram() {
        return this.shaderInstance.getProgramRef();
    }

    public List<Integer> getSamplerLocs() {
        if (this.shaderInstance != null) return ((JsonEffectGlShaderAccessor) this.shaderInstance).getSamplerShaderLocs();
        return null;
    }

    protected abstract String getShaderName();

    abstract public void handleLoad();

    public void initialize() {
        final ResourceManager manager = MinecraftClient.getInstance().getResourceManager();
        KZEAddon.LOGGER.info("Initializing shader: " + this.getShaderName());
        this.reloadShaders(manager);
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

        for (int id = 0; id < this.textures.size(); id++) {
            RenderSystem.activeTexture('蓀' + id);
            GL20.glBindTexture(GL11.GL_TEXTURE_2D, this.textures.get(id));
        }
        this.textures.clear();
        RenderSystem.activeTexture(GL20.GL_TEXTURE0);
    }
}
