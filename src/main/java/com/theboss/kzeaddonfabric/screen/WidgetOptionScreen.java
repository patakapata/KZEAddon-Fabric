package com.theboss.kzeaddonfabric.screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.GlAllocationUtils;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import org.lwjgl.opengl.GL11;

import java.nio.FloatBuffer;

public class WidgetOptionScreen extends Screen {
    public static final Identifier WIDGETS = new Identifier("minecraft", "textures/gui/widgets.png");
    public static final Identifier MAIN_WEAPON_TOOLTIP = new Identifier("kzeaddon-fabric", "textures/gui/option/tooltip_test.png");

    private final FloatBuffer MATRIX_BUFFER = GlAllocationUtils.allocateFloatBuffer(4 * 4 * 4);

    private int cX;
    private int cY;

    private AbstractButtonWidget mainWeapon;
    private AbstractButtonWidget subWeapon;
    private AbstractButtonWidget meleeWeapon;
    private AbstractButtonWidget totalAmmo;
    private AbstractButtonWidget reloadTime;

    private AbstractButtonWidget _PENDING1;
    private AbstractButtonWidget _PENDING2;
    private AbstractButtonWidget _PENDING3;

    public void renderMainWeaponTooltip(ButtonWidget btn, MatrixStack matrices, int mouseX, int mouseY) {
        float angle = (float) (System.currentTimeMillis() % 1000D / 1000D * 360.0);
        matrices.push();
        matrices.translate(mouseX, mouseY, 0);
        matrices.multiply(Vector3f.NEGATIVE_Z.getDegreesQuaternion(angle));
        matrices.peek().getModel().writeToBuffer(this.MATRIX_BUFFER);
        matrices.pop();
        MinecraftClient.getInstance().getTextureManager().bindTexture(MAIN_WEAPON_TOOLTIP);
        GL11.glMultMatrixf(this.MATRIX_BUFFER);
        drawTexture(matrices, -8, -8, 0, 0, 16, 16, 16, 16);
        matrices.peek().getModel().writeToBuffer(this.MATRIX_BUFFER);
        GL11.glLoadMatrixf(this.MATRIX_BUFFER);
        MinecraftClient.getInstance().getTextureManager().bindTexture(WIDGETS);
    }

    public WidgetOptionScreen(Screen parent) {
        this();
        this.setParent(parent);
    }

    public WidgetOptionScreen() {
        super(Text.of("Widget Option Screen"));
    }

    @Override
    protected void init() {
        this.cX = this.width / 2;
        this.cY = this.height / 2;

        this.mainWeapon = new ButtonWidget(this.cX - 103, this.cY - 55, 98, 20, new TranslatableText("menu.kzeaddon.option.mainWeapon"), this::onPressMainW);
        this.subWeapon = new ButtonWidget(this.cX - 103, this.cY - 25, 98, 20, new TranslatableText("menu.kzeaddon.option.subWeapon"), this::onPressSubW);
        this.meleeWeapon = new ButtonWidget(this.cX - 103, this.cY + 5, 98, 20, new TranslatableText("menu.kzeaddon.option.meleeWeapon"), this::onPressMeleeW);
        this.totalAmmo = new ButtonWidget(this.cX - 103, this.cY + 35, 98, 20, new TranslatableText("menu.kzeaddon.option.totalAmmo"), this::onPressTotalAmmo);
        this.reloadTime = new ButtonWidget(this.cX + 5, this.cY - 55, 98, 20, new TranslatableText("menu.kzeaddon.option.reloadTime"), this::onPressReloadTime);

        this._PENDING1 = new ButtonWidget(this.cX + 5, this.cY - 25, 98, 20, new TranslatableText("menu.kzeaddon.option.pending"), btn -> this.onPressPending(btn, 1));
        this._PENDING2 = new ButtonWidget(this.cX + 5, this.cY + 5, 98, 20, new TranslatableText("menu.kzeaddon.option.pending"), btn -> this.onPressPending(btn, 2));
        this._PENDING3 = new ButtonWidget(this.cX + 5, this.cY + 35, 98, 20, new TranslatableText("menu.kzeaddon.option.pending"), btn -> this.onPressPending(btn, 3));

        this.addButton(this.mainWeapon);
        this.addButton(this.subWeapon);
        this.addButton(this.meleeWeapon);
        this.addButton(this.totalAmmo);
        this.addButton(this.reloadTime);

        this.addButton(this._PENDING1);
        this.addButton(this._PENDING2);
        this.addButton(this._PENDING3);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
    }

    public void onPressMainW(AbstractButtonWidget btn) {
        System.out.println("Main Weapon");
    }

    public void onPressSubW(AbstractButtonWidget btn) {
        System.out.println("Sub Weapon");
    }

    public void onPressMeleeW(AbstractButtonWidget btn) {
        System.out.println("Melee Weapon");
    }

    public void onPressTotalAmmo(AbstractButtonWidget btn) {
        System.out.println("Total Ammo");
    }

    public void onPressReloadTime(AbstractButtonWidget btn) {
        System.out.println("Reload Time");
    }

    public void onPressPending(AbstractButtonWidget btn, int id) {
        System.out.println("Pending with " + id);
    }
}
