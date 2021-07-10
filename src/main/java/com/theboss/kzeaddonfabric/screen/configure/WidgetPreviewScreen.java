package com.theboss.kzeaddonfabric.screen.configure;

import com.theboss.kzeaddonfabric.enums.Anchor;
import com.theboss.kzeaddonfabric.screen.Screen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.util.function.Consumer;

public class WidgetPreviewScreen extends Screen {
    private final MinecraftClient client;
    private final Tessellator tessellator;
    private final BufferBuilder buffer;
    private final TextRenderer textRenderer;

    private final Consumer<WidgetPreviewScreen> saveConsumer;
    private String sampleText;
    private int x;
    private int y;
    private float scale;
    private Anchor windowAnchor;
    private Anchor widgetAnchor;

    public WidgetPreviewScreen(int x, int y, float scale, Anchor windowAnchor, Anchor widgetAnchor, Consumer<WidgetPreviewScreen> saveConsumer) {
        super(Text.of("Widget Preview Screen"));
        this.x = x;
        this.y = y;
        this.scale = scale;
        this.windowAnchor = windowAnchor;
        this.widgetAnchor = widgetAnchor;
        this.saveConsumer = saveConsumer;
        this.sampleText = "Sample";

        this.client = MinecraftClient.getInstance();
        this.tessellator = Tessellator.getInstance();
        this.buffer = this.tessellator.getBuffer();
        this.textRenderer = this.client.textRenderer;
    }

    public void close(boolean shouldSave) {
        if (shouldSave) this.saveConsumer.accept(this);

        this.onClose();
    }

    public String getSampleText() {
        return this.sampleText;
    }

    public void setSampleText(String sampleText) {
        this.sampleText = sampleText;
    }

    public float getScale() {
        return this.scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public float getScaledHeight() {
        return this.getTextHeight() * this.scale;
    }

    public float getScaledTextWidth() {
        return this.getTextWidth() * this.scale;
    }

    public int getTextHeight() {
        return this.textRenderer.fontHeight;
    }

    public int getTextWidth() {
        return this.textRenderer.getWidth(this.sampleText);
    }

    public Anchor getWidgetAnchor() {
        return this.widgetAnchor;
    }

    public void setWidgetAnchor(Anchor widgetAnchor) {
        this.widgetAnchor = widgetAnchor;
    }

    public Anchor getWindowAnchor() {
        return this.windowAnchor;
    }

    public void setWindowAnchor(Anchor windowAnchor) {
        this.windowAnchor = windowAnchor;
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

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);
    }
}
