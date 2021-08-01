package com.theboss.kzeaddonfabric.mixin.client;

import com.theboss.kzeaddonfabric.enums.Anchor;
import com.theboss.kzeaddonfabric.screen.configure.ConstantWidgetConfigureScreen;
import com.theboss.kzeaddonfabric.screen.options.RootOptionScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {

    public TitleScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        this.addButton(new ButtonWidget(this.width - 20, -12, 20, 20, Text.of("O"), btn -> MinecraftClient.getInstance().openScreen(new RootOptionScreen())));
        this.addButton(new ButtonWidget(0, -12, 20, 20, Text.of("T"), btn -> MinecraftClient.getInstance().openScreen(new ConstantWidgetConfigureScreen(Text.of("Default"), 0xFFFFFF, Anchor.MIDDLE_MIDDLE, Anchor.MIDDLE_MIDDLE, 0, 0))));
    }
}
