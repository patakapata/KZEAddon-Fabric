package com.theboss.kzeaddonfabric.wip;

import com.mojang.util.UUIDTypeAdapter;

import java.util.UUID;

public class Test {
    public static void main(String[] args) {
        UUID uuid = UUIDTypeAdapter.fromString("ca1fd023decb44b1a943f5f5c5a0a7ab");
        System.out.println("UUID: " + uuid.toString());
    }
}
