package com.theboss.kzeaddonfabric;

import com.mojang.blaze3d.systems.RenderSystem;
import com.theboss.kzeaddonfabric.render.HUD;
import com.theboss.kzeaddonfabric.screen.KZEAddonLogScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.Matrix4f;
import org.lwjgl.opengl.GL11;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class KZEAddonLog implements HUD {
    private final List<Entry> history;
    private MinecraftClient mc;
    private TextRenderer textRenderer;
    private int maxHistorySize;
    private int x;
    private int y;
    private int normalShowLines;
    private boolean showTime;

    protected static int getMaxWidth(TextRenderer textRenderer, List<Entry> entries, boolean showTime) {
        int max = -1;
        for (Entry entry : entries) {
            int width = entry.getWidth(textRenderer, showTime);
            if (width > max) max = width;
        }

        return max;
    }

    public KZEAddonLog(int maxHistorySize, int x, int y, int normalShowLines) {
        this(maxHistorySize, x, y, normalShowLines, false);
    }

    public KZEAddonLog(int maxHistorySize, int x, int y, int normalShowLines, boolean showTime) {
        this.history = new ArrayList<>();
        this.maxHistorySize = maxHistorySize;
        this.x = x;
        this.y = y;
        this.normalShowLines = normalShowLines;
        this.showTime = showTime;
    }

    /**
     * ログにメッセージを追加する
     *
     * @param msg メッセージ
     */
    public void add(String msg) {
        this.history.add(new Entry(Text.of(msg)));
        this.checkBounds();
    }

    /**
     * ログにメッセージを追加する
     *
     * @param msg メッセージ
     */
    public void add(Text msg) {
        this.history.add(new Entry(msg));
        this.checkBounds();
    }

    /**
     * {@link #getHistorySize() エントリ数} が {@link #getMaxHistorySize() ログの最大サイズ} を超えていた場合
     * <p>
     * 最大サイズ以下になるまで古いログから削除する
     */
    protected void checkBounds() {
        int over = this.history.size() - this.maxHistorySize;
        if (over > 0) {
            this.history.subList(0, over).clear();
        }
    }

    /**
     * ログを全消去する
     */
    public void clear() {
        this.history.clear();
    }

    /**
     * ログにエラーレベルのメッセージを追加する
     *
     * @param msg メッセージ
     */
    public void error(String msg) {
        this.history.add(new Entry(Text.of(msg), 0xFF5555));
        this.checkBounds();
    }

    /**
     * ログにエラーレベルのメッセージを追加する
     *
     * @param msg メッセージ
     */
    public void error(Text msg) {
        this.history.add(new Entry(msg, 0xFF5555));
        this.checkBounds();
    }

    /**
     * 指定したインデックスに対応するエントリのメッセージを取得する
     *
     * @param index エントリのインデックス
     * @return エントリのメッセージ
     */
    public Text get(int index) {
        return this.history.get(index).getMsg();
    }

    public List<Entry> getEntries() {
        return this.history;
    }

    /**
     * 現在ログに入っているエントリの数を取得する
     *
     * @return エントリ数
     */
    public int getHistorySize() {
        return this.history.size();
    }

    /**
     * ログの最大サイズを取得する
     *
     * @return ログの最大サイズ
     */
    public int getMaxHistorySize() {
        return this.maxHistorySize;
    }

    /**
     * ログの最大サイズを指定する
     *
     * @param maxHistorySize ログの最大サイズ
     */
    public void setMaxHistorySize(int maxHistorySize) {
        this.maxHistorySize = maxHistorySize;
        this.checkBounds();
    }

    /**
     * 通常時のログの最大表示数を取得する
     *
     * @return ログの最大表示数
     */
    public int getNormalShowLines() {
        return this.normalShowLines;
    }

    /**
     * 通常時のログの最大表示数を設定する
     *
     * @param normalShowLines ログの最大表示数
     */
    public void setNormalShowLines(int normalShowLines) {
        this.normalShowLines = normalShowLines;
    }

    /**
     * ログのX座標を取得する
     *
     * @return ログのX座標
     */
    public int getX() {
        return this.x;
    }

    /**
     * ログのX座標を設定する
     *
     * @param x ログのX座標
     */
    public void setX(int x) {
        this.x = x;
    }

    /**
     * ログのY座標を取得する
     *
     * @return ログのY座標
     */
    public int getY() {
        return this.y;
    }

    /**
     * ログのY座標を設定する
     *
     * @param y ログのY座標
     */
    public void setY(int y) {
        this.y = y;
    }

    /**
     * 引数 from から 引数 to に含まれるエントリを取得する
     *
     * @param from 開始インデックス
     * @param to   終了インデックス
     * @return 含まれているエントリ一覧
     */
    public List<Entry> inRangeEntries(int from, int to) {
        if (this.history.isEmpty()) return Collections.emptyList();
        if (from < 0) from = 0;
        if (from > this.history.size() - 1) to = this.history.size() - 1;
        return this.history.subList(from, to);
    }

    /**
     * ログに情報レベルのメッセージを追加する
     *
     * @param msg 追加するメッセージ
     */
    public void info(String msg) {
        this.history.add(new Entry(Text.of(msg), 0xAAAAAA));
        this.checkBounds();
    }

    /**
     * ログに情報レベルのメッセージを追加する
     *
     * @param msg 追加するメッセージ
     */
    public void info(Text msg) {
        this.history.add(new Entry(msg, 0xAAAAAA));
        this.checkBounds();
    }

    /**
     * ログを初期化する
     */
    @Override
    public void init() {
        this.mc = MinecraftClient.getInstance();
        this.textRenderer = this.mc.textRenderer;
    }

    /**
     * ログがタイムスタンプを表示しているかを取得する
     *
     * @return 表示している場合 true それ以外の場合 false
     */
    public boolean isShowTime() {
        return this.showTime;
    }

    /**
     * ログがタイムスタンプを表示するかを設定する
     *
     * @param showTime true の場合 表示 それ以外の場合 非表示
     */
    public void setShowTime(boolean showTime) {
        this.showTime = showTime;
    }

    public void openLogScreen() {
        MinecraftClient.getInstance().openScreen(new KZEAddonLogScreen());
        KZEAddon.MOD_LOG.add(Text.of("Log screen opened"));
    }

    /**
     * 指定されたインデックスの位置にあるログを削除する
     *
     * @param index 削除する対象のインデックス
     */
    public void remove(int index) {
        this.history.remove(index);
    }

    /**
     * ログを描画する
     *
     * @param matrices マトリックススタック
     */
    @Override
    public void render(MatrixStack matrices) {
        if (this.mc.currentScreen instanceof KZEAddonLogScreen) return;
        if (this.textRenderer == null) this.textRenderer = this.mc.textRenderer;
        matrices.push();
        matrices.translate(this.x, this.y, 0);
        int lastIndex = this.history.size() - 1;
        int showLines = Math.min(this.normalShowLines - 1, lastIndex);
        RenderSystem.enableBlend();
        List<Entry> entries = this.inRangeEntries(lastIndex - 9, lastIndex + 1);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        buffer.begin(GL11.GL_QUADS, VertexFormats.POSITION_COLOR);
        for (int i = showLines; i >= 0; i--) {
            Entry entry = entries.get(i);
            entry.renderBackground(matrices, buffer, this.textRenderer, this.x, this.y + (showLines - i) * this.textRenderer.fontHeight, this.showTime);
        }
        RenderSystem.disableTexture();
        tessellator.draw();
        RenderSystem.enableTexture();

        for (int i = showLines; i >= 0; i--) {
            Entry entry = entries.get(i);
            entry.render(matrices, this.textRenderer, this.x, this.y + (showLines - i) * this.textRenderer.fontHeight, this.showTime);
        }
        matrices.pop();
    }

    @Override
    public void tick() {
        if (this.mc.currentScreen != null && this.mc.currentScreen.isPauseScreen()) return;
        this.history.forEach(Entry::tick);
    }

    /**
     * ログに警告レベルのメッセージを追加する
     *
     * @param msg 追加するメッセージ
     */
    public void warn(String msg) {
        this.history.add(new Entry(Text.of(msg), 0xFFFF55));
        this.checkBounds();
    }

    /**
     * ログに警告レベルのメッセージを追加する
     *
     * @param msg 追加するメッセージ
     */
    public void warn(Text msg) {
        this.history.add(new Entry(msg, 0xFFFF55));
        this.checkBounds();
    }

    /**
     * ログの中身
     */
    public static class Entry {
        /**
         * ログの文字の実体
         */
        protected Text msg;
        /**
         * ログが追加されてからの経過時間(チック単位)
         */
        protected int age;
        /**
         * メッセージの色(αは含まない) 0xRRGGBB
         */
        protected int color;
        /**
         * ログが追加された時間のテキスト
         */
        protected Text timeText;

        public static String now() {
            LocalTime now = LocalTime.now();
            return String.format("[%2d:%2d:%2d] ", now.getHour(), now.getMinute(), now.getSecond());
        }

        /**
         * 全て指定するコンストラクタ
         *
         * @param msg      表示されるメッセージ
         * @param age      ログが追加されてからの経過チック
         * @param color    ログの色
         * @param timeText タイムスタンプテキスト
         */
        public Entry(Text msg, int age, int color, Text timeText) {
            this.msg = msg;
            this.age = age;
            this.color = color & 0xFFFFFF;
            this.timeText = timeText;
        }

        /**
         * テキストとタイムスタンプと色を指定するコンストラクタ
         *
         * @param msg   表示されるメッセージ
         * @param age   ログが追加されてからの経過時間(チック単位)
         * @param color ログの色
         */
        public Entry(Text msg, int age, int color) {
            this.msg = msg;
            this.age = age;
            this.color = color & 0xFFFFFF;
            this.timeText = Text.of(now());
        }

        /**
         * テキストとタイムスタンプを指定するコンストラクタ
         * <p>
         * 経過時間は {@code 0}
         *
         * @param msg   表示されるメッセージ
         * @param color ログの色
         */
        public Entry(Text msg, int color) {
            this.msg = msg;
            this.age = 0;
            this.color = color & 0xFFFFFF;
            this.timeText = Text.of(now());
        }

        /**
         * テキストを指定するコンストラクタ
         * <p>
         * 色は {@code 0xFFFFFF}
         * <p>
         * 経過時間は {@code 0}
         *
         * @param msg 表示されるメッセージ
         */
        public Entry(Text msg) {
            this.msg = msg;
            this.age = 0;
            this.color = 0xFFFFFF;
            this.timeText = Text.of(now());
        }

        /**
         * このエントリ経過チックを取得する
         *
         * @return エントリが追加されてからの経過時間
         */
        public int getAge() {
            return this.age;
        }

        /**
         * 不透明度を取得します
         *
         * @return 0~255の間の値
         */
        protected int getAlpha() {
            if (this.age > 200) return 0;
            else if (this.age < 180) return 255;
            else return (int) ((200 - this.age) / 20F * 255);
        }

        /**
         * エントリの文字色を取得する
         *
         * @return 0xRRGGBB
         */
        public int getColor() {
            return this.color;
        }

        /**
         * エントリの文字色を指定する
         *
         * @param color 0xRRGGBB
         */
        public void setColor(int color) {
            this.color = color;
        }

        /**
         * メッセージを取得する
         *
         * @return このエントリのメッセージ
         */
        public Text getMsg() {
            return this.msg;
        }

        /**
         * メッセージを設定する
         *
         * @param msg 設定するメッセージ
         */
        public void setMsg(Text msg) {
            this.msg = msg;
        }

        public Text getTimeText() {
            return this.timeText;
        }

        /**
         * このエントリの描画時の幅を取得する
         *
         * @param textRenderer 文字の幅取得用
         * @return エントリの幅
         */
        public int getWidth(TextRenderer textRenderer, boolean showTime) {
            return textRenderer.getWidth(this.msg) + (showTime ? textRenderer.getWidth(this.timeText) : 0) + 5;
        }

        /**
         * このエントリを描画する
         *
         * @param matrices     マトリックススタック
         * @param textRenderer テキストレンダラ
         * @param x            X座標のオフセット
         * @param y            Y座標のオフセット
         */
        public void render(MatrixStack matrices, TextRenderer textRenderer, float x, float y, boolean showTime) {
            int alpha = this.getAlpha();
            if (alpha > 3) {
                if (showTime) textRenderer.drawWithShadow(matrices, this.timeText, x, y, this.color | alpha << 24);
                textRenderer.drawWithShadow(matrices, this.msg, showTime ? x + textRenderer.getWidth(this.timeText) : x, y, this.color | alpha << 24);
            }
        }

        public void renderAsOpaque(MatrixStack matrices, TextRenderer textRenderer, float x, float y, boolean showTime) {
            if (showTime) textRenderer.drawWithShadow(matrices, this.timeText, x, y, this.color | 0xFF000000);
            textRenderer.drawWithShadow(matrices, this.msg, showTime ? x + textRenderer.getWidth(this.timeText) : x, y, this.color | 0xFF000000);
        }

        /**
         * {@link VertexFormats#POSITION_COLOR} で開始されたバッファーに背景を書き込みます
         *
         * @param matrices     マトリックススタック
         * @param buffer       書き込む対象のバッファー
         * @param textRenderer 文字幅取得用のテキストレンダラ
         * @param x            X座標のオフセット
         * @param y            Y座標のオフセット
         * @param showTime     タイムスタンプの幅を考慮するか
         */
        public void renderBackground(MatrixStack matrices, BufferBuilder buffer, TextRenderer textRenderer, float x, float y, boolean showTime) {
            float alpha = this.getAlpha() / 511F;
            int width = this.getWidth(textRenderer, showTime);
            int height = textRenderer.fontHeight;
            Matrix4f matrix = matrices.peek().getModel();

            buffer.vertex(matrix, x, y, 0).color(0, 0, 0, alpha).next();
            buffer.vertex(matrix, x, y + height, 0).color(0, 0, 0, alpha).next();
            buffer.vertex(matrix, x + width, y + height, 0).color(0, 0, 0, alpha).next();
            buffer.vertex(matrix, x + width, y, 0).color(0, 0, 0, alpha).next();
        }

        /**
         * このエントリの経過チックを設定する
         *
         * @param age 新しい経過時間
         */
        public void setTimestamp(int age) {
            this.age = age;
        }

        public void tick() {
            this.age++;
        }
    }
}
