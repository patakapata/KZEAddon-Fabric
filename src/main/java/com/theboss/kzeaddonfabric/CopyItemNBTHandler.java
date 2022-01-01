package com.theboss.kzeaddonfabric;

import com.theboss.kzeaddonfabric.utils.VanillaUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

import java.util.List;

public class CopyItemNBTHandler {
    public static void handleEvent(ItemStack stack, TooltipContext ctx, List<Text> list) {
        // -------------------------------------------------- //
        // NBTコピー
        MinecraftClient mc = MinecraftClient.getInstance();
        boolean isPressed = InputUtil.isKeyPressed(mc.getWindow().getHandle(), KeyBindings.COPY_ITEM_TAG.getCode());
        NbtElement tag = stack.getTag();
        if (tag != null) {
            String str = tag.toText().getString();
            if (isPressed) {
                if (!KeyBindings.COPY_FLIP_FLIP) {
                    KeyBindings.COPY_FLIP_FLIP = true;
                    VanillaUtils.copyToClipboard(new TranslatableText(stack.getTranslationKey()), str);
                }
            } else {
                if (KeyBindings.COPY_FLIP_FLIP) {
                    KeyBindings.COPY_FLIP_FLIP = false;
                }
            }
        }
    }

    private CopyItemNBTHandler() {}
}
