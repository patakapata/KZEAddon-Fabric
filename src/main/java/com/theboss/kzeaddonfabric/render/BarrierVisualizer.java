package com.theboss.kzeaddonfabric.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.theboss.kzeaddonfabric.KZEAddon;
import com.theboss.kzeaddonfabric.enums.BarrierVisualizeOrigin;
import com.theboss.kzeaddonfabric.render.shader.InvertShader;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class BarrierVisualizer {
    private final Controller controller;
    private int[] color;

    public static BarrierVisualizer getInstance() {
        return KZEAddon.BAR_VISUALIZER;
    }

    public BarrierVisualizer() {
        this.controller = new Controller();
        this.color = new int[]{255, 255, 255, 255};
    }

    public void destroy() {
        this.controller.destroy();
    }

    public void draw(MatrixStack matrices, float delta) {
        MinecraftClient client = MinecraftClient.getInstance();
        Profiler profiler = client.getProfiler();
        Vec3d pos = client.gameRenderer.getCamera().getPos();

        profiler.push("Generate mvp");
        matrices.push();
        matrices.translate(-pos.x, -pos.y, -pos.z);
        MatrixStack mvp = new MatrixStack();
        mvp.peek().getModel().multiply(client.gameRenderer.getBasicProjectionMatrix(client.gameRenderer.getCamera(), delta, true));
        if (MinecraftClient.getInstance().options.bobView) {
            KZEAddon.bobView(mvp, delta);
        }
        mvp.peek().getModel().multiply(matrices.peek().getModel());
        matrices.pop();

        InvertShader.INSTANCE.setMVP(mvp.peek().getModel());
        InvertShader.INSTANCE.bind();
        profiler.swap("Draw");
        RenderSystem.disableTexture();
        this.controller.draw();
        RenderSystem.enableTexture();
        profiler.pop();
        InvertShader.INSTANCE.unbind();
    }

    public int[] getColor() {
        return this.color;
    }

    public void setColor(int[] newColor) {
        if (newColor.length == 4) {
            this.color = newColor;
        } else if (newColor.length == 3) {
            this.color = new int[]{newColor[0], newColor[1], newColor[2], 255};
        }
    }

    public int getDistance() {
        return this.controller.renderDistance;
    }

    public void setDistance(int distance) {
        if (distance < 0) throw new IllegalArgumentException("distance should be bigger than 0!");
        this.controller.resizeTo = distance;
    }

    public void init() {
        this.controller.init(0);
    }

    public void tick() {
        if (MinecraftClient.getInstance().player == null) return;
        BlockPos center = null;
        if (KZEAddon.Options.getBarrierVisualizeOrigin() == BarrierVisualizeOrigin.MYSELF) {
            center = MinecraftClient.getInstance().player.getBlockPos();
        } else {
            HitResult result = MinecraftClient.getInstance().player.raycast(50.0, MinecraftClient.getInstance().getTickDelta(), false);
            if (result.getType() == HitResult.Type.BLOCK) {
                center = ((BlockHitResult) result).getBlockPos();
            }
        }
        if (center != null) {
            this.controller.tick(center);
        } else {
            System.err.println("Center is null!");
        }
    }

    public static class Chunk {
        private final VBOWrapper vbo = new VBOWrapper();
        private final int id;
        private boolean[][][] flags;
        private BlockPos origin;
        private boolean isChanged;
        private boolean isReady;

        public Chunk(int id) {
            this.id = id;
        }

        public void destroy() {
            this.vbo.destroy();
        }

        public void draw() {
            if (!this.isReady) return;

            if (this.isChanged) {
                this.isChanged = false;
                this.vbo.upload();
            }

            this.vbo.bind();
            this.vbo.draw();
            this.vbo.unbind();
        }

        public int getId() {
            return this.id;
        }

        public void init(int initialCapacity, BlockPos origin) {
            this.vbo.init(initialCapacity);
            this.origin = origin;
            this.flags = new boolean[16][16][16];
            this.resetFlags();
        }

        public void resetFlags() {
            for (int x = 0; x < 16; x++)
                for (int y = 0; y < 16; y++)
                    for (int z = 0; z < 16; z++)
                        this.flags[x][y][z] = false;
        }

        public void setOrigin(BlockPos origin) {
            if (!(this.origin.equals(origin))) {
                this.origin = origin;
                this.resetFlags();
                this.isChanged = true;
                this.isReady = false;
            }
        }

        public void update(World world, boolean isForce) {
            boolean needUpload = isForce;
            BlockPos.Mutable pos = new BlockPos.Mutable();
            for (int x = 0; x < 16; x++) {
                pos.setX(this.origin.getX() + x);
                for (int y = 0; y < 16; y++) {
                    pos.setY(this.origin.getY() + y);
                    for (int z = 0; z < 16; z++) {
                        pos.setZ(this.origin.getZ() + z);

                        boolean isMatch = world.getBlockState(pos).getBlock().equals(Blocks.BARRIER);
                        if (this.flags[x][y][z] != isMatch) {
                            needUpload = true;
                            this.flags[x][y][z] = isMatch;
                        }
                    }
                }
            }

            if (needUpload) {
                this.vbo.begin(GL11.GL_LINES, VertexFormats.POSITION_COLOR);
                for (int x = 0; x < 16; x++) {
                    pos.setX(this.origin.getX() + x);
                    for (int y = 0; y < 16; y++) {
                        pos.setY(this.origin.getY() + y);
                        for (int z = 0; z < 16; z++) {
                            pos.setZ(this.origin.getZ() + z);
                            if (this.flags[x][y][z]) {
                                this.wireframe(pos);
                            }
                        }
                    }
                }
                this.vbo.end();
                this.isReady = true;
                RenderSystem.recordRenderCall(this.vbo::upload);
            }
        }

        private void wireframe(BlockPos pos) {
            int[] color = getInstance().getColor();
            float[] x = new float[]{pos.getX() + 0.1F, pos.getX() + 0.9F};
            float[] y = new float[]{pos.getY() + 0.1F, pos.getY() + 0.9F};
            float[] z = new float[]{pos.getZ() + 0.1F, pos.getZ() + 0.9F};

            this.vbo.vertex(x[0], y[0], z[0]);
            this.vbo.color(color[0], color[1], color[2], color[3]);
            this.vbo.vertex(x[1], y[0], z[0]);
            this.vbo.color(color[0], color[1], color[2], color[3]);
            this.vbo.vertex(x[1], y[0], z[0]);
            this.vbo.color(color[0], color[1], color[2], color[3]);
            this.vbo.vertex(x[1], y[0], z[1]);
            this.vbo.color(color[0], color[1], color[2], color[3]);
            this.vbo.vertex(x[1], y[0], z[1]);
            this.vbo.color(color[0], color[1], color[2], color[3]);
            this.vbo.vertex(x[0], y[0], z[1]);
            this.vbo.color(color[0], color[1], color[2], color[3]);
            this.vbo.vertex(x[0], y[0], z[1]);
            this.vbo.color(color[0], color[1], color[2], color[3]);
            this.vbo.vertex(x[0], y[0], z[0]);
            this.vbo.color(color[0], color[1], color[2], color[3]);

            this.vbo.vertex(x[0], y[1], z[0]);
            this.vbo.color(color[0], color[1], color[2], color[3]);
            this.vbo.vertex(x[1], y[1], z[0]);
            this.vbo.color(color[0], color[1], color[2], color[3]);
            this.vbo.vertex(x[1], y[1], z[0]);
            this.vbo.color(color[0], color[1], color[2], color[3]);
            this.vbo.vertex(x[1], y[1], z[1]);
            this.vbo.color(color[0], color[1], color[2], color[3]);
            this.vbo.vertex(x[1], y[1], z[1]);
            this.vbo.color(color[0], color[1], color[2], color[3]);
            this.vbo.vertex(x[0], y[1], z[1]);
            this.vbo.color(color[0], color[1], color[2], color[3]);
            this.vbo.vertex(x[0], y[1], z[1]);
            this.vbo.color(color[0], color[1], color[2], color[3]);
            this.vbo.vertex(x[0], y[1], z[0]);
            this.vbo.color(color[0], color[1], color[2], color[3]);

            this.vbo.vertex(x[0], y[0], z[0]);
            this.vbo.color(color[0], color[1], color[2], color[3]);
            this.vbo.vertex(x[0], y[1], z[0]);
            this.vbo.color(color[0], color[1], color[2], color[3]);
            this.vbo.vertex(x[1], y[0], z[0]);
            this.vbo.color(color[0], color[1], color[2], color[3]);
            this.vbo.vertex(x[1], y[1], z[0]);
            this.vbo.color(color[0], color[1], color[2], color[3]);
            this.vbo.vertex(x[1], y[0], z[1]);
            this.vbo.color(color[0], color[1], color[2], color[3]);
            this.vbo.vertex(x[1], y[1], z[1]);
            this.vbo.color(color[0], color[1], color[2], color[3]);
            this.vbo.vertex(x[0], y[0], z[1]);
            this.vbo.color(color[0], color[1], color[2], color[3]);
            this.vbo.vertex(x[0], y[1], z[1]);
            this.vbo.color(color[0], color[1], color[2], color[3]);
        }
    }

    public static class Controller {
        private final Consumer<Chunk> UPDATER = chunk -> CompletableFuture.runAsync(() -> chunk.update(MinecraftClient.getInstance().world, false));

        private int renderDistance;
        private int diameter;
        private Chunk[][][] chunks;
        private int lastChunkX;
        private int lastChunkY;
        private int lastChunkZ;
        private int resizeTo = -1;
        private boolean isFirst = true;
        private boolean shouldRebuild;

        public void destroy() {
            for (int x = 0; x < this.diameter; x++) {
                for (int y = 0; y < this.diameter; y++) {
                    for (int z = 0; z < this.diameter; z++) {
                        this.chunks[x][y][z].destroy();
                    }
                }
            }
        }

        public void draw() {
            Profiler profiler = KZEAddon.getProfiler();

            profiler.push("Resize a buffer");
            if (this.resizeTo != -1) {
                this.init(this.resizeTo);
                this.reallocate();
                this.resizeTo = -1;
            }

            profiler.swap("Draw chunks");
            this.run(Chunk::draw);
            profiler.pop();
        }

        public void init(int renderDistance) {
            if (!this.isFirst) {
                this.destroy();
            }
            this.isFirst = false;
            this.renderDistance = renderDistance;
            this.diameter = this.renderDistance * 2 + 1;
            this.chunks = new Chunk[this.diameter][this.diameter][this.diameter];
            int id = 0;
            for (int x = 0; x < this.diameter; x++) {
                for (int y = 0; y < this.diameter; y++) {
                    for (int z = 0; z < this.diameter; z++) {
                        Chunk chunk = new Chunk(id++);
                        chunk.init(1, new BlockPos(0, 0, 0));
                        this.chunks[x][y][z] = chunk;
                    }
                }
            }
        }

        public void move(int x, int y, int z) {
            if (x != 0 && x < this.diameter) this.moveOnX(x);
            if (y != 0 && y < this.diameter) this.moveOnY(y);
            if (z != 0 && z < this.diameter) this.moveOnZ(z);
            if (x != 0 || y != 0 || z != 0) {
                this.shouldRebuild = true;
            }
        }

        private void moveOnX(int amount) {
            Chunk[][][] holder = new Chunk[this.diameter][this.diameter][this.diameter];

            for (int x = 0; x < this.diameter; x++) {
                for (int y = 0; y < this.diameter; y++) {
                    for (int z = 0; z < this.diameter; z++) {
                        int moveTo = (x + amount) % this.diameter;
                        if (moveTo < 0) moveTo = this.diameter + moveTo;
                        holder[moveTo][y][z] = this.chunks[x][y][z];
                    }
                }
            }

            this.chunks = holder;
        }

        private void moveOnY(int amount) {
            Chunk[][][] holder = new Chunk[this.diameter][this.diameter][this.diameter];

            for (int x = 0; x < this.diameter; x++) {
                for (int y = 0; y < this.diameter; y++) {
                    for (int z = 0; z < this.diameter; z++) {
                        int moveTo = (y + amount) % this.diameter;
                        if (moveTo < 0) moveTo = this.diameter + moveTo;
                        holder[x][moveTo][z] = this.chunks[x][y][z];
                    }
                }
            }

            this.chunks = holder;
        }

        private void moveOnZ(int amount) {
            Chunk[][][] holder = new Chunk[this.diameter][this.diameter][this.diameter];

            for (int x = 0; x < this.diameter; x++) {
                for (int y = 0; y < this.diameter; y++) {
                    for (int z = 0; z < this.diameter; z++) {
                        int moveTo = (z + amount) % this.diameter;
                        if (moveTo < 0) moveTo = this.diameter + moveTo;
                        holder[x][y][moveTo] = this.chunks[x][y][z];
                    }
                }
            }

            this.chunks = holder;
        }

        public void reallocate() {
            int startX = this.lastChunkX - this.renderDistance;
            int startY = this.lastChunkY - this.renderDistance;
            int startZ = this.lastChunkZ - this.renderDistance;

            for (int x = 0; x < this.diameter; x++) {
                for (int y = 0; y < this.diameter; y++) {
                    for (int z = 0; z < this.diameter; z++) {
                        int chunkX = (startX + x) * 16;
                        int chunkY = (startY + y) * 16;
                        int chunkZ = (startZ + z) * 16;
                        this.chunks[x][y][z].setOrigin(new BlockPos(chunkX, chunkY, chunkZ));
                    }
                }
            }
        }

        public int round(int value) {
            boolean isNegative = value < 0;
            if (isNegative) ++value;
            int result = value / 16;
            return isNegative ? result - 1 : result;
        }

        public void run(Consumer<Chunk> task) {
            for (int x = 0; x < this.diameter; x++) {
                for (int y = 0; y < this.diameter; y++) {
                    for (int z = 0; z < this.diameter; z++) {
                        task.accept(this.chunks[x][y][z]);
                    }
                }
            }
        }

        public void tick(BlockPos center) {
            int x = this.round(center.getX());
            int y = this.round(center.getY());
            int z = this.round(center.getZ());
            this.updateRegion(x, y, z);
            // this.run(chunk -> CompletableFuture.runAsync(() -> chunk.update(world, this.shouldRebuild)));
            this.run(this.UPDATER);
            this.shouldRebuild = false;
        }

        public void updateRegion(int centerChunkX, int centerChunkY, int centerChunkZ) {
            int moveX = centerChunkX - this.lastChunkX;
            int moveY = centerChunkY - this.lastChunkY;
            int moveZ = centerChunkZ - this.lastChunkZ;

            this.lastChunkX = centerChunkX;
            this.lastChunkY = centerChunkY;
            this.lastChunkZ = centerChunkZ;

            if (!(moveX == 0 && moveY == 0 && moveZ == 0)) {
                this.move(-moveX, -moveY, -moveZ);
                this.reallocate();
            }
        }
    }
}
