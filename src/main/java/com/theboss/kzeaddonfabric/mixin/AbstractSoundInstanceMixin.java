package com.theboss.kzeaddonfabric.mixin;

import com.theboss.kzeaddonfabric.KZEAddon;
import net.minecraft.client.sound.AbstractSoundInstance;
import net.minecraft.client.sound.Sound;
import net.minecraft.client.sound.SoundInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractSoundInstance.class)
public abstract class AbstractSoundInstanceMixin implements SoundInstance {

    @Shadow
    protected Sound sound;

    @Inject(method = "getVolume", at = @At("RETURN"), cancellable = true)
    private void onGetVolume(CallbackInfoReturnable<Float> cir) {
        if (this.sound.getIdentifier().getPath().startsWith("gunshot/") && KZEAddon.getOptions().isChangeGunfireVolume) {
            cir.setReturnValue(cir.getReturnValueF() * KZEAddon.getOptions().gunfireVolumeMultiplier);
        }
    }
}
