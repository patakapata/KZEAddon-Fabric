package com.theboss.kzeaddonfabric.mixin.client;

import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(WorldRenderer.class)
public interface WorldRendererAccessor {
    @Accessor("bufferBuilders")
    BufferBuilderStorage _getBufferBuilderStorage();
}
