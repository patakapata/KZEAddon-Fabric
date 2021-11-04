package com.theboss.kzeaddonfabric.mixin;

import com.theboss.kzeaddonfabric.KeyBindings;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameOptions.class)
public abstract class GameOptionsMixin {
    @Mutable
    @Shadow
    @Final
    public KeyBinding[] keysAll;

    @Inject(method = "load", at = @At("HEAD"))
    private void onLoad(CallbackInfo ci) {
        this.keysAll = KeyBindings.registerKeybindings(this.keysAll);
    }
}
