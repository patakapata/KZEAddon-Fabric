package com.theboss.kzeaddonfabric.wip;

import com.mojang.blaze3d.systems.RenderSystem;
import com.theboss.kzeaddonfabric.screen.Screen;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.Matrix4f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

public class WidgetSelectScreen extends Screen {
    private boolean isMenuOpened;
    private double menuX;
    private double menuY;

    public WidgetSelectScreen() {
        super(Text.of("Widget select screen"));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            this.isMenuOpened = true;
            this.menuX = mouseX;
            this.menuY = mouseY;
            return true;
        } else if (this.isMenuOpened) {
            this.isMenuOpened = false;
            this.menuX = 0;
            this.menuY = 0;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        Matrix4f matrix = matrices.peek().getModel();

        if (this.isMenuOpened) {
            float left = (float) this.menuX;
            float right = left + 50;
            float up = (float) this.menuY;
            float down = up + 50;

            RenderSystem.disableTexture();
            buffer.begin(GL11.GL_LINE_LOOP, VertexFormats.POSITION);
            buffer.vertex(matrix, left, up, 0).next();
            buffer.vertex(matrix, left, down, 0).next();
            buffer.vertex(matrix, right, down, 0).next();
            buffer.vertex(matrix, right, up, 0).next();
            tessellator.draw();

            if (left <= mouseX && mouseX <= right && up <= mouseY && mouseY <= down) {
                int segment = (int) ((mouseY - up) / 25);

                RenderSystem.color4f(1F, 1F, 1F, 0.5F);
                RenderSystem.enableBlend();
                buffer.begin(GL11.GL_QUADS, VertexFormats.POSITION);
                buffer.vertex(matrix, left, up + 25 * segment, 0).next();
                buffer.vertex(matrix, left, up + 25 * (segment + 1), 0).next();
                buffer.vertex(matrix, right, up + 25 * (segment + 1), 0).next();
                buffer.vertex(matrix, right, up + 25 * segment, 0).next();
                tessellator.draw();
                RenderSystem.disableBlend();
                RenderSystem.color4f(1F, 1F, 1F, 1F);
            }

            RenderSystem.enableTexture();
        }
    }
}
