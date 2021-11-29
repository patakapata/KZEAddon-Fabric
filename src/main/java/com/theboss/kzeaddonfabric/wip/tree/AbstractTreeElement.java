package com.theboss.kzeaddonfabric.wip.tree;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Matrix4f;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class AbstractTreeElement {
    protected final TextureManager texManager;
    protected final ItemRenderer itemRenderer;
    protected final TextRenderer textRenderer;
    protected final List<AbstractTreeElement> children;

    public AbstractTreeElement(MinecraftClient mc) {
        this(mc, new ArrayList<>());
    }

    public AbstractTreeElement(MinecraftClient mc, List<AbstractTreeElement> children) {
        this.texManager = mc.getTextureManager();
        this.itemRenderer = mc.getItemRenderer();
        this.textRenderer = mc.textRenderer;

        this.children = children;
    }

    public void addChild(AbstractTreeElement child) {
        this.children.add(child);
    }

    public void addChildren(AbstractTreeElement... child) {
        this.children.addAll(Arrays.asList(child));
    }

    public void removeChild(AbstractTreeElement child) {
        this.children.remove(child);
    }

    public void clearChildren() {
        this.children.clear();
    }

    public AbstractTreeElement getChild(int index) {
        return this.children.get(index);
    }

    public List<AbstractTreeElement> getChildren() {
        return new ArrayList<>(this.children);
    }

    public int getVerticalMarginNumber() {
        int childrenNum = this.children.size();

        return childrenNum <= 0 ? 0 : childrenNum - 1;
    }

    public float getHeight(float verticalMargin) {
        float height = this.getVerticalMarginNumber() * verticalMargin;

        for (AbstractTreeElement element : this.children) {
            height += element.getHeight(verticalMargin);
        }

        return height;
    }

    public float getChildVerticalOffset(int index, float verticalMargin) {
        return (this.getVerticalMarginNumber() * verticalMargin / -2) + (index * verticalMargin);
    }

    public abstract void render(MatrixStack matrices, float x, float y, float delta);

    public void renderChildren(MatrixStack matrices, float x, float y, float delta) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        Matrix4f matrix = matrices.peek().getModel();
        float verticalMargin = 21;
        float vOffset = this.getChildVerticalOffset(0, verticalMargin);
        float hOffset = 35;

        if (!this.children.isEmpty()) {
            buffer.begin(GL11.GL_LINES, VertexFormats.POSITION);
            buffer.vertex(matrix, x, y, 0).next();
            buffer.vertex(matrix, x + hOffset / 2, y, 0).next();
            RenderSystem.disableTexture();
            buffer.vertex(matrix, x + hOffset / 2, y + vOffset, 0).next();
            buffer.vertex(matrix, x + hOffset / 2, y + this.getChildVerticalOffset(this.children.size() - 1, verticalMargin), 0).next();
            tessellator.draw();
            RenderSystem.enableTexture();
        }

        for (AbstractTreeElement child : this.children) {
            RenderSystem.disableTexture();
            buffer.begin(GL11.GL_LINES, VertexFormats.POSITION);
            buffer.vertex(matrix, x + hOffset / 2, y + vOffset, 0).next();
            buffer.vertex(matrix, x + hOffset, y + vOffset, 0).next();
            tessellator.draw();
            RenderSystem.enableTexture();

            child.renderChildren(matrices, x + hOffset, y + vOffset, delta);
            child.render(matrices, x + hOffset, y + vOffset, delta);

            vOffset += verticalMargin;
        }
    }
}
