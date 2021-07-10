package com.theboss.kzeaddonfabric.screen.button;


import com.mojang.blaze3d.systems.RenderSystem;
import com.theboss.kzeaddonfabric.Color;
import com.theboss.kzeaddonfabric.screen.ColorSelectScreen;
import com.theboss.kzeaddonfabric.screen.Screen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.Matrix4f;
import org.lwjgl.opengl.GL11;

import java.util.function.Consumer;

public class ColorPreviewButton extends ClickableWidget {
    private final Consumer<Color> saveConsumer;
    private int color;
    private Screen parent;

    public ColorPreviewButton(int x, int y, int width, int height, Text message, int color, Consumer<Color> saveConsumer) {
        super(x, y, width, height, message);
        this.color = color;
        this.saveConsumer = saveConsumer;
    }

    public int getColor() {
        return this.color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        ColorSelectScreen screen = new ColorSelectScreen(this.color, this.saveConsumer);
        screen.setParent(this.parent);
        MinecraftClient.getInstance().openScreen(screen);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        Matrix4f matrix = matrices.peek().getModel();

        int[] color = Color.parse(this.color);

        int endX = this.x + this.width;
        int endY = this.y + this.height;
        RenderSystem.disableTexture();
        buffer.begin(GL11.GL_QUADS, VertexFormats.POSITION_COLOR);
        buffer.vertex(matrix, this.x + 2, this.y + 2, 0.0F).color(color[0], color[1], color[2], 255).next();
        buffer.vertex(matrix, this.x + 2, endY - 2, 0.0F).color(color[0], color[1], color[2], 255).next();
        buffer.vertex(matrix, endX - 2, endY - 2, 0.0F).color(color[0], color[1], color[2], 255).next();
        buffer.vertex(matrix, endX - 2, this.y + 2, 0.0F).color(color[0], color[1], color[2], 255).next();
        tessellator.draw();
        RenderSystem.enableTexture();
    }

    public void setParent(Screen parent) {
        this.parent = parent;
    }
}
