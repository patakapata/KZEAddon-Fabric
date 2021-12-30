package com.theboss.kzeaddonfabric;

import com.google.common.collect.Lists;
import com.theboss.kzeaddonfabric.events.KeyPressingEvents;
import com.theboss.kzeaddonfabric.mixin.accessor.KeyBindingAccessor;
import net.minecraft.client.option.KeyBinding;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class KeyBindings {
    public static final List<KeyBinding> modKeys = new ArrayList<>();
    public static final List<KeyBindingWrapper> handledKeyList = new ArrayList<>();

    public static KeyBindingWrapper OPEN_LOG;
    public static KeyBindingWrapper HIDE_PLAYERS;
    public static KeyBindingWrapper ADD_GROW_TARGET;
    public static KeyBindingWrapper COPY_ITEM_TAG;
    public static KeyBindingWrapper DEBUG_KEY;
    public static KeyBindingWrapper UN_STACK;

    public static boolean COPY_FLIP_FLIP = false;


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

    public static void tickKeys() {
        handledKeyList.forEach(KeyBindingWrapper::tick);
    }

    public static void registerKeybindings() {
        ADD_GROW_TARGET = new KeyBindingWrapper("key.kzeaddon.in_game.obsession.add", GLFW.GLFW_KEY_G, "key.categories.kzeaddon.in_game", KeyPressingEvents::onPressAddObsessionTarget);
        HIDE_PLAYERS = new KeyBindingWrapper("key.kzeaddon.in_game.hide_ally", GLFW.GLFW_KEY_R, "key.categories.kzeaddon.in_game", KeyPressingEvents::onPressHideTeammates);
        UN_STACK = new KeyBindingWrapper("key.kzeaddon.in_game.un_stack", GLFW.GLFW_KEY_N, "key.categories.kzeaddon.in_game", KeyPressingEvents::onPressUnStack);
        OPEN_LOG = new KeyBindingWrapper("key.kzeaddon.wip.open_log", 0, "key.categories.kzeaddon.wip", unused -> KZEAddon.getModLog().openLogScreen());
        COPY_ITEM_TAG = new KeyBindingWrapper("key.kzeaddon.wip.copy_item_tag", GLFW.GLFW_KEY_H, "key.categories.kzeaddon.wip");
        DEBUG_KEY = new KeyBindingWrapper("key.kzeaddon.wip.debug", GLFW.GLFW_KEY_RIGHT_BRACKET, "key.categories.kzeaddon.wip", KeyPressingEvents::onPressDebug);
    }
}
