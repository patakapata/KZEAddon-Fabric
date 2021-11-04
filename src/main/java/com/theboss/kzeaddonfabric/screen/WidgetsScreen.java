package com.theboss.kzeaddonfabric.screen;

import com.theboss.kzeaddonfabric.KZEAddon;
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

        this.addButton(new ButtonWidget(cX - 103, cY - 40, 98, 20, new TranslatableText("menu.widgets.kzeaddon.main_weapon"), unused -> KZEAddon.widgetRenderer.openArrangementScreen("primary")));
        this.addButton(new ButtonWidget(cX + 5, cY - 40, 98, 20, new TranslatableText("menu.widgets.kzeaddon.sub_weapon"), unused -> KZEAddon.widgetRenderer.openArrangementScreen("secondary")));

        this.addButton(new ButtonWidget(cX - 103, cY - 10, 98, 20, new TranslatableText("menu.widgets.kzeaddon.melee_weapon"), unused -> KZEAddon.widgetRenderer.openArrangementScreen("melee")));
        this.addButton(new ButtonWidget(cX + 5, cY - 10, 98, 20, new TranslatableText("menu.widgets.kzeaddon.reload_time"), unused -> KZEAddon.widgetRenderer.openArrangementScreen("reload_time")));

        this.addButton(new ButtonWidget(cX - 103, cY + 20, 98, 20, new TranslatableText("menu.widgets.kzeaddon.total_ammo"), unused -> KZEAddon.widgetRenderer.openArrangementScreen("total_ammo")));
        this.addButton(new ButtonWidget(cX + 5, cY + 20, 98, 20, new TranslatableText("menu.widgets.kzeaddon.literal_widgets"), unused -> KZEAddon.widgetRenderer.openLiteralWidgetsScreen()));
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
        this.renderWIPText(matrices);
    }
}
