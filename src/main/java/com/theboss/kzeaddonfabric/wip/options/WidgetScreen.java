package com.theboss.kzeaddonfabric.wip.options;

import com.theboss.kzeaddonfabric.KZEAddon;
import com.theboss.kzeaddonfabric.screen.Screen;
import com.theboss.kzeaddonfabric.screen.configure.GunAmmoWidgetConfigureScreen;
import com.theboss.kzeaddonfabric.screen.configure.SimpleWidgetConfigureScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.jetbrains.annotations.Nullable;

public class WidgetScreen extends Screen {
    private int cY;
    private int cX;

    public WidgetScreen(@Nullable Object parent) {
        this();
        this.setParent(parent);
    }

    public WidgetScreen() {
        super(Text.of("Widgets screen"));
    }

    @Override
    protected void init() {
        this.cX = this.width / 2;
        this.cY = this.height / 2;

        // lu
        this.addButton(new ButtonWidget(this.cX - 95, this.cY - 40, 90, 20, new TranslatableText("menu.kzeaddon.option.mainWeapon"), btn -> {
            GunAmmoWidgetConfigureScreen screen = new GunAmmoWidgetConfigureScreen(KZEAddon.Options.getPrimaryAmmo());
            screen.setParent(this);
            screen.open(this.client);
        }));
        // lm
        this.addButton(new ButtonWidget(this.cX - 95, this.cY - 10, 90, 20, new TranslatableText("menu.kzeaddon.option.subWeapon"), btn -> {
            GunAmmoWidgetConfigureScreen screen = new GunAmmoWidgetConfigureScreen(KZEAddon.Options.getSecondaryAmmo());
            screen.setParent(this);
            screen.open(this.client);
        }));
        // ld
        this.addButton(new ButtonWidget(this.cX - 95, this.cY + 20, 90, 20, new TranslatableText("menu.kzeaddon.option.meleeWeapon"), btn -> {
            GunAmmoWidgetConfigureScreen screen = new GunAmmoWidgetConfigureScreen(KZEAddon.Options.getMeleeAmmo());
            screen.setParent(this);
            screen.open(this.client);
        }));

        // ru
        this.addButton(new ButtonWidget(this.cX + 5, this.cY - 40, 90, 20, new TranslatableText("menu.kzeaddon.option.totalAmmo"), btn -> {
            SimpleWidgetConfigureScreen screen = new SimpleWidgetConfigureScreen(KZEAddon.Options.getTotalAmmo());
            screen.setParent(this);
            screen.open(this.client);
        }));
        // rm
        this.addButton(new ButtonWidget(this.cX + 5, this.cY - 10, 90, 20, new TranslatableText("menu.kzeaddon.option.reloadTime"), btn -> {
            SimpleWidgetConfigureScreen screen = new SimpleWidgetConfigureScreen(KZEAddon.Options.getReloadIndicator());
            screen.setParent(this);
            screen.open(this.client);
        }));
        // rd
        this.addButton(new ButtonWidget(this.cX + 5, this.cY + 20, 90, 20, new TranslatableText("menu.kzeaddon.option.constants"), btn -> { KZEAddon.LOGGER.info("Constants screen"); }));
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
    }
}
