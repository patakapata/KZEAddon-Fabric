package com.theboss.kzeaddonfabric.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.GlAllocationUtils;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

import java.nio.FloatBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class BarrierVisualizer {
    private final Controller controller;
    private final FloatBuffer MATRIX_BUFFER = GlAllocationUtils.allocateFloatBuffer(4 * 4 * 4);

    public BarrierVisualizer() {
        this.controller = new Controller();
    }

    public void init() {
        this.controller.init(0);
    }

    public void destroy() {
        this.controller.destroy();
    }

    public void tick() {
        if (MinecraftClient.getInstance().player == null) return;
        this.controller.tick(MinecraftClient.getInstance().player.getBlockPos());
    }

    public void draw(MatrixStack matrices, float delta) {

        MinecraftClient client = MinecraftClient.getInstance();
        Vec3d pos = client.gameRenderer.getCamera().getPos();

        matrices.push();
        matrices.translate(-pos.x, -pos.y, -pos.z);
        Matrix4f matrix = matrices.peek().getModel();
        matrix.writeToBuffer(MATRIX_BUFFER);
        matrices.pop();

        GL11.glMultMatrixf(MATRIX_BUFFER);
        this.controller.draw();
        GL11.glLoadIdentity();
    }

    public void setDistance(int distance) {
        if (distance < 0) throw new IllegalArgumentException("distance should be bigger than 0!");
        this.controller.resizeTo = distance;
    }

    public int getDistance() {
        return this.controller.renderDistance;
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

        public void init(int renderDistance) {
            if (!this.isFirst) {
                this.destroy();
            }
            this.isFirst = false;
            this.renderDistance = renderDistance;
            this.diameter = this.renderDistance * 2 + 1;
            this.chunks = new Chunk[diameter][diameter][diameter];
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

        public void destroy() {
            for (int x = 0; x < this.diameter; x++) {
                for (int y = 0; y < this.diameter; y++) {
                    for (int z = 0; z < this.diameter; z++) {
                        this.chunks[x][y][z].destroy();
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

        public void tick(BlockPos center) {
            this.updateRegion(center.getX() / 16, center.getY() / 16, center.getZ() / 16);
            // this.run(chunk -> CompletableFuture.runAsync(() -> chunk.update(world, this.shouldRebuild)));
            this.run(UPDATER);
            this.shouldRebuild = false;
        }

        public void draw() {
            if (this.resizeTo != -1) {
                this.init(this.resizeTo);
                this.reallocate();
                this.resizeTo = -1;
            }

            this.run(Chunk::draw);
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

    public static class Chunk {
        private final VBOWrapper vbo = new VBOWrapper();
        private boolean[][][] flags;
        private BlockPos origin;
        private boolean isChanged;
        private boolean isReady;
        private int id;

        public Chunk(int id) {
            this.id = id;
        }

        public int getId() {
            return this.id;
        }

        public void init(int initialCapacity, BlockPos origin) {
            vbo.init(initialCapacity);
            this.origin = origin;
            this.flags = new boolean[16][16][16];
            this.resetFlags();
        }

        public void destroy() {
            this.vbo.destroy();
        }

        public void setOrigin(BlockPos origin) {
            if (!(this.origin.equals(origin))) {
                this.origin = origin;
                this.resetFlags();
                this.isChanged = true;
                this.isReady = false;
            }
        }

        public void resetFlags() {
            for (int x = 0; x < 16; x++)
                for (int y = 0; y < 16; y++)
                    for (int z = 0; z < 16; z++)
                        this.flags[x][y][z] = false;
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
            }
            // this.isChanged = needUpload;
            if (needUpload) {
                RenderSystem.recordRenderCall(this.vbo::upload);
            }
        }

        private void wireframe(BlockPos pos) {
            float[] x = new float[]{pos.getX() + 0.1F, pos.getX() + 0.9F};
            float[] y = new float[]{pos.getY() + 0.1F, pos.getY() + 0.9F};
            float[] z = new float[]{pos.getZ() + 0.1F, pos.getZ() + 0.9F};

            this.vbo.vertex(x[0], y[0], z[0]);
            this.vbo.color(255, 0, 0, 255);
            this.vbo.vertex(x[1], y[0], z[0]);
            this.vbo.color(255, 0, 0, 255);
            this.vbo.vertex(x[1], y[0], z[0]);
            this.vbo.color(255, 0, 0, 255);
            this.vbo.vertex(x[1], y[0], z[1]);
            this.vbo.color(255, 0, 0, 255);
            this.vbo.vertex(x[1], y[0], z[1]);
            this.vbo.color(255, 0, 0, 255);
            this.vbo.vertex(x[0], y[0], z[1]);
            this.vbo.color(255, 0, 0, 255);
            this.vbo.vertex(x[0], y[0], z[1]);
            this.vbo.color(255, 0, 0, 255);
            this.vbo.vertex(x[0], y[0], z[0]);
            this.vbo.color(255, 0, 0, 255);

            this.vbo.vertex(x[0], y[1], z[0]);
            this.vbo.color(255, 0, 0, 255);
            this.vbo.vertex(x[1], y[1], z[0]);
            this.vbo.color(255, 0, 0, 255);
            this.vbo.vertex(x[1], y[1], z[0]);
            this.vbo.color(255, 0, 0, 255);
            this.vbo.vertex(x[1], y[1], z[1]);
            this.vbo.color(255, 0, 0, 255);
            this.vbo.vertex(x[1], y[1], z[1]);
            this.vbo.color(255, 0, 0, 255);
            this.vbo.vertex(x[0], y[1], z[1]);
            this.vbo.color(255, 0, 0, 255);
            this.vbo.vertex(x[0], y[1], z[1]);
            this.vbo.color(255, 0, 0, 255);
            this.vbo.vertex(x[0], y[1], z[0]);
            this.vbo.color(255, 0, 0, 255);

            this.vbo.vertex(x[0], y[0], z[0]);
            this.vbo.color(255, 0, 0, 255);
            this.vbo.vertex(x[0], y[1], z[0]);
            this.vbo.color(255, 0, 0, 255);
            this.vbo.vertex(x[1], y[0], z[0]);
            this.vbo.color(255, 0, 0, 255);
            this.vbo.vertex(x[1], y[1], z[0]);
            this.vbo.color(255, 0, 0, 255);
            this.vbo.vertex(x[1], y[0], z[1]);
            this.vbo.color(255, 0, 0, 255);
            this.vbo.vertex(x[1], y[1], z[1]);
            this.vbo.color(255, 0, 0, 255);
            this.vbo.vertex(x[0], y[0], z[1]);
            this.vbo.color(255, 0, 0, 255);
            this.vbo.vertex(x[0], y[1], z[1]);
            this.vbo.color(255, 0, 0, 255);
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
    }
}
