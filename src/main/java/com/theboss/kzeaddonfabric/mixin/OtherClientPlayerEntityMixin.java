package com.theboss.kzeaddonfabric.mixin;

import com.mojang.authlib.GameProfile;
import com.theboss.kzeaddonfabric.KZEAddon;
import com.theboss.kzeaddonfabric.VanillaUtils;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(OtherClientPlayerEntity.class)
public abstract class OtherClientPlayerEntityMixin extends AbstractClientPlayerEntity {
    public OtherClientPlayerEntityMixin(ClientWorld world, GameProfile profile) {
        super(world, profile);
    }

    @Override
    public boolean isInvisible() {
        if (KZEAddon.options.shouldHideTeammates && VanillaUtils.isTeammate(this)) return true;
        return super.isInvisible();
    }
}
