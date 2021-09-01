package com.theboss.kzeaddonfabric.events;

import com.theboss.kzeaddonfabric.KZEAddon;
import com.theboss.kzeaddonfabric.render.VBOWrapperRegistry;
import com.theboss.kzeaddonfabric.render.shader.BarrierShader;
import com.theboss.kzeaddonfabric.render.shader.InvertShader;
import com.theboss.kzeaddonfabric.render.shader.ParticleShader;
import com.theboss.kzeaddonfabric.render.shader.ScanShader;
import com.theboss.kzeaddonfabric.wip.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.math.Vec3f;
import net.minecraft.util.profiler.Profiler;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

public class EventsListener {
    public static final Queue<Runnable> onTickTasks = new ArrayDeque<>();
    public static int maxMultiTex;
    private static long lastPollQueueTime = System.currentTimeMillis();

    /**
     * Private rendering system
     *
     * @param client A minecraft client instance
     */
    @SuppressWarnings("unused")
    public static void onClientStop(MinecraftClient client) {
        // KZEAddon.BAR_VISUALIZER.destroy();
        VBOWrapperRegistry.destroyWrappers();
        KZEAddon.saveConfig();
        // TODO Shader delete
        InvertShader.INSTANCE.close();
        FrameBufferLearn.INSTANCE.close();
        ScanShader.INSTANCE.close();
        ParticleShader.INSTANCE.close();
        InstancedPerlinParticleManager.INSTANCE.close();
        BarrierShader.INSTANCE.close();
        // InstancedBarrierVisualizer.INSTANCE.close();
        ChunkInstancedBarrierVisualizer.INSTANCE.close();
    }

    public static void onGetLeftTextTail(CallbackInfoReturnable<List<String>> cir) {
        List<String> list = cir.getReturnValue();

        Vec3f prevCenter = FrameBufferLearn.INSTANCE.getPrevCenter();
        Vec3f currentCenter = FrameBufferLearn.INSTANCE.getCenter();
        Vec3f diff = new Vec3f(currentCenter.getX(), currentCenter.getY(), currentCenter.getZ());
        diff.subtract(prevCenter);
        list.add("");
        list.add("Depth Circle >");
        list.add(String.format("Previous Center - %.3f / %.3f / %.3f", prevCenter.getX(), prevCenter.getY(), prevCenter.getZ()));
        list.add(String.format("Current Center  - %.3f / %.3f / %.3f", currentCenter.getX(), currentCenter.getY(), currentCenter.getZ()));
        list.add(String.format("Move Amount     - %s §7/ %s §7/ %s", vecToDisplay(diff.getX()), vecToDisplay(diff.getY()), vecToDisplay(diff.getZ())));
    }

    public static void onGetRightTextTail(CallbackInfoReturnable<List<String>> cir) {
        List<String> list = cir.getReturnValue();

        boolean shouldRebuild = ChunkInstancedBarrierVisualizer.INSTANCE.isShouldRebuild();
        boolean forceRebuild = ChunkInstancedBarrierVisualizer.INSTANCE.isLastShouldRebuild();

        long pollQueueTime = ChunkInstancedBarrierVisualizer.INSTANCE.getLastPollQueueTime();

        list.add("");
        list.add("CIBV > ");
        list.add("Last Poll Queue - " + (-lastPollQueueTime + pollQueueTime) + " ms ago");
        list.add("Should rebuild - " + (shouldRebuild ? "§a" : "§c") + shouldRebuild);
        list.add("Force rebuild - " + (forceRebuild ? "§a" : "§c") + forceRebuild);
        list.add("Diameter - " + ChunkInstancedBarrierVisualizer.INSTANCE.getLastDiameter());
        list.add("Move X - " + ChunkInstancedBarrierVisualizer.INSTANCE.getLastMoveX());
        list.add("Move Y - " + ChunkInstancedBarrierVisualizer.INSTANCE.getLastMoveY());
        list.add("Move Z - " + ChunkInstancedBarrierVisualizer.INSTANCE.getLastMoveZ());
        list.add("");
        list.add("Trace CMD Connect: " + DebugCommand.traceCmdList.size() + "block(s)");
        list.add("Light level: " + RenderingEventsListener.lightLevel);
        list.add("Max multi tex: " + maxMultiTex);

        lastPollQueueTime = pollQueueTime;
    }

    /**
     * Get the entity glow color event
     *
     * @param entity Target entity
     */
    public static int onGetTeamColorValue(Entity entity) {
        AbstractTeam team = entity.getScoreboardTeam();
        if (KZEAddon.priorityGlowPlayers.contains(entity.getUuid())) {
            return KZEAddon.Options.getPriorityGlowColor().get();
        } else if (team != null) {
            String name = team.getName();
            if (name.equals("e")) {
                return KZEAddon.Options.getHumanGlowColor().get();
            } else if (name.equals("z")) {
                return KZEAddon.Options.getZombieGlowColor().get();
            }
        }
        return -1;
    }

    /**
     * Click tick event listener
     */
    public static void onTick(MinecraftClient client) {
        Profiler profiler = client.getProfiler();
        ClientPlayerEntity player = client.player;
        if (player == null) return;

        profiler.push("Check team visibility");
        AbstractTeam team = MinecraftClient.getInstance().player.getScoreboardTeam();
        if (team != null) {
            if (team.shouldShowFriendlyInvisibles() == KZEAddon.Options.isCompletelyInvisible()) {
                ((Team) team).setShowFriendlyInvisibles(!KZEAddon.Options.isCompletelyInvisible());
            }
        }
        profiler.swap("Barrier Visualizer tick");
        // KZEAddon.BAR_VISUALIZER.tick();
        profiler.swap("KZE Information tick");
        KZEAddon.KZE_INFO.tick();
        profiler.swap("Keys tick");
        KZEAddon.tickKeys();
        // TODO Particle system test
        if (client.currentScreen != null && !client.currentScreen.isPauseScreen()) {
            profiler.swap("PerlinParticle");
            PerlinParticle[] particles = KZEAddon.PARTICLES.toArray(new PerlinParticle[0]);
            for (int i = 0; i < particles.length; i++) {
                PerlinParticle pp = particles[i];
                if (pp == null) continue;
                pp.tick(client);
                if (pp.isOutdated()) {
                    KZEAddon.PARTICLES.remove(pp);
                }
            }
        }

        if (client.currentScreen == null || !client.currentScreen.isPauseScreen()) {
            InstancedPerlinParticleManager.INSTANCE.tick();
        }
        // InstancedBarrierVisualizer ibv = InstancedBarrierVisualizer.INSTANCE;
        // ibv.setCenter(player.getBlockPos());
        // ibv.tick();
        ChunkInstancedBarrierVisualizer.INSTANCE.setVisualizeCenter(player.getPos());
        ChunkInstancedBarrierVisualizer.INSTANCE.setCenter(player.chunkX, player.chunkY, player.chunkZ);
        ChunkInstancedBarrierVisualizer.INSTANCE.tick();

        FrameBufferLearn.INSTANCE.tick();
        profiler.swap("Poll all tick tasks");
        Runnable task;
        while ((task = onTickTasks.poll()) != null) {
            task.run();
        }
        KZEAddon.MOD_LOG.tick();
        profiler.pop();
    }

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
