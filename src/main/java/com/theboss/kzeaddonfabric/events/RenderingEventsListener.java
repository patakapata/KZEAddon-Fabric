package com.theboss.kzeaddonfabric.events;

import com.theboss.kzeaddonfabric.CopyItemNBTHandler;
import com.theboss.kzeaddonfabric.FavoriteItemManager;
import com.theboss.kzeaddonfabric.KZEAddon;
import com.theboss.kzeaddonfabric.ingame.Weapon;
import com.theboss.kzeaddonfabric.mixin.accessor.CameraAccessor;
import com.theboss.kzeaddonfabric.render.ChunkInstancedBarrierVisualizer;
import com.theboss.kzeaddonfabric.render.RenderContext;
import com.theboss.kzeaddonfabric.render.shader.BarrierShader;
import com.theboss.kzeaddonfabric.render.shader.OldBarrierShader;
import com.theboss.kzeaddonfabric.screen.KillLogScreen;
import com.theboss.kzeaddonfabric.utils.VanillaUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.BlockView;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RenderingEventsListener extends DrawableHelper {
    private static final List<Float> deltaHistory = new ArrayList<>();

    private static void closeShaders() {
        BarrierShader.INSTANCE.close();
        OldBarrierShader.INSTANCE.close();
    }

    private static void initShaders() {
        BarrierShader.INSTANCE.initialize();
        OldBarrierShader.INSTANCE.initialize();
    }

    public static void onCameraUpdate(Camera camera, BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta) {
        if (thirdPerson) {
            Vec3d offset = KZEAddon.options.cameraOffset;
            CameraAccessor accessor = (CameraAccessor) camera;
            accessor.invokeMoveBy(offset.x, offset.y, offset.z);
        }
    }

    /**
     * クライアント終了時の処理
     */
    public static void onClose() {
        closeShaders();
        ChunkInstancedBarrierVisualizer.INSTANCE.close();
    }

    public static Optional<BipedEntityModel.ArmPose> onGetArmPose(AbstractClientPlayerEntity player, Hand hand) {
        ItemStack item = player.getStackInHand(hand);

        if (Weapon.quickReloadCheck(item)) {
            return Optional.of(BipedEntityModel.ArmPose.CROSSBOW_CHARGE);
        } else if (item.getItem().equals(Items.DIAMOND_HOE)) {
            return Optional.of(BipedEntityModel.ArmPose.CROSSBOW_HOLD);
        }

        return Optional.empty();
    }

    /**
     * シェーダーやバッファーの初期化
     */
    public static void onInit() {
        initShaders();
        ChunkInstancedBarrierVisualizer.INSTANCE.initialize();
    }

    public static void onPreRenderTranslucent(RenderContext context) {
        ChunkInstancedBarrierVisualizer.render(context.getDelta());
        deltaHistory.add(context.getDelta());
        if (deltaHistory.size() > 200) {
            deltaHistory.remove(0);
        }
    }

    /**
     * Hud render event listener
     *
     * @param matrices {@link MatrixStack}
     * @param delta    A rendering delay
     */
    public static void onRenderHud(MatrixStack matrices, float delta) {
        Profiler profiler = VanillaUtils.getProfiler();
        MinecraftClient mc = MinecraftClient.getInstance();
        Window window = mc.getWindow();

        profiler.push("Widgets");
        KZEAddon.widgetRenderer.render(matrices, delta);
        profiler.swap("KillLog");
        if (!(mc.currentScreen instanceof KillLogScreen))
            KZEAddon.killLog.render(matrices, window.getScaledWidth(), 0);
        if (KZEAddon.options.isShowModLog)
            KZEAddon.getModLog().render(matrices);
        profiler.pop();
    }

    /**
     * アイテムのツールチップを取得する際の処理
     */
    public static void onRenderItemTooltip(ItemStack stack, TooltipContext ctx, List<Text> list) {
        if (MinecraftClient.getInstance().currentScreen == null) return;

        CopyItemNBTHandler.handleEvent(stack, ctx, list);
        FavoriteItemManager.handleEvent(stack, ctx, list);
    }

    public static void onWindowResized(Window window) {
        ChunkInstancedBarrierVisualizer.INSTANCE.framebuffer.setSize(window);
    }
}
