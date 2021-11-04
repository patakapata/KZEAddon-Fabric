package com.theboss.kzeaddonfabric.events;

import com.theboss.kzeaddonfabric.KZEAddon;
import com.theboss.kzeaddonfabric.KeyBindings;
import com.theboss.kzeaddonfabric.render.ChunkInstancedBarrierVisualizer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.profiler.Profiler;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

public class EventsListener {
    public static final Queue<Runnable> onTickTasks = new ArrayDeque<>();
    private static long lastPollQueueTime = System.currentTimeMillis();

    /**
     * クライアントが終了するときに実行
     *
     * @param client マインクラフトのクライアントのインスタンス
     */
    @SuppressWarnings("unused")
    public static void onClientStop(MinecraftClient client) {
        RenderingEventsListener.onClose();
        KZEAddon.options.save();
        KZEAddon.widgetRenderer.save();
        KZEAddon.stats.save();
    }

    public static void onGetLeftTextTail(List<String> list) {
    }

    public static void onGetRightTextTail(List<String> list) {
        boolean shouldRebuild = ChunkInstancedBarrierVisualizer.INSTANCE.isShouldRebuild();
        boolean forceRebuild = ChunkInstancedBarrierVisualizer.INSTANCE.isLastShouldRebuild();

        long pollQueueTime = ChunkInstancedBarrierVisualizer.INSTANCE.getLastPollQueueTime();

        list.add("");
        list.add("CIBV > ");
        list.add("Last Poll Queue - " + (-lastPollQueueTime + pollQueueTime) + " ms ago");
        list.add("Should rebuild - " + (shouldRebuild ? "§a" : "§c") + shouldRebuild);
        list.add("Force rebuild - " + (forceRebuild ? "§a" : "§c") + forceRebuild);
        list.add("Diameter - " + ChunkInstancedBarrierVisualizer.INSTANCE.getLastDiameter());
        list.add("Move XYZ - " + ChunkInstancedBarrierVisualizer.INSTANCE.getLastMoveX() + ", " + ChunkInstancedBarrierVisualizer.INSTANCE.getLastMoveY() + ", " + ChunkInstancedBarrierVisualizer.INSTANCE.getLastMoveZ());

        lastPollQueueTime = pollQueueTime;
    }

    /**
     * エンティティの発光色を取得する時に発生するイベント
     *
     * @param entity Target entity
     */
    public static int onGetTeamColorValue(Entity entity) {
        AbstractTeam team = entity.getScoreboardTeam();
        if (KZEAddon.getPriorityGlowPlayers().contains(entity.getUuid())) {
            return KZEAddon.options.priorityGlowColor.get();
        } else if (team != null) {
            String name = team.getName();
            if (name.equals("e")) {
                return KZEAddon.options.humanGlowColor.get();
            } else if (name.equals("z")) {
                return KZEAddon.options.zombieGlowColor.get();
            }
        }
        return -1;
    }

    /**
     * チック処理が始まる前に発生するイベント
     */
    public static void onTick(MinecraftClient client) {
        Profiler profiler = client.getProfiler();
        ClientPlayerEntity player = client.player;
        if (player == null) return;

        profiler.push("KZE Information tick");
        KZEAddon.kzeInfo.tick();
        profiler.swap("Keys tick");
        KeyBindings.tickKeys();
        profiler.swap("Barrier Visualizer tick");

        HitResult hitResult = player.raycast(KZEAddon.options.barrierVisualizeRaycastDistance, 0, false);
        if (KZEAddon.options.barrierVisualizeUseCrosshairCenter && hitResult.getType() == HitResult.Type.BLOCK) {
            BlockPos pos = ((BlockHitResult) hitResult).getBlockPos();
            ChunkInstancedBarrierVisualizer.INSTANCE.setCenter(MathHelper.floor(pos.getX() / 16F), MathHelper.floor(pos.getY() / 16F), MathHelper.floor(pos.getZ() / 16F));
            ChunkInstancedBarrierVisualizer.INSTANCE.setVisualizeCenter(hitResult.getPos());
        } else {
            ChunkInstancedBarrierVisualizer.INSTANCE.setCenter(player.chunkX, player.chunkY, player.chunkZ);
            ChunkInstancedBarrierVisualizer.INSTANCE.setVisualizeCenter(player.getPos());
        }

        ChunkInstancedBarrierVisualizer.INSTANCE.tick();
        profiler.swap("Poll all tick tasks");
        Runnable task;
        while ((task = onTickTasks.poll()) != null) {
            task.run();
        }
        KZEAddon.getModLog().tick();
        KZEAddon.killLog.tick();
        profiler.pop();
    }

    /**
     * チック処理をするスレッドで実行したいタスクを記録する
     *
     * @param task チック処理のスレッドで実行したいタスク
     */
    public static void recordTickTask(Runnable task) {
        onTickTasks.add(task);
    }

    public static String vecToDisplay(float vec) {
        if (Math.abs(vec) < 0.001) {
            return "§7±0";
        } else if (vec > 0) {
            return String.format("§a+%3.3f", vec);
        } else {
            return String.format("§c%3.3f", vec);
        }
    }
}
