package com.theboss.kzeaddonfabric.utils;

import com.theboss.kzeaddonfabric.KZEAddon;
import com.theboss.kzeaddonfabric.Options;
import com.theboss.kzeaddonfabric.render.shader.impl.OldBarrierShader;
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
import org.lwjgl.glfw.GLFW;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;

public class ModUtils {

    private static void addBarrierEntries(Options options, ConfigEntryBuilder entryBuilder, ConfigCategory barrierVis) {
        // バリアを可視化するか
        barrierVis.addEntry(entryBuilder.startBooleanToggle(new TranslatableText("option.kzeaddon.barrier.is_visualize"), options.isVisualizeBarriers).setDefaultValue(false).setTooltip(new TranslatableText("option.kzeaddon.barrier.is_visualize.tooltip")).setSaveConsumer(newV -> options.isVisualizeBarriers = newV).build());
        // フェード効果を有効化するか
        barrierVis.addEntry(entryBuilder.startBooleanToggle(new TranslatableText("option.kzeaddon.barrier.is_fade"), options.isBarrierFade).setDefaultValue(false).setTooltip(new TranslatableText("option.kzeaddon.barrier.is_fade.tooltip")).setSaveConsumer(newV -> options.isBarrierFade = newV).build());
        // 可視化の範囲(チャンク単位)
        barrierVis.addEntry(entryBuilder.startIntField(new TranslatableText("option.kzeaddon.barrier.radius"), options.barrierVisualizeRadius).setDefaultValue(1).setTooltip(new TranslatableText("option.kzeaddon.barrier.radius.tooltip", 1)).setSaveConsumer(newV -> {
            options.barrierVisualizeRadius = newV;
            KZEAddon.getBarrierVisualizer().setRadius(newV);
        }).build());
        // 可視化されたブロックの表示範囲
        barrierVis.addEntry(entryBuilder.startFloatField(new TranslatableText("option.kzeaddon.barrier.fade_radius"), options.barrierFadeRadius).setDefaultValue(16F).setTooltip(new TranslatableText("option.kzeaddon.barrier.fade_radius.tooltip")).setSaveConsumer(newV -> options.barrierFadeRadius = newV).build());
        // 可視化の範囲をターゲットブロックにするか
        barrierVis.addEntry(entryBuilder.startBooleanToggle(new TranslatableText("option.kzeaddon.barrier.use_crosshair"), options.isCrosshairVisualizeOrigin).setDefaultValue(false).setTooltip(new TranslatableText("option.kzeaddon.barrier.use_crosshair.tooltip")).setSaveConsumer(newV -> options.isCrosshairVisualizeOrigin = newV).build());
        // ターゲットブロックの検出範囲
        barrierVis.addEntry(entryBuilder.startFloatField(new TranslatableText("option.kzeaddon.barrier.crosshair_distance"), options.barrierVisualizeRaycastDistance).setDefaultValue(40F).setTooltip(new TranslatableText("option.kzeaddon.barrier.crosshair_distance.tooltip")).setSaveConsumer(newV -> options.barrierVisualizeRaycastDistance = newV).build());
        // 可視化モデルの色
        barrierVis.addEntry(entryBuilder.startColorField(new TranslatableText("option.kzeaddon.barrier.color"), options.barrierColor.get()).setDefaultValue(0xAA0000).setTooltip(new TranslatableText("option.kzeaddon.barrier.color.tooltip")).setSaveConsumer(newV -> {
            Color color = new Color(newV);
            options.barrierColor = color;
            KZEAddon.getBarrierVisualizer().recordRenderCall(() -> OldBarrierShader.getInstance().setColor(color.get()));
        }).build());
        // 可視化モデルの線の太さ
        barrierVis.addEntry(entryBuilder.startFloatField(new TranslatableText("option.kzeaddon.barrier.line_width"), options.barrierLineWidth).setDefaultValue(2.0F).setTooltip(new TranslatableText("option.kzeaddon.barrier.line_width.tooltip")).setSaveConsumer(newV -> options.barrierLineWidth = newV).build());
    }

    private static void addDebugEntries(Options options, ConfigEntryBuilder entryBuilder, ConfigCategory debug) {
        // デバッグ用設定を表示するか
        debug.addEntry(entryBuilder.startBooleanToggle(new TranslatableText("option.kzeaddon.debug.is_show_debug_config"), options.isShowDebugConfig).setDefaultValue(false).setTooltip(new TranslatableText("option.kzeaddon.debug.is_show_debug_config.tooltip")).setSaveConsumer(newV -> options.isShowDebugConfig = newV).build());
        // Mod専用ログを表示するか
        debug.addEntry(entryBuilder.startBooleanToggle(new TranslatableText("option.kzeaddon.debug.show_mod_log"), options.isShowModLog).setDefaultValue(false).setTooltip(new TranslatableText("option.kzeaddon.debug.show_mod_log.tooltip")).setSaveConsumer(newV -> options.isShowModLog = newV).build());
        // バリア可視化のチャンク状態の表示
        debug.addEntry(entryBuilder.startBooleanToggle(new TranslatableText("option.kzeaddon.debug.show_chunk_states"), options.isShowChunkState)
                                   .setDefaultValue(false)
                                   .setSaveConsumer(v -> options.isShowChunkState = v)
                                   .build());
    }

    private static void addGeneralEntries(Options options, ConfigEntryBuilder entryBuilder, ConfigCategory general) {
        // -------------------------------------------------- //
        // 機能のオン/オフ
        // キルログを表示するか
        general.addEntry(entryBuilder.startBooleanToggle(new TranslatableText("option.kzeaddon.general.show_kill_log"), options.isShowKillLog).setDefaultValue(false).setTooltip(new TranslatableText("option.kzeaddon.general.show_kill_log.tooltip")).setSaveConsumer(newV -> options.isShowKillLog = newV).build());
        // 自分のキルを強調表示するか
        general.addEntry(entryBuilder.startBooleanToggle(new TranslatableText("option.kzeaddon.general.highlight_my_kill"), options.isHighlightMyKill).setDefaultValue(true).setTooltip(new TranslatableText("option.kzeaddon.general.highlight_my_kill.tooltip")).setSaveConsumer(newV -> options.isHighlightMyKill = newV).build());
        // KZEでリソースパックを無視できるようにするか
        general.addEntry(entryBuilder.startBooleanToggle(new TranslatableText("option.kzeaddon.general.ignore_resource_pack", options.isIgnoreResourcePack), options.isIgnoreResourcePack).setDefaultValue(true).setTooltip(new TranslatableText("option.kzeaddon.general.ignore_resource_pack.tooltip")).setSaveConsumer(newV -> options.isIgnoreResourcePack = newV).build());
        // 銃声の音量を変更するか
        general.addEntry(entryBuilder.startBooleanToggle(new TranslatableText("option.kzeaddon.general.change_gunfire_volume", options.isChangeGunfireVolume), options.isChangeGunfireVolume).setDefaultValue(true).setTooltip(new TranslatableText("option.kzeaddon.general.change_gunfire_volume.tooltip")).setSaveConsumer(newV -> options.isChangeGunfireVolume = newV).build());
        // 味方を透明化時に見えないようにするか
        general.addEntry(entryBuilder.startBooleanToggle(new TranslatableText("option.kzeaddon.general.completely_hide_ally"), options.isShowFriendlyInvisibles).setDefaultValue(false).setTooltip(new TranslatableText("option.kzeaddon.general.completely_hide_ally.tooltip")).setSaveConsumer(newV -> {
            options.isShowFriendlyInvisibles = newV;
            // 所属チームの可視フラグを更新
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player != null && mc.world != null && mc.player.getScoreboardTeam() != null) {
                Scoreboard scoreboard = mc.world.getScoreboard();
                Team team = scoreboard.getTeam(mc.player.getScoreboardTeam().getName());
                if (team.shouldShowFriendlyInvisibles() != newV) team.setShowFriendlyInvisibles(newV);
            }
        }).build());
        // -------------------------------------------------- //
        // 機能の値
        // 銃声の音量
        general.addEntry(entryBuilder.startIntSlider(new TranslatableText("option.kzeaddon.general.gunfire_volume"), (int) (options.gunfireVolumeMultiplier * 100), 0, 100).setDefaultValue(50).setTooltip(new TranslatableText("option.kzeaddon.common.default_value", "50")).setSaveConsumer(newV -> options.gunfireVolumeMultiplier = newV / 100F).setTextGetter(v -> Text.of(v + "%")).build());
        // 優先ターゲットの発光色
        general.addEntry(entryBuilder.startColorField(new TranslatableText("option.kzeaddon.general.obsession_color"), options.obsessionGlowColor.get()).setDefaultValue(0xFF0000).setTooltip(new TranslatableText("option.kzeaddon.common.default_value", "0xFF0000")).setSaveConsumer(newV -> options.obsessionGlowColor = new Color(newV)).build());
        // 人間チームの発光色
        general.addEntry(entryBuilder.startColorField(new TranslatableText("option.kzeaddon.general.human_color"), options.humanGlowColor.get()).setDefaultValue(0x00AAAA).setTooltip(new TranslatableText("option.kzeaddon.common.default_value", "0x00AAAA")).setSaveConsumer(newV -> options.humanGlowColor = new Color(newV)).build());
        // 感染者チームの発光色
        general.addEntry(entryBuilder.startColorField(new TranslatableText("option.kzeaddon.general.zombie_color"), options.zombieGlowColor.get()).setDefaultValue(0x00AA00).setTooltip(new TranslatableText("option.kzeaddon.common.default_value", "0x00AA00")).setSaveConsumer(newV -> options.zombieGlowColor = new Color(newV)).build());
    }

    public static Screen createConfigScreen(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create().setParentScreen(parent).setTitle(new TranslatableText("title.kzeaddon.config")).setTransparentBackground(true);

        Options options = KZEAddon.getOptions();
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        addGeneralEntries(options, entryBuilder, builder.getOrCreateCategory(new TranslatableText("category.kzeaddon.option.general")));
        addBarrierEntries(options, entryBuilder, builder.getOrCreateCategory(new TranslatableText("category.kzeaddon.option.barrier")));
        if (options.isShowDebugConfig) addDebugEntries(options, entryBuilder, builder.getOrCreateCategory(new TranslatableText("category.kzeaddon.option.debug")));

        return builder.build();
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

    public static void sendCurrentPositionPacket() {
        MinecraftClient mc = MinecraftClient.getInstance();

        if (mc.player != null) {
            ClientPlayerEntity player = mc.player;
            PlayerMoveC2SPacket packet = new PlayerMoveC2SPacket.PositionOnly(player.isSneaking() ? 0 : mc.player.getX(), player.isSneaking() ? 0 : mc.player.getY(), player.isSneaking() ? 0 : mc.player.getZ(), player.isOnGround());
            player.networkHandler.sendPacket(packet);
        }
    }

    private ModUtils() {}
}
