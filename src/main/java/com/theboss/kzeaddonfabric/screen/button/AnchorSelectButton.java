package com.theboss.kzeaddonfabric.screen.button;

import com.mojang.blaze3d.systems.RenderSystem;
import com.theboss.kzeaddonfabric.KZEAddon;
import com.theboss.kzeaddonfabric.enums.Anchor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.Element;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;

import java.util.function.Consumer;

public class AnchorSelectButton extends DrawableHelper implements Element, Drawable {
    private static final Identifier TEXTURE = new Identifier(KZEAddon.MOD_ID, "textures/gui/option/anchor_select.png");
    private static final Logger LOGGER = LogManager.getLogger("AnchorSelectButton");
    private static long lastLogTimestamp = 0;
    private static final int logInterval = 1_000; // By milli seconds
    private final Consumer<AnchorSelectButton> onClick;
    private final Consumer<Anchor> saveConsumer;
    private Anchor value;
    private int x;
    private int y;
    private int width;
    private int height;

    public AnchorSelectButton(Anchor anchor, int x, int y, int width, int height, Consumer<Anchor> saveConsumer, Consumer<AnchorSelectButton> onClick) {
        this.value = anchor;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.saveConsumer = saveConsumer;
        this.onClick = onClick;
    }

    public AnchorSelectButton(Anchor anchor, int x, int y, int width, int height, Consumer<Anchor> saveConsumer) {
        this(anchor, x, y, width, height, saveConsumer, unused -> {});
    }

    public AnchorSelectButton(Anchor anchor, int x, int y, int width, int height) {
        this(anchor, x, y, width, height, unused -> {});
    }

    public static Anchor getById(int id) {
        if (id < 0 || id > 8) return null;
        return Anchor.values()[id];
    }

    protected static void log(String str) {
        long timestamp = System.currentTimeMillis();
        long elapsed = timestamp - AnchorSelectButton.lastLogTimestamp;
        if (elapsed >= AnchorSelectButton.logInterval) {
            AnchorSelectButton.lastLogTimestamp = timestamp;
            AnchorSelectButton.LOGGER.log(Level.INFO, str);
        }
    }

    public int isMouseOverOnPart(double mouseX, double mouseY) {
        float[] innerX = new float[]{this.x + 3, this.x + this.width - 3};
        float[] innerY = new float[]{this.y + 3, this.y + this.height - 3};
        float innerWidth = innerX[1] - innerX[0];
        float innerHeight = innerY[1] - innerY[0];
        float partX = innerWidth / 3;
        float partY = innerHeight / 3;

        if ((mouseX <= innerX[0] || mouseX >= innerX[1]) || (mouseY <= innerY[0] || mouseY >= innerY[1])) return -1;

        double diffX = mouseX - innerX[0];
        double diffY = mouseY - innerY[0];

        return ((int) Math.floor(diffX / partX)) + ((int) Math.floor(diffY / partY)) * 3;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        float v = this.isMouseOver(mouseX, mouseY) ? 0.5F : 0F;
        float[] innerX = new float[]{this.x + 3, this.x + this.width - 3};
        float[] innerY = new float[]{this.y + 3, this.y + this.height - 3};
        float innerWidth = innerX[1] - innerX[0];
        float innerHeight = innerY[1] - innerY[0];
        float partX = innerWidth / 3;
        float partY = innerHeight / 3;

        MinecraftClient.getInstance().getTextureManager().bindTexture(AnchorSelectButton.TEXTURE);
        buffer.begin(GL11.GL_QUADS, VertexFormats.POSITION_TEXTURE);
        buffer.vertex(this.x, this.y, 0).texture(0F, v).next();
        buffer.vertex(this.x, this.y + this.height, 0).texture(0F, v + 0.5F).next();
        buffer.vertex(this.x + this.width, this.y + this.height, 0).texture(1F, v + 0.5F).next();
        buffer.vertex(this.x + this.width, this.y, 0).texture(1F, v).next();
        tessellator.draw();

        RenderSystem.disableTexture();
        buffer.begin(GL11.GL_QUADS, VertexFormats.POSITION_COLOR);
        buffer.vertex(innerX[0], innerY[0], 0).color(0, 0, 0, 255).next();
        buffer.vertex(innerX[0], innerY[1], 0).color(0, 0, 0, 255).next();
        buffer.vertex(innerX[1], innerY[1], 0).color(0, 0, 0, 255).next();
        buffer.vertex(innerX[1], innerY[0], 0).color(0, 0, 0, 255).next();
        tessellator.draw();

        float vX = innerX[0] + (innerWidth - partX) * this.value.getX();
        float vY = innerY[0] + (innerHeight - partY) * this.value.getY();
        buffer.begin(GL11.GL_QUADS, VertexFormats.POSITION_COLOR);
        buffer.vertex(vX, vY, 0).color(100, 100, 100, 255).next();
        buffer.vertex(vX, vY + partY, 0).color(100, 100, 100, 255).next();
        buffer.vertex(vX + partX, vY + partY, 0).color(100, 100, 100, 255).next();
        buffer.vertex(vX + partX, vY, 0).color(100, 100, 100, 255).next();
        tessellator.draw();

        buffer.begin(GL11.GL_LINES, VertexFormats.POSITION);
        buffer.vertex(innerX[0], innerY[0] + partY, 0).next();
        buffer.vertex(innerX[1], innerY[0] + partY, 0).next();
        buffer.vertex(innerX[0], innerY[0] + partY * 2, 0).next();
        buffer.vertex(innerX[1], innerY[0] + partY * 2, 0).next();
        buffer.vertex(innerX[0] + partX, innerY[0], 0).next();
        buffer.vertex(innerX[0] + partX, innerY[1], 0).next();
        buffer.vertex(innerX[0] + partX * 2, innerY[0], 0).next();
        buffer.vertex(innerX[0] + partX * 2, innerY[1], 0).next();
        tessellator.draw();
        RenderSystem.enableTexture();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.isMouseOver(mouseX, mouseY)) {
            this.onClick.accept(this);
            MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            int id = this.isMouseOverOnPart(mouseX, mouseY);
            if (id == -1) {
                AnchorSelectButton.LOGGER.info("Invalid position!");
            } else {
                this.value = AnchorSelectButton.getById(id);
                this.saveConsumer.accept(this.value);
            }
            return true;
        }
        return false;
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

    public int getWidth() {
        return this.width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return this.height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public Anchor getValue() {
        return this.value;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return (mouseX >= this.x && mouseX <= this.x + this.width) && (mouseY >= this.y && mouseY <= this.y + this.height);
    }
}
