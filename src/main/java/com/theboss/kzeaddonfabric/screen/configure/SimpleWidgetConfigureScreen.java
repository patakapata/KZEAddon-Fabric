package com.theboss.kzeaddonfabric.screen.configure;

import com.theboss.kzeaddonfabric.Color;
import com.theboss.kzeaddonfabric.enums.Anchor;
import com.theboss.kzeaddonfabric.render.widgets.Widget;
import com.theboss.kzeaddonfabric.screen.button.ColorPreviewButton;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.util.function.Consumer;

public class SimpleWidgetConfigureScreen extends WidgetConfigureScreen {
    private int color;
    private ColorPreviewButton colorPreviewButton;

    public SimpleWidgetConfigureScreen(Anchor windowAnc, Anchor widgetAnc, int x, int y, int color, Consumer<WidgetConfigureScreen> saveConsumer, Runnable additionalDataSaver) {
        super(windowAnc, widgetAnc, x, y, saveConsumer);
        this.color = color;
        this.additionalDataSaver = additionalDataSaver;
    }

    public SimpleWidgetConfigureScreen(Anchor windowAnc, Anchor widgetAnc, int color, int x, int y) {
        super(windowAnc, widgetAnc, x, y);
        this.color = color;
    }

    public SimpleWidgetConfigureScreen(Widget widget) {
        super(widget);
        this.color = widget.getColor() & 0xFFFFFF;
        this.additionalDataSaver = () -> widget.setColor(new Color(this.color));
    }

    @Override
    protected void init() {
        super.init();


        this.colorPreviewButton = new ColorPreviewButton(this.cX + 87, this.cY - 68, 20, 20, Text.of(null), this.color, color -> {
            this.colorPreviewButton.setColor(color.get());
            this.color = color.get();
        });
        this.colorPreviewButton.setParent(this);

        this.addButton(this.colorPreviewButton);
    }

    @Override
    public void renderCustomBackground(MatrixStack matrices, int mouseX, int mouseY) {
        super.renderCustomBackground(matrices, mouseX, mouseY);

        MinecraftClient.getInstance().getTextureManager().bindTexture(TEXTURE);
        this.drawTexture(matrices, this.cX + 17, this.cY - 73, 0, 209, 95, 30);
    }
}
