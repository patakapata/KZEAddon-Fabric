package com.theboss.kzeaddonfabric;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.theboss.kzeaddonfabric.enums.Switchable;
import com.theboss.kzeaddonfabric.events.GetTeamColorValueEvent;
import com.theboss.kzeaddonfabric.mixin.client.KeyBindingAccessor;
import com.theboss.kzeaddonfabric.render.BarrierVisualizer;
import com.theboss.kzeaddonfabric.wip.MarkedArea;
import com.theboss.kzeaddonfabric.wip.RenameItemCommand;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.impl.client.keybinding.KeyBindingRegistryImpl;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.client.util.GlAllocationUtils;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.awt.*;
import java.io.*;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Environment(EnvType.CLIENT)
public class KZEAddon implements ClientModInitializer {
    public static final Logger LOGGER = LogManager.getLogger("KZEAddon-Fabric");
    public static final String MOD_ID = "kzeaddon-fabric";
    public static final BarrierVisualizer BAR_VISUALIZER = new BarrierVisualizer();
    public static final KZEInformation KZE_INFO = new KZEInformation();
    public static final List<UUID> priorityGlowPlayers = new ArrayList<>();

    private static File optionsFile;

    public static Options OPTIONS;
    public static KeyBindingWrapper ADD_GROW_TARGET;
    public static KeyBindingWrapper HIDE_PLAYERS;
    public static KeyBinding COPY_ITEM_TAG;

    public static boolean KEY_FLIPFLOP_COPY = false;
    public static boolean isHideTeammates;

    public static final MarkedArea MARKED_AREA = new MarkedArea(new MarkedArea.Area(new BlockPos(0, 1, 0), new BlockPos(10, 11, 10)));

    @Override
    public void onInitializeClient() {
        MinecraftClient client = MinecraftClient.getInstance();

        ClientLifecycleEvents.CLIENT_STOPPING.register(KZEAddon::onClientStop);

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

        KZEAddon.COPY_ITEM_TAG = new KeyBinding("key.kzeaddon.wip.copy_item_tag", GLFW.GLFW_KEY_H, "key.categories.kzeaddon.wip");
        KZEAddon.HIDE_PLAYERS = new KeyBindingWrapper("key.kzeaddon.hide_teammates", GLFW.GLFW_KEY_R, "key.categories.kzeaddon.in_game", key -> {
            if (KZEAddon.OPTIONS.getHideTeammates() == Switchable.HOLD) KZEAddon.isHideTeammates = true;
            else if (KZEAddon.OPTIONS.getHideTeammates() == Switchable.TOGGLE) KZEAddon.isHideTeammates = !KZEAddon.isHideTeammates;
        }, key -> {
            if (KZEAddon.OPTIONS.getHideTeammates() == Switchable.HOLD) KZEAddon.isHideTeammates = false;
        });
        KeyBindingRegistryImpl.registerKeyBinding(KZEAddon.COPY_ITEM_TAG);

        ItemTooltipCallback.EVENT.register(KZEAddon::handleItemTooltip);
        GetTeamColorValueEvent.EVENT.register(KZEAddon::onGetTeamColorValue);
        CommandRegistrationCallback.EVENT.register(RenameItemCommand::register);
        Registry.register(Registry.SOUND_EVENT, CustomSounds.HONK_ID, CustomSounds.HONK_EVENT);
    }

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

    public static void onGetTeamColorValue(Entity entity, CallbackInfoReturnable<Integer> cir) {
        AbstractTeam team = entity.getScoreboardTeam();
        if (team != null) {
            String name = team.getName();
            if (KZEAddon.priorityGlowPlayers.contains(entity.getUuid())) {
                cir.setReturnValue(KZEAddon.OPTIONS.getPriorityGlowColor().get());
            } else if (name.equals("e")) {
                cir.setReturnValue(KZEAddon.OPTIONS.getHumanGlowColor().get());
            } else if (name.equals("z")) {
                cir.setReturnValue(KZEAddon.OPTIONS.getZombieGlowColor().get());
            }
        }
    }

    public static void handleItemTooltip(ItemStack stack, TooltipContext context, List<Text> list) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.currentScreen == null) return;
        boolean isPressed = InputUtil.isKeyPressed(mc.getWindow().getHandle(), ((KeyBindingAccessor) COPY_ITEM_TAG).getBoundKey().getCode());
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

    public static void onRenderHud(MatrixStack matrices, float tickDelta) {
        OPTIONS.renderWidgets(matrices);
    }

    public static void onRenderWorld(MatrixStack matrices, float tickDelta) {
        BAR_VISUALIZER.draw(tickDelta);
        // MARKED_AREA.render(matrices, tickDelta);
    }

    public static void onRenderInit() {
        BAR_VISUALIZER.init();
        BAR_VISUALIZER.setDistance(OPTIONS.getBarrierVisualizeRadius());
    }

    public static void onClientStop(MinecraftClient client) {
        BAR_VISUALIZER.destroy();
        saveConfig();
    }

    public static void onTick() {

        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        if (player == null) return;

        AbstractTeam team = MinecraftClient.getInstance().player.getScoreboardTeam();
        if (team != null) {
            if (team.shouldShowFriendlyInvisibles() == KZEAddon.OPTIONS.isCompletelyInvisible()) {
                ((Team) team).setShowFriendlyInvisibles(!KZEAddon.OPTIONS.isCompletelyInvisible());
                KZEAddon.addChatLog("Visibility flag changed to " + !KZEAddon.OPTIONS.isCompletelyInvisible());
            }
        }

        KZEAddon.BAR_VISUALIZER.tick();
        KZEAddon.KZE_INFO.tick();
        // 優先発光対象指定 試作
        KZEAddon.ADD_GROW_TARGET.tick();
        KZEAddon.HIDE_PLAYERS.tick();
    }

    public static void loadConfig() {
        try {
            Gson gson = new GsonBuilder().create();
            JsonReader reader = new JsonReader(new FileReader(optionsFile));
            OPTIONS = gson.fromJson(reader, Options.class);
            OPTIONS.initWidgets();
            KZEAddon.BAR_VISUALIZER.setDistance(OPTIONS.getBarrierVisualizeRadius());
        } catch (Exception e) {
            LOGGER.warn("Config file load failed");
            resetConfig();
        }
    }

    public static void saveConfig() {
        try {
            FileWriter fWriter = new FileWriter(optionsFile);
            PrintWriter pWriter = new PrintWriter(new BufferedWriter(fWriter));
            Gson gson = new GsonBuilder().setPrettyPrinting().excludeFieldsWithoutExposeAnnotation().create();
            String content = gson.toJson(OPTIONS);
            pWriter.print(content);
            pWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void resetConfig() {
        try {
            if (!optionsFile.exists())
                if (!optionsFile.createNewFile()) LOGGER.error("Config file create failed");
        } catch (IOException e) {
            e.printStackTrace();
        }
        OPTIONS = new Options();
        OPTIONS.initWidgets();
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

    public static void addChatLog(String format, Object... args) {
        addChatLog(String.format(format, args));
    }

    public static void addChatLog(String msg) {
        addChatLog(Text.of(msg));
    }

    public static void addChatLog(Text text) {
        MinecraftClient client = MinecraftClient.getInstance();
        client.inGameHud.getChatHud().addMessage(text);
    }

    public static boolean isTeammate(Entity entity) {
        return MinecraftClient.getInstance().player.isTeammate(entity);
    }
}
