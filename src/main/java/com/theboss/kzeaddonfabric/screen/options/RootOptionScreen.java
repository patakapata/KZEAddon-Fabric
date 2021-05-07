package com.theboss.kzeaddonfabric.screen.options;

import com.theboss.kzeaddonfabric.CustomSounds;
import com.theboss.kzeaddonfabric.screen.Screen;
import com.theboss.kzeaddonfabric.screen.options.widgets.WidgetOptionScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

public class RootOptionScreen extends Screen {
    private int cX;
    private int cY;

    private AbstractButtonWidget widgets;
    private AbstractButtonWidget glowColor;
    private AbstractButtonWidget _PENDING;
    private AbstractButtonWidget other;

    private AbstractButtonWidget close;

    private int honkTimer;

    public RootOptionScreen(Object parent) {
        this();
        this.setParent(parent);
    }

    public RootOptionScreen() {
        super(Text.of("Root Option Screen"));
    }

    @Override
    protected void init() {
        this.cX = this.width / 2;
        this.cY = this.height / 2;

        this.widgets = new ButtonWidget(this.cX - 103, this.cY - 60, 98, 20, new TranslatableText("menu.kzeaddon.option.widget"), this::onPressWidget);
        this.glowColor = new ButtonWidget(this.cX + 5, this.cY - 60, 98, 20, new TranslatableText("menu.kzeaddon.option.glowColor"), this::onPressGlowColor);
        this._PENDING = new ButtonWidget(this.cX - 103, this.cY - 30, 98, 20, new TranslatableText("menu.kzeaddon.option.pending"), this::onPressPending);
        this.other = new ButtonWidget(this.cX + 5, this.cY - 30, 98, 20, new TranslatableText("menu.kzeaddon.option.other"), this::onPressOther);
        this.close = new ButtonWidget(this.cX - 49, this.height - 30, 98, 20, new TranslatableText("menu.kzeaddon.option.close"), btn -> this.onClose());

        this.addButton(this.widgets);
        this.addButton(this.glowColor);
        this.addButton(this._PENDING);
        this.addButton(this.other);
        this.addButton(this.close);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        if (client.world == null) this.renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
    }

    protected void playHonk() {
        MinecraftClient mc = MinecraftClient.getInstance();
        mc.getSoundManager().play(PositionedSoundInstance.master(CustomSounds.HONK_EVENT, 1.0F));
    }

    public void openScreen(Screen screen) {
        MinecraftClient.getInstance().openScreen(screen);
    }

    protected void onPressWidget(ButtonWidget button) {
        this.openScreen(new WidgetOptionSelectScreen(this));
    }

    protected void onPressGlowColor(ButtonWidget button) {
        MinecraftClient.getInstance().openScreen(new GlowColorOptionScreen(this));
    }

    protected void onPressPending(ButtonWidget button) {
        MinecraftClient mc = MinecraftClient.getInstance();
        mc.getSoundManager().stopSounds(new Identifier("minecraft:ui.button.click"), SoundCategory.MASTER);
        this.honkTimer = 20;
        this._PENDING.setMessage(Text.of("This is honkable"));
        this.playHonk();
        mc.openScreen(new WidgetOptionScreen(Text.of("TEMP")));
    }

    @Override
    public void tick() {
        super.tick();
        if (this.honkTimer-- > 0) {
            if (this.honkTimer == 0) {
                this._PENDING.setMessage(new TranslatableText("menu.kzeaddon.option.pending"));
            }
        }
    }

    protected void onPressOther(ButtonWidget button) {}
}
