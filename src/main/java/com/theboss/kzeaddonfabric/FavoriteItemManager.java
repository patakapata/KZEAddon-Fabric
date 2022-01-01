package com.theboss.kzeaddonfabric;

import com.theboss.kzeaddonfabric.mixin.accessor.KeyBindingAccessor;
import com.theboss.kzeaddonfabric.utils.ModUtils;
import com.theboss.kzeaddonfabric.utils.VanillaUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class FavoriteItemManager {
    private static ItemStack lastItem = ItemStack.EMPTY;
    private static boolean holdProcessed;
    private static float holdProgress;
    private static long lastTime;

    public static void handleEvent(ItemStack stack, TooltipContext ctx, List<Text> list) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (!stack.equals(lastItem)) {
            holdProgress = 0;
            lastTime = System.currentTimeMillis();
            lastItem = stack;
        }
        float requireTime = 0.5F;
        int divide = 40;

        long now = System.currentTimeMillis();
        float delta = (now - lastTime) / 1_000F;
        lastTime = now;
        if (ModUtils.getKeyState(((KeyBindingAccessor) mc.options.keyForward).getBoundKey().getCode()) == GLFW.GLFW_PRESS) {

            if (holdProgress < 1) {
                holdProgress += delta / requireTime;
                if (holdProgress > 1) {
                    holdProgress = 1;
                }
            }
        } else if (holdProgress > 0) {
            holdProgress -= delta / requireTime * 2F;
            if (holdProgress < 0) holdProgress = 0;
        }

        if (holdProgress == 1) {
            if (!holdProcessed) {
                holdProcessed = true;
                boolean containItem = KZEAddon.isFavoriteItem(stack);

                if (!containItem) {
                    KZEAddon.addFavoriteItem(stack.copy());
                    KZEAddon.info("Favorite Items > " + VanillaUtils.textAsString(stack.getName()) + " is added!");

                } else {
                    KZEAddon.removeFavoriteItem(stack);
                    KZEAddon.info("Favorite Items > " + VanillaUtils.textAsString(stack.getName()) + " is removed!");
                }
            }
        } else {
            holdProcessed = false;
        }
        StringBuilder builder = new StringBuilder();
        int done = MathHelper.floor(holdProgress * divide);
        builder.append("§f");
        for (int i = 0; i < done; i++) builder.append("|");
        builder.append("§7");
        for (int i = 0; i < divide - done; i++) builder.append("|");
        builder.append("§r");

        LiteralText text = new LiteralText("(");
        text.append(VanillaUtils.textAsString(mc.options.keyForward.getBoundKeyLocalizedText()).toUpperCase());
        text.append(") ");
        text.append(builder.toString());

        list.add(Text.of(KZEAddon.isFavoriteItem(stack) ? "§a✔ §6Favorite§r" : "§c✗ §8Favorite§r"));
        list.add(list.size() - (ctx.isAdvanced() ? 1 : 0), text);
    }

    private FavoriteItemManager() {}
}
