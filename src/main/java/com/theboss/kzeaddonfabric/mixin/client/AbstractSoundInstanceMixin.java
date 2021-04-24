package com.theboss.kzeaddonfabric.mixin.client;

import com.theboss.kzeaddonfabric.KZEAddon;
import net.minecraft.client.sound.AbstractSoundInstance;
import net.minecraft.client.sound.Sound;
import net.minecraft.client.sound.SoundInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(AbstractSoundInstance.class)
public abstract class AbstractSoundInstanceMixin implements SoundInstance {

    @Shadow
    protected float volume;

    @Shadow
    protected Sound sound;

    /**
     * @author theBooooSS
     * @reason 銃声の音量を変えるため
     */
    @Overwrite
    public float getVolume() {
        boolean isGunfire = this.sound.getIdentifier().getPath().startsWith("gunshot/");
        if (isGunfire && KZEAddon.OPTIONS.isSetGunfireSoundVolume()) {
            return KZEAddon.OPTIONS.getGunfireVolume();
        }
        return this.volume * this.sound.getVolume();
    }
}
