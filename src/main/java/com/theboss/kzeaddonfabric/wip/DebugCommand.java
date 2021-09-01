package com.theboss.kzeaddonfabric.wip;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.theboss.kzeaddonfabric.KZEAddon;
import com.theboss.kzeaddonfabric.KZEAddonLog;
import com.theboss.kzeaddonfabric.events.EventsListener;
import com.theboss.kzeaddonfabric.events.RenderingEventsListener;
import com.theboss.kzeaddonfabric.render.shader.ScanShader;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CommandBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.CommandBlockBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.CommandBlockScreen;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.TextArgumentType;
import net.minecraft.command.argument.Vec3ArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.theboss.kzeaddonfabric.KZEAddon.info;
import static net.minecraft.server.command.CommandManager.*;

@Environment(EnvType.CLIENT)
public class DebugCommand {
    private static final Map<String, Object> debugTable = new HashMap<>();
    public static boolean cibvDebug = false;
    public static int printMoveAmountNumber = 0;
    public static boolean showCIBVChunkStates = false;
    public static BlockPos traceCmdBlockStart = null;
    public static BlockPos traceCmdBlockEnd = null;
    public static List<BlockPos> traceCmdList = new CopyOnWriteArrayList<>();
    public static boolean traceIsLooping = false;

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

    public static boolean containsDebugKey(String key) {
        return debugTable.containsKey(key);
    }

    public static int debug(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        cibvDebug = !cibvDebug;
        sendMsg(ctx, "CIBV > Debug is " + (cibvDebug ? "On" : "Off") + " now");
        return -1;
    }

    @SuppressWarnings("unchecked")
    public static <T> T getDebugTableContent(String name, Class<T> type) {
        Object obj = debugTable.get(name);

        if (obj.getClass().equals(type)) {
            return (T) obj;
        } else {
            return null;
        }
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

    public static Object putDebugTableContent(String name, Object obj) {
        return debugTable.put(name, obj);
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
                                        literal("debug").executes(DebugCommand::debug)
                                ).then(
                                        literal("radius").then(
                                                argument("value", IntegerArgumentType.integer(0))
                                                        .executes(DebugCommand::radius)
                                        )
                                ).then(
                                        literal("model_info").executes(DebugCommand::modelInfo)
                                ).then(
                                        literal("chunks").executes(DebugCommand::chunkOffsets)
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
                                                    ChunkInstancedBarrierVisualizer.INSTANCE.setVisualizeRadius(radius);
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
                        ).then(
                                literal("fb_learn").then(
                                        literal("toggle").executes(ctx -> {
                                            boolean shouldRender = FrameBufferLearn.INSTANCE.isShouldRender();
                                            FrameBufferLearn.INSTANCE.setShouldRender(!shouldRender);
                                            info("FB Learn > " + shouldRender + " => " + !shouldRender);
                                            return 0;
                                        })
                                ).then(
                                        literal("resize").executes(ctx -> {
                                            FrameBufferLearn.INSTANCE.updateTextureSize();
                                            info("FB Learn > Resize queued!");
                                            return 0;
                                        })
                                ).then(
                                        literal("ddd").then(
                                                argument("value", BoolArgumentType.bool()).executes(ctx -> {
                                                    boolean bool = BoolArgumentType.getBool(ctx, "value");
                                                    ScanShader.INSTANCE.isDirectDepthDisplay(bool);
                                                    info("FB Learn > DDD is " + bool + " now!");
                                                    return 0;
                                                })
                                        )
                                ).then(
                                        literal("center").then(
                                                argument("position", Vec3ArgumentType.vec3()).executes(ctx -> {
                                                    Vec3d position = Vec3ArgumentType.getVec3(ctx, "position");
                                                    EventsListener.recordTickTask(() -> {
                                                        sendMsg(ctx, "Center is " + position.toString());
                                                        FrameBufferLearn.INSTANCE.setCenter(position);
                                                    });
                                                    return 0;
                                                })
                                        )
                                ).then(
                                        literal("width").then(
                                                argument("value", FloatArgumentType.floatArg(0.0F)).executes(
                                                        ctx -> {
                                                            float width = FloatArgumentType.getFloat(ctx, "value");
                                                            FrameBufferLearn.INSTANCE.setWidth(width);
                                                            sendMsg(ctx, "Line width is " + width + " now!");
                                                            return 0;
                                                        }
                                                )
                                        ).executes(
                                                ctx -> {
                                                    float width = FrameBufferLearn.INSTANCE.getWidth();
                                                    sendMsg(ctx, "Line width is + " + width);
                                                    return 0;
                                                }
                                        )
                                )
                        )
                ).then(
                        literal("rec").then(
                                argument("position", BlockPosArgumentType.blockPos()).executes(DebugCommand::remoteEditCommand)
                        )
                ).then(
                        literal("open").then(
                                argument("position", BlockPosArgumentType.blockPos()).executes(DebugCommand::open)
                        )
                )
        );
    }

    public static void registerClientCommands(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(
                lit("kaf").then(
                        lit("dev").then(
                                lit("log").then(
                                        arg("count", IntegerArgumentType.integer(1)).executes(
                                                ctx -> {
                                                    int count = ctx.getArgument("count", Integer.class);
                                                    sendFeedback(ctx, count + " message(s) added");
                                                    for (int i = 0; i < count; i++) {
                                                        KZEAddon.MOD_LOG.add(Text.of(String.valueOf(count)));
                                                    }
                                                    return 0;
                                                }
                                        )
                                ).then(
                                        lit("add").then(
                                                arg("message", TextArgumentType.text()).executes(
                                                        ctx -> {
                                                            Text msg = ctx.getArgument("message", Text.class);
                                                            sendFeedback(ctx, "Message 「" + KZEAddon.textAsString(msg) + "」 added");
                                                            KZEAddon.MOD_LOG.add(msg);
                                                            return 0;
                                                        }
                                                )
                                        )
                                ).then(
                                        lit("list").then(
                                                arg("index", IntegerArgumentType.integer(0)).executes(
                                                        ctx -> {
                                                            int index = ctx.getArgument("index", Integer.class);
                                                            KZEAddonLog log = KZEAddon.MOD_LOG;
                                                            if (index >= log.getHistorySize()) return -1;
                                                            Text text = log.get(index);
                                                            sendFeedback(ctx, text);
                                                            return 0;
                                                        }
                                                )
                                        )
                                ).then(
                                        lit("clear").executes(ctx -> {
                                            KZEAddon.MOD_LOG.clear();
                                            KZEAddon.MOD_LOG.add("Mod log is cleared!");
                                            return 0;
                                        })
                                ).then(
                                        lit("open").executes(ctx -> {
                                            RenderSystem.recordRenderCall(() -> KZEAddon.MOD_LOG.openLogScreen());
                                            return 0;
                                        })
                                ).then(
                                        lit("show").executes(ctx -> {
                                            KZEAddon.setShowModLog(true);
                                            return 0;
                                        })
                                ).then(
                                        lit("hide").executes(ctx -> {
                                            KZEAddon.setShowModLog(false);
                                            return 0;
                                        })
                                ).then(
                                        lit("toggle").executes(ctx -> {
                                            KZEAddon.setShowModLog(!KZEAddon.isShowModLog());
                                            return 0;
                                        })
                                ).then(
                                        lit("time").then(
                                                lit("show").executes(ctx -> {
                                                    KZEAddon.MOD_LOG.setShowTime(true);
                                                    info("Time is visible now");
                                                    return 0;
                                                })
                                        ).then(
                                                lit("hide").executes(ctx -> {
                                                    KZEAddon.MOD_LOG.setShowTime(false);
                                                    info("Time is invisible now");
                                                    return 0;
                                                })
                                        )
                                )
                        )
                )
        );
    }

    public static int remoteEditCommand(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        try {
            ServerWorld world = ctx.getSource().getWorld();
            BlockPos pos = BlockPosArgumentType.getBlockPos(ctx, "position");
            traceIsLooping = false;
            traceCmdList.clear();
            traceCommandBlocks(traceCmdList, pos, world);
            traceCmdBlockStart = traceCmdList.stream().min(RenderingEventsListener::compareBlockPos).orElse(null);
            traceCmdBlockEnd = traceCmdList.stream().max(RenderingEventsListener::compareBlockPos).orElse(null);
            if (traceCmdBlockStart != null) {
                info("traceCmdBlock Start = " + traceCmdBlockStart.toShortString());
            }
            if (traceCmdBlockEnd != null) {
                info("traceCmdBlock End = " + traceCmdBlockEnd.toShortString());
            }
        } catch (CommandSyntaxException ex) {
            throw ex;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    @SuppressWarnings("unchecked")
    public static <T> T removeDebugTableContent(String key) {
        if (debugTable.containsKey(key)) {
            return (T) debugTable.remove(key);
        } else {
            return null;
        }
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

    public static List<BlockPos> traceCommandBlocks(BlockPos pos, World world) {
        List<BlockPos> result = new ArrayList<>();
        traceCommandBlocks(result, pos, world);
        return result;
    }

    public static void traceCommandBlocks(List<BlockPos> result, BlockPos pos, World world) {
        BlockState state = world.getBlockState(pos);

        if (isCommandBlock(state)) {
            if (!result.contains(pos)) {
                result.add(pos);
                Direction dir = state.get(CommandBlock.FACING);
                traceCommandBlocks(result, pos.add(dir.getVector()), world);
            } else {
                traceIsLooping = true;
                result.add(pos);
            }
        }
    }

    private DebugCommand() {}
}
