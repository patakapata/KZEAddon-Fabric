package com.theboss.kzeaddonfabric.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.theboss.kzeaddonfabric.utils.RenderingUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class RadialTextureScreen extends Screen {
    private final int texture;
    private boolean updateProgress = true;
    private double progress;
    private float size = 100;


    public RadialTextureScreen(Identifier texture) {
        super(Text.of("Radial texture screen"));

        AbstractTexture tex = MinecraftClient.getInstance().getTextureManager().getTexture(texture);
        this.texture = tex != null ? tex.getGlId() : 0;
    }

    public RadialTextureScreen(int texture) {
        super(Text.of("Radial texture screen"));

        this.texture = texture;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            this.updateProgress = !this.updateProgress;
            return true;
        }
        return false;
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
        MinecraftClient mc = MinecraftClient.getInstance();
        Window window = mc.getWindow();

        this.renderWIPText(matrices);

        matrices.push();
        matrices.translate(window.getScaledWidth() / 2.0, window.getScaledHeight() / 2.0, 0);
        this.radial(matrices, delta);
        matrices.pop();
    }

    public void radial(MatrixStack matrices, float delta) {
        if (this.updateProgress) {
            this.progress = System.currentTimeMillis() % 3_000 / 3_000F;
        }
        RenderSystem.bindTexture(this.texture);
        RenderSystem.enableBlend();
        RenderingUtils.drawRadialTexture(matrices, this.progress, this.size, 0, 0, 1, 1);
        RenderSystem.disableBlend();
        RenderSystem.bindTexture(0);
    }
}
