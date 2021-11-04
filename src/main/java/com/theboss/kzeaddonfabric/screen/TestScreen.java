package com.theboss.kzeaddonfabric.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.theboss.kzeaddonfabric.ModUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.Matrix4f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

public class TestScreen extends Screen {

    public TestScreen() {
        super(Text.of("Test Screen"));
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        MinecraftClient mc = MinecraftClient.getInstance();
        Window window = mc.getWindow();

        matrices.push();
        RenderSystem.disableCull();
        matrices.translate(this.width / 2F, this.height / 2F, 0);
        // RenderSystem.disableTexture();
        RenderSystem.shadeModel(GL11.GL_SMOOTH);

        RenderSystem.matrixMode(GL11.GL_PROJECTION);
        RenderSystem.pushMatrix();
        RenderSystem.loadIdentity();
        // RenderSystem.multMatrix(VanillaUtils.getProjectionMatrix(delta));
        RenderSystem.ortho(0.0D, (double) window.getFramebufferWidth() / window.getScaleFactor(), (double) window.getFramebufferHeight() / window.getScaleFactor(), 0.0D, 1000.0D, 3000.0D);
        RenderSystem.matrixMode(GL11.GL_MODELVIEW);

        Matrix4f matrix = matrices.peek().getModel();
        buffer.begin(GL11.GL_QUADS, VertexFormats.POSITION_COLOR);
        buffer.vertex(matrix, -5, -5, 0).color(1F, 0F, 0F, 1F).next();
        buffer.vertex(matrix, -5, 5, 0).color(1F, 1F, 0F, 1F).next();
        buffer.vertex(matrix, 5, 5, 0).color(0F, 0F, 1F, 1F).next();
        buffer.vertex(matrix, 5, -5, 0).color(0F, 1F, 0F, 1F).next();
        tessellator.draw();

        RenderSystem.matrixMode(GL11.GL_PROJECTION);
        RenderSystem.popMatrix();
        RenderSystem.matrixMode(GL11.GL_MODELVIEW);

        RenderSystem.shadeModel(GL11.GL_FLAT);
        RenderSystem.enableCull();
        matrices.pop();
        // RenderSystem.enableTexture();

        this.renderMatrixContents(matrices, mouseX, mouseY, ModUtils.getKeyState(GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS);
    }
}
