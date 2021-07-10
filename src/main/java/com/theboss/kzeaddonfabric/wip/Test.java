package com.theboss.kzeaddonfabric.wip;

import com.google.common.collect.Lists;

import java.util.List;

public class Test {
    public static void main(String[] args) {
        List<String> list = Lists.newArrayList("String A", "Beta string", "EXAMPLE");

        String a = list.remove(3);

        System.out.println(a);
    }
}
