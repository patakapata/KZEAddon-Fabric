package com.theboss.kzeaddonfabric.wip;

import com.mojang.blaze3d.platform.GlStateManager;
import com.theboss.kzeaddonfabric.KZEAddon;
import com.theboss.kzeaddonfabric.SimplexNoise;
import com.theboss.kzeaddonfabric.mixin.Matrix4fAccessor;
import com.theboss.kzeaddonfabric.render.shader.ParticleShader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.GlAllocationUtils;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix4f;
import org.lwjgl.opengl.GL11;

import java.nio.FloatBuffer;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.lwjgl.opengl.GL33.*;

public class InstancedPerlinParticleManager {
    public final static InstancedPerlinParticleManager INSTANCE = new InstancedPerlinParticleManager();

    private final Queue<RenderTask> tasks = new ArrayDeque<>();
    private final List<ParticleInstance> particles = new CopyOnWriteArrayList<>();
    private FloatBuffer modelBuffer;
    private FloatBuffer offsetBuffer;
    private int vao;
    private int modelVBO;
    private int offsetVBO;
    private int particlesCount;
    private int modelVertices;

    public static int round(int origin, int threshold) {
        if (threshold < 0 || origin % threshold == 0) return origin;
        int factor = origin / threshold;
        return threshold * (factor + 1);
    }

    private static int roundBufferSize(int size) {
        // Round by 2 MB
        return InstancedPerlinParticleManager.round(size, 2097152);
    }

    private InstancedPerlinParticleManager() {}

    public void addParticle(ParticleInstance particle) {
        this.particles.add(particle);
    }

    public void clearParticles() {
        this.particles.clear();
    }

    /**
     * 片付け
     */
    public void close() {
        glDeleteVertexArrays(this.vao);
        GlStateManager.deleteBuffers(this.modelVBO);
        GlStateManager.deleteFramebuffers(this.offsetVBO);
    }

    public int getModelVertices() {
        return this.modelVertices;
    }

    public int getParticlesCount() {
        return this.particlesCount;
    }

    /**
     * バッファの残り容量が指定値より低い場合、
     * 2MBで丸めた新サイズのバッファーを
     * 現在のバッファーと取り替えます
     *
     * @param expectedRemainSize バイト単位
     */
    private void growModelBuffer(int expectedRemainSize) {
        int remainSize = this.modelBuffer.remaining();
        if (remainSize < expectedRemainSize) {
            int roundedSize = InstancedPerlinParticleManager.roundBufferSize(this.modelBuffer.capacity() + expectedRemainSize);
            FloatBuffer newBuffer = GlAllocationUtils.allocateByteBuffer(roundedSize).asFloatBuffer();
            this.modelBuffer.flip();
            newBuffer.put(this.modelBuffer);

            this.modelBuffer = newBuffer;
            this.tasks.add(() -> {
                glBindBuffer(GL_ARRAY_BUFFER, this.modelVBO);
                glBufferData(GL_ARRAY_BUFFER, this.modelBuffer, GL_DYNAMIC_DRAW);
                glBindBuffer(GL_ARRAY_BUFFER, 0);
            });
        }
    }

    /**
     * バッファの残り容量が指定値より低い場合、
     * 2MBで丸めた新サイズのバッファーを
     * 現在のバッファーと取り替えます
     *
     * @param expectedRemainSize バイト単位
     */
    private void growOffsetBuffer(int expectedRemainSize) {
        int remainSize = this.offsetBuffer.remaining();
        if (remainSize < expectedRemainSize) {
            int roundedSize = InstancedPerlinParticleManager.roundBufferSize(this.offsetBuffer.capacity() + expectedRemainSize);
            KZEAddon.LOGGER.info("OffsetBuffer > Size updated " + this.offsetBuffer.capacity() + " -> " + roundedSize);
            FloatBuffer newBuffer = GlAllocationUtils.allocateByteBuffer(roundedSize).asFloatBuffer();
            this.offsetBuffer.flip();
            newBuffer.put(this.offsetBuffer);

            this.offsetBuffer = newBuffer;
            this.tasks.add(() -> {
                glBindBuffer(GL_ARRAY_BUFFER, this.offsetVBO);
                glBufferData(GL_ARRAY_BUFFER, this.offsetBuffer, GL_DYNAMIC_DRAW);
                glBindBuffer(GL_ARRAY_BUFFER, 0);
            });
        }
    }

    /**
     * 初期化
     */
    public void init() {
        // VAOの生成 & バインド
        this.vao = glGenVertexArrays();
        glBindVertexArray(this.vao);

        // CPU側のバッファを初期化
        this.modelBuffer = GlAllocationUtils.allocateByteBuffer(2097152).asFloatBuffer();
        this.offsetBuffer = GlAllocationUtils.allocateByteBuffer(2097152).asFloatBuffer();

        // VBOの生成
        this.modelVBO = GlStateManager.genBuffers();
        this.offsetVBO = GlStateManager.genBuffers();

        // 初期データの生成
        this.setModel(new float[]{
                -0.1F, 0.1F, 0.0F,
                -0.1F, -0.1F, 0.0F,
                0.1F, -0.1F, 0.0F,
                0.1F, 0.1F, 0.0F
        });

        // 要素の有効化 & 場所の指定 & バッファの容量確保
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        glBindBuffer(GL_ARRAY_BUFFER, this.modelVBO);
        glBufferData(GL_ARRAY_BUFFER, this.modelBuffer, GL_DYNAMIC_DRAW);
        glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 0, 0L);
        glBindBuffer(GL_ARRAY_BUFFER, this.offsetVBO);
        glBufferData(GL_ARRAY_BUFFER, this.offsetBuffer, GL_DYNAMIC_DRAW);
        glVertexAttribPointer(1, 4, GL11.GL_FLOAT, false, 0, 0L);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        // 頂点配列除数を指定
        glVertexAttribDivisor(0, 0);
        glVertexAttribDivisor(1, 1);

        // VAO をアンバインド
        glBindVertexArray(0);
    }

    private void pollRenderTasks() {
        RenderTask task;
        while ((task = this.tasks.poll()) != null) {
            task.run();
        }
    }

    public void render(float delta) {
        this.updatePositions(delta);
        this.pollRenderTasks();

        Matrix4f proj = KZEAddon.getProjectionMatrix(delta);
        Matrix4f view = KZEAddon.getViewMatrix(delta).peek().getModel();
        proj.multiply(view);
        ParticleShader.INSTANCE.setMVP(proj);
        ParticleShader.INSTANCE.setCamRightWS(
                ((Matrix4fAccessor) (Object) view).a00(),
                ((Matrix4fAccessor) (Object) view).a10(),
                ((Matrix4fAccessor) (Object) view).a20()
        );
        ParticleShader.INSTANCE.setCamUpWS(
                ((Matrix4fAccessor) (Object) view).a01(),
                ((Matrix4fAccessor) (Object) view).a11(),
                ((Matrix4fAccessor) (Object) view).a21()
        );

        ParticleShader.INSTANCE.bind();
        glBindVertexArray(this.vao);
        glDrawArraysInstanced(GL_QUADS, 0, this.modelVertices, this.particlesCount);
        glBindVertexArray(0);
        ParticleShader.INSTANCE.unbind();
    }

    public void setModel(float[] vertices) {
        this.modelBuffer.clear();
        this.growModelBuffer(vertices.length * 4);
        this.modelBuffer.put(vertices);
        this.modelBuffer.flip();
        this.tasks.add(this::uploadModelBuffer);
        this.modelVertices = vertices.length / 3;
    }

    public void tick() {
        this.particles.forEach(ParticleInstance::tick);
        this.particles.removeIf(ParticleInstance::isOutdated);
    }

    public void updatePositions(float delta) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.currentScreen != null && mc.currentScreen.isPauseScreen()) return;
        this.offsetBuffer.clear();
        for (ParticleInstance instance : this.particles) {
            this.growOffsetBuffer(16);
            instance.writePosition(delta);
        }
        this.particlesCount = this.particles.size();
        this.offsetBuffer.flip();

        this.tasks.add(this::uploadOffsetBuffer);
    }

    public void uploadModelBuffer() {
        glBindBuffer(GL_ARRAY_BUFFER, this.modelVBO);
        glBufferSubData(GL_ARRAY_BUFFER, 0L, this.modelBuffer);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    public void uploadOffsetBuffer() {
        glBindBuffer(GL_ARRAY_BUFFER, this.offsetVBO);
        glBufferSubData(GL_ARRAY_BUFFER, 0L, this.offsetBuffer);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    @FunctionalInterface
    public interface RenderTask {
        void run();
    }

    public static class ParticleInstance {
        private final double seed;
        private double x;
        private double y;
        private double z;
        private double lastX;
        private double lastY;
        private double lastZ;
        private double vecX;
        private double vecY;
        private double vecZ;
        private int lifeTime;
        private int age;
        private boolean isPersistence;

        public ParticleInstance(double x, double y, double z, int lifeTime, boolean isPersistence, double seed) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.lastX = x;
            this.lastY = y;
            this.lastZ = z;
            this.vecX = 0;
            this.vecY = 0;
            this.vecZ = 0;
            this.lifeTime = lifeTime;
            this.age = 0;
            this.isPersistence = isPersistence;
            this.seed = seed;
        }

        public ParticleInstance(double x, double y, double z, int lifeTime, boolean isPersistence) {
            this(x, y, z, lifeTime, isPersistence, 0.0);
        }

        public ParticleInstance(double x, double y, double z, int lifeTime) {
            this(x, y, z, lifeTime, false, 0.0);
        }

        public int getAge() {
            return this.age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public int getLifeTime() {
            return this.lifeTime;
        }

        public void setLifeTime(int lifeTime) {
            this.lifeTime = lifeTime;
        }

        public double getVecX() {
            return this.vecX;
        }

        public void setVecX(double vecX) {
            this.vecX = vecX;
        }

        public double getVecY() {
            return this.vecY;
        }

        public void setVecY(double vecY) {
            this.vecY = vecY;
        }

        public double getVecZ() {
            return this.vecZ;
        }

        public void setVecZ(double vecZ) {
            this.vecZ = vecZ;
        }

        public double getX() {
            return this.x;
        }

        public void setX(double x) {
            this.x = x;
        }

        public double getY() {
            return this.y;
        }

        public void setY(double y) {
            this.y = y;
        }

        public double getZ() {
            return this.z;
        }

        public void setZ(double z) {
            this.z = z;
        }

        public boolean isOutdated() {
            return !this.isPersistence && this.age >= this.lifeTime;
        }

        public boolean isPersistence() {
            return this.isPersistence;
        }

        public void setPersistence(boolean persistence) {
            this.isPersistence = persistence;
        }

        public void tick() {
            if (!this.isPersistence) this.age++;

            this.lastX = this.x;
            this.lastY = this.y;
            this.lastZ = this.z;

            this.x += SimplexNoise.noise(this.z, this.seed) / 4;
            this.y += SimplexNoise.noise(this.x, this.seed) / 4;
            this.z += SimplexNoise.noise(this.y, this.seed) / 4;
        }

        public void writePosition(float delta) {
            FloatBuffer offset = InstancedPerlinParticleManager.INSTANCE.offsetBuffer;

            offset.put(new float[]{
                    (float) MathHelper.lerp(delta, this.lastX, this.x),
                    (float) MathHelper.lerp(delta, this.lastY, this.y),
                    (float) MathHelper.lerp(delta, this.lastZ, this.z),
                    1 - ((float) this.age / this.lifeTime)
            });
        }
    }
}
