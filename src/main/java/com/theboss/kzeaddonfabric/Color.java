package com.theboss.kzeaddonfabric;

import com.google.gson.annotations.Expose;

public class Color {
    @Expose
    private int red;
    @Expose
    private int green;
    @Expose
    private int blue;

    public static Color lerp(Color from, Color to, double progress) {
        int red = (int) ((to.red - from.red) * progress);
        int green = (int) ((to.green - from.green) * progress);
        int blue = (int) ((to.blue - from.blue) * progress);

        return new Color(from.red + red, from.green + green, from.blue + blue);
    }

    public Color(int color) {
        this.red = color >> 16 & 0xFF;
        this.green = color >> 8 & 0xFF;
        this.blue = color & 0xFF;
    }

    public Color(int red, int green, int blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    public String toHexString() {
        return Integer.toHexString(this.get());
    }

    public int get() {
        return this.red << 16 | this.green << 8 | this.blue;
    }

    public int getRed() {
        return this.red;
    }

    public void setRed(int red) {
        this.red = red;
    }

    public int getGreen() {
        return this.green;
    }

    public void setGreen(int green) {
        this.green = green;
    }

    public int getBlue() {
        return this.blue;
    }

    public void setBlue(int blue) {
        this.blue = blue;
    }
}
