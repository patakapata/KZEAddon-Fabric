package com.theboss.kzeaddonfabric.screen.options.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import com.theboss.kzeaddonfabric.enums.Anchor;
import com.theboss.kzeaddonfabric.screen.Screen;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.lwjgl.opengl.GL11;

public class WidgetOptionScreen extends Screen {
    private int cX;
    private int cY;

    private Rectangle background;

    private Anchor widgetAnchorValue;
    private Anchor windowAnchorValue;
    private int offsetXValue;
    private int offsetYValue;
    private int opacityValue;
    private boolean visibilityValue;

    public WidgetOptionScreen(Text title) {
        super(title);

        this.widgetAnchorValue = Anchor.LEFT_UP;
        this.windowAnchorValue = Anchor.LEFT_UP;
    }

    @Override
    protected void init() {
        this.cX = this.width / 2;
        this.cY = this.height / 2;

        this.background = new Rectangle(Anchor.LEFT_UP, 5, 5, this.width - 10, this.height - 10, 0xFFE0E0E0, 0x50F0F0F0);
    }

    public void save() {}

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        if (this.client.world == null) this.renderBackground(matrices);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        this.background.render(matrices);

        super.render(matrices, mouseX, mouseY, delta);
    }

    protected void renderBranch(Tessellator tessellator, BufferBuilder buffer, int x1, int x2, int originY, int[] destY) {
        int cX = x1 + (x2 - x1) / 2;
        RenderSystem.disableTexture();
        buffer.begin(GL11.GL_LINES, VertexFormats.POSITION_COLOR);
        buffer.vertex(x1, originY, 0).color(255, 255, 255, 255).next();
        buffer.vertex(x2, originY, 0).color(255, 255, 255, 255).next();
        int minY = originY;
        int maxY = originY;
        for (int y : destY) {
            buffer.vertex(cX, y, 0).color(255, 255, 255, 255).next();
            buffer.vertex(x2, y, 0).color(255, 255, 255, 255).next();
            if (y < minY) minY = y;
            if (y > maxY) maxY = y;
        }
        buffer.vertex(cX, minY, 0).color(255, 255, 255, 255).next();
        buffer.vertex(cX, maxY, 0).color(255, 255, 255, 255).next();
        tessellator.draw();
        RenderSystem.enableTexture();
    }
}
