package com.theboss.kzeaddonfabric.mixin;

import net.minecraft.client.gl.JsonEffectGlShader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(JsonEffectGlShader.class)
public abstract class JsonEffectGlShaderMixin {
    @ModifyArg(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Identifier;<init>(Ljava/lang/String;)V"), index = 0)
    private String replaceConstructor(String org) {
        if (org.contains(":")) {
            String[] id = org.substring(16, org.length() - 5).split(":");
            org = id[0] + ":shaders/program/" + id[1] + ".json";
        }
        return org;
    }

    @ModifyArg(method = "getShader", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Identifier;<init>(Ljava/lang/String;)V"), index = 0)
    private static String replaceGetShader(String org) {
        if (org.contains(":")) {
            String ext = org.substring(org.length() - 3);
            String[] id = org.substring(16, org.length() - 4).split(":");
            org = id[0] + ":shaders/program/" + id[1] + "." + ext;
        }
        return org;
    }
}
