package me.patakapata.kzeaddon;

import me.patakapata.kzeaddon.overlay.WeaponOverlay;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.AbstractTeam;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

import java.util.Optional;

public class KzeAddonClientMod implements ClientModInitializer {
    public static String MOD_ID = "kzeaddon-fabric";
    public static Options options = new Options();
    public static Logger LOGGER = LogManager.getLogger("KzeAddon");

    public static final String ZOMBIE_TEAM = "z";
    public static final String HUMAN_TEAM = "e";
    public static final KeyBinding TOGGLE_HIDE_ALLY = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.kzeaddon.toggle_hide_ally", GLFW.GLFW_KEY_R, "category.kzeaddon.key"));

    public static WeaponOverlay weaponOverlay;

    @Override
    public void onInitializeClient() {
        options.loadData();
        Runtime.getRuntime().addShutdownHook(new Thread(options::writeData));

        weaponOverlay = new WeaponOverlay();
        weaponOverlay.registerOverlay();

        ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
        ClientTickEvents.END_CLIENT_TICK.register(KzeData.getInstance()::onClientTick);
        ClientTickEvents.END_CLIENT_TICK.register(weaponOverlay::onClientTick);
        HudRenderCallback.EVENT.register(weaponOverlay::onRenderHud);
    }

    public void onClientTick(MinecraftClient ignored) {
        if (TOGGLE_HIDE_ALLY.wasPressed()) {
            options.hideAlly = !options.hideAlly;
            LOGGER.info("Toggle hide ally to " + (options.hideAlly ? "enable" : "disable"));
        }
    }

    public static Optional<ClientPlayerEntity> player() {
        return Optional.ofNullable(MinecraftClient.getInstance().player);
    }

    public static boolean isAlly(Entity entity, Entity other) {
        AbstractTeam team = other.getScoreboardTeam();
        return entity == other || (team != null && entity.getScoreboardTeam() == team);
    }

    public static boolean isAlly(Entity entity) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        return entity != null && player != null && isAlly(entity, player);
    }

    public static boolean isInvisible(Entity entity) {
        return entity.isInvisible() || (options.hideAlly && isAlly(entity));
    }

    public static boolean isInvisibleTo(Entity entity, PlayerEntity player) {
        if (player.isSpectator()) {
            return false;
        } else {
            AbstractTeam abstractTeam = entity.getScoreboardTeam();
            return (abstractTeam == null || player.getScoreboardTeam() != abstractTeam || !abstractTeam.shouldShowFriendlyInvisibles()) && isInvisible(entity);
        }
    }
}
