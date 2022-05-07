package com.theboss.kzeaddonfabric.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.theboss.kzeaddonfabric.KZEAddon;
import com.theboss.kzeaddonfabric.widgets.impl.TextWidget;
import com.theboss.kzeaddonfabric.widgets.api.Widget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix4f;

import java.util.List;

import static com.theboss.kzeaddonfabric.render.Constants.*;

public class WidgetListScreen extends Screen {
    private final List<Widget> widgets;
    private int margin;
    private float scroll;
    private int selected;
    private ButtonWidget deleteButton;
    private ButtonWidget editButton;

    public WidgetListScreen(List<Widget> widgets) {
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
        this.deleteButton = new ButtonWidget(cX - 55, this.height - 30, 50, 20, new TranslatableText("menu.kzeaddon.delete"), this::onDeleteButtonPressed);
        this.editButton = new ButtonWidget(cX + 5, this.height - 30, 50, 20, new TranslatableText("menu.kzeaddon.edit"), this::onEditButtonPressed);

        this.updateButtonState();

        this.addButton(this.deleteButton);
        this.addButton(this.editButton);
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

    protected void onDeleteButtonPressed(ButtonWidget btn) {
        if (this.assertSelect()) {
            Widget widget = this.widgets.get(this.selected);

            if (widget instanceof TextWidget) {
                KZEAddon.getWidgetRenderer().removeText((TextWidget) widget);
            } else {
                KZEAddon.getWidgetRenderer().remove(widget);
            }

            this.widgets.remove(this.selected);
            this.selected--;
            if (this.widgets.size() == 0) this.selected = -1;
            else if (this.selected < 0) this.selected = 0;
            this.updateButtonState();
        }
    }

    protected void onEditButtonPressed(ButtonWidget btn) {
        if (this.assertSelect()) {
            KZEAddon.getWidgetRenderer().openEditScreen(this.widgets.get(this.selected));
        }
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        this.drawBottom(matrices);
        super.render(matrices, mouseX, mouseY, delta);
        int cX = this.width / 2;

        this.drawHighlight(matrices);

        float y;
        for (int i = 0; i < this.widgets.size(); i++) {
            Widget widget = this.widgets.get(i);
            int color = widget.getColor() | 0xFF000000;
            y = this.getOffset() + this.getEntryHeight() * i;
            Text text = widget.getName();
            int width = this.textRenderer.getWidth(text);
            this.textRenderer.drawWithShadow(matrices, text, cX - width / 2F, y, color);
        }

        boolean isActive = this.deleteButton.active;
        this.deleteButton.active = true;
        if (this.selected != -1 && this.widgets.get(this.selected).isBuiltIn() && this.deleteButton.isMouseOver(mouseX, mouseY)) {
            LiteralText body = new LiteralText("組み込みウィジェットは削除不可能です");
            body.setStyle(body.getStyle().withColor(Formatting.RED));
            this.renderTooltip(matrices, body, MathHelper.floor(mouseX), MathHelper.floor(mouseY));
        }
        this.deleteButton.active = isActive;
    }

    private void drawHighlight(MatrixStack matrices) {
        if (this.selected == -1) return;

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        Matrix4f matrix = matrices.peek().getModel();
        float y = (-this.margin / 2F) + this.getOffset() + this.getEntryHeight() * this.selected;
        int cX = this.width / 2;
        float width = this.textRenderer.getWidth(this.widgets.get(this.selected).getName()) / 2F + 2;

        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        buffer.begin(GL_LINE_LOOP, VertexFormats.POSITION_COLOR);
        buffer.vertex(matrix, cX - width, y, 0).color(1F, 1F, 1F, 1F).next();
        buffer.vertex(matrix, cX - width, y + this.getEntryHeight(), 0).color(1F, 1F, 1F, 1F).next();
        buffer.vertex(matrix, cX + width, y + this.getEntryHeight(), 0).color(1F, 1F, 1F, 1F).next();
        buffer.vertex(matrix, cX + width, y, 0).color(1F, 1F, 1F, 1F).next();
        tessellator.draw();
        buffer.begin(GL_QUADS, VertexFormats.POSITION_COLOR);
        buffer.vertex(matrix, cX - width, y, 0).color(1F, 1F, 1F, 0.5F).next();
        buffer.vertex(matrix, cX - width, y + this.getEntryHeight(), 0).color(1F, 1F, 1F, 0.5F).next();
        buffer.vertex(matrix, cX + width, y + this.getEntryHeight(), 0).color(1F, 1F, 1F, 0.5F).next();
        buffer.vertex(matrix, cX + width, y, 0).color(1F, 1F, 1F, 0.5F).next();
        tessellator.draw();
        RenderSystem.disableBlend();
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

        buffer.begin(GL_QUADS, VertexFormats.POSITION_COLOR);
        buffer.vertex(matrix, 0, this.height - 50, 0).color(0f, 0f, 0f, 0f).next();
        buffer.vertex(matrix, 0, this.height, 0).color(0f, 0f, 0f, 1f).next();
        buffer.vertex(matrix, this.width, this.height, 0).color(0f, 0f, 0f, 1f).next();
        buffer.vertex(matrix, this.width, this.height - 50, 0).color(0f, 0f, 0f, 0f).next();

        RenderSystem.enableBlend();
        RenderSystem.shadeModel(GL_SMOOTH);
        tessellator.draw();
        RenderSystem.shadeModel(GL_FLAT);
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
        this.updateButtonState();
        return true;
    }

    public void updateButtonState() {
        this.deleteButton.active = this.selected != -1 && !this.widgets.get(this.selected).isBuiltIn();
        this.editButton.active = this.selected != -1;
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
