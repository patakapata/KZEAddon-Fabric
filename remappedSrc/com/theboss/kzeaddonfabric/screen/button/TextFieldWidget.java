package com.theboss.kzeaddonfabric.screen.button;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public class TextFieldWidget extends net.minecraft.client.gui.widget.TextFieldWidget {
    public TextFieldWidget(TextRenderer textRenderer, int x, int y, int width, int height, Text text) {
        super(textRenderer, x, y, width, height, text);
    }

    public TextFieldWidget(TextRenderer textRenderer, int x, int y, int width, int height, net.minecraft.client.gui.widget.@Nullable TextFieldWidget copyFrom, Text text) {
        super(textRenderer, x, y, width, height, copyFrom, text);
    }

    public void setFocus(boolean bool) {
        this.setTextFieldFocused(bool);
        this.onFocusedChanged(!bool);
    }
}
