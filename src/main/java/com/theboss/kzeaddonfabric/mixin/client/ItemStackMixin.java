package com.theboss.kzeaddonfabric.mixin.client;

import com.theboss.kzeaddonfabric.KZEAddon;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Inject(method = "getTooltip", at = @At("RETURN"))
    private void getTooltip(PlayerEntity entity, TooltipContext tooltipContext, CallbackInfoReturnable<List<Text>> info) {
        KZEAddon.handleItemTooltip((ItemStack) (Object) this, tooltipContext, info.getReturnValue());
    }
}
