package com.theboss.kzeaddonfabric.wip;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import java.util.concurrent.CompletableFuture;

public class WidgetValueTargetArgumentType implements ArgumentType<WidgetValueTargetArgumentType.WidgetValueTarget> {

    public static WidgetValueTargetArgumentType operation() {
        return new WidgetValueTargetArgumentType();
    }

    private WidgetValueTargetArgumentType() {}

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        for (WidgetValueTarget target : WidgetValueTarget.values()) {
            builder.suggest(target.toString());
        }
        return builder.buildFuture();
    }

    @Override
    public WidgetValueTarget parse(StringReader reader) throws CommandSyntaxException {
        return WidgetValueTarget.valueOf(reader.readString());
    }

    public static enum WidgetValueTarget {
        X, Y, SCALE, OPACITY
    }
}
