package com.theboss.kzeaddonfabric;

import com.google.gson.annotations.Expose;
import com.theboss.kzeaddonfabric.enums.*;
import com.theboss.kzeaddonfabric.render.widgets.GunAmmoWidget;
import com.theboss.kzeaddonfabric.render.widgets.ReloadIndicatorWidget;
import com.theboss.kzeaddonfabric.render.widgets.TotalAmmoWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;

@SuppressWarnings("unused")
public class Options {
    @Expose
    private Color priorityGlowColor;
    @Expose
    private Color humanGlowColor;
    @Expose
    private Color zombieGlowColor;

    @Expose
    private Switchable hideTeammates;
    @Expose
    private CameraSwitchType forceSmoothCamera;
    @Expose
    private boolean ignoreResourcePack;
    @Expose
    private boolean setGunfireSoundVolume;
    @Expose
    private float gunfireVolume;
    @Expose
    private BarrierVisualizeOrigin barrierVisualizeOrigin;
    @Expose
    private int barrierVisualizeRadius;
    @Expose
    private boolean isCompletelyInvisible;
    @Expose
    private boolean shouldHighlightMyKill;

    @Expose
    private GunAmmoWidget primaryAmmo;
    @Expose
    private GunAmmoWidget secondaryAmmo;
    @Expose
    private GunAmmoWidget meleeAmmo;
    @Expose
    private TotalAmmoWidget totalAmmo;
    @Expose
    private ReloadIndicatorWidget reloadIndicator;

    public Options() {
        this.priorityGlowColor = new Color(255, 0, 255);
        this.humanGlowColor = new Color(0, 0, 255);
        this.zombieGlowColor = new Color(0, 255, 0);

        this.hideTeammates = Switchable.TOGGLE;
        this.forceSmoothCamera = CameraSwitchType.DISABLED;
        this.ignoreResourcePack = false;
        this.setGunfireSoundVolume = true;
        this.gunfireVolume = 1.0F;
        this.barrierVisualizeOrigin = BarrierVisualizeOrigin.MYSELF;
        this.barrierVisualizeRadius = 1;
        this.isCompletelyInvisible = false;
        this.shouldHighlightMyKill = true;

        this.primaryAmmo = new GunAmmoWidget(WeaponSlot.PRIMARY, Anchor.MIDDLE_MIDDLE, Anchor.MIDDLE_DOWN, 1.0F, -80, -35, 255, new Color(0x00FF00), new Color(0x990000), new Color(0xFF0000));
        this.secondaryAmmo = new GunAmmoWidget(WeaponSlot.SECONDARY, Anchor.MIDDLE_MIDDLE, Anchor.MIDDLE_DOWN, 1.0F, -60, -35, 255, new Color(0x00FF00), new Color(0x990000), new Color(0xFF0000));
        this.meleeAmmo = new GunAmmoWidget(WeaponSlot.MELEE, Anchor.MIDDLE_MIDDLE, Anchor.MIDDLE_DOWN, 1.0F, -40, -35, 255, new Color(0x00FF00), new Color(0x990000), new Color(0xFF0000));
        this.totalAmmo = new TotalAmmoWidget(Anchor.RIGHT_MIDDLE, Anchor.MIDDLE_DOWN, 1.0F, -95, -10, 255, 0xFFFFFF);
        this.reloadIndicator = new ReloadIndicatorWidget(Anchor.MIDDLE_MIDDLE, Anchor.MIDDLE_MIDDLE, 1.0F, 0, 10, 255, 0xFFFFFF);
    }

    public BarrierVisualizeOrigin getBarrierVisualizeOrigin() {
        return this.barrierVisualizeOrigin;
    }

    public void setBarrierVisualizeOrigin(BarrierVisualizeOrigin barrierVisualizeOrigin) {
        this.barrierVisualizeOrigin = barrierVisualizeOrigin;
    }

    public int getBarrierVisualizeRadius() {
        return this.barrierVisualizeRadius;
    }

    public void setBarrierVisualizeRadius(int barrierVisualizeRadius) {
        this.barrierVisualizeRadius = barrierVisualizeRadius;
    }

    public CameraSwitchType getForceSmoothCamera() {
        return this.forceSmoothCamera;
    }

    public void setForceSmoothCamera(CameraSwitchType forceSmoothCamera) {
        this.forceSmoothCamera = forceSmoothCamera;
    }

    public float getGunfireVolume() {
        return this.gunfireVolume;
    }

    public void setGunfireVolume(float gunfireVolume) {
        this.gunfireVolume = gunfireVolume;
    }

    public Switchable getHideTeammates() {
        return this.hideTeammates;
    }

    public void setHideTeammates(Switchable hideTeammates) {
        this.hideTeammates = hideTeammates;
    }

    public Color getHumanGlowColor() {
        return this.humanGlowColor;
    }

    public void setHumanGlowColor(Color humanGlowColor) {
        this.humanGlowColor = humanGlowColor;
    }

    public GunAmmoWidget getMeleeAmmo() {
        return this.meleeAmmo;
    }

    public void setMeleeAmmo(GunAmmoWidget meleeAmmo) {
        this.meleeAmmo = meleeAmmo;
    }

    public GunAmmoWidget getPrimaryAmmo() {
        return this.primaryAmmo;
    }

    public void setPrimaryAmmo(GunAmmoWidget primaryAmmo) {
        this.primaryAmmo = primaryAmmo;
    }

    public Color getPriorityGlowColor() {
        return this.priorityGlowColor;
    }

    public void setPriorityGlowColor(Color priorityGlowColor) {
        this.priorityGlowColor = priorityGlowColor;
    }

    public ReloadIndicatorWidget getReloadIndicator() {
        return this.reloadIndicator;
    }

    public void setReloadIndicator(ReloadIndicatorWidget reloadIndicator) {
        this.reloadIndicator = reloadIndicator;
    }

    public GunAmmoWidget getSecondaryAmmo() {
        return this.secondaryAmmo;
    }

    public void setSecondaryAmmo(GunAmmoWidget secondaryAmmo) {
        this.secondaryAmmo = secondaryAmmo;
    }

    public TotalAmmoWidget getTotalAmmo() {
        return this.totalAmmo;
    }

    public void setTotalAmmo(TotalAmmoWidget totalAmmo) {
        this.totalAmmo = totalAmmo;
    }

    public Color getZombieGlowColor() {
        return this.zombieGlowColor;
    }

    public void setZombieGlowColor(Color zombieGlowColor) {
        this.zombieGlowColor = zombieGlowColor;
    }

    public void initWidgets() {
        this.primaryAmmo.setTargetSlot(WeaponSlot.PRIMARY);
        this.secondaryAmmo.setTargetSlot(WeaponSlot.SECONDARY);
        this.meleeAmmo.setTargetSlot(WeaponSlot.MELEE);
    }

    public boolean isCompletelyInvisible() {
        return this.isCompletelyInvisible;
    }

    public void setCompletelyInvisible(boolean completelyInvisible) {
        this.isCompletelyInvisible = completelyInvisible;
    }

    public boolean isIgnoreResourcePack() {
        return this.ignoreResourcePack;
    }

    public void setIgnoreResourcePack(boolean ignoreResourcePack) {
        this.ignoreResourcePack = ignoreResourcePack;
    }

    public boolean isSetGunfireSoundVolume() {
        return this.setGunfireSoundVolume;
    }

    public void setSetGunfireSoundVolume(boolean setGunfireSoundVolume) {
        this.setGunfireSoundVolume = setGunfireSoundVolume;
    }

    public boolean isShouldHighlightMyKill() {
        return this.shouldHighlightMyKill;
    }

    public void setShouldHighlightMyKill(boolean shouldHighlightMyKill) {
        this.shouldHighlightMyKill = shouldHighlightMyKill;
    }

    public void renderWidgets(MatrixStack matrices) {
        MinecraftClient client = MinecraftClient.getInstance();
        Window window = client.getWindow();
        TextRenderer textRenderer = client.textRenderer;

        this.primaryAmmo.render(matrices, window, textRenderer);
        this.secondaryAmmo.render(matrices, window, textRenderer);
        this.meleeAmmo.render(matrices, window, textRenderer);
        this.totalAmmo.render(matrices, window, textRenderer);
        this.reloadIndicator.render(matrices, window, textRenderer);
    }
}
