package com.theboss.kzeaddonfabric.wip;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.theboss.kzeaddonfabric.KZEAddon;
import com.theboss.kzeaddonfabric.render.shader.ScanShader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.texture.TextureUtil;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL30;

public final class FrameBufferLearn {
    public static final FrameBufferLearn INSTANCE = new FrameBufferLearn();
    private static final long START_TIME = System.currentTimeMillis();

    private int fbo;
    private int colorTexture;
    private int depthTexture;
    private boolean shouldTextureResize;
    private boolean shouldRender;

    private int texWidth;
    private int texHeight;
    private Vec3f center = new Vec3f(0F, 0F, 0F);
    private Vec3f prevCenter = new Vec3f(0F, 0F, 0F);
    private float width = 0.1F;

    private FrameBufferLearn() {}

    public void blit(Framebuffer framebuffer) {
        int width = framebuffer.viewportWidth;
        int height = framebuffer.viewportHeight;

        RenderSystem.depthMask(false);
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.disableCull();

        int oldTex = GlStateManager.getInteger(GL11.GL_TEXTURE_BINDING_2D);
        RenderSystem.bindTexture(this.depthTexture);

        this.setupMatrices(width, height);
        framebuffer.beginWrite(false);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, VertexFormats.POSITION_TEXTURE);
        buffer.vertex(0, height, 0).texture(0F, 0F).next();
        buffer.vertex(width, height, 0).texture(1F, 0F).next();
        buffer.vertex(width, 0, 0).texture(1F, 1F).next();
        buffer.vertex(0, 0, 0).texture(0F, 1F).next();
        tessellator.draw();

        this.restoreMatrices();

        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.bindTexture(oldTex);
        RenderSystem.enableCull();
    }

    public void close() {
        GlStateManager.deleteFramebuffers(this.fbo);
        TextureUtil.deleteId(this.colorTexture);
        TextureUtil.deleteId(this.depthTexture);
        ScanShader.INSTANCE.setDepthTex(0);

        this.fbo = 0;
        this.colorTexture = 0;
        this.depthTexture = 0;
        this.texWidth = 0;
        this.texHeight = 0;
    }

    private void createDepthCopyFramebuffer() {
        Framebuffer framebuffer = MinecraftClient.getInstance().getFramebuffer();

        this.fbo = GlStateManager.genFramebuffers();

        this.colorTexture = this.createTexture(framebuffer.viewportWidth, framebuffer.viewportHeight, GL11.GL_RGBA8, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE);
        this.depthTexture = this.createTexture(framebuffer.viewportWidth, framebuffer.viewportHeight, GL30.GL_DEPTH_COMPONENT, GL30.GL_DEPTH_COMPONENT, GL11.GL_FLOAT);

        this.texWidth = framebuffer.viewportWidth;
        this.texHeight = framebuffer.viewportHeight;

        GlStateManager.bindFramebuffer(GL30.GL_FRAMEBUFFER, this.fbo);
        GlStateManager.framebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, this.colorTexture, 0);
        GlStateManager.framebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL11.GL_TEXTURE_2D, this.depthTexture, 0);
        GlStateManager.bindFramebuffer(GL30.GL_FRAMEBUFFER, 0);

        ScanShader.INSTANCE.setDepthTex(this.depthTexture);
    }

    private int createTexture(int width, int height, int internalFormat, int format, int type) {
        int texture = TextureUtil.generateId();

        GlStateManager.bindTexture(texture);
        GlStateManager.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
        GlStateManager.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
        GlStateManager.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GlStateManager.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GlStateManager.texParameter(GL11.GL_TEXTURE_2D, GL14.GL_DEPTH_TEXTURE_MODE, GL11.GL_LUMINANCE);
        GlStateManager.texParameter(GL11.GL_TEXTURE_2D, GL14.GL_TEXTURE_COMPARE_MODE, GL14.GL_NONE);
        GlStateManager.texParameter(GL11.GL_TEXTURE_2D, GL14.GL_TEXTURE_COMPARE_FUNC, GL14.GL_LEQUAL);
        GlStateManager.texImage2D(GL11.GL_TEXTURE_2D, 0, internalFormat, width, height, 0, format, type, null);
        GlStateManager.bindTexture(0);

        return texture;
    }

    public void doRender(float delta) {

        if (this.shouldRender) {
            if (this.fbo == 0) {
                this.createDepthCopyFramebuffer();
                KZEAddon.LOGGER.warn("FBO GENERATED > " + this.texWidth + "x" + this.texHeight);
            }
            this.render(delta);
        } else {
            if (this.fbo != 0) {
                this.close();
            }
        }
    }

    public Vec3f getCenter() {
        return this.center;
    }

    public void setCenter(@NotNull Vec3d pos) {
        this.setCenter(new Vec3f(pos));
    }

    public void setCenter(@NotNull Vec3f pos) {
        this.center = pos;
        if (this.prevCenter == null) this.prevCenter = this.center;
    }

    public int getColorTexture() {
        return this.colorTexture;
    }

    public int getDepthTexture() {
        return this.depthTexture;
    }

    public Vec3f getPrevCenter() {
        return this.prevCenter;
    }

    public float getWidth() {
        return this.width;
    }

    public void setWidth(float width) {
        this.width = width;
        ScanShader.INSTANCE.setWidth(width);
    }

    private void internalUpdateTexSize(int id, int width, int height, int internalFormat, int format, int type) {
        this.shouldTextureResize = false;
        //
        //        TextureUtil.deleteId(this.colorTexture);
        //        TextureUtil.deleteId(this.depthTexture);
        //
        //        this.colorTexture = this.createTexture(framebuffer.viewportWidth, framebuffer.viewportHeight, GL11.GL_RGBA8, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE);
        //        this.depthTexture = this.createTexture(framebuffer.viewportWidth, framebuffer.viewportHeight, GL30.GL_DEPTH_COMPONENT, GL30.GL_DEPTH_COMPONENT, GL11.GL_FLOAT);
        //
        //        GlStateManager.bindFramebuffer(GL30.GL_FRAMEBUFFER, this.fbo);
        //        GlStateManager.framebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, this.colorTexture, 0);
        //        GlStateManager.framebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL11.GL_TEXTURE_2D, this.depthTexture, 0);
        //        GlStateManager.bindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
        //
        //        ScanShader.INSTANCE.setDepthTex(this.depthTexture);

        GlStateManager.bindTexture(id);
        GlStateManager.texImage2D(GL11.GL_TEXTURE_2D, 0, internalFormat, width, height, 0, format, type, null);
        GlStateManager.bindTexture(0);
    }

    private void internalUpdateTexturesSize(Framebuffer framebuffer) {
        this.internalUpdateTexSize(this.colorTexture, framebuffer.viewportWidth, framebuffer.viewportHeight, GL11.GL_RGBA8, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE);
        this.internalUpdateTexSize(this.depthTexture, framebuffer.viewportWidth, framebuffer.viewportHeight, GL30.GL_DEPTH_COMPONENT, GL30.GL_DEPTH_COMPONENT, GL11.GL_FLOAT);
    }

    public boolean isShouldRender() {
        return this.shouldRender;
    }

    public void setShouldRender(boolean isShouldRender) {
        this.shouldRender = isShouldRender;
    }

    public Vec3f lerpCenter(float delta) {
        return new Vec3f(
                MathHelper.lerp(delta, this.prevCenter.getX(), this.center.getX()),
                MathHelper.lerp(delta, this.prevCenter.getY(), this.center.getY()),
                MathHelper.lerp(delta, this.prevCenter.getZ(), this.center.getZ())
        );
    }

    private void render(float delta) {
        MinecraftClient mc = MinecraftClient.getInstance();
        Framebuffer framebuffer = mc.getFramebuffer();

        this.updateDepthTexture(framebuffer);

        Matrix4f invViewMatrix = KZEAddon.getViewMatrix(delta).peek().getModel();
        invViewMatrix.invert();
        ScanShader.INSTANCE.setInvViewMat(invViewMatrix);

        Matrix4f invProjMatrix = KZEAddon.getProjectionMatrix(delta);
        invProjMatrix.invert();
        ScanShader.INSTANCE.setInvProjMat(invProjMatrix);

        Vec3f pos = new Vec3f(mc.gameRenderer.getCamera().getPos());
        ScanShader.INSTANCE.setPosition(pos);
        ScanShader.INSTANCE.setCenter(this.lerpCenter(delta));

        float time = (System.currentTimeMillis() - FrameBufferLearn.START_TIME) / 1_000.0F;
        time = time % 1.0F / 1.0F;
        float radius = 5.0F + (float) Math.abs(Math.sin(time * Math.PI * 2.0)) * 3.0F;
        radius = 3.0F;

        ScanShader.INSTANCE.setRadius(radius);
        ScanShader.INSTANCE.bind();

        this.blit(framebuffer);

        ScanShader.INSTANCE.unbind();
    }

    private void restoreMatrices() {
        RenderSystem.matrixMode(GL11.GL_PROJECTION);
        RenderSystem.popMatrix();
        RenderSystem.matrixMode(GL11.GL_MODELVIEW);
        RenderSystem.popMatrix();
    }

    public void setCenter(float x, float y, float z) {
        this.setCenter(new Vec3f(x, y, z));
    }

    private void setupMatrices(int width, int height) {
        // -------------------------------------------------- //
        // Setup the projection matrix
        RenderSystem.matrixMode(GL11.GL_PROJECTION);
        RenderSystem.pushMatrix();
        RenderSystem.loadIdentity();
        RenderSystem.ortho(0, width, height, 0, 1000, 3000);
        // -------------------------------------------------- //
        // Setup the modelview matrix
        RenderSystem.matrixMode(GL11.GL_MODELVIEW);
        RenderSystem.pushMatrix();
        RenderSystem.loadIdentity();
        RenderSystem.translated(0, 0, -2000);
        RenderSystem.viewport(0, 0, width, height);
    }

    public void tick() {
        this.prevCenter = this.center;
    }

    private void updateDepthTexture(Framebuffer framebuffer) {
        if (this.shouldTextureResize) this.internalUpdateTexturesSize(framebuffer);
        GlStateManager.bindFramebuffer(GL30.GL_READ_FRAMEBUFFER, framebuffer.fbo);
        GlStateManager.bindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, this.fbo);
        GL30.glBlitFramebuffer(
                0, 0,
                framebuffer.viewportWidth, framebuffer.viewportHeight,
                0, 0,
                framebuffer.viewportWidth, framebuffer.viewportHeight,
                GL30.GL_DEPTH_BUFFER_BIT, GL30.GL_NEAREST
        );
        GL30.glBlitFramebuffer(
                0, 0,
                framebuffer.viewportWidth, framebuffer.viewportHeight,
                0, 0,
                framebuffer.viewportWidth, framebuffer.viewportHeight,
                GL30.GL_COLOR_BUFFER_BIT, GL30.GL_NEAREST
        );
    }

    public void updateTextureSize() {
        this.shouldTextureResize = true;
    }
}
