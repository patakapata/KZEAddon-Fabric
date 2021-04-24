package com.theboss.kzeaddonfabric.render;

import net.minecraft.client.util.GlAllocationUtils;
import net.minecraft.client.util.math.Vector4f;
import net.minecraft.util.math.Matrix4f;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;

public class VBOWrapper {
    private static final Logger LOGGER = LogManager.getLogger("VBOWrapper2");

    private int STRIDE;
    private int mode;
    private VertexFormats.VertexFormat format;
    private ByteBuffer buffer;
    private int vboId;
    private boolean isBuilding;
    private boolean sizeChanged;

    public void init(int initialCapacity) {
        this.buffer = GlAllocationUtils.allocateByteBuffer(initialCapacity);
        this.vboId = glGenBuffers();

        bind();
        glBufferData(GL_ARRAY_BUFFER, this.buffer, GL_DYNAMIC_DRAW);
        unbind();
    }

    public void destroy() {
        if (glIsBuffer(this.vboId))
            glDeleteBuffers(this.vboId);
    }

    ByteBuffer getBuffer() {
        return this.buffer;
    }

    public void bind() {
        glBindBuffer(GL_ARRAY_BUFFER, this.vboId);
    }

    public void unbind() {
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    public void grow(int count) {
        int remaining = this.buffer.remaining();
        if (count > remaining) {
            int cap = this.buffer.capacity();
            int newSize = cap + roundBufferSize(count);
            ByteBuffer byteBuff = GlAllocationUtils.allocateByteBuffer(newSize);
            this.buffer.flip();
            byteBuff.put(this.buffer);
            this.buffer = byteBuff;
            this.sizeChanged = true;
        }

    }

    private int roundBufferSize(int amount) {
        int i = 2097152; // 2MB = (2 * 1024 * 1024 = 2097152)KB
        if (amount == 0) {
            return i;
        } else {
            if (amount < 0) {
                i *= -1;
            }

            int j = amount % i;
            return j == 0 ? amount : amount + i - j;
        }
    }

    public void begin(int mode, VertexFormats.VertexFormat format) {
        if (this.isBuilding)
            throw new IllegalStateException("Already building!");
        this.buffer.clear();
        this.isBuilding = true;
        this.mode = mode;
        this.format = format;
        this.STRIDE = this.format.size();
    }

    public void end() {
        if (!this.isBuilding)
            throw new IllegalStateException("Not started building!");
        this.buffer.flip();
        this.isBuilding = false;
    }

    public void upload() {
        bind();

        if (this.sizeChanged) {
            glBufferData(GL_ARRAY_BUFFER, this.buffer.capacity(), GL_DYNAMIC_DRAW);
            this.sizeChanged = false;
        }
        glBufferSubData(GL_ARRAY_BUFFER, 0, this.buffer);

        unbind();
    }

    public int getBuffCapacity() {
        return this.buffer.capacity();
    }

    public int getBuffLimit() {
        return this.buffer.limit();
    }

    public void prepare() {
        int offset = 0;
        for (VertexFormats.VertexFormat.Element element : this.format.elements()) {
            glEnableClientState(element.id());
            switch (element.id()) {
                case GL_VERTEX_ARRAY:
                    glVertexPointer(element.count(), element.type().getGLType(), this.format.size(), offset);
                    break;
                case GL_COLOR_ARRAY:
                    glColorPointer(element.count(), element.type().getGLType(), this.format.size(), offset);
                    break;
                case GL_TEXTURE_COORD_ARRAY:
                    glTexCoordPointer(element.count(), element.type().getGLType(), this.format.size(), offset);
                    break;
            }
            offset += element.size();
        }
    }

    public void circumstance() {
        for (VertexFormats.VertexFormat.Element element : this.format.elements()) {
            glDisableClientState(element.id());
        }
    }

    public void draw() {
        bind();
        this.prepare();
        glDrawArrays(this.mode, 0, this.buffer.limit() / this.STRIDE);
        this.circumstance();
        unbind();
    }

    public void vertex(Matrix4f matrix, float x, float y, float z) {
        grow(12);
        Vector4f vec = new Vector4f(x, y, z, 1F);
        vec.transform(matrix);

        this.buffer.putFloat(vec.getX());
        this.buffer.putFloat(vec.getY());
        this.buffer.putFloat(vec.getZ());
    }

    public void vertex(float x, float y, float z) {
        grow(12);

        this.buffer.putFloat(x);
        this.buffer.putFloat(y);
        this.buffer.putFloat(z);
    }

    public void vertex(double x, double y, double z) {
        this.vertex((float) x, (float) y, (float) z);
    }

    public void vertex(Matrix4f matrix, double x, double y, double z) {
        vertex(matrix, (float) x, (float) y, (float) z);
    }

    public void color(int red, int green, int blue, int alpha) {
        grow(4);

        this.buffer.put((byte) red);
        this.buffer.put((byte) green);
        this.buffer.put((byte) blue);
        this.buffer.put((byte) alpha);
    }

    public void color(float red, float green, float blue, float alpha) {
        color((byte) (255 * red), (byte) (255 * green), (byte) (255 * blue), (byte) (255 * alpha));
    }

    public void uv(int u, int v) {
        grow(8);

        this.buffer.putInt(u);
        this.buffer.putInt(v);
    }

    public void put(byte b) {
        grow(1);

        this.buffer.put(b);
    }

    public void put(float f) {
        grow(4);

        this.buffer.putFloat(4);
    }

    public void put(int i, byte b) {
        this.buffer.put(i, b);
    }

    public void put(int i, float f) {
        this.buffer.putFloat(i, f);
    }
}