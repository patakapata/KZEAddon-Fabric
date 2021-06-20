package com.theboss.kzeaddonfabric.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.theboss.kzeaddonfabric.Color;
import com.theboss.kzeaddonfabric.screen.button.SliderWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import org.lwjgl.opengl.GL11;

import java.util.function.Consumer;

public class ColorSelectScreen extends Screen {
    public static final Identifier PREVIEW_FRAME = new Identifier("kzeaddon-fabric", "textures/gui/option/colorselector_frame.png");
    private final Consumer<Color> saveConsumer;
    private int cX;
    private int cY;

    private int color;
    private SliderWidget red;
    private SliderWidget green;
    private SliderWidget blue;

    public ColorSelectScreen(int color, Consumer<Color> saveConsumer) {
        super(new LiteralText("ColorSelectScreen"));
        this.color = color;
        this.saveConsumer = saveConsumer;
    }

    public int getColor() {
        return (this.red.getAmount() << 16) | (this.green.getAmount() << 8) | this.blue.getAmount();
    }

    public void setColor(int color) {
        double red = (color >> 16 & 0xFF) / 255.0;
        double green = (color >> 8 & 0xFF) / 255.0;
        double blue = (color & 0xFF) / 255.0;

        this.red.setValue(red);
        this.green.setValue(green);
        this.blue.setValue(blue);
    }

    @Override
    protected void init() {
        this.cX = this.width / 2;
        this.cY = this.height / 2;

        this.red = new SliderWidget(this.cX - 49, this.cY - 35, 98, 20, new LiteralText(""), 0.0, 0, 255, this::onColorUpdate);
        this.green = new SliderWidget(this.cX - 49, this.cY - 10, 98, 20, new LiteralText(""), 0.0, 0, 255, this::onColorUpdate);
        this.blue = new SliderWidget(this.cX - 49, this.cY + 15, 98, 20, new LiteralText(""), 0.0, 0, 255, this::onColorUpdate);

        this.red.setColorSupplier((aDouble, aBoolean) -> {
            if (aBoolean) { return this.red.getAmount() << 16; } else { return 10526880; }
        });
        this.green.setColorSupplier((aDouble, aBoolean) -> {
            if (aBoolean) { return this.green.getAmount() << 8; } else { return 10526880; }
        });
        this.blue.setColorSupplier((aDouble, aBoolean) -> {
            if (aBoolean) { return this.blue.getAmount(); } else { return 10526880; }
        });

        this.setColor(this.color);

        this.addButton(this.red);
        this.addButton(this.green);
        this.addButton(this.blue);
        this.addButton(new ButtonWidget(this.cX - 49, this.height - 30, 98, 20, new TranslatableText("menu.kzeaddon.option.close"), btn -> this.onClose()));
    }

    @Override
    public void init(MinecraftClient client, int width, int height) {
        super.init(client, width, height);
    }

    public boolean isMouseOverPreview(int mouseX, int mouseY) {
        int up = this.cY - 35;
        int down = up + 70;
        int left = this.cX + 54;
        int right = left + 20;

        return ((mouseX >= left && mouseX <= right) && (mouseY >= up && mouseY <= down));
    }

    @Override
    public void onClose() {
        this.saveConsumer.accept(new Color(this.getColor()));
        super.onClose();
    }

    public void onColorUpdate() {
        this.color = this.getColor();
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);

        int up = this.cY - 35;
        int down = up + 70;
        int left = this.cX + 54;
        int right = left + 20;
        boolean isMouseOverPreview = this.isMouseOverPreview(mouseX, mouseY);

        this.client.getTextureManager().bindTexture(PREVIEW_FRAME);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        int red = this.color >> 16 & 0xFF;
        int green = this.color >> 8 & 0xFF;
        int blue = this.color & 0xFF;

        RenderSystem.disableTexture();
        buffer.begin(GL11.GL_QUADS, VertexFormats.POSITION_COLOR);
        buffer.vertex(left, up, 0).color(red, green, blue, 255).next();
        buffer.vertex(left, down, 0).color(red, green, blue, 255).next();
        buffer.vertex(right, down, 0).color(red, green, blue, 255).next();
        buffer.vertex(right, up, 0).color(red, green, blue, 255).next();
        tessellator.draw();
        RenderSystem.enableTexture();

        drawTexture(matrices, left, up, isMouseOverPreview ? 20 : 0, 0, 20, 70, 64, 128);
    }
}
