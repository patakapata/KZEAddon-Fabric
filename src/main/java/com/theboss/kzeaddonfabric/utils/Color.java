package com.theboss.kzeaddonfabric.utils;

public class Color {
    public static final Color BLACK = new Color(0x000000);
    public static final Color DARK_BLUE = new Color(0x0000AA);
    public static final Color DARK_GREEN = new Color(0x00AA00);
    public static final Color DARK_AQUA = new Color(0xAAAA);
    public static final Color DARK_RED = new Color(0xAA0000);
    public static final Color DARK_PURPLE = new Color(0xAA00AA);
    public static final Color GOLD = new Color(0xFFAA00);
    public static final Color GRAY = new Color(0xAAAAAA);
    public static final Color DARK_GRAY = new Color(0x555555);
    public static final Color BLUE = new Color(0x5555FF);
    public static final Color GREEN = new Color(0x55FF55);
    public static final Color AQUA = new Color(0x55FFFF);
    public static final Color RED = new Color(0xFF5555);
    public static final Color LIGHT_PURPLE = new Color(0xFF55FF);
    public static final Color YELLOW = new Color(0xFFFF55);
    public static final Color WHITE = new Color(0xFFFFFF);

    private short red;
    private short green;
    private short blue;


    public static String toHexString(int color) {
        return Color.toHexString(color >> 16 & 0xFF, color >> 8 & 0xFF, color & 0xFF);
    }

    public static String toHexString(int red, int green, int blue) {
        return fullString(red) + fullString(green) + fullString(blue);
    }

    protected static String fullString(int value) {
        String str = Integer.toHexString(value);
        return str.length() == 1 ? "0" + str : str;
    }

    public static Color lerp(Color from, Color to, double progress) {
        int red = (int) ((to.red - from.red) * progress);
        int green = (int) ((to.green - from.green) * progress);
        int blue = (int) ((to.blue - from.blue) * progress);

        return new Color(from.red + red, from.green + green, from.blue + blue);
    }

    public static int parseWithAlpha(int alpha, int red, int green, int blue) {
        return alpha << 24 | red << 16 | green << 8 | blue;
    }

    /**
     * @param color 0xAARRGGBB
     * @return { A, R, G, B }
     */
    public static int[] parseWithAlpha(int color) {
        return new int[]{color >> 24 & 0xFF, color >> 16 & 0xFF, color >> 8 & 0xFF, color & 0xFF};
    }

    public static int parse(int red, int green, int blue) {
        return red << 16 | green << 8 | blue;
    }

    /**
     * @param color 0xRRGGBB
     * @return { R, G, B }
     */
    public static int[] parse(int color) {
        return new int[]{color >> 16 & 0xFF, color >> 8 & 0xFF, color & 0xFF};
    }

    public Color(int color) {
        this.red = (short) (color >> 16 & 0xFF);
        this.green = (short) (color >> 8 & 0xFF);
        this.blue = (short) (color & 0xFF);
    }

    public Color(int red, int green, int blue) {
        this.red = (short) (red & 0xFF);
        this.green = (short) (green & 0xFF);
        this.blue = (short) (blue & 0xFF);
    }

    public String toHexString() {
        return fullString(this.red) + fullString(this.green) + fullString(this.blue);
    }

    public int get() {
        return this.red << 16 | this.green << 8 | this.blue;
    }

    public int getRed() {
        return this.red;
    }

    public void setRed(int red) {
        this.red = (short) (red & 0xFF);
    }

    public int getGreen() {
        return this.green;
    }

    public void setGreen(int green) {
        this.green = (short) (green & 0xFF);
    }

    public int getBlue() {
        return this.blue;
    }

    public void setBlue(int blue) {
        this.blue = (short) (blue & 0xFF);
    }
}
