package com.theboss.kzeaddonfabric;

import com.theboss.kzeaddonfabric.render.Widget;

public class Config {
    private Widget primaryWeapon;
    private Widget secondaryWeapon;
    private Widget meleeWeapon;

    private Widget firstSkill;
    private Widget secondSkill;
    private Widget thirdSkill;

    public Config() {
    }

    public Widget getPrimaryWeapon() {
        return primaryWeapon;
    }

    public Widget getSecondaryWeapon() {
        return secondaryWeapon;
    }

    public Widget getMeleeWeapon() {
        return meleeWeapon;
    }

    public Widget getFirstSkill() {
        return firstSkill;
    }

    public Widget getSecondSkill() {
        return secondSkill;
    }

    public Widget getThirdSkill() {
        return thirdSkill;
    }
}
