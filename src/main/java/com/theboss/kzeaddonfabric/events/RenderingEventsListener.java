package com.theboss.kzeaddonfabric.events;

import com.theboss.kzeaddonfabric.KZEAddon;
import com.theboss.kzeaddonfabric.KeyBindings;
import com.theboss.kzeaddonfabric.ingame.Weapon;
import com.theboss.kzeaddonfabric.mixin.accessor.CameraAccessor;
import com.theboss.kzeaddonfabric.mixin.accessor.KeyBindingAccessor;
import com.theboss.kzeaddonfabric.render.ChunkInstancedBarrierVisualizer;
import com.theboss.kzeaddonfabric.render.shader.BarrierShader;
import com.theboss.kzeaddonfabric.render.shader.OldBarrierShader;
import com.theboss.kzeaddonfabric.screen.KillLogScreen;
import com.theboss.kzeaddonfabric.utils.ModUtils;
import com.theboss.kzeaddonfabric.utils.VanillaUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.BlockView;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.Optional;

public class RenderingEventsListener extends DrawableHelper {
    // -------------------------------------------------- //
    // General
    public static long lastTime;
    // -------------------------------------------------- //
    // Favorite Items
    public static float holdProgress;
    public static boolean holdProcessed;
    public static ItemStack lastItem = ItemStack.EMPTY;

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
     * ????????????????????????????????????
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
     * ?????????????????????????????????????????????
     */
    public static void onInit() {
        initShaders();
        ChunkInstancedBarrierVisualizer.INSTANCE.initialize();
    }

    /**
     * ??????????????????????????????
     *
     * @param matrices {@link net.minecraft.client.util.math.MatrixStack}
     * @param delta    A render delay
     */
    public static void onPostRenderWorld(MatrixStack matrices, float delta) {}

    /**
     * ??????????????????????????????(???????????????????????????????????????????????????????????????)????????????
     *
     * @param matrices           ?????????????????????????????????????????????????????????????????????
     * @param delta              ????????????????????????????????????(?????????)
     * @param limitTime          ??????
     * @param renderBlockOutline ????????????????????????????????????
     * @param camera             ?????????
     * @param gameRenderer       ????????????????????????
     * @param lightTexManager    ???????????????????????????????????????????????????
     * @param unused             ??????
     */
    public static void onPreRenderCutout(MatrixStack matrices, float delta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightTexManager, Matrix4f unused) {
        Profiler profiler = VanillaUtils.getProfiler();

        profiler.push("Render barriers");
        ChunkInstancedBarrierVisualizer.render(delta);
        profiler.pop();
    }

    public static void onPreRenderSolid(MatrixStack matrices, float delta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f) {
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
     * ????????????????????????????????????????????????????????????
     */
    @SuppressWarnings("unused")
    public static void onRenderItemTooltip(ItemStack stack, TooltipContext ctx, List<Text> list) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.currentScreen == null) return;

        // -------------------------------------------------- //
        // NBT?????????
        boolean isPressed = InputUtil.isKeyPressed(mc.getWindow().getHandle(), KeyBindings.COPY_ITEM_TAG.getCode());
        NbtElement tag = stack.getTag();
        if (tag != null) {
            String str = tag.toText().getString();
            if (isPressed) {
                if (!KeyBindings.COPY_FLIP_FLIP) {
                    KeyBindings.COPY_FLIP_FLIP = true;
                    VanillaUtils.copyToClipboard(new TranslatableText(stack.getTranslationKey()), str);
                }
            } else {
                if (KeyBindings.COPY_FLIP_FLIP) {
                    KeyBindings.COPY_FLIP_FLIP = false;
                }
            }
        }
        // -------------------------------------------------- //
        // ?????????????????????????????????
        if (!stack.equals(lastItem)) {
            holdProgress = 0;
            lastTime = System.currentTimeMillis();
            lastItem = stack;
        }
        float requireTime = 0.5F;
        int divide = 40;

        long now = System.currentTimeMillis();
        float delta = (now - lastTime) / 1_000F;
        lastTime = now;
        if (ModUtils.getKeyState(((KeyBindingAccessor) mc.options.keyForward).getBoundKey().getCode()) == GLFW.GLFW_PRESS) {

            if (holdProgress < 1) {
                holdProgress += delta / requireTime;
                if (holdProgress > 1) {
                    holdProgress = 1;
                }
            }
        } else if (holdProgress > 0) {
            holdProgress -= delta / requireTime * 2F;
            if (holdProgress < 0) holdProgress = 0;
        }

        if (holdProgress == 1) {
            if (!holdProcessed) {
                holdProcessed = true;
                boolean containItem = KZEAddon.isFavoriteItem(stack);

                if (!containItem) {
                    KZEAddon.addFavoriteItem(stack.copy());
                    KZEAddon.info("Favorite Items > " + VanillaUtils.textAsString(stack.getName()) + " is added!");

                } else {
                    KZEAddon.removeFavoriteItem(stack);
                    KZEAddon.info("Favorite Items > " + VanillaUtils.textAsString(stack.getName()) + " is removed!");
                }
            }
        } else {
            holdProcessed = false;
        }
        StringBuilder builder = new StringBuilder();
        int done = MathHelper.floor(holdProgress * divide);
        builder.append("??f");
        for (int i = 0; i < done; i++) builder.append("|");
        builder.append("??7");
        for (int i = 0; i < divide - done; i++) builder.append("|");
        builder.append("??r");

        LiteralText text = new LiteralText("(");
        text.append(VanillaUtils.textAsString(mc.options.keyForward.getBoundKeyLocalizedText()).toUpperCase());
        text.append(") ");
        text.append(builder.toString());

        list.add(Text.of(KZEAddon.isFavoriteItem(stack) ? "??a??? ??6Favorite??r" : "??c??? ??8Favorite??r"));
        list.add(list.size() - (ctx.isAdvanced() ? 1 : 0), text);
        // -------------------------------------------------- //
        // ???????????????????????????
        if (ctx.isAdvanced()) {
            NbtCompound nbt = stack.getTag();
            if (nbt != null && nbt.contains("CustomModelData")) {
                int customModelData = nbt.getInt("CustomModelData");
                list.add(list.size() - 2, Text.of("----------"));
                VanillaUtils.visualizeNbt("CustomModelData,HideFlags,Unbreakable,Damage", nbt, list, list.size() - 2);
            }
        }
    }

    public static void onWindowResized(Window window) {
        ChunkInstancedBarrierVisualizer.INSTANCE.framebuffer.setSize(window);
    }
}
