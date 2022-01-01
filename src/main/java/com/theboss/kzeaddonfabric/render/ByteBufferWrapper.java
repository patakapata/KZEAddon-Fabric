package com.theboss.kzeaddonfabric.render;

import net.minecraft.client.util.GlAllocationUtils;

import java.nio.Buffer;
import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL33.*;

/**
 * Bufferの{@link Buffer#flip() flip()}や{@link Buffer#clear() clear()}などを、
 * いちいちキャストするのめんどくさいのと、<br>
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
     * バッファーの現在のリミットを取得する
     *
     * @return 現在のリミット
     */
    public int limit() {
        return this.buffer.limit();
    }

    /**
     * バッファーの現在のポジションを返す
     *
     * @return 現在のポジション
     */
    public int position() {
        return ((Buffer) this.buffer).position();
    }

    public ByteBufferWrapper put(byte b1) {
        this.grow(1);
        this.buffer.put(b1);
        return this;
    }

    public ByteBufferWrapper put(byte b1, byte b2) {
        this.grow(2);
        this.buffer.put(b1);
        this.buffer.put(b2);
        return this;
    }

    public ByteBufferWrapper put(int i1) {
        this.grow(4);
        this.buffer.putInt(i1);
        return this;
    }

    public ByteBufferWrapper put(int i1, int i2) {
        this.grow(8);
        this.buffer.putInt(i1);
        this.buffer.putInt(i2);
        return this;
    }

    public ByteBufferWrapper put(int i1, int i2, int i3) {
        this.grow(12);
        this.buffer.putInt(i1);
        this.buffer.putInt(i2);
        this.buffer.putInt(i3);
        return this;
    }

    public ByteBufferWrapper put(float f1) {
        this.grow(4);
        this.buffer.putFloat(f1);
        return this;
    }

    public ByteBufferWrapper put(float f1, float f2) {
        this.grow(8);
        this.buffer.putFloat(f1);
        this.buffer.putFloat(f2);
        return this;
    }

    public ByteBufferWrapper put(float f1, float f2, float f3) {
        this.grow(12);
        this.buffer.putFloat(f1);
        this.buffer.putFloat(f2);
        this.buffer.putFloat(f3);
        return this;
    }

    public ByteBufferWrapper put(float f1, float f2, float f3, float f4) {
        this.grow(16);
        this.buffer.putFloat(f1);
        this.buffer.putFloat(f2);
        this.buffer.putFloat(f3);
        this.buffer.putFloat(f4);
        return this;
    }

    public ByteBufferWrapper put(double d1) {
        this.grow(4);
        this.buffer.putFloat((float) d1);
        return this;
    }

    public ByteBufferWrapper put(double d1, double d2) {
        this.grow(8);
        this.buffer.putFloat((float) d1);
        this.buffer.putFloat((float) d2);
        return this;
    }

    public ByteBufferWrapper put(double d1, double d2, double d3) {
        this.grow(12);
        this.buffer.putFloat((float) d1);
        this.buffer.putFloat((float) d2);
        this.buffer.putFloat((float) d3);
        return this;
    }

    public ByteBufferWrapper put(double d1, double d2, double d3, double d4) {
        this.grow(16);
        this.buffer.putFloat((float) d1);
        this.buffer.putFloat((float) d2);
        this.buffer.putFloat((float) d3);
        this.buffer.putFloat((float) d4);
        return this;
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
