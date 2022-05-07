package com.theboss.kzeaddonfabric.screen;

import com.theboss.kzeaddonfabric.KZEAddon;
import com.theboss.kzeaddonfabric.render.BarrierVisualizer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class DebugScreen extends Screen {
    public DebugScreen() {
        super(Text.of("Debug screen"));
    }

    @Override
    protected void init() {
        int cX = this.width / 2;
        int cY = this.height / 2;

        // Size = 80x20
        this.addButton(new ButtonWidget(cX - 40, cY - 25, 80, 20, Text.of("ModLog"), this::onPressDebug));
        this.addButton(new ButtonWidget(cX - 40, cY, 80, 20, Text.of("Toggle FBO"), this::onPressToggleFBO));
        this.addButton(new ButtonWidget(cX - 40, cY + 25, 80, 20, Text.of("Edit widgets"), this::onPressEditWidgets));
        this.addButton(new ButtonWidget(cX - 40, cY + 50, 80, 20, Text.of("Config BV"), this::onPressConfigBV));
    }

    private void onPressConfigBV(ButtonWidget btn) {
        this.open(BarrierVisualizer.createConfigScreen(KZEAddon.getBarrierVisualizer(), null));
    }

    private void onPressEditWidgets(ButtonWidget btn) {
        KZEAddon.getWidgetRenderer().openWidgetsScreen();
    }

    private void onPressToggleFBO(ButtonWidget btn) {
        BarrierVisualizer visualizer = KZEAddon.getBarrierVisualizer();
        visualizer.setUseFBO(!visualizer.isUseFbo());
    }

    private void onPressDebug(ButtonWidget btn) {
        this.open(new KZEAddonLogScreen());
    }

    private void open(net.minecraft.client.gui.screen.Screen screen) {
        MinecraftClient.getInstance().openScreen(screen);
    }
}
