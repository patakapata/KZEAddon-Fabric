package com.theboss.kzeaddonfabric.screen.options;

import com.mojang.blaze3d.systems.RenderSystem;
import com.theboss.kzeaddonfabric.Color;
import com.theboss.kzeaddonfabric.KZEAddon;
import com.theboss.kzeaddonfabric.Options;
import com.theboss.kzeaddonfabric.screen.ColorSelectScreen;
import com.theboss.kzeaddonfabric.screen.Screen;
import com.theboss.kzeaddonfabric.screen.button.TextFieldWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
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
    public static final Identifier TEXTURE = new Identifier("kzeaddon-fabric", "textures/gui/option/background/glow_color.png");
    public static final Identifier PREVIEW = new Identifier("kzeaddon-fabric", "textures/gui/option/color_preview.png");

    private int cX;
    private int cY;

    private int priorityValue;
    private int humanValue;
    private int zombieValue;

    private TextFieldWidget priority;
    private TextFieldWidget human;
    private TextFieldWidget zombie;
    private ClickableWidget priorityPreview;
    private ClickableWidget humanPreview;
    private ClickableWidget zombiePreview;

    private ClickableWidget save;
    private ClickableWidget discard;

    public GlowColorOptionScreen(Screen parent) {
        this();
        this.setParent(parent);
    }

    public GlowColorOptionScreen() {
        super(Text.of("GlowColor Option Screen"));
        Options options = KZEAddon.Options;
        this.priorityValue = options.getPriorityGlowColor().get();
        this.humanValue = options.getHumanGlowColor().get();
        this.zombieValue = options.getZombieGlowColor().get();
    }

    protected void saveToOptions() {
        Options options = KZEAddon.Options;

        options.setPriorityGlowColor(new Color(this.priorityValue));
        options.setHumanGlowColor(new Color(this.humanValue));
        options.setZombieGlowColor(new Color(this.zombieValue));
    }

    protected void close(boolean shouldSave) {
        if (shouldSave) {
            this.saveToOptions();
            KZEAddon.LOGGER.info("Glow color configuration has been saved to instance");
        } else {
            KZEAddon.LOGGER.info("Glow color configuration has been discard");
        }
        this.onClose();
    }

    @Override
    protected void init() {
        this.cX = this.width / 2;
        this.cY = this.height / 2;

        this.priority = new TextFieldWidget(this.textRenderer, this.cX + 6, this.cY - 50, 73, 20, Text.of("Priority field"));
        this.human = new TextFieldWidget(this.textRenderer, this.cX + 6, this.cY - 20, 73, 20, Text.of("Human field"));
        this.zombie = new TextFieldWidget(this.textRenderer, this.cX + 6, this.cY + 10, 73, 20, Text.of("Zombie field"));
        this.priorityPreview = new TexturedButtonWidget(this.cX + 84, this.cY - 50, 20, 20, 0, 0, 20, PREVIEW, 32, 64, btn -> this.onPressPreview(btn, 0));
        this.humanPreview = new TexturedButtonWidget(this.cX + 84, this.cY - 20, 20, 20, 0, 0, 20, PREVIEW, 32, 64, btn -> this.onPressPreview(btn, 1));
        this.zombiePreview = new TexturedButtonWidget(this.cX + 84, this.cY + 10, 20, 20, 0, 0, 20, PREVIEW, 32, 64, btn -> this.onPressPreview(btn, 2));

        this.discard = new ButtonWidget(this.cX - 47, this.cY + 61, 44, 20, new TranslatableText("menu.kzeaddon.option.discard"), btn -> this.close(false));
        this.save = new ButtonWidget(this.cX + 3, this.cY + 61, 44, 20, new TranslatableText("menu.kzeaddon.option.save"), btn -> this.close(true));

        this.initTextFieldsContents();

        this.priority.setChangedListener(str -> this.validateColorField(str, this.priority, 0));
        this.human.setChangedListener(str -> this.validateColorField(str, this.human, 1));
        this.zombie.setChangedListener(str -> this.validateColorField(str, this.zombie, 2));

        this.addTextField(this.priority);
        this.addTextField(this.human);
        this.addTextField(this.zombie);
        this.addButton(this.priorityPreview);
        this.addButton(this.humanPreview);
        this.addButton(this.zombiePreview);
        this.addButton(this.save);
        this.addButton(this.discard);
    }

    public void initTextFieldsContents() {
        this.priority.setText(Color.toHexString(this.priorityValue).toUpperCase());
        this.human.setText(Color.toHexString(this.humanValue).toUpperCase());
        this.zombie.setText(Color.toHexString(this.zombieValue).toUpperCase());
    }

    /**
     * Get the Color
     *
     * @param id Target
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
     * Set the Color
     *
     * @param color Value({@link com.theboss.kzeaddonfabric.Color})
     * @param id    Target
     * @return When same to target true, otherwise false
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
        System.out.println("validate with " + id);
        try {
            if (str.length() <= 6) {
                int color = Integer.parseInt(str, 16);
                this.setColorById(color, id);
                this.setFocusedTFW(widget);
                widget.setEditableColor(0xE0E0E0);
            } else {
                widget.setEditableColor(0xFF0000);
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
        if (this.client.world == null) this.renderBackground(matrices);
        // Rendering the background
        this.client.getTextureManager().bindTexture(TEXTURE);
        int width = 256;
        int height = 177;
        matrices.push();
        matrices.translate(this.cX, this.cY, 0.0);
        this.drawTexture(matrices, -width / 2, -height / 2, 0, 0, width, height);
        matrices.pop();
        super.render(matrices, mouseX, mouseY, delta);

        TranslatableText priorityMsg = new TranslatableText("menu.kzeaddon.option.priorityGlowColor");
        TranslatableText humanMsg = new TranslatableText("menu.kzeaddon.option.humanGlowColor");
        TranslatableText zombieMsg = new TranslatableText("menu.kzeaddon.option.zombieGlowColor");

        int msgX = this.cX - 42;
        int fontHeight = this.textRenderer.fontHeight / 2;

        // Priority msg cord
        int priorityMsgY = this.priority.y + this.priority.getHeight() / 2 - fontHeight;
        float priorityMsgX = msgX - this.textRenderer.getWidth(priorityMsg) / 2F;
        //
        int humanMsgY = this.human.y + this.human.getHeight() / 2 - fontHeight;
        float humanMsgX = msgX - this.textRenderer.getWidth(humanMsg) / 2F;
        //
        int zombieMsgY = this.zombie.y + this.zombie.getHeight() / 2 - fontHeight;
        float zombieMsgX = msgX - this.textRenderer.getWidth(zombieMsg) / 2F;

        int textColor = 0x2F2F2F;

        this.textRenderer.draw(matrices, priorityMsg, priorityMsgX, priorityMsgY, textColor);
        this.textRenderer.draw(matrices, humanMsg, humanMsgX, humanMsgY, textColor);
        this.textRenderer.draw(matrices, zombieMsg, zombieMsgX, zombieMsgY, textColor);

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
