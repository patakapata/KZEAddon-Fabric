package com.theboss.kzeaddonfabric.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.theboss.kzeaddonfabric.Color;
import com.theboss.kzeaddonfabric.KZEAddon;
import com.theboss.kzeaddonfabric.Options;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import org.lwjgl.opengl.GL11;

import java.util.function.Consumer;

public class GlowColorOptionScreen extends Screen {
    public static final Identifier PREVIEW = new Identifier("kzeaddon-fabric", "textures/gui/option/color_preview.png");

    private int cX;
    private int cY;

    private int priorityValue;
    private int humanValue;
    private int zombieValue;

    private TextFieldWidget priority;
    private TextFieldWidget human;
    private TextFieldWidget zombie;
    private AbstractButtonWidget priorityPreview;
    private AbstractButtonWidget humanPreview;
    private AbstractButtonWidget zombiePreview;
    private AbstractButtonWidget close;

    public GlowColorOptionScreen(Screen parent) {
        this();
        this.setParent(parent);
    }

    public GlowColorOptionScreen() {
        super(Text.of("GlowColor Option Screen"));
        Options options = KZEAddon.OPTIONS;
        this.priorityValue = options.getPriorityGlowColor().get();
        this.humanValue = options.getHumanGlowColor().get();
        this.zombieValue = options.getZombieGlowColor().get();
    }

    protected void saveToOptions() {
        Options options = KZEAddon.OPTIONS;

        options.setPriorityGlowColor(new Color(this.priorityValue));
        options.setHumanGlowColor(new Color(this.humanValue));
        options.setZombieGlowColor(new Color(this.zombieValue));
    }

    @Override
    public void onClose() {
        this.saveToOptions();
        super.onClose();
    }

    @Override
    protected void init() {
        this.cX = this.width / 2;
        this.cY = this.height / 2;

        this.priority = new TextFieldWidget(this.textRenderer, this.cX + 5, this.cY - 55, 73, 20, Text.of("Priority field"));
        this.human = new TextFieldWidget(this.textRenderer, this.cX + 5, this.cY - 25, 73, 20, Text.of("Human field"));
        this.zombie = new TextFieldWidget(this.textRenderer, this.cX + 5, this.cY + 5, 73, 20, Text.of("Zombie field"));
        this.priorityPreview = new TexturedButtonWidget(this.cX + 83, this.cY - 55, 20, 20, 0, 0, 20, PREVIEW, 32, 64, btn -> this.onPressPreview(btn, 0));
        this.humanPreview = new TexturedButtonWidget(this.cX + 83, this.cY - 25, 20, 20, 0, 0, 20, PREVIEW, 32, 64, btn -> this.onPressPreview(btn, 1));
        this.zombiePreview = new TexturedButtonWidget(this.cX + 83, this.cY + 5, 20, 20, 0, 0, 20, PREVIEW, 32, 64, btn -> this.onPressPreview(btn, 2));
        this.close = new ButtonWidget(this.cX - 49, this.height - 30, 98, 20, new TranslatableText("menu.kzeaddon.option.close"), btn -> this.onClose());

        this.initTextFieldsContents();

        this.priority.setChangedListener(str -> this.validateColorField(str, this.priority, 0));
        this.human.setChangedListener(str -> this.validateColorField(str, this.human, 1));
        this.zombie.setChangedListener(str -> this.validateColorField(str, this.zombie, 2));

        this.addButton(this.priority);
        this.addButton(this.human);
        this.addButton(this.zombie);
        this.addButton(this.priorityPreview);
        this.addButton(this.humanPreview);
        this.addButton(this.zombiePreview);
        this.addButton(this.close);
    }

    public void initTextFieldsContents() {
        this.priority.setText(Color.toHexString(this.priorityValue));
        this.human.setText(Color.toHexString(this.humanValue));
        this.zombie.setText(Color.toHexString(this.zombieValue));
    }

    /**
     * Colorを取得します
     *
     * @param id 対象
     * @return {@link com.theboss.kzeaddonfabric.Color}
     */
    public int getColorById(int id) {
        switch (id) {
            case 0:
                return this.priorityValue;
            case 1:
                return this.humanValue;
            case 2:
                return this.zombieValue;
            default:
                throw new IllegalArgumentException("Out bounds of index : " + id);
        }
    }

    /**
     * Colorを設定します
     *
     * @param color 値({@link com.theboss.kzeaddonfabric.Color})
     * @param id    対象
     * @return 同じだった場合 false 他の場合 true
     */
    public boolean setColorById(int color, int id) {
        if (this.getColorById(id) == color) return false;
        switch (id) {
            case 0:
                this.priorityValue = color;
                break;
            case 1:
                this.humanValue = color;
                break;
            case 2:
                this.zombieValue = color;
                break;
            default:
                throw new IllegalArgumentException("Out bounds of index : " + id);
        }
        return true;
    }

    protected void validateColorField(String str, TextFieldWidget widget, int id) {
        try {
            int color = Integer.parseInt(str, 16);
            if (this.setColorById(color, id)) {
                widget.setEditableColor(0xE0E0E0);
            }
        } catch (Exception ex) {
            widget.setEditableColor(0xFF0000);
        }
    }

    public void onPressPreview(ButtonWidget btn, int id) {
        int color = this.getColorById(id);
        Consumer<Color> saveConsumer = value -> this.setColorById(value.get(), id);
        this.openColorSelector(color, saveConsumer);
    }

    public void openColorSelector(int color, Consumer<Color> saveConsumer) {
        Screen screen = new ColorSelectScreen(color, saveConsumer);
        screen.setParent(this);
        this.client.openScreen(screen);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        if (client.world == null) this.renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);

        int msgX = this.cX - 5;
        int fontHeight = textRenderer.fontHeight / 2;
        int priorityMsgY = this.priority.y + this.priority.getHeight() / 2 - fontHeight;
        int humanMsgY = this.human.y + this.human.getHeight() / 2 - fontHeight;
        int zombieMsgY = this.zombie.y + this.zombie.getHeight() / 2 - fontHeight;

        TranslatableText priorityMsg = new TranslatableText("menu.kzeaddon.option.priorityGlowColor");
        TranslatableText humanMsg = new TranslatableText("menu.kzeaddon.option.humanGlowColor");
        TranslatableText zombieMsg = new TranslatableText("menu.kzeaddon.option.zombieGlowColor");

        this.textRenderer.drawWithShadow(matrices, priorityMsg, msgX - this.textRenderer.getWidth(priorityMsg), priorityMsgY, 0xE0E0E0);
        this.textRenderer.drawWithShadow(matrices, humanMsg, msgX - this.textRenderer.getWidth(humanMsg), humanMsgY, 0xE0E0E0);
        this.textRenderer.drawWithShadow(matrices, zombieMsg, msgX - this.textRenderer.getWidth(zombieMsg), zombieMsgY, 0xE0E0E0);

        int[] priorityColor = Color.parse(this.priorityValue);
        int[] humanColor = Color.parse(this.humanValue);
        int[] zombieColor = Color.parse(this.zombieValue);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        RenderSystem.disableTexture();
        buffer.begin(GL11.GL_QUADS, VertexFormats.POSITION_COLOR);
        buffer.vertex(this.priorityPreview.x + 2, this.priorityPreview.y + 2, 1.0F).color(priorityColor[0], priorityColor[1], priorityColor[2], 255).next();
        buffer.vertex(this.priorityPreview.x + 2, this.priorityPreview.y + 18, 1.0F).color(priorityColor[0], priorityColor[1], priorityColor[2], 255).next();
        buffer.vertex(this.priorityPreview.x + 18, this.priorityPreview.y + 18, 1.0F).color(priorityColor[0], priorityColor[1], priorityColor[2], 255).next();
        buffer.vertex(this.priorityPreview.x + 18, this.priorityPreview.y + 2, 1.0F).color(priorityColor[0], priorityColor[1], priorityColor[2], 255).next();

        buffer.vertex(this.humanPreview.x + 2, this.humanPreview.y + 2, 1.0F).color(humanColor[0], humanColor[1], humanColor[2], 255).next();
        buffer.vertex(this.humanPreview.x + 2, this.humanPreview.y + 18, 1.0F).color(humanColor[0], humanColor[1], humanColor[2], 255).next();
        buffer.vertex(this.humanPreview.x + 18, this.humanPreview.y + 18, 1.0F).color(humanColor[0], humanColor[1], humanColor[2], 255).next();
        buffer.vertex(this.humanPreview.x + 18, this.humanPreview.y + 2, 1.0F).color(humanColor[0], humanColor[1], humanColor[2], 255).next();

        buffer.vertex(this.zombiePreview.x + 2, this.zombiePreview.y + 2, 1.0F).color(zombieColor[0], zombieColor[1], zombieColor[2], 255).next();
        buffer.vertex(this.zombiePreview.x + 2, this.zombiePreview.y + 18, 1.0F).color(zombieColor[0], zombieColor[1], zombieColor[2], 255).next();
        buffer.vertex(this.zombiePreview.x + 18, this.zombiePreview.y + 18, 1.0F).color(zombieColor[0], zombieColor[1], zombieColor[2], 255).next();
        buffer.vertex(this.zombiePreview.x + 18, this.zombiePreview.y + 2, 1.0F).color(zombieColor[0], zombieColor[1], zombieColor[2], 255).next();
        tessellator.draw();
        RenderSystem.enableTexture();
    }
}
