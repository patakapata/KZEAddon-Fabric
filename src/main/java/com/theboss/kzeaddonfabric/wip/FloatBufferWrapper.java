package com.theboss.kzeaddonfabric.wip;

import net.minecraft.client.util.GlAllocationUtils;

import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.util.function.Consumer;

import static com.theboss.kzeaddonfabric.KZEAddon.warn;

public class FloatBufferWrapper {
    public FloatBuffer buffer;
    protected Consumer<FloatBufferWrapper> uploadTask;

    /**
     * origin を threshold で丸める
     *
     * @param origin    丸める値
     * @param threshold 閾値
     * @return 丸められた値
     */
    public static int round(int origin, int threshold) {
        if (threshold < 0 || origin % threshold == 0) return origin;
        int factor = origin / threshold;
        return threshold * (factor + 1);
    }

    /**
     * 新バッファサイズを出す際に使う
     * 2MB単位で大きくなる
     *
     * @param size 丸め前の値
     * @return 2MBで丸められた値
     */
    private static int roundBufferSize(int size) {
        // Round by 2 MB
        return round(size, 2097152);
    }

    /**
     * 初期容量とアップロードタスクを指定するコンストラクタ
     *
     * @param initialCapacity 初期容量
     * @param uploadTask      アップロードタスク
     */
    public FloatBufferWrapper(int initialCapacity, Consumer<FloatBufferWrapper> uploadTask) {
        this.buffer = GlAllocationUtils.allocateByteBuffer(initialCapacity).asFloatBuffer();
        this.uploadTask = uploadTask;
    }

    /**
     * 初期容量を指定するコンストラクタ
     *
     * @param initialCapacity 初期容量
     */
    public FloatBufferWrapper(int initialCapacity) {
        this(initialCapacity, unused -> {});
    }

    /**
     * {@link Buffer#capacity()}へのショートカット
     *
     * @return {@link Buffer#capacity()}
     */
    public int capacity() {
        return this.buffer.capacity();
    }

    /**
     * {@link Buffer#clear()}へのショートカット
     */
    @SuppressWarnings("RedundantCast")
    public void clear() {
        ((Buffer) this.buffer).clear();
    }

    /**
     * {@link Buffer#flip()}へのショートカット
     */
    @SuppressWarnings("RedundantCast")
    public void flip() {
        ((Buffer) this.buffer).flip();
    }

    /**
     * 指定サイズが入らなかった場合、
     * バッファを少し大きくして再生成
     *
     * @param expRemain バイト単位
     */
    public void grow(int expRemain) {
        int remainSize = this.buffer.remaining();
        if (remainSize < expRemain) {
            int roundedSize = roundBufferSize(this.size() + expRemain);
            warn("Buffer is growing " + this.size() + " -> " + roundedSize);
            FloatBuffer newBuffer = GlAllocationUtils.allocateByteBuffer(roundedSize).asFloatBuffer();
            this.flip();
            newBuffer.put(this.buffer);

            this.buffer = newBuffer;
            this.uploadTask.accept(this);
        }
    }

    /**
     * {@link Buffer#position()}へのショートカット
     *
     * @return {@link Buffer#position()}
     */
    @SuppressWarnings("RedundantCast")
    public int position() {
        return ((Buffer) this.buffer).position();
    }

    /**
     * {@link FloatBuffer#put(float[])}へのショートカット
     *
     * @param array 追加する値
     */
    public void put(float[] array) {
        this.buffer.put(array);
    }

    /**
     * {@link FloatBuffer#put(float)}へのショートカット
     *
     * @param v 追加する値
     */
    public void put(float v) {
        this.buffer.put(v);
    }

    /**
     * バッファのサイズが変わった時に
     * GPUにアップロードする用
     * (他に使うのもあり)
     *
     * @param uploadTask Do something!
     */
    public void setUploadTask(Consumer<FloatBufferWrapper> uploadTask) {
        this.uploadTask = uploadTask;
    }

    /**
     * バイト単位でバッファの容量を返します
     *
     * @return バイト単位でのこのバッファの容量
     */
    public int size() {
        return this.buffer.capacity() * 4;
    }
}
