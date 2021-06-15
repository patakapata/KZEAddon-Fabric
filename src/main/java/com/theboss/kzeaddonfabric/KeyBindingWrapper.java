package com.theboss.kzeaddonfabric;

import net.fabricmc.fabric.impl.client.keybinding.KeyBindingRegistryImpl;
import net.minecraft.client.options.KeyBinding;

import java.util.function.Consumer;

public class KeyBindingWrapper {
    private final KeyBinding keybinding;
    private final Consumer<KeyBinding> onPress;
    private final Consumer<KeyBinding> onRelease;

    private boolean lastIsPressed;

    public KeyBindingWrapper(String translationKey, int code, String category, Consumer<KeyBinding> onPress, Consumer<KeyBinding> onRelease) {
        this.keybinding = new KeyBinding(translationKey, code, category);
        KeyBindingRegistryImpl.registerKeyBinding(this.keybinding);
        this.onPress = onPress;
        this.onRelease = onRelease;
    }

    public KeyBindingWrapper(String translationKey, int code, String category, Consumer<KeyBinding> onPress) {
        this(translationKey, code, category, onPress, unused -> {});
    }

    public KeyBindingWrapper(String translationKey, int code, String category) {
        this(translationKey, code, category, unused -> {});
    }

    public void tick() {
        boolean isPressed = this.isPressed();

        if (isPressed) {
            if (!this.lastIsPressed) {
                this.onPress.accept(this.keybinding);
            }
        } else {
            if (this.lastIsPressed) {
                this.onRelease.accept(this.keybinding);
            }
        }

        this.lastIsPressed = isPressed;
    }

    public boolean isPressed() {
        return this.keybinding.isPressed();
    }

    public String getTranslationKey() {
        return this.keybinding.getTranslationKey();
    }
}
