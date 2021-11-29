package com.theboss.kzeaddonfabric.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.theboss.kzeaddonfabric.KZEAddon;
import com.theboss.kzeaddonfabric.utils.RenderingUtils;
import com.theboss.kzeaddonfabric.utils.VanillaUtils;
import com.theboss.kzeaddonfabric.render.shader.BarrierShader;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.*;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import static com.theboss.kzeaddonfabric.KZEAddon.info;
import static org.lwjgl.opengl.GL33.*;

public class ChunkInstancedBarrierVisualizer {
    public static final ChunkInstancedBarrierVisualizer INSTANCE = new ChunkInstancedBarrierVisualizer(1);
    private final Queue<Runnable> renderQueue = new ArrayDeque<>();
    private final BlockPos.Mutable lastCenter;
    private final List<Chunk> chunks;
    private final Queue<Chunk> needUpdateChunks;
    private BlockPos center;
    private int radius;
    private boolean shouldRebuild;
    private ByteBufferWrapper bufferModel;
    private int vboModel;
    private int modelVertices;
    private Vec3d visualizeCenter;
    private long lastPollQueueTime;

    private int lastDiameter;
    private int lastMoveX;
    private int lastMoveY;
    private int lastMoveZ;
    private boolean lastShouldRebuild;

    public final OriginalFramebuffer framebuffer;

    public long getLastPollQueueTime() {
        return this.lastPollQueueTime;
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
        if (radius < 0) radius = 0;
        this.radius = radius;
        this.chunks = new ArrayList<>();
        this.needUpdateChunks = new ArrayDeque<>();
        this.visualizeCenter = VisualizeCenter;
        this.framebuffer = new OriginalFramebuffer("CIBV");
    }

    public ChunkInstancedBarrierVisualizer(int radius) {
        this(new BlockPos(0, 0, 0), new Vec3d(0, 0, 0), radius);
    }

    public void close() {
        GlStateManager.deleteBuffers(this.vboModel);
        this.framebuffer.close();
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
            RenderingUtils.glError("setRadius head");
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
            RenderingUtils.glError("setRadius tail");
            info("Set radius tail");
        });
    }

    public void initialize() {
        this.vboModel = GlStateManager.genBuffers();
        this.bufferModel = new ByteBufferWrapper(1024 * 1024 * 2, () -> recordRenderCall(() -> {
            this.bufferModel.updateSizeOnVBO(this.vboModel);
        }));
        this.bufferModel.updateSizeOnVBO(this.vboModel);
        this.useDefaultModel();
        this.setRadius(this.radius);
        this.framebuffer.initialize(MinecraftClient.getInstance().getWindow());
        RenderingUtils.glError("Initialize CIBV");
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
        RenderingUtils.glError("pollRenderQueue Head");
        Runnable task;
        while ((task = this.renderQueue.poll()) != null) {
            task.run();
        }
        RenderingUtils.glError("pollRenderQueue Tail");
    }

    public static void render(boolean useFBO, float delta) {
        INSTANCE.doRender(useFBO, delta);
    }

    public void doRender(boolean useFBO, float delta) {
        if (useFBO) {
            RenderSystem.blendFunc(GL_ONE, GL_ZERO);

            RenderSystem.depthMask(false);
            this.framebuffer.loadDepth(MinecraftClient.getInstance().getFramebuffer());
            this.framebuffer.begin();
            RenderSystem.depthMask(true);
        } else {
            RenderSystem.defaultBlendFunc();
        }

        this.pollRenderQueue();
        this.pollUpdateQueue();

        if (!KZEAddon.options.shouldBarrierVisualize) return;

        BarrierShader shader = BarrierShader.INSTANCE;
        shader.setCenter(this.visualizeCenter);
        shader.setRadius(KZEAddon.options.barrierVisualizeShowRadius);
        shader.setUseFade(KZEAddon.options.shouldUseFade);
        shader.setColor(KZEAddon.options.barrierColor);

        Vec3d cam = MinecraftClient.getInstance().gameRenderer.getCamera().getPos();
        MatrixStack mvp = new MatrixStack();
        Matrix4f vp = mvp.peek().getModel();
        vp.loadIdentity();
        vp.multiply(VanillaUtils.getVPMatrix(delta));
        mvp.translate(-cam.x, -cam.y, -cam.z);

        shader.setMVP(vp);

        RenderSystem.lineWidth(KZEAddon.options.barrierLineWidth);
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();

        shader.bind();
        this.chunks.forEach(Chunk::render);
        shader.unbind();

        RenderSystem.disableDepthTest();
        RenderSystem.disableBlend();
        RenderSystem.lineWidth(1.0F);

        if (useFBO) {
            this.framebuffer.end();
            RenderSystem.defaultBlendFunc();
            this.framebuffer.blit(MinecraftClient.getInstance().getFramebuffer());
        }


        RenderingUtils.glError("render");
    }

    private void pollUpdateQueue() {
        Chunk chunk;
        while (!this.needUpdateChunks.isEmpty()) {
            chunk = this.needUpdateChunks.poll();
            Chunk finalChunk = chunk;
            CompletableFuture.runAsync(() -> finalChunk.rebuild(false));
        }
    }

    @Nullable
    public Chunk getChunk(int x, int y, int z) {
        Iterator<Chunk> chunks = chunkIterator();
        Chunk chunk;
        while (chunks.hasNext()) {
            chunk = chunks.next();
            if (chunk.offset.getX() == x && chunk.offset.getY() == y && chunk.offset.getZ() == z) return chunk;
        }

        return null;
    }

    public boolean isInRange(BlockPos offset) {
        int diameter = this.radius * 2 + 1;
        return offset.getX() <= diameter && offset.getY() <= diameter && offset.getZ() <= diameter;
    }

    public void uploadAllChunks() {
        this.chunks.forEach(Chunk::upload);
    }

    /**
     * Update a chunk contains specified block position.
     * @param pos The position that the chunk to be updated contains
     */
    public void update(BlockPos pos) {
        BlockPos offset = VanillaUtils.toChunk(pos).subtract(this.center);
        if (!this.isInRange(offset)) return;

        Chunk chunk = this.getChunk(offset.getX(), offset.getY(), offset.getZ());

        if (chunk != null && !this.needUpdateChunks.contains(chunk)) {
            this.needUpdateChunks.add(chunk);
        }
    }

    /**
     * Use instead {@link #update(BlockPos)}
     *
     * @param pos The position that the chunk to be updated contains
     */
    @Deprecated
    public void onBlockUpdate(BlockPos pos) {
        BlockPos offset = new BlockPos(pos.getX() / 16F - this.center.getX(), pos.getY() / 16F - this.center.getY(), pos.getZ() / 16F - this.center.getZ());
        int diameter = this.radius * 2 + 1;
        if (offset.getX() > diameter || offset.getY() > diameter || offset.getZ() > diameter) return;
        Iterator<Chunk> itr = chunkIterator();
        while (itr.hasNext()) {
            Chunk chunk = itr.next();
            if (chunk.getOffset().equals(offset) && chunk.getState() == ChunkUpdateState.NEUTRAL) {
                KZEAddon.info("CIBV > " + offset.toShortString() + "(" + pos.toShortString() + ") is updated");
                chunk.rebuild(false);
                return;
            }
        }
    }

    public static void renderChunkStates(MatrixStack matrices, Function<Chunk, String> parser) {
        INSTANCE.doRenderChunkStates(matrices, parser);
    }

    public void doRenderChunkStates(MatrixStack matrices, Function<Chunk, String> parser) {
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

    public void setModel(float[] vertices) {
        this.bufferModel.clear();
        this.bufferModel.grow(vertices.length * 4);
        for (float tmp : vertices) {
            this.bufferModel.put(tmp);
        }
        this.bufferModel.flip();
        recordRenderCall(() -> {
            RenderingUtils.glError("setModel head");
            info("Model VBO : " + this.vboModel);
            this.bufferModel.updateDataOnVBO(this.vboModel);
            RenderingUtils.glError("setModel tail");
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
        if (!KZEAddon.options.shouldBarrierVisualize) return;

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
        this.chunks.forEach(it -> it.tick(moveAmount, forceRebuild));
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
        private int lastLimit;

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

        public boolean isLoaded() {
            MinecraftClient mc = MinecraftClient.getInstance();
            return mc.world != null && mc.player != null && mc.world.isChunkLoaded(mc.player.chunkX + this.offset.getX(), mc.player.chunkZ + this.offset.getZ());
        }

        public int getLastLimit() {
            return this.lastLimit;
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
                RenderingUtils.glError("Chunk init head");
                this.bufferOffset.updateSizeOnVBO(this.vboOffset);
                RenderingUtils.glError("Chunk init tail");
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

        public ChunkUpdateState getState() {
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

        private void upload() {
            RenderSystem.recordRenderCall(() -> {
                if(this.state != ChunkUpdateState.UPDATE_CPU) {
                    this.bufferOffset.updateDataOnVBO(this.vboOffset);
                    KZEAddon.getModLog().info(this.getId() + " | Uploaded");
                }
            });
        }

        public void rebuild(boolean forceRebuild) {
            ClientWorld world = MinecraftClient.getInstance().world;
            if(!this.isLoaded()) {
                this.state = ChunkUpdateState.UPDATE_CPU;
                recordRenderCall(() -> this.rebuild(true));
                return;
            }
            if (world != null && (!this.isRebuilding() || forceRebuild)) {
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

                this.bufferOffset.flip();
                this.state = ChunkUpdateState.WAIT_UPLOAD;
                // recordRenderCall(() -> {
                //     KZEAddon.glError("Chunk rebuild head");
                //     this.bufferOffset.updateDataOnVBO(this.vboOffset);
                //     KZEAddon.glError("Chunk rebuild tail");
                //     this.state = ChunkUpdateState.NEUTRAL;
                // });
                int finalBarrierCount = barrierCount;
                RenderSystem.recordRenderCall(() -> {
                    RenderingUtils.glError("Chunk rebuild head");
                    this.barrierCount = finalBarrierCount;
                    this.lastLimit = this.bufferOffset.limit();
                    this.bufferOffset.updateDataOnVBO(this.vboOffset);
                    RenderingUtils.glError("Chunk rebuild tail");
                    this.state = ChunkUpdateState.NEUTRAL;
                });
            }
        }

        public void render() {
            RenderingUtils.glError("CIBV render head");

            glBindVertexArray(this.vao);
            glDrawArraysInstanced(GL_LINES, 0, ChunkInstancedBarrierVisualizer.INSTANCE.getModelVertices(), this.barrierCount);
            glBindVertexArray(0);

            RenderingUtils.glError("CIBV render tail");
        }

        public void setOffset(int x, int y, int z) {
            this.setOffset(new BlockPos(x, y, z));
        }

        public void tick(BlockPos moveAmount, boolean forceRebuild) {
            if (!forceRebuild) {
                BlockPos origin = this.offset.subtract(moveAmount);
                BlockPos wrapped = wrapOffset(origin, ChunkInstancedBarrierVisualizer.INSTANCE.radius);
                if (!this.offset.equals(wrapped)) {
                    this.offset = wrapped;
                    if (!origin.equals(wrapped)) {
                        CompletableFuture.runAsync(() -> this.rebuild(false));
                    }
                }
            } else {
                CompletableFuture.runAsync(() -> this.rebuild(true));
            }
        }
    }

    public enum ChunkUpdateState {
        NEUTRAL(0, 'a'), UPDATE_CPU(1, 'c'), WAIT_UPLOAD(2, 'e');

        private final int id;
        private final char colorCode;

        ChunkUpdateState(int id, char colorCode) {
            this.id = id;
            this.colorCode = colorCode;
        }

        public int getId() {
            return this.id;
        }

        public char getColorCode() {
            return this.colorCode;
        }
    }
}
