package com.theboss.kzeaddonfabric;

import com.theboss.kzeaddonfabric.ingame.Skill;
import com.theboss.kzeaddonfabric.ingame.Weapon;

public class KZEInformation {
    private Weapon primary;
    private Weapon secondary;
    private Weapon melee;

    private Skill first;
    private Skill second;
    private Skill third;

    public void tick() {
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
}
