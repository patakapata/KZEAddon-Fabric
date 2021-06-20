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
     * @reason Change the gunfire sound volume
     */
    @Overwrite
    public float getVolume() {
        boolean isGunfire = this.sound.getIdentifier().getPath().startsWith("gunshot/");
        if (isGunfire && KZEAddon.Options.isSetGunfireSoundVolume()) {
            return KZEAddon.Options.getGunfireVolume();
        }
        return this.volume * this.sound.getVolume();
    }
}
