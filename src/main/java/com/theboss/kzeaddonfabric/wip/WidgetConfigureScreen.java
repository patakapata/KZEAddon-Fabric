package com.theboss.kzeaddonfabric.wip;

import com.mojang.blaze3d.systems.RenderSystem;
import com.theboss.kzeaddonfabric.enums.Anchor;
import com.theboss.kzeaddonfabric.render.widgets.Widget;
import com.theboss.kzeaddonfabric.screen.Screen;
import com.theboss.kzeaddonfabric.screen.button.AnchorSelectButton;
import com.theboss.kzeaddonfabric.screen.button.TextFieldWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.function.Consumer;

@SuppressWarnings("unused")
public class WidgetConfigureScreen extends Screen {
    public static final Identifier TEXTURE = new Identifier("kzeaddon-fabric", "textures/gui/option/background/widget_configure.png");

    private int cX;
    private int cY;
    private Consumer<WidgetConfigureScreen> saveConsumer;

    private Anchor windowAnc;
    private Anchor widgetAnc;
    private int x;
    private int y;

    private String posXContent;
    private String posYContent;

    private AnchorSelectButton windowSelect;
    private AnchorSelectButton widgetSelect;
    private TextFieldWidget posX;
    private TextFieldWidget posY;

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

        this.saveConsumer = screen -> {
            widget.setWindowAnchor(this.windowAnc);
            widget.setWidgetAnchor(this.widgetAnc);
            widget.setOffsetX(this.x);
            widget.setOffsetY(this.y);
        };
    }

    @Override
    protected void init() {
        MinecraftClient client = MinecraftClient.getInstance();
        Window window = client.getWindow();

        this.cX = window.getScaledWidth() / 2;
        this.cY = window.getScaledHeight() / 2;

        this.windowSelect = new AnchorSelectButton(this.windowAnc, this.cX - 108, this.cY - 68, 20, 20, value -> this.windowAnc = value);
        this.widgetSelect = new AnchorSelectButton(this.widgetAnc, this.cX - 28, this.cY - 69, 20, 20, value -> this.widgetAnc = value);

        this.addButton(new TexturedButtonWidget(this.cX - 109, this.cY + 58, 30, 16, 0, 177, 16, TEXTURE, 256, 256, btn -> this.close(false), Text.of("Discard button")));
        this.addButton(new TexturedButtonWidget(this.cX - 74, this.cY + 58, 30, 16, 30, 177, 16, TEXTURE, 256, 256, btn -> this.close(true), Text.of("Save button")));
        this.addButton(new TexturedButtonWidget(this.cX - 39, this.cY + 58, 30, 16, 60, 177, 16, TEXTURE, 256, 256, btn -> {}, Text.of("Preview button")));
        this.posXContent = String.valueOf(this.x);
        this.posYContent = String.valueOf(this.y);
        this.posX = new TextFieldWidget(this.textRenderer, this.cX - 65, this.cY - 12, 61, 20, Text.of("X Field"));
        this.posY = new TextFieldWidget(this.textRenderer, this.cX - 65, this.cY + 13, 61, 20, Text.of("Y Field"));
        this.posX.setText(this.posXContent);
        this.posY.setText(this.posYContent);

        this.posX.setChangedListener(s -> {
            if (!s.equals(this.posXContent)) {
                this.posXContent = s;
                try {
                    this.x = Integer.parseInt(this.posXContent);
                    this.posX.setEditableColor(0xFFFFFF);
                } catch (NumberFormatException ex) {
                    this.posX.setEditableColor(0xFF0000);
                }
            } else {
                this.posY.setSelected(false);
            }
        });
        this.posY.setChangedListener(s -> {
            if (!s.equals(this.posYContent)) {
                this.posYContent = s;
                try {
                    this.y = Integer.parseInt(this.posYContent);
                    this.posY.setEditableColor(0xFFFFFF);
                } catch (NumberFormatException ex) {
                    this.posY.setEditableColor(0xFF0000);
                }
            } else {
                this.posX.setSelected(false);
            }
        });

        this.addTextField(this.posX);
        this.addTextField(this.posY);
    }

    public void close(boolean shouldSave) {
        if (shouldSave) this.saveConsumer.accept(this);
        this.onClose();
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

    public Anchor getWindowAnc() {
        return this.windowAnc;
    }

    public void setWindowAnc(Anchor windowAnc) {
        this.windowAnc = windowAnc;
    }

    public Anchor getWidgetAnc() {
        return this.widgetAnc;
    }

    public void setWidgetAnc(Anchor widgetAnc) {
        this.widgetAnc = widgetAnc;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
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

        this.windowSelect.render(matrices, mouseX, mouseY, delta);
        this.widgetSelect.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.windowSelect.mouseClicked(mouseX, mouseY, button)) return true;
        if (this.widgetSelect.mouseClicked(mouseX, mouseY, button)) return true;
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
