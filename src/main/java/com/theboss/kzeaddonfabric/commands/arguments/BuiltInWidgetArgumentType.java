package com.theboss.kzeaddonfabric.commands.arguments;

import com.mojang.brigadier.context.CommandContext;
import com.theboss.kzeaddonfabric.widgets.WidgetRenderer;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;

import java.util.Arrays;
import java.util.List;

public class BuiltInWidgetArgumentType extends EnumArgumentType<WidgetRenderer.BuiltInWidget> {
    public static BuiltInWidgetArgumentType builtin() {
        return new BuiltInWidgetArgumentType(Arrays.asList(WidgetRenderer.BuiltInWidget.values()));
    }

    public static WidgetRenderer.BuiltInWidget getBuiltInWidget(String name, CommandContext<FabricClientCommandSource> ctx) {
        return ctx.getArgument(name, WidgetRenderer.BuiltInWidget.class);
    }

    private BuiltInWidgetArgumentType(List<WidgetRenderer.BuiltInWidget> list) {
        super(list);
    }
}
