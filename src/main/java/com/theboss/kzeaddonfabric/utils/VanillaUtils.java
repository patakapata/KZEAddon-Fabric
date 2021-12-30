package com.theboss.kzeaddonfabric.utils;

import com.mojang.brigadier.context.CommandContext;
import com.theboss.kzeaddonfabric.KZEAddon;
import com.theboss.kzeaddonfabric.mixin.accessor.DefaultPosArgumentAccessor;
import com.theboss.kzeaddonfabric.mixin.accessor.LookingPosArgumentAccessor;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.command.argument.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.util.profiler.Profiler;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

public class VanillaUtils {
    /**
     * Add text the chat log
     *
     * @param msg Text to add
     */
    public static void addChatLog(String msg) {
        addChatLog(Text.of(msg));
    }

    /**
     * Add text the chat log using format
     *
     * @param format Text format see the {@link String#format(String, Object...)}
     * @param args   arguments
     */
    public static void addChatLog(String format, Object... args) {
        addChatLog(String.format(format, args));
    }

    /**
     * Add text the chat log
     *
     * @param text Text to add
     */
    public static void addChatLog(Text text) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.inGameHud != null && client.inGameHud.getChatHud() != null) {
            client.inGameHud.getChatHud().addMessage(text);
        } else {
            KZEAddon.info(text);
        }
    }

    public static int compareBlockPos(Vec3i vec1, Vec3i vec2) {
        int x = Integer.compare(vec1.getX(), vec2.getX());
        int y = Integer.compare(vec1.getY(), vec2.getY());
        int z = Integer.compare(vec1.getZ(), vec2.getZ());

        if (vec1.equals(vec2)) return 0;
        else if (y > 0 && x > 0 && z > 0) return 1;
        else return -1;
    }

    /**
     * Copy to clipboard
     *
     * @param name    Notification text
     * @param content Copy contents
     */
    public static void copyToClipboard(Text name, String content) {
        MinecraftClient mc = MinecraftClient.getInstance();
        ToastManager toastManager = mc.getToastManager();
        KZEAddon.info(content);
        try {
            mc.keyboard.setClipboard(content);
            toastManager.add(new SystemToast(SystemToast.Type.TUTORIAL_HINT, new TranslatableText("kzeaddon.copied_to_clipboard"), name));
        } catch (Exception ex) {
            toastManager.add(new SystemToast(SystemToast.Type.TUTORIAL_HINT, new TranslatableText("kzeaddon.copy_failed"), Text.of(ex.getMessage())));
            ex.printStackTrace();
        }
    }

    public static int getCustomModelData(ItemStack item) {
        NbtCompound tag = item.getTag();
        if (tag == null) return -1;
        return tag.getInt("CustomModelData");
    }

    public static Profiler getProfiler() {
        return MinecraftClient.getInstance().getProfiler();
    }

    /**
     * ワールドの時間を取得
     *
     * @return プレイヤーがワールドに入っている場合、<br>
     * ワールドの時間 それ以外の場合 0
     */
    public static long getWorldTime() {
        ClientWorld world = MinecraftClient.getInstance().world;

        return world == null ? 0 : world.getTime();
    }

    public static boolean isSneaking() {
        MinecraftClient mc = MinecraftClient.getInstance();
        return mc.player != null && mc.player.isSneaking();
    }

    /**
     * Return true When team with main client player, otherwise false.
     *
     * @param entity Other entity
     * @return true when team with main client player.
     */
    public static boolean isTeammate(Entity entity) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return false;
        return player.isTeammate(entity);
    }

    public static int lerp(int color1, int color2, double progress) {
        int[] array1 = parse(color1);
        int[] array2 = parse(color2);
        int[] diff = new int[]{ array2[0] - array1[0], array2[1] - array1[1], array2[2] - array1[2] };

        array1[0] += diff[0] * progress;
        array1[1] += diff[1] * progress;
        array1[2] += diff[2] * progress;

        return parse(array1[0], array1[1], array1[2]);
    }

    public static int[] parse(int color) {
        return new int[]{
                color >> 16 & 0xFF,
                color >> 8 & 0xFF,
                color & 0xFF
        };
    }

    public static int parse(int red, int green, int blue) {
        return red << 16 | green << 8 | blue;
    }

    public static Text randomText() {
        return Text.of(UUID.randomUUID().toString());
    }

    /**
     * ターゲットのエンティティを取得します
     *
     * @param entity   視線の元のエンティティ
     * @param distance 最大距離
     * @return 距離内にエンティティがいた場合、{@link EntityHitResult} それ以外の場合 {@link BlockHitResult}
     */
    public static HitResult raycastIgnoreBlock(Entity entity, double distance, Predicate<Entity> predicate) {
        Vec3d vec3d = entity.getCameraPosVec(1.0F);
        Vec3d vec3d2 = entity.getRotationVec(1.0F);
        Vec3d vec3d3 = vec3d.add(vec3d2.x * distance, vec3d2.y * distance, vec3d2.z * distance);
        Box box = entity.getBoundingBox().stretch(vec3d2.multiply(distance)).expand(1.0D, 1.0D, 1.0D);
        EntityHitResult entityHitResult = ProjectileUtil.raycast(entity, vec3d, vec3d3, box, predicate, distance * distance);
        if (entityHitResult == null) {
            return BlockHitResult.createMissed(entity.getPos(), Direction.getFacing(vec3d2.x, vec3d2.y, vec3d2.z), new BlockPos(entity.getPos()));
        }
        return entityHitResult;
    }

    public static String textAsString(Text text) {
        StringBuilder builder = new StringBuilder();
        text.visit(asString -> {
            builder.append(asString);
            return Optional.empty();
        });
        return builder.toString();
    }

    private static Vec3d toAbsoluteCoordinate(CommandContext<FabricClientCommandSource> ctx, DefaultPosArgument pos) {
        Vec3d origin = ctx.getSource().getPosition();
        CoordinateArgument x = ((DefaultPosArgumentAccessor) pos).getX();
        CoordinateArgument y = ((DefaultPosArgumentAccessor) pos).getY();
        CoordinateArgument z = ((DefaultPosArgumentAccessor) pos).getZ();

        return new Vec3d(
                x.toAbsoluteCoordinate(origin.x),
                y.toAbsoluteCoordinate(origin.y),
                z.toAbsoluteCoordinate(origin.z)
        );
    }

    private static Vec3d toAbsoluteCoordinate(CommandContext<FabricClientCommandSource> ctx, LookingPosArgument pos) {
        LookingPosArgumentAccessor acs = (LookingPosArgumentAccessor) pos;
        Vec2f vec2f = ctx.getSource().getRotation();
        Vec3d vec3d = EntityAnchorArgumentType.EntityAnchor.FEET.positionAt(ctx.getSource().getEntity());
        float f = MathHelper.cos((vec2f.y + 90.0F) * 0.017453292F);
        float g = MathHelper.sin((vec2f.y + 90.0F) * 0.017453292F);
        float h = MathHelper.cos(-vec2f.x * 0.017453292F);
        float i = MathHelper.sin(-vec2f.x * 0.017453292F);
        float j = MathHelper.cos((-vec2f.x + 90.0F) * 0.017453292F);
        float k = MathHelper.sin((-vec2f.x + 90.0F) * 0.017453292F);
        Vec3d vec3d2 = new Vec3d(f * h, i, g * h);
        Vec3d vec3d3 = new Vec3d(f * j, k, g * j);
        Vec3d vec3d4 = vec3d2.crossProduct(vec3d3).multiply(-1.0D);
        double d = vec3d2.x * acs.getZ() + vec3d3.x * acs.getY() + vec3d4.x * acs.getX();
        double e = vec3d2.y * acs.getZ() + vec3d3.y * acs.getY() + vec3d4.y * acs.getX();
        double l = vec3d2.z * acs.getZ() + vec3d3.z * acs.getY() + vec3d4.z * acs.getX();
        return new Vec3d(vec3d.x + d, vec3d.y + e, vec3d.z + l);
    }

    public static Vec3d toAbsoluteCoordinate(CommandContext<FabricClientCommandSource> ctx, PosArgument pos) {
        if (pos instanceof DefaultPosArgument) return toAbsoluteCoordinate(ctx, (DefaultPosArgument) pos);
        else if (pos instanceof LookingPosArgument) return toAbsoluteCoordinate(ctx, (LookingPosArgument) pos);
        else return null;
    }

    public static BlockPos toChunk(BlockPos pos) {
        return new BlockPos(
                (pos.getX() >= 0 ? pos.getX() : pos.getX() - 15) / 16,
                (pos.getY() >= 0 ? pos.getY() : pos.getY() - 15) / 16,
                (pos.getZ() >= 0 ? pos.getZ() : pos.getZ() - 15) / 16
        );
    }

    public static String toShortString(Vec3d vec, int depth) {
        return String.format("%." + depth + "f, %." + depth + "f, %." + depth + "f", vec.x, vec.y, vec.z);
    }

    public static String toShortString(Vec3d vec) {
        return toShortString(vec, 2);
    }

    public static void visualizeNbt(String name, NbtCompound nbt, List<Text> list, int index) {
        try {
            String[] names = name.split(",");
            List<Text> add = new ArrayList<>();
            for (String str : names) {
                if (nbt.contains(str)) {
                    NbtElement element = nbt.get(str);

                    if (element != null) {
                        add.add(Text.of("§8" + str + " : " + element.asString() + "§r"));
                    }
                }
            }

            list.addAll(index, add);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private VanillaUtils() {}
}
