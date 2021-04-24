package com.theboss.kzeaddonfabric.wip;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import net.minecraft.command.argument.TextArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class RenameItemCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated) {
        RootCommandNode<ServerCommandSource> root = dispatcher.getRoot();
        LiteralCommandNode<ServerCommandSource> rename = CommandManager.literal("rename").build();
        LiteralCommandNode<ServerCommandSource> name = CommandManager.literal("name").build();
        LiteralCommandNode<ServerCommandSource> lore = CommandManager.literal("lore").build();

        // Name branch
        ArgumentCommandNode<ServerCommandSource, Text> nameValue = CommandManager.argument("nameValue", TextArgumentType.text()).executes(RenameItemCommand::rename).build();

        // Lore branch

        root.addChild(rename);
        rename.addChild(name);
        rename.addChild(lore);
        name.addChild(nameValue);
    }

    public static int rename(CommandContext<ServerCommandSource> ctx) {
        Text newName = TextArgumentType.getTextArgument(ctx, "nameValue");
        Entity entity = ctx.getSource().getEntity();
        if (entity == null) return -1;
        ItemStack stack = entity.getItemsHand().iterator().next();
        Text oldName = stack.getName();
        stack.setCustomName(newName);
        ctx.getSource().sendFeedback(Text.of(oldName.asString() + " -> " + newName.asString()), true);
        return 1;
    }
}
