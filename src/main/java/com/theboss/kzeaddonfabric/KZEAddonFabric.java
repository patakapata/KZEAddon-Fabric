package com.theboss.kzeaddonfabric;

import com.theboss.kzeaddonfabric.render.BarrierVisualizer;
import com.theboss.kzeaddonfabric.render.hud.TestHUD;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;

@Environment(EnvType.CLIENT)
public class KZEAddonFabric implements ClientModInitializer {
    public static final String MOD_ID = "kzeaddon-fabric";

    private static final KZEInformation information = new KZEInformation();
    private static final ConfigWrapper CONFIG_WRAPPER = new ConfigWrapper(MinecraftClient.getInstance().runDirectory.getAbsolutePath() + "\\config\\" + MOD_ID + ".json", new Config());

    public static final TestHUD TEST_HUD = new TestHUD();
    public static final BarrierVisualizer BAR_VISUALIZER = new BarrierVisualizer();

    public static ConfigWrapper getConfigWrapper() {
        return CONFIG_WRAPPER;
    }

    @Override
    public void onInitializeClient() {
        CONFIG_WRAPPER.loadConfig();
    }

    public static void onRenderHud(MatrixStack matrices, float tickDelta) {
        // TEST_HUD.render(matrices);
    }

    public static void onRenderWorld(MatrixStack matrices, float tickDelta) {
        BAR_VISUALIZER.draw(matrices, tickDelta);
    }

    public static void onRenderInit() {
        BAR_VISUALIZER.init();
        BAR_VISUALIZER.setDistance(3);
    }

    public static void onClientStop() {
        CONFIG_WRAPPER.saveConfig();
        BAR_VISUALIZER.destroy();
    }

    public static void onTick() {
        information.tick();
        // TEST_HUD.tick();
        BAR_VISUALIZER.tick();
    }
}
