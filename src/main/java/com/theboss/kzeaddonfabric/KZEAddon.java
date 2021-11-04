package com.theboss.kzeaddonfabric;

import com.mojang.brigadier.CommandDispatcher;
import com.theboss.kzeaddonfabric.commands.KZEAddonFabricCommand;
import com.theboss.kzeaddonfabric.events.EventsListener;
import com.theboss.kzeaddonfabric.ingame.Stats;
import com.theboss.kzeaddonfabric.render.WidgetRenderer;
import com.theboss.kzeaddonfabric.render.widgets.Widget;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.ResourceTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.*;

@Environment(EnvType.CLIENT)
public class KZEAddon implements ClientModInitializer {
    private static List<UUID> priorityGlowPlayers;
    private static KZEAddonLog modLog;

    public static final Logger LOGGER = LogManager.getLogger("KZEAddon-Fabric");
    public static final String MOD_ID = "kzeaddon-fabric";
    public static final Map<Integer, String> GL_ERRORS;
    public static Options options;
    public static WidgetRenderer widgetRenderer;
    public static KZEInformation kzeInfo;
    public static Stats stats;
    public static KillLog killLog;
    public static File configFolder;
    public static DefaultedList<ItemStack> favoriteItems = DefaultedList.of();
    public static ItemGroup favoriteItemsGroup = FabricItemGroupBuilder.create(new Identifier(MOD_ID, "favorites"))
            .icon(
                    () -> {
                        switch (new Random().nextInt(3)) {
                            case 0:
                                return new ItemStack(Items.APPLE, 1);
                            case 1:
                                return new ItemStack(Items.NETHERITE_AXE, 1);
                            case 2:
                                return new ItemStack(Items.GLASS, 1);
                            default:
                                return new ItemStack(Items.BARRIER, 1);
                        }
                    })
            .appendItems(list -> favoriteItems.stream().filter(Objects::nonNull).forEach(list::add))
            .build();


    static {
        Map<Integer, String> ERROR_MAP = new HashMap<>();

        ERROR_MAP.put(0x0500, "GL_INVALID_ENUM");
        ERROR_MAP.put(0x0501, "GL_INVALID_VALUE");
        ERROR_MAP.put(0x0502, "GL_INVALID_OPERATION");
        ERROR_MAP.put(0x0503, "GL_STACK_OVERFLOW");
        ERROR_MAP.put(0x0504, "GL_STACK_UNDERFLOW");
        ERROR_MAP.put(0x0505, "GL_OUT_OF_MEMORY");
        ERROR_MAP.put(0x0506, "GL_INVALID_FRAMEBUFFER_OPERATION");
        ERROR_MAP.put(0x0507, "GL_CONTEXT_LOST");
        ERROR_MAP.put(0x0508, "GL_TABLE_TOO_LARGE");

        GL_ERRORS = Collections.unmodifiableMap(ERROR_MAP);
    }

    public static void addFavoriteItem(ItemStack item) {
        favoriteItems.add(item);
    }

    public static void removeFavoriteItem(ItemStack item) {
        Iterator<ItemStack> itr = favoriteItems.iterator();
        ItemStack var;
        int i = 0;

        while (itr.hasNext()) {
            var = itr.next();
            if (ItemStack.areEqual(var, item)) {
                favoriteItems.remove(i);
                return;
            }

            i++;
        }
    }

    public static boolean isFavoriteItem(ItemStack item) {
        Iterator<ItemStack> itr = favoriteItems.iterator();
        ItemStack var;

        while (itr.hasNext()) {
            var = itr.next();

            if (ItemStack.areEqual(var, item)) return true;
        }

        return false;
    }

    public static void info(String msg) {
        modLog.info(msg);
    }

    public static void info(Text msg) {
        modLog.info(msg);
    }

    public static void warn(String msg) {
        modLog.warn(msg);
    }

    public static void warn(Text msg) {
        modLog.warn(msg);
    }

    public static void error(String msg) {
        modLog.error(msg);
    }

    public static void error(Text msg) {
        modLog.error(msg);
    }

    /**
     * 優先発光の対象プレイヤー一覧を取得
     *
     * @return 優先発光対象プレイヤー一覧
     */
    public static List<UUID> getPriorityGlowPlayers() {
        return priorityGlowPlayers;
    }

    /**
     * KZEAddon専用のログを取得
     *
     * @return 専用ログ
     */
    public static KZEAddonLog getModLog() {
        return modLog;
    }

    /**
     * Modの初期化
     */
    @Override
    public void onInitializeClient() {
        configFolder = new File(MinecraftClient.getInstance().runDirectory, "config" + File.separatorChar + MOD_ID);

        KeyBindings.registerKeybindings();
        this.registerCommands();
        this.registerClientCommands();
        this.registerSounds();

        ClientTickEvents.START_CLIENT_TICK.register(EventsListener::onTick);
        ClientLifecycleEvents.CLIENT_STOPPING.register(EventsListener::onClientStop);
    }

    public static void postClientInitialize(MinecraftClient mc) {
        registerTexture(mc, new Identifier(MOD_ID, "textures/gui/missing_skin.png"));

        options = new Options(configFolder);
        kzeInfo = new KZEInformation(mc);
        widgetRenderer = new WidgetRenderer(configFolder);

        priorityGlowPlayers = new ArrayList<>();
        modLog = new KZEAddonLog(mc, 1000, 0, 0, 10);
        killLog = new KillLog(mc, 3, 3, 10);
        stats = new Stats(configFolder);

        List<Widget> widgets = new ArrayList<>();
        WidgetDispatcher dispatcher = new WidgetDispatcher(widgets);
        LOGGER.info("Widget Registration");
        for (EntrypointContainer<WidgetRegister> entryPoint : FabricLoader.getInstance().getEntrypointContainers("kzeaddon", WidgetRegister.class)) {
            entryPoint.getEntrypoint().register(dispatcher);

            int count = widgets.size();
            String modName = entryPoint.getProvider().getMetadata().getName();
            LOGGER.info(modName + ": " + count + " widget(s) registered");
            if (!widgets.isEmpty()) {
                widgets.forEach(it -> widgetRenderer.addCustom(it));
                widgets.clear();
            }
        }
    }

    private static void registerTexture(MinecraftClient mc, Identifier id) {
        TextureManager texManager = mc.getTextureManager();
        texManager.registerTexture(id, new ResourceTexture(id));
    }

    /**
     * 音の登録
     */
    private void registerSounds() {
        Registry.register(Registry.SOUND_EVENT, CustomSounds.HONK_ID, CustomSounds.HONK_EVENT);
        Registry.register(Registry.SOUND_EVENT, CustomSounds.VOTE_NOTIFIC_ID, CustomSounds.VOTE_NOTIFIC_EVENT);
    }

    /**
     * クライアント側のコマンドの登録
     */
    public void registerClientCommands() {
        CommandDispatcher<FabricClientCommandSource> dispatcher = ClientCommandManager.DISPATCHER;

        KZEAddonFabricCommand.registerClientCommands(dispatcher);
    }

    /**
     * 共通のコマンドの登録
     */
    public void registerCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            /*
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
            ArgumentCommandNode<ServerCommandSource, Integer> perlinCount = CommandManager.argument("count", IntegerArgumentType.integer(1)).build();
            LiteralCommandNode<ServerCommandSource> iparticle = CommandManager.literal("iparticle").build();
            ArgumentCommandNode<ServerCommandSource, Integer> divisor = CommandManager.argument("divisor", IntegerArgumentType.integer(1)).build();
            ArgumentCommandNode<ServerCommandSource, PosArgument> start = CommandManager.argument("start", Vec3ArgumentType.vec3()).build();
            ArgumentCommandNode<ServerCommandSource, PosArgument> end = CommandManager.argument("end", Vec3ArgumentType.vec3()).build();
            LiteralCommandNode<ServerCommandSource> ibv = CommandManager.literal("ibv").build();
            ArgumentCommandNode<ServerCommandSource, Integer> red = CommandManager.argument("red", IntegerArgumentType.integer(0, 255)).build();
            ArgumentCommandNode<ServerCommandSource, Integer> green = CommandManager.argument("green", IntegerArgumentType.integer(0, 255)).build();
            ArgumentCommandNode<ServerCommandSource, Integer> blue = CommandManager.argument("blue", IntegerArgumentType.integer(0, 255)).build();
            ArgumentCommandNode<ServerCommandSource, Integer> alpha = CommandManager.argument("alpha", IntegerArgumentType.integer(0, 255)).executes(KZEAddon::setBarrierColorCmd).build();
            LiteralCommandNode<ServerCommandSource> radius = CommandManager.literal("radius").build();
            ArgumentCommandNode<ServerCommandSource, Float> radiusValue = CommandManager.argument("value", FloatArgumentType.floatArg(0.0F)).build();

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
             */

            KZEAddonFabricCommand.register(dispatcher);
        });
    }
}
