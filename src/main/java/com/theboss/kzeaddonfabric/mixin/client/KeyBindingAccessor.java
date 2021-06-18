package com.theboss.kzeaddonfabric.mixin.client;

import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(KeyBinding.class)
public interface KeyBindingAccessor {

    @Accessor("boundKey")
    InputUtil.Key getBoundKey();


    @Accessor("categoryOrderMap")
    static Map<String, Integer> fabric_getCategoryMap() {
        throw new AssertionError();
    }
}
