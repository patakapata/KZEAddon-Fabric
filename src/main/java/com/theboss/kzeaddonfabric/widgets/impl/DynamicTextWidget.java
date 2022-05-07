package com.theboss.kzeaddonfabric.widgets.impl;

import com.theboss.kzeaddonfabric.enums.Anchor;
import com.theboss.kzeaddonfabric.widgets.AbstractTextWidget;
import com.theboss.kzeaddonfabric.widgets.Offset;
import net.minecraft.text.Text;

import java.util.function.Supplier;

public class DynamicTextWidget extends AbstractTextWidget {
    private Supplier<Text> supplier;

    public DynamicTextWidget(Supplier<Text> supplier) {
        super();

        this.supplier = supplier;
    }

    public DynamicTextWidget(Supplier<Text> supplier, float scale, Offset offset, Anchor anchor) {
        super(scale, offset, anchor);

        this.supplier = supplier;
    }

    public Supplier<Text> getSupplier() {
        return this.supplier;
    }

    public void setSupplier(Supplier<Text> supplier) {
        this.supplier = supplier;
    }

    @Override
    public Text getText() {
        return this.supplier.get();
    }

    @Override
    public boolean isBuiltIn() {
        return false;
    }
}
