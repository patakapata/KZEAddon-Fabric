package com.theboss.kzeaddonfabric.screen.options;

import com.theboss.kzeaddonfabric.enums.BarrierVisualizeOrigin;
import com.theboss.kzeaddonfabric.enums.CameraSwitchType;
import com.theboss.kzeaddonfabric.enums.Switchable;
import com.theboss.kzeaddonfabric.screen.Screen;
import com.theboss.kzeaddonfabric.screen.button.CycleableButton;
import net.minecraft.text.Text;

public class OtherOptionScreen extends Screen {
    private int cX;
    private int cY;

    private Switchable hideTeammateType;
    private CameraSwitchType cameraSwitchType;
    private boolean isIgnoreResourcePack;
    private boolean isChangeGunfireVolume;
    private float gunfireVolume;
    private BarrierVisualizeOrigin visualizeOrigin;
    private int visualizeRadius;
    private boolean isCompletelyInvisibleTeammate;
    private boolean shouldHighlightMyKill;

    public OtherOptionScreen(Switchable hideTeammateType, CameraSwitchType cameraSwitchType, boolean isIgnoreResourcePack, boolean isChangeGunfireVolume, float gunfireVolume, BarrierVisualizeOrigin visualizeOrigin, int visualizeRadius, boolean isCompletelyInvisibleTeammate, boolean shouldHighlightMyKill) {
        super(Text.of("Other options screen"));
        this.hideTeammateType = hideTeammateType;
        this.cameraSwitchType = cameraSwitchType;
        this.isIgnoreResourcePack = isIgnoreResourcePack;
        this.isChangeGunfireVolume = isChangeGunfireVolume;
        this.gunfireVolume = gunfireVolume;
        this.visualizeOrigin = visualizeOrigin;
        this.visualizeRadius = visualizeRadius;
        this.isCompletelyInvisibleTeammate = isCompletelyInvisibleTeammate;
        this.shouldHighlightMyKill = shouldHighlightMyKill;
    }

    public CameraSwitchType getCameraSwitchType() {
        return this.cameraSwitchType;
    }

    public void setCameraSwitchType(CameraSwitchType cameraSwitchType) {
        this.cameraSwitchType = cameraSwitchType;
    }

    public float getGunfireVolume() {
        return this.gunfireVolume;
    }

    public void setGunfireVolume(float gunfireVolume) {
        this.gunfireVolume = gunfireVolume;
    }

    public Switchable getHideTeammateType() {
        return this.hideTeammateType;
    }

    public void setHideTeammateType(Switchable hideTeammateType) {
        this.hideTeammateType = hideTeammateType;
    }

    public BarrierVisualizeOrigin getVisualizeOrigin() {
        return this.visualizeOrigin;
    }

    public void setVisualizeOrigin(BarrierVisualizeOrigin visualizeOrigin) {
        this.visualizeOrigin = visualizeOrigin;
    }

    public int getVisualizeRadius() {
        return this.visualizeRadius;
    }

    public void setVisualizeRadius(int visualizeRadius) {
        this.visualizeRadius = visualizeRadius;
    }

    @Override
    protected void init() {
        this.cX = this.width / 2;
        this.cY = this.height / 2;

        CycleableButton<Switchable> hideTeammateType = new CycleableButton<>(this.cX - 121, this.cY - 81, 74, 20, Switchable.values(), this.hideTeammateType);
        hideTeammateType.setMessageSupplier(Switchable::text);
        hideTeammateType.setSaveConsumer(btn -> this.setHideTeammateType(btn.selected()));
        //
        CycleableButton<Boolean> isCompletelyInvisibleTeammate = new CycleableButton<Boolean>(this.cX - 37, this.cY - 81, 74, 20, new Boolean[]{true, false}, this.isCompletelyInvisibleTeammate);
        isCompletelyInvisibleTeammate.setMessageSupplier(bl -> Text.of("" + bl));
        isCompletelyInvisibleTeammate.setSaveConsumer(btn -> this.setCompletelyInvisibleTeammate(btn.selected()));
        //
        CycleableButton<Boolean> shouldHighlightMyKill = new CycleableButton<>(this.cX + 47, this.cY - 81, 74, 20, new Boolean[]{true, false}, this.shouldHighlightMyKill);
        shouldHighlightMyKill.setMessageSupplier(bl -> Text.of("" + bl));
        shouldHighlightMyKill.setSaveConsumer(btn -> this.setShouldHighlightMyKill(btn.selected()));
        //
        CycleableButton<CameraSwitchType> forceSmoothCameraType = new CycleableButton<>(this.cX - 121, this.cY - 51, 74, 20, CameraSwitchType.values(), this.cameraSwitchType);
        forceSmoothCameraType.setMessageSupplier(CameraSwitchType::text);
        forceSmoothCameraType.setSaveConsumer(btn -> this.setCameraSwitchType(btn.selected()));
        //
        CycleableButton<Boolean> isChangeGunfireVolume = new CycleableButton<>(this.cX - 37, this.cY - 51, 74, 20, new Boolean[]{true, false}, this.isChangeGunfireVolume);
        isChangeGunfireVolume.setMessageSupplier(bl -> Text.of("" + bl));
        isChangeGunfireVolume.setSaveConsumer(btn -> this.setChangeGunfireVolume(btn.selected()));

        this.addButton(hideTeammateType);
        this.addButton(isCompletelyInvisibleTeammate);
        this.addButton(shouldHighlightMyKill);
        this.addButton(forceSmoothCameraType);
        this.addButton(isChangeGunfireVolume);
    }

    public boolean isChangeGunfireVolume() {
        return this.isChangeGunfireVolume;
    }

    public void setChangeGunfireVolume(boolean changeGunfireVolume) {
        this.isChangeGunfireVolume = changeGunfireVolume;
    }

    public boolean isCompletelyInvisibleTeammate() {
        return this.isCompletelyInvisibleTeammate;
    }

    public void setCompletelyInvisibleTeammate(boolean completelyInvisibleTeammate) {
        this.isCompletelyInvisibleTeammate = completelyInvisibleTeammate;
    }

    public boolean isIgnoreResourcePack() {
        return this.isIgnoreResourcePack;
    }

    public void setIgnoreResourcePack(boolean ignoreResourcePack) {
        this.isIgnoreResourcePack = ignoreResourcePack;
    }

    public boolean isShouldHighlightMyKill() {
        return this.shouldHighlightMyKill;
    }

    public void setShouldHighlightMyKill(boolean shouldHighlightMyKill) {
        this.shouldHighlightMyKill = shouldHighlightMyKill;
    }
}
