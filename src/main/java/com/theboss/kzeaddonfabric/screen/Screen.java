package com.theboss.kzeaddonfabric.screen;

import com.theboss.kzeaddonfabric.KZEAddon;
import com.theboss.kzeaddonfabric.screen.button.TextFieldWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public abstract class Screen extends net.minecraft.client.gui.screen.Screen {
    private final List<TextFieldWidget> textFields;
    private final ParentWrapper parent;

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
        this.parent.open(this.client);
    }

    public void open(MinecraftClient client) {
        if (client != null) {
            client.openScreen(this);
        } else {
            KZEAddon.LOGGER.error("MinecraftClient is null!");
        }
    }

    public void open() {
        this.open(this.client);
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
