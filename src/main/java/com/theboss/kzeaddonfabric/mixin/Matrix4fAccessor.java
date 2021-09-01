package com.theboss.kzeaddonfabric.mixin;

import net.minecraft.util.math.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Matrix4f.class)
public interface Matrix4fAccessor {

    @Accessor("a00")
    float a00();

    @Accessor("a10")
    float a01();

    @Accessor("a01")
    float a10();

    @Accessor("a11")
    float a11();

    @Accessor("a02")
    float a20();

    @Accessor("a12")
    float a21();
}
