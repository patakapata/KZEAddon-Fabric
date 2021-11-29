package com.theboss.kzeaddonfabric.mixin.accessor;

import net.minecraft.client.gl.JsonEffectGlShader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(JsonEffectGlShader.class)
public interface JsonEffectGlShaderAccessor {
    @Accessor("activeShader")
    static JsonEffectGlShader getActiveShader() {
        throw new AssertionError();
    }

    @Accessor("activeProgramRef")
    static int getActiveProgramRef() {
        throw new AssertionError();
    }

    @Accessor("name")
    String getName();

    @Accessor("samplerShaderLocs")
    List<Integer> getSamplerShaderLocs();
}
