package com.theboss.kzeaddonfabric.screen.button;

import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class CycleableButton<E> extends ClickableWidget {
    private final List<E> entries;
    private int selected;
    private Function<E, Text> messageSupplier;
    private Consumer<CycleableButton<E>> saveConsumer;

    public static <T> int indexOf(T[] array, T target) {
        int i = 0;
        for (T tmp : array) {
            if (tmp.equals(target)) return i;
            i++;
        }
        return -1;
    }

    public CycleableButton(int x, int y, int width, int height, List<E> list, E selected) {
        this(x, y, width, height, list, list.indexOf(selected));
    }

    public CycleableButton(int x, int y, int width, int height, E[] list, E selected) {
        this(x, y, width, height, list, indexOf(list, selected));
    }

    public CycleableButton(int x, int y, int width, int height, E[] list, int selected) {
        this(x, y, width, height, new ArrayList<>(Arrays.asList(list)), selected);
    }

    public CycleableButton(int x, int y, int width, int height, List<E> entries, int selected) {
        super(x, y, width, height, Text.of(entries.get(selected).toString()));
        this.entries = entries;
        this.selected = selected;
        this.messageSupplier = e -> Text.of(e.toString());
        this.saveConsumer = btn -> {};

        this.updateMsg();
    }

    public Function<E, Text> getMessageSupplier() {
        return this.messageSupplier;
    }

    public void setMessageSupplier(Function<E, Text> messageSupplier) {
        this.messageSupplier = messageSupplier;
    }

    public Consumer<CycleableButton<E>> getSaveConsumer() {
        return this.saveConsumer;
    }

    public void setSaveConsumer(Consumer<CycleableButton<E>> saveConsumer) {
        this.saveConsumer = saveConsumer;
    }

    public int getSelected() {
        return this.selected;
    }

    public void setSelected(int selected) {
        this.selected = selected;
    }

    public int indexOf(E e) {
        return this.entries.indexOf(e);
    }

    public void next() {
        this.selected = (this.selected + 1) % this.entries.size();
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        this.next();
        this.saveConsumer.accept(this);
    }

    public void previous() {
        this.selected--;
        if (this.selected < 0) this.selected = this.entries.size() - 1;
    }

    public E selected() {
        return this.entries.get(this.selected);
    }

    public void updateMsg() {
        this.setMessage(this.messageSupplier.apply(this.entries.get(this.selected)));
    }
}
