package com.theboss.kzeaddonfabric.wip;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.GlAllocationUtils;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MarkedArea {
    private final FloatBuffer MATRIX_BUFFER = GlAllocationUtils.allocateFloatBuffer(4 * 4 * 4);
    private final Area area;

    public MarkedArea(Area area) {
        this.area = area;
    }

    public void zn(BlockPos min, BlockPos max, int lowerAlpha, float uZ, float offsetP, float v, BufferBuilder buffer, StringBuilder builder) {
        // Z-
        buffer.vertex(min.getX(), min.getY(), min.getZ()).color(20, 20, 255, lowerAlpha).texture(uZ, offsetP).next();
        buffer.vertex(max.getX(), min.getY(), min.getZ()).color(20, 20, 255, lowerAlpha).texture(0, offsetP).next();
        buffer.vertex(max.getX(), max.getY(), min.getZ()).color(20, 20, 255, 0).texture(0, offsetP - v).next();
        buffer.vertex(min.getX(), max.getY(), min.getZ()).color(20, 20, 255, 0).texture(uZ, offsetP - v).next();
        builder.append("Z- ");
    }

    public void zp(BlockPos min, BlockPos max, int lowerAlpha, float uZ, float offsetP, float v, BufferBuilder buffer, StringBuilder builder) {
        // Z+
        buffer.vertex(min.getX(), min.getY(), max.getZ()).color(20, 20, 255, lowerAlpha).texture(uZ, offsetP).next();
        buffer.vertex(max.getX(), min.getY(), max.getZ()).color(20, 20, 255, lowerAlpha).texture(0, offsetP).next();
        buffer.vertex(max.getX(), max.getY(), max.getZ()).color(20, 20, 255, 0).texture(0, offsetP - v).next();
        buffer.vertex(min.getX(), max.getY(), max.getZ()).color(20, 20, 255, 0).texture(uZ, offsetP - v).next();
        builder.append("Z+ ");
    }

    public void xn(BlockPos min, BlockPos max, int lowerAlpha, float uX, float offsetP, float v, BufferBuilder buffer, StringBuilder builder) {
        // X-
        buffer.vertex(min.getX(), min.getY(), min.getZ()).color(20, 20, 255, lowerAlpha).texture(uX, offsetP).next();
        buffer.vertex(min.getX(), min.getY(), max.getZ()).color(20, 20, 255, lowerAlpha).texture(0, offsetP).next();
        buffer.vertex(min.getX(), max.getY(), max.getZ()).color(20, 20, 255, 0).texture(0, offsetP - v).next();
        buffer.vertex(min.getX(), max.getY(), min.getZ()).color(20, 20, 255, 0).texture(uX, offsetP - v).next();
        builder.append("X- ");
    }

    public void xp(BlockPos min, BlockPos max, int lowerAlpha, float uX, float offsetP, float v, BufferBuilder buffer, StringBuilder builder) {
        // X+
        buffer.vertex(max.getX(), min.getY(), min.getZ()).color(20, 20, 255, lowerAlpha).texture(uX, offsetP).next();
        buffer.vertex(max.getX(), max.getY(), min.getZ()).color(20, 20, 255, 0).texture(uX, offsetP - v).next();
        buffer.vertex(max.getX(), max.getY(), max.getZ()).color(20, 20, 255, 0).texture(0, offsetP - v).next();
        buffer.vertex(max.getX(), min.getY(), max.getZ()).color(20, 20, 255, lowerAlpha).texture(0, offsetP).next();
        builder.append("X+ ");
    }

    public void render(Vec3d cam, BlockPos min, BlockPos max, int lowerAlpha, float uX, float uZ, float offsetP, float v, BufferBuilder buffer) {
        int halfXDiff = this.area.getXDiff() / 2;
        int halfZDiff = this.area.getZDiff() / 2;
        BlockPos c = min.add(halfXDiff, 0, halfZDiff);
        Map<Vec3d, Runnable> map = new HashMap<>();
        StringBuilder builder = new StringBuilder();

        map.put(new Vec3d(c.getX(), c.getY(), c.getZ() - halfZDiff), () -> this.zn(min, max, lowerAlpha, uZ, offsetP, v, buffer, builder));
        map.put(new Vec3d(c.getX() + halfXDiff, c.getY(), c.getZ()), () -> this.xp(min, max, lowerAlpha, uX, offsetP, v, buffer, builder));
        map.put(new Vec3d(c.getX(), c.getY(), c.getZ() + halfZDiff), () -> this.zp(min, max, lowerAlpha, uZ, offsetP, v, buffer, builder));
        map.put(new Vec3d(c.getX() - halfXDiff, c.getY(), c.getZ()), () -> this.xn(min, max, lowerAlpha, uX, offsetP, v, buffer, builder));

        List<Vec3d> sortOnly = new ArrayList<>(map.keySet());
        sortOnly.sort((v1, v2) -> MarkedArea.distanceSort(cam, v1, v2));

        for (Vec3d vec : sortOnly) {
            map.get(vec).run();
        }


        MinecraftClient.getInstance().inGameHud.setOverlayMessage(Text.of(builder.toString() + " | " + c.getX() + ", " + c.getY() + ", " + c.getZ()), true);
    }

    public static int distanceSort(Vec3d origin, Vec3d pos1, Vec3d pos2) {
        double d1 = origin.distanceTo(pos1);
        double d2 = origin.distanceTo(pos2);
        return Double.compare(d2, d1);
    }

    public void render(MatrixStack matrices, float delta) {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        Vec3d cam = client.gameRenderer.getCamera().getPos();

        if (player == null) return;

        double distance = Math.max(0.0, this.area.distanceTo(player.getPos()) - Math.abs(this.area.getXDiff() / 2.0));
        int lowerAlpha = (int) (255 * (1 - clamp(distance / 16, 0.0, 1.0)));
        long timeMillis = System.currentTimeMillis();
        float offsetP = timeMillis % 1000 / 1000.0F;
        float v = this.area.getYDiff();
        float uX = this.area.getXDiff();
        float uZ = this.area.getZDiff();
        BlockPos min = this.area.begin;
        BlockPos max = this.area.end;

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        matrices.push();
        matrices.translate(-cam.x, -cam.y, -cam.z);
        matrices.peek().getModel().writeRowFirst(this.MATRIX_BUFFER);
        GL11.glMultMatrixf(this.MATRIX_BUFFER);
        RenderSystem.enableBlend();
        RenderSystem.shadeModel(GL11.GL_SMOOTH);
        RenderSystem.enablePolygonOffset();
        RenderSystem.polygonOffset(-1, -1);
        RenderSystem.disableCull();
        client.getTextureManager().bindTexture(new Identifier("kzeaddon-fabric", "textures/wip/marked_area.png"));
        buffer.begin(GL11.GL_QUADS, VertexFormats.POSITION_COLOR_TEXTURE);
        this.render(cam, min, max, lowerAlpha, uX, uZ, offsetP, v, buffer);
        tessellator.draw();

        GL11.glLoadIdentity();
        matrices.pop();
        RenderSystem.enableCull();
        RenderSystem.disablePolygonOffset();
        RenderSystem.shadeModel(GL11.GL_FLAT);
        RenderSystem.disableBlend();
    }

    public static double clamp(double x, double min, double max) {
        return Math.min(Math.max(x, min), max);
    }

    public static class Area {
        private BlockPos begin;
        private BlockPos end;

        public Area(BlockPos begin, BlockPos end) {
            this.begin = begin;
            this.end = end;

            this.sort();
        }

        protected void sort() {
            BlockPos begin = new BlockPos(Math.min(this.begin.getX(), this.end.getX()), Math.min(this.begin.getY(), this.end.getY()), Math.min(this.begin.getZ(), this.end.getZ()));
            BlockPos end = new BlockPos(Math.max(this.begin.getX(), this.end.getX()), Math.max(this.begin.getY(), this.end.getY()), Math.max(this.begin.getZ(), this.end.getZ()));

            this.begin = begin;
            this.end = end;
        }

        public BlockPos getBegin() {
            return this.begin;
        }

        public BlockPos getEnd() {
            return this.end;
        }

        public void setBegin(BlockPos begin) {
            this.begin = begin;
            this.sort();
        }

        public void setEnd(BlockPos end) {
            this.end = end;
            this.sort();
        }

        public int getXDiff() {
            return this.end.getX() - this.begin.getX();
        }

        public int getYDiff() {
            return this.end.getY() - this.begin.getY();
        }

        public int getZDiff() {
            return this.end.getZ() - this.begin.getZ();
        }

        public double distanceTo(Vec3d loc) {
            return loc.distanceTo(new Vec3d(this.begin.getX() + this.getXDiff() / 2.0, this.begin.getY() + this.getYDiff() / 2.0, this.begin.getZ() + this.getZDiff() / 2.0));
        }

        public int getMinX() {
            return this.begin.getX();
        }

        public int getMaxX() {
            return this.end.getX();
        }

        public int getMinY() {
            return this.begin.getY();
        }

        public int getMaxY() {
            return this.end.getY();
        }

        public int getMinZ() {
            return this.begin.getZ();
        }

        public int getMaxZ() {
            return this.end.getZ();
        }
    }
}
