package com.theboss.kzeaddonfabric.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;

import java.util.ArrayList;
import java.util.List;

public class WidgetEditor {
    private final List<Widget> widgets;
    private Widget target;

    public WidgetEditor() {
        this(new ArrayList<>());
    }

    public WidgetEditor(List<Widget> widgets) {
        this.widgets = widgets;
        this.target = null;
    }

    public void setTarget(Widget target) {
        if (!this.widgets.contains(target)) throw new IllegalArgumentException("Invalid widget!");
        if (this.target != null) this.widgets.add(this.target);
        this.widgets.remove(this.target);
        this.target = target;
    }

    public void render(MatrixStack matrices) {
        MinecraftClient client = MinecraftClient.getInstance();
        Window window = client.getWindow();
        TextRenderer textRenderer = client.textRenderer;

        for (Widget widget : this.widgets) {
            widget.render(matrices, window, textRenderer);
        }
    }
}
