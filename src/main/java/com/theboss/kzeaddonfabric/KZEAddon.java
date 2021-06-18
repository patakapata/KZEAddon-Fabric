package com.theboss.kzeaddonfabric;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.systems.RenderSystem;
import com.theboss.kzeaddonfabric.enums.Anchor;
import com.theboss.kzeaddonfabric.enums.Switchable;
import com.theboss.kzeaddonfabric.events.GetTeamColorValueEvent;
import com.theboss.kzeaddonfabric.mixin.client.KeyBindingAccessor;
import com.theboss.kzeaddonfabric.render.BarrierVisualizer;
import com.theboss.kzeaddonfabric.screen.WidgetConfigureScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.options.KeyBinding;
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
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.Tag;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.awt.*;
import java.io.*;
import java.lang.reflect.Field;
import java.nio.FloatBuffer;
import java.util.List;
import java.util.*;

@Environment(EnvType.CLIENT)
public class KZEAddon implements ClientModInitializer {
    public static final Logger LOGGER = LogManager.getLogger("KZEAddon-Fabric");
    public static final String MOD_ID = "kzeaddon-fabric";
    public static final BarrierVisualizer BAR_VISUALIZER = new BarrierVisualizer();
    public static final KZEInformation KZE_INFO = new KZEInformation();
    public static final List<UUID> priorityGlowPlayers = new ArrayList<>();
    public static Options Options;

    public static List<KeyBinding> modKeys = new ArrayList<>();
    public static KeyBindingWrapper ADD_GROW_TARGET;
    public static KeyBindingWrapper HIDE_PLAYERS;
    public static KeyBindingWrapper DEBUG_KEY;
    public static KeyBindingWrapper COPY_ITEM_TAG;

    public static boolean KEY_FLIPFLOP_COPY = false;
    public static boolean isHideTeammates;
    private static File optionsFile;

    /**
     * Get the entity glow color event
     *
     * @param entity Target entity
     * @param cir    Returnable callback info
     */
    public static void onGetTeamColorValue(Entity entity, CallbackInfoReturnable<Integer> cir) {
        AbstractTeam team = entity.getScoreboardTeam();
        if (team != null) {
            String name = team.getName();
            if (KZEAddon.priorityGlowPlayers.contains(entity.getUuid())) {
                cir.setReturnValue(KZEAddon.Options.getPriorityGlowColor().get());
            } else if (name.equals("e")) {
                cir.setReturnValue(KZEAddon.Options.getHumanGlowColor().get());
            } else if (name.equals("z")) {
                cir.setReturnValue(KZEAddon.Options.getZombieGlowColor().get());
            }
        }
    }

    /**
     * Get a matrix contents
     *
     * @param matrices target matrix
     * @return String array split by rows
     */
    public static String[] getMatrixContents(MatrixStack matrices) {
        FloatBuffer buffer = GlAllocationUtils.allocateFloatBuffer(4 * 4 * 4);
        Matrix4f matrix = matrices.peek().getModel();
        matrix.writeToBuffer(buffer);
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

    /**
     * Event on the render item tooltip in gui
     *
     * @param stack   Item below cursor
     * @param context I DON'T KNOW
     * @param list    Tooltip rows list
     */
    public static void handleItemTooltip(ItemStack stack, TooltipContext context, List<Text> list) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.currentScreen == null) return;
        boolean isPressed = InputUtil.isKeyPressed(mc.getWindow().getHandle(), COPY_ITEM_TAG.getCode());
        Tag tag = stack.getTag();
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
     * Overload a {@link #drawPlayerHead(MatrixStack, GameProfile, int, int)}
     *
     * @param matrices MatrixStack
     * @param player   Skin get destination
     * @param x        Rendering offset in screen space
     * @param y        Rendering offset in screen space
     */
    public static void drawPlayerHead(MatrixStack matrices, PlayerEntity player, int x, int y) {
        KZEAddon.drawPlayerHead(matrices, player.getGameProfile(), x, y);
    }

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
     * Hud render event listener
     *
     * @param matrices  {@link MatrixStack}
     * @param tickDelta A rendering delay
     */
    public static void onRenderHud(MatrixStack matrices, float tickDelta) {
        Options.renderWidgets(matrices);
    }

    /**
     * Render world event listener method
     * Not expect call by you
     *
     * @param matrices  {@link net.minecraft.client.util.math.MatrixStack}
     * @param tickDelta A render delay
     */
    public static void onRenderWorld(MatrixStack matrices, float tickDelta) {
        BAR_VISUALIZER.draw(tickDelta);
        // MARKED_AREA.render(matrices, tickDelta);
    }

    /**
     * Private rendering system initialization
     */
    public static void onRenderInit() {
        BAR_VISUALIZER.init();
        BAR_VISUALIZER.setDistance(Options.getBarrierVisualizeRadius());
    }

    /**
     * Private rendering system
     *
     * @param client A minecraft client instance
     */
    public static void onClientStop(MinecraftClient client) {
        BAR_VISUALIZER.destroy();
        saveConfig();
    }

    /**
     * Click tick event listener
     */
    public static void onTick() {

        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        if (player == null) return;

        AbstractTeam team = MinecraftClient.getInstance().player.getScoreboardTeam();
        if (team != null) {
            if (team.shouldShowFriendlyInvisibles() == KZEAddon.Options.isCompletelyInvisible()) {
                ((Team) team).setShowFriendlyInvisibles(!KZEAddon.Options.isCompletelyInvisible());
                KZEAddon.addChatLog("Visibility flag changed to " + !KZEAddon.Options.isCompletelyInvisible());
            }
        }

        KZEAddon.BAR_VISUALIZER.tick();
        KZEAddon.KZE_INFO.tick();

        KZEAddon.tickKeys();
    }

    public static void tickKeys() {
        KZEAddon.ADD_GROW_TARGET.tick();
        KZEAddon.HIDE_PLAYERS.tick();
        KZEAddon.DEBUG_KEY.tick();
    }

    /**
     * @param entity      Render target entity
     * @param originAlpha Original alpha value (Changed by status effect and nbt tags)
     *                    <p>
     *                    <p>
     *                    Note:
     *                    Alpha is work on a player only
     */
    public static float[] getEntityRenderColor(LivingEntity entity, float originAlpha) {
        if (entity.equals(MinecraftClient.getInstance().targetedEntity)) {
            return new float[]{1F, 0F, 1F, 0.5F};
        }
        return new float[]{1F, 1F, 1F, 1F};
    }

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
     * システムのデフォルトのエディターを使用して
     * コンフィグファイルを開く
     *
     * @return 開くのに成功した場合 true 失敗したら false
     * <p>
     * Windowsでのみ使用可能
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
            matrices.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(-camera.getYaw()));
            matrices.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(camera.getPitch()));
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
                    HitResult result = raycastIgnoreBlock(MinecraftClient.getInstance().player, 100.0);

                    if (result.getType() == HitResult.Type.ENTITY) {
                        Entity entity = ((EntityHitResult) result).getEntity();
                        if (!KZEAddon.priorityGlowPlayers.contains(entity.getUuid())) {
                            KZEAddon.priorityGlowPlayers.add(entity.getUuid());
                        } else {
                            KZEAddon.priorityGlowPlayers.remove(entity.getUuid());
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
            if (MinecraftClient.getInstance().world != null) MinecraftClient.getInstance().openScreen(new WidgetConfigureScreen(Anchor.MIDDLE_MIDDLE, Anchor.MIDDLE_MIDDLE, 0, 0));
            KZEAddon.addChatLog("DEBUG KEY PRESSED");
        });
        GetTeamColorValueEvent.EVENT.register(KZEAddon::onGetTeamColorValue);
        Registry.register(Registry.SOUND_EVENT, CustomSounds.HONK_ID, CustomSounds.HONK_EVENT);
    }
}
