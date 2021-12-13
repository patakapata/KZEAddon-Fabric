package com.theboss.kzeaddonfabric.utils;

import com.theboss.kzeaddonfabric.KZEAddon;
import com.theboss.kzeaddonfabric.Options;
import com.theboss.kzeaddonfabric.render.ChunkInstancedBarrierVisualizer;
import com.theboss.kzeaddonfabric.render.shader.BarrierShader;
import com.theboss.kzeaddonfabric.wip.tree.AbstractTreeElement;
import com.theboss.kzeaddonfabric.wip.tree.ItemTreeElement;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import org.lwjgl.glfw.GLFW;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Random;

public class ModUtils {

    public static boolean prepareIO(File file) {
        try {
            if (file.exists()) {
                return true;
            } else {
                if (file.getParentFile().exists() || file.getParentFile().mkdirs()) {
                    return file.createNewFile();
                } else {
                    return false;
                }
            }
        } catch (Exception ex) {
            return false;
        }
    }

    private ModUtils() {}

    public static void sendCurrentPositionPacket() {
        MinecraftClient mc = MinecraftClient.getInstance();

        if (mc.player != null) {
            ClientPlayerEntity player = mc.player;
            PlayerMoveC2SPacket packet = new PlayerMoveC2SPacket.PositionOnly(player.isSneaking() ? 0 : mc.player.getX(), player.isSneaking() ? 0 : mc.player.getY(), player.isSneaking() ? 0 : mc.player.getZ(), player.isOnGround());
            player.networkHandler.sendPacket(packet);
        }
    }

    public static void genRandomTree(MinecraftClient mc, ItemTreeElement parent, int maxFork, int maxDepth, Random rand) {
        int fork = rand.nextInt(maxFork - 1) + 1;
        ItemTreeElement child;

        for (int i = 0; i < fork; i++) {
            child = new ItemTreeElement(mc, Registry.ITEM.getRandom(rand));
            parent.addChild(child);

            if (maxDepth - 1 > 0 && rand.nextBoolean()) {
                genRandomTree(mc, child, maxFork, maxDepth - 1, rand);
            }
        }

        parent.getItem().setCount(parent.getChildren().size());
    }

    public static void updateTreeIconCount(ItemTreeElement parent) {
        for (AbstractTreeElement child : parent.getChildren()) {
            if (child instanceof ItemTreeElement) {
                int size = child.getChildren().size();
                ((ItemTreeElement) child).getItem().setCount(size <= 0 ? 1 : size);
            }
        }
    }

    public static int getKeyState(int code) {
        return GLFW.glfwGetKey(MinecraftClient.getInstance().getWindow().getHandle(), code);
    }

    public static String getStackTrace(Exception ex) {
        StringWriter sWriter = new StringWriter();
        PrintWriter pWriter = new PrintWriter(sWriter);

        ex.printStackTrace(pWriter);

        return sWriter.toString();
    }


    public static Screen createConfigScreen(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(new TranslatableText("title.kzeaddon.config"))
                .setTransparentBackground(true);

        Options options = KZEAddon.options;
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        ConfigCategory general = builder.getOrCreateCategory(new TranslatableText("category.kzeaddon.general"));
        ConfigCategory barrierVisualize = builder.getOrCreateCategory(new TranslatableText("category.kzeaddon.barrier_vis"));
        ConfigCategory debug = builder.getOrCreateCategory(new TranslatableText("category.kzeaddon.debug"));

        addGeneralEntries(options, entryBuilder, general);
        addBarrierVisEntries(options, entryBuilder, barrierVisualize);
        addDebugEntries(options, entryBuilder, debug);

        return builder.build();
    }

    private static void addDebugEntries(Options options, ConfigEntryBuilder entryBuilder, ConfigCategory debug) {
        debug.addEntry(
                entryBuilder.startSubCategory(new TranslatableText("subcategory.kzeaddon.cam_offset"), Arrays.asList(
                        entryBuilder.startDoubleField(Text.of("x"), options.cameraOffset.x)
                                .setSaveConsumer(newV -> options.cameraOffset = new Vec3d(newV, options.cameraOffset.y, options.cameraOffset.z))
                                .build(),
                        entryBuilder.startDoubleField(Text.of("y"), options.cameraOffset.y)
                                .setSaveConsumer(newV -> options.cameraOffset = new Vec3d(options.cameraOffset.x, newV, options.cameraOffset.z))
                                .build(),
                        entryBuilder.startDoubleField(Text.of("z"), options.cameraOffset.z)
                                .setSaveConsumer(newV -> options.cameraOffset = new Vec3d(options.cameraOffset.x, options.cameraOffset.y, newV))
                                .build()
                )).build()
        );
        debug.addEntry(
                entryBuilder.startIntSlider(Text.of("Integer Slider"), 0, 0, 100)
                        .build()
        );
        debug.addEntry(
                entryBuilder.startSubCategory(Text.of("SubCategory"))
                        .build()
        );
        debug.addEntry(
                entryBuilder.startTextDescription(Text.of("Description")).build()
        );
    }

    private static void addBarrierVisEntries(Options options, ConfigEntryBuilder entryBuilder, ConfigCategory barrierVis) {
        // バリアを可視化するか
        barrierVis.addEntry(
                entryBuilder.startBooleanToggle(new TranslatableText("option.kzeaddon.should_barrier_visualize"), options.shouldBarrierVisualize)
                        .setDefaultValue(false)
                        .setTooltip(new TranslatableText("tooltip.kzeaddon.option.should_barrier_visualize"))
                        .setSaveConsumer(newV -> options.shouldBarrierVisualize = newV)
                        .build()
        );
        // 可視化の範囲をターゲットブロックにするか
        barrierVis.addEntry(
                entryBuilder.startBooleanToggle(new TranslatableText("option.kzeaddon.bv_use_crosshair"), options.barrierVisualizeUseCrosshairCenter)
                        .setDefaultValue(false)
                        .setTooltip(new TranslatableText("tooltip.kzeaddon.option.bv_use_crosshair"))
                        .setSaveConsumer(newV -> options.barrierVisualizeUseCrosshairCenter = newV)
                        .build()
        );
        // フェード効果を有効化するか
        barrierVis.addEntry(
                entryBuilder.startBooleanToggle(new TranslatableText("option.kzeaddon.use_fade"), options.shouldUseFade)
                        .setDefaultValue(false)
                        .setTooltip(new TranslatableText("tooltip.kzeaddon.option.use_fade"))
                        .setSaveConsumer(newV -> options.shouldUseFade = newV)
                        .build()
        );
        // 可視化モデルの色
        barrierVis.addEntry(
                entryBuilder.startColorField(new TranslatableText("option.kzeaddon.bv_color"), options.barrierColor.get())
                        .setDefaultValue(0xAA0000)
                        .setTooltip(new TranslatableText("tooltip.kzeaddon.option.bv_color"))
                        .setSaveConsumer(newV -> {
                            Color color = new Color(newV);
                            options.barrierColor = color;
                            ChunkInstancedBarrierVisualizer.recordRenderCall(() -> BarrierShader.INSTANCE.setColor(color));
                        })
                        .build()
        );
        // 可視化の範囲(チャンク単位)
        barrierVis.addEntry(
                entryBuilder.startIntField(new TranslatableText("option.kzeaddon.bv_radius"), options.barrierVisualizeRadius)
                        .setDefaultValue(1)
                        .setTooltip(new TranslatableText("tooltip.kzeaddon.option.bv_radius"))
                        .setSaveConsumer(newV -> {
                            options.barrierVisualizeRadius = newV;
                            ChunkInstancedBarrierVisualizer.INSTANCE.setRadius(newV);
                        })
                        .build()
        );
        // 可視化されたブロックの表示範囲
        barrierVis.addEntry(
                entryBuilder.startFloatField(new TranslatableText("option.kzeaddon.bv_visualize_show_radius"), options.barrierVisualizeShowRadius)
                        .setDefaultValue(16F)
                        .setTooltip(new TranslatableText("tooltip.kzeaddon.bv_visualize_show_radius"))
                        .setSaveConsumer(newV -> options.barrierVisualizeShowRadius = newV)
                        .build()
        );
        // ターゲットブロックの検出範囲
        barrierVis.addEntry(
                entryBuilder.startFloatField(new TranslatableText("option.kzeaddon.bv_raycast_distance"), options.barrierVisualizeRaycastDistance)
                        .setDefaultValue(40F)
                        .setTooltip(new TranslatableText("tooltip.kzeaddon.option.bv_raycast_distance"))
                        .setSaveConsumer(newV -> options.barrierVisualizeRaycastDistance = newV)
                        .build()
        );
        // 可視化モデルの線の太さ
        barrierVis.addEntry(
                entryBuilder.startFloatField(new TranslatableText("option.kzeaddon.bv_line_width"), options.barrierLineWidth)
                        .setDefaultValue(2.0F)
                        .setTooltip(new TranslatableText("tooltip.kzeaddon.option.bv_line_width"))
                        .setSaveConsumer(newV -> options.barrierLineWidth = newV)
                        .build()
        );
    }

    private static void addGeneralEntries(Options options, ConfigEntryBuilder entryBuilder, ConfigCategory general) {
        // -------------------------------------------------- //
        // 機能のオン/オフ
        // キルログを表示するか
        general.addEntry(
                entryBuilder.startBooleanToggle(new TranslatableText("option.kzeaddon.show_kill_log"), options.shouldShowKillLog)
                        .setDefaultValue(false)
                        .setTooltip(new TranslatableText("tooltip.kzeaddon.option.show_kill_log"))
                        .setSaveConsumer(newV -> options.shouldShowKillLog = newV)
                        .build()
        );
        // 自分のキルを強調表示するか
        general.addEntry(
                entryBuilder.startBooleanToggle(new TranslatableText("option.kzeaddon.highlight_my_kill"), options.shouldHighlightMyKill)
                        .setDefaultValue(true)
                        .setTooltip(new TranslatableText("tooltip.kzeaddon.option.highlight_my_kill"))
                        .setSaveConsumer(newV -> options.shouldHighlightMyKill = newV)
                        .build()
        );
        // Mod専用ログを表示するか
        general.addEntry(
                entryBuilder.startBooleanToggle(new TranslatableText("option.kzeaddon.show_mod_log"), options.shouldShowModLog)
                        .setDefaultValue(false)
                        .setTooltip(new TranslatableText("tooltip.kzeaddon.option.show_mod_log"))
                        .setSaveConsumer(newV -> options.shouldShowModLog = newV)
                        .build()
        );
        // KZEでリソースパックを無視できるようにするか
        general.addEntry(
                entryBuilder.startBooleanToggle(new TranslatableText("option.kzeaddon.ignore_resource_pack", options.shouldIgnoreResourcePack), options.shouldIgnoreResourcePack)
                        .setDefaultValue(true)
                        .setTooltip(new TranslatableText("tooltip.kzeaddon.option.ignore_resource_pack"))
                        .setSaveConsumer(newV -> options.shouldIgnoreResourcePack = newV)
                        .build()
        );
        // 銃声の音量を変更するか
        general.addEntry(
                entryBuilder.startBooleanToggle(new TranslatableText("option.kzeaddon.change_gunfire_volume", options.shouldChangeGunfireVolume), options.shouldChangeGunfireVolume)
                        .setDefaultValue(true)
                        .setTooltip(new TranslatableText("tooltip.kzeaddon.option.change_gunfire_volume"))
                        .setSaveConsumer(newV -> options.shouldChangeGunfireVolume = newV)
                        .build()
        );
        // 味方を透明化時に見えないようにするか
        general.addEntry(
                entryBuilder.startBooleanToggle(new TranslatableText("option.kzeaddon.completely_hide_allies"), options.shouldShowFriendlyInvisibles)
                        .setDefaultValue(false)
                        .setTooltip(new TranslatableText("tooltip.kzeaddon.completely_hide_allies"))
                        .setSaveConsumer(newV -> {
                            options.shouldShowFriendlyInvisibles = newV;
                            // 所属チームの可視フラグを更新
                            MinecraftClient mc = MinecraftClient.getInstance();
                            if (mc.player != null && mc.world != null && mc.player.getScoreboardTeam() != null) {
                                Scoreboard scoreboard = mc.world.getScoreboard();
                                Team team = scoreboard.getTeam(mc.player.getScoreboardTeam().getName());
                                if (team.shouldShowFriendlyInvisibles() != newV)
                                    team.setShowFriendlyInvisibles(newV);
                            }
                        })
                        .build()
        );
        // -------------------------------------------------- //
        // 機能の値
        // 銃声の音量の倍率
        general.addEntry(
                entryBuilder.startIntSlider(new TranslatableText("option.kzeaddon.gunfire_volume"), (int) (options.gunfireVolumeMultiplier * 100), 0, 100)
                        .setDefaultValue(50)
                        .setTooltip(new TranslatableText("tooltip.kzeaddon.option.gunfire_volume"))
                        .setSaveConsumer(newV -> options.gunfireVolumeMultiplier = newV / 100F)
                        .build()
        );
        // general.addEntry(
        //         entryBuilder.startFloatField(new TranslatableText("option.kzeaddon.gunfire_volume_multiplier"), options.gunfireVolumeMultiplier)
        //                 .setDefaultValue(0.5F)
        //                 .setTooltip(new TranslatableText("tooltip.kzeaddon.option.gunfire_volume_multiplier"))
        //                 .setSaveConsumer(newV -> options.gunfireVolumeMultiplier = newV)
        //                 .build()
        // );
        // 優先ターゲットの発光色
        general.addEntry(
                entryBuilder.startColorField(new TranslatableText("option.kzeaddon.obsession_color"), options.obsessionGlowColor.get())
                        .setDefaultValue(0xFF0000)
                        .setTooltip(new TranslatableText("tooltip.kzeaddon.option.obsession_color"))
                        .setSaveConsumer(newV -> options.obsessionGlowColor = new Color(newV))
                        .build()
        );
        // 人間チームの発光色
        general.addEntry(
                entryBuilder.startColorField(new TranslatableText("option.kzeaddon.human_color"), options.humanGlowColor.get())
                        .setDefaultValue(0x00AAAA)
                        .setTooltip(new TranslatableText("tooltip.kzeaddon.option.human_color"))
                        .setSaveConsumer(newV -> options.humanGlowColor = new Color(newV))
                        .build()
        );
        // 感染者チームの発光色
        general.addEntry(
                entryBuilder.startColorField(new TranslatableText("option.kzeaddon.zombie_color"), options.zombieGlowColor.get())
                        .setDefaultValue(0x00AA00)
                        .setTooltip(new TranslatableText("tooltip.kzeaddon.option.zombie_color"))
                        .setSaveConsumer(newV -> options.zombieGlowColor = new Color(newV))
                        .build()
        );
    }
}
