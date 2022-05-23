package me.patakapata.kzeaddon.mixin;

import me.patakapata.kzeaddon.KzeAddonClientMod;
import net.minecraft.entity.Entity;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.util.Nameable;
import net.minecraft.world.entity.EntityLike;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(Entity.class)
public abstract class EntityMixin implements Nameable, EntityLike, CommandOutput {
    @Inject(method = "getTeamColorValue", at = @At(value = "RETURN"), locals = LocalCapture.CAPTURE_FAILSOFT, cancellable = true)
    protected void inject_getTeamColorValue(CallbackInfoReturnable<Integer> cir, AbstractTeam abstractTeam) {
        if(abstractTeam != null && abstractTeam.getName().equals(KzeAddonClientMod.ZOMBIE_TEAM) && KzeAddonClientMod.options.overrideZombieTeamColor) {
            cir.setReturnValue(KzeAddonClientMod.options.getZombieTeamColor());
        }
    }
}
