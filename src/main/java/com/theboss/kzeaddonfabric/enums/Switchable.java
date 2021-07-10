package com.theboss.kzeaddonfabric.enums;

import net.minecraft.text.Text;

public enum Switchable {
    TOGGLE, HOLD, DISABLED;

    public Text text() {
        return Text.of(this.toString());
    }
}
