package com.theboss.kzeaddonfabric.widgets.api;

import com.theboss.kzeaddonfabric.utils.Dispatcher;

public interface WidgetRegister {
    void registerWidget(Dispatcher<Widget> dispatcher);

    void registerWidgetType(Dispatcher<Class<? extends Widget>> dispatcher);
}
