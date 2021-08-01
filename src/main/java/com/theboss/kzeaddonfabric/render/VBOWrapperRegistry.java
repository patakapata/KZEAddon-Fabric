package com.theboss.kzeaddonfabric.render;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class VBOWrapperRegistry {
    public static List<VBOWrapper> entries = new ArrayList<>();

    public static void clear() {
        entries.clear();
    }

    public static void destroyWrappers() {
        for (VBOWrapper wrapper : entries) {
            wrapper.destroy();
        }
    }

    public static void forEach(Consumer<VBOWrapper> consumer) {
        for (VBOWrapper wrapper : entries) {
            consumer.accept(wrapper);
        }
    }

    public static void initWrappers(int initialCapacity) {
        for (VBOWrapper wrapper : entries) {
            wrapper.init(initialCapacity);
        }
    }

    public static void register(VBOWrapper entry) {
        entries.add(entry);
    }

    public static void remove(VBOWrapper wrapper) {
        entries.remove(wrapper);
    }

    public static void remove(int index) {
        entries.remove(index);
    }

    private VBOWrapperRegistry() {}
}
