package com.theboss.kzeaddonfabric.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.theboss.kzeaddonfabric.KZEAddon;
import com.theboss.kzeaddonfabric.screen.RadialTextureScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
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
                new ButtonWidget(this.width - 40, this.height - 40, 20, 20, Text.of("DEBUG"), unused -> {
                    RenderSystem.recordRenderCall(() -> {
                        RadialTextureScreen screen = new RadialTextureScreen(new Identifier(KZEAddon.MOD_ID, "textures/gui/frame.png"));
                        screen.open(MinecraftClient.getInstance());
                    });
                })
        );
    }
}
