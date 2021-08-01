package com.theboss.kzeaddonfabric.wip;

import com.theboss.kzeaddonfabric.SimplexNoise;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
import org.lwjgl.opengl.GL11;

import java.util.function.Supplier;

public class PerlinParticle {
    private double posX;
    private double posY;
    private double posZ;
    private double lastPosX;
    private double lastPosY;
    private double lastPosZ;
    private int lifeTime;
    private int age;
    private double seed;
    private float offsetU;
    private float offsetV;
    private Supplier<Boolean> isShouldMove;

    public static int whichNear(Vec3d cam, PerlinParticle p1, PerlinParticle p2) {
        if (cam == null || p1 == null || p2 == null) return 0;
        double d1 = cam.squaredDistanceTo(p1.posX, p1.posY, p1.posZ);
        double d2 = cam.squaredDistanceTo(p2.posX, p2.posY, p2.posZ);
        return Double.compare(d2, d1);
    }

    public PerlinParticle(double posX, double posY, double posZ, int lifeTime, double seed) {
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;
        this.lastPosX = this.posX;
        this.lastPosY = this.posY;
        this.lastPosZ = this.posZ;
        this.lifeTime = lifeTime;
        this.seed = seed;
        this.age = 0;
        this.isShouldMove = () -> true;
        this.updateUV(1);
    }

    public int getAge() {
        return this.age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public Supplier<Boolean> getIsShouldMove() {
        return this.isShouldMove;
    }

    public void setIsShouldMove(Supplier<Boolean> isShouldMove) {
        this.isShouldMove = isShouldMove;
    }

    public int getLifeTime() {
        return this.lifeTime;
    }

    public void setLifeTime(int lifeTime) {
        this.lifeTime = lifeTime;
    }

    public double getPosX() {
        return this.posX;
    }

    public void setPosX(double posX) {
        this.posX = posX;
    }

    public double getPosY() {
        return this.posY;
    }

    public void setPosY(double posY) {
        this.posY = posY;
    }

    public double getPosZ() {
        return this.posZ;
    }

    public void setPosZ(double posZ) {
        this.posZ = posZ;
    }

    public boolean isOutdated() {
        return this.age >= this.lifeTime;
    }

    public void render(MatrixStack matrices, float delta) {
        MinecraftClient client = MinecraftClient.getInstance();
        Camera camera = client.gameRenderer.getCamera();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        Matrix4f matrix;

        //        float x = (float) this.posX;
        //        float y = (float) this.posY;
        //        float z = (float) this.posZ;

        float x = (float) MathHelper.lerp(delta, this.lastPosX, this.posX);
        float y = (float) MathHelper.lerp(delta, this.lastPosY, this.posY);
        float z = (float) MathHelper.lerp(delta, this.lastPosZ, this.posZ);

        this.lastPosX = x;
        this.lastPosY = y;
        this.lastPosZ = z;

        matrices.push();
        matrix = matrices.peek().getModel();
        matrices.translate(x, y, z);
        matrices.multiply(Vec3f.NEGATIVE_Y.getDegreesQuaternion(camera.getYaw() + 180.0F));
        matrices.multiply(Vec3f.NEGATIVE_X.getDegreesQuaternion(camera.getPitch()));
        float alpha = 1 - (float) this.age / this.lifeTime;
        buffer.begin(GL11.GL_QUADS, VertexFormats.POSITION_COLOR_TEXTURE);
        buffer.vertex(matrix, -0.05F, 0.05F, 0F).color(1F, 1F, 1F, alpha).texture(this.offsetU, this.offsetV).next();
        buffer.vertex(matrix, -0.05F, -0.05F, 0F).color(1F, 1F, 1F, alpha).texture(this.offsetU, this.offsetV + 0.5F).next();
        buffer.vertex(matrix, 0.05F, -0.05F, 0F).color(1F, 1F, 1F, alpha).texture(this.offsetU + 0.5F, this.offsetV + 0.5F).next();
        buffer.vertex(matrix, 0.05F, 0.05F, 0F).color(1F, 1F, 1F, alpha).texture(this.offsetU + 0.5F, this.offsetV).next();
        tessellator.draw();
        matrices.pop();
    }

    public void tick(MinecraftClient client) {
        //        double factor = KZEAddon.getWorldTime() / 20.0 / 2_0;

        double moveX = SimplexNoise.noise(this.posZ, this.seed) / 4;
        double moveY = SimplexNoise.noise(this.posX, this.seed) / 4;
        double moveZ = SimplexNoise.noise(this.posY, this.seed) / 4;

        //        this.lastPosX = this.posX;
        //        this.lastPosY = this.posY;
        //        this.lastPosZ = this.posZ;

        if (this.isShouldMove.get()) {
            this.posX += moveX;
            this.posY += moveY;
            this.posZ += moveZ;
        }

        double texSeed = 1 - (double) this.age / this.lifeTime;
        this.updateUV(texSeed);

        this.age++;
    }

    public void updateUV(double seed) {
        if (seed <= 0.25) {
            this.offsetU = 0.0F;
            this.offsetV = 0.0F;
        } else if (seed <= 0.5) {
            this.offsetU = 0.5F;
            this.offsetV = 0.0F;
        } else if (seed <= 0.75) {
            this.offsetU = 0.0F;
            this.offsetV = 0.5F;
        } else {
            this.offsetU = 0.5F;
            this.offsetV = 0.5F;
        }
    }
}
