package com.theboss.kzeaddonfabric.wip;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.GlAllocationUtils;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

public class BillboardTest {
    public static BillboardTest INSTANCE = new BillboardTest();

    public List<BlockPos> locations = new ArrayList<>();
    public FloatBuffer MATRIX_BUFFER = GlAllocationUtils.allocateFloatBuffer(4 * 4 * 4);

    public static BillboardTest getInstance() {
        return BillboardTest.INSTANCE;
    }

    public BillboardTest() {
        this.locations.add(new BlockPos(0, 20, 0));
    }

    public void render(MatrixStack matrices, float delta) {
        MinecraftClient mc = MinecraftClient.getInstance();
        Camera camera = mc.gameRenderer.getCamera();
        Vec3d cam = camera.getPos();
        matrices.push();
        matrices.translate(-cam.x, -cam.y, -cam.z);
        matrices.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(-camera.getPitch()));
        matrices.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(180 - (camera.getYaw() + 180.0F)));
        matrices.peek().getModel().writeToBuffer(this.MATRIX_BUFFER);
        GL11.glMultMatrixf(this.MATRIX_BUFFER);

        RenderSystem.disableTexture();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, VertexFormats.POSITION_COLOR);
        for (BlockPos pos : this.locations) {
            int x = pos.getX();
            int y = pos.getY();
            int z = pos.getZ();
            buffer.vertex(x - 0.25F, y - 0.5F, z).color(255, 255, 255, 255).next();
            buffer.vertex(x - 0.25F, y + 0.5F, z).color(255, 255, 255, 255).next();
            buffer.vertex(x + 0.25F, y + 0.5F, z).color(255, 255, 255, 255).next();
            buffer.vertex(x + 0.25F, y - 0.5F, z).color(255, 255, 255, 255).next();
        }
        tessellator.draw();
        RenderSystem.enableTexture();
        GL11.glLoadIdentity();
        matrices.pop();
    }
}
