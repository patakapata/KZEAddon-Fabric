package com.theboss.kzeaddonfabric.utils;

import com.google.common.collect.Lists;

import java.util.List;

public class Dispatcher<T> {
    private final List<T> list;

    public Dispatcher(List<T> list) {
        this.list = list;
    }

    public void register(T t) {
        if (!this.list.contains(t)) {
            this.list.add(t);
        }
    }

    public List<T> getContents() {
        return Lists.newArrayList(this.list);
    }
}
