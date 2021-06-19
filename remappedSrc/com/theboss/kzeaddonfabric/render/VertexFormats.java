package com.theboss.kzeaddonfabric.render;

import com.google.common.collect.ImmutableList;

import static org.lwjgl.opengl.GL11.*;

public class VertexFormats {
    public static final VertexFormat EMPTY = new VertexFormat(ImmutableList.of());
    public static final VertexFormat POSITION = new VertexFormat(ImmutableList.of(VertexFormat.Element.POS_ELEMENT));
    public static final VertexFormat POSITION_COLOR = new VertexFormat(ImmutableList.of(VertexFormat.Element.POS_ELEMENT, VertexFormat.Element.COLOR_ELEMENT));
    public static final VertexFormat POSITION_UV = new VertexFormat(ImmutableList.of(VertexFormat.Element.POS_ELEMENT, VertexFormat.Element.UV_ELEMENT));
    public static final VertexFormat POSITION_COLOR_UV = new VertexFormat(ImmutableList.of(VertexFormat.Element.POS_ELEMENT, VertexFormat.Element.COLOR_ELEMENT, VertexFormat.Element.UV_ELEMENT));

    public static class VertexFormat {
        private final ImmutableList<Element> elements;
        private final int size;

        public VertexFormat(ImmutableList<Element> elements) {
            this.elements = elements;
            int size = 0;
            for (int i = 0; i < this.elements.size(); i++) {
                Element element = this.elements.get(i);
                size += element.size;
            }
            this.size = size;
        }

        public ImmutableList<Element> elements() {
            return this.elements;
        }

        public int size() {
            return this.size;
        }

        public static class Element {
            public static final Element POS_ELEMENT = new Element(GL_VERTEX_ARRAY, 3, Type.FLOAT);
            public static final Element COLOR_ELEMENT = new Element(GL_COLOR_ARRAY, 4, Type.UNSIGNED_BYTE);
            public static final Element UV_ELEMENT = new Element(GL_TEXTURE_COORD_ARRAY, 2, Type.FLOAT);

            private final int id;
            private final int count;
            private final Type type;
            private final int size;

            public Element(int id, int count, Type type) {
                this.id = id;
                this.count = count;
                this.type = type;
                this.size = this.count * this.type.BYTES;
            }

            public int count() {
                return this.count;
            }

            public Type type() {
                return this.type;
            }

            public int size() {
                return this.size;
            }

            public int id() {
                return this.id;
            }
        }

        public static class Type {
            public static final Type UNSIGNED_BYTE = new Type(Byte.BYTES, GL_UNSIGNED_BYTE);
            public static final Type BYTE = new Type(Byte.BYTES, GL_BYTE);
            public static final Type SHORT = new Type(Short.BYTES, GL_SHORT);
            public static final Type INT = new Type(Integer.BYTES, GL_INT);
            public static final Type FLOAT = new Type(Float.BYTES, GL_FLOAT);
            public static final Type DOUBLE = new Type(Double.BYTES, GL_DOUBLE);

            private final int BYTES;
            private final int GL_TYPE;

            public int getBYTES() {
                return this.BYTES;
            }

            public int getGLType() {
                return this.GL_TYPE;
            }

            public Type(int BYTES, int GL_TYPE) {
                this.BYTES = BYTES;
                this.GL_TYPE = GL_TYPE;
            }
        }
    }

}
