package com.theboss.kzeaddonfabric.events.listeners;

import com.theboss.kzeaddonfabric.KZEAddon;
import com.theboss.kzeaddonfabric.KeyBindings;
import com.theboss.kzeaddonfabric.Options;
import com.theboss.kzeaddonfabric.wip.BlockEventListener;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.profiler.Profiler;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class EventsListener {
    public static final Queue<Runnable> onTickTasks = new ArrayDeque<>();
    public static final List<BlockEventListener> listeners = new ArrayList<>();

    public static void onBlockUpdate(BlockPos pos, BlockState state) {
        KZEAddon.getBarrierVisualizer().update(pos);
        listeners.forEach(listener -> listener.handle(pos, state));
    }

    /**
     * クライアントが終了するときに実行
     *
     * @param client マインクラフトのクライアントのインスタンス
     */
    public static void onClientStop(MinecraftClient client) {
        RenderingEventsListener.onClose();
        KZEAddon.getOptions().barrierModel = KZEAddon.getBarrierVisualizer().getModelFile();
        KZEAddon.getOptions().save();
        KZEAddon.getWidgetRenderer().save();
        KZEAddon.getStats().save();
    }

    public static void onGetLeftTextTail(List<String> list) {}

    public static void onGetRightTextTail(List<String> list) {}

    /**
     * エンティティの発光色を取得する時に発生するイベント
     *
     * @param entity Target entity
     */
    public static int onGetTeamColorValue(Entity entity) {
        AbstractTeam team = entity.getScoreboardTeam();
        Options options = KZEAddon.getOptions();
        if (KZEAddon.getObsessions().contains(entity.getUuid())) {
            return options.obsessionGlowColor.get();
        } else if (team != null) {
            String name = team.getName();
            if (name.equals("e")) {
                return options.humanGlowColor.get();
            } else if (name.equals("z")) {
                return options.zombieGlowColor.get();
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
        KZEAddon.getKZEInfo().tick();
        profiler.swap("Keys tick");
        KeyBindings.tickKeys();
        profiler.swap("Barrier Visualizer tick");
        KZEAddon.getBarrierVisualizer().tick();
        profiler.swap("Poll all tick tasks");
        Runnable task;
        while ((task = onTickTasks.poll()) != null) {
            task.run();
        }
        KZEAddon.getModLog().tick();
        KZEAddon.getKillLog().tick();
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
