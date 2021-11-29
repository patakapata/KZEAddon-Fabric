package com.theboss.kzeaddonfabric.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.theboss.kzeaddonfabric.KZEAddon;
import com.theboss.kzeaddonfabric.widgets.Widget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.Matrix4f;
import org.lwjgl.opengl.GL11;

import java.util.List;

public class LiteralWidgetsScreen extends Screen {
    private final List<Widget> widgets;
    private int margin;
    private float scroll;
    private int selected;

    public LiteralWidgetsScreen(List<Widget> widgets) {
        super(Text.of("Literal widgets screen"));
        this.widgets = widgets;
        this.margin = 5;
        this.scroll = 0;
        this.selected = -1;
    }

    @Override
    protected void init() {
        int cX = this.width / 2;
        int cY = this.height / 2;

        // -------------------------------------------------- //
        // Bottom
        this.addButton(new ButtonWidget(cX - 55, this.height - 30, 50, 20, new TranslatableText("button.literal_widgets.kzeaddon.delete"), this::deletePressed));
        this.addButton(new ButtonWidget(cX + 5, this.height - 30, 50, 20, new TranslatableText("button.literal_widgets.kzeaddon.arrange"), this::arrangePressed));
    }

    public int getSelected() {
        return this.selected;
    }

    public void setSelected(int selected) {
        this.selected = selected;
    }

    public void setMargin(int margin) {
        this.margin = margin;
    }

    protected boolean assertSelect() {
        return this.selected != -1;
    }

    protected void deletePressed(ButtonWidget btn) {
        if (this.assertSelect()) {
            KZEAddon.LOGGER.info("Delete button Pressed");
            this.widgets.remove(this.selected);
            this.selected--;
            if (this.widgets.size() == 0) this.selected = -1;
            else if (this.selected < 0) this.selected = 0;
        }
    }

    protected void arrangePressed(ButtonWidget btn) {
        if (this.assertSelect()) {
            KZEAddon.LOGGER.info("Arrange button Pressed");
            KZEAddon.widgetRenderer.openArrangementScreen(this.widgets.get(this.selected));
        }
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        this.drawBottom(matrices);
        super.render(matrices, mouseX, mouseY, delta);
        int cX = this.width / 2;

        float y;
        for (int i = 0; i < this.widgets.size(); i++) {
            Widget widget = this.widgets.get(i);
            int color = widget.getColor() | 0xFF000000;
            y = this.getOffset() + this.getEntryHeight() * i;
            Text text = widget.getText();
            int width = this.textRenderer.getWidth(text);
            this.textRenderer.drawWithShadow(matrices, text, cX - width / 2F, y, color);
        }

        this.drawHighlight(matrices);
    }

    private void drawHighlight(MatrixStack matrices) {
        if (this.selected == -1) return;

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        Matrix4f matrix = matrices.peek().getModel();
        float y = (-this.margin / 2F) + this.getOffset() + this.getEntryHeight() * this.selected;
        int cX = this.width / 2;
        float width = this.textRenderer.getWidth(this.widgets.get(this.selected).getText()) / 2F + 2;

        buffer.begin(GL11.GL_LINE_LOOP, VertexFormats.POSITION_COLOR);
        buffer.vertex(matrix, cX - width, y, 0).color(1F, 1F, 1F, 1F).next();
        buffer.vertex(matrix, cX - width, y + this.getEntryHeight(), 0).color(1F, 1F, 1F, 1F).next();
        buffer.vertex(matrix, cX + width, y + this.getEntryHeight(), 0).color(1F, 1F, 1F, 1F).next();
        buffer.vertex(matrix, cX + width, y, 0).color(1F, 1F, 1F, 1F).next();

        RenderSystem.disableTexture();
        tessellator.draw();
        RenderSystem.enableTexture();
    }

    private float getOffset() {
        return this.margin + this.scroll;
    }

    private float getMaxHeight() {
        return this.getOffset() + this.getEntryHeight() * this.widgets.size();
    }

    private void drawBottom(MatrixStack matrices) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        Matrix4f matrix = matrices.peek().getModel();

        buffer.begin(GL11.GL_QUADS, VertexFormats.POSITION_COLOR);
        buffer.vertex(matrix, 0, this.height - 50, 0).color(0f, 0f, 0f, 0f).next();
        buffer.vertex(matrix, 0, this.height, 0).color(0f, 0f, 0f, 1f).next();
        buffer.vertex(matrix, this.width, this.height, 0).color(0f, 0f, 0f, 1f).next();
        buffer.vertex(matrix, this.width, this.height - 50, 0).color(0f, 0f, 0f, 0f).next();

        RenderSystem.enableBlend();
        RenderSystem.shadeModel(GL11.GL_SMOOTH);
        tessellator.draw();
        RenderSystem.shadeModel(GL11.GL_FLAT);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (super.mouseScrolled(mouseX, mouseY, amount)) return true;
        this.scroll += amount * 2;
        float maxScroll = -this.getEntryHeight() * (this.widgets.size() - 1);
        if (this.scroll > 0) this.scroll = 0;
        else if (this.scroll <= maxScroll) this.scroll = maxScroll;
        KZEAddon.LOGGER.info("Scroll > " + this.scroll + "(" + this.getMaxHeight() + ")");
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) return true;
        this.selected = this.getWidgetIndex(mouseY);
        return true;
    }

    protected float getEntryHeight() {
        return this.textRenderer.fontHeight + this.margin;
    }

    protected int getWidgetIndex(double y) {
        float start = this.margin / 2F + this.scroll;
        if (y < start) return -1;
        int result = (int) ((y - start) / this.getEntryHeight());
        return result >= this.widgets.size() ? -1 : result;
    }
}
