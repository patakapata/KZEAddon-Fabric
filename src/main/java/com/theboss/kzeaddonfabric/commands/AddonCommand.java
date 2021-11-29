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
import com.theboss.kzeaddonfabric.commands.arguments.DirectionArgumentType;
import com.theboss.kzeaddonfabric.events.RenderingEventsListener;
import com.theboss.kzeaddonfabric.render.ChunkInstancedBarrierVisualizer;
import com.theboss.kzeaddonfabric.screen.WidgetArrangementScreen;
import com.theboss.kzeaddonfabric.utils.VanillaUtils;
import com.theboss.kzeaddonfabric.widgets.LiteralWidget;
import com.theboss.kzeaddonfabric.widgets.Widget;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.argument.PosArgument;
import net.minecraft.command.argument.TextArgumentType;
import net.minecraft.command.argument.Vec3ArgumentType;
import net.minecraft.text.Text;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.List;

import static com.theboss.kzeaddonfabric.KZEAddon.killLog;

public class AddonCommand {

    private AddonCommand() {}

    private static LiteralArgumentBuilder<FabricClientCommandSource> l(String name) {
        return ClientCommandManager.literal(name);
    }

    private static <T> RequiredArgumentBuilder<FabricClientCommandSource, T> a(String name, ArgumentType<T> type) {
        return ClientCommandManager.argument(name, type);
    }

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(
                l("gtek").then(
                        l("debug").then(
                                l("holo_wall").then(
                                        l("position").then(
                                                a("pos", Vec3ArgumentType.vec3()).executes(ctx -> {
                                                    // PASS Set new position
                                                    Vec3d newPos = VanillaUtils.toAbsoluteCoordinate(ctx, ctx.getArgument("pos", PosArgument.class));
                                                    Vec3d old = RenderingEventsListener.holoWall.getPosition();
                                                    RenderingEventsListener.holoWall.setPosition(newPos);
                                                    ctx.getSource().sendFeedback(Text.of("Position > [" + VanillaUtils.toShortString(old) + "] -> [" + VanillaUtils.toShortString(newPos) + "]"));
                                                    return 0;
                                                })
                                        ).executes(ctx -> {
                                            // PASS Get current position
                                            ctx.getSource().sendFeedback(Text.of("Position > " + VanillaUtils.toShortString(RenderingEventsListener.holoWall.getPosition())));
                                            return 0;
                                        })
                                ).then(
                                        l("direction").then(
                                                // PASS Set new direction
                                                a("dir", DirectionArgumentType.direction()).executes(ctx -> {
                                                    Direction newDir = DirectionArgumentType.getDirection("dir", ctx);
                                                    Direction old = RenderingEventsListener.holoWall.getDirection();
                                                    RenderingEventsListener.holoWall.setDirection(newDir);
                                                    ctx.getSource().sendFeedback(Text.of("Direction > [" + old.toString() + "] -> [" + newDir.toString() + "]"));
                                                    return 0;
                                                })
                                        ).executes(ctx -> {
                                            // PASS Get current direction
                                            ctx.getSource().sendFeedback(Text.of("Direction > " + RenderingEventsListener.holoWall.getDirection().toString()));
                                            return 0;
                                        })
                                ).then(
                                        l("size").then(
                                                // TODO Need verify
                                                // Set new size
                                                a("value", FloatArgumentType.floatArg(0)).executes(ctx -> {
                                                    float value = FloatArgumentType.getFloat(ctx, "value");
                                                    float oldValue = RenderingEventsListener.holoWall.getSize();
                                                    RenderingEventsListener.holoWall.setSize(value);
                                                    ctx.getSource().sendFeedback(Text.of(String.format("Size > %.2f -> %.2f", oldValue, value)));
                                                    return 0;
                                                })
                                        ).executes(ctx -> {
                                            // PASS Get current size
                                            ctx.getSource().sendFeedback(Text.of(String.format("Size > %.2f", RenderingEventsListener.holoWall.getSize())));
                                            return 0;
                                        })
                                )
                        ).then(
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
                                                    KZEAddon.options.barrierVisualizeShowRadius = value;
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
                                )
                        ).then(
                                l("log").then(
                                        l("add").then(
                                                a("value", TextArgumentType.text()).executes(ctx -> {
                                                    Text text = ctx.getArgument("value", Text.class);
                                                    KZEAddon.getModLog().add(text);
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
                                                    LiteralWidget widget = new LiteralWidget(text);
                                                    KZEAddon.widgetRenderer.addLiteral(widget);
                                                    ctx.getSource().sendFeedback(Text.of("Widget > Widget added [" + VanillaUtils.textAsString(text) + "]"));
                                                    return 0;
                                                })
                                        )
                                ).then(
                                        l("remove").then(
                                                a("value", IntegerArgumentType.integer(0)).executes(ctx -> {
                                                    int value = IntegerArgumentType.getInteger(ctx, "value");
                                                    if (value > KZEAddon.widgetRenderer.literalSize()) {
                                                        ctx.getSource().sendError(Text.of("Widget > Index outbounds"));
                                                        return -1;
                                                    }
                                                    LiteralWidget removed = KZEAddon.widgetRenderer.getLiteral(value);
                                                    KZEAddon.widgetRenderer.removeLiteral(value);
                                                    ctx.getSource().sendFeedback(Text.of("Widget > Widget removed [" + VanillaUtils.textAsString((Text) removed) + "]"));
                                                    return 0;
                                                })
                                        )
                                ).then(
                                        l("list").executes(ctx -> {
                                            StringBuilder builder = new StringBuilder();
                                            KZEAddon.widgetRenderer.forEachLiteral(widget -> {
                                                builder.append(VanillaUtils.textAsString(widget.getText()));
                                                builder.append(", ");
                                            });
                                            ctx.getSource().sendFeedback(Text.of(builder.toString()));
                                            return KZEAddon.widgetRenderer.literalSize();
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
                                                a("index", IntegerArgumentType.integer(0)).executes(ctx -> {
                                                    int index = ctx.getArgument("index", Integer.class);
                                                    if (index >= KZEAddon.widgetRenderer.literalSize()) {
                                                        ctx.getSource().sendError(Text.of("Index " + index + " is out of bounds!"));
                                                        return -1;
                                                    }
                                                    Widget widget = KZEAddon.widgetRenderer.getLiteral(index);
                                                    WidgetArrangementScreen screen = new WidgetArrangementScreen(widget);
                                                    RenderSystem.recordRenderCall(() -> screen.open(MinecraftClient.getInstance()));
                                                    ctx.getSource().sendFeedback(Text.of("Widget [" + VanillaUtils.textAsString(widget.getText()) + "] arrange screen opened"));
                                                    return 1;
                                                })
                                        ).then(
                                                l("primary").executes(ctx -> {
                                                    RenderSystem.recordRenderCall(() -> KZEAddon.widgetRenderer.openArrangementScreen("primary"));
                                                    ctx.getSource().sendFeedback(Text.of("Primary weapon widget arrangement screen opened"));
                                                    return 1;
                                                })
                                        ).then(
                                                l("secondary").executes(ctx -> {
                                                    RenderSystem.recordRenderCall(() -> KZEAddon.widgetRenderer.openArrangementScreen("secondary"));
                                                    ctx.getSource().sendFeedback(Text.of("Secondary weapon widget arrangement screen opened"));
                                                    return 1;
                                                })
                                        ).then(
                                                l("melee").executes(ctx -> {
                                                    RenderSystem.recordRenderCall(() -> KZEAddon.widgetRenderer.openArrangementScreen("melee"));
                                                    ctx.getSource().sendFeedback(Text.of("Melee weapon widget arrangement screen opened"));
                                                    return 1;
                                                })
                                        ).then(
                                                l("reload_time").executes(ctx -> {
                                                    RenderSystem.recordRenderCall(() -> KZEAddon.widgetRenderer.openArrangementScreen("reload_time"));
                                                    ctx.getSource().sendFeedback(Text.of("Reload time widget arrangement screen opened"));
                                                    return 1;
                                                })
                                        ).then(
                                                l("total_ammo").executes(ctx -> {
                                                    RenderSystem.recordRenderCall(() -> KZEAddon.widgetRenderer.openArrangementScreen("total_ammo"));
                                                    ctx.getSource().sendFeedback(Text.of("Total ammo widget arrangement screen opened"));
                                                    return 1;
                                                })
                                        )
                                )
                        ).then(
                                l("kill_log").then(
                                        l("list").executes(ctx -> {
                                            KZEAddon.getModLog().info("---------- Kill log entries ----------");
                                            List<KillLog.Entry> list = killLog.getLogs();
                                            for (int i = 0; i < list.size(); i++) {
                                                KillLog.Entry entry = list.get(i);
                                                KZEAddon.getModLog().info(i + " | [" + entry.getAttacker().getName() + " " + entry.getMark() + " " + entry.getVictim().getName() + "]");
                                            }
                                            KZEAddon.getModLog().info("---------------------------------");
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
                        )
                )
        );
    }
}
