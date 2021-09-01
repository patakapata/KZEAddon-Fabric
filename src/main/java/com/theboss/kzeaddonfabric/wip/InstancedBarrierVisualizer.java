package com.theboss.kzeaddonfabric.wip;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.theboss.kzeaddonfabric.KZEAddon;
import com.theboss.kzeaddonfabric.render.shader.BarrierShader;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.profiler.Profiler;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Objects;
import java.util.Queue;

import static com.theboss.kzeaddonfabric.KZEAddon.warn;
import static org.lwjgl.opengl.GL33.*;

public class InstancedBarrierVisualizer {
    public static final InstancedBarrierVisualizer INSTANCE = new InstancedBarrierVisualizer(new BlockPos(0, 0, 0), 16);

    protected BlockPos center;
    protected BlockPos start;
    protected BlockPos end;
    protected int vao;
    protected float radius;
    protected int modelVBO;
    protected int offsetVBO;
    protected FloatBufferWrapper modelBuffer;
    protected FloatBufferWrapper offsetBuffer;
    protected Queue<Runnable> renderQueue = new ArrayDeque<>();
    protected int modelVertices;
    protected int barriersCount;
    protected int modelVBOSize;
    protected int offsetVBOSize;

    public static int whichNear(BlockPos center, BlockPos a, BlockPos b) {
        double aD = center.getSquaredDistance(a);
        double bD = center.getSquaredDistance(b);

        return Double.compare(aD, bD);
    }

    public InstancedBarrierVisualizer(BlockPos center, float radius) {
        this.center = center;
        this.start = center.add(-radius, -radius, -radius);
        this.end = center.add(radius, radius, radius);
        this.radius = radius;
    }

    public void close() {
        glDeleteVertexArrays(this.vao);
        GlStateManager.deleteBuffers(this.modelVBO);
        GlStateManager.deleteBuffers(this.offsetVBO);
    }

    public int getBarriersCount() {
        return this.barriersCount;
    }

    public int getModelBufferSize() {
        return this.modelBuffer.size();
    }

    public int getModelVBOSize() {
        return this.modelVBOSize;
    }

    public int getModelVertices() {
        return this.modelVertices;
    }

    public int getOffsetBufferSize() {
        return this.offsetBuffer.size();
    }

    public int getOffsetVBOSize() {
        return this.offsetVBOSize;
    }

    public void initialize() {
        this.modelBuffer = new FloatBufferWrapper(2097152, it -> this.renderQueue.add(this::updateModelBufferSize));
        this.offsetBuffer = new FloatBufferWrapper(2097152, it -> this.renderQueue.add(this::updateOffsetBufferSize));
        this.modelVBOSize = 2097152;
        this.offsetVBOSize = 2097152;

        this.vao = glGenVertexArrays();

        this.modelVBO = GlStateManager.genBuffers();
        this.offsetVBO = GlStateManager.genBuffers();

        glBindVertexArray(this.vao);

        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        glBindBuffer(GL_ARRAY_BUFFER, this.modelVBO);
        glBufferData(GL_ARRAY_BUFFER, this.modelBuffer.size(), GL_DYNAMIC_DRAW);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        glBindBuffer(GL_ARRAY_BUFFER, this.offsetVBO);
        glBufferData(GL_ARRAY_BUFFER, this.offsetBuffer.size(), GL_DYNAMIC_DRAW);
        glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        glVertexAttribDivisor(0, 0);
        glVertexAttribDivisor(1, 1);

        glBindVertexArray(0);

        this.setModel(new float[]{
                -0.4F, -0.4F, -0.4F,
                -0.4F, -0.4F, 0.4F,
                -0.4F, -0.4F, 0.4F,
                0.4F, -0.4F, 0.4F,
                0.4F, -0.4F, 0.4F,
                0.4F, -0.4F, -0.4F,
                0.4F, -0.4F, -0.4F,
                -0.4F, -0.4F, -0.4F,
                -0.4F, 0.4F, -0.4F,
                -0.4F, 0.4F, 0.4F,
                -0.4F, 0.4F, 0.4F,
                0.4F, 0.4F, 0.4F,
                0.4F, 0.4F, 0.4F,
                0.4F, 0.4F, -0.4F,
                0.4F, 0.4F, -0.4F,
                -0.4F, 0.4F, -0.4F,
                -0.4F, -0.4F, -0.4F,
                -0.4F, 0.4F, -0.4F,
                -0.4F, -0.4F, 0.4F,
                -0.4F, 0.4F, 0.4F,
                0.4F, -0.4F, 0.4F,
                0.4F, 0.4F, 0.4F,
                0.4F, -0.4F, -0.4F,
                0.4F, 0.4F, -0.4F
        });
    }

    protected void pollRenderQueue() {
        Runnable task;
        while ((task = this.renderQueue.poll()) != null) {
            task.run();
        }
    }

    protected Iterable<BlockPos> regionIterable() {
        return BlockPos.iterate(this.start, this.end);
    }

    public void render(float delta) {
        Profiler profiler = KZEAddon.getProfiler();

        KZEAddon.glError("pollRenderQueue");
        profiler.push("pollRenderQueue");
        this.pollRenderQueue();

        MinecraftClient mc = MinecraftClient.getInstance();
        Vec3d center = Objects.requireNonNull(mc.player).getPos();

        KZEAddon.glError("Uniforms setup");
        profiler.swap("Uniforms setup");
        BarrierShader.INSTANCE.setMVP(KZEAddon.getVPMatrix(delta));
        BarrierShader.INSTANCE.setCenter((float) center.getX(), (float) center.getY(), (float) center.getZ());
        BarrierShader.INSTANCE.setRadius(this.radius);

        KZEAddon.glError("Draw");
        profiler.swap("Draw");
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        BarrierShader.INSTANCE.bind();
        glBindVertexArray(this.vao);
        glDrawArraysInstanced(GL_LINES, 0, this.modelVertices, this.barriersCount);
        glBindVertexArray(0);
        BarrierShader.INSTANCE.unbind();
        profiler.pop();
        KZEAddon.glError("After draw");
    }

    public void setCenter(BlockPos center) {
        this.center = center;
        this.start = center.add(-this.radius, -this.radius, -this.radius);
        this.end = center.add(this.radius, this.radius, this.radius);
    }

    public void setModel(float[] vertices) {
        this.modelBuffer.clear();
        this.modelBuffer.grow(vertices.length * 4);
        this.modelBuffer.put(vertices);
        this.modelBuffer.flip();

        this.modelVertices = vertices.length / 3;
        this.renderQueue.add(this::uploadModelBuffer);
    }

    public void setRadius(float radius) {
        this.radius = radius;
        this.setCenter(this.center);
    }

    public void tick() {
        Profiler profiler = KZEAddon.getProfiler();
        profiler.push("updateOffsets");
        this.updateOffsets();
        profiler.pop();
    }

    public void updateModelBufferSize() {
        warn("ModelVBO size changed to " + this.modelBuffer.size());
        glBindBuffer(GL_ARRAY_BUFFER, this.modelVBO);
        glBufferData(GL_ARRAY_BUFFER, this.modelBuffer.buffer, GL_DYNAMIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        this.modelVBOSize = this.modelBuffer.size();
    }

    public void updateOffsetBufferSize() {
        warn("OffsetVBO size changed to " + this.offsetBuffer.size());
        glBindBuffer(GL_ARRAY_BUFFER, this.offsetVBO);
        glBufferData(GL_ARRAY_BUFFER, this.offsetBuffer.buffer, GL_DYNAMIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        this.offsetVBOSize = this.offsetBuffer.size();
    }

    protected void updateOffsets() {
        Profiler profiler = KZEAddon.getProfiler();
        ClientWorld world = MinecraftClient.getInstance().world;
        Iterator<BlockPos> iterator = this.regionIterable().iterator();

        if (world == null) return;

        profiler.push("Write to buffer");
        this.offsetBuffer.clear();

        this.barriersCount = 0;
        while (iterator.hasNext()) {
            BlockPos pos = iterator.next();
            BlockState state = world.getBlockState(pos);

            if (state.getBlock().equals(Blocks.BARRIER)) {
                this.offsetBuffer.grow(12);
                this.offsetBuffer.put(new float[]{
                        pos.getX() + 0.5F,
                        pos.getY() + 0.5F,
                        pos.getZ() + 0.5F
                });
                this.barriersCount++;
            }
        }

        this.offsetBuffer.flip();
        this.renderQueue.add(this::uploadOffsetBuffer);
        profiler.pop();
    }

    public void uploadModelBuffer() {
        KZEAddon.glError("uploadModelBuffer head");

        glBindBuffer(GL_ARRAY_BUFFER, this.modelVBO);
        glBufferSubData(GL_ARRAY_BUFFER, 0, this.modelBuffer.buffer);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        KZEAddon.glError("uploadModelBuffer tail");
    }

    public void uploadOffsetBuffer() {
        KZEAddon.glError("uploadOffsetBuffer head");

        glBindBuffer(GL_ARRAY_BUFFER, this.offsetVBO);
        glBufferSubData(GL_ARRAY_BUFFER, 0, this.offsetBuffer.buffer);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        KZEAddon.glError("uploadOffsetBuffer tail");
    }
}
