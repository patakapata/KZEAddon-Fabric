package com.theboss.kzeaddonfabric.widgets;

import com.theboss.kzeaddonfabric.utils.Exclude;
import com.theboss.kzeaddonfabric.utils.VanillaUtils;
import com.theboss.kzeaddonfabric.enums.Anchor;
import com.theboss.kzeaddonfabric.ingame.Weapon;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public class WeaponWidget extends AbstractWidget {
    @Exclude
    private final Weapon weapon;
    private int emptyColor;
    private int lowColor;
    private int fullColor;
    private short alpha;

    public WeaponWidget(Weapon weapon, int emptyColor, int lowColor, int fullColor, int alpha, float x, float y, float scale, Anchor windowAnchor, Anchor elementAnchor) {
        super(x, y, scale, windowAnchor, elementAnchor);

        this.weapon = weapon;
        this.emptyColor = emptyColor;
        this.lowColor = lowColor;
        this.fullColor = fullColor;
        this.alpha = (short) (alpha & 0xFF);
    }

    public WeaponWidget() {
        super(0F, 0F, 0F, null, null);

        this.weapon = null;
    }

    public void copy(WeaponWidget other) {
        this.setX(other.getX());
        this.setY(other.getY());
        this.setScale(other.getScale());
        this.setWindowAnchor(other.getWindowAnchor());
        this.setElementAnchor(other.getElementAnchor());
        this.emptyColor = other.emptyColor;
        this.lowColor = other.lowColor;
        this.fullColor = other.fullColor;
        this.alpha = other.alpha;
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
        @SuppressWarnings("ConstantConditions") int ammo = this.weapon.getInMagazineAmmo();
        if (ammo == 0) {
            return this.emptyColor;
        } else {
            float delta = (float) this.weapon.getInMagazineAmmo() / this.weapon.getMaxMagazineAmmo();
            return VanillaUtils.lerp(this.lowColor, this.fullColor, delta);
        }
    }

    @Override
    public short getAlpha() {
        return this.alpha;
    }

    @Override
    public Text getText() {
        //noinspection ConstantConditions
        return Text.of(String.valueOf(this.weapon.getInMagazineAmmo()));
    }

    @Override
    public void render(int scaledWidth, int scaledHeight, TextRenderer textRenderer, MatrixStack matrices, float delta) {
        //noinspection ConstantConditions
        if (this.weapon.getInMagazineAmmo() == -1) return;
        super.render(scaledWidth, scaledHeight, textRenderer, matrices, delta);
    }
}
