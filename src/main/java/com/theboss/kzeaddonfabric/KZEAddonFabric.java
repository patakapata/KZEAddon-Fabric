package com.theboss.kzeaddonfabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.math.MatrixStack;

@Environment(EnvType.CLIENT)
public class KZEAddonFabric implements ClientModInitializer {
    private static KZEInformation information = new KZEInformation();

    @Override
    public void onInitializeClient() {
    }

    public static void onRenderHud(MatrixStack matrices, float tickDelta) {}

    public static void onRenderInit() {
    }

    public static void onClientStop() {
    }

    public static void onTick() {
        information.tick();
    }
}
