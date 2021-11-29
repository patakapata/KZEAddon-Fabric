package com.theboss.kzeaddonfabric.commands.arguments;

import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.util.math.Direction;

import java.util.Arrays;
import java.util.List;

public class DirectionArgumentType extends EnumArgumentType<Direction> {
    public static DirectionArgumentType direction() {
        return new DirectionArgumentType(Arrays.asList(Direction.values()));
    }

    public static Direction getDirection(String name, CommandContext<FabricClientCommandSource> ctx) {
        return ctx.getArgument(name, Direction.class);
    }

    private DirectionArgumentType(List<Direction> list) {
        super(list);
    }
}
