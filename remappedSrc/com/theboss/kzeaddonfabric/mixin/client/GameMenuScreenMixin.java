package com.theboss.kzeaddonfabric.mixin.client;

import com.theboss.kzeaddonfabric.KZEAddon;
import com.theboss.kzeaddonfabric.screen.options.RootOptionScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameMenuScreen.class)
public abstract class GameMenuScreenMixin extends Screen {
    private ClickableWidget saveBtn;
    private ClickableWidget loadBtn;
    private ClickableWidget openBtn;
    private ClickableWidget optionBtn;

    private int saveBtnTime = -1;
    private int loadBtnTime = -1;
    private int openBtnTime = -1;

    public GameMenuScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "initWidgets", at = @At("RETURN"))
    private void onInitWidgets(CallbackInfo ci) {
        this.optionBtn = new ButtonWidget(this.width - 30, this.height - 120, 20, 20, Text.of("@"), btn -> MinecraftClient.getInstance().openScreen(new RootOptionScreen(this)));
        this.openBtn = new ButtonWidget(this.width - 30, this.height - 90, 20, 20, Text.of("O"), btn -> {
            btn.setMessage(Text.of(KZEAddon.openConfigWithEditor() ? "*" : "X"));
            this.openBtnTime = 20;
        });
        this.saveBtn = new ButtonWidget(this.width - 30, this.height - 60, 20, 20, Text.of("S"), btn -> {
            KZEAddon.saveConfig();
            btn.setMessage(Text.of("*"));
            this.saveBtnTime = 20;
        });
        this.loadBtn = new ButtonWidget(this.width - 30, this.height - 30, 20, 20, Text.of("L"), btn -> {
            KZEAddon.loadConfig();
            btn.setMessage(Text.of("*"));
            this.loadBtnTime = 20;
        });
        this.addButton(this.optionBtn);
        this.addButton(this.openBtn);
        this.addButton(this.saveBtn);
        this.addButton(this.loadBtn);
    }

    @Override
    public void tick() {
        if (this.saveBtnTime > 0) {
            this.saveBtnTime--;
            if (this.saveBtnTime == 0) {
                this.saveBtn.setMessage(Text.of("S"));
            }
        }
        if (this.loadBtnTime > 0) {
            this.loadBtnTime--;
            if (this.loadBtnTime == 0) {
                this.loadBtn.setMessage(Text.of("L"));
            }
        }
        if (this.openBtnTime > 0) {
            this.openBtnTime--;
            if (this.openBtnTime == 0) {
                this.openBtn.setMessage(Text.of("O"));
            }
        }
    }
}
