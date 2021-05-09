package com.theboss.kzeaddonfabric.screen.button;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.Element;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import org.lwjgl.opengl.GL11;

import java.util.function.Consumer;

public class AnchorSelectButton extends DrawableHelper implements Element, Drawable {
    private static final Identifier TEXTURE = new Identifier("kzeaddon-fabric", "textures/gui/option/anchor_select.png");
    private int x;
    private int y;
    private int width;
    private int height;
    private Consumer<AnchorSelectButton> onClick;

    public AnchorSelectButton(int x, int y, int width, int height) {
        this(x, y, width, height, btn -> {});
    }

    public AnchorSelectButton(int x, int y, int width, int height, Consumer<AnchorSelectButton> onClick) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.onClick = onClick;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        float v = this.isMouseOver(mouseX, mouseY) ? 0.5F : 0F;

        // MinecraftClient.getInstance().getTextureManager().bindTexture(AnchorSelectButton.TEXTURE);
        buffer.begin(GL11.GL_QUADS, VertexFormats.POSITION);
        buffer.vertex(this.x, this.y, 0).next();
        buffer.vertex(this.x, this.y + this.height, 0).next();
        buffer.vertex(this.x + this.width, this.y + this.height, 0).next();
        buffer.vertex(this.x + this.width, this.y, 0).next();
        tessellator.draw();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.isMouseOver(mouseX, mouseY)) {
            this.onClick.accept(this);
            MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            return true;
        }
        return false;
    }

    public int getX() {
        return this.x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return this.y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getWidth() {
        return this.width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return this.height;
    }

    public void setHeight(int height) {
        this.height = height;
    }


    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return (mouseX >= this.x && mouseX <= this.x + this.width) && (mouseY >= this.y && mouseY <= this.y + this.height);
    }
}
