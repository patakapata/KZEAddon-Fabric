package com.theboss.kzeaddonfabric.widgets.impl;

import com.theboss.kzeaddonfabric.enums.Anchor;
import com.theboss.kzeaddonfabric.ingame.Weapon;
import com.theboss.kzeaddonfabric.utils.Exclude;
import com.theboss.kzeaddonfabric.utils.VanillaUtils;
import com.theboss.kzeaddonfabric.widgets.AbstractTextWidget;
import com.theboss.kzeaddonfabric.widgets.Offset;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public class WeaponWidget extends AbstractTextWidget {
    @Exclude
    private final Weapon weapon;
    private String name;
    private int emptyColor;
    private int lowColor;
    private int fullColor;
    private short alpha;

    public WeaponWidget(Weapon weapon, String name, int emptyColor, int lowColor, int fullColor, int alpha, float scale, Offset offset, Anchor anchor) {
        super(scale, offset, anchor);

        this.weapon = weapon;
        this.name = name;
        this.emptyColor = emptyColor & 0xFFFFFF;
        this.lowColor = lowColor & 0xFFFFFF;
        this.fullColor = fullColor & 0xFFFFFF;
        this.alpha = (short) (alpha & 0xFF);
    }

    public WeaponWidget() {
        super(0, null, null);

        this.weapon = null;
        this.name = null;
    }

    public void copy(WeaponWidget other) {
        this.setOffset(other.getOffset());
        this.setScale(other.getScale());
        this.setAnchor(other.getAnchor());
        this.name = other.name;
        this.emptyColor = other.emptyColor;
        this.lowColor = other.lowColor;
        this.fullColor = other.fullColor;
        this.alpha = other.alpha;
    }

    @Override
    public boolean isBuiltIn() {
        return true;
    }

    @Override
    public Text getName() {
        return Text.of(this.name);
    }

    public int getEmptyColor() {
        return this.emptyColor;
    }

    public void setEmptyColor(int emptyColor) {
        this.emptyColor = emptyColor;
    }

    public int getLowColor() {
        return this.lowColor;
    }

    public void setLowColor(int lowColor) {
        this.lowColor = lowColor;
    }

    public int getFullColor() {
        return this.fullColor;
    }

    public void setFullColor(int fullColor) {
        this.fullColor = fullColor;
    }

    @Override
    public int getColor() {
        int ammo = this.weapon.getInMagazineAmmo();
        if (ammo == 0) {
            return this.emptyColor;
        } else {
            float delta = (float) this.weapon.getInMagazineAmmo() / this.weapon.getMaxMagazineAmmo();
            return VanillaUtils.lerp(this.lowColor, this.fullColor, delta);
        }
    }

    @Override
    public int getAlpha() {
        return this.alpha;
    }

    @Override
    public Text getText() {
        return Text.of(String.valueOf(this.weapon.getInMagazineAmmo()));
    }

    @Override
    public void render(MatrixStack matrices, float delta) {
        if (this.weapon == null || this.weapon.getInMagazineAmmo() == -1) return;
        super.render(matrices, delta);
    }
}
