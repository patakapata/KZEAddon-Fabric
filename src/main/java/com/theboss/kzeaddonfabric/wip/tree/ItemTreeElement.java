package com.theboss.kzeaddonfabric.wip.tree;

import com.mojang.blaze3d.systems.RenderSystem;
import com.theboss.kzeaddonfabric.KZEAddon;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Matrix4f;
import org.lwjgl.opengl.GL11;

public class ItemTreeElement extends AbstractTreeElement {
    private static final Identifier FRAME = new Identifier(KZEAddon.MOD_ID, "textures/gui/frame.png");

    private ItemStack item;

    public ItemTreeElement(MinecraftClient mc, ItemConvertible item) {
        super(mc);

        this.item = new ItemStack(item);
    }

    public ItemTreeElement(MinecraftClient mc, ItemStack item) {
        super(mc);

        this.item = item;
    }

    @Override
    public void render(MatrixStack matrices, float x, float y, float delta) {
        this.renderBackground(matrices, x, y);
        this.renderItem((int) x, (int) y);
    }

    private void renderBackground(MatrixStack matrices, float x, float y) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        this.texManager.bindTexture(FRAME);
        Matrix4f matrix = matrices.peek().getModel();

        float size = 10;

        RenderSystem.enableBlend();
        buffer.begin(GL11.GL_QUADS, VertexFormats.POSITION_TEXTURE);
        buffer.vertex(matrix, x - size, y - size, 0.0F).texture(0F, 0F).next();
        buffer.vertex(matrix, x - size, y + size, 0.0F).texture(0F, 1F).next();
        buffer.vertex(matrix, x + size, y + size, 0.0F).texture(1F, 1F).next();
        buffer.vertex(matrix, x + size, y - size, 0.0F).texture(1F, 0F).next();
        tessellator.draw();
        RenderSystem.disableBlend();
    }

    private void renderItem(int x, int y) {
        this.itemRenderer.renderGuiItemIcon(this.item, x - 8, y - 8);
        this.itemRenderer.renderGuiItemOverlay(this.textRenderer, this.item, x - 8, y - 8);
    }

    public ItemStack getItem() {
        return this.item;
    }

    public void setItem(ItemStack item) {
        this.item = item;
    }
}
