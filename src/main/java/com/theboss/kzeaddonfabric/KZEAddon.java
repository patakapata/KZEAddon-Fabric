package com.theboss.kzeaddonfabric;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import com.theboss.kzeaddonfabric.enums.Switchable;
import com.theboss.kzeaddonfabric.events.EventsListener;
import com.theboss.kzeaddonfabric.ingame.KillLog;
import com.theboss.kzeaddonfabric.mixin.Matrix4fAccessor;
import com.theboss.kzeaddonfabric.mixin.client.KeyBindingAccessor;
import com.theboss.kzeaddonfabric.render.BarrierVisualizer;
import com.theboss.kzeaddonfabric.render.shader.BarrierShader;
import com.theboss.kzeaddonfabric.render.widgets.Widget;
import com.theboss.kzeaddonfabric.wip.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.entity.PlayerModelPart;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.client.util.GlAllocationUtils;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.EntitySelectorReader;
import net.minecraft.command.argument.PosArgument;
import net.minecraft.command.argument.Vec3ArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL33;

import java.awt.*;
import java.io.*;
import java.lang.reflect.Field;
import java.nio.FloatBuffer;
import java.util.List;
import java.util.*;

@Environment(EnvType.CLIENT)
public class KZEAddon implements ClientModInitializer {
    public static final String CC_REGEX = "^(§([0-9]|[a-f]))";
    public static final BarrierVisualizer BAR_VISUALIZER = new BarrierVisualizer();
    public static final KZEInformation KZE_INFO = new KZEInformation();
    public static final Logger LOGGER = LogManager.getLogger("KZEAddon-Fabric");
    public static final Identifier PARTICLE_SPRITE = new Identifier("kzeaddon-fabric", "textures/wip/particle_sprite.png");
    public static final String MOD_NAME = "KZEAddon-Fabric";
    public static final String MOD_ID = "kzeaddon-fabric";
    public static final List<UUID> priorityGlowPlayers = new ArrayList<>();
    public static final List<KeyBinding> modKeys = new ArrayList<>();
    public static final MarkedArea MARKED_AREA = new MarkedArea(new MarkedArea.Area(new BlockPos(-10, 0, -10), new BlockPos(10, 10, 10)));
    public static final List<PerlinParticle> PARTICLES = new ArrayList<>();
    public static final Map<Integer, String> GL_ERRORS = new HashMap<>();
    public static KeyBindingWrapper ADD_GROW_TARGET;
    public static KeyBindingWrapper COPY_ITEM_TAG;
    public static KeyBindingWrapper DEBUG_KEY;
    public static KeyBindingWrapper HIDE_PLAYERS;
    public static boolean KEY_FLIPFLOP_COPY = false;
    public static Options Options;
    public static boolean isHideTeammates;
    public static StringBuilder S_BUILDER = new StringBuilder();
    public static PlaneLayers TEST_LAYERS = new PlaneLayers();
    public static KZEAddonLog MOD_LOG = new KZEAddonLog(1000, 0, 0, 10, true);
    private static File optionsFile;
    private static boolean showModLog = true;

    static {
        GL_ERRORS.put(0x0500, "GL_INVALID_ENUM");
        GL_ERRORS.put(0x0501, "GL_INVALID_VALUE");
        GL_ERRORS.put(0x0502, "GL_INVALID_OPERATION");
        GL_ERRORS.put(0x0503, "GL_STACK_OVERFLOW");
        GL_ERRORS.put(0x0504, "GL_STACK_UNDERFLOW");
        GL_ERRORS.put(0x0505, "GL_OUT_OF_MEMORY");
        GL_ERRORS.put(0x0506, "GL_INVALID_FRAMEBUFFER_OPERATION");
        GL_ERRORS.put(0x0507, "GL_CONTEXT_LOST");
        GL_ERRORS.put(0x0508, "GL_TABLE_TOO_LARGE");
    }

    /**
     * Add text the chat log
     *
     * @param msg Text to add
     * @deprecated Use {@link #info(String)} instead.
     */
    @Deprecated
    public static void addChatLog(String msg) {
        addChatLog(Text.of(msg));
    }

    /**
     * Add text the chat log using format
     *
     * @param format Text format see the {@link String#format(String, Object...)}
     * @param args   arguments
     * @deprecated Use {@link #info(String)} instead.
     */
    @Deprecated
    @SuppressWarnings("unused")
    public static void addChatLog(String format, Object... args) {
        addChatLog(String.format(format, args));
    }

    /**
     * Add text the chat log
     *
     * @param text Text to add
     * @deprecated Use {@link #info(Text)} instead.
     */
    @Deprecated
    public static void addChatLog(Text text) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.inGameHud != null && client.inGameHud.getChatHud() != null) {
            client.inGameHud.getChatHud().addMessage(text);
        } else {
            info(text);
        }
    }

    public static void bobView(MatrixStack matrices, float f) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.getCameraEntity() instanceof PlayerEntity) {
            PlayerEntity playerEntity = (PlayerEntity) client.getCameraEntity();
            float g = playerEntity.horizontalSpeed - playerEntity.prevHorizontalSpeed;
            float h = -(playerEntity.horizontalSpeed + g * f);
            float i = MathHelper.lerp(f, playerEntity.prevStrideDistance, playerEntity.strideDistance);
            matrices.translate((double) (MathHelper.sin(h * 3.1415927F) * i * 0.5F), (double) (-Math.abs(MathHelper.cos(h * 3.1415927F) * i)), 0.0D);
            matrices.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(MathHelper.sin(h * 3.1415927F) * i * 3.0F));
            matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(Math.abs(MathHelper.cos(h * 3.1415927F - 0.2F) * i) * 5.0F));
        }

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
        info(content);
        try {
            mc.keyboard.setClipboard(content);
            toastManager.add(new SystemToast(SystemToast.Type.TUTORIAL_HINT, new TranslatableText("kzeaddon.copied_to_clipboard"), name));
        } catch (Exception ex) {
            toastManager.add(new SystemToast(SystemToast.Type.TUTORIAL_HINT, new TranslatableText("kzeaddon.copy_failed"), Text.of(ex.getMessage())));
            ex.printStackTrace();
        }
    }

    /**
     * Overload a {@link #drawPlayerHead(MatrixStack, GameProfile, int, int)}
     *
     * @param matrices MatrixStack
     * @param player   Skin get destination
     * @param x        Rendering offset in screen space
     * @param y        Rendering offset in screen space
     */
    @SuppressWarnings("unused")
    public static void drawPlayerHead(MatrixStack matrices, PlayerEntity player, int x, int y) {
        KZEAddon.drawPlayerHead(matrices, player.getGameProfile(), x, y);
    }

    @SuppressWarnings({"SpellCheckingInspection", "ConstantConditions"})
    public static void drawPlayerHead(MatrixStack matrices, GameProfile profile, int x, int y) {
        MinecraftClient client = MinecraftClient.getInstance();
        PlayerListEntry entry = client.player.networkHandler.getPlayerListEntry(profile.getId());
        PlayerEntity playerEntity = client.world.getPlayerByUuid(profile.getId());
        boolean bl2 = playerEntity != null && playerEntity.isPartVisible(PlayerModelPart.CAPE) && ("Dinnerbone".equals(profile.getName()) || "Grumm".equals(profile.getName()));
        client.getTextureManager().bindTexture(entry.getSkinTexture());
        int ad = 8 + (bl2 ? 8 : 0);
        int ae = 8 * (bl2 ? -1 : 1);
        DrawableHelper.drawTexture(matrices, x, y, 8, 8, 8.0F, (float) ad, 8, ae, 64, 64);
        if (playerEntity != null && playerEntity.isPartVisible(PlayerModelPart.HAT)) {
            int af = 8 + (bl2 ? 8 : 0);
            int ag = 8 * (bl2 ? -1 : 1);
            DrawableHelper.drawTexture(matrices, x, y, 8, 8, 40.0F, (float) af, 8, ag, 64, 64);
        }
    }

    public static void error(String msg) {
        MOD_LOG.error(msg);
    }

    public static void error(Text msg) {
        MOD_LOG.error(msg);
    }

    public static int executeCmd(CommandContext<ServerCommandSource> ctx) {
        WidgetsCommandArgumentType.Widgets widgetEnum = ctx.getArgument("name", WidgetsCommandArgumentType.Widgets.class);
        WidgetValueTargetArgumentType.WidgetValueTarget valueTarget = ctx.getArgument("target", WidgetValueTargetArgumentType.WidgetValueTarget.class);
        float prevValue;
        float value = ctx.getArgument("value", Float.class);
        Widget widget = getWidgetByEnum(widgetEnum);
        if (widget == null) return 0;
        switch (valueTarget) {
            case X:
                prevValue = widget.getOffsetX();
                widget.setOffsetX((int) value);
                break;
            case Y:
                prevValue = widget.getOffsetY();
                widget.setOffsetY((int) value);
                break;
            case SCALE:
                prevValue = widget.getScaleFactor();
                widget.setScaleFactor(value);
                break;
            case OPACITY:
                prevValue = widget.getOpacity();
                widget.setOpacity((short) value);
                break;
            default:
                return 0;
        }

        info(String.format("§6%s §7の §6%s §7を §6%.2f §7から §6%.2f §7に変更しました§r", widgetEnum.toString(), valueTarget.toString(), prevValue, value));
        return 1;
    }

    @SuppressWarnings("unused")
    public static double[] fromMatrix(Matrix4f matrix) {
        double[] result = new double[16];
        int i = 0;
        for (int y = 0; y < 4; y++) {
            for (int x = 0; x < 4; x++) {
                try {
                    Field field = Matrix4f.class.getDeclaredField("a" + y + "" + x);
                    field.setAccessible(true);
                    result[i++] = field.getFloat(matrix);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        return result;
    }

    @SuppressWarnings("ConstantConditions")
    public static Vec3f getCameraRightWorldSpace(Matrix4f viewMatrix) {
        return new Vec3f(
                ((Matrix4fAccessor) (Object) viewMatrix).a00(),
                ((Matrix4fAccessor) (Object) viewMatrix).a10(),
                ((Matrix4fAccessor) (Object) viewMatrix).a20()
        );
    }

    @SuppressWarnings("ConstantConditions")
    public static Vec3f getCameraUpWorldSpace(Matrix4f viewMatrix) {
        return new Vec3f(
                ((Matrix4fAccessor) (Object) viewMatrix).a01(),
                ((Matrix4fAccessor) (Object) viewMatrix).a11(),
                ((Matrix4fAccessor) (Object) viewMatrix).a21()
        );
    }

    /**
     * Get a matrix contents
     *
     * @param matrices target matrix
     * @return String array split by rows
     */
    @SuppressWarnings("unused")
    public static String[] getMatrixContents(MatrixStack matrices) {
        FloatBuffer buffer = GlAllocationUtils.allocateFloatBuffer(4 * 4 * 4);
        Matrix4f matrix = matrices.peek().getModel();
        matrix.writeRowFirst(buffer);
        String[] result = new String[4];
        StringBuilder builder = new StringBuilder();

        int i = 0;
        for (int y = 0; y < 4; y++) {
            for (int x = 0; x < 4; x++) {
                builder.append(String.format("%3.2f", buffer.get(i++))).append(" ");
            }
            result[y] = builder.toString();
            builder.setLength(0);
        }

        return result;
    }

    public static Profiler getProfiler() {
        return MinecraftClient.getInstance().getProfiler();
    }

    public static Matrix4f getProjectionMatrix(float delta) {
        MinecraftClient mc = MinecraftClient.getInstance();
        return mc.gameRenderer.getBasicProjectionMatrix(mc.gameRenderer.getCamera(), delta, true);
    }

    public static Matrix4f getVPMatrix(float delta) {
        Matrix4f projection = KZEAddon.getProjectionMatrix(delta);
        MatrixStack view = KZEAddon.getViewMatrix(delta);
        projection.multiply(view.peek().getModel());

        return projection;
    }

    public static MatrixStack getViewMatrix(float delta) {
        MinecraftClient mc = MinecraftClient.getInstance();
        Camera camera = mc.gameRenderer.getCamera();
        Vec3d pos = camera.getPos();
        MatrixStack matrix = new MatrixStack();

        if (mc.options.bobView) {
            bobView(matrix, delta);
        }

        matrix.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(camera.getPitch()));
        matrix.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(camera.getYaw() + 180.0F));
        matrix.translate(-pos.getX(), -pos.getY(), -pos.getZ());

        return matrix;
    }

    private static Widget getWidgetByEnum(WidgetsCommandArgumentType.Widgets name) {
        switch (name) {
            case MAIN_W:
                return Options.getPrimaryAmmo();
            case SUB_W:
                return Options.getSecondaryAmmo();
            case MELEE_W:
                return Options.getMeleeAmmo();
            case RELOAD_TIME:
                return Options.getReloadIndicator();
            case TOTAL_AMMO:
                return Options.getTotalAmmo();
            default:
                return null;
        }
    }

    /**
     * 現在いるワールドの時間を取得します
     *
     * @return ワールドに入っている場合、そのワールドの時間 入っていないと -1
     */
    public static long getWorldTime() {
        return Optional.ofNullable(MinecraftClient.getInstance().world).map(World::getTime).orElse(-1L);
    }

    public static void glError(String location) {
        int error = GL33.glGetError();
        if (error != 0) {
            String msg = "";
            if (KZEAddon.GL_ERRORS.containsKey(error)) msg = KZEAddon.GL_ERRORS.get(error);
            info(Text.Serializer.fromJson("{\"translate\":\"%s%s%s %s %s (%s)\",\"with\":[" +
                    "{\"text\":\"[\",\"color\":\"gold\"}," +
                    "{\"text\":\"ERROR\",\"color\":\"red\"}," +
                    "{\"text\":\"]\",\"color\":\"gold\"}," +
                    "{\"text\":\"" + location + "\",\"color\":\"white\"}," +
                    "{\"text\":\"" + error + "\",\"color\":\"white\"}," +
                    "{\"text\":\"" + msg + "\",\"color\":\"white\"}" +
                    "]}"));
        }
    }

    /**
     * Event on the render item tooltip in gui
     *
     * @param stack   Item below cursor
     * @param context I DON'T KNOW
     * @param list    Tooltip rows list
     */
    @SuppressWarnings("unused")
    public static void handleItemTooltip(ItemStack stack, TooltipContext context, List<Text> list) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.currentScreen == null) return;
        boolean isPressed = InputUtil.isKeyPressed(mc.getWindow().getHandle(), COPY_ITEM_TAG.getCode());
        NbtElement tag = stack.getTag();
        if (tag != null) {
            String str = tag.toText().getString();
            if (isPressed) {
                if (!KEY_FLIPFLOP_COPY) {
                    KEY_FLIPFLOP_COPY = true;
                    copyToClipboard(new TranslatableText(stack.getTranslationKey()), str);
                }
            } else {
                if (KEY_FLIPFLOP_COPY) {
                    KEY_FLIPFLOP_COPY = false;
                }
            }
        }

        if (context.isAdvanced()) {
            NbtCompound nbt = stack.getTag();
            if (nbt != null && nbt.contains("CustomModelData")) {
                int customModelData = nbt.getInt("CustomModelData");
                list.add(list.size() - 2, Text.of("----------"));
                visualizeNbt("CustomModelData,HideFlags,Unbreakable,Damage", nbt, list, list.size() - 2);
            }
        }
    }

    /**
     * See {@link KZEAddonLog#info(String)}
     */
    public static void info(String msg) {
        MOD_LOG.info(msg);
    }

    /**
     * See {@link KZEAddonLog#info(Text)}
     */
    public static void info(Text msg) {
        MOD_LOG.info(msg);
    }

    public static int instanceParticleCmd(CommandContext<ServerCommandSource> ctx) {
        ServerCommandSource source = ctx.getSource();
        int divisor = IntegerArgumentType.getInteger(ctx, "divisor");
        Vec3d start = Vec3ArgumentType.getPosArgument(ctx, "start").toAbsolutePos(source);
        Vec3d end = Vec3ArgumentType.getPosArgument(ctx, "end").toAbsolutePos(source);
        Vec3d diff = end.subtract(start).multiply(1.0 / divisor);
        Random rand = new Random();
        for (int i = 0; i < divisor; i++) {
            Vec3d pos = start.add(diff.multiply(i));
            InstancedPerlinParticleManager.ParticleInstance particle = new InstancedPerlinParticleManager.ParticleInstance(pos.x, pos.y, pos.z, 200, false, rand.nextDouble());
            InstancedPerlinParticleManager.INSTANCE.addParticle(particle);
        }
        return 1;
    }

    public static boolean isShowModLog() {
        return showModLog;
    }

    public static void setShowModLog(boolean isShow) {
        showModLog = isShow;
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
        return MinecraftClient.getInstance().player.isTeammate(entity);
    }

    public static int killLogDebug(CommandContext<ServerCommandSource> ctx) {
        int index = IntegerArgumentType.getInteger(ctx, "index");
        boolean isTargetVictim = IntegerArgumentType.getInteger(ctx, "isTargetVictim") == 1;
        KillLog.LogEntry entry = KZE_INFO.getKillLog().get(index);
        info(isTargetVictim ? entry.getVictim() : entry.getAttacker());
        return 1;
    }

    /**
     * Create a config instance from specify a file.
     */
    public static void loadConfig() {
        try {
            Gson gson = new GsonBuilder().create();
            JsonReader reader = new JsonReader(new FileReader(optionsFile));
            Options = gson.fromJson(reader, Options.class);
            Options.initWidgets();
            KZEAddon.BAR_VISUALIZER.setDistance(Options.getBarrierVisualizeRadius());
        } catch (Exception e) {
            LOGGER.warn("Config file load failed");
            resetConfig();
        }
    }

    /**
     * Open the config file by system default editor.
     *
     * @return Open successfully to true, otherwise false
     * <p>
     * Available on windows only
     */
    public static boolean openConfigWithEditor() {
        try {
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                String cmd = "rundll32 url.dll,FileProtocolHandler " + optionsFile.toPath().toAbsolutePath();
                Runtime.getRuntime().exec(cmd);
            } else {
                Desktop.getDesktop().edit(optionsFile);
            }
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public static String patternToEntities(String pattern, ServerCommandSource source) {
        String result = null;
        try {
            EntitySelectorReader reader = new EntitySelectorReader(new StringReader(pattern));
            EntitySelector selector = reader.read();
            List<? extends Entity> entities = selector.getEntities(source);
            StringBuilder builder = new StringBuilder();
            for (Entity entity : entities) {
                builder.append(entity.getCustomName()).append(", ");
            }
            result = builder.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return result;
    }

    private static int perlinParticle(CommandContext<ServerCommandSource> ctx) {
        Vec3d pos;
        int count;
        try {
            pos = Vec3ArgumentType.getVec3(ctx, "pos");
            count = IntegerArgumentType.getInteger(ctx, "count");
        } catch (CommandSyntaxException ex) {
            return 0;
        }

        Random rand = new Random();
        float div = 1F / Math.max(count, 1);
        double x = pos.getX();
        double y = pos.getY();
        double z = pos.getZ();
        float seed = rand.nextFloat();

        int particleNum = 0;
        for (int xI = 0; xI < count; xI++) {
            for (int yI = 0; yI < count; yI++) {
                for (int zI = 0; zI < count; zI++) {
                    if (xI == 0 || xI == count - 1 || yI == 0 || yI == count - 1 || zI == 0 || zI == count - 1) {
                        PerlinParticle pp = new PerlinParticle(x + xI * div, y + yI * div, z + zI * div, 60 + (int) (rand.nextFloat() * 20 - 10), seed);
                        pp.setIsShouldMove(() -> pp.getAge() > 20);
                        PARTICLES.add(pp);
                        particleNum++;
                    }
                }
            }
        }

        ctx.getSource().sendFeedback(Text.of(particleNum + " Particle(s) added"), false);

        return 1;
    }

    /**
     * Get the target block
     *
     * @param entity   Ray caster
     * @param distance Max distance
     * @return Hit result {@link BlockHitResult} or {@link EntityHitResult}
     */
    public static HitResult raycastIgnoreBlock(Entity entity, double distance) {
        long time = -System.currentTimeMillis();
        HitResult result;
        Vec3d vec3d = entity.getCameraPosVec(1.0F);
        Vec3d vec3d2 = entity.getRotationVec(1.0F);
        Vec3d vec3d3 = vec3d.add(vec3d2.x * distance, vec3d2.y * distance, vec3d2.z * distance);
        Box box = entity.getBoundingBox().stretch(vec3d2.multiply(distance)).expand(1.0D, 1.0D, 1.0D);
        EntityHitResult entityHitResult = ProjectileUtil.raycast(entity, vec3d, vec3d3, box, entityx -> !entityx.isSpectator() && entityx.collides() && entityx.getType().equals(EntityType.PLAYER), distance * distance);
        if (entityHitResult == null) {
            result = BlockHitResult.createMissed(entity.getPos(), Direction.getFacing(vec3d2.x, vec3d2.y, vec3d2.z), new BlockPos(entity.getPos()));
        } else {
            info("raycast > Entity found");
            info("raycast > Distance: " + String.format("%.2f", entity.distanceTo(entityHitResult.getEntity())));
            result = entityHitResult;
        }
        time += System.currentTimeMillis();
        info("raycast > Elapsed time: " + String.format("%.2f", time / 1000.0));
        return result;
    }

    /**
     * From fabric keybinding api
     *
     * @param keysAll keys array
     * @return processed keys array
     */
    public static KeyBinding[] registerKeybindings(KeyBinding[] keysAll) {
        Map<String, Integer> categoryMap = KeyBindingAccessor.fabric_getCategoryMap();
        Optional<Integer> largest = categoryMap.values().stream().max(Integer::compareTo);
        int largestInt = largest.orElse(0);
        for (KeyBinding key : modKeys) {
            String category = key.getCategory();
            if (!categoryMap.containsKey(category)) categoryMap.put(category, largestInt++);
        }

        List<KeyBinding> list = Lists.newArrayList(keysAll);
        list.removeAll(modKeys);
        list.addAll(modKeys);
        return list.toArray(new KeyBinding[0]);
    }

    public static String removeHeadColorCode(String str) {
        str = str.replaceAll(CC_REGEX, "");
        if (str.matches(CC_REGEX)) {
            return removeHeadColorCode(str);
        } else {
            return str;
        }
    }

    @SuppressWarnings({"unused", "ConstantConditions"})
    public static void renderPlayerHead(MatrixStack matrices, float tickDelta) {
        float texSize = 64;
        MinecraftClient client = MinecraftClient.getInstance();
        List<AbstractClientPlayerEntity> players = client.world.getPlayers();
        ClientPlayerEntity mainPlayer = client.player;
        Camera camera = client.gameRenderer.getCamera();
        Vec3d cam = camera.getPos();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        for (AbstractClientPlayerEntity player : players) {
            if (player.equals(mainPlayer)) continue;
            client.getTextureManager().bindTexture(client.player.networkHandler.getPlayerListEntry(player.getGameProfile().getId()).getSkinTexture());
            matrices.push();
            Matrix4f matrix = matrices.peek().getModel();
            matrices.translate(MathHelper.lerp(tickDelta, player.lastRenderX, player.getX()) - cam.x, MathHelper.lerp(tickDelta, player.lastRenderY, player.getY()) + 2.5 - cam.y, MathHelper.lerp(tickDelta, player.lastRenderZ, player.getZ()) - cam.z);
            matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(-camera.getYaw()));
            matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(camera.getPitch()));
            buffer.begin(GL11.GL_QUADS, VertexFormats.POSITION_TEXTURE);
            buffer.vertex(matrix, 0.5F, 0.5F, 0).texture(8 / texSize, 8 / texSize).next();
            buffer.vertex(matrix, 0.5F, -0.5F, 0).texture(8 / texSize, 16 / texSize).next();
            buffer.vertex(matrix, -0.5F, -0.5F, 0).texture(16 / texSize, 16 / texSize).next();
            buffer.vertex(matrix, -0.5F, 0.5F, 0).texture(16 / texSize, 8 / texSize).next();
            tessellator.draw();
            buffer.begin(GL11.GL_QUADS, VertexFormats.POSITION_TEXTURE);
            buffer.vertex(matrix, 0.6F, 0.6F, 0).texture(40 / texSize, 8 / texSize).next();
            buffer.vertex(matrix, 0.6F, -0.6F, 0).texture(40 / texSize, 16 / texSize).next();
            buffer.vertex(matrix, -0.6F, -0.6F, 0).texture(48 / texSize, 16 / texSize).next();
            buffer.vertex(matrix, -0.6F, 0.6F, 0).texture(48 / texSize, 8 / texSize).next();
            RenderSystem.enableBlend();
            RenderSystem.enablePolygonOffset();
            RenderSystem.polygonOffset(-1, -1);
            tessellator.draw();
            RenderSystem.disablePolygonOffset();
            RenderSystem.enableBlend();
            matrices.pop();
        }
    }

    /**
     * Restore the default values of config instance
     */
    public static void resetConfig() {
        try {
            if (!optionsFile.exists())
                if (!optionsFile.createNewFile()) LOGGER.error("Config file create failed");
        } catch (IOException e) {
            e.printStackTrace();
        }
        Options = new Options();
        Options.initWidgets();
        saveConfig();
    }

    /**
     * Create or override config file by config instance.
     */
    public static void saveConfig() {
        try {
            FileWriter fWriter = new FileWriter(optionsFile);
            PrintWriter pWriter = new PrintWriter(new BufferedWriter(fWriter));
            Gson gson = new GsonBuilder().setPrettyPrinting().excludeFieldsWithoutExposeAnnotation().create();
            String content = gson.toJson(Options);
            pWriter.print(content);
            pWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static int setBVRadius(CommandContext<ServerCommandSource> ctx) {
        float radius = FloatArgumentType.getFloat(ctx, "value");
        InstancedBarrierVisualizer.INSTANCE.setRadius(radius);
        info("Radius is " + radius);
        return 1;
    }

    private static int setBarrierColorCmd(CommandContext<ServerCommandSource> ctx) {
        int red = IntegerArgumentType.getInteger(ctx, "red");
        int green = IntegerArgumentType.getInteger(ctx, "green");
        int blue = IntegerArgumentType.getInteger(ctx, "blue");
        int alpha = IntegerArgumentType.getInteger(ctx, "alpha");
        BarrierShader.INSTANCE.setColor(red / 255F, green / 255F, blue / 255F, alpha / 255F);
        info(Text.of(String.format("Color changed to %d %d %d %d", red, green, blue, alpha)));
        return 1;
    }

    public static String textAsString(Text text) {
        StringBuilder builder = new StringBuilder();
        text.visit(asString -> {
            builder.append(asString);
            return Optional.empty();
        });
        return builder.toString();
    }

    public static void tickKeys() {
        KZEAddon.ADD_GROW_TARGET.tick();
        KZEAddon.HIDE_PLAYERS.tick();
        KZEAddon.DEBUG_KEY.tick();
    }

    @SuppressWarnings("unused")
    public static void toMatrix(Matrix4f matrix, double[] contents) {
        for (int x = 0; x < 4; x++) {
            for (int y = 0; y < 4; y++) {
                try {
                    Field field = Matrix4f.class.getDeclaredField("a" + x + "" + y);
                    field.setAccessible(true);
                    field.set(matrix, (float) contents[y * 4 + x]);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public static String toShortString(Vec3d vec) {
        return String.format("%.3f, %.3f, %.3f", vec.getX(), vec.getY(), vec.getZ());
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

    public static void warn(String msg) {
        MOD_LOG.warn(msg);
    }

    public static void warn(Text msg) {
        MOD_LOG.warn(msg);
    }

    public static void xPlane(MatrixStack matrices, float x, float y0, float z0, float y1, float z1, float u0, float v0, float u1, float v1) {
        Matrix4f matrix = matrices.peek().getModel();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        buffer.begin(GL11.GL_QUADS, VertexFormats.POSITION_TEXTURE);
        buffer.vertex(matrix, x, y0, z0).texture(u0, v0).next();
        buffer.vertex(matrix, x, y0, z1).texture(u0, v1).next();
        buffer.vertex(matrix, x, y1, z1).texture(u1, v1).next();
        buffer.vertex(matrix, x, y1, z0).texture(u1, v0).next();
        tessellator.draw();
    }

    public static void yPlane(MatrixStack matrices, float y, float x0, float z0, float x1, float z1, float u0, float v0, float u1, float v1) {
        Matrix4f matrix = matrices.peek().getModel();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        buffer.begin(GL11.GL_QUADS, VertexFormats.POSITION_TEXTURE);
        buffer.vertex(matrix, x0, y, z0).texture(u0, v0).next();
        buffer.vertex(matrix, x0, y, z1).texture(u0, v1).next();
        buffer.vertex(matrix, x1, y, z1).texture(u1, v1).next();
        buffer.vertex(matrix, x1, y, z0).texture(u1, v0).next();
        tessellator.draw();
    }

    public static void zPlane(MatrixStack matrices, float z, float x0, float y0, float x1, float y1, float u0, float v0, float u1, float v1) {
        Matrix4f matrix = matrices.peek().getModel();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        buffer.begin(GL11.GL_QUADS, VertexFormats.POSITION_TEXTURE);
        buffer.vertex(matrix, x0, y0, z).texture(u0, v0).next();
        buffer.vertex(matrix, x1, y0, z).texture(u1, v0).next();
        buffer.vertex(matrix, x1, y1, z).texture(u1, v1).next();
        buffer.vertex(matrix, x0, y1, z).texture(u0, v1).next();
        tessellator.draw();
    }

    /**
     * Mod initialization
     */
    @Override
    public void onInitializeClient() {
        MinecraftClient mc = MinecraftClient.getInstance();

        optionsFile = new File(mc.runDirectory.getAbsolutePath() + "\\config\\" + MOD_ID + ".json");
        loadConfig();
        this.registerKeybindings();

        Registry.register(Registry.SOUND_EVENT, CustomSounds.HONK_ID, CustomSounds.HONK_EVENT);
        Registry.register(Registry.SOUND_EVENT, CustomSounds.VOTE_NOTIFIC_ID, CustomSounds.VOTE_NOTIFIC_EVENT);

        MOD_LOG.init();

        TEST_LAYERS = new PlaneLayers(
                new PlaneLayers.Plane(new Identifier(MOD_ID, "textures/wip/layer_0.png")) {
                    private double time = 0;

                    @Override
                    public void render(Tessellator tessellator, BufferBuilder buffer, MatrixStack matrices, float delta) {
                        this.bindTexture();
                        double time = getWorldTime();
                        double lerp = MathHelper.lerp(delta, this.time, time);
                        this.time = time;
                        RenderSystem.enableBlend();
                        matrices.push();
                        matrices.multiply(Vec3f.NEGATIVE_Y.getDegreesQuaternion((float) (lerp % 60 / 60 * 360)));
                        yPlane(matrices, 0F, -1F, -1F, 1F, 1F, 0F, 0F, 1F, 1F);
                        matrices.pop();
                        RenderSystem.disableBlend();

                    }
                },
                new PlaneLayers.Plane(new Identifier(MOD_ID, "textures/wip/layer_1.png")) {
                    private double time = 0;

                    @Override
                    public void render(Tessellator tessellator, BufferBuilder buffer, MatrixStack matrices, float delta) {
                        this.bindTexture();
                        double time = getWorldTime();
                        double lerp = MathHelper.lerp(delta, this.time, time);
                        this.time = time;
                        RenderSystem.enableBlend();
                        matrices.push();
                        matrices.translate(0, 0.01F, 0);
                        matrices.multiply(Vec3f.NEGATIVE_Y.getDegreesQuaternion(360 - (float) (lerp % 60 / 60 * 360)));
                        yPlane(matrices, 0F, -1F, -1F, 1F, 1F, 0F, 0F, 1F, 1F);
                        matrices.pop();
                        RenderSystem.disableBlend();
                    }
                },
                new PlaneLayers.Plane(new Identifier(MOD_ID, "textures/wip/layer_2.png")) {
                    private double time = 0;

                    @Override
                    public void render(Tessellator tessellator, BufferBuilder buffer, MatrixStack matrices, float delta) {
                        this.bindTexture();
                        double time = getWorldTime();
                        double lerp = MathHelper.lerp(delta, this.time, time);
                        this.time = time;
                        RenderSystem.enableBlend();
                        matrices.push();
                        matrices.translate(0, 0.015F, 0);
                        matrices.multiply(Vec3f.NEGATIVE_Y.getDegreesQuaternion(360 - (float) (lerp % 60 / 60 * 360)));
                        yPlane(matrices, 0F, -1F, -1F, 1F, 1F, 0F, 0F, 1F, 1F);
                        matrices.pop();
                        RenderSystem.disableBlend();
                    }
                },
                new PlaneLayers.Plane(new Identifier(MOD_ID, "textures/wip/layer_sp.png")) {
                    private double time = 0;

                    @Override
                    public void render(Tessellator tessellator, BufferBuilder buffer, MatrixStack matrices, float delta) {
                        this.bindTexture();
                        double time = getWorldTime();
                        double lerp = MathHelper.lerp(delta, this.time, time);
                        this.time = time;
                        matrices.push();
                        matrices.translate(0, 0.020F, 0);
                        matrices.multiply(Vec3f.NEGATIVE_Y.getDegreesQuaternion(360 - (float) (lerp % 60 / 60 * 360)));
                        matrices.translate(0.8F, 0F, 0F);
                        matrices.multiply(Vec3f.NEGATIVE_Y.getDegreesQuaternion((float) (lerp % 60 / 60 * 360)));
                        RenderSystem.enableBlend();
                        yPlane(matrices, 0F, -0.2F, -0.2F, 0.2F, 0.2F, 0F, 0F, 1F, 1F);
                        RenderSystem.disableBlend();
                        matrices.pop();
                    }
                }
        );

        ClientTickEvents.START_CLIENT_TICK.register(EventsListener::onTick);
        ClientLifecycleEvents.CLIENT_STOPPING.register(EventsListener::onClientStop);
        this.registerCommands();
        this.registerClientCommands();
    }

    public void registerClientCommands() {
        CommandDispatcher<FabricClientCommandSource> dispatcher = ClientCommandManager.DISPATCHER;

        DebugCommand.registerClientCommands(dispatcher);
    }

    public void registerCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            RootCommandNode<ServerCommandSource> root = dispatcher.getRoot();
            LiteralCommandNode<ServerCommandSource> widget = CommandManager.literal("widget").build();
            LiteralCommandNode<ServerCommandSource> verify = CommandManager.literal("verify").build();
            LiteralCommandNode<ServerCommandSource> set = CommandManager.literal("set").build();
            ArgumentCommandNode<ServerCommandSource, WidgetsCommandArgumentType.Widgets> name = CommandManager.argument("name", WidgetsCommandArgumentType.widget()).build();
            ArgumentCommandNode<ServerCommandSource, WidgetValueTargetArgumentType.WidgetValueTarget> operation = CommandManager.argument("target", WidgetValueTargetArgumentType.operation()).build();
            ArgumentCommandNode<ServerCommandSource, Float> value = CommandManager.argument("value", FloatArgumentType.floatArg()).executes(KZEAddon::executeCmd).build();
            LiteralCommandNode<ServerCommandSource> killLog = CommandManager.literal("killlog").build();
            ArgumentCommandNode<ServerCommandSource, Integer> index = CommandManager.argument("index", IntegerArgumentType.integer(0)).build();
            ArgumentCommandNode<ServerCommandSource, Integer> isTargetVictim = CommandManager.argument("isTargetVictim", IntegerArgumentType.integer(0, 1)).executes(KZEAddon::killLogDebug).build();
            LiteralCommandNode<ServerCommandSource> perlin = CommandManager.literal("perlin").build();
            ArgumentCommandNode<ServerCommandSource, PosArgument> perlinPos = CommandManager.argument("pos", Vec3ArgumentType.vec3()).build();
            ArgumentCommandNode<ServerCommandSource, Integer> perlinCount = CommandManager.argument("count", IntegerArgumentType.integer(1)).executes(KZEAddon::perlinParticle).build();
            LiteralCommandNode<ServerCommandSource> iparticle = CommandManager.literal("iparticle").build();
            ArgumentCommandNode<ServerCommandSource, Integer> divisor = CommandManager.argument("divisor", IntegerArgumentType.integer(1)).build();
            ArgumentCommandNode<ServerCommandSource, PosArgument> start = CommandManager.argument("start", Vec3ArgumentType.vec3()).build();
            ArgumentCommandNode<ServerCommandSource, PosArgument> end = CommandManager.argument("end", Vec3ArgumentType.vec3()).executes(KZEAddon::instanceParticleCmd).build();
            LiteralCommandNode<ServerCommandSource> ibv = CommandManager.literal("ibv").build();
            ArgumentCommandNode<ServerCommandSource, Integer> red = CommandManager.argument("red", IntegerArgumentType.integer(0, 255)).build();
            ArgumentCommandNode<ServerCommandSource, Integer> green = CommandManager.argument("green", IntegerArgumentType.integer(0, 255)).build();
            ArgumentCommandNode<ServerCommandSource, Integer> blue = CommandManager.argument("blue", IntegerArgumentType.integer(0, 255)).build();
            ArgumentCommandNode<ServerCommandSource, Integer> alpha = CommandManager.argument("alpha", IntegerArgumentType.integer(0, 255)).executes(KZEAddon::setBarrierColorCmd).build();
            LiteralCommandNode<ServerCommandSource> radius = CommandManager.literal("radius").build();
            ArgumentCommandNode<ServerCommandSource, Float> radiusValue = CommandManager.argument("value", FloatArgumentType.floatArg(0.0F)).executes(KZEAddon::setBVRadius).build();

            root.addChild(perlin);
            perlin.addChild(perlinPos);
            perlinPos.addChild(perlinCount);

            root.addChild(widget);
            widget.addChild(set);
            widget.addChild(verify);
            set.addChild(name);
            name.addChild(operation);
            operation.addChild(value);

            root.addChild(killLog);
            killLog.addChild(index);
            index.addChild(isTargetVictim);

            root.addChild(iparticle);
            iparticle.addChild(divisor);
            divisor.addChild(start);
            start.addChild(end);

            root.addChild(ibv);
            ibv.addChild(red);
            red.addChild(green);
            green.addChild(blue);
            blue.addChild(alpha);

            ibv.addChild(radius);
            radius.addChild(radiusValue);

            DebugCommand.register(dispatcher);
        });
    }

    private void registerKeybindings() {
        ADD_GROW_TARGET = new KeyBindingWrapper("key.kzeaddon.glow.priority.add", GLFW.GLFW_KEY_G, "key.categories.kzeaddon.in_game",
                unused -> {
                    @SuppressWarnings("ConstantConditions")
                    HitResult result = raycastIgnoreBlock(MinecraftClient.getInstance().player, 100.0);

                    if (result.getType() == HitResult.Type.ENTITY) {
                        UUID uuid = ((EntityHitResult) result).getEntity().getUuid();
                        if (!KZEAddon.priorityGlowPlayers.contains(uuid)) {
                            KZEAddon.priorityGlowPlayers.add(uuid);
                        } else {
                            KZEAddon.priorityGlowPlayers.remove(uuid);
                        }
                    }
                },
                unused -> {}
        );

        KZEAddon.COPY_ITEM_TAG = new KeyBindingWrapper("key.kzeaddon.wip.copy_item_tag", GLFW.GLFW_KEY_H, "key.categories.kzeaddon.wip");
        KZEAddon.HIDE_PLAYERS = new KeyBindingWrapper("key.kzeaddon.hide_teammates", GLFW.GLFW_KEY_R, "key.categories.kzeaddon.in_game", key -> {
            if (KZEAddon.Options.getHideTeammates() == Switchable.HOLD) KZEAddon.isHideTeammates = true;
            else if (KZEAddon.Options.getHideTeammates() == Switchable.TOGGLE) KZEAddon.isHideTeammates = !KZEAddon.isHideTeammates;
        }, key -> {
            if (KZEAddon.Options.getHideTeammates() == Switchable.HOLD) KZEAddon.isHideTeammates = false;
        });
        KZEAddon.DEBUG_KEY = new KeyBindingWrapper("key.kzeaddon.debug", GLFW.GLFW_KEY_RIGHT_BRACKET, "key.categories.kzeaddon.wip", key -> {
            MOD_LOG.openLogScreen();
        });
    }
}
