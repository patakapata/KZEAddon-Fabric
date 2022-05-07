package com.theboss.kzeaddonfabric.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.theboss.kzeaddonfabric.KZEAddon;
import com.theboss.kzeaddonfabric.utils.RenderUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.Window;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL33.*;

public class FramebufferWrapper {
    // -------------------------------------------------- //
    // Other
    private final String name;
    // -------------------------------------------------- //
    // Screen dimensions
    private int width;
    private int height;
    // -------------------------------------------------- //
    // Buffer references
    private int fbo;
    private int texture;
    private int depth;


    public FramebufferWrapper() {
        this("NoName");
    }

    public FramebufferWrapper(String name) {
        this.name = name;
    }

    public void begin() {
        glBindFramebuffer(GL_FRAMEBUFFER, this.fbo);
        glClearColor(0, 0, 0, 0);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    public void end() {
        MinecraftClient.getInstance().getFramebuffer().beginWrite(false);
    }

    public void blit() {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        MinecraftClient mc = MinecraftClient.getInstance();
        Window window = mc.getWindow();
        int width = window.getScaledWidth();
        int height = window.getScaledHeight();

        buffer.begin(GL_QUADS, VertexFormats.POSITION_TEXTURE);
        buffer.vertex(0, 0, -2000).texture(0, 1).next();
        buffer.vertex(0, height, -2000).texture(0, 0).next();
        buffer.vertex(width, height, -2000).texture(1, 0).next();
        buffer.vertex(width, 0, -2000).texture(1, 1).next();

        GlStateManager.color4f(1F, 1F, 1F, 1F);
        RenderUtils.setupOrthogonalProjectionMatrix();
        GlStateManager.bindTexture(this.texture);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.disableAlphaTest();
        RenderSystem.depthMask(false);

        tessellator.draw();

        RenderSystem.depthMask(true);
        RenderSystem.enableAlphaTest();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        GlStateManager.bindTexture(0);
        RenderUtils.popProjectionMatrix();
    }

    public void checkFBOCompleteness() {
        glBindFramebuffer(GL_FRAMEBUFFER, this.fbo);
        int res = glCheckFramebufferStatus(GL_FRAMEBUFFER);
        MinecraftClient.getInstance().getFramebuffer().beginWrite(false);

        switch (res) {
            case GL_FRAMEBUFFER_COMPLETE:
                KZEAddon.LOGGER.info(this.name + " > " + "GL_FRAMEBUFFER_COMPLETE");
                break;
            case GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT:
                KZEAddon.LOGGER.error(this.name + " > " + "GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT");
                break;
            case GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT:
                KZEAddon.LOGGER.error(this.name + " > " + "GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT");
                break;
            case GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER:
                KZEAddon.LOGGER.error(this.name + " > " + "GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER");
                break;
            case GL_FRAMEBUFFER_ATTACHMENT_OBJECT_TYPE:
                KZEAddon.LOGGER.error(this.name + " > " + "GL_FRAMEBUFFER_ATTACHMENT_OBJECT_TYPE");
                break;
            case GL_FRAMEBUFFER_UNSUPPORTED:
                KZEAddon.LOGGER.error(this.name + " > " + "GL_FRAMEBUFFER_UNSUPPORTED");
                break;
            case GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE:
                KZEAddon.LOGGER.error(this.name + " > " + "GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE");
                break;
            case GL_FRAMEBUFFER_INCOMPLETE_LAYER_TARGETS:
                KZEAddon.LOGGER.error(this.name + " > " + "GL_FRAMEBUFFER_INCOMPLETE_LAYER_TARGETS");
                break;
            case GL_FRAMEBUFFER_UNDEFINED:
                KZEAddon.LOGGER.error(this.name + " > " + "GL_FRAMEBUFFER_UNDEFINED");
                break;
            case 0:
                KZEAddon.LOGGER.error(this.name + " > " + "Un expected error");
        }
    }

    public void close() {
        if (glIsFramebuffer(this.fbo))
            GlStateManager.deleteFramebuffers(this.fbo);
        if (glIsTexture(this.texture))
            GlStateManager.deleteTexture(this.texture);
        if (glIsTexture(this.depth))
            GlStateManager.deleteTexture(this.depth);

        this.fbo = 0;
        this.texture = 0;
        this.depth = 0;
    }

    public int getColorTexture() {
        return this.texture;
    }

    public int getDepthTexture() {
        return this.depth;
    }

    public int getHeight() {
        return this.height;
    }

    public int getWidth() {
        return this.width;
    }

    private void initDepthTexture() {
        this.depth = GlStateManager.genTextures();
        GlStateManager.bindTexture(this.depth);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT, this.width, this.height, 0, GL_DEPTH_COMPONENT, GL_UNSIGNED_BYTE, (ByteBuffer) null);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_DEPTH_TEXTURE_MODE, GL_LUMINANCE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_MODE, GL_NONE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_FUNC, GL_LEQUAL);
        GlStateManager.bindTexture(0);
    }

    private void initTexture() {
        this.texture = GlStateManager.genTextures();
        GlStateManager.bindTexture(this.texture);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, this.width, this.height, 0, GL_RGBA, GL_UNSIGNED_BYTE, (ByteBuffer) null);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_DEPTH_TEXTURE_MODE, GL_LUMINANCE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_MODE, GL_NONE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_FUNC, GL_LEQUAL);
        GlStateManager.bindTexture(0);
    }

    public void initialize(Window window) {
        this.initialize(window.getFramebufferWidth(), window.getFramebufferHeight());
    }

    public void initialize(int width, int height) {
        this.width = width;
        this.height = height;

        KZEAddon.LOGGER.info(this.name + " > Initialize fbo (res." + this.width + "x" + this.height + ")");

        this.close();
        this.initTexture();
        this.initDepthTexture();

        this.fbo = GlStateManager.genFramebuffers();
        GlStateManager.bindFramebuffer(GL_FRAMEBUFFER, this.fbo);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, this.texture, 0);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, this.depth, 0);
        GlStateManager.bindFramebuffer(GL_FRAMEBUFFER, 0);

        this.checkFBOCompleteness();
    }

    public void loadColor(Framebuffer framebuffer) {
        glBindFramebuffer(GL_READ_FRAMEBUFFER, framebuffer.fbo);
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, this.fbo);
        glBlitFramebuffer(
                0, 0, framebuffer.viewportWidth, framebuffer.viewportHeight,
                0, 0, this.width, this.height,
                GL_COLOR_BUFFER_BIT, GL_LINEAR
        );
    }

    public void loadDepth(Framebuffer framebuffer) {
        this.loadDepth(framebuffer.fbo, framebuffer.viewportWidth, framebuffer.viewportHeight);
    }

    public void loadDepth(int fbo, int width, int height) {
        glBindFramebuffer(GL_READ_FRAMEBUFFER, fbo);
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, this.fbo);
        glBlitFramebuffer(
                0, 0, width, height,
                0, 0, this.width, this.height,
                GL_DEPTH_BUFFER_BIT, GL_NEAREST
        );
    }

    public void saveDepth(Framebuffer framebuffer) {
        glBindFramebuffer(GL_READ_FRAMEBUFFER, this.fbo);
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, framebuffer.fbo);
        glBlitFramebuffer(
                0, 0, this.width, this.height,
                0, 0, framebuffer.viewportWidth, framebuffer.viewportHeight,
                GL_DEPTH_BUFFER_BIT, GL_NEAREST
        );
    }

    public void setSize(Window window) {
        this.width = window.getFramebufferWidth();
        this.height = window.getFramebufferHeight();

        GlStateManager.bindTexture(this.texture);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, this.width, this.height, 0, GL_RGBA, GL_UNSIGNED_BYTE, (ByteBuffer) null);
        GlStateManager.bindTexture(this.depth);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT, this.width, this.height, 0, GL_DEPTH_COMPONENT, GL_UNSIGNED_BYTE, (ByteBuffer) null);
        GlStateManager.bindTexture(0);
    }
}
