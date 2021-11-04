package com.theboss.kzeaddonfabric.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.theboss.kzeaddonfabric.enums.Anchor;
import com.theboss.kzeaddonfabric.render.widgets.Widget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

public class WidgetArrangementScreen extends Screen {
    private final Widget widget;
    private float scale;
    private float x;
    private float y;
    private float width;
    private float height;
    private Anchor windowAnchor;
    private Anchor elementAnchor;

    private boolean isDragging;
    private float dragOffsetX;
    private float dragOffsetY;

    public WidgetArrangementScreen(Widget widget) {
        super(Text.of("Widget arrangement screen"));

        this.widget = widget;
        this.scale = this.widget.getScale();
        this.x = this.widget.getX();
        this.y = this.widget.getY();
        this.windowAnchor = this.widget.getWindowAnchor();
        this.elementAnchor = this.widget.getElementAnchor();

        this.isDragging = false;
        this.dragOffsetX = 0.0F;
        this.dragOffsetY = 0.0F;
    }

    public Text getText() {
        return this.widget.getText();
    }

    @Override
    protected void init() {
        this.width = this.widget.getWidth(this.textRenderer);
        this.height = this.textRenderer.fontHeight;
    }

    public boolean isMouseOverWidget(double mouseX, double mouseY) {
        float x = this.getAbsoluteX();
        float y = this.getAbsoluteY();

        return (mouseX >= x && mouseX <= x + this.getWidth()) && (mouseY >= y && mouseY <= y + this.getHeight());
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            if (this.isMouseOverWidget(mouseX, mouseY)) {
                this.isDragging = true;
                this.dragOffsetX = (float) (mouseX - this.getAbsoluteCenterX());
                this.dragOffsetY = (float) (mouseY - this.getAbsoluteCenterY());

                return true;
            }
        } else if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            this.x = 0;
            this.y = 0;
            return true;
        } else if (button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
            AnchorSelectScreen screen = new AnchorSelectScreen(this.windowAnchor, this.elementAnchor, scr -> {
                this.windowAnchor = scr.getWindowAnchor();
                this.elementAnchor = scr.getElementAnchor();
            });
            screen.setParent(this);
            screen.open(MinecraftClient.getInstance());
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (this.isDragging) {
            Window window = MinecraftClient.getInstance().getWindow();
            this.x = (float) (mouseX - this.dragOffsetX - window.getScaledWidth() * this.windowAnchor.getXFactor());
            this.y = (float) (mouseY - this.dragOffsetY - window.getScaledHeight() * this.windowAnchor.getYFactor());
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        this.isDragging = false;
        this.dragOffsetX = 0;
        this.dragOffsetY = 0;
        return true;
    }

    protected float getAbsoluteX() {
        return MinecraftClient.getInstance().getWindow().getScaledWidth() * this.windowAnchor.getXFactor() + this.x - (this.width * this.scale) * this.elementAnchor.getXFactor();
    }

    protected float getAbsoluteY() {
        return MinecraftClient.getInstance().getWindow().getScaledHeight() * this.windowAnchor.getYFactor() + this.y - (this.textRenderer.fontHeight * this.scale) * this.elementAnchor.getYFactor();
    }

    protected float getAbsoluteCenterX() {
        return MinecraftClient.getInstance().getWindow().getScaledWidth() * this.windowAnchor.getXFactor() + this.x;
    }

    protected float getAbsoluteCenterY() {
        return MinecraftClient.getInstance().getWindow().getScaledHeight() * this.windowAnchor.getYFactor() + this.y;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);

        MinecraftClient mc = MinecraftClient.getInstance();
        Window window = mc.getWindow();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        int scaledWidth = window.getScaledWidth();
        int scaledHeight = window.getScaledHeight();

        matrices.push();
        matrices.translate(scaledWidth * this.windowAnchor.getXFactor() + this.x, scaledHeight * this.windowAnchor.getYFactor() + this.y, 0);
        matrices.scale(this.scale, this.scale, this.scale);
        this.textRenderer.drawWithShadow(
                matrices,
                this.getText(),
                -(this.textRenderer.getWidth(this.getText()) * this.elementAnchor.getXFactor()),
                -(this.textRenderer.fontHeight * this.elementAnchor.getYFactor()),
                this.widget.getColor() | this.widget.getAlpha() << 24
        );
        matrices.pop();

        float elementWidth = this.width * this.scale;
        float elementHeight = this.height * this.scale;
        float minX = this.getAbsoluteX();
        float minY = this.getAbsoluteY();

        // this.widget.render(scaledWidth, scaledHeight, this.textRenderer, matrices, delta);
        buffer.begin(GL11.GL_LINES, VertexFormats.POSITION_COLOR);
        // -------------------------------------------------- //
        // Widget rectangle
        buffer.vertex(minX, minY, 0).color(1F, 1F, 1F, 1F).next();
        buffer.vertex(minX, minY + elementHeight, 0).color(1F, 1F, 1F, 1F).next();
        buffer.vertex(minX, minY + elementHeight, 0).color(1F, 1F, 1F, 1F).next();
        buffer.vertex(minX + elementWidth, minY + elementHeight, 0).color(1F, 1F, 1F, 1F).next();
        buffer.vertex(minX + elementWidth, minY + elementHeight, 0).color(1F, 1F, 1F, 1F).next();
        buffer.vertex(minX + elementWidth, minY, 0).color(1F, 1F, 1F, 1F).next();
        buffer.vertex(minX + elementWidth, minY, 0).color(1F, 1F, 1F, 1F).next();
        buffer.vertex(minX, minY, 0).color(1F, 1F, 1F, 1F).next();
        // -------------------------------------------------- //
        // Axis line
        float axisX = this.getAbsoluteCenterX();
        float axisY = this.getAbsoluteCenterY();
        buffer.vertex(0, axisY, 0).color(1F, 0F, 0F, 1F).next();
        buffer.vertex(scaledWidth, axisY, 0).color(1F, 0F, 0F, 1F).next();
        buffer.vertex(axisX, 0, 0).color(0F, 1F, 0F, 1F).next();
        buffer.vertex(axisX, scaledHeight, 0).color(0F, 1F, 0F, 1F).next();

        RenderSystem.disableTexture();
        tessellator.draw();
        RenderSystem.enableTexture();

        matrices.push();
        matrices.translate(mouseX, mouseY, 0);
        this.renderTooltip(matrices, Text.of(String.format("§%s%.2f x %.2f§r", this.isMouseOverWidget(mouseX, mouseY) ? "a" : "c", this.x, this.y)), 0, 0);
        matrices.pop();
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        float multiplier = 1.0F;
        if ((modifiers & GLFW.GLFW_MOD_SHIFT) == GLFW.GLFW_MOD_SHIFT) {
            multiplier *= 10;
        }
        if ((modifiers & GLFW.GLFW_MOD_CONTROL) == GLFW.GLFW_MOD_CONTROL) {
            multiplier /= 10;
        }
        switch (keyCode) {
            case GLFW.GLFW_KEY_UP:
                this.y -= multiplier;
                break;
            case GLFW.GLFW_KEY_LEFT:
                this.x -= multiplier;
                break;
            case GLFW.GLFW_KEY_DOWN:
                this.y += multiplier;
                break;
            case GLFW.GLFW_KEY_RIGHT:
                this.x += multiplier;
                break;
            default:
                return false;
        }
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        this.scale += amount / 10;
        if (this.scale < 0) this.scale = 0;
        else if (this.scale > 100) this.scale = 100;
        return true;
    }

    public float getScale() {
        return this.scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public float getX() {
        return this.x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return this.y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getWidth() {
        return this.width * this.scale;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getHeight() {
        return this.height * this.scale;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    @Override
    public void onClose() {
        this.widget.setX(this.x);
        this.widget.setY(this.y);
        this.widget.setScale(this.scale);
        this.widget.setWindowAnchor(this.windowAnchor);
        this.widget.setElementAnchor(this.elementAnchor);
        super.onClose();
    }
}
