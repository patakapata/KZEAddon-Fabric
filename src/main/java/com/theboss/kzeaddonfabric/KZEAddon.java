package com.theboss.kzeaddonfabric;

import com.theboss.kzeaddonfabric.enums.Anchor;
import com.theboss.kzeaddonfabric.events.impl.ClientEvents;
import com.theboss.kzeaddonfabric.events.impl.KillLogEvents;
import com.theboss.kzeaddonfabric.events.impl.ReloadEvents;
import com.theboss.kzeaddonfabric.events.listeners.EventsListener;
import com.theboss.kzeaddonfabric.events.listeners.RenderingEventsListener;
import com.theboss.kzeaddonfabric.ingame.KZEInformation;
import com.theboss.kzeaddonfabric.ingame.Stats;
import com.theboss.kzeaddonfabric.render.BarrierVisualizer;
import com.theboss.kzeaddonfabric.render.shader.impl.OldBarrierShader;
import com.theboss.kzeaddonfabric.utils.CustomSounds;
import com.theboss.kzeaddonfabric.utils.Dispatcher;
import com.theboss.kzeaddonfabric.utils.RenderUtils;
import com.theboss.kzeaddonfabric.utils.VanillaUtils;
import com.theboss.kzeaddonfabric.widgets.AbstractTextWidget;
import com.theboss.kzeaddonfabric.widgets.Offset;
import com.theboss.kzeaddonfabric.widgets.WidgetRenderer;
import com.theboss.kzeaddonfabric.widgets.api.Widget;
import com.theboss.kzeaddonfabric.widgets.api.WidgetRegister;
import com.theboss.kzeaddonfabric.widgets.impl.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.Vec3f;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL44;

import java.io.File;
import java.util.*;

@Environment(EnvType.CLIENT)
public class KZEAddon implements ClientModInitializer, WidgetRegister {
    public static final Logger LOGGER = LogManager.getLogger("KZEAddon-Fabric");
    public static final String MOD_ID = "kzeaddon-fabric";
    public static final Map<Integer, String> GL_ERRORS;
    private static Options options;
    private static WidgetRenderer widgetRenderer;
    private static KZEInformation kzeInfo;
    private static KillLog killLog;
    private static File configFolder;
    private static Stats stats;
    private static BarrierVisualizer barrierVisualizer;
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
    }

    public static Options getOptions() {
        return options;
    }

    public static WidgetRenderer getWidgetRenderer() {
        return widgetRenderer;
    }

    public static KZEInformation getKZEInfo() {
        return kzeInfo;
    }

    public static KillLog getKillLog() {
        return killLog;
    }

    public static Stats getStats() {
        return stats;
    }

    public static BarrierVisualizer getBarrierVisualizer() {
        return barrierVisualizer;
    }

    public static void info(Text msg) {
        modLog.info(msg);
    }

    public static void info(String msg) {
        modLog.info(msg);
    }

    public static void warn(Text msg) {
        modLog.warn(msg);
    }

    public static void warn(String msg) {
        modLog.warn(msg);
    }

    public static void error(Text msg) {
        modLog.error(msg);
    }

    public static void error(String msg) {
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

    public static void postClientInitialize(MinecraftClient mc) {
        RenderUtils.registerTexture(MOD_ID, "textures/gui/missing_skin.png");
        RenderUtils.registerTexture(MOD_ID, "textures/gui/frame.png");
        RenderUtils.registerTexture(MOD_ID, "textures/default_skin.png");
        AbstractTextWidget.textRenderer = mc.textRenderer;

        handleWidgetTypeRegistration();
        options = new Options(configFolder);
        kzeInfo = new KZEInformation(mc);
        widgetRenderer = new WidgetRenderer(configFolder);

        obsessions = new ArrayList<>();
        modLog = new KZEAddonLog(mc, 1000, 0, 0, 10);
        killLog = new KillLog(mc, 3, 3, 10);
        stats = new Stats(configFolder);
        handleWidgetRegistration();

        barrierVisualizer = new BarrierVisualizer(options.barrierVisualizeRadius);
        RenderingEventsListener.onInit();
        OldBarrierShader.getInstance().setColor(options.barrierColor.get());
    }

    private static void handleWidgetRegistration() {
        List<Widget> widgets = new ArrayList<>();
        Dispatcher<Widget> dispatcher = new Dispatcher<>(widgets);
        for (EntrypointContainer<WidgetRegister> entryPoint : FabricLoader.getInstance().getEntrypointContainers("kzeaddon", WidgetRegister.class)) {
            entryPoint.getEntrypoint().registerWidget(dispatcher);
        }
        if (!widgets.isEmpty()) {
            widgets.forEach(widgetRenderer::add);
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

    /**
     * Modの初期化
     */
    @Override
    public void onInitializeClient() {
        configFolder = FabricLoader.getInstance().getConfigDir().resolve(MOD_ID).toFile();
        LOGGER.info("Configuration: " + configFolder);

        KeyBindings.registerKeybindings();
        this.registerSounds();

        // Original events
        ClientEvents.TICK.register(EventsListener::onTick);
        ClientEvents.STOP.register(EventsListener::onClientStop);
        KillLogEvents.ADD.register((log, entry) -> info(new TranslatableText("debug.kzeaddon.kill_log.add", entry)));
        ReloadEvents.START.register(info -> info(new TranslatableText("debug.kzeaddon.reload.start")));
        ReloadEvents.COMPLETE.register(info -> info(new TranslatableText("debug.kzeaddon.reload.complete")));
        ReloadEvents.REFUSE.register(info -> info(new TranslatableText("debug.kzeaddon.reload.refuse")));
    }

    @Override
    public void registerWidget(Dispatcher<Widget> dispatcher) {}

    @Override
    public void registerWidgetType(Dispatcher<Class<? extends Widget>> dispatcher) {
        dispatcher.register(TextWidget.class);
        dispatcher.register(WeaponWidget.class);
        dispatcher.register(ReloadTimeWidget.class);
        dispatcher.register(TotalAmmoWidget.class);
    }

    /**
     * 音の登録
     */
    private void registerSounds() {
        Registry.register(Registry.SOUND_EVENT, CustomSounds.HONK_ID, CustomSounds.HONK_EVENT);
        Registry.register(Registry.SOUND_EVENT, CustomSounds.VOTE_NOTIFIC_ID, CustomSounds.VOTE_NOTIFIC_EVENT);
    }
}
