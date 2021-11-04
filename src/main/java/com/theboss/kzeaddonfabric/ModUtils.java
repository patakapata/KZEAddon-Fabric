package com.theboss.kzeaddonfabric;

import com.theboss.kzeaddonfabric.render.ChunkInstancedBarrierVisualizer;
import com.theboss.kzeaddonfabric.render.shader.BarrierShader;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;

public class ModUtils {

    private ModUtils() {}

    public static void sendCurrentPositionPacket() {
        MinecraftClient mc = MinecraftClient.getInstance();

        if (mc.player != null) {
            ClientPlayerEntity player = mc.player;
            PlayerMoveC2SPacket packet = new PlayerMoveC2SPacket.PositionOnly(player.isSneaking() ? mc.player.getX() : 0, player.isSneaking() ? mc.player.getY() : 0, player.isSneaking() ? mc.player.getZ() : 0, player.isOnGround());
            player.networkHandler.sendPacket(packet);
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
        // 可視化の範囲(チャンク単位)
        barrierVis.addEntry(
                entryBuilder.startIntField(new TranslatableText("option.kzeaddon.bv_radius"), options.barrierVisualizeRadius)
                        .setDefaultValue(1)
                        .setTooltip(new TranslatableText("tooltip.kzeaddon.option.bv_radius"))
                        .setSaveConsumer(newV -> options.barrierVisualizeRadius = newV)
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
        // フェード効果を有効化するか
        barrierVis.addEntry(
                entryBuilder.startBooleanToggle(new TranslatableText("option.kzeaddon.use_fade"), options.shouldUseFade)
                        .setDefaultValue(false)
                        .setTooltip(new TranslatableText("tooltip.kzeaddon.option.use_fade"))
                        .setSaveConsumer(newV -> options.shouldUseFade = newV)
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
        // -------------------------------------------------- //
        // 機能の値
        // 銃声の音量の倍率
        general.addEntry(
                entryBuilder.startFloatField(new TranslatableText("option.kzeaddon.gunfire_volume_multiplier"), options.gunfireVolumeMultiplier)
                        .setDefaultValue(0.5F)
                        .setTooltip(new TranslatableText("tooltip.kzeaddon.option.gunfire_volume_multiplier"))
                        .setSaveConsumer(newV -> options.gunfireVolumeMultiplier = newV)
                        .build()
        );
        // 優先ターゲットの発光色
        general.addEntry(
                entryBuilder.startColorField(new TranslatableText("option.kzeaddon.priority_color"), options.priorityGlowColor.get())
                        .setDefaultValue(0xFF0000)
                        .setTooltip(new TranslatableText("tooltip.kzeaddon.option.priority_color"))
                        .setSaveConsumer(newV -> options.priorityGlowColor = new Color(newV))
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
