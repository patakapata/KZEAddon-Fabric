package com.theboss.kzeaddonfabric.screen.button;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.util.function.BiFunction;

public class SliderWidget extends net.minecraft.client.gui.widget.SliderWidget {
    private BiFunction<Double, Boolean, Integer> colorSupplier = (value, active) -> active ? 16777215 : 10526880;
    private final Runnable saveRunnable;
    private final int min;
    private final int max;

    public SliderWidget(int x, int y, int width, int height, Text text, double value, int min, int max, Runnable saveRunnable) {
        super(x, y, width, height, text, value);
        this.min = min;
        this.max = max;
        this.saveRunnable = saveRunnable;
        this.updateMessage();
    }

    public int getAmount() {
        int diff = this.max - this.min;
        return (int) (this.min + diff * this.value);
    }

    public void setValue(double value) {
        this.value = value;
        this.updateMessage();
    }

    @Override
    protected void updateMessage() {
        this.setMessage(new LiteralText("" + this.getAmount()));
    }

    @Override
    protected void applyValue() {
        this.saveRunnable.run();
    }

    @Override
    public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        TextRenderer textRenderer = minecraftClient.textRenderer;
        int i = this.getYImage(this.isHovered());

        minecraftClient.getTextureManager().bindTexture(WIDGETS_TEXTURE);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, this.alpha);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();

        this.drawTexture(matrices, this.x, this.y, 0, 46 + i * 20, this.width / 2, this.height);
        this.drawTexture(matrices, this.x + this.width / 2, this.y, 200 - this.width / 2, 46 + i * 20, this.width / 2, this.height);
        this.renderBackground(matrices, minecraftClient, mouseX, mouseY);

        int j = this.getMessageColor();
        drawCenteredText(matrices, textRenderer, this.getMessage(), this.x + this.width / 2, this.y + (this.height - 8) / 2, j | MathHelper.ceil(this.alpha * 255.0F) << 24);
    }

    public void setColorSupplier(BiFunction<Double, Boolean, Integer> function) {
        this.colorSupplier = function;
    }

    protected int getMessageColor() {
        return this.colorSupplier.apply(this.value, this.active);
    }
}
