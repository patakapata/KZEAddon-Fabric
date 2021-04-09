package com.theboss.kzeaddonfabric.screen.button;

import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.text.LiteralText;

import java.util.function.Consumer;

public class EnumCycleButton<T extends Enum<?>> extends AbstractButtonWidget {
    private final T[] entries;
    private final Consumer<T> saver;
    private int selected;

    public EnumCycleButton(int x, int y, int width, int height, T[] entries, T selected, Consumer<T> saver) {
        super(x, y, width, height, new LiteralText(""));

        this.entries = entries;
        this.selected = this.indexOf(selected);
        this.saver = saver;

        this.onChanged();
    }

    public int indexOf(T t) {
        for (int i = 0; i < this.entries.length; i++) {
            if (this.entries[i].equals(t)) return i;
        }
        return -1;
    }

    public void next() {
        int oldSelect = this.selected;
        int length = this.entries.length;
        ++this.selected;
        this.selected %= length;
        if (this.selected != oldSelect) this.onChanged();
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        this.next();
    }


    public void setSelected(int index) {
        if (this.selected != index) {
            this.selected = index;
            this.onChanged();
        }
    }

    public T getSelected() {
        return this.entries[this.selected];
    }

    public void onChanged() {
        T selected = this.getSelected();

        this.setMessage(new LiteralText(selected.toString()));
        this.saver.accept(selected);
    }
}
