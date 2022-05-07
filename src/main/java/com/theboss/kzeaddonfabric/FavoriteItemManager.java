package com.theboss.kzeaddonfabric;

import com.theboss.kzeaddonfabric.mixin.accessor.KeyBindingAccessor;
import com.theboss.kzeaddonfabric.utils.ModUtils;
import com.theboss.kzeaddonfabric.utils.VanillaUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FavoriteItemManager {
    private static final DefaultedList<ItemStack> favoriteItems = DefaultedList.of();
    private static ItemStack lastItem = ItemStack.EMPTY;
    private static boolean holdProcessed;
    private static float holdProgress;
    private static long lastTime;

    public static void addItem(ItemStack item) {
        favoriteItems.add(item);
    }

    public static List<ItemStack> getItems() {
        return new ArrayList<>(favoriteItems);
    }

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
        float delta = (now - lastTime) / 50F;
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
                boolean containItem = isFavorite(stack);

                if (!containItem) {
                    addItem(stack.copy());
                    KZEAddon.info("Favorite Items > " + VanillaUtils.textAsString(stack.getName()) + " is added!");

                } else {
                    removeItem(stack);
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

        list.add(Text.of(isFavorite(stack) ? "§a✔ §6Favorite§r" : "§c✗ §8Favorite§r"));
        list.add(list.size() - (ctx.isAdvanced() ? 1 : 0), text);
    }

    public static boolean isFavorite(ItemStack item) {
        Iterator<ItemStack> itr = favoriteItems.iterator();
        ItemStack var;

        while (itr.hasNext()) {
            var = itr.next();

            if (ItemStack.areEqual(var, item)) return true;
        }

        return false;
    }

    public static void removeItem(ItemStack item) {
        Iterator<ItemStack> itr = favoriteItems.iterator();
        ItemStack var;
        int i = 0;

        while (itr.hasNext()) {
            var = itr.next();
            if (ItemStack.areEqual(var, item)) {
                favoriteItems.remove(i);
                return;
            }

            i++;
        }
    }

    private FavoriteItemManager() {}
}
