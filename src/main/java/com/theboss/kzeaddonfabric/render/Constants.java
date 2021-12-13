package com.theboss.kzeaddonfabric.render;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

public class Constants {
    // -------------------------------------------------- //
    // OpenGL
    public static final int GL_POINTS = GL11.GL_POINTS;
    public static final int GL_LINES = GL11.GL_LINES;
    public static final int GL_LINE_LOOP = GL11.GL_LINE_LOOP;
    public static final int GL_LINE_STRIP = GL11.GL_LINE_STRIP;
    public static final int GL_TRIANGLES = GL11.GL_TRIANGLES;
    public static final int GL_TRIANGLE_STRIP = GL11.GL_TRIANGLE_STRIP;
    public static final int GL_TRIANGLE_FAN = GL11.GL_TRIANGLE_FAN;
    public static final int GL_QUADS = GL11.GL_QUADS;
    public static final int GL_QUAD_STRIP = GL11.GL_QUAD_STRIP;
    public static final int GL_POLYGON = GL11.GL_POLYGON;

    public static final int GL_SMOOTH = GL11.GL_SMOOTH;
    public static final int GL_FLAT = GL11.GL_FLAT;
    // -------------------------------------------------- //
    // GLFW
    public static final int GLFW_MOUSE_BUTTON_LEFT = GLFW.GLFW_MOUSE_BUTTON_LEFT;
    public static final int GLFW_MOUSE_BUTTON_MIDDLE = GLFW.GLFW_MOUSE_BUTTON_MIDDLE;
    public static final int GLFW_MOUSE_BUTTON_RIGHT = GLFW.GLFW_MOUSE_BUTTON_RIGHT;

    private Constants() {}
}
