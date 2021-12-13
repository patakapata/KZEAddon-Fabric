package com.theboss.kzeaddonfabric.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public abstract class EnumArgumentType<T> implements ArgumentType<T> {
    protected List<T> list;

    protected EnumArgumentType() {
        this(new ArrayList<>());
    }

    protected EnumArgumentType(List<T> list) {
        this.list = list;
    }

    @Override
    public T parse(StringReader reader) throws CommandSyntaxException {
        String str = reader.readString();
        return this.search(str);
    }

    protected T search(String name) {
        name = name.toUpperCase();

        for (T tmp : this.list) {
            if (tmp.toString().toUpperCase().equals(name)) return tmp;
        }

        return this.list.isEmpty() ? null : this.list.get(0);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> ctx, SuggestionsBuilder builder) {
        if (ctx.getInput().endsWith(" ")) {
            for (T tmp : this.list)
                builder.suggest(tmp.toString().toUpperCase());
            return builder.buildFuture();
        }

        String[] input = ctx.getInput().split(" ");
        String last = input[input.length - 1].toUpperCase();

        for (T tmp : this.list) {
            String name = tmp.toString().toUpperCase();
            if (name.startsWith(last))
                builder.suggest(name);
        }
        return builder.buildFuture();
    }
}
