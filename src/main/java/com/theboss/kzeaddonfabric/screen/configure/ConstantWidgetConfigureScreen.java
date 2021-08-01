package com.theboss.kzeaddonfabric.screen.configure;

import com.theboss.kzeaddonfabric.Color;
import com.theboss.kzeaddonfabric.enums.Anchor;
import com.theboss.kzeaddonfabric.render.widgets.ConstantWidget;
import com.theboss.kzeaddonfabric.screen.button.ColorPreviewButton;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.util.function.Consumer;

public class ConstantWidgetConfigureScreen extends WidgetConfigureScreen {
    private Text text;
    private int color;
    private ColorPreviewButton colorPreviewButton;

    public ConstantWidgetConfigureScreen(Text text, int color, Anchor windowAnc, Anchor widgetAnc, int x, int y, Consumer<WidgetConfigureScreen> saveConsumer) {
        super(windowAnc, widgetAnc, x, y, saveConsumer);

        this.text = text;
        this.color = color;
    }

    public ConstantWidgetConfigureScreen(Text text, int color, Anchor windowAnc, Anchor widgetAnc, int x, int y) {
        super(windowAnc, widgetAnc, x, y);

        this.text = text;
        this.color = color;
    }

    public ConstantWidgetConfigureScreen(ConstantWidget widget) {
        super(widget);

        this.text = widget.getText();
        this.color = widget.getColor();

        this.additionalDataSaver = () -> {
            widget.setText(this.text);
            widget.setColor(new Color(this.color));
        };
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
