package com.theboss.kzeaddonfabric;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import com.theboss.kzeaddonfabric.enums.Switchable;
import com.theboss.kzeaddonfabric.ingame.KillLog;
import com.theboss.kzeaddonfabric.mixin.client.KeyBindingAccessor;
import com.theboss.kzeaddonfabric.render.BarrierVisualizer;
import com.theboss.kzeaddonfabric.render.widgets.Widget;
import com.theboss.kzeaddonfabric.wip.PlaneLayers;
import com.theboss.kzeaddonfabric.wip.WidgetValueTargetArgumentType;
import com.theboss.kzeaddonfabric.wip.WidgetsCommandArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
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
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtElement;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.Team;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

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
    public static final String MOD_NAME = "KZEAddon-Fabric";
    public static final String MOD_ID = "kzeaddon-fabric";
    public static final List<UUID> priorityGlowPlayers = new ArrayList<>();
    public static final List<KeyBinding> modKeys = new ArrayList<>();
    public static KeyBindingWrapper ADD_GROW_TARGET;
    public static KeyBindingWrapper COPY_ITEM_TAG;
    public static KeyBindingWrapper DEBUG_KEY;
    public static KeyBindingWrapper HIDE_PLAYERS;
    public static boolean KEY_FLIPFLOP_COPY = false;
    public static Options Options;
    public static boolean isHideTeammates;
    public static StringBuilder S_BUILDER = new StringBuilder();
    public static PlaneLayers TEST_LAYERS = new PlaneLayers();
    private static File optionsFile;

    /**
     * Add text the chat log using format
     *
     * @param format Text format see the {@link String#format(String, Object...)}
     * @param args   arguments
     */
    @SuppressWarnings("unused")
    public static void addChatLog(String format, Object... args) {
        addChatLog(String.format(format, args));
    }

    /**
     * Add text the chat log
     *
     * @param msg Text to add
     */
    public static void addChatLog(String msg) {
        addChatLog(Text.of(msg));
    }

    /**
     * Add text the chat log
     *
     * @param text Text to add
     */
    public static void addChatLog(Text text) {
        MinecraftClient client = MinecraftClient.getInstance();
        client.inGameHud.getChatHud().addMessage(text);
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
        System.out.println(content);
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

        KZEAddon.addChatLog("§6%s §7の §6%s §7を §6%.2f §7から §6%.2f §7に変更しました§r", widgetEnum.toString(), valueTarget.toString(), prevValue, value);
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

    public static long getWorldTime() {
        return MinecraftClient.getInstance().world.getTime();
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
        addChatLog(isTargetVictim ? entry.getVictim() : entry.getAttacker());
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
     * Private rendering system
     *
     * @param client A minecraft client instance
     */
    @SuppressWarnings("unused")
    public static void onClientStop(MinecraftClient client) {
        BAR_VISUALIZER.destroy();
        saveConfig();
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
     * Hud render event listener
     *
     * @param matrices {@link MatrixStack}
     * @param delta    A rendering delay
     */
    @SuppressWarnings("unused")
    public static void onRenderHud(MatrixStack matrices, float delta) {
        Profiler profiler = getProfiler();
        profiler.push("KZEAddon$onRenderHud");

        profiler.push("Widgets");
        Options.renderWidgets(matrices);
        profiler.swap("KillLog");
        if (MinecraftClient.getInstance().player.isSneaking()) {
            KZE_INFO.getKillLog().render(matrices, delta);
        }

        profiler.pop();
        profiler.pop();
    }

    /**
     * Private rendering system initialization
     */
    public static void onRenderInit() {
        BAR_VISUALIZER.init();
        BAR_VISUALIZER.setDistance(Options.getBarrierVisualizeRadius());
    }

    /**
     * Render world event listener method
     * Not expect call by you
     *
     * @param matrices {@link net.minecraft.client.util.math.MatrixStack}
     * @param delta    A render delay
     */
    @SuppressWarnings("unused")
    public static void onRenderWorld(MatrixStack matrices, float delta) {
        Profiler profiler = getProfiler();

        profiler.swap("KZEAddon$onRenderWorld");
        profiler.push("Barrier Visualizer");
        BAR_VISUALIZER.draw(matrices, delta);
        profiler.pop();
        // MARKED_AREA.render(matrices, tickDelta);
        // MinecraftClient client = MinecraftClient.getInstance();
        // Vec3d pos = client.cameraEntity.getPos();
        // Entity entity = client.cameraEntity;
        /* TEST_LAYERS.render(matrices,
                (float) MathHelper.lerp(delta, entity.lastRenderX, pos.getX()),
                (float) MathHelper.lerp(delta, entity.lastRenderY, pos.getY()),
                (float) MathHelper.lerp(delta, entity.lastRenderZ, pos.getZ()),
                delta);
         */
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
        KZEAddon.BAR_VISUALIZER.tick();
        profiler.swap("KZE Information tick");
        KZEAddon.KZE_INFO.tick();
        profiler.swap("Keys tick");
        KZEAddon.tickKeys();

        profiler.pop();
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
            addChatLog("raycast > Entity found");
            addChatLog("raycast > Distance: " + String.format("%.2f", entity.distanceTo(entityHitResult.getEntity())));
            result = entityHitResult;
        }
        time += System.currentTimeMillis();
        addChatLog("raycast > Elapsed time: " + String.format("%.2f", time / 1000.0));
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
        MinecraftClient client = MinecraftClient.getInstance();

        optionsFile = new File(client.runDirectory.getAbsolutePath() + "\\config\\" + MOD_ID + ".json");
        loadConfig();
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
        });

        Registry.register(Registry.SOUND_EVENT, CustomSounds.HONK_ID, CustomSounds.HONK_EVENT);
        Registry.register(Registry.SOUND_EVENT, CustomSounds.VOTE_NOTIFIC_ID, CustomSounds.VOTE_NOTIFIC_EVENT);

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

        ClientTickEvents.START_CLIENT_TICK.register(KZEAddon::onTick);
        ClientLifecycleEvents.CLIENT_STOPPING.register(KZEAddon::onClientStop);
        this.registerCommands();
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

            root.addChild(widget);
            widget.addChild(set);
            widget.addChild(verify);
            set.addChild(name);
            name.addChild(operation);
            operation.addChild(value);

            root.addChild(killLog);
            killLog.addChild(index);
            index.addChild(isTargetVictim);
        });
    }
}
