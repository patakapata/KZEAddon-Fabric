package com.theboss.kzeaddonfabric.wip;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PlaneLayers {
    private final List<Plane> planes;

    public PlaneLayers(Plane... planes) {
        this.planes = new ArrayList<>(Arrays.asList(planes));
    }

    public void render(MatrixStack matrices, float x, float y, float z, float delta) {
        RenderSystem.enablePolygonOffset();
        RenderSystem.polygonOffset(-1, -1);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        Vec3d cam = MinecraftClient.getInstance().gameRenderer.getCamera().getPos();
        matrices.push();
        matrices.translate(x - cam.x, y - cam.y, z - cam.z);
        this.planes.forEach(plane -> plane.render(tessellator, buffer, matrices, delta));
        matrices.pop();
        RenderSystem.disablePolygonOffset();
    }

    public static abstract class Plane {
        private Identifier texture;

        public Plane(Identifier texture) {
            this.texture = texture;
        }

        public void bindTexture() {
            MinecraftClient.getInstance().getTextureManager().bindTexture(this.texture);
        }

        public Identifier getTexture() {
            return this.texture;
        }

        public void setTexture(Identifier texture) {
            this.texture = texture;
        }

        public abstract void render(Tessellator tessellator, BufferBuilder buffer, MatrixStack matrices, float delta);
    }
}
