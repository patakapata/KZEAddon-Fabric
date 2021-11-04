package com.theboss.kzeaddonfabric;

import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class CustomSounds {
    public static final Identifier HONK_ID = new Identifier(KZEAddon.MOD_ID, "honk");
    public static final Identifier VOTE_NOTIFIC_ID = new Identifier(KZEAddon.MOD_ID, "vote.start");

    public static final SoundEvent HONK_EVENT = new SoundEvent(HONK_ID);
    public static final SoundEvent VOTE_NOTIFIC_EVENT = new SoundEvent(VOTE_NOTIFIC_ID);
}
