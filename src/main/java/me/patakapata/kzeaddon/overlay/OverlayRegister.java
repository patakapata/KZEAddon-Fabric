package me.patakapata.kzeaddon.overlay;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;

public class OverlayRegister {
    private final List<OverlayEntry> list = new ArrayList<>();

    public OverlayRegister() {}

    public void register(OverlayEntry overlay) {
        this.list.add(overlay);
    }

    public List<OverlayEntry> pop() {
        return ImmutableList.copyOf(this.list);
    }
}
