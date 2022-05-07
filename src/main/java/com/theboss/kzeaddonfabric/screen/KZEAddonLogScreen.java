package com.theboss.kzeaddonfabric.screen;

import com.theboss.kzeaddonfabric.KZEAddon;
import com.theboss.kzeaddonfabric.KZEAddonLog;
import com.theboss.kzeaddonfabric.render.BarrierVisualizer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

import java.util.List;

public class KZEAddonLogScreen extends Screen {
    private final List<KZEAddonLog.Entry> entries;
    private int scroll;

    public KZEAddonLogScreen() {
        super(Text.of("KZEAddon Log Screen"));
        this.entries = KZEAddon.getModLog().getEntries();
        this.scroll = 0;
    }

    protected void checkScrollBounds() {
        if (this.scroll > 0) this.scroll = 0;
        if (this.scroll <= -this.entries.size()) this.scroll = -this.entries.size() + 1;
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        this.scroll += amount;
        this.checkScrollBounds();
        return true;
    }

    @Override
    protected void init() {
        this.addButton(new ButtonWidget(this.width - 100, this.height - 30, 80, 20, new TranslatableText("button.kzeaddon.barrier.reallocate"), unused -> {
            BarrierVisualizer visualizer = KZEAddon.getBarrierVisualizer();
            visualizer.recordRenderCall(visualizer::forceReallocate);
            KZEAddon.info("Barrier visualizer reallocate queued!");
        }));
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
        if (this.client == null) this.client = MinecraftClient.getInstance();
        int lastIndex = this.entries.size() - 1;

        for (int i = 0; i <= lastIndex; i++) {
            KZEAddonLog.Entry entry = this.entries.get(lastIndex - i);
            entry.renderAsOpaque(matrices, this.textRenderer, 0, (this.scroll * this.textRenderer.fontHeight) + (i * this.textRenderer.fontHeight), KZEAddon.getModLog().isShowTime());
        }
    }
}
