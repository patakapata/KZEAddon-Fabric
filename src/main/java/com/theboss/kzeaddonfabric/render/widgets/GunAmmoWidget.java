package com.theboss.kzeaddonfabric.render.widgets;

import com.google.gson.annotations.Expose;
import com.theboss.kzeaddonfabric.Color;
import com.theboss.kzeaddonfabric.KZEAddonFabric;
import com.theboss.kzeaddonfabric.enums.Anchor;
import com.theboss.kzeaddonfabric.enums.WeaponSlot;
import com.theboss.kzeaddonfabric.ingame.Weapon;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public class GunAmmoWidget extends Widget {
    @Expose(serialize = false, deserialize = false)
    private WeaponSlot targetSlot;
    @Expose
    private Color magazineFull;
    @Expose
    private Color magazineLow;
    @Expose
    private Color magazineEmpty;

    public GunAmmoWidget(WeaponSlot targetSlot, Anchor widgetAnchor, Anchor windowAnchor, float scaleFactor, int offsetX, int offsetY, int opacity, Color magazineFull, Color magazineLow, Color magazineEmpty) {
        super(widgetAnchor, windowAnchor, scaleFactor, offsetX, offsetY, opacity);
        this.targetSlot = targetSlot;
        this.magazineFull = magazineFull;
        this.magazineLow = magazineLow;
        this.magazineEmpty = magazineEmpty;
    }

    public GunAmmoWidget(GunAmmoWidget source) {
        this(source.targetSlot, source.getWidgetAnchor(), source.getWindowAnchor(), source.getScaleFactor(), source.getOffsetX(), source.getOffsetY(), source.getOpacity(), source.magazineFull, source.magazineLow, source.magazineEmpty);
    }

    protected Weapon getWeapon() {
        return KZEAddonFabric.KZE_INFO.getWeapon(this.targetSlot);
    }

    public WeaponSlot getTargetSlot() {
        return this.targetSlot;
    }

    public void setTargetSlot(WeaponSlot targetSlot) {
        this.targetSlot = targetSlot;
    }

    public Color getMagazineFull() {
        return this.magazineFull;
    }

    public void setMagazineFull(Color magazineFull) {
        this.magazineFull = magazineFull;
    }

    public Color getMagazineLow() {
        return this.magazineLow;
    }

    public void setMagazineLow(Color magazineLow) {
        this.magazineLow = magazineLow;
    }

    public Color getMagazineEmpty() {
        return this.magazineEmpty;
    }

    public void setMagazineEmpty(Color magazineEmpty) {
        this.magazineEmpty = magazineEmpty;
    }

    @Override
    public Text getText() {
        return Text.of(Integer.toString(this.getWeapon().getInMagazineAmmo()));
    }

    @Override
    public void render(MatrixStack matrices, Window window, TextRenderer textRenderer) {
        this.setVisibility(this.getWeapon().isValid());
        super.render(matrices, window, textRenderer);
    }

    @Override
    public int getColor() {
        double progress = this.getWeapon().inMagazineAmmoPercentage();
        if (progress == 0) {
            return this.magazineEmpty.get();
        } else {
            return Color.lerp(this.magazineFull, this.magazineLow, 1 - progress).get();
        }
    }
}
