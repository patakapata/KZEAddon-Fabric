package com.theboss.kzeaddonfabric.ingame;

public class Skill {
    private String name;
    private int cost;

    public Skill(String name, int cost) {
        this.name = name;
        this.cost = cost;
    }

    public String getName() {
        return name;
    }

    public int getCost() {
        return cost;
    }
}
