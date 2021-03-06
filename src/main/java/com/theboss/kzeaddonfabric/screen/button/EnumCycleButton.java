package com.theboss.kzeaddonfabric.screen.button;

import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

import java.util.function.Consumer;
import java.util.function.Function;

public class EnumCycleButton<T extends Enum<?>> extends ClickableWidget {
    private final T[] entries;
    private final Consumer<T> saver;
    private int selected;
    private Function<T, Text> messageFunc;

    public EnumCycleButton(int x, int y, int width, int height, T[] entries, T selected, Consumer<T> saver) {
        this(x, y, width, height, entries, selected, saver, value -> Text.of(value.toString()));
    }

    public EnumCycleButton(int x, int y, int width, int height, T[] entries, T selected, Consumer<T> saver, Function<T, Text> messageFunc) {
        super(x, y, width, height, new LiteralText(""));

        this.entries = entries;
        this.selected = this.indexOf(selected);
        this.saver = saver;
        this.messageFunc = messageFunc;

        this.onChanged();
    }

    public void setMessageFunc(Function<T, Text> func) {
        this.messageFunc = func;
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

        this.setMessage(this.messageFunc.apply(selected));
        this.saver.accept(selected);
    }
}
