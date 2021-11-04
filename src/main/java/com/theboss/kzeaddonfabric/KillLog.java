package com.theboss.kzeaddonfabric;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix4f;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class KillLog {
    public static final Identifier MISSING_SKIN = new Identifier(KZEAddon.MOD_ID, "textures/gui/missing_skin.png");
    public static int SEPARATOR;

    private final MinecraftClient mc;
    private final List<Entry> logs;
    private int showLines;
    private int verticalMargin;
    private int horizontalMargin;

    public KillLog(MinecraftClient mc, int verticalMargin, int horizontalMargin, int showLines) {
        this.mc = mc;
        this.logs = new ArrayList<>();
        this.verticalMargin = verticalMargin;
        this.horizontalMargin = horizontalMargin;
        this.showLines = showLines;

        SEPARATOR = this.mc.textRenderer.getWidth(" ");

        // -------------------------------------------------- //
        // Example entries
        this.logs.add(new Entry(new Player("Attacker"), new Player("Victim"), "@", false, this.mc));
    }

    public int getLogSize() {
        return this.logs.size();
    }

    public int getVerticalMargin() {
        return this.verticalMargin;
    }

    public void setVerticalMargin(int verticalMargin) {
        this.verticalMargin = verticalMargin;
    }

    public int getHorizontalMargin() {
        return this.horizontalMargin;
    }

    public void setHorizontalMargin(int horizontalMargin) {
        this.horizontalMargin = horizontalMargin;
    }

    public int getShowLines() {
        return this.showLines;
    }

    public void setShowLines(int showLines) {
        this.showLines = showLines;
    }

    public int getEntryHeight() {
        return this.mc.textRenderer.fontHeight + this.verticalMargin;
    }

    public void clear() {
        this.logs.clear();
    }

    public void add(Entry entry) {
        this.logs.add(entry);
    }

    public void remove(int index) {
        this.logs.remove(index);
    }

    public void remove(Entry obj) {
        this.logs.remove(obj);
    }

    public void tick() {
        if (this.mc.currentScreen != null && this.mc.currentScreen.isPauseScreen()) return;
        this.logs.forEach(Entry::tick);
    }

    public void render(MatrixStack matrices, float x, float y, int maxShowLines, int alpha) {
        if (this.logs.isEmpty()) return;
        int showLine = Math.min(this.logs.size(), maxShowLines);
        List<Entry> showLines = this.logs.subList(this.logs.size() - showLine, this.logs.size());

        int index;
        float eY;
        Entry entry;
        for (int i = showLine; i > 0; i--) {
            index = (showLine - i);
            entry = showLines.get(index);
            eY = (i - 1) * (this.mc.textRenderer.fontHeight + this.verticalMargin) + this.verticalMargin + y;
            entry.render(matrices, x - entry.getWidth() - this.horizontalMargin, eY, alpha);
        }
    }

    public void render(MatrixStack matrices, int x, int y, int maxShowLines) {
        if (this.logs.isEmpty()) return;
        int showLine = Math.min(this.logs.size(), maxShowLines);
        List<Entry> showLines = this.logs.subList(this.logs.size() - showLine, this.logs.size());

        int index;
        int eY;
        Entry entry;
        for (int i = showLine; i > 0; i--) {
            index = (showLine - i);
            entry = showLines.get(index);
            eY = (i - 1) * (this.mc.textRenderer.fontHeight + this.verticalMargin) + this.verticalMargin + y;
            entry.render(matrices, x - entry.getWidth() - this.horizontalMargin, eY);
        }
    }

    public void render(MatrixStack matrices, int x, int y) {
        this.render(matrices, x, y, this.showLines);
    }

    public static class Entry {
        private final Player attacker;
        private final Player victim;
        private final String mark;
        private final boolean isInfection;
        private final boolean shouldHighlight;
        private int lifeTime;
        private final TextRenderer textRenderer;

        public Entry(Player attacker, Player victim, String mark, boolean isInfection, MinecraftClient mc) {
            this.attacker = attacker;
            this.victim = victim;
            this.mark = mark;
            this.isInfection = isInfection;
            this.shouldHighlight = mc.getSession().getUsername().equals(this.attacker.getName());
            this.lifeTime = 200;
            this.textRenderer = mc.textRenderer;
        }

        public boolean isShouldHighlight() {
            return this.shouldHighlight;
        }

        public void tick() {
            if (this.lifeTime >= 1) this.lifeTime--;
        }


        public void render(MatrixStack matrices, float x, float y, int alpha) {
            if (alpha > 3) {
                int offset = SEPARATOR;
                this.attacker.render(matrices, x + offset, y, alpha, this.textRenderer, this.isInfection ? 0x00AA00 : 0x00AAAA);
                offset += this.attacker.getWidth(this.textRenderer);
                this.textRenderer.drawWithShadow(matrices, " " + this.mark, x + offset, y, 0x00FFFFFF | alpha << 24);
                offset += this.textRenderer.getWidth(" " + this.mark + " ");
                this.victim.render(matrices, x + offset, y, alpha, this.textRenderer, this.isInfection ? 0x00AAAA : 0x00AA00);
                if (this.shouldHighlight)
                    this.renderHighlight(matrices, x, y - 2, alpha / 255F);
            }
        }

        public void render(MatrixStack matrices, int x, int y) {
            // -------------------------------------------------- //
            // Old
            // this.textRenderer.drawWithShadow(matrices, this.attacker.getName() + " " + this.mark + " " + this.victim.getName(), x, y, 0xFFFFFFFF);
            // this.textRenderer.drawWithShadow(matrices, this.attacker.getSkinId() + " " + this.victim.getSkinId(), x - 30, y, 0xFFFFFFFF);
            // RenderingUtils.pushTexture();
            // this.attacker.renderHead(matrices, x - 50, y, this.textRenderer.fontHeight, this.textRenderer.fontHeight);
            // this.victim.renderHead(matrices, x - 40, y, this.textRenderer.fontHeight, this.textRenderer.fontHeight);
            // RenderingUtils.popTexture();
            // -------------------------------------------------- //
            this.render(matrices, x, y, MathHelper.floor(this.getAlpha() * 255) & 0xFF);
        }

        public float getAlpha() {
            if (this.lifeTime > 20) return 1F;
            else {
                return Math.max(this.lifeTime / 20F, 0);
            }
        }

        public void renderHighlight(MatrixStack matrices, float x, float y, float alpha) {
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();

            RenderSystem.disableTexture();
            RenderSystem.enableBlend();
            Matrix4f matrix = matrices.peek().getModel();
            float right = x + this.getWidth();
            float down = y + this.textRenderer.fontHeight + KZEAddon.killLog.getVerticalMargin();

            buffer.begin(GL11.GL_LINE_LOOP, VertexFormats.POSITION_COLOR);
            buffer.vertex(matrix, x, y, 0).color(1F, 0F, 0F, alpha).next();
            buffer.vertex(matrix, x, down, 0).color(1F, 0F, 0F, alpha).next();
            buffer.vertex(matrix, right, down, 0).color(1F, 0F, 0F, alpha).next();
            buffer.vertex(matrix, right, y, 0).color(1F, 0F, 0F, alpha).next();
            tessellator.draw();

            RenderSystem.disableBlend();
            RenderSystem.enableTexture();
        }

        public boolean isInfection() {
            return this.isInfection;
        }

        public int getWidth() {
            return this.textRenderer.getWidth(" " + this.attacker.getName() + " " + this.mark + "  " + this.victim.getName()) + this.textRenderer.fontHeight * 2 + SEPARATOR * 2;
        }
    }

    public static class Player {
        private final String name;
        private final int skinId;

        public Player(String name) {
            this.name = name;

            MinecraftClient mc = MinecraftClient.getInstance();
            int fallbackTex = RenderingUtils.getGlId(MISSING_SKIN).orElse(0);
            if (mc.player == null) {
                this.skinId = fallbackTex;
            } else {
                ClientPlayNetworkHandler network = mc.player.networkHandler;
                PlayerListEntry entry = network.getPlayerListEntry(name);

                if (entry != null) {
                    this.skinId = RenderingUtils.getGlId(entry.getSkinTexture()).orElse(fallbackTex);
                } else {
                    this.skinId = fallbackTex;
                }
            }
        }

        public Player(String name, int skinId) {
            this.name = name;
            this.skinId = skinId;
        }

        public String getName() {
            return this.name;
        }

        public int getSkinId() {
            return this.skinId;
        }

        public void bindSkin() {
            RenderSystem.bindTexture(this.skinId);
        }

        public void render(MatrixStack matrices, float x, float y, int alpha, TextRenderer textRenderer, int textColor) {
            //noinspection SuspiciousNameCombination
            this.renderHead(matrices, x, y - 0.5F, textRenderer.fontHeight, textRenderer.fontHeight, alpha / 255F);
            textRenderer.drawWithShadow(matrices, this.getName(), x + textRenderer.fontHeight + SEPARATOR, y, textColor | alpha << 24);
        }

        public int getWidth(TextRenderer textRenderer) {
            return textRenderer.fontHeight + SEPARATOR + textRenderer.getWidth(this.getName());
        }

        public void renderHead(MatrixStack matrices, float x, float y, int width, int height, float alpha) {
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            Matrix4f matrix = matrices.peek().getModel();

            this.bindSkin();
            RenderSystem.enableBlend();
            buffer.begin(GL11.GL_QUADS, VertexFormats.POSITION_COLOR_TEXTURE);
            // Base
            buffer.vertex(matrix, x, y, 0).color(1F, 1F, 1F, alpha).texture(0.125F, 0.125F).next();
            buffer.vertex(matrix, x, y + height, 0).color(1F, 1F, 1F, alpha).texture(0.125F, 0.25F).next();
            buffer.vertex(matrix, x + width, y + height, 0).color(1F, 1F, 1F, alpha).texture(0.25F, 0.25F).next();
            buffer.vertex(matrix, x + width, y, 0).color(1F, 1F, 1F, alpha).texture(0.25F, 0.125F).next();
            // Hat
            buffer.vertex(matrix, x, y, 0).color(1F, 1F, 1F, alpha).texture(0.625F, 0.125F).next();
            buffer.vertex(matrix, x, y + height, 0).color(1F, 1F, 1F, alpha).texture(0.625F, 0.25F).next();
            buffer.vertex(matrix, x + width, y + height, 0).color(1F, 1F, 1F, alpha).texture(0.75F, 0.25F).next();
            buffer.vertex(matrix, x + width, y, 0).color(1F, 1F, 1F, alpha).texture(0.75F, 0.125F).next();
            tessellator.draw();

            RenderSystem.disableBlend();
        }
    }
}
