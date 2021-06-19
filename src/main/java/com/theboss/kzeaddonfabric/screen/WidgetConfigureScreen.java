package com.theboss.kzeaddonfabric.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.theboss.kzeaddonfabric.enums.Anchor;
import com.theboss.kzeaddonfabric.render.widgets.Widget;
import com.theboss.kzeaddonfabric.screen.button.AnchorSelectButton;
import com.theboss.kzeaddonfabric.screen.button.TextFieldWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

import java.util.function.Consumer;

@SuppressWarnings("unused")
public class WidgetConfigureScreen extends Screen {
    public static final Identifier TEXTURE = new Identifier("kzeaddon-fabric", "textures/gui/option/background/widget_configure.png");
    private final Consumer<WidgetConfigureScreen> saveConsumer;
    private int cX;
    private int cY;
    private Anchor windowAnc;
    private Anchor widgetAnc;
    private int x;
    private int y;
    private boolean isVisible;
    private short opacity;

    private String posXContent;
    private String posYContent;
    private String opacityContent;

    private AnchorSelectButton windowSelectWid;
    private AnchorSelectButton widgetSelectWid;
    private TextFieldWidget xWid;
    private TextFieldWidget yWid;
    private TextFieldWidget opacityWid;
    private ButtonWidget visibilityWid;

    public WidgetConfigureScreen(Anchor windowAnc, Anchor widgetAnc, int x, int y, Consumer<WidgetConfigureScreen> saveConsumer) {
        super(new LiteralText("WidgetOptionScreen"));

        this.windowAnc = windowAnc;
        this.widgetAnc = widgetAnc;
        this.x = x;
        this.y = y;
        this.saveConsumer = saveConsumer;
    }

    public WidgetConfigureScreen(Anchor windowAnc, Anchor widgetAnc, int x, int y) {
        super(new LiteralText("WidgetOptionScreen"));

        this.windowAnc = windowAnc;
        this.widgetAnc = widgetAnc;
        this.x = x;
        this.y = y;
        this.saveConsumer = unused -> {};
    }

    public WidgetConfigureScreen(Widget widget) {
        super(new LiteralText("WidgetOptionScreen"));

        this.windowAnc = widget.getWindowAnchor();
        this.widgetAnc = widget.getWidgetAnchor();
        this.x = widget.getOffsetX();
        this.y = widget.getOffsetY();
        this.isVisible = widget.isVisible();
        this.opacity = widget.getOpacity();

        this.saveConsumer = screen -> {
            widget.setWindowAnchor(this.getWindowAnc());
            widget.setWidgetAnchor(this.getWidgetAnc());
            widget.setOffsetX(this.getX());
            widget.setOffsetY(this.getY());
            widget.setVisibility(this.isVisible());
            widget.setOpacity(this.getOpacity());
        };
    }

    public void close(boolean shouldSave) {
        if (shouldSave) this.saveConsumer.accept(this);
        this.onClose();
    }

    public short getOpacity() {
        return this.opacity;
    }

    public void setOpacity(short opacity) {
        this.opacity = opacity;
    }

    public Text getVisibilityMessage() {
        return new TranslatableText("menu.kzeaddon." + (this.isVisible ? "show" : "hide"));
    }

    public Anchor getWidgetAnc() {
        return this.widgetAnc;
    }

    public void setWidgetAnc(Anchor widgetAnc) {
        this.widgetAnc = widgetAnc;
    }

    public Anchor getWindowAnc() {
        return this.windowAnc;
    }

    public void setWindowAnc(Anchor windowAnc) {
        this.windowAnc = windowAnc;
    }

    public int getX() {
        return this.x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return this.y;
    }

    public void setY(int y) {
        this.y = y;
    }

    @Override
    protected void init() {
        MinecraftClient client = MinecraftClient.getInstance();
        Window window = client.getWindow();

        this.cX = window.getScaledWidth() / 2;
        this.cY = window.getScaledHeight() / 2;

        this.windowSelectWid = new AnchorSelectButton(this.windowAnc, this.cX - 108, this.cY - 68, 20, 20, value -> this.windowAnc = value);
        this.widgetSelectWid = new AnchorSelectButton(this.widgetAnc, this.cX - 28, this.cY - 69, 20, 20, value -> this.widgetAnc = value);

        this.addButton(new TexturedButtonWidget(this.cX - 109, this.cY + 58, 30, 16, 0, 177, 16, TEXTURE, 256, 256, btn -> this.close(false), (button, matrices, mouseX, mouseY) -> this.renderTooltip(matrices, new TranslatableText("menu.kzeaddon.option.discard"), mouseX, mouseY), Text.of("Discard button")));
        this.addButton(new TexturedButtonWidget(this.cX - 74, this.cY + 58, 30, 16, 30, 177, 16, TEXTURE, 256, 256, btn -> this.close(true), (button, matrices, mouseX, mouseY) -> this.renderTooltip(matrices, new TranslatableText("menu.kzeaddon.option.save"), mouseX, mouseY), Text.of("Save button")));
        this.addButton(new TexturedButtonWidget(this.cX - 39, this.cY + 58, 30, 16, 60, 177, 16, TEXTURE, 256, 256, btn -> {}, (button, matrices, mouseX, mouseY) -> this.renderTooltip(matrices, new TranslatableText("menu.kzeaddon.option.preview"), mouseX, mouseY), Text.of("Preview button")));
        this.posXContent = String.valueOf(this.x);
        this.posYContent = String.valueOf(this.y);
        this.xWid = new TextFieldWidget(this.textRenderer, this.cX - 65, this.cY - 12, 61, 20, Text.of("X Field"));
        this.yWid = new TextFieldWidget(this.textRenderer, this.cX - 65, this.cY + 13, 61, 20, Text.of("Y Field"));
        this.xWid.setText(this.posXContent);
        this.yWid.setText(this.posYContent);

        this.xWid.setChangedListener(s -> {
            if (!s.equals(this.posXContent)) {
                this.posXContent = s;
                try {
                    this.x = Integer.parseInt(this.posXContent);
                    this.xWid.setEditableColor(0xFFFFFF);
                } catch (NumberFormatException ex) {
                    this.xWid.setEditableColor(0xFF0000);
                }
            } else {
                this.setFocusedTFW(this.xWid);
            }
        });
        this.yWid.setChangedListener(s -> {
            if (!s.equals(this.posYContent)) {
                this.posYContent = s;
                try {
                    this.y = Integer.parseInt(this.posYContent);
                    this.yWid.setEditableColor(0xFFFFFF);
                } catch (NumberFormatException ex) {
                    this.yWid.setEditableColor(0xFF0000);
                }
            } else {
                this.setFocusedTFW(this.yWid);
            }
        });

        this.addTextField(this.xWid);
        this.addTextField(this.yWid);

        int x = this.cX + 64;
        int aY = 17;
        int bY = this.cY + 88 - 16;

        this.visibilityWid = new ButtonWidget(x - 70 / 2, bY - 20, 70, 20, this.getVisibilityMessage(), this::toggleVisibility);
        this.opacityWid = new TextFieldWidget(this.textRenderer, x + 5, bY - 50, 35, 20, Text.of("Opacity Field"));
        this.opacityWid.setText(String.valueOf(this.opacity));
        this.opacityWid.setChangedListener(s -> {
            if (!s.equals(this.opacityContent)) {
                this.opacityContent = s;
                try {
                    this.opacity = Short.parseShort(this.opacityContent);
                    this.opacityWid.setEditableColor(0xFFFFFF);
                } catch (NumberFormatException ex) {
                    this.opacityWid.setEditableColor(0xFF0000);
                }
            } else {
                this.setFocusedTFW(this.opacityWid);
            }
        });

        this.addButton(this.visibilityWid);
        this.addTextField(this.opacityWid);

        this.initAdditionalElements();
    }

    protected void initAdditionalElements() {
    }

    public boolean isVisible() {
        return this.isVisible;
    }

    public void setVisible(boolean visible) {
        this.isVisible = visible;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.windowSelectWid.mouseClicked(mouseX, mouseY, button)) return true;
        if (this.widgetSelectWid.mouseClicked(mouseX, mouseY, button)) return true;
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        if (this.client.world == null) this.renderBackground(matrices);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        Window window = this.client.getWindow();

        // Rendering the background
        int width = 256;
        int height = 177;
        matrices.push();
        matrices.translate(this.cX, this.cY, 0.0);
        this.client.getTextureManager().bindTexture(TEXTURE);
        this.drawTexture(matrices, -width / 2, -height / 2, 0, 0, width, height);
        matrices.pop();

        // Render buttons
        super.render(matrices, mouseX, mouseY, delta);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);

        this.windowSelectWid.render(matrices, mouseX, mouseY, delta);
        this.widgetSelectWid.render(matrices, mouseX, mouseY, delta);

        // Custom widgets
        int x = this.cX + 63;
        int aY = 17;
        int bY = this.cY + 88 - 16;
        int lineLength = (int) (128 * 0.6);

        Text opacityText = new TranslatableText("menu.kzeaddon.option.opacity");
        int opacityWidth = this.textRenderer.getWidth(opacityText);
        this.textRenderer.draw(matrices, opacityText, x - 5 - opacityWidth, bY - 45, 0xFF000000);

        int x1 = x - lineLength / 2;
        int x2 = x + lineLength / 2;
        RenderSystem.disableTexture();
        this.drawHorizontalLine(matrices, x1, x2, bY - 25, 0xFF2E2E2E);
        this.drawHorizontalLine(matrices, x1, x2, bY - 55, 0xFF2E2E2E);
        RenderSystem.enableTexture();

        // Vanilla but additional widgets
        this.renderAdditionalElements(matrices, delta);
    }

    protected void renderAdditionalElements(MatrixStack matrices, float tickDelta) {
    }

    public void toggleVisibility(ButtonWidget btn) {
        this.isVisible = !this.isVisible;
        this.updateVisibilityButton();
    }

    public void updateVisibilityButton() {
        this.visibilityWid.setMessage(this.getVisibilityMessage());
    }
}
