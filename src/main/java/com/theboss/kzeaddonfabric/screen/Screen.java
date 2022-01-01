package com.theboss.kzeaddonfabric.screen;

import com.theboss.kzeaddonfabric.KZEAddon;
import com.theboss.kzeaddonfabric.screen.button.TextFieldWidget;
import com.theboss.kzeaddonfabric.utils.RenderingUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

import java.util.ArrayList;
import java.util.List;

public abstract class Screen extends net.minecraft.client.gui.screen.Screen {
    protected static final TranslatableText WIP_TEXT = new TranslatableText("menu.kzeaddon.work_in_progress");
    protected final List<TextFieldWidget> textFields;
    protected final ParentWrapper parent;

    protected Screen(Text title) {
        super(title);

        this.parent = new ParentWrapper(null);
        this.textFields = new ArrayList<>();
    }

    public void addTextField(TextFieldWidget textFieldWidget) {
        this.textFields.add(textFieldWidget);
        this.addButton(textFieldWidget);
    }

    @Override
    public void init(MinecraftClient client, int width, int height) {
        this.textFields.clear();
        super.init(client, width, height);
    }

    @Override
    public void onClose() {
        this.parent.open(MinecraftClient.getInstance());
    }

    public void open(MinecraftClient client) {
        if (client != null) {
            client.openScreen(this);
        } else {
            KZEAddon.LOGGER.error("MinecraftClient is null!");
        }
    }

    public void renderMatrixContents(MatrixStack matrices, int x, int y, boolean isProjectionMatrix) {
        this.renderTooltip(matrices, RenderingUtils.getMatrixContent(isProjectionMatrix), x, y);
    }

    protected void renderWIPText(MatrixStack matrices) {
        matrices.push();
        matrices.translate(this.width, this.height, 0);
        this.textRenderer.drawWithShadow(matrices, WIP_TEXT, -this.textRenderer.getWidth(WIP_TEXT), -this.textRenderer.fontHeight, 0xAAAAAA);
        matrices.pop();
    }

    public void setFocusedTFW(TextFieldWidget widget) {
        if (!this.textFields.contains(widget)) return;
        this.textFields.forEach(it -> it.setTextFieldFocused(it.equals(widget)));
    }

    public void setParent(Object screen) {
        this.parent.setParent(screen);
    }

    @Override
    public void tick() {
        this.textFields.forEach(TextFieldWidget::tick);
    }
}
