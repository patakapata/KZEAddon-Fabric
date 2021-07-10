package com.theboss.kzeaddonfabric.screen.options;

import com.theboss.kzeaddonfabric.KZEAddon;
import com.theboss.kzeaddonfabric.screen.configure.GunAmmoWidgetConfigureScreen;
import com.theboss.kzeaddonfabric.screen.Screen;
import com.theboss.kzeaddonfabric.screen.configure.SimpleWidgetConfigureScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.util.GlAllocationUtils;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3f;
import org.lwjgl.opengl.GL11;

import java.nio.FloatBuffer;

public class WidgetSelectScreen extends Screen {
    public static final Identifier WIDGETS = new Identifier("minecraft", "textures/gui/widgets.png");
    public static final Identifier MAIN_WEAPON_TOOLTIP = new Identifier("kzeaddon-fabric", "textures/gui/option/tooltip_test.png");
    private static final Identifier TEXTURE = new Identifier("kzeaddon-fabric", "textures/gui/option/background/widget_select.png");
    private final FloatBuffer MATRIX_BUFFER = GlAllocationUtils.allocateFloatBuffer(4 * 4 * 4);

    private int cX;
    private int cY;

    private ClickableWidget mainWeapon;
    private ClickableWidget subWeapon;
    private ClickableWidget meleeWeapon;
    private ClickableWidget totalAmmo;
    private ClickableWidget reloadTime;

    private ClickableWidget _PENDING1;
    private ClickableWidget _PENDING2;
    private ClickableWidget _PENDING3;

    public WidgetSelectScreen(Screen parent) {
        this();
        this.setParent(parent);
    }

    public WidgetSelectScreen() {
        super(Text.of("Widget Option Screen"));
    }

    @Override
    protected void init() {
        this.cX = this.width / 2;
        this.cY = this.height / 2;

        this.mainWeapon = new ButtonWidget(this.cX - 89, this.cY - 80, 82, 20, new TranslatableText("menu.kzeaddon.option.mainWeapon"), this::onPressMainW);
        this.subWeapon = new ButtonWidget(this.cX - 89, this.cY - 50, 82, 20, new TranslatableText("menu.kzeaddon.option.subWeapon"), this::onPressSubW);
        this.meleeWeapon = new ButtonWidget(this.cX - 89, this.cY - 20, 82, 20, new TranslatableText("menu.kzeaddon.option.meleeWeapon"), this::onPressMeleeW);
        this.totalAmmo = new ButtonWidget(this.cX - 89, this.cY + 10, 82, 20, new TranslatableText("menu.kzeaddon.option.totalAmmo"), this::onPressTotalAmmo);
        this.reloadTime = new ButtonWidget(this.cX + 5, this.cY - 80, 82, 20, new TranslatableText("menu.kzeaddon.option.reloadTime"), this::onPressReloadTime);

        this._PENDING1 = new ButtonWidget(this.cX + 5, this.cY - 50, 82, 20, new TranslatableText("menu.kzeaddon.option.pending"), btn -> this.onPressPending(btn, 1));
        this._PENDING2 = new ButtonWidget(this.cX + 5, this.cY - 20, 82, 20, new TranslatableText("menu.kzeaddon.option.pending"), btn -> this.onPressPending(btn, 2));
        this._PENDING3 = new ButtonWidget(this.cX + 5, this.cY + 10, 82, 20, new TranslatableText("menu.kzeaddon.option.pending"), btn -> this.onPressPending(btn, 3));

        this.addButton(this.mainWeapon);
        this.addButton(this.subWeapon);
        this.addButton(this.meleeWeapon);
        this.addButton(this.totalAmmo);
        this.addButton(this.reloadTime);

        this.addButton(this._PENDING1);
        this.addButton(this._PENDING2);
        this.addButton(this._PENDING3);

        this.addButton(new ButtonWidget(this.cX - 41, this.cY + 61, 82, 20, new TranslatableText("menu.kzeaddon.option.back"), btn -> this.onClose()));
    }

    public void onPressMainW(ClickableWidget btn) {
        this.openScreen(new GunAmmoWidgetConfigureScreen(KZEAddon.Options.getPrimaryAmmo()));
    }

    public void onPressMeleeW(ClickableWidget btn) {
        this.openScreen(new GunAmmoWidgetConfigureScreen(KZEAddon.Options.getMeleeAmmo()));
    }

    public void onPressPending(ClickableWidget btn, int id) {
        System.out.println("Pending with " + id);
    }

    public void onPressReloadTime(ClickableWidget btn) {
        this.openScreen(new SimpleWidgetConfigureScreen(KZEAddon.Options.getReloadIndicator()));
    }

    public void onPressSubW(ClickableWidget btn) {
        this.openScreen(new GunAmmoWidgetConfigureScreen(KZEAddon.Options.getSecondaryAmmo()));
    }

    public void onPressTotalAmmo(ClickableWidget btn) {
        this.openScreen(new SimpleWidgetConfigureScreen(KZEAddon.Options.getTotalAmmo()));
    }

    public void openScreen(Screen screen) {
        screen.setParent(this);
        MinecraftClient.getInstance().openScreen(screen);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        if (this.client.world == null) this.renderBackground(matrices);
        // Rendering the background
        int width = 256;
        int height = 177;
        matrices.push();
        matrices.translate(this.cX, this.cY, 0.0);
        this.client.getTextureManager().bindTexture(TEXTURE);
        this.drawTexture(matrices, -width / 2, -height / 2, 0, 0, width, height);
        matrices.pop();

        super.render(matrices, mouseX, mouseY, delta);
    }

    public void renderMainWeaponTooltip(ButtonWidget btn, MatrixStack matrices, int mouseX, int mouseY) {
        float angle = (float) (System.currentTimeMillis() % 1000D / 1000D * 360.0);
        matrices.push();
        matrices.translate(mouseX, mouseY, 0);
        matrices.multiply(Vec3f.NEGATIVE_Z.getDegreesQuaternion(angle));
        matrices.peek().getModel().writeRowFirst(this.MATRIX_BUFFER);
        matrices.pop();
        MinecraftClient.getInstance().getTextureManager().bindTexture(MAIN_WEAPON_TOOLTIP);
        GL11.glMultMatrixf(this.MATRIX_BUFFER);
        drawTexture(matrices, -8, -8, 0, 0, 16, 16, 16, 16);
        matrices.peek().getModel().writeRowFirst(this.MATRIX_BUFFER);
        GL11.glLoadMatrixf(this.MATRIX_BUFFER);
        MinecraftClient.getInstance().getTextureManager().bindTexture(WIDGETS);
    }
}
