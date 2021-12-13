package com.theboss.kzeaddonfabric.widgets;

import com.theboss.kzeaddonfabric.utils.Dispatcher;

public interface WidgetRegister {
    void registerWidget(Dispatcher<Widget> dispatcher);

    void registerWidgetType(Dispatcher<Class<?>> dispatcher);
}
