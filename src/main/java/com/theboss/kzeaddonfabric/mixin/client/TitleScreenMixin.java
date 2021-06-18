package com.theboss.kzeaddonfabric.mixin.client;

import com.theboss.kzeaddonfabric.enums.Anchor;
import com.theboss.kzeaddonfabric.screen.button.AnchorSelectButton;
import com.theboss.kzeaddonfabric.screen.options.RootOptionScreen;
import com.theboss.kzeaddonfabric.screen.WidgetConfigureScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {
    private AbstractButtonWidget openKZEAddonOption;
    private AnchorSelectButton aSButton;

    public TitleScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        this.openKZEAddonOption = new ButtonWidget(this.width - 30, this.height - 30, 20, 20, Text.of("O"), btn -> MinecraftClient.getInstance().openScreen(new RootOptionScreen()));
        this.aSButton = new AnchorSelectButton(Anchor.LEFT_UP, 10, 10, 30, 30, btn -> MinecraftClient.getInstance().openScreen(new WidgetConfigureScreen(Anchor.LEFT_UP, Anchor.RIGHT_DOWN, 0, 0)));
        this.addButton(this.openKZEAddonOption);
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"))
    private void onMouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        this.aSButton.mouseClicked(mouseX, mouseY, button);
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void onRender(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        this.aSButton.render(matrices, mouseX, mouseY, delta);
    }
}
