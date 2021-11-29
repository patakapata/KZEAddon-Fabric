package com.theboss.kzeaddonfabric.widgets;

import com.google.common.collect.Lists;
import com.theboss.kzeaddonfabric.widgets.Widget;

import java.util.List;

public class WidgetDispatcher {
    private final List<Widget> list;

    public WidgetDispatcher(List<Widget> list) {
        this.list = list;
    }

    public void register(Widget widget) {
        if (!this.list.contains(widget)) {
            this.list.add(widget);
        }
    }

    public List<Widget> getWidgets() {
        return Lists.newArrayList(this.list);
    }
}
