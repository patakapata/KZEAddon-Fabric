package com.theboss.kzeaddonfabric;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.theboss.kzeaddonfabric.render.BarrierVisualizer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;

@Environment(EnvType.CLIENT)
public class KZEAddonFabric implements ClientModInitializer {
    public static final Logger LOGGER = LogManager.getLogger("KZEAddon-Fabric");
    public static final String MOD_ID = "kzeaddon-fabric";

    private static File optionsFile;
    public static Options OPTIONS;

    public static final BarrierVisualizer BAR_VISUALIZER = new BarrierVisualizer();
    public static final KZEInformation KZE_INFO = new KZEInformation();

    @Override
    public void onInitializeClient() {
        MinecraftClient client = MinecraftClient.getInstance();

        optionsFile = new File(client.runDirectory.getAbsolutePath() + "\\config\\" + MOD_ID + ".json");
        loadConfig();
        OPTIONS.initWidgets();
    }

    public static void onRenderHud(MatrixStack matrices, float tickDelta) {
        OPTIONS.renderWidgets(matrices);
    }

    public static void onRenderWorld(MatrixStack matrices, float tickDelta) {
        BAR_VISUALIZER.draw(tickDelta);
    }

    public static void onRenderInit() {
        BAR_VISUALIZER.init();
        BAR_VISUALIZER.setDistance(OPTIONS.getBarrierVisualizeRadius());
    }

    public static void onClientStop() {
        BAR_VISUALIZER.destroy();
        saveConfig();
    }

    public static void onTick() {
        BAR_VISUALIZER.tick();
        KZE_INFO.tick();

        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        if (player == null) return;
        if (player.isSneaking()) {
            if (!KZE_INFO.isReloading())
                KZE_INFO.setReloadFromMainhandWeapon();
        } else {
            if (KZE_INFO.isReloading()) {
                KZE_INFO.cancelReload();
            }
        }
    }

    public static void loadConfig() {
        try {
            Gson gson = new GsonBuilder().create();
            JsonReader reader = new JsonReader(new FileReader(optionsFile));
            OPTIONS = gson.fromJson(reader, Options.class);
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
        saveConfig();
    }

    @Deprecated
    public static void addChatLog(Text text) {
        MinecraftClient client = MinecraftClient.getInstance();
        client.inGameHud.getChatHud().addMessage(text);
    }
}
