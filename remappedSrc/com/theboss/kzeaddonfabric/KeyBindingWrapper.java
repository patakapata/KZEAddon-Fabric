package com.theboss.kzeaddonfabric;

import com.theboss.kzeaddonfabric.mixin.client.KeyBindingAccessor;
import java.util.function.Consumer;
import net.minecraft.client.option.KeyBinding;

public class KeyBindingWrapper {
    private final KeyBinding keybinding;
    private final Consumer<KeyBinding> onPress;
    private final Consumer<KeyBinding> onRelease;

    private boolean lastIsPressed;

    public KeyBindingWrapper(String translationKey, int code, String category, Consumer<KeyBinding> onPress, Consumer<KeyBinding> onRelease) {
        this.keybinding = new KeyBinding(translationKey, code, category);
        this.onPress = onPress;
        this.onRelease = onRelease;
        KZEAddon.modKeys.add(this.keybinding);
    }

    public KeyBindingWrapper(String translationKey, int code, String category, Consumer<KeyBinding> onPress) {
        this(translationKey, code, category, onPress, unused -> {});
    }

    @SuppressWarnings("unused")
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

    public int getCode() {
        return ((KeyBindingAccessor) this.keybinding).getBoundKey().getCode();
    }

    public boolean isPressed() {
        return this.keybinding.isPressed();
    }

    @SuppressWarnings("unused")
    public String getTranslationKey() {
        return this.keybinding.getTranslationKey();
    }
}
