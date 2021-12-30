package com.theboss.kzeaddonfabric;

import com.mojang.brigadier.CommandDispatcher;
import com.theboss.kzeaddonfabric.commands.AddonCommand;
import com.theboss.kzeaddonfabric.events.EventsListener;
import com.theboss.kzeaddonfabric.events.KillLogEvents;
import com.theboss.kzeaddonfabric.events.ReloadEvents;
import com.theboss.kzeaddonfabric.events.RenderingEventsListener;
import com.theboss.kzeaddonfabric.ingame.KZEInformation;
import com.theboss.kzeaddonfabric.ingame.Stats;
import com.theboss.kzeaddonfabric.render.ChunkInstancedBarrierVisualizer;
import com.theboss.kzeaddonfabric.utils.CustomSounds;
import com.theboss.kzeaddonfabric.utils.Dispatcher;
import com.theboss.kzeaddonfabric.utils.RenderingUtils;
import com.theboss.kzeaddonfabric.widgets.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.*;

@Environment(EnvType.CLIENT)
public class KZEAddon implements ClientModInitializer, WidgetRegister {
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
    private static List<UUID> obsessions;
    private static KZEAddonLog modLog;

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

        FabricItemGroupBuilder.create(
                                      new Identifier(MOD_ID, "favorites"))
                              .icon(() -> new ItemStack(Items.BELL))
                              .appendItems(list -> favoriteItems.stream().filter(Objects::nonNull).forEach(list::add))
                              .build();
    }

    public static void addFavoriteItem(ItemStack item) {
        favoriteItems.add(item);
    }

    public static void error(String msg) {
        modLog.error(msg);
    }

    public static void error(Text msg) {
        modLog.error(msg);
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
     * 優先発光の対象プレイヤー一覧を取得
     *
     * @return 優先発光対象プレイヤー一覧
     */
    public static List<UUID> getObsessions() {
        return obsessions;
    }

    private static void handleWidgetRegistration() {
        List<Widget> widgets = new ArrayList<>();
        Dispatcher<Widget> dispatcher = new Dispatcher<>(widgets);
        for (EntrypointContainer<WidgetRegister> entryPoint : FabricLoader.getInstance().getEntrypointContainers("kzeaddon", WidgetRegister.class)) {
            entryPoint.getEntrypoint().registerWidget(dispatcher);
        }
        if (!widgets.isEmpty()) {
            widgets.forEach(widgetRenderer::add);
        } else {
            LOGGER.info("Dispatcher is empty");
        }
    }

    private static void handleWidgetTypeRegistration() {
        List<Class<? extends Widget>> types = new ArrayList<>();
        Dispatcher<Class<? extends Widget>> dispatcher = new Dispatcher<>(types);
        for (EntrypointContainer<WidgetRegister> entryPoint : FabricLoader.getInstance().getEntrypointContainers("kzeaddon", WidgetRegister.class)) {
            entryPoint.getEntrypoint().registerWidgetType(dispatcher);
        }
        if (!types.isEmpty()) {
            types.forEach(Widget.Serializer::registerType);
        }
    }

    public static void info(String msg) {
        modLog.info(msg);
    }

    public static void info(Text msg) {
        modLog.info(msg);
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

    public static void postClientInitialize(MinecraftClient mc) {
        RenderingUtils.registerTexture(MOD_ID, "textures/gui/missing_skin.png");
        RenderingUtils.registerTexture(MOD_ID, "textures/gui/frame.png");

        AbstractTextWidget.textRenderer = mc.textRenderer;

        handleWidgetTypeRegistration();
        options = new Options(configFolder);
        kzeInfo = new KZEInformation(mc);
        widgetRenderer = new WidgetRenderer(configFolder);

        ChunkInstancedBarrierVisualizer.INSTANCE.setRadius(options.barrierVisualizeRadius);
        obsessions = new ArrayList<>();
        modLog = new KZEAddonLog(mc, 1000, 0, 0, 10);
        killLog = new KillLog(mc, 3, 3, 10);
        stats = new Stats(configFolder);
        handleWidgetRegistration();

        RenderingEventsListener.onInit();
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

    public static void warn(String msg) {
        modLog.warn(msg);
    }

    public static void warn(Text msg) {
        modLog.warn(msg);
    }

    /**
     * Modの初期化
     */
    @Override
    public void onInitializeClient() {
        configFolder = new File(MinecraftClient.getInstance().runDirectory, "config" + File.separatorChar + MOD_ID);

        KeyBindings.registerKeybindings();
        this.registerClientCommands();
        this.registerSounds();

        // Fabric API Events
        ClientTickEvents.START_CLIENT_TICK.register(EventsListener::onTick);
        ClientLifecycleEvents.CLIENT_STOPPING.register(EventsListener::onClientStop);

        // Original events
        ReloadEvents.BEGIN.register(info -> info("Reload is start"));
        ReloadEvents.REFUSE.register(info -> info("Reload was refused"));
        ReloadEvents.COMPLETE.register(info -> info("Reload is complete"));
        KillLogEvents.ADD_ENTRY.register((log, entry) -> {
            LiteralText body = new LiteralText("Add entry: [");
            body.append(entry.getAttacker().getName())
                .append(" ")
                .append(entry.getMark())
                .append(" ")
                .append(entry.getVictim().getName())
                .append("]");
            info(body);
        });
    }

    /**
     * クライアント側のコマンドの登録
     */
    public void registerClientCommands() {
        CommandDispatcher<FabricClientCommandSource> dispatcher = ClientCommandManager.DISPATCHER;

        AddonCommand.register(dispatcher);
    }

    /**
     * 音の登録
     */
    private void registerSounds() {
        Registry.register(Registry.SOUND_EVENT, CustomSounds.HONK_ID, CustomSounds.HONK_EVENT);
        Registry.register(Registry.SOUND_EVENT, CustomSounds.VOTE_NOTIFIC_ID, CustomSounds.VOTE_NOTIFIC_EVENT);
    }

    @Override
    public void registerWidget(Dispatcher<Widget> dispatcher) {
    }

    @Override
    public void registerWidgetType(Dispatcher<Class<? extends Widget>> dispatcher) {
        dispatcher.register(TextWidget.class);
        dispatcher.register(WeaponWidget.class);
        dispatcher.register(ReloadTimeWidget.class);
        dispatcher.register(TotalAmmoWidget.class);
    }
}
