package com.theboss.kzeaddonfabric.render.hud;

import com.theboss.kzeaddonfabric.render.Anchor;
import com.theboss.kzeaddonfabric.render.Widget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;

public class TestHUD implements HUD {
    private double x;
    private double y;
    private double z;

    private final Widget cordX = new Widget(() -> new LiteralText(String.format("%.2f", x)), Anchor.RIGHT_MIDDLE, Anchor.MIDDLE_MIDDLE, 1.0F, () -> 0xFFFFFF, 0, 0);
    private final Widget cordY = new Widget(() -> new LiteralText(String.format("%.2f", y)), Anchor.RIGHT_MIDDLE, Anchor.MIDDLE_MIDDLE, 1.0F, () -> 0xFFFFFF, 0, 10);
    private final Widget cordZ = new Widget(() -> new LiteralText(String.format("%.2f", z)), Anchor.RIGHT_MIDDLE, Anchor.MIDDLE_MIDDLE, 1.0F, () -> {
        double a = x % 16 / 16;
        int b = (int) (255 * a);
        return (b << 16) | (b << 8) | b;
    }, 0, 20);

    @Override
    public void tick() {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;

        this.x = player.getX();
        this.y = player.getY();
        this.z = player.getZ();
    }

    @Override
    public void render(MatrixStack matrices) {
        MinecraftClient client = MinecraftClient.getInstance();
        Window window = client.getWindow();
        TextRenderer textRenderer = client.textRenderer;

        cordX.render(matrices, window, textRenderer);
        cordY.render(matrices, window, textRenderer);
        cordZ.render(matrices, window, textRenderer);
    }
}
