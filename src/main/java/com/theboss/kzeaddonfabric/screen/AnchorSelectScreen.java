package com.theboss.kzeaddonfabric.screen;

import com.theboss.kzeaddonfabric.enums.Anchor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.lwjgl.glfw.GLFW;

import java.util.function.Consumer;

public class AnchorSelectScreen extends Screen {
    private Anchor windowAnchor;
    private Anchor elementAnchor;

    private ButtonWidget windowAnchorButton;
    private ButtonWidget elementAnchorButton;

    private final Consumer<AnchorSelectScreen> saveConsumer;

    public AnchorSelectScreen(Anchor windowAnchor, Anchor elementAnchor, Consumer<AnchorSelectScreen> saveConsumer) {
        super(Text.of("Anchor select screen"));
        this.windowAnchor = windowAnchor;
        this.elementAnchor = elementAnchor;
        this.saveConsumer = saveConsumer;
    }

    @Override
    protected void init() {
        int cX = this.width / 2;
        int cY = this.height / 2;

        this.windowAnchorButton = new ButtonWidget(cX - 49, cY - 30, 98, 20, Text.of(""), this::cycleWindowAnchor);
        this.elementAnchorButton = new ButtonWidget(cX - 49, cY + 10, 98, 20, Text.of(""), this::cycleElementAnchor);

        this.updateWindowAnchorButton();
        this.updateElementAnchorButton();

        this.addButton(this.windowAnchorButton);
        this.addButton(this.elementAnchorButton);
    }

    protected void updateWindowAnchorButton() {
        this.windowAnchorButton.setMessage(Text.of(this.windowAnchor.toString()));
    }

    protected void updateElementAnchorButton() {
        this.elementAnchorButton.setMessage(Text.of(this.elementAnchor.toString()));
    }

    protected int indexOf(Anchor src) {
        Anchor[] array = Anchor.values();

        for (int i = 0; i < array.length; i++) {
            if (array[i].equals(src)) return i;
        }

        return 0;
    }

    protected Anchor next(Anchor src) {
        Anchor[] values = Anchor.values();
        int index = this.indexOf(src) + 1;
        if (index >= values.length) return values[0];
        return values[index];
    }

    protected Anchor prev(Anchor src) {
        Anchor[] values = Anchor.values();
        int index = this.indexOf(src) - 1;
        if (index < 0) return values[values.length - 1];
        return values[index];
    }

    protected void cycleWindowAnchor(ButtonWidget button) {
        boolean invert = GLFW.glfwGetKey(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS;
        this.windowAnchor = invert ? this.prev(this.windowAnchor) : this.next(this.windowAnchor);
        this.updateWindowAnchorButton();
    }

    protected void cycleElementAnchor(ButtonWidget button) {
        boolean invert = GLFW.glfwGetKey(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS;
        this.elementAnchor = invert ? this.prev(this.elementAnchor) : this.next(this.elementAnchor);
        this.updateElementAnchorButton();
    }

    public Anchor getWindowAnchor() {
        return this.windowAnchor;
    }

    public void setWindowAnchor(Anchor windowAnchor) {
        this.windowAnchor = windowAnchor;
    }

    public Anchor getElementAnchor() {
        return this.elementAnchor;
    }

    public void setElementAnchor(Anchor elementAnchor) {
        this.elementAnchor = elementAnchor;
    }

    @Override
    public void onClose() {
        this.saveConsumer.accept(this);
        super.onClose();
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
        this.renderWIPText(matrices);

        Text tooltip;
        if (this.windowAnchorButton.isMouseOver(mouseX, mouseY)) {
            tooltip = new TranslatableText("menu.kzeaddon.option.window_anchor.tooltip");
            this.renderTooltip(matrices, tooltip, mouseX, mouseY);
        } else if (this.elementAnchorButton.isMouseOver(mouseX, mouseY)) {
            tooltip = new TranslatableText("menu.kzeaddon.option.element_anchor.tooltip");
            this.renderTooltip(matrices, tooltip, mouseX, mouseY);
        }
    }
}
