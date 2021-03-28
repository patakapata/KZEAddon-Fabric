package com.theboss.kzeaddonfabric;

import com.theboss.kzeaddonfabric.ingame.Skill;
import com.theboss.kzeaddonfabric.ingame.Weapon;
import net.minecraft.client.MinecraftClient;

public class KZEInformation {
    private Weapon primary;
    private Weapon secondary;
    private Weapon melee;

    private Skill first;
    private Skill second;
    private Skill third;

    private boolean isHuman;

    public void tick() {
        MinecraftClient client = MinecraftClient.getInstance();

        this.isHuman = (client.player != null && client.player.getScoreboardTeam() != null && client.player.getScoreboardTeam().getName().equals("e"));
    }

    public Weapon getPrimaryWeapon() {
        return this.primary;
    }

    public Weapon getSecondaryWeapon() {
        return this.secondary;
    }

    public Weapon getMeleeWeapon() {
        return this.melee;
    }

    public Skill getFirstSkill() {
        return this.first;
    }

    public Skill getSecondSkill() {
        return this.second;
    }

    public Skill getThirdSkill() {
        return this.third;
    }

    public boolean isHuman() {
        return this.isHuman;
    }
}
