package com.theboss.kzeaddonfabric.wip;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.theboss.kzeaddonfabric.KZEAddon;
import com.theboss.kzeaddonfabric.render.shader.BarrierShader;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.*;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL33.*;

import static com.theboss.kzeaddonfabric.KZEAddon.info;

public class ChunkInstancedBarrierVisualizer {
    public static final ChunkInstancedBarrierVisualizer INSTANCE = new ChunkInstancedBarrierVisualizer(1);
    private final Queue<Runnable> renderQueue = new ArrayDeque<>();
    private final BlockPos.Mutable lastCenter;
    private final List<Chunk> chunks;
    private BlockPos center;
    private int radius;
    private boolean shouldRebuild;
    private ByteBufferWrapper bufferModel;
    private int vboModel;
    private int modelVertices;
    private Vec3d visualizeCenter;
    private float visualizeRadius;
    private float alphaThreshold;
    private long lastPollQueueTime;

    private int lastDiameter;
    private int lastMoveX;
    private int lastMoveY;
    private int lastMoveZ;
    private boolean lastShouldRebuild;

    public long getLastPollQueueTime() {
        return this.lastPollQueueTime;
    }

    public static BlockPos asChunkPos(Vec3d pos) {
        return new BlockPos(
                pos.x / 16,
                pos.y / 16,
                pos.z / 16
        );
    }

    public static Iterator<Chunk> chunkIterator() {
        return INSTANCE.chunks.iterator();
    }

    public static void recordRenderCall(Runnable task) {
        ChunkInstancedBarrierVisualizer.INSTANCE.renderQueue.add(task);
    }

    public ChunkInstancedBarrierVisualizer(BlockPos center, Vec3d VisualizeCenter, int radius) {
        this.center = center;
        this.lastCenter = this.center.mutableCopy();
        this.radius = radius;
        this.chunks = new ArrayList<>();
        this.visualizeCenter = VisualizeCenter;
        this.visualizeRadius = 1.0F;
        this.alphaThreshold = 0.0F;
    }

    public ChunkInstancedBarrierVisualizer(int radius) {
        this(new BlockPos(0, 0, 0), new Vec3d(0, 0, 0), radius);
    }

    public void close() {
        GlStateManager.deleteBuffers(this.vboModel);
        this.chunks.forEach(Chunk::close);
    }

    public void forceReallocate() {
        int i = 0;
        for (int x = -this.radius; x <= this.radius; x++) {
            for (int y = -this.radius; y <= this.radius; y++) {
                for (int z = -this.radius; z <= this.radius; z++) {
                    this.chunks.get(i++).setOffset(x, y, z);
                }
            }
        }
        this.shouldRebuild = true;
    }

    public float getAlphaThreshold() {
        return this.alphaThreshold;
    }

    public void setAlphaThreshold(float alphaThreshold) {
        this.alphaThreshold = alphaThreshold;
    }

    public BlockPos getCenter() {
        return this.center;
    }

    public void setCenter(BlockPos center) {
        if (!this.center.equals(center)) {
            this.center = center;
        }
    }

    public int getLastDiameter() {
        return this.lastDiameter;
    }

    public int getLastMoveX() {
        return this.lastMoveX;
    }

    public int getLastMoveY() {
        return this.lastMoveY;
    }

    public int getLastMoveZ() {
        return this.lastMoveZ;
    }

    public int getModelVertices() {
        return this.modelVertices;
    }

    public int getRadius() {
        return this.radius;
    }

    public void setRadius(int radius) {
        recordRenderCall(() -> {
            info("Set radius head");
            KZEAddon.glError("setRadius head");
            this.radius = radius;
            this.chunks.forEach(Chunk::close);
            this.chunks.clear();
            int id = 0;
            for (int x = -radius; x <= radius; x++) {
                for (int y = -radius; y <= radius; y++) {
                    for (int z = -radius; z <= radius; z++) {
                        Chunk chunk = new Chunk(new BlockPos(x, y, z), id++);
                        chunk.initialize();
                        this.chunks.add(chunk);
                    }
                }
            }
            this.chunks.sort(Comparator.comparingDouble(o -> o.getOffset().getSquaredDistance(new Vec3i(0, 0, 0))));
            this.shouldRebuild = true;
            KZEAddon.glError("setRadius tail");
            info("Set radius tail");
        });
    }

    public float getVisualizeRadius() {
        return this.visualizeRadius;
    }

    public void setVisualizeRadius(float visualizeRadius) {
        this.visualizeRadius = visualizeRadius;
    }

    public void initialize() {
        KZEAddon.glError("initialize body head");
        this.vboModel = GlStateManager.genBuffers();
        KZEAddon.glError("vboModel initialize");
        this.bufferModel = new ByteBufferWrapper(1024 * 1024 * 2, () -> recordRenderCall(() -> {
            KZEAddon.glError("initialize head");
            this.bufferModel.updateSizeOnVBO(this.vboModel);
            KZEAddon.glError("initialize tail");
        }));
        KZEAddon.glError("bufferModel initialize");
        this.bufferModel.updateSizeOnVBO(this.vboModel);
        KZEAddon.glError("model vbo update size");
        this.useDefaultModel();
        KZEAddon.glError("use the default model");
        this.setRadius(this.radius);
        KZEAddon.glError("setting up radius");
        KZEAddon.glError("initialize body tail");
    }

    public boolean isLastShouldRebuild() {
        return this.lastShouldRebuild;
    }

    public boolean isShouldRebuild() {
        return this.shouldRebuild;
    }

    public void setShouldRebuild(boolean shouldRebuild) {
        this.shouldRebuild = shouldRebuild;
    }

    private void pollRenderQueue() {
        this.lastPollQueueTime = System.currentTimeMillis();
        KZEAddon.glError("pollRenderQueue Head");
        Runnable task;
        while ((task = this.renderQueue.poll()) != null) {
            task.run();
        }
        KZEAddon.glError("pollRenderQueue Tail");
    }

    public void render(float delta) {
        this.pollRenderQueue();

        BarrierShader shader = BarrierShader.INSTANCE;
        shader.setCenter(this.visualizeCenter);
        shader.setRadius(((this.radius * 16) + 8) * this.visualizeRadius);
        shader.setMVP(KZEAddon.getVPMatrix(delta));

        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.enableAlphaTest();
        RenderSystem.alphaFunc(GL_GREATER, this.alphaThreshold);

        shader.bind();
        this.chunks.forEach(Chunk::render);
        shader.unbind();

        RenderSystem.disableAlphaTest();
        RenderSystem.defaultAlphaFunc();
    }

    // TODO 描画の基礎からやり直し
    public void renderChunkOffsets(MatrixStack matrices, Function<Chunk, String> parser) {
        MinecraftClient mc = MinecraftClient.getInstance();
        Camera camera = mc.gameRenderer.getCamera();
        Vec3d camPos = camera.getPos();
        TextRenderer textRenderer = mc.textRenderer;
        Quaternion yQ = Vec3f.NEGATIVE_Y.getDegreesQuaternion(camera.getYaw() + 180);
        Quaternion xQ = Vec3f.NEGATIVE_X.getDegreesQuaternion(camera.getPitch() + 180);
        int height = textRenderer.fontHeight;

        for (Chunk chunk : this.chunks) {
            BlockPos offset = this.center.add(chunk.getOffset());
            String text = parser.apply(chunk);
            int width = textRenderer.getWidth(text);
            matrices.push();
            matrices.translate(offset.getX() * 16 + 8 - camPos.getX(), offset.getY() * 16 + 8 - camPos.getY(), offset.getZ() * 16 + 8 - camPos.getZ());
            matrices.multiply(yQ);
            matrices.multiply(xQ);
            matrices.scale(0.1F, 0.1F, 0.1F);
            textRenderer.drawWithShadow(matrices, text, -(width / 2F), -(height / 2F), 0xFFFFFFFF);
            matrices.pop();
        }
    }

    public void setCenter(int x, int y, int z) {
        this.setCenter(new BlockPos(x, y, z));
    }

    public void setDebugOverlay() {
        MinecraftClient mc = MinecraftClient.getInstance();
        mc.inGameHud.setOverlayMessage(Text.of(
                this.chunks.size() + " Chunk(s), Radius: " + this.radius + "(" + (this.radius * 2 + 1) + "^3)"
        ), false);
    }

    public void setModel(float[] vertices) {
        this.bufferModel.clear();
        this.bufferModel.grow(vertices.length * 4);
        for (float tmp : vertices) {
            this.bufferModel.put(tmp);
        }
        this.bufferModel.flip();
        recordRenderCall(() -> {
            KZEAddon.glError("setModel head");
            info("Model VBO : " + this.vboModel);
            this.bufferModel.updateDataOnVBO(this.vboModel);
            KZEAddon.glError("setModel tail");
        });
        this.modelVertices = vertices.length / 3;
    }

    public void setVisualizeCenter(Vec3d center) {
        this.visualizeCenter = center;
    }

    public void setVisualizeCenter(double x, double y, double z) {
        this.setVisualizeCenter(new Vec3d(x, y, z));
    }

    private boolean testRebuildFlag(BlockPos moveAmount, int diameter) {
        if (this.shouldRebuild) {
            info("Force rebuild flag");
            return true;
        } else if (Math.abs(moveAmount.getX()) >= diameter) {
            info("X axis amount is over! (" + Math.abs(moveAmount.getX()) + " >= " + diameter + ")");
            return true;
        } else if (Math.abs(moveAmount.getY()) >= diameter) {
            info("Y axis amount is over! (" + Math.abs(moveAmount.getY()) + " >= " + diameter + ")");
            return true;
        } else if (Math.abs(moveAmount.getZ()) >= diameter) {
            info("Z axis amount is over! (" + Math.abs(moveAmount.getZ()) + " >= " + diameter + ")");
            return true;
        }

        return false;
    }

    public void tick() {
        if (DebugCommand.cibvDebug) this.setDebugOverlay();
        BlockPos moveAmount = this.center.subtract(this.lastCenter);
        this.lastCenter.set(this.center);
        int diameter = this.radius * 2 + 1;
        boolean forceRebuild = this.testRebuildFlag(moveAmount, diameter);
        this.shouldRebuild = false;
        this.lastDiameter = diameter;
        this.lastMoveX = Math.abs(moveAmount.getX());
        this.lastMoveY = Math.abs(moveAmount.getY());
        this.lastMoveZ = Math.abs(moveAmount.getZ());
        this.lastShouldRebuild = forceRebuild;
        if (moveAmount.getX() != 0 || moveAmount.getY() != 0 || moveAmount.getZ() != 0)
            KZEAddon.info("Chunk move : " + moveAmount.toShortString() + " Center(" + this.center.toShortString() + ")");
        this.chunks.forEach(it -> it.tick(moveAmount, forceRebuild));
        if (DebugCommand.printMoveAmountNumber > 0) DebugCommand.printMoveAmountNumber--;
    }

    public void useDefaultModel() {
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

    public static class Chunk {
        private final int id;
        private BlockPos offset;
        private int vboOffset;
        private ByteBufferWrapper bufferOffset;
        private int vao;
        private int barrierCount;
        private ChunkUpdateState state;

        public static int wrap(int src, int range) {
            if (range == 0) return src;
            int diameter = range * 2 + 1;
            if (src >= diameter || src <= -diameter) {
                src %= diameter;
            }

            if (src >= range + 1) {
                return -range + (src - range - 1);
            } else if (src <= -(range + 1)) {
                return range + (src + range + 1);
            } else {
                return src;
            }
        }

        public static BlockPos wrapOffset(BlockPos offset, int radius) {
            return new BlockPos(
                    wrap(offset.getX(), radius),
                    wrap(offset.getY(), radius),
                    wrap(offset.getZ(), radius)
            );
        }

        public Chunk(BlockPos offset, int id) {
            this.offset = offset;
            this.id = id;
            this.state = ChunkUpdateState.NEUTRAL;
        }

        public Iterator<BlockPos> chunkIterator() {
            BlockPos center = ChunkInstancedBarrierVisualizer.INSTANCE.center;
            return BlockPos.iterate(
                    new BlockPos(
                            (center.getX() + this.offset.getX()) * 16,
                            (center.getY() + this.offset.getY()) * 16,
                            (center.getZ() + this.offset.getZ()) * 16
                    ),
                    new BlockPos(
                            (center.getX() + this.offset.getX()) * 16 + 15,
                            (center.getY() + this.offset.getY()) * 16 + 15,
                            (center.getZ() + this.offset.getZ()) * 16 + 15
                    )
            ).iterator();
        }

        public void close() {
            glDeleteVertexArrays(this.vao);
            GlStateManager.deleteBuffers(this.vboOffset);
        }

        public int getId() {
            return this.id;
        }

        public BlockPos getOffset() {
            return this.offset;
        }

        public void setOffset(BlockPos pos) {
            this.offset = pos;
        }

        public void initialize() {
            this.vao = glGenVertexArrays();
            this.vboOffset = GlStateManager.genBuffers();
            this.bufferOffset = new ByteBufferWrapper(1024 * 1024 * 2, () -> recordRenderCall(() -> {
                KZEAddon.glError("Chunk init head");
                this.bufferOffset.updateSizeOnVBO(this.vboOffset);
                KZEAddon.glError("Chunk init tail");
            }));

            glBindVertexArray(this.vao);
            glEnableVertexAttribArray(0);
            glEnableVertexAttribArray(1);
            glBindBuffer(GL_ARRAY_BUFFER, ChunkInstancedBarrierVisualizer.INSTANCE.vboModel);
            glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
            glBindBuffer(GL_ARRAY_BUFFER, this.vboOffset);
            glBufferData(GL_ARRAY_BUFFER, this.bufferOffset.capacity(), GL_DYNAMIC_DRAW);
            glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);
            glBindBuffer(GL_ARRAY_BUFFER, 0);

            glVertexAttribDivisor(0, 0);
            glVertexAttribDivisor(1, 1);

            glBindVertexArray(0);
        }

        public ChunkUpdateState getUpdateState() {
            return this.state;
        }

        public boolean isRebuilding() {
            return this.state == ChunkUpdateState.UPDATE_CPU || this.state == ChunkUpdateState.WAIT_UPLOAD;
        }

        public void move(int x, int y, int z) {
            this.offset = new BlockPos(x, y, z);
        }

        public void move(BlockPos offset) {
            this.offset = offset;
        }

        public BlockPos originInWorldPos() {
            BlockPos center = ChunkInstancedBarrierVisualizer.INSTANCE.center;
            return new BlockPos(
                    (center.getX() + this.offset.getX()) * 16,
                    (center.getY() + this.offset.getY()) * 16,
                    (center.getZ() + this.offset.getZ()) * 16
            );
        }

        public void rebuild(boolean ignoreFlag) {
            ClientWorld world = MinecraftClient.getInstance().world;
            if(world != null && (!this.isRebuilding() || ignoreFlag)) {
                this.state = ChunkUpdateState.UPDATE_CPU;

                Iterator<BlockPos> itr = this.chunkIterator();

                this.bufferOffset.clear();
                int barrierCount = 0;

                while (itr.hasNext()) {
                    BlockPos pos = itr.next();
                    BlockState state = world.getBlockState(pos);

                    if (state.getBlock().equals(Blocks.BARRIER)) {
                        this.bufferOffset.grow(12);
                        this.bufferOffset.put(pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F);
                        barrierCount++;
                    }
                }

                this.barrierCount = barrierCount;
                this.bufferOffset.flip();
                this.state = ChunkUpdateState.WAIT_UPLOAD;
                // recordRenderCall(() -> {
                //     KZEAddon.glError("Chunk rebuild head");
                //     this.bufferOffset.updateDataOnVBO(this.vboOffset);
                //     KZEAddon.glError("Chunk rebuild tail");
                //     this.state = ChunkUpdateState.NEUTRAL;
                // });
                RenderSystem.recordRenderCall(() -> {
                    KZEAddon.glError("Chunk rebuild head");
                    this.bufferOffset.updateDataOnVBO(this.vboOffset);
                    KZEAddon.glError("Chunk rebuild tail");
                    this.state = ChunkUpdateState.NEUTRAL;
                });
            }
        }

        public void render() {
            glBindVertexArray(this.vao);
            glDrawArraysInstanced(GL_LINES, 0, ChunkInstancedBarrierVisualizer.INSTANCE.getModelVertices(), this.barrierCount);
            glBindVertexArray(0);
        }

        public void setOffset(int x, int y, int z) {
            this.setOffset(new BlockPos(x, y, z));
        }

        public void tick(BlockPos moveAmount, boolean forceRebuild) {
            if (!forceRebuild) {
                BlockPos origin = this.offset.subtract(moveAmount);
                BlockPos wrapped = wrapOffset(origin, ChunkInstancedBarrierVisualizer.INSTANCE.radius);
                if (DebugCommand.printMoveAmountNumber > 0) KZEAddon.info("Wrapped > " + wrapped.toShortString());
                if (!this.offset.equals(wrapped)) {
                    this.offset = wrapped;
                    if(!origin.equals(wrapped)) {
                        CompletableFuture.runAsync(() -> this.rebuild(false));
                    }
                }
            } else {
                CompletableFuture.runAsync(() -> this.rebuild(true));
            }
        }
    }

    public enum ChunkUpdateState {
        NEUTRAL(0), UPDATE_CPU(1), WAIT_UPLOAD(2);

        private final int id;

        ChunkUpdateState(int id) {
            this.id = id;
        }

        public int getId() {
            return this.id;
        }
    }
}
