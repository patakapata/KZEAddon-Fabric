package com.theboss.kzeaddonfabric.wip.options;

import com.theboss.kzeaddonfabric.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public class ConstantWidgetsScreen extends Screen {
    private int cX;
    private int cY;

    public ConstantWidgetsScreen(@Nullable Object parent) {
        this();
        this.setParent(parent);
    }

    public ConstantWidgetsScreen() {
        super(Text.of("Constant widgets"));
    }

    @Override
    protected void init() {
        this.cX = this.width / 2;
        this.cY = this.height / 2;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);

        String[] str = "Constant Widgets\nScreen".split("\n");
        int i = 0;
        for (String tmp : str) {
            int width = this.textRenderer.getWidth(tmp);
            this.textRenderer.drawWithShadow(matrices, tmp, this.cX - width / 2F, (this.cY - this.textRenderer.fontHeight / 2F) + (i * this.textRenderer.fontHeight), 0xFFFFFF);
            i++;
        }
    }
}
