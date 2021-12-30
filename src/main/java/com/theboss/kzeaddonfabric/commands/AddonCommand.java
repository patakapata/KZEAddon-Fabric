package com.theboss.kzeaddonfabric.commands;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.theboss.kzeaddonfabric.KZEAddon;
import com.theboss.kzeaddonfabric.KZEAddonLog;
import com.theboss.kzeaddonfabric.KillLog;
import com.theboss.kzeaddonfabric.commands.arguments.BuiltInWidgetArgumentType;
import com.theboss.kzeaddonfabric.enums.Anchor;
import com.theboss.kzeaddonfabric.render.ChunkInstancedBarrierVisualizer;
import com.theboss.kzeaddonfabric.utils.ModUtils;
import com.theboss.kzeaddonfabric.utils.VanillaUtils;
import com.theboss.kzeaddonfabric.widgets.Offset;
import com.theboss.kzeaddonfabric.widgets.TextWidget;
import com.theboss.kzeaddonfabric.widgets.WidgetRenderer;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.command.argument.TextArgumentType;
import net.minecraft.resource.ResourceManager;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Arrays;
import java.util.List;

import static com.theboss.kzeaddonfabric.KZEAddon.killLog;

public class AddonCommand {

    private static <T> RequiredArgumentBuilder<FabricClientCommandSource, T> a(String name, ArgumentType<T> type) {
        return ClientCommandManager.argument(name, type);
    }

    private static LiteralArgumentBuilder<FabricClientCommandSource> l(String name) {
        return ClientCommandManager.literal(name);
    }

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(
                l("gtek").then(
                        l("debug").then(
                                l("cibv").then(
                                        l("radius").then(
                                                a("value", IntegerArgumentType.integer(0)).executes(ctx -> {
                                                    int radius = IntegerArgumentType.getInteger(ctx, "value");
                                                    ChunkInstancedBarrierVisualizer.INSTANCE.setRadius(radius);
                                                    KZEAddon.options.barrierVisualizeRadius = radius;
                                                    ctx.getSource().sendFeedback(Text.of("CIBV > Radius is " + radius));
                                                    return 0;
                                                })
                                        )
                                ).then(
                                        l("rebuild").executes(ctx -> {
                                            ChunkInstancedBarrierVisualizer.INSTANCE.setShouldRebuild(true);
                                            ctx.getSource().sendFeedback(Text.of("CIBV > Upload queued!"));
                                            return 0;
                                        })
                                ).then(
                                        l("vis_radius").then(
                                                a("value", FloatArgumentType.floatArg(0F)).executes(ctx -> {
                                                    float value = FloatArgumentType.getFloat(ctx, "value");
                                                    KZEAddon.options.barrierFadeRadius = value;
                                                    ctx.getSource().sendFeedback(Text.of("CIBV > Visualize radius is " + value));
                                                    return 0;
                                                })
                                        )
                                ).then(
                                        l("reallocate").executes(ctx -> {
                                            ChunkInstancedBarrierVisualizer.INSTANCE.forceReallocate();
                                            ctx.getSource().sendFeedback(Text.of("CIBV > Chunks reallocate queued"));
                                            return 0;
                                        })
                                ).then(
                                        l("load_model").then(
                                                a("id", IdentifierArgumentType.identifier()).executes(ctx -> {
                                                    Identifier id = ctx.getArgument("id", Identifier.class);
                                                    if (ChunkInstancedBarrierVisualizer.INSTANCE.loadModelFromResource(id)) {
                                                        ctx.getSource().sendFeedback(Text.of("Model `" + id + "` successfully loaded"));
                                                        return 0;
                                                    } else {
                                                        ctx.getSource().sendError(Text.of("Model " + id + " load failed"));
                                                        return -1;
                                                    }
                                                }).suggests((context, builder) -> {
                                                    MinecraftClient mc = MinecraftClient.getInstance();
                                                    ResourceManager resManager = mc.getResourceManager();
                                                    CommandSource.suggestIdentifiers(resManager.findResources("barrier_model", unused -> true), builder);
                                                    return builder.buildFuture();
                                                })
                                        )
                                )
                        ).then(
                                l("log").then(
                                        l("add").then(
                                                a("value", TextArgumentType.text()).executes(ctx -> {
                                                    Text text = ctx.getArgument("value", Text.class);
                                                    KZEAddon.info(text);
                                                    ctx.getSource().sendFeedback(Text.of("Log added [" + VanillaUtils.textAsString(text) + "]"));
                                                    return 0;
                                                })
                                        )
                                ).then(
                                        l("list").executes(ctx -> {
                                            List<KZEAddonLog.Entry> entries = KZEAddon.getModLog().getEntries();
                                            for (int i = 0; i < entries.size(); i++) {
                                                KZEAddonLog.Entry entry = entries.get(i);
                                                KZEAddon.LOGGER.info(i + " > (" + VanillaUtils.textAsString(entry.getTimeText()) + ") " + VanillaUtils.textAsString(entry.getMsg()));
                                            }
                                            return entries.size();
                                        })
                                ).then(
                                        l("clear").executes(ctx -> {
                                            KZEAddon.getModLog().clear();
                                            ctx.getSource().sendFeedback(Text.of("Log is cleared"));
                                            return 0;
                                        })
                                ).then(
                                        l("open").executes(ctx -> {
                                            RenderSystem.recordRenderCall(KZEAddon.getModLog()::openLogScreen);
                                            return 0;
                                        })
                                ).then(
                                        l("toggle").executes(ctx -> {
                                            boolean newState = !KZEAddon.getModLog().isHiding();
                                            KZEAddon.getModLog().setHiding(newState);
                                            ctx.getSource().sendFeedback(Text.of("AddonLog > Log is " + (newState ? "visible" : "invisible") + " now"));
                                            return 0;
                                        })
                                ).then(
                                        l("time").executes(ctx -> {
                                            boolean newState = !KZEAddon.getModLog().isShowTime();
                                            KZEAddon.getModLog().setShowTime(newState);
                                            ctx.getSource().sendFeedback(Text.of("AddonLog > Timestamp is " + (newState ? "visible" : "invisible") + " now"));
                                            return 0;
                                        })
                                )
                        ).then(
                                l("widget").then(
                                        l("add").then(
                                                a("value", TextArgumentType.text()).executes(ctx -> {
                                                    Text text = ctx.getArgument("value", Text.class);
                                                    TextWidget widget = new TextWidget(1.0F, text, new Offset(Anchor.MIDDLE_MIDDLE, 0, 0), Anchor.LEFT_UP, 0xFFFFFF, 0xFF);
                                                    KZEAddon.widgetRenderer.addText(widget);
                                                    ctx.getSource().sendFeedback(Text.of("Widget > Widget added [" + VanillaUtils.textAsString(text) + "]"));
                                                    return 0;
                                                })
                                        )
                                ).then(
                                        l("remove").then(
                                                a("value", IntegerArgumentType.integer(0)).executes(ctx -> {
                                                    int value = IntegerArgumentType.getInteger(ctx, "value");
                                                    if (value > KZEAddon.widgetRenderer.textWidgetCount()) {
                                                        ctx.getSource().sendError(Text.of("Widget > Index outbounds"));
                                                        return -1;
                                                    }
                                                    TextWidget removed = KZEAddon.widgetRenderer.getTextWidget(value);
                                                    KZEAddon.widgetRenderer.remove(value);
                                                    ctx.getSource().sendFeedback(Text.of("Widget > Widget removed [" + VanillaUtils.textAsString((Text) removed) + "]"));
                                                    return 0;
                                                })
                                        )
                                ).then(
                                        l("list").executes(ctx -> {
                                            StringBuilder builder = new StringBuilder();
                                            KZEAddon.widgetRenderer.forEachText(widget -> {
                                                builder.append(VanillaUtils.textAsString(widget.getName()));
                                                builder.append(", ");
                                            });
                                            ctx.getSource().sendFeedback(Text.of(builder.toString()));
                                            return KZEAddon.widgetRenderer.textWidgetCount();
                                        })
                                ).then(
                                        l("save").executes(ctx -> {
                                            KZEAddon.widgetRenderer.save();
                                            ctx.getSource().sendFeedback(Text.of("Widgets > Saved to file"));
                                            return 0;
                                        })
                                ).then(
                                        l("reload").executes(ctx -> {
                                            KZEAddon.widgetRenderer.load();
                                            ctx.getSource().sendFeedback(Text.of("Widgets > Reload from file"));
                                            return 0;
                                        })
                                ).then(
                                        l("edit").then(
                                                l("builtin").then(
                                                        a("name", BuiltInWidgetArgumentType.builtin()).executes(ctx -> {
                                                            WidgetRenderer.BuiltInWidget name = BuiltInWidgetArgumentType.getBuiltInWidget("name", ctx);
                                                            RenderSystem.recordRenderCall(() -> KZEAddon.widgetRenderer.openEditScreen(name));
                                                            return 0;
                                                        })
                                                )
                                        ).then(
                                                l("text").then(
                                                        a("index", IntegerArgumentType.integer(0)).executes(ctx -> {
                                                            int index = IntegerArgumentType.getInteger(ctx, "index");
                                                            RenderSystem.recordRenderCall(() -> {
                                                                WidgetRenderer renderer = KZEAddon.widgetRenderer;
                                                                renderer.openEditScreen(renderer.getTextWidget(index));
                                                            });
                                                            return 0;
                                                        })
                                                )
                                        ).then(
                                                l("custom").then(
                                                        a("index", IntegerArgumentType.integer(0)).executes(ctx -> {
                                                            int index = IntegerArgumentType.getInteger(ctx, "index");
                                                            RenderSystem.recordRenderCall(() -> {
                                                                WidgetRenderer renderer = KZEAddon.widgetRenderer;
                                                                renderer.openEditScreen(renderer.getCustomWidget(index));
                                                            });
                                                            return 0;
                                                        })
                                                )
                                        )
                                )
                        ).then(
                                l("kill_log").then(
                                        l("list").executes(ctx -> {
                                            KZEAddon.info("---------- Kill log entries ----------");
                                            List<KillLog.Entry> list = killLog.getLogs();
                                            for (int i = 0; i < list.size(); i++) {
                                                KillLog.Entry entry = list.get(i);
                                                KZEAddon.info(i + " | [" + entry.getAttacker().getName() + " " + entry.getMark() + " " + entry.getVictim().getName() + "]");
                                            }
                                            KZEAddon.info("---------------------------------");
                                            return 0;
                                        })
                                ).then(
                                        l("add").then(
                                                a("attacker", StringArgumentType.string()).then(
                                                        a("mark", StringArgumentType.string()).then(
                                                                a("victim", StringArgumentType.string()).executes(ctx -> {
                                                                    String attacker = ctx.getArgument("attacker", String.class);
                                                                    String mark = ctx.getArgument("mark", String.class);
                                                                    String victim = ctx.getArgument("victim", String.class);

                                                                    ctx.getSource().sendFeedback(Text.of("KILL LOG > ADD | " + attacker + " " + mark + " " + victim));
                                                                    KZEAddon.killLog.add(new KillLog.Entry(
                                                                            new KillLog.Player(attacker),
                                                                            new KillLog.Player(victim),
                                                                            mark, System.currentTimeMillis() % 2 == 0,
                                                                            MinecraftClient.getInstance()
                                                                    ));
                                                                    return 0;
                                                                })
                                                        )
                                                )
                                        )
                                ).then(
                                        l("clear").executes(ctx -> {
                                            KZEAddon.killLog.clear();
                                            ctx.getSource().sendFeedback(Text.of("KillLog > Log is cleared"));
                                            return 0;
                                        })
                                )
                        ).then(
                                l("scan").executes(ctx -> {
                                    Arrays.stream(ModUtils.scanCommandTree().split("\n")).map(Text::of).forEach(ctx.getSource()::sendFeedback);
                                    return 0;
                                })
                        )
                )
        );
    }

    private AddonCommand() {}
}
