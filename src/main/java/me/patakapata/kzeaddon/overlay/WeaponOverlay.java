package me.patakapata.kzeaddon.overlay;

import me.patakapata.kzeaddon.KzeAddonClientMod;
import me.patakapata.kzeaddon.KzeData;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;

public class WeaponOverlay {
    private final List<OverlayEntry> entries = new ArrayList<>();

    public WeaponOverlay() {
        KzeData kzeData = KzeData.getInstance();

        this.entries.add(new OverlayEntry(
                kzeData::isMainWeaponValid,
                () -> Text.of(Integer.toString(kzeData.getMainWeapon().getAmmoInMagazine())),
                () -> 0xFFFFFFFF,
                Anchor.MIDDLE_DOWN,
                Anchor.MIDDLE_DOWN,
                -80, -25
        ));
        this.entries.add(new SlideEntry(
                kzeData::isHoldMainWeapon,
                () -> Text.of(Integer.toString(kzeData.getMainWeapon().getRemainingAmmo())),
                () -> 0xFFFFFFFF,
                Anchor.MIDDLE_DOWN,
                Anchor.MIDDLE_DOWN,
                -80, -25,
                -80, -50
        ));
        this.entries.add(new OverlayEntry(
                kzeData::isSecondaryWeaponValid,
                () -> Text.of(Integer.toString(kzeData.getSecondaryWeapon().getAmmoInMagazine())),
                () -> 0xFFFFFFFF,
                Anchor.MIDDLE_DOWN,
                Anchor.MIDDLE_DOWN,
                -60,
                -25
        ));
        this.entries.add(new SlideEntry(
                kzeData::isHoldSecondaryWeapon,
                () -> Text.of(Integer.toString(kzeData.getSecondaryWeapon().getRemainingAmmo())),
                () -> 0xFFFFFFFF,
                Anchor.MIDDLE_DOWN,
                Anchor.MIDDLE_DOWN,
                -60, -25,
                -60, -50
        ));
        this.entries.add(new OverlayEntry(
                kzeData::isMeleeWeaponValid,
                () -> Text.of(Integer.toString(kzeData.getMeleeWeapon().getAmmoInMagazine())),
                () -> 0xFFFFFFFF,
                Anchor.MIDDLE_DOWN,
                Anchor.MIDDLE_DOWN,
                -40,
                -25
        ));
        this.entries.add(new SlideEntry(
                kzeData::isHoldMeleeWeapon,
                () -> Text.of(Integer.toString(kzeData.getMeleeWeapon().getRemainingAmmo())),
                () -> 0xFFFFFFFF,
                Anchor.MIDDLE_DOWN,
                Anchor.MIDDLE_DOWN,
                -40, -25,
                -40, -50
        ));
    }

    public void registerOverlay() {
        OverlayRegister registerer = new OverlayRegister();
        for (RegisterOverlay register : FabricLoader.getInstance().getEntrypoints("kzeaddon", RegisterOverlay.class)) {
            register.registerOverlay(registerer);
        }
        List<OverlayEntry> registered = registerer.pop();
        this.entries.addAll(registered);

        KzeAddonClientMod.LOGGER.info("Registered " + registered.size() + " widget(s)");
    }

    public void onRenderHud(MatrixStack matrices, float delta) {
        MinecraftClient mc = MinecraftClient.getInstance();
        TextRenderer textRenderer = mc.textRenderer;
        Window window = mc.getWindow();
        int scaledWidth = window.getScaledWidth();
        int scaledHeight = window.getScaledHeight();

        for (OverlayEntry entry : this.entries) {
            int color = entry.getColor();
            if (entry.getShouldRender() && ((color >> 24) & 0xFF) > 10) {
                Text text = entry.getText();
                int width = textRenderer.getWidth(text);
                int height = textRenderer.fontHeight;
                int x = MathHelper.floor((scaledWidth * entry.getWindowAnchor().getX()) - (width * entry.getElementAnchor().getX()));
                int offsetX = MathHelper.floor(entry.getX(delta));
                int y = MathHelper.floor((scaledHeight * entry.getWindowAnchor().getY()) - (height * entry.getElementAnchor().getY()));
                int offsetY = MathHelper.floor(entry.getY(delta));
                matrices.push();
                matrices.translate(
                        x + offsetX,
                        y + offsetY,
                        0
                );
                textRenderer.draw(matrices, text, 0, 0, color);
                matrices.pop();
            }
        }
    }

    public void onClientTick(MinecraftClient mc) {
        for (OverlayEntry entry : this.entries) {
            entry.tick(mc);
        }
    }

}
