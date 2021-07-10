package com.theboss.kzeaddonfabric.ingame;

import com.mojang.blaze3d.systems.RenderSystem;
import com.theboss.kzeaddonfabric.KZEAddon;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.packet.s2c.play.BossBarS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Matrix4f;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class KillLog {
    private final List<LogEntry> entries;
    private int count;

    public KillLog(int capacity) {
        this.entries = new ArrayList<>(capacity);
        this.count = 0;
    }

    public void add(LogEntry entry) {
        this.add(this.entries.size(), entry);
    }

    public void add(int index, LogEntry entry) {
        this.entries.add(index, entry);

        if (this.count + 1 >= 100) {
            this.entries.remove(0);
        } else {
            this.count++;
        }
    }

    public void clear() {
        this.entries.clear();
        this.count = 0;
    }

    public LogEntry get(int index) {
        return this.entries.get(index);
    }

    public int getEntryCount() {
        return this.count;
    }

    public void handlePacket(BossBarS2CPacket packet) {
        Text name = packet.getName();
        BossBarS2CPacket.Type type = packet.getType();
        if (type == BossBarS2CPacket.Type.ADD || type == BossBarS2CPacket.Type.UPDATE_NAME) {
            LogEntry entry = LogEntry.of(name);
            if (entry != null) {
                this.add(entry);
            } else {
                this.add(new LogEntry("Parse failed: " + LogEntry.textAsString(name), "*", "*", false));
            }
        }
    }

    public int maxWidth() {
        int width = 0;
        for (int i = 0; i < this.entries.size(); i++) {
            int tmp = this.entries.get(i).getWidth();
            if (i == 0) {
                width = tmp;
            } else if (tmp > width) {
                width = tmp;
            }
        }

        return width;
    }

    public void remove(LogEntry entry) {
        if (this.entries.remove(entry)) {
            this.count -= 1;
        }
    }

    public void remove(int index) {
        this.entries.remove(index);
        this.count -= 1;
    }

    public void render(MatrixStack matrices, float delta) {
        MinecraftClient client = MinecraftClient.getInstance();
        Window window = client.getWindow();
        Tessellator tessellator = Tessellator.getInstance();
        TextRenderer textRenderer = client.textRenderer;
        BufferBuilder buffer = tessellator.getBuffer();
        Matrix4f matrix = matrices.peek().getModel();
        int right = window.getScaledWidth();
        float down = 10F + this.count * textRenderer.fontHeight;
        float maxWidth = this.maxWidth() + 10F;

        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        buffer.begin(GL11.GL_QUADS, VertexFormats.POSITION_COLOR);
        buffer.vertex(matrix, right - maxWidth, 0F, 0F).color(20, 20, 20, 128).next();
        buffer.vertex(matrix, right - maxWidth, down, 0F).color(20, 20, 20, 128).next();
        buffer.vertex(matrix, right, down, 0F).color(20, 20, 20, 128).next();
        buffer.vertex(matrix, right, 0F, 0F).color(20, 20, 20, 128).next();
        tessellator.draw();
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();

        int i = 0;
        for (LogEntry entry : this.entries) {
            int x = right - entry.getWidth() - 5;
            int y = 5 + i++ * textRenderer.fontHeight;
            entry.render(matrices, x, y, textRenderer);
        }
    }

    public static class LogEntry {
        public static final Identifier MISSING_SKIN = new Identifier("kzeaddon-fabric", "textures/gui/missing_skin.png");
        protected static final int MARGIN = 3;

        protected String attacker;
        protected String victim;
        protected String mark;
        protected boolean isInfection;
        protected boolean shouldHighlight;
        protected int spaceWidth;

        protected Identifier victimSkin;
        protected Identifier attackerSkin;

        public static LogEntry of(Text bossBarName) {
            String nameString = textAsString(bossBarName);
            if (nameString.length() < 80) return null;
            boolean isInfection = nameString.startsWith("§3");
            String[] elements = nameString.substring(79).split(" ");
            if (elements.length < 3) return null;
            String victim = elements[0];
            String mark = elements[1];
            String attacker = elements[2];

            return new LogEntry(victim, mark, attacker, isInfection);
        }

        public static String textAsString(Text text) {
            StringBuilder builder = new StringBuilder();
            text.visit(asString -> {
                builder.append(asString);
                return Optional.empty();
            });
            return builder.toString();
        }

        public LogEntry(String victim, String mark, String attacker, boolean isInfection) {
            this.victim = victim;
            this.mark = mark;
            this.attacker = attacker;
            this.isInfection = isInfection;
            this.spaceWidth = MinecraftClient.getInstance().textRenderer.getWidth(" ");

            this.victimSkin = null;
            this.attackerSkin = null;

            this.highlightCheck();
        }

        public void bindAttackerSkin() {
            MinecraftClient client = MinecraftClient.getInstance();
            TextureManager texManager = client.getTextureManager();

            if (this.attackerSkin == null) {
                ClientPlayNetworkHandler networkHandler = MinecraftClient.getInstance().player.networkHandler;
                PlayerListEntry entry = networkHandler.getPlayerListEntry(this.attacker.substring(2));
                if (entry == null) {
                    this.attackerSkin = MISSING_SKIN;
                } else if (entry.getProfile().getName().equals(this.attacker.substring(2))) {
                    this.attackerSkin = entry.getSkinTexture();
                } else {
                    this.attackerSkin = MISSING_SKIN;
                }
            }
            texManager.bindTexture(this.attackerSkin);
        }

        public void bindVictimSkin() {
            MinecraftClient client = MinecraftClient.getInstance();
            TextureManager texManager = client.getTextureManager();

            if (this.victimSkin == null) {
                ClientPlayNetworkHandler networkHandler = MinecraftClient.getInstance().player.networkHandler;
                PlayerListEntry entry = networkHandler.getPlayerListEntry(this.victim.substring(2));
                if (entry == null) {
                    this.victimSkin = MISSING_SKIN;
                } else if (entry.getProfile().getName().equals(this.victim.substring(2))) {
                    this.victimSkin = entry.getSkinTexture();
                } else {
                    this.victimSkin = MISSING_SKIN;
                }
            }
            texManager.bindTexture(this.victimSkin);
        }

        public void drawAttackerHead(MatrixStack matrices, int x, int y) {
            this.bindAttackerSkin();
            DrawableHelper.drawTexture(matrices, x, y, 8, 8, 8, 8, 8, 8, 64, 64);
            DrawableHelper.drawTexture(matrices, x, y, 8, 8, 40, 8, 8, 8, 64, 64);
        }

        public void drawHighlight(MatrixStack matrices, int x, int y) {
            MinecraftClient client = MinecraftClient.getInstance();
            TextRenderer textRenderer = client.textRenderer;
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            Matrix4f matrix = matrices.peek().getModel();
            int width = this.getWidth();

            buffer.begin(GL11.GL_LINE_LOOP, VertexFormats.POSITION_COLOR);
            buffer.vertex(matrix, x, y, 0F).color(255, 0, 0, 255).next();
            buffer.vertex(matrix, x, y + textRenderer.fontHeight, 0F).color(255, 0, 0, 255).next();
            buffer.vertex(matrix, x + width, y + textRenderer.fontHeight, 0F).color(255, 0, 0, 255).next();
            buffer.vertex(matrix, x + width, y, 0F).color(255, 0, 0, 255).next();
            tessellator.draw();
        }

        public void drawVictimHead(MatrixStack matrices, int x, int y) {
            this.bindVictimSkin();
            DrawableHelper.drawTexture(matrices, x, y, 8, 8, 8, 8, 8, 8, 64, 64);
            DrawableHelper.drawTexture(matrices, x, y, 8, 8, 40, 8, 8, 8, 64, 64);
        }

        public String getAttacker() {
            return this.attacker;
        }

        public void setAttacker(String attacker) {
            this.attacker = attacker;

            this.highlightCheck();
        }

        public int getAttackerWidth(TextRenderer textRenderer) {
            return textRenderer.fontHeight + MARGIN + textRenderer.getWidth(this.attacker);
        }

        public String getMark() {
            return this.mark;
        }

        public int getMarkWidth(TextRenderer textRenderer) {
            return textRenderer.getWidth(this.mark);
        }

        public String getVictim() {
            return this.victim;
        }

        public void setVictim(String victim) {
            this.victim = victim;
        }

        public int getVictimWidth(TextRenderer textRenderer) {
            return textRenderer.fontHeight + MARGIN + textRenderer.getWidth(this.victim);
        }

        public int getWidth() {
            TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
            return textRenderer.getWidth(this.victim + " " + this.mark + " " + this.attacker) + (textRenderer.fontHeight + MARGIN) * 2;
        }

        protected void highlightCheck() {
            this.shouldHighlight = this.attacker.substring(2).equals(textAsString(MinecraftClient.getInstance().player.getName()));
        }

        public boolean isInfectionLog() {
            return this.isInfection;
        }

        public boolean isShouldHighlight() {
            return this.shouldHighlight && KZEAddon.Options.isShouldHighlightMyKill();
        }

        public void render(MatrixStack matrices, int x, int y, TextRenderer textRenderer) {
            this.renderVictim(matrices, x, y, textRenderer);
            this.renderMark(matrices, x + this.getVictimWidth(textRenderer) + this.spaceWidth, y, textRenderer);
            this.renderAttacker(matrices, x + this.getVictimWidth(textRenderer) + this.getMarkWidth(textRenderer) + this.spaceWidth * 2, y, textRenderer);
            if (this.isShouldHighlight()) {
                this.drawHighlight(matrices, x, y);
            }
        }

        public void renderAttacker(MatrixStack matrices, int x, int y, TextRenderer textRenderer) {
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            this.drawAttackerHead(matrices, x, y);
            textRenderer.drawWithShadow(matrices, this.attacker, x + textRenderer.fontHeight + MARGIN, y, 0xFFFFFF);
        }

        public void renderMark(MatrixStack matrices, float x, float y, TextRenderer textRenderer) {
            textRenderer.drawWithShadow(matrices, this.mark, x, y, 0xFFFFFF);
        }

        public void renderVictim(MatrixStack matrices, int x, int y, TextRenderer textRenderer) {
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            this.drawVictimHead(matrices, x, y);
            textRenderer.drawWithShadow(matrices, this.victim, x + textRenderer.fontHeight + MARGIN, y, 0xFFFFFF);
        }

        @Override
        public String toString() {
            return "Victim: " + this.victim + ", Mark: " + this.mark + ", Attacker: " + this.attacker;
        }
    }
}
