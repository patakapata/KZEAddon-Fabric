package com.theboss.kzeaddonfabric.wip;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

@FunctionalInterface
public interface BlockEventListener {
    void handle(BlockPos pos, BlockState state);
}
