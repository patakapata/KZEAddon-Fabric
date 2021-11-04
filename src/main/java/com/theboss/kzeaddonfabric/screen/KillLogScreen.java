package com.theboss.kzeaddonfabric.screen;

import com.theboss.kzeaddonfabric.KZEAddon;
import com.theboss.kzeaddonfabric.KillLog;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

public class KillLogScreen extends Screen {
    private final KillLog log;
    private int scrollAmount;
    private float lastScrollAmount;

    public KillLogScreen() {
        super(Text.of("KillLog Screen"));

        this.log = KZEAddon.killLog;
        this.scrollAmount = 0;
        this.lastScrollAmount = this.scrollAmount;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);

        float scroll = MathHelper.lerp(delta, this.lastScrollAmount, this.scrollAmount);
        this.lastScrollAmount = scroll;

        MinecraftClient mc = MinecraftClient.getInstance();
        KillLog killLog = KZEAddon.killLog;
        killLog.render(matrices, mc.getWindow().getScaledWidth(), scroll, killLog.getLogSize(), 255);

        this.textRenderer.drawWithShadow(matrices, Text.of("Scroll: " + this.scrollAmount), 0, this.height / 2F, 0xFFFFFFFF);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        this.scrollAmount += amount * KZEAddon.options.killLogScrollMultiplier;
        if (this.scrollAmount < -(this.log.getLogSize() - 1) * this.log.getEntryHeight()) {
            this.scrollAmount = -(this.log.getLogSize() - 1) * this.log.getEntryHeight();
        } else if (this.scrollAmount > this.textRenderer.fontHeight) {
            this.scrollAmount = this.textRenderer.fontHeight;
        }
        return true;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.scrollAmount > 0) this.scrollAmount--;
    }
}
