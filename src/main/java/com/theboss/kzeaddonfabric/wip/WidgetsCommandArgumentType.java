package com.theboss.kzeaddonfabric.wip;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import java.util.concurrent.CompletableFuture;

public class WidgetsCommandArgumentType implements ArgumentType<WidgetsCommandArgumentType.Widgets> {

    public static WidgetsCommandArgumentType widget() {
        return new WidgetsCommandArgumentType();
    }

    private WidgetsCommandArgumentType() {}

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        for (Widgets widgets : Widgets.values()) {
            builder.suggest(widgets.toString());
        }
        return builder.buildFuture();
    }

    @Override
    public Widgets parse(StringReader reader) throws CommandSyntaxException {
        return Widgets.valueOf(reader.readString());
    }

    public static enum Widgets {
        MAIN_W, SUB_W, MELEE_W, RELOAD_TIME, TOTAL_AMMO
    }
}
