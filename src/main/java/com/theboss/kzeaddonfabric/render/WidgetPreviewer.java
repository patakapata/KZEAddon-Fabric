package com.theboss.kzeaddonfabric.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;

import java.util.ArrayList;
import java.util.List;

public class WidgetPreviewer {
    private final List<Widget> widgets;

    public WidgetPreviewer() {
        this(new ArrayList<>());
    }

    public WidgetPreviewer(WidgetPreviewer previewer) {
        this(previewer.widgets);
    }

    public WidgetPreviewer(List<Widget> widgets) {
        this.widgets = widgets;
    }

    public List<Widget> getWidgets() {
        return this.widgets;
    }

    public void addWidget(Widget widget) {
        this.widgets.add(widget);
    }

    public void render(MatrixStack matrices) {
        MinecraftClient client = MinecraftClient.getInstance();
        Window window = client.getWindow();
        TextRenderer textRenderer = client.textRenderer;
        for (Widget widget : widgets) {
            widget.render(matrices, window, textRenderer);
        }
    }
}
