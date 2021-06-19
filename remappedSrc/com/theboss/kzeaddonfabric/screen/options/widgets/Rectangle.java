package com.theboss.kzeaddonfabric.screen.options.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import com.theboss.kzeaddonfabric.Color;
import com.theboss.kzeaddonfabric.enums.Anchor;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import org.lwjgl.opengl.GL11;

public class Rectangle extends DrawableHelper {
    private Anchor anchor;
    private int x1;
    private int x2;
    private int y1;
    private int y2;
    private int lineColor;
    private int[] fillColor;

    /**
     * @param anchor    Rectangle Anchor
     * @param x         X position
     * @param y         Y position
     * @param width     Rectangle width
     * @param height    Rectangle height
     * @param lineColor 0xAARRGGBB
     * @param fillColor 0xAARRGGBB
     */
    public Rectangle(Anchor anchor, int x, int y, int width, int height, int lineColor, int fillColor) {
        this.anchor = anchor;
        this.x1 = x;
        this.x2 = this.x1 + width;
        this.y1 = y;
        this.y2 = this.y1 + height;
        this.lineColor = lineColor;
        this.fillColor = Color.parseWithAlpha(fillColor);
    }

    protected int translateX(int x) {
        return (int) (x - this.getWidth() * this.anchor.getXFactor());
    }

    protected int translateY(int y) {
        return (int) (y - this.getHeight() * this.anchor.getYFactor());
    }

    public void render(MatrixStack matrices) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        buffer.begin(GL11.GL_QUADS, VertexFormats.POSITION_COLOR);
        buffer.vertex(this.x1, this.y1, 0).color(this.fillColor[1], this.fillColor[2], this.fillColor[3], this.fillColor[0]).next();
        buffer.vertex(this.x1, this.y2, 0).color(this.fillColor[1], this.fillColor[2], this.fillColor[3], this.fillColor[0]).next();
        buffer.vertex(this.x2, this.y2, 0).color(this.fillColor[1], this.fillColor[2], this.fillColor[3], this.fillColor[0]).next();
        buffer.vertex(this.x2, this.y1, 0).color(this.fillColor[1], this.fillColor[2], this.fillColor[3], this.fillColor[0]).next();
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        tessellator.draw();
        RenderSystem.disableBlend();
        RenderSystem.enableTexture();

        this.drawHorizontalLine(matrices, this.x1, this.x2, this.y1, this.lineColor);
        this.drawHorizontalLine(matrices, this.x1, this.x2, this.y2, this.lineColor);
        this.drawVerticalLine(matrices, this.x1, this.y1, this.y2, this.lineColor);
        this.drawVerticalLine(matrices, this.x2, this.y1, this.y2, this.lineColor);
    }

    public Anchor getAnchor() {
        return this.anchor;
    }

    public void setAnchor(Anchor anchor) {
        this.anchor = anchor;
    }

    public int getX() {
        return this.x1;
    }

    public int getY() {
        return this.y1;
    }

    public int getWidth() {
        return this.x2 - this.x1;
    }

    public int getHeight() {
        return this.y2 - this.y1;
    }

    public void setX(int x) {
        this.x1 = x;
    }

    public void setY(int y) {
        this.y1 = y;
    }

    public void setWidth(int width) {
        this.x2 = this.x1 + width;
    }

    public void setHeight(int height) {
        this.y2 = this.y1 + height;
    }

    public int getLineColor() {
        return this.lineColor;
    }

    public void setLineColor(int lineColor) {
        this.lineColor = lineColor;
    }

    public int getFillColor() {
        return Color.parseWithAlpha(this.fillColor[0], this.fillColor[1], this.fillColor[2], this.fillColor[3]);
    }

    public void setFillColor(int fillColor) {
        this.fillColor = Color.parseWithAlpha(fillColor);
    }
}
