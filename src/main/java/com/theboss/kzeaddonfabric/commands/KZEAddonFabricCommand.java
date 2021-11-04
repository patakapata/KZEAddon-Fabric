package com.theboss.kzeaddonfabric.commands;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.theboss.kzeaddonfabric.KZEAddon;
import com.theboss.kzeaddonfabric.KillLog;
import com.theboss.kzeaddonfabric.VanillaUtils;
import com.theboss.kzeaddonfabric.events.RenderingEventsListener;
import com.theboss.kzeaddonfabric.render.ChunkInstancedBarrierVisualizer;
import com.theboss.kzeaddonfabric.render.widgets.LiteralWidget;
import com.theboss.kzeaddonfabric.render.widgets.Widget;
import com.theboss.kzeaddonfabric.screen.WidgetArrangementScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.CommandBlockBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.CommandBlockScreen;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.command.argument.*;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Iterator;

import static com.theboss.kzeaddonfabric.KZEAddon.info;
import static net.minecraft.server.command.CommandManager.*;

@Environment(EnvType.CLIENT)
public class KZEAddonFabricCommand {
    public static boolean cibvDebug = false;
    public static int printMoveAmountNumber = 0;
    public static boolean showCIBVChunkStates = false;
    public static final SuggestionProvider<FabricClientCommandSource> SUGGEST_PROVIDER = (ctx, builder) -> {
        builder.suggest("entry1", Text.of("Tooltip-1"));
        builder.suggest("2entry", Text.of("2-Tooltip"));
        builder.suggest("ent-3-ry", Text.of("Tool-3-tip"));
        builder.suggest("ENTRY_4", Text.of("TOOLTIP_5"));
        return builder.buildFuture();
    };

    private static RequiredArgumentBuilder<FabricClientCommandSource, ?> arg(String name, ArgumentType<?> type) {
        return ClientCommandManager.argument(name, type);
    }

    public static int chunkOffsets(CommandContext<ServerCommandSource> ctx) {
        Iterator<ChunkInstancedBarrierVisualizer.Chunk> itr = ChunkInstancedBarrierVisualizer.chunkIterator();
        int i = 0;
        while (itr.hasNext()) {
            info(String.format("%3d", (i++)) + " > " + itr.next().getOffset().toShortString());
        }
        return 0;
    }

    public static int debug(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        cibvDebug = !cibvDebug;
        sendMsg(ctx, "CIBV > Debug is " + (cibvDebug ? "On" : "Off") + " now");
        return -1;
    }

    public static boolean isCommandBlock(BlockState state) {
        Block block = state.getBlock();
        return block.equals(Blocks.COMMAND_BLOCK) || block.equals(Blocks.CHAIN_COMMAND_BLOCK) || block.equals(Blocks.REPEATING_COMMAND_BLOCK);
    }

    private static LiteralArgumentBuilder<FabricClientCommandSource> lit(String name) {
        return ClientCommandManager.literal(name);
    }

    public static int modelInfo(CommandContext<ServerCommandSource> ctx) {
        sendMsg(ctx, "<========> Model Info <==========>");
        sendMsg(ctx, "Vertices: " + ChunkInstancedBarrierVisualizer.INSTANCE.getModelVertices());
        sendMsg(ctx, "<============================>");
        return 0;
    }

    private static int open(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerCommandSource source = ctx.getSource();
        BlockPos pos = BlockPosArgumentType.getBlockPos(ctx, "position");
        World world = source.getWorld();

        if (openCommandEdit(world, pos)) {
            info("正常に開きました");
        } else {
            info("開くのに失敗しました");
        }
        return 0;
    }

    public static boolean openCommandEdit(World world, BlockPos pos) {
        try {
            info("Trying open the screen...");
            BlockState state = world.getBlockState(pos);
            Block block = state.getBlock();

            if (block.equals(Blocks.COMMAND_BLOCK) || block.equals(Blocks.CHAIN_COMMAND_BLOCK) || block.equals(Blocks.REPEATING_COMMAND_BLOCK)) {
                MinecraftClient mc = MinecraftClient.getInstance();
                BlockEntity blEntity = world.getBlockEntity(pos);
                if (blEntity == null) {
                    info(pos.toShortString() + " isn't command block!");
                    return false;
                }

                CommandBlockBlockEntity blockEntity = (CommandBlockBlockEntity) blEntity;
                info("Command: " + blockEntity.getCommandExecutor().getCommand());
                CommandBlockScreen screen = new CommandBlockScreen(blockEntity);
                //                mc.send(screen::updateCommandBlock);
                RenderSystem.recordRenderCall(screen::updateCommandBlock);
                mc.openScreen(screen);
            }
            info("Open successfully");
        } catch (Exception ex) {
            ex.printStackTrace();
            info("Open failed");
            return false;
        } finally {
            info("Process is ended");
        }
        return true;
    }

    public static int radius(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        int newRadius = IntegerArgumentType.getInteger(ctx, "value");
        int oldRadius = ChunkInstancedBarrierVisualizer.INSTANCE.getRadius();
        ChunkInstancedBarrierVisualizer.INSTANCE.setRadius(newRadius);
        sendMsg(ctx, "CIBV > Radius is " + newRadius + " from " + oldRadius + " now");
        return -1;
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                literal("kzeaddon").then(
                        literal("debug").then(
                                literal("cibv").then(
                                        literal("debug").executes(KZEAddonFabricCommand::debug)
                                ).then(
                                        literal("radius").then(
                                                argument("value", IntegerArgumentType.integer(0))
                                                        .executes(KZEAddonFabricCommand::radius)
                                        )
                                ).then(
                                        literal("model_info").executes(KZEAddonFabricCommand::modelInfo)
                                ).then(
                                        literal("chunks").executes(KZEAddonFabricCommand::chunkOffsets)
                                ).then(
                                        literal("rebuild").executes(ctx -> {
                                            ChunkInstancedBarrierVisualizer.INSTANCE.setShouldRebuild(true);
                                            sendMsg(ctx, "All chunks rebuild queued!");
                                            return 0;
                                        })
                                ).then(
                                        literal("visRadius").then(
                                                argument("value", FloatArgumentType.floatArg(0.0F)).executes(ctx -> {
                                                    float radius = FloatArgumentType.getFloat(ctx, "value");
                                                    KZEAddon.options.barrierVisualizeShowRadius = radius;
                                                    sendMsg(ctx, "CIBV > Visualize radius is " + radius + " now");
                                                    return 0;
                                                })
                                        )
                                ).then(
                                        literal("alphaThreshold").then(
                                                argument("value", FloatArgumentType.floatArg(0.0F, 1.0F)).executes(ctx -> {
                                                    float threshold = FloatArgumentType.getFloat(ctx, "value");
                                                    ChunkInstancedBarrierVisualizer.INSTANCE.setAlphaThreshold(threshold);
                                                    sendMsg(ctx, "CIBV > Alpha threshold is " + threshold + " now");
                                                    return 0;
                                                })
                                        )
                                ).then(
                                        literal("print").executes(ctx -> {
                                            printMoveAmountNumber += 10;
                                            return 0;
                                        })
                                ).then(
                                        literal("reallocate").executes(ctx -> {
                                            ChunkInstancedBarrierVisualizer.INSTANCE.forceReallocate();
                                            sendMsg(ctx, "All chunks are force reallocated!");
                                            return 0;
                                        })
                                ).then(
                                        literal("toggleShowState").executes(ctx -> {
                                            showCIBVChunkStates = !showCIBVChunkStates;
                                            sendMsg(ctx, "Show states: " + showCIBVChunkStates);
                                            return 0;
                                        })
                                )
                        )
                ).then(
                        literal("open").then(
                                argument("position", BlockPosArgumentType.blockPos()).executes(KZEAddonFabricCommand::open)
                        )
                )
        );
    }

    public static void registerClientCommands(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        LiteralCommandNode<FabricClientCommandSource> kaf = dispatcher.register(
                lit("kaf").then(
                        lit("dev").then(
                                lit("log").then(
                                        arg("count", IntegerArgumentType.integer(1)).executes(
                                                ctx -> {
                                                    int count = ctx.getArgument("count", Integer.class);
                                                    sendFeedback(ctx, count + " message(s) added");
                                                    for (int i = 0; i < count; i++) {
                                                        KZEAddon.getModLog().add(Text.of(String.valueOf(i)));
                                                    }
                                                    return 0;
                                                }
                                        )
                                ).then(
                                        lit("add").then(
                                                arg("message", TextArgumentType.text()).executes(
                                                        ctx -> {
                                                            Text msg = ctx.getArgument("message", Text.class);
                                                            sendFeedback(ctx, "Message 「" + VanillaUtils.textAsString(msg) + "」 added");
                                                            KZEAddon.getModLog().add(msg);
                                                            return 0;
                                                        }
                                                )
                                        )
                                ).then(
                                        lit("list").then(
                                                arg("index", IntegerArgumentType.integer(0)).executes(
                                                        ctx -> {
                                                            int index = ctx.getArgument("index", Integer.class);
                                                            if (index >= KZEAddon.getModLog().getHistorySize()) return -1;
                                                            Text text = KZEAddon.getModLog().get(index);
                                                            sendFeedback(ctx, text);
                                                            return 0;
                                                        }
                                                )
                                        )
                                ).then(
                                        lit("clear").executes(ctx -> {
                                            KZEAddon.getModLog().clear();
                                            KZEAddon.getModLog().add("Mod log is cleared!");
                                            return 0;
                                        })
                                ).then(
                                        lit("open").executes(ctx -> {
                                            RenderSystem.recordRenderCall(KZEAddon.getModLog()::openLogScreen);
                                            return 0;
                                        })
                                ).then(
                                        lit("show").executes(ctx -> {
                                            KZEAddon.getModLog().setHiding(false);
                                            return 0;
                                        })
                                ).then(
                                        lit("hide").executes(ctx -> {
                                            KZEAddon.getModLog().setHiding(true);
                                            return 0;
                                        })
                                ).then(
                                        lit("time").then(
                                                lit("show").executes(ctx -> {
                                                    KZEAddon.getModLog().setShowTime(true);
                                                    info("Time is visible now");
                                                    return 0;
                                                })
                                        ).then(
                                                lit("hide").executes(ctx -> {
                                                    KZEAddon.getModLog().setShowTime(false);
                                                    info("Time is invisible now");
                                                    return 0;
                                                })
                                        ).executes(ctx -> {
                                            sendError(ctx, "/kaf dev log time [ show / hide ]");
                                            return 0;
                                        })
                                ).executes(ctx -> {
                                    sendError(ctx, "/kaf dev log [ <count> / add / list / clear / open / show / hide / time]");
                                    return 0;
                                })
                        ).then(
                                lit("widget").then(
                                        lit("add").then(
                                                arg("text", TextArgumentType.text()).executes(ctx -> {
                                                    Text text = ctx.getArgument("text", Text.class);
                                                    LiteralWidget literalWidget = new LiteralWidget(text);
                                                    KZEAddon.widgetRenderer.addLiteral(literalWidget);
                                                    sendFeedback(ctx, "LiteralWidget [" + VanillaUtils.textAsString(text) + "] is registered");
                                                    return 1;
                                                })
                                        )
                                ).then(
                                        lit("remove").then(
                                                arg("index", IntegerArgumentType.integer(0)).executes(ctx -> {
                                                    int index = ctx.getArgument("index", Integer.class);
                                                    if (index >= KZEAddon.widgetRenderer.literalSize()) {
                                                        sendError(ctx, "Index " + index + " is out of bounds!");
                                                        return -1;
                                                    }
                                                    LiteralWidget removed = KZEAddon.widgetRenderer.getLiteral(index);
                                                    KZEAddon.widgetRenderer.removeLiteral(removed);
                                                    sendFeedback(ctx, "Index " + index + " [" + VanillaUtils.textAsString(removed.getText()) + "] is removed");
                                                    return 1;
                                                })
                                        )
                                ).then(
                                        lit("list").executes(ctx -> {
                                            StringBuilder builder = new StringBuilder();
                                            KZEAddon.widgetRenderer.forEachLiteral(widget -> {
                                                builder.append(VanillaUtils.textAsString(widget.getText()));
                                                builder.append(", ");
                                            });
                                            sendFeedback(ctx, builder.toString());
                                            return KZEAddon.widgetRenderer.literalSize();
                                        })
                                ).then(
                                        lit("edit").then(
                                                arg("index", IntegerArgumentType.integer(0)).executes(ctx -> {
                                                    int index = ctx.getArgument("index", Integer.class);
                                                    if (index >= KZEAddon.widgetRenderer.literalSize()) {
                                                        sendError(ctx, "Index " + index + " is out of bounds!");
                                                        return -1;
                                                    }
                                                    Widget widget = KZEAddon.widgetRenderer.getLiteral(index);
                                                    WidgetArrangementScreen screen = new WidgetArrangementScreen(widget);
                                                    RenderSystem.recordRenderCall(() -> screen.open(MinecraftClient.getInstance()));
                                                    sendFeedback(ctx, "Widget [" + VanillaUtils.textAsString(widget.getText()) + "] arrange screen opened");
                                                    return 1;
                                                })
                                        ).then(
                                                lit("primary").executes(ctx -> {
                                                    RenderSystem.recordRenderCall(() -> KZEAddon.widgetRenderer.openArrangementScreen("primary"));
                                                    sendFeedback(ctx, "Primary weapon widget arrangement screen opened");
                                                    return 1;
                                                })
                                        ).then(
                                                lit("secondary").executes(ctx -> {
                                                    RenderSystem.recordRenderCall(() -> KZEAddon.widgetRenderer.openArrangementScreen("secondary"));
                                                    sendFeedback(ctx, "Secondary weapon widget arrangement screen opened");
                                                    return 1;
                                                })
                                        ).then(
                                                lit("melee").executes(ctx -> {
                                                    RenderSystem.recordRenderCall(() -> KZEAddon.widgetRenderer.openArrangementScreen("melee"));
                                                    sendFeedback(ctx, "Melee weapon widget arrangement screen opened");
                                                    return 1;
                                                })
                                        ).then(
                                                lit("reload_time").executes(ctx -> {
                                                    RenderSystem.recordRenderCall(() -> KZEAddon.widgetRenderer.openArrangementScreen("reload_time"));
                                                    sendFeedback(ctx, "Reload time widget arrangement screen opened");
                                                    return 1;
                                                })
                                        ).then(
                                                lit("total_ammo").executes(ctx -> {
                                                    RenderSystem.recordRenderCall(() -> KZEAddon.widgetRenderer.openArrangementScreen("total_ammo"));
                                                    sendFeedback(ctx, "Total ammo widget arrangement screen opened");
                                                    return 1;
                                                })
                                        )
                                ).then(
                                        lit("reload").executes(ctx -> {
                                            KZEAddon.widgetRenderer.load();
                                            sendFeedback(ctx, "Widgets is reloaded from file");
                                            return 0;
                                        })
                                ).then(
                                        lit("save").executes(ctx -> {
                                            KZEAddon.widgetRenderer.save();
                                            sendFeedback(ctx, "Widgets is saved to file");
                                            return 0;
                                        })
                                ).executes(ctx -> {
                                    sendError(ctx, "/kaf dev widget [ add / remove / list / edit / reload / save ]");
                                    return 0;
                                })
                        ).then(
                                lit("kill_log").then(
                                        lit("list").executes(ctx -> {
                                            return 0;
                                        })
                                ).then(
                                        lit("add").then(
                                                arg("attacker", StringArgumentType.string()).then(
                                                        arg("mark", StringArgumentType.string()).then(
                                                                arg("victim", StringArgumentType.string()).executes(ctx -> {
                                                                    String attacker = ctx.getArgument("attacker", String.class);
                                                                    String mark = ctx.getArgument("mark", String.class);
                                                                    String victim = ctx.getArgument("victim", String.class);

                                                                    sendFeedback(ctx, "KILL LOG > ADD | " + attacker + " " + mark + " " + victim);
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
                                        lit("remove")
                                ).executes(ctx -> {
                                    sendError(ctx, "/kaf dev kill_log [ list / add / remove ]");
                                    return 0;
                                })
                        ).executes(ctx -> {
                            sendError(ctx, "/kaf dev [ log / widget / kill_log ]");
                            return 0;
                        })
                ).executes(ctx -> {
                    sendError(ctx, "/kaf [ dev ]");
                    return 0;
                })
        );
        dispatcher.register(lit("kzeaddon-fabric").redirect(kaf));
    }

    private static void sendError(CommandContext<FabricClientCommandSource> ctx, String msg) {
        sendError(ctx.getSource(), Text.of("[KzeAddonFabric] " + msg));
    }

    private static void sendError(CommandContext<FabricClientCommandSource> ctx, Text msg) {
        sendError(ctx.getSource(), msg);
    }

    private static void sendError(FabricClientCommandSource source, Text msg) {
        source.sendError(msg);
    }

    private static void sendFeedback(CommandContext<FabricClientCommandSource> ctx, String msg) {
        sendFeedback(ctx.getSource(), Text.of("[KzeAddonFabric] " + msg));
    }

    private static void sendFeedback(CommandContext<FabricClientCommandSource> ctx, Text msg) {
        sendFeedback(ctx.getSource(), msg);
    }

    private static void sendFeedback(FabricClientCommandSource source, Text msg) {
        source.sendFeedback(msg);
    }

    public static void sendMsg(CommandContext<ServerCommandSource> ctx, String msg) {
        ctx.getSource().sendFeedback(Text.of(msg), false);
    }

    private KZEAddonFabricCommand() {}
}
