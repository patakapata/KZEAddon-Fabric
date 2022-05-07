package com.theboss.kzeaddonfabric.screen;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import com.theboss.kzeaddonfabric.KZEAddon;
import com.theboss.kzeaddonfabric.enums.Anchor;
import com.theboss.kzeaddonfabric.widgets.Offset;
import com.theboss.kzeaddonfabric.widgets.api.Widget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix4f;
import org.lwjgl.glfw.GLFW;

import static com.theboss.kzeaddonfabric.render.Constants.*;

public class WidgetEditScreen extends Screen {
    private final Widget widget;
    private float scale;
    private Offset offset;
    private Anchor anchor;

    private float widHeight;
    private float widWidth;

    private boolean isDragging;
    private float dragOffsetX;
    private float dragOffsetY;

    public static WidgetEditScreen create(Widget widget) {
        return create(widget, null);
    }

    public static WidgetEditScreen create(Widget widget, Object parent) {
        WidgetEditScreen screen = new WidgetEditScreen(widget);
        screen.setParent(parent);

        return screen;
    }

    private WidgetEditScreen(Widget widget) {
        super(Text.of("Widget arrangement screen"));

        this.widget = widget;
        this.offset = widget.getOffset().copy();
        this.anchor = this.widget.getAnchor();
        this.scale = this.widget.getScale();
    }

    @Override
    protected void init() {
        this.widWidth = this.widget.getWidth();
        this.widHeight = this.textRenderer.fontHeight;
    }

    @Override
    public void onClose() {
        this.widget.setOffset(this.offset);
        this.widget.setAnchor(this.anchor);
        this.widget.setScale(this.scale);
        super.onClose();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == GLFW_MOUSE_BUTTON_LEFT) {
            if (this.isMouseOverWidget(mouseX, mouseY)) {
                this.isDragging = true;
                this.dragOffsetX = (this.widWidth * this.offset.anchor.getX() + this.offset.x) - (float) mouseX;
                this.dragOffsetY = (this.widHeight * this.offset.anchor.getY() + this.offset.y) - (float) mouseY;
            }
            return true;
        } else if (button == GLFW_MOUSE_BUTTON_RIGHT) {
            this.offset.x = 0;
            this.offset.y = 0;
            this.scale = 1.0F;
            return true;
        } else if (button == GLFW_MOUSE_BUTTON_MIDDLE) {
            this.migrateAnchor(this.offset.anchor.next());
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    public void migrateAnchor(Anchor newAnchor) {
        KZEAddon.LOGGER.info("WidgetEditScreen > Migrate anchor [ " + this.offset.anchor + " -> " + newAnchor + " ]");

        float x = newAnchor.getX() - this.offset.anchor.getX();
        float y = newAnchor.getY() - this.offset.anchor.getY();

        this.offset.anchor = newAnchor;
        this.offset.x = (-this.width * x) + this.offset.x;
        this.offset.y = (-this.height * y) + this.offset.y;
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        if (this.isDragging) {
            this.offset.x = ((float) mouseX - this.widWidth * this.offset.anchor.getX()) + (this.dragOffsetX);
            this.offset.y = ((float) mouseY - this.widHeight * this.offset.anchor.getY()) + (this.dragOffsetY);
        }
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == GLFW_MOUSE_BUTTON_LEFT) {
            this.isDragging = false;
            this.dragOffsetX = 0;
            this.dragOffsetY = 0;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    public boolean isMouseOverWidget(double mouseX, double mouseY) {
        Window window = MinecraftClient.getInstance().getWindow();
        float x = window.getScaledWidth() * this.offset.anchor.getX() + this.offset.x - this.widWidth * this.anchor.getX();
        float y = window.getScaledHeight() * this.offset.anchor.getY() + this.offset.y - this.widHeight * this.anchor.getY();

        return x <= mouseX && mouseX <= x + this.widWidth && y <= mouseY && mouseY <= y + this.widHeight;
    }

    public Text getName() {
        return this.widget.getName();
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);

        MinecraftClient mc = MinecraftClient.getInstance();
        Window window = mc.getWindow();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        this.widWidth = this.widget.getWidth();
        this.widHeight = this.widget.getHeight();

        float windowWidth = window.getScaledWidth();
        float windowHeight = window.getScaledHeight();
        float x = windowWidth * this.offset.anchor.getX() + this.offset.x;
        float y = windowHeight * this.offset.anchor.getY() + this.offset.y;

        RenderSystem.disableTexture();
        Matrix4f matrix = matrices.peek().getModel();
        buffer.begin(GL_LINES, VertexFormats.POSITION_COLOR);
        buffer.vertex(matrix, 0, y, 0).color(1F, 0F, 0F, 1F).next();
        buffer.vertex(matrix, windowWidth, y, 0).color(1F, 0F, 0F, 1F).next();
        buffer.vertex(matrix, x, 0, 0).color(0F, 1F, 0F, 1F).next();
        buffer.vertex(matrix, x, windowHeight, 0).color(0F, 1F, 0F, 1F).next();
        tessellator.draw();
        RenderSystem.enableTexture();

        matrices.push();
        this.offset.apply(matrices, window);
        matrices.scale(this.scale, this.scale, this.scale);
        matrices.translate(-this.widWidth * this.anchor.getX(), -this.widHeight * this.anchor.getY(), 0);
        this.widget.render(matrices, delta);
        matrix = matrices.peek().getModel();
        RenderSystem.disableTexture();
        buffer.begin(GL_LINE_LOOP, VertexFormats.POSITION);
        buffer.vertex(matrix, 0, 0, 0).next();
        buffer.vertex(matrix, 0, this.widHeight, 0).next();
        buffer.vertex(matrix, this.widWidth, this.widHeight, 0).next();
        buffer.vertex(matrix, this.widWidth, 0, 0).next();
        tessellator.draw();
        if (this.isMouseOverWidget(mouseX, mouseY)) {
            RenderSystem.enableBlend();
            RenderSystem.color4f(1F, 1F, 1F, 0.5F);
            buffer.begin(GL_QUADS, VertexFormats.POSITION);
            buffer.vertex(matrix, 0, 0, 0).next();
            buffer.vertex(matrix, 0, this.widHeight, 0).next();
            buffer.vertex(matrix, this.widWidth, this.widHeight, 0).next();
            buffer.vertex(matrix, this.widWidth, 0, 0).next();
            tessellator.draw();
            RenderSystem.color4f(1F, 1F, 1F, 1F);
            RenderSystem.disableBlend();
        }
        RenderSystem.enableTexture();
        matrices.pop();

        this.renderTooltip(matrices, ImmutableList.of(
                Text.of(String.format("Pos | %.2fx%.2f", this.offset.x, this.offset.y)),
                Text.of(String.format("Res | %dx%d", this.width, this.height))
        ), MathHelper.floor(mouseX), MathHelper.floor(mouseY));
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        this.scale -= amount / 2;
        return super.mouseScrolled(mouseX, mouseY, amount);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        float amount = 1.0F;
        if ((modifiers & GLFW.GLFW_MOD_SHIFT) == 0b1) {
            amount *= 10;
        }
        if ((modifiers & GLFW.GLFW_MOD_CONTROL) >> 1 == 0b1) {
            amount /= 10;
        }

        switch (keyCode) {
            case GLFW.GLFW_KEY_UP:
                this.offset.y -= amount;
                break;
            case GLFW.GLFW_KEY_LEFT:
                this.offset.x -= amount;
                break;
            case GLFW.GLFW_KEY_RIGHT:
                this.offset.x += amount;
                break;
            case GLFW.GLFW_KEY_DOWN:
                this.offset.y += amount;
                break;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
