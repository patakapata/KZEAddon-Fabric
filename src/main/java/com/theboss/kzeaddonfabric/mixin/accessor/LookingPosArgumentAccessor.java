package com.theboss.kzeaddonfabric.mixin.accessor;

import net.minecraft.command.argument.LookingPosArgument;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LookingPosArgument.class)
public interface LookingPosArgumentAccessor {
    @Accessor("x")
    double getX();

    @Accessor("y")
    double getY();

    @Accessor("z")
    double getZ();
}
