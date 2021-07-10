package com.theboss.kzeaddonfabric.enums;

import net.minecraft.text.Text;

public enum CameraSwitchType {
    FORCE_ON, FORCE_OFF, DISABLED;

    public Text text() {
        return Text.of(this.toString());
    }
}
