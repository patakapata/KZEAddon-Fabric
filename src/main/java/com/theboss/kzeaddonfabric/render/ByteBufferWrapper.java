package com.theboss.kzeaddonfabric.render;

import net.minecraft.client.util.GlAllocationUtils;

import java.nio.Buffer;
import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL33.*;

/**
 * Bufferの{@link Buffer#flip() flip()}や{@link Buffer#clear() clear()}などを、
 * いちいちキャストするのめんどくさいのと、<p>
 * 可変サイズのバッファが欲しいという所から作られた
 * {@link ByteBuffer} のラッパークラスです
 */
@SuppressWarnings("RedundantCast")
public class ByteBufferWrapper {
    public ByteBuffer buffer;
    private Runnable onSizeChange;

    /**
     * x を y で丸めた値を返す
     *
     * @param x 丸め対象
     * @param y 閾値
     * @return 丸められた値
     */
    public static int round(int x, int y) {
        if (x % y == 0) return x;
        int factor = x / y;

        return y * (factor + 1);
    }

    /**
     * newSize を 2097152(2MB)で丸めた値を返す
     *
     * @param newSize 丸め対象
     * @return 丸められた値
     */
    public static int roundBufferSize(int newSize) {
        return ByteBufferWrapper.round(newSize, 2097152);
    }

    /**
     * バッファーの初期容量とサイズ変更時のタスクを指定する
     *
     * @param initialCapacity バッファーの初期容量
     * @param onSizeChange    サイズ変更のタスク
     */
    public ByteBufferWrapper(int initialCapacity, Runnable onSizeChange) {
        this.buffer = GlAllocationUtils.allocateByteBuffer(initialCapacity);
        this.onSizeChange = onSizeChange;
    }

    /**
     * バッファーの初期容量を指定する
     *
     * @param initialCapacity バッファーの初期容量
     */
    public ByteBufferWrapper(int initialCapacity) {
        this(initialCapacity, () -> {});
    }

    /**
     * バッファーの容量を取得する
     * {@link Buffer#capacity() これ} と同じ
     *
     * @return バッファーの容量
     */
    public int capacity() {
        return ((Buffer) this.buffer).capacity();
    }

    /**
     * バッファーのポジションを0に戻し、リミットとマークを解除する <p>
     * {@link Buffer#clear() これ} と同じ
     */
    public void clear() {
        ((Buffer) this.buffer).clear();
    }

    /**
     * バッファーをフリップする <p>
     * {@link Buffer#flip() これ} と同じ
     */
    public void flip() {
        ((Buffer) this.buffer).flip();
    }

    /**
     * バッファーの残容量が expRemainSize を超えていた場合、<p>
     * 残容量 + expRemainSize の大きさの新しいバッファを生成する
     *
     * @param expRemainSize 期待される残容量
     */
    public void grow(int expRemainSize) {
        int remainSize = this.buffer.remaining();
        if (remainSize < expRemainSize) {
            int newSize = ByteBufferWrapper.roundBufferSize(this.buffer.capacity() + expRemainSize);
            ByteBuffer newBuffer = GlAllocationUtils.allocateByteBuffer(newSize);
            this.buffer.flip();
            newBuffer.put(this.buffer);

            this.buffer = newBuffer;
            this.onSizeChange.run();
        }
    }

    /**
     * バッファーの現在のポジションを返す
     *
     * @return 現在のポジション
     */
    public int position() {
        return ((Buffer) this.buffer).position();
    }

    /**
     * バッファーの現在のリミットを取得する
     *
     * @return 現在のリミット
     */
    public int limit() {
        return this.buffer.limit();
    }

    /**
     * 引数をバッファーに追加する
     *
     * @param v 追加する値
     */
    public void put(byte v) {
        this.buffer.put(v);
    }

    /**
     * 引数をバッファーに追加する
     *
     * @param v1 追加する値
     * @param v2 追加する値
     * @param v3 追加する値
     */
    public void put(byte v1, byte v2, byte v3) {
        this.buffer.put(v1);
        this.buffer.put(v2);
        this.buffer.put(v3);
    }

    /**
     * 引数をバッファーに追加する
     *
     * @param v1 追加する値
     * @param v2 追加する値
     * @param v3 追加する値
     * @param v4 追加する値
     */
    public void put(byte v1, byte v2, byte v3, byte v4) {
        this.buffer.put(v1);
        this.buffer.put(v2);
        this.buffer.put(v3);
        this.buffer.put(v4);
    }

    /**
     * 引数をバッファーに追加する
     *
     * @param v 追加する値
     */
    public void put(float v) {
        this.buffer.putFloat(v);
    }

    /**
     * 引数をバッファーに追加する
     *
     * @param v1 追加する値
     * @param v2 追加する値
     */
    public void put(float v1, float v2) {
        this.buffer.putFloat(v1);
        this.buffer.putFloat(v2);
    }

    /**
     * 引数をバッファーに追加する
     *
     * @param v1 追加する値
     * @param v2 追加する値
     * @param v3 追加する値
     */
    public void put(float v1, float v2, float v3) {
        this.buffer.putFloat(v1);
        this.buffer.putFloat(v2);
        this.buffer.putFloat(v3);
    }

    /**
     * 引数をバッファーに追加する
     *
     * @param v1 追加する値
     * @param v2 追加する値
     * @param v3 追加する値
     * @param v4 追加する値
     */
    public void put(float v1, float v2, float v3, float v4) {
        this.buffer.putFloat(v1);
        this.buffer.putFloat(v2);
        this.buffer.putFloat(v3);
        this.buffer.putFloat(v4);
    }

    /**
     * 引数を{@link Float}にキャストして、
     * バッファーに追加する
     *
     * @param v 追加する値
     */
    public void put(double v) {
        this.buffer.putFloat((float) v);
    }


    /**
     * 引数を{@link Float}にキャストして、
     * バッファーに追加する
     *
     * @param v1 追加する値
     * @param v2 追加する値
     */
    public void put(double v1, double v2) {
        this.buffer.putFloat((float) v1);
        this.buffer.putFloat((float) v2);
    }

    /**
     * 引数を{@link Float}にキャストして、
     * バッファーに追加する
     *
     * @param v1 追加する値
     * @param v2 追加する値
     * @param v3 追加する値
     */
    public void put(double v1, double v2, double v3) {
        this.buffer.putFloat((float) v1);
        this.buffer.putFloat((float) v2);
        this.buffer.putFloat((float) v3);
    }

    /**
     * バッファーのサイズが変わった時に実行されるタスクを変更する
     *
     * @param onSizeChange 実行するタスク
     */
    public void setOnSizeChange(Runnable onSizeChange) {
        this.onSizeChange = onSizeChange;
    }

    /**
     * VBOのサイズを更新し、内容物をすべて破棄する(?)
     *
     * @param vbo 対象の VBO ID
     */
    public void updateSizeOnVBO(int vbo) {
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, this.buffer.capacity(), GL_DYNAMIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    /**
     * VBOのサイズ更新を行わず、
     * バッファーのデータのみを転送する
     *
     * @param vbo 対象の VBO ID
     */
    public void updateDataOnVBO(int vbo) {
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferSubData(GL_ARRAY_BUFFER, 0, this.buffer);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    /**
     * VBOのサイズを更新し、データを転送する
     * <p>
     * ({@link #updateSizeOnVBO(int) サイズ更新}をした後に{@link #updateDataOnVBO(int) データ転送}を実行するのと同じ)
     *
     * @param vbo 対象の VBO ID
     */
    public void uploadToVBO(int vbo) {
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, this.buffer, GL_DYNAMIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }
}
