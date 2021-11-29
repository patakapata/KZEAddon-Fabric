package com.theboss.kzeaddonfabric.wip;

import com.mojang.blaze3d.systems.RenderSystem;
import com.theboss.kzeaddonfabric.KZEAddon;
import com.theboss.kzeaddonfabric.render.ByteBufferWrapper;
import com.theboss.kzeaddonfabric.render.OriginalFramebuffer;
import com.theboss.kzeaddonfabric.render.shader.HoloWallShader;
import com.theboss.kzeaddonfabric.utils.RenderingUtils;
import com.theboss.kzeaddonfabric.utils.VanillaUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.*;
import org.lwjgl.opengl.GL11;

import static org.lwjgl.opengl.GL33.*;

public class HoloWall {
    private Direction direction;
    private Vec3d pos;
    private float size;
    private int vao;
    private int vbo;
    private ByteBufferWrapper buffer;
    public final OriginalFramebuffer fbo;

    public HoloWall(Direction direction, Vec3d pos, float size) {
        this.direction = direction;
        this.pos = pos;
        this.size = size;
        this.fbo = new OriginalFramebuffer("HoloWall");
    }

    private void applyRotate(MatrixStack matrices) {
        Quaternion quart;
        switch (this.direction) {
            case NORTH:
            default:
                quart = Vec3f.POSITIVE_Y.getDegreesQuaternion(0);
                break;
            case EAST:
                quart = Vec3f.POSITIVE_Y.getDegreesQuaternion(90);
                break;
            case WEST:
                quart = Vec3f.POSITIVE_Y.getDegreesQuaternion(-90);
                break;
            case SOUTH:
                quart = Vec3f.POSITIVE_Y.getDegreesQuaternion(180);
                break;
            case UP:
                quart = Vec3f.POSITIVE_X.getDegreesQuaternion(90);
                break;
            case DOWN:
                quart = Vec3f.POSITIVE_X.getDegreesQuaternion(-90);
                break;
        }
        matrices.multiply(quart);
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public void init() {
        this.vao = glGenVertexArrays();
        this.vbo = glGenBuffers();
        this.buffer = new ByteBufferWrapper(1024 * 1024 * 2, () -> this.buffer.updateSizeOnVBO(this.vbo));
        this.fbo.initialize(MinecraftClient.getInstance().getWindow());

        glBindVertexArray(this.vao);
        glEnableVertexAttribArray(0);
        glBindBuffer(GL_ARRAY_BUFFER, this.vbo);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

        this.buffer.clear();
        this.buffer.put(-1, 1, 0);
        this.buffer.put(-1, -1, 0);
        this.buffer.put(1, -1, 0);
        this.buffer.put(1, 1, 0);
        this.buffer.flip();
        this.buffer.uploadToVBO(this.vbo);
    }

    public void onWindowResized(Window window) {
        this.fbo.setSize(window);
    }

    public void close() {
        if (glIsVertexArray(this.vao))
            glDeleteVertexArrays(this.vao);
        if (glIsBuffer(this.vbo))
            glDeleteBuffers(this.vbo);
        this.fbo.close();
    }

    public void render(MatrixStack matrices, float delta) {
        MinecraftClient mc = MinecraftClient.getInstance();
        Window window = mc.getWindow();
        Vec3d cam = mc.gameRenderer.getCamera().getPos();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bBuilder = tessellator.getBuffer();

        matrices.push();
        matrices.translate(this.pos.x - cam.x, this.pos.y - cam.y, this.pos.z - cam.z);
        this.applyRotate(matrices);
        Matrix4f matrix = matrices.peek().getModel();

        RenderSystem.enableDepthTest();
        this.fbo.begin();
        this.fbo.loadDepth(mc.getFramebuffer());

        this.drawPlane(mc, tessellator, bBuilder, matrix);
        this.drawRadialPlane(mc, matrices);
        this.drawMessage(matrices, mc);

        matrices.pop();
        this.fbo.end();
        // this.fbo.blit(mc.getFramebuffer());

        RenderingUtils.setupOrthogonalProjectionMatrix();
        Matrix4f projectionMat = VanillaUtils.getProjectionMatrix(delta);
        MatrixStack stack = VanillaUtils.getViewMatrix(delta);
        Matrix4f viewMat = stack.peek().getModel();

        projectionMat.invert();
        viewMat.invert();

        RenderSystem.pushMatrix();
        RenderSystem.loadIdentity();
        RenderSystem.enableBlend();

        HoloWallShader.INSTANCE.setPosition(mc.gameRenderer.getCamera().getPos());
        if (mc.player != null)
            HoloWallShader.INSTANCE.setCenter(new Vec3d(mc.player.lastRenderX, mc.player.lastRenderY, mc.player.lastRenderZ));
        HoloWallShader.INSTANCE.setInverseProjection(projectionMat);
        HoloWallShader.INSTANCE.setInverseView(viewMat);
        HoloWallShader.INSTANCE.setDepthTexture(this.fbo.getDepthTexture());
        HoloWallShader.INSTANCE.setColorTexture(this.fbo.getColorTexture());
        HoloWallShader.INSTANCE.setResolution(window.getFramebufferWidth(), window.getFramebufferHeight());
        HoloWallShader.INSTANCE.setVisibleDistance(5.0F);

        HoloWallShader.INSTANCE.bind();

        glBindVertexArray(this.vao);
        glDrawArrays(GL11.GL_QUADS, 0, 4);
        glBindVertexArray(0);

        HoloWallShader.INSTANCE.unbind();

        RenderSystem.popMatrix();
        RenderingUtils.popProjectionMatrix();
    }

    private void drawRadialPlane(MinecraftClient mc, MatrixStack matrices) {
        matrices.push();
        matrices.translate(0, 0, -0.1);
        matrices.scale(-1, -1, 1);
        RenderingUtils.pushTexture();
        RenderSystem.enableAlphaTest();
        RenderSystem.alphaFunc(GL_GREATER, 0);
        mc.getTextureManager().bindTexture(new Identifier(KZEAddon.MOD_ID, "textures/gui/frame.png"));
        RenderingUtils.drawRadialTexture(matrices, System.currentTimeMillis() % 3_000 / 3_000.0, this.size / 1.5F, 0, 0, 1, 1);
        RenderSystem.disableAlphaTest();
        RenderingUtils.popTexture();
        matrices.pop();
    }

    private void drawMessage(MatrixStack matrices, MinecraftClient mc) {
        Text text = Text.of(String.valueOf(System.currentTimeMillis() % 3_000));
        matrices.push();
        matrices.scale(0.1F, -0.1F, 1);
        matrices.translate(0.0, 0.0, -0.2);
        matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(180));
        mc.textRenderer.drawWithShadow(matrices, text, -mc.textRenderer.getWidth(text) / 2F, -mc.textRenderer.fontHeight / 2F, 0xFFFFFF);
        matrices.pop();
    }

    private void drawPlane(MinecraftClient mc, Tessellator tessellator, BufferBuilder bBuilder, Matrix4f matrix) {
        RenderSystem.enableDepthTest();
        float offset = System.currentTimeMillis() % 3000 / 3000F;
        RenderingUtils.pushTexture();
        mc.getTextureManager().bindTexture(new Identifier("minecraft:textures/misc/forcefield.png"));
        RenderSystem.color4f(1f, 0f, 0f, 1f);

        bBuilder.begin(GL_QUADS, VertexFormats.POSITION_TEXTURE);
        bBuilder.vertex(matrix, this.size, this.size, 0).texture(offset + this.size, offset + this.size).next();
        bBuilder.vertex(matrix, this.size, -this.size, 0).texture(offset + this.size, offset).next();
        bBuilder.vertex(matrix, -this.size, -this.size, 0).texture(offset, offset).next();
        bBuilder.vertex(matrix, -this.size, this.size, 0).texture(offset, offset + this.size).next();
        tessellator.draw();

        RenderSystem.color4f(1f, 1f, 1f, 1f);
        RenderingUtils.popTexture();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(true);
    }

    public void setPosition(Vec3d pos) {
        this.pos = pos;
    }

    public void setSize(float size) {
        this.size = size;
    }

    public Direction getDirection() {
        return this.direction;
    }

    public Vec3d getPosition() {
        return this.pos;
    }

    public float getSize() {
        return this.size;
    }
}
