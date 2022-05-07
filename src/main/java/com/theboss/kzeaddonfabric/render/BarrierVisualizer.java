package com.theboss.kzeaddonfabric.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.theboss.kzeaddonfabric.KZEAddon;
import com.theboss.kzeaddonfabric.Options;
import com.theboss.kzeaddonfabric.render.shader.impl.OldBarrierShader;
import com.theboss.kzeaddonfabric.utils.Color;
import com.theboss.kzeaddonfabric.utils.RenderUtils;
import com.theboss.kzeaddonfabric.utils.VanillaUtils;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.resource.ReloadableResourceManager;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SynchronousResourceReloader;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL33;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.theboss.kzeaddonfabric.KZEAddon.*;
import static org.lwjgl.opengl.GL33.*;

public class BarrierVisualizer implements SynchronousResourceReloader {
    private final FramebufferWrapper framebuffer;
    private final OldBarrierShader SHADER = OldBarrierShader.getInstance();
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
    private int primitiveType;
    private boolean isUseFbo;
    private Identifier modelFile;
    private boolean noCenterUpdate;

    private static int getPrimitiveTypeIdByName(String name) throws IllegalArgumentException {
        name = name.endsWith("\r") ? name.substring(0, name.length() - 1) : name;
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

    public static Screen createConfigScreen(BarrierVisualizer instance, @Nullable Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create().setTransparentBackground(true);
        ConfigEntryBuilder eBuilder = builder.entryBuilder();
        Options options = getOptions();
        ConfigCategory category = builder.getOrCreateCategory(new TranslatableText("category.kzeaddon.option.barrier"));

        category.addEntry(eBuilder.startIntSlider(new TranslatableText("menu.kzeaddon.barrier.visualize_radius"), options.barrierVisualizeRadius, 0, 5)
                                  .setSaveConsumer(v -> {
                                      options.barrierVisualizeRadius = v;
                                      instance.setRadius(v);
                                  })
                                  .build());
        category.addEntry(eBuilder.startBooleanToggle(new TranslatableText("menu.kzeaddon.barrier.use_fade"), options.isBarrierFade)
                                  .setSaveConsumer(v -> {
                                      options.isBarrierFade = v;
                                      instance.SHADER.setUseFade(v);
                                  })
                                  .build());
        category.addEntry(eBuilder.startIntSlider(new TranslatableText("menu.kzeaddon.barrier.fade_radius"), ((int) (options.barrierFadeRadius * 10)), 0, 640)
                                  .setTextGetter(v -> Text.of(String.format("%.1f", v / 10F)))
                                  .setSaveConsumer(v -> {
                                      options.barrierFadeRadius = v / 10F;
                                      instance.SHADER.setFadeRadius(v / 10F);
                                  })
                                  .build());
        category.addEntry(eBuilder.startColorField(new TranslatableText("menu.kzeaddon.barrier.color"), options.barrierColor.get())
                                  .setSaveConsumer(v -> options.barrierColor = new Color(v))
                                  .build());
        category.addEntry(eBuilder.startStringDropdownMenu(new TranslatableText("menu.kzeaddon.barrier.model_file"), instance.modelFile == null ? "" : instance.modelFile.toString().substring(30))
                                  .setSelections(() -> MinecraftClient.getInstance().getResourceManager().findResources("barrier_model/", unused -> true).stream()
                                                                      .map(id -> id.getPath().substring(14))
                                                                      .collect(Collectors.toList()).iterator())
                                  .setSaveConsumer(str -> {
                                      Identifier id = new Identifier(MOD_ID, "barrier_model/" + str);
                                      if (MinecraftClient.getInstance().getResourceManager().containsResource(id)) {
                                          RenderSystem.recordRenderCall(() -> instance.loadModelFromResource(id));
                                      }
                                  })
                                  .build());
        category.addEntry(eBuilder.startFloatField(new TranslatableText("menu.kzeaddon.barrier.crosshair_reach_distance"), options.barrierVisualizeRaycastDistance)
                                  .setSaveConsumer(v -> options.barrierVisualizeRaycastDistance = v)
                                  .build());

        builder.setParentScreen(parent);
        return builder.build();
    }

    private static int wrap(int src, int range) {
        if (range == 0) return 0;
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

    private static BlockPos wrapOffset(BlockPos offset, int radius) {
        return new BlockPos(
                wrap(offset.getX(), radius),
                wrap(offset.getY(), radius),
                wrap(offset.getZ(), radius)
        );
    }

    public BarrierVisualizer(int radius) {
        this(new BlockPos(0, 0, 0), new Vec3d(0, 0, 0), radius);
    }

    public BarrierVisualizer(BlockPos center, Vec3d VisualizeCenter, int radius) {
        this.center = center;
        this.modelFile = null;
        this.lastCenter = this.center.mutableCopy();
        if (radius < 0) radius = 0;
        this.radius = radius;
        this.chunks = new ArrayList<>();
        this.needUpdateChunks = new ArrayDeque<>();
        this.visualizeCenter = VisualizeCenter;
        this.framebuffer = new FramebufferWrapper("BarrierVisualizer");
        this.isUseFbo = true;
    }

    public FramebufferWrapper getFramebuffer() {
        return this.framebuffer;
    }

    public void onWindowResized(Window window) {
        this.framebuffer.setSize(window);
    }

    public long getRenderTimeAvg() {
        long v = 0;
        for (long t : this.elapseTimes) v += t;

        return v / this.elapseTimes.size();
    }

    public void recordRenderCall(Runnable task) {
        this.renderQueue.add(task);
    }

    public boolean isUseFbo() {
        return this.isUseFbo;
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
        Iterator<Chunk> chunks = this.chunkIterator();
        Chunk chunk;
        while (chunks.hasNext()) {
            chunk = chunks.next();
            if (chunk.offset.getX() == x && chunk.offset.getY() == y && chunk.offset.getZ() == z) return chunk;
        }

        return null;
    }

    public void forceReallocate() {
        int i = 0;
        this.chunks.sort(Comparator.comparingInt(Chunk::getId));
        int radius = this.radius;
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    this.chunks.get(i++).setOffset(x, y, z);
                }
            }
        }
        this.shouldRebuild = true;
    }

    public int getModelVertices() {
        return this.modelVertices;
    }

    public int getRadius() {
        return this.radius;
    }

    public void setRadius(int radius) {
        if (!RenderSystem.isOnRenderThread()) {
            this.recordRenderCall(() -> this.setRadius(radius));
        }

        this.radius = radius;
        this.chunks.forEach(Chunk::close);
        this.chunks.clear();
        int id = 0;
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Chunk chunk = new Chunk(new BlockPos(x, y, z), id++);
                    chunk.initialize();
                    chunk.rebuild(false);
                    this.chunks.add(chunk);
                }
            }
        }
        this.chunks.sort(Comparator.comparingDouble(o -> o.getOffset().getSquaredDistance(new Vec3i(0, 0, 0))));
        this.shouldRebuild = true;
        RenderUtils.glError("setRadius");
    }

    public Identifier getModelFile() {
        return this.modelFile;
    }

    public void initialize(int radius) {
        this.radius = radius;

        this.vboModel = GlStateManager.genBuffers();
        this.bufferModel = new ByteBufferWrapper(1024 * 1024 * 2, () -> this.recordRenderCall(() -> this.bufferModel.updateSizeOnVBO(this.vboModel)));
        this.bufferModel.updateSizeOnVBO(this.vboModel);
        this.loadModelFromResource(getOptions().barrierModel);
        this.setRadius(this.radius);
        this.framebuffer.initialize(MinecraftClient.getInstance().getWindow());
        ResourceManager resManager = MinecraftClient.getInstance().getResourceManager();
        if (resManager instanceof ReloadableResourceManager) {
            ((ReloadableResourceManager) resManager).registerReloader(this);
        }
        RenderUtils.glError("Initialize CIBV");
    }

    public Iterator<Chunk> chunkIterator() {
        return this.chunks.iterator();
    }

    public void close() {
        GlStateManager.deleteBuffers(this.vboModel);
        this.framebuffer.close();
        this.chunks.forEach(Chunk::close);
    }

    public void render(float delta) {
        long startAt = System.nanoTime();
        Options options = getOptions();
        int prevFBO = 0;

        if (this.isUseFbo) {
            RenderSystem.blendFunc(GL_ONE, GL_ZERO);
            RenderSystem.depthMask(true);
            prevFBO = glGetInteger(GL_DRAW_FRAMEBUFFER_BINDING);
            Framebuffer framebuffer = MinecraftClient.getInstance().getFramebuffer();
            this.framebuffer.begin();
            this.framebuffer.loadDepth(prevFBO, framebuffer.viewportWidth, framebuffer.viewportHeight);
        } else {
            RenderSystem.defaultBlendFunc();
        }

        this.pollRenderQueue();
        this.pollUpdateQueue();

        if (!options.isVisualizeBarriers) return;

        this.updateCenter();
        this.SHADER.setCenter(this.visualizeCenter);
        this.SHADER.setFadeRadius(options.barrierFadeRadius);
        this.SHADER.setUseFade(options.isBarrierFade);
        this.SHADER.setColor(options.barrierColor.get());

        Vec3d cam = MinecraftClient.getInstance().gameRenderer.getCamera().getPos();
        MatrixStack mvp = new MatrixStack();
        Matrix4f vp = mvp.peek().getModel();
        vp.loadIdentity();
        vp.multiply(RenderUtils.getVPMatrix(delta));
        mvp.translate(-cam.x, -cam.y, -cam.z);

        this.SHADER.setMVP(vp);

        RenderSystem.lineWidth(options.barrierLineWidth);
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.enableAlphaTest();
        RenderSystem.alphaFunc(GL_GREATER, 0);

        this.SHADER.bind();
        this.chunks.forEach(c -> c.render(this.primitiveType));
        if (options.isShowBarrierWireframe) {
            this.SHADER.setColorTemporary(0);
            RenderUtils.polygonLineMode();
            RenderSystem.enableLineOffset();
            RenderSystem.polygonOffset(-2, -2);
            RenderSystem.disableCull();
            this.chunks.forEach(c -> c.render(this.primitiveType));
            RenderSystem.enableCull();
            RenderSystem.disableLineOffset();
            RenderUtils.polygonFillMode();
        }
        this.SHADER.unbind();

        RenderSystem.disableDepthTest();
        RenderSystem.disableBlend();
        RenderSystem.lineWidth(1.0F);

        if (this.isUseFbo) {
            RenderSystem.defaultBlendFunc();
            RenderSystem.depthMask(false);

            if (prevFBO != 0) glBindFramebuffer(GL_FRAMEBUFFER, prevFBO);
            this.framebuffer.blit();
        }

        this.elapseTimes.add(-startAt + System.nanoTime());
        if (this.elapseTimes.size() > 20) this.elapseTimes.remove(0);

        RenderUtils.glError("render");
    }

    public void renderChunkState(MatrixStack matrices, Function<Chunk, String> parser) {
        MinecraftClient mc = MinecraftClient.getInstance();
        Camera camera = mc.gameRenderer.getCamera();
        Vec3d cam = camera.getPos();
        TextRenderer textRenderer = mc.textRenderer;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        int height = textRenderer.fontHeight;
        if (mc.player == null) return;

        Box box = new Box(0.1, 0.1, 0.1,
                          15.9, 15.9, 15.9);

        for (Chunk chunk : this.chunks) {
            BlockPos offset = this.lastCenter.add(chunk.getOffset());
            String text = parser.apply(chunk);
            int width = textRenderer.getWidth(text);

            matrices.push();
            matrices.translate(offset.getX() * 16 + 8 - cam.getX(), offset.getY() * 16 + 8 - cam.getY(), offset.getZ() * 16 + 8 - cam.getZ());
            matrices.multiply(camera.getRotation());
            matrices.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(180));
            matrices.scale(0.1F, 0.1F, 0.1F);
            textRenderer.drawWithShadow(matrices, text, -(width / 2F), -(height / 2F), 0xFFFFFFFF);
            matrices.pop();

            matrices.push();
            matrices.translate(offset.getX() * 16 - cam.x, offset.getY() * 16 - cam.y, offset.getZ() * 16 - cam.z);

            buffer.begin(GL_QUADS, VertexFormats.POSITION_TEXTURE);
            RenderUtils.drawTexturedBox(matrices, buffer, box, 0, 0, 1, 1);
            RenderSystem.enableDepthTest();
            RenderSystem.disableCull();
            RenderSystem.enableBlend();
            RenderSystem.enableAlphaTest();
            mc.getTextureManager().bindTexture(new Identifier(MOD_ID, "textures/chunk.png"));

            tessellator.draw();

            RenderSystem.disableAlphaTest();
            RenderSystem.disableBlend();
            RenderSystem.enableCull();
            RenderSystem.disableDepthTest();

            matrices.pop();
        }

        int aX = (this.center.getX() - this.radius) * 16;
        int aY = (this.center.getY() - this.radius) * 16;
        int aZ = (this.center.getZ() - this.radius) * 16;

        int bX = aX + ((this.radius * 2 + 1) * 16);
        int bY = aY + ((this.radius * 2 + 1) * 16);
        int bZ = aZ + ((this.radius * 2 + 1) * 16);

        matrices.push();
        matrices.translate(-cam.x, -cam.y, -cam.z);
        RenderSystem.enableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.enableAlphaTest();
        mc.getTextureManager().bindTexture(new Identifier(MOD_ID, "textures/chunk.png"));
        buffer.begin(GL11.GL_QUADS, VertexFormats.POSITION_TEXTURE);
        RenderUtils.drawTexturedBox(matrices, buffer, new Box(aX, aY, aZ, bX, bY, bZ), 0, 0, this.radius * 2 + 1, this.radius * 2 + 1);
        RenderSystem.color4f(1F, 0F, 0F, 1F);
        tessellator.draw();
        RenderSystem.color4f(1F, 1F, 1F, 1F);
        RenderSystem.disableAlphaTest();
        RenderSystem.disableBlend();
        RenderSystem.enableCull();
        RenderSystem.disableDepthTest();
        matrices.pop();
    }

    public int getVertices() {
        return this.bufferModel.limit() / 12;
    }

    public boolean isInRange(BlockPos offset) {
        int diameter = this.radius * 2 + 1;
        return offset.getX() <= diameter && offset.getY() <= diameter && offset.getZ() <= diameter;
    }

    public boolean isShouldRebuild() {
        return this.shouldRebuild;
    }

    public void markShouldRebuild() {
        this.shouldRebuild = true;
    }

    public boolean loadModelFromResource(Identifier id) {
        if (id == null) return false;

        if (!RenderSystem.isOnRenderThread()) {
            this.recordRenderCall(() -> this.loadModelFromResource(id));
            return true;
        }

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
                    double x, y, z;
                    this.bufferModel.clear();
                    for (int i = 1; i < lines.length; i++) {
                        line = lines[i];
                        if (line.startsWith("//")) continue;
                        String[] divided = line.split(",");
                        x = Double.parseDouble(divided[0]);
                        y = Double.parseDouble(divided[1]);
                        z = Double.parseDouble(divided[2]);
                        this.bufferModel.grow(12);
                        this.bufferModel.put(x, y, z);
                    }
                    this.bufferModel.flip();
                    this.primitiveType = primType;
                    this.bufferModel.uploadToVBO(this.vboModel);
                    this.modelFile = id;
                    this.modelVertices = this.bufferModel.limit() / 12;
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
        Iterator<Chunk> itr = this.chunkIterator();
        while (itr.hasNext()) {
            Chunk chunk = itr.next();
            if (chunk.getOffset().equals(offset) && chunk.getState() == ChunkUpdateState.NEUTRAL) {
                chunk.rebuild(false);
                return;
            }
        }
    }

    private void pollRenderQueue() {
        Runnable task;
        while ((task = this.renderQueue.poll()) != null) {
            task.run();
        }
        RenderUtils.glError("KZEAddon$pollRenderQueue");
    }

    private void pollUpdateQueue() {
        Chunk chunk;
        while (!this.needUpdateChunks.isEmpty()) {
            chunk = this.needUpdateChunks.poll();
            Chunk finalChunk = chunk;
            CompletableFuture.runAsync(() -> finalChunk.rebuild(false));
        }
    }

    @Override
    public void reload(ResourceManager manager) {
        if (this.modelFile != null) {
            this.loadModelFromResource(this.modelFile);
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
        this.recordRenderCall(() -> {
            RenderUtils.glError("setModel head");
            this.bufferModel.updateDataOnVBO(this.vboModel);
            this.modelVertices = vertices.length / 3;
            RenderUtils.glError("setModel tail");
        });
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

    public void tick() {
        if (!KZEAddon.getOptions().isVisualizeBarriers) return;

        BlockPos moveAmount = this.center.subtract(this.lastCenter);
        this.lastCenter.set(this.center);

        if (moveAmount.getX() != 0 || moveAmount.getY() != 0 || moveAmount.getZ() != 0) {
            KZEAddon.info("Move: " + moveAmount.toShortString());
        }

        this.chunks.forEach(it -> it.tick(moveAmount));
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

    private void updateCenter() {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null || this.noCenterUpdate) return;
        HitResult hitResult = player.raycast(KZEAddon.getOptions().barrierVisualizeRaycastDistance, 0, false);
        if (KZEAddon.getOptions().isCrosshairVisualizeOrigin && hitResult.getType() == HitResult.Type.BLOCK) {
            this.center = VanillaUtils.toChunk(((BlockHitResult) hitResult).getBlockPos());
            this.visualizeCenter = hitResult.getPos();
        } else {
            this.center = new BlockPos(player.chunkX, player.chunkY, player.chunkZ);
            this.visualizeCenter = new Vec3d(player.lastRenderX, player.lastRenderY, player.lastRenderZ);
        }
    }

    public void uploadAllChunks() {
        this.chunks.forEach(Chunk::upload);
    }

    public void useDefaultModel() {
        this.primitiveType = GL11.GL_LINES;
        this.modelFile = null;
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

    public class Chunk {
        private final int id;
        private BlockPos offset;
        private int vboOffset;
        private ByteBufferWrapper bufferOffset;
        private int vao;
        private int barrierCount;
        private ChunkUpdateState state;
        private int lastLimit;

        public Chunk(BlockPos offset, int id) {
            this.offset = offset;
            this.id = id;
            this.state = ChunkUpdateState.NEUTRAL;
        }

        public Iterator<BlockPos> chunkIterator() {
            BlockPos origin = this.originInWorldPos();
            return BlockPos.iterate(origin, origin.add(15, 15, 15)).iterator();
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

        public void setOffset(BlockPos offset) {
            this.offset = offset;
        }

        public ChunkUpdateState getState() {
            return this.state;
        }

        public void initialize() {
            this.vao = glGenVertexArrays();
            this.vboOffset = GlStateManager.genBuffers();
            this.bufferOffset = new ByteBufferWrapper(1024 * 1024 * 2, () -> BarrierVisualizer.this.recordRenderCall(() -> {
                RenderUtils.glError("Chunk init head");
                this.bufferOffset.updateSizeOnVBO(this.vboOffset);
                RenderUtils.glError("Chunk init tail");
            }));

            int posIndex = GL20.glGetAttribLocation(BarrierVisualizer.this.SHADER.getProgram(), "position");
            int offset = GL20.glGetAttribLocation(BarrierVisualizer.this.SHADER.getProgram(), "offset");

            glBindVertexArray(this.vao);
            glEnableVertexAttribArray(posIndex);
            glEnableVertexAttribArray(offset);
            glBindBuffer(GL_ARRAY_BUFFER, BarrierVisualizer.this.vboModel);
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

        public BlockPos originInWorldPos() {
            BlockPos center = BarrierVisualizer.this.center;
            return new BlockPos(
                    (center.getX() + this.offset.getX()) * 16,
                    (center.getY() + this.offset.getY()) * 16,
                    (center.getZ() + this.offset.getZ()) * 16
            );
        }

        public void rebuild(boolean forceRebuild) {
            ClientWorld world = MinecraftClient.getInstance().world;
            boolean isLoaded = this.isLoaded();
            if (!this.isRebuilding() && !isLoaded) {
                this.state = ChunkUpdateState.UPDATE_CPU;
                BarrierVisualizer.this.recordRenderCall(() -> this.rebuild(true));
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
                int finalBarrierCount = barrierCount;
                RenderSystem.recordRenderCall(() -> {
                    this.barrierCount = finalBarrierCount;
                    this.lastLimit = this.bufferOffset.limit();
                    this.bufferOffset.updateDataOnVBO(this.vboOffset);
                    RenderUtils.glError("Chunk rebuild task");
                    this.state = ChunkUpdateState.NEUTRAL;
                });
            }
        }

        public void render(int primType) {
            RenderUtils.glError("CIBV render head");

            glBindVertexArray(this.vao);
            GL33.glDrawArraysInstanced(primType, 0, BarrierVisualizer.this.getModelVertices(), this.barrierCount);
            glBindVertexArray(0);

            RenderUtils.glError("CIBV render tail");
        }

        public void setOffset(int x, int y, int z) {
            this.setOffset(new BlockPos(x, y, z));
        }

        public void tick(BlockPos moveAmount) {
            this.tick(moveAmount, false);
        }

        public void tick(BlockPos moveAmount, boolean forceRebuild) {
            if (!forceRebuild) {
                BlockPos origin = this.offset.subtract(moveAmount);
                BlockPos wrapped = wrapOffset(origin, BarrierVisualizer.this.radius);
                if (moveAmount.getX() != 0 || moveAmount.getY() != 0 || moveAmount.getZ() != 0) {
                    this.setOffset(wrapped);
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
                }
            });
        }

        public String toFancyString() {
            return "§" + this.state.getColorCode() + this.getId() + "(" + this.getOffset().toShortString() + ")§r";
        }
    }
}
