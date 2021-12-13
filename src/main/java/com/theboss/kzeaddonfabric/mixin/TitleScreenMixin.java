package com.theboss.kzeaddonfabric.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.theboss.kzeaddonfabric.wip.WidgetSelectScreen;
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
        this.addButton(
                new ButtonWidget(this.width - 40, this.height - 40, 20, 20, Text.of("DEBUG"), unused -> RenderSystem.recordRenderCall(() -> {
                    WidgetSelectScreen screen = new WidgetSelectScreen();
                    screen.open(MinecraftClient.getInstance());
                }))
        );
    }
}
