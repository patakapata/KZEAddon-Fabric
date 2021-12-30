package com.theboss.kzeaddonfabric.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.theboss.kzeaddonfabric.KZEAddon;
import com.theboss.kzeaddonfabric.render.shader.OldBarrierShader;
import com.theboss.kzeaddonfabric.utils.RenderingUtils;
import com.theboss.kzeaddonfabric.utils.VanillaUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import static com.theboss.kzeaddonfabric.KZEAddon.info;
import static org.lwjgl.opengl.GL33.*;

public class ChunkInstancedBarrierVisualizer {
    public static final ChunkInstancedBarrierVisualizer INSTANCE = new ChunkInstancedBarrierVisualizer(1);
    public final OriginalFramebuffer framebuffer;
    private final OldBarrierShader SHADER = OldBarrierShader.INSTANCE;
    private final Queue<Runnable> renderQueue = new ArrayDeque<>();
    private final BlockPos.Mutable lastCenter;
    private final List<Chunk> chunks;
    private final Queue<Chunk> needUpdateChunks;
    private final List<Long> elapseTimes = new ArrayList<>();
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
    private int primitiveType;
    private boolean isUseFbo;

    public static Iterator<Chunk> chunkIterator() {
        return INSTANCE.chunks.iterator();
    }

    private static int getPrimitiveTypeIdByName(String name) throws IllegalArgumentException {
        switch (name) {
            case "POINTS":
                return GL11.GL_POINTS;
            case "LINES":
                return GL11.GL_LINES;
            case "TRIANGLES":
                return GL11.GL_TRIANGLES;
            case "QUADS":
                return GL11.GL_QUADS;
            default:
                throw new IllegalArgumentException("Invalid name " + name);
        }
    }

    public static long getRenderTimeAvg() {
        long v = 0;
        for (long t : INSTANCE.elapseTimes) v += t;

        return v / INSTANCE.elapseTimes.size();
    }

    public static void recordRenderCall(Runnable task) {
        ChunkInstancedBarrierVisualizer.INSTANCE.renderQueue.add(task);
    }

    public static void render(float delta) {
        INSTANCE.doRender(delta);
    }

    public static void renderChunkStates(MatrixStack matrices, Function<Chunk, String> parser) {
        INSTANCE.doRenderChunkStates(matrices, parser);
    }

    public ChunkInstancedBarrierVisualizer(int radius) {
        this(new BlockPos(0, 0, 0), new Vec3d(0, 0, 0), radius);
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

    public void close() {
        GlStateManager.deleteBuffers(this.vboModel);
        this.framebuffer.close();
        this.chunks.forEach(Chunk::close);
    }

    public void doRender(float delta) {
        long startAt = System.nanoTime();

        if (this.isUseFbo) {
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

        if (!KZEAddon.options.isVisualizeBarriers) return;

        // BarrierShader shader = BarrierShader.INSTANCE;
        // shader.setCenter(this.visualizeCenter);
        // shader.setFadeRadius(KZEAddon.options.barrierFadeRadius);
        // shader.setUseFade(KZEAddon.options.isBarrierFade);
        // shader.setColor(KZEAddon.options.barrierColor);
        this.SHADER.setCenter(this.visualizeCenter);
        this.SHADER.setFadeRadius(KZEAddon.options.barrierFadeRadius);
        this.SHADER.setUseFade(KZEAddon.options.isBarrierFade);
        this.SHADER.setColor(KZEAddon.options.barrierColor.get());

        Vec3d cam = MinecraftClient.getInstance().gameRenderer.getCamera().getPos();
        MatrixStack mvp = new MatrixStack();
        Matrix4f vp = mvp.peek().getModel();
        vp.loadIdentity();
        vp.multiply(RenderingUtils.getVPMatrix(delta));
        mvp.translate(-cam.x, -cam.y, -cam.z);

        // shader.setMVP(vp);
        this.SHADER.setMVP(vp);

        RenderSystem.lineWidth(KZEAddon.options.barrierLineWidth);
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();

        // shader.bind();
        this.SHADER.bind();
        this.chunks.forEach(Chunk::render);
        this.SHADER.unbind();
        // shader.unbind();

        RenderSystem.disableDepthTest();
        RenderSystem.disableBlend();
        RenderSystem.lineWidth(1.0F);

        if (this.isUseFbo) {
            this.framebuffer.end();
            RenderSystem.defaultBlendFunc();
            this.framebuffer.blit(MinecraftClient.getInstance().getFramebuffer());
        }

        this.elapseTimes.add(-startAt + System.nanoTime());
        if (this.elapseTimes.size() > 20) this.elapseTimes.remove(0);

        RenderingUtils.glError("render");
    }

    public void doRenderChunkStates(MatrixStack matrices, Function<Chunk, String> parser) {
        MinecraftClient mc = MinecraftClient.getInstance();
        Camera camera = mc.gameRenderer.getCamera();
        Vec3d camPos = camera.getPos();
        TextRenderer textRenderer = mc.textRenderer;
        int height = textRenderer.fontHeight;

        for (Chunk chunk : this.chunks) {
            BlockPos offset = this.center.add(chunk.getOffset());
            String text = parser.apply(chunk);
            int width = textRenderer.getWidth(text);
            matrices.push();
            matrices.translate(offset.getX() * 16 + 8 - camPos.getX(), offset.getY() * 16 + 8 - camPos.getY(), offset.getZ() * 16 + 8 - camPos.getZ());
            matrices.multiply(camera.getRotation());
            matrices.scale(0.1F, 0.1F, 0.1F);
            textRenderer.drawWithShadow(matrices, text, -(width / 2F), -(height / 2F), 0xFFFFFFFF);
            matrices.pop();
        }
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

    public long getLastPollQueueTime() {
        return this.lastPollQueueTime;
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
        this.bufferModel = new ByteBufferWrapper(1024 * 1024 * 2, () -> recordRenderCall(() -> this.bufferModel.updateSizeOnVBO(this.vboModel)));
        this.bufferModel.updateSizeOnVBO(this.vboModel);
        this.useDefaultModel();
        this.setRadius(this.radius);
        this.framebuffer.initialize(MinecraftClient.getInstance().getWindow());
        RenderingUtils.glError("Initialize CIBV");
    }

    public boolean isInRange(BlockPos offset) {
        int diameter = this.radius * 2 + 1;
        return offset.getX() <= diameter && offset.getY() <= diameter && offset.getZ() <= diameter;
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

    public boolean isUseFbo() {
        return this.isUseFbo;
    }

    public boolean loadModelFromResource(Identifier id) {
        MinecraftClient mc = MinecraftClient.getInstance();
        try {
            Resource resource = mc.getResourceManager().getResource(id);
            if (resource != null) {
                try (InputStreamReader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
                    StringBuilder builder = new StringBuilder();
                    int c;
                    while ((c = reader.read()) != -1) {
                        builder.append((char) c);
                    }
                    String[] lines = builder.toString().split("\n");
                    int primType = getPrimitiveTypeIdByName(lines[0]);
                    String line;
                    this.bufferModel.clear();
                    for (int i = 1; i < lines.length; i++) {
                        line = lines[i];
                        if (line.startsWith("//")) continue;
                        String[] divided = line.split(",");
                        this.bufferModel.put(Double.parseDouble(divided[0]),
                                             Double.parseDouble(divided[1]),
                                             Double.parseDouble(divided[2]));
                    }
                    this.bufferModel.flip();
                    this.primitiveType = primType;
                    this.bufferModel.uploadToVBO(this.vboModel);
                    return true;
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        this.useDefaultModel();
        return false;
    }

    /**
     * Use instead {@link #update(BlockPos)}
     *
     * @param pos The position that the chunk to be updated contains
     * @see #update(BlockPos)
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

    private void pollRenderQueue() {
        this.lastPollQueueTime = System.currentTimeMillis();
        RenderingUtils.glError("pollRenderQueue Head");
        Runnable task;
        while ((task = this.renderQueue.poll()) != null) {
            task.run();
        }
        RenderingUtils.glError("pollRenderQueue Tail");
    }

    private void pollUpdateQueue() {
        Chunk chunk;
        while (!this.needUpdateChunks.isEmpty()) {
            chunk = this.needUpdateChunks.poll();
            Chunk finalChunk = chunk;
            CompletableFuture.runAsync(() -> finalChunk.rebuild(false));
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

    /**
     * モデルのプリミティブを変更する
     *
     * @param type {@link GL11#GL_POINTS POINTS}, {@link GL11#GL_LINES LINES}, {@link GL11#GL_TRIANGLES TRIANGLES}, {@link GL11#GL_QUADS QUADS} の内どれか一つ
     */
    public void setPrimitiveType(int type) {
        this.primitiveType = type;
    }

    public void setUseFBO(boolean isUseFbo) {
        this.isUseFbo = isUseFbo;
    }

    public void setVisualizeCenter(double x, double y, double z) {
        this.setVisualizeCenter(new Vec3d(x, y, z));
    }

    public void setVisualizeCenter(Vec3d center) {
        this.visualizeCenter = center;
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
        if (!KZEAddon.options.isVisualizeBarriers) return;

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

    /**
     * Update a chunk contains specified block position.
     *
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

    public void uploadAllChunks() {
        this.chunks.forEach(Chunk::upload);
    }

    public void useCrystalModel() {
        this.primitiveType = GL11.GL_TRIANGLES;
        float minOuter = -0.4F;
        float minInner = -0.3F;
        float maxInner = -minInner;
        float maxOuter = -minOuter;

        this.setModel(new float[]{
                // Down1
                minOuter, minOuter, minOuter,
                maxOuter, minOuter, maxOuter,
                minOuter, minOuter, maxOuter,
                // Down2
                minOuter, minOuter, minOuter,
                maxOuter, minOuter, minOuter,
                maxOuter, minOuter, maxOuter,
                // Up1
                minOuter, maxOuter, minOuter,
                minOuter, maxOuter, maxOuter,
                maxOuter, maxOuter, maxOuter,
                // Up2
                minOuter, maxOuter, minOuter,
                maxOuter, maxOuter, maxOuter,
                maxOuter, maxOuter, minOuter,
                // North1
                minOuter, minOuter, maxOuter,
                maxOuter, minOuter, maxOuter,
                minOuter, maxOuter, maxOuter,
                // North2
                minOuter, maxOuter, maxOuter,
                maxOuter, minOuter, maxOuter,
                maxOuter, maxOuter, maxOuter,
                // East1
                minOuter, minOuter, minOuter,
                minOuter, minOuter, maxOuter,
                minOuter, maxOuter, maxOuter,
                // East2
                minOuter, minOuter, minOuter,
                minOuter, maxOuter, maxOuter,
                minOuter, maxOuter, minOuter,
                // West1
                maxOuter, minOuter, minOuter,
                maxOuter, maxOuter, minOuter,
                maxOuter, maxOuter, maxOuter,
                // West2
                maxOuter, minOuter, minOuter,
                maxOuter, maxOuter, maxOuter,
                maxOuter, minOuter, maxOuter,
                // South1
                minOuter, minOuter, minOuter,
                minOuter, maxOuter, minOuter,
                maxOuter, maxOuter, minOuter,
                // South2
                minOuter, minOuter, minOuter,
                maxOuter, maxOuter, minOuter,
                maxOuter, minOuter, minOuter,
        });
    }

    public void useDefaultModel() {
        this.primitiveType = GL11.GL_LINES;
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

    public enum ChunkUpdateState {
        NEUTRAL(0, 'a'), UPDATE_CPU(1, 'c'), WAIT_UPLOAD(2, 'e');

        private final int id;
        private final char colorCode;

        ChunkUpdateState(int id, char colorCode) {
            this.id = id;
            this.colorCode = colorCode;
        }

        public char getColorCode() {
            return this.colorCode;
        }

        public int getId() {
            return this.id;
        }
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

        public int getLastLimit() {
            return this.lastLimit;
        }

        public BlockPos getOffset() {
            return this.offset;
        }

        public void setOffset(BlockPos pos) {
            this.offset = pos;
        }

        public ChunkUpdateState getState() {
            return this.state;
        }

        public void initialize() {
            this.vao = glGenVertexArrays();
            this.vboOffset = GlStateManager.genBuffers();
            this.bufferOffset = new ByteBufferWrapper(1024 * 1024 * 2, () -> recordRenderCall(() -> {
                RenderingUtils.glError("Chunk init head");
                this.bufferOffset.updateSizeOnVBO(this.vboOffset);
                RenderingUtils.glError("Chunk init tail");
            }));

            int posIndex = GL20.glGetAttribLocation(ChunkInstancedBarrierVisualizer.INSTANCE.SHADER.getProgram(), "position");
            int offset = GL20.glGetAttribLocation(ChunkInstancedBarrierVisualizer.INSTANCE.SHADER.getProgram(), "offset");

            glBindVertexArray(this.vao);
            glEnableVertexAttribArray(posIndex);
            glEnableVertexAttribArray(offset);
            glBindBuffer(GL_ARRAY_BUFFER, ChunkInstancedBarrierVisualizer.INSTANCE.vboModel);
            glVertexAttribPointer(posIndex, 3, GL_FLOAT, false, 0, 0);
            glBindBuffer(GL_ARRAY_BUFFER, this.vboOffset);
            glBufferData(GL_ARRAY_BUFFER, this.bufferOffset.capacity(), GL_DYNAMIC_DRAW);
            glVertexAttribPointer(offset, 3, GL_FLOAT, false, 0, 0);
            glBindBuffer(GL_ARRAY_BUFFER, 0);

            glVertexAttribDivisor(posIndex, 0);
            glVertexAttribDivisor(offset, 1);

            glBindVertexArray(0);
        }

        public boolean isLoaded() {
            MinecraftClient mc = MinecraftClient.getInstance();
            return mc.world != null && mc.player != null && mc.world.isChunkLoaded(mc.player.chunkX + this.offset.getX(), mc.player.chunkZ + this.offset.getZ());
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

        public void rebuild(boolean forceRebuild) {
            ClientWorld world = MinecraftClient.getInstance().world;
            if (!this.isLoaded()) {
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
            glDrawArraysInstanced(INSTANCE.primitiveType, 0, ChunkInstancedBarrierVisualizer.INSTANCE.getModelVertices(), this.barrierCount);
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

        private void upload() {
            RenderSystem.recordRenderCall(() -> {
                if (this.state != ChunkUpdateState.UPDATE_CPU) {
                    this.bufferOffset.updateDataOnVBO(this.vboOffset);
                    KZEAddon.info(this.getId() + " | Uploaded");
                }
            });
        }
    }
}
