package com.theboss.kzeaddonfabric.screen.configure;

import com.theboss.kzeaddonfabric.Color;
import com.theboss.kzeaddonfabric.enums.Anchor;
import com.theboss.kzeaddonfabric.render.widgets.GunAmmoWidget;
import com.theboss.kzeaddonfabric.screen.button.ColorPreviewButton;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.util.function.Consumer;

public class GunAmmoWidgetConfigureScreen extends WidgetConfigureScreen {
    private int fullColor;
    private int lowColor;
    private int emptyColor;

    private ColorPreviewButton fullPreview;
    private ColorPreviewButton lowPreview;
    private ColorPreviewButton emptyPreview;

    public GunAmmoWidgetConfigureScreen(Anchor windowAnc, Anchor widgetAnc, int x, int y, int fullColor, int lowColor, int emptyColor, Consumer<WidgetConfigureScreen> saveConsumer) {
        super(windowAnc, widgetAnc, x, y, saveConsumer);
        this.fullColor = fullColor;
        this.lowColor = lowColor;
        this.emptyColor = emptyColor;
    }

    public GunAmmoWidgetConfigureScreen(Anchor windowAnc, Anchor widgetAnc, int x, int y, int fullColor, int lowColor, int emptyColor) {
        super(windowAnc, widgetAnc, x, y);
        this.fullColor = fullColor;
        this.lowColor = lowColor;
        this.emptyColor = emptyColor;
    }

    public GunAmmoWidgetConfigureScreen(GunAmmoWidget widget) {
        super(widget);
        this.fullColor = widget.getMagazineFull().get() & 0xFFFFFF;
        this.lowColor = widget.getMagazineLow().get() & 0xFFFFFF;
        this.emptyColor = widget.getMagazineEmpty().get() & 0xFFFFFF;
        this.additionalDataSaver = () -> {
            widget.setMagazineFull(new Color(this.fullColor));
            widget.setMagazineLow(new Color(this.lowColor));
            widget.setMagazineEmpty(new Color(this.emptyColor));
        };
    }

    @Override
    protected void init() {
        super.init();

        this.fullPreview = new ColorPreviewButton(this.cX + 87, this.cY - 68, 20, 20, Text.of(null), this.fullColor, color -> {
            this.fullPreview.setColor(color.get());
            this.fullColor = color.get();
        });
        this.fullPreview.setParent(this);
        this.lowPreview = new ColorPreviewButton(this.cX + 87, this.cY - 45, 20, 20, Text.of(null), this.lowColor, color -> {
            this.lowPreview.setColor(color.get());
            this.lowColor = color.get();
        });
        this.lowPreview.setParent(this);
        this.emptyPreview = new ColorPreviewButton(this.cX + 87, this.cY - 22, 20, 20, Text.of(null), this.emptyColor, color -> {
            this.emptyPreview.setColor(color.get());
            this.emptyColor = color.get();
        });
        this.emptyPreview.setParent(this);

        this.addButton(this.fullPreview);
        this.addButton(this.lowPreview);
        this.addButton(this.emptyPreview);
    }

    @Override
    public void renderCustomBackground(MatrixStack matrices, int mouseX, int mouseY) {
        super.renderCustomBackground(matrices, mouseX, mouseY);

        this.drawTexture(matrices, this.cX + 17, this.cY - 73, 161, 180, 95, 76);
    }
}
