package com.theboss.kzeaddonfabric.wip.options;

import com.theboss.kzeaddonfabric.KZEAddon;
import com.theboss.kzeaddonfabric.screen.Screen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.jetbrains.annotations.Nullable;

public class RootScreen extends Screen {
    private int cX;
    private int cY;

    public RootScreen(@Nullable Object parent) {
        this();
        this.setParent(parent);
    }

    public RootScreen() {
        super(Text.of("Options root screen"));
    }

    @Override
    protected void init() {
        this.cX = this.width / 2;
        this.cY = this.height / 2;

        // Button size is 90x20
        this.addButton(new ButtonWidget(this.cX - 100, this.cY - 30, 90, 20, new TranslatableText("menu.kzeaddon.option.widget"), btn -> {
            MinecraftClient.getInstance().openScreen(new WidgetScreen(this));
        }));
        this.addButton(new ButtonWidget(this.cX + 10, this.cY - 30, 90, 20, new TranslatableText("menu.kzeaddon.option.glowColor"), btn -> { KZEAddon.LOGGER.info("Open the glow color screen"); }));
        this.addButton(new ButtonWidget(this.cX - 100, this.cY + 10, 90, 20, new TranslatableText("menu.kzeaddon.option.in_game"), btn -> {KZEAddon.LOGGER.info("Open the in game screen");}));
        this.addButton(new ButtonWidget(this.cX + 10, this.cY + 10, 90, 20, new TranslatableText("menu.kzeaddon.option.other"), btn -> { KZEAddon.LOGGER.info("Open the others screen"); }));
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
    }
}
