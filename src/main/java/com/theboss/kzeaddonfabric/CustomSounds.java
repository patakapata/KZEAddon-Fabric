package com.theboss.kzeaddonfabric;

import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class CustomSounds {
    public static final Identifier HONK_ID = new Identifier("kzeaddon-fabric", "honk");
    public static final Identifier VOTE_NOTIFIC_ID = new Identifier("kzeaddon-fabric", "voting/enter");

    public static SoundEvent HONK_EVENT = new SoundEvent(HONK_ID);
    public static SoundEvent VOTE_NOTIFIC_EVENT = new SoundEvent(VOTE_NOTIFIC_ID);
}
