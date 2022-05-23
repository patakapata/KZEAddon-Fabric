package me.patakapata.kzeaddon;

import me.patakapata.kzeaddon.overlay.OverlayRegister;
import me.patakapata.kzeaddon.overlay.RegisterOverlay;
import me.patakapata.kzeaddon.overlay.Anchor;
import me.patakapata.kzeaddon.overlay.OverlayEntry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;

public class ExampleOverlay implements RegisterOverlay {
    @Override
    public void registerOverlay(OverlayRegister register) {
        MinecraftClient mc = MinecraftClient.getInstance();
        ClientPlayerEntity player = mc.player;

        register.register(new OverlayEntry(
                () -> player != null && player.isSneaking(),
                () -> Text.of("スニーク中"),
                () -> 0xFFFFFFFF,
                Anchor.MIDDLE_DOWN,
                Anchor.MIDDLE_DOWN,
                0, -60
        ));
    }
}
