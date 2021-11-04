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
    public static KeyBindingWrapper CAM_X;
    public static KeyBindingWrapper CAM_Y;
    public static KeyBindingWrapper CAM_Z;

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
        OPEN_LOG = new KeyBindingWrapper("key.kzeaddon.open_log", 0, "key.categories.kzeaddon.wip", unused -> KZEAddon.getModLog().openLogScreen());
        ADD_GROW_TARGET = new KeyBindingWrapper("key.kzeaddon.glow.priority.add", GLFW.GLFW_KEY_G, "key.categories.kzeaddon.in_game", KeyPressingEvents::onPressAddGlowTarget);
        COPY_ITEM_TAG = new KeyBindingWrapper("key.kzeaddon.wip.copy_item_tag", GLFW.GLFW_KEY_H, "key.categories.kzeaddon.wip");
        HIDE_PLAYERS = new KeyBindingWrapper("key.kzeaddon.hide_teammates", GLFW.GLFW_KEY_R, "key.categories.kzeaddon.in_game", KeyPressingEvents::onPressHideTeammates);
        DEBUG_KEY = new KeyBindingWrapper("key.kzeaddon.debug", GLFW.GLFW_KEY_RIGHT_BRACKET, "key.categories.kzeaddon.wip", KeyPressingEvents::onPressDebug);
        UN_STACK = new KeyBindingWrapper("key.kzeaddon.un_stack", GLFW.GLFW_KEY_N, "key.categories.kzeaddon.in_game", KeyPressingEvents::onPressUnStack);
        CAM_X = new KeyBindingWrapper("x", GLFW.GLFW_KEY_KP_4, "key.categories.kzeaddon.wip", KeyPressingEvents::onCamX);
        CAM_Y = new KeyBindingWrapper("y", GLFW.GLFW_KEY_KP_8, "key.categories.kzeaddon.wip", KeyPressingEvents::onCamY);
        CAM_Z = new KeyBindingWrapper("z", GLFW.GLFW_KEY_KP_5, "key.categories.kzeaddon.wip", KeyPressingEvents::onCamZ);
    }
}
