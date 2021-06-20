package com.theboss.kzeaddonfabric.mixin.client.resource_loader;

import com.theboss.kzeaddonfabric.wip.resource_loader.ModResourcePackCreator;
import net.minecraft.client.resource.ClientBuiltinResourcePackProvider;
import net.minecraft.resource.ResourcePackProfile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(ClientBuiltinResourcePackProvider.class)
public abstract class ClientBuiltInResourcePackProviderMixin {
    @Inject(method = "register", at = @At("RETURN"))
    private void addBuiltinResourcePacks(Consumer<ResourcePackProfile> profileAdder, ResourcePackProfile.Factory factory, CallbackInfo ci) {
        ModResourcePackCreator.CLIENT_RESOURCE_PACK_PROVIDER.register(profileAdder, factory);
    }
}
