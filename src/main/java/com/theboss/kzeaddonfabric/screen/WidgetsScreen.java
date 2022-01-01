package com.theboss.kzeaddonfabric.screen;

import com.theboss.kzeaddonfabric.KZEAddon;
import com.theboss.kzeaddonfabric.widgets.WidgetRenderer;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public class WidgetsScreen extends Screen {
    public WidgetsScreen() {
        super(Text.of("Widgets screen"));
    }

    @Override
    protected void init() {
        int cX = this.width / 2;
        int cY = this.height / 2;

        this.addButton(new ButtonWidget(cX - 103, cY - 40, 98, 20, new TranslatableText("menu.kzeaddon.widgets.main_weapon"), unused -> KZEAddon.widgetRenderer.openEditScreen(WidgetRenderer.BuiltInWidget.PRIMARY)));
        this.addButton(new ButtonWidget(cX + 5, cY - 40, 98, 20, new TranslatableText("menu.kzeaddon.widgets.sub_weapon"), unused -> KZEAddon.widgetRenderer.openEditScreen(WidgetRenderer.BuiltInWidget.SECONDARY)));

        this.addButton(new ButtonWidget(cX - 103, cY - 10, 98, 20, new TranslatableText("menu.kzeaddon.widgets.melee_weapon"), unused -> KZEAddon.widgetRenderer.openEditScreen(WidgetRenderer.BuiltInWidget.MELEE)));
        this.addButton(new ButtonWidget(cX + 5, cY - 10, 98, 20, new TranslatableText("menu.kzeaddon.widgets.reload_time"), unused -> KZEAddon.widgetRenderer.openEditScreen(WidgetRenderer.BuiltInWidget.RELOAD_TIME)));

        this.addButton(new ButtonWidget(cX - 103, cY + 20, 98, 20, new TranslatableText("menu.kzeaddon.widgets.total_ammo"), unused -> KZEAddon.widgetRenderer.openEditScreen(WidgetRenderer.BuiltInWidget.TOTAL_AMMO)));
        this.addButton(new ButtonWidget(cX + 5, cY + 20, 98, 20, new TranslatableText("menu.kzeaddon.widgets.text_widgets"), unused -> KZEAddon.widgetRenderer.openWidgetListScreen()));

        this.addButton(new ButtonWidget(this.width - 40, this.height - 30, 30, 20, Text.of("Save"), unused -> KZEAddon.widgetRenderer.save()));
        this.addButton(new ButtonWidget(this.width - 40, this.height - 60, 30, 20, Text.of("Load"), unused -> KZEAddon.widgetRenderer.load()));
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
        this.renderWIPText(matrices);
    }
}
