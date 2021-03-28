package com.theboss.kzeaddonfabric;

import com.theboss.kzeaddonfabric.render.hud.TestHUD;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.math.MatrixStack;

@Environment(EnvType.CLIENT)
public class KZEAddonFabric implements ClientModInitializer {
    private static final KZEInformation information = new KZEInformation();
    public static final TestHUD TEST_HUD = new TestHUD();

    public static int color;

    @Override
    public void onInitializeClient() {
    }

    public static void onRenderHud(MatrixStack matrices, float tickDelta) {
        TEST_HUD.render(matrices);
    }

    public static void onRenderInit() {
    }

    public static void onClientStop() {
        System.out.println("client stopping!");
    }

    public static void onTick() {
        information.tick();
        TEST_HUD.tick();
    }
}
