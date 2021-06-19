package com.theboss.kzeaddonfabric.wip;

import net.minecraft.util.math.Vec3d;

public class Matrix4d {
    private double[][] matrix;

    public static Matrix4d translate(double x, double y, double z) {
        return new Matrix4d(new double[][]{
                {
                        1, 0, 0, x
                },
                {
                        0, 1, 0, y
                },
                {
                        0, 0, 1, z
                },
                {
                        0, 0, 0, 1
                }
        });
    }

    public static Matrix4d scale(double x, double y, double z) {
        return new Matrix4d(new double[][]{
                {
                        x, 0, 0, 0
                },
                {
                        0, y, 0, 0
                },
                {
                        0, 0, z, 0
                },
                {
                        0, 0, 0, 1
                }
        });
    }

    public Matrix4d() {
        this(new double[][]{
                {
                        1, 0, 0, 0
                },
                {
                        0, 1, 0, 0
                },
                {
                        0, 0, 1, 0
                },
                {
                        0, 0, 0, 1
                }
        });
    }

    public Matrix4d(double[][] matrix) {
        this.matrix = matrix;
    }

    public double get(int x, int y) {
        return this.matrix[x][y];
    }

    public void set(int x, int y, double value) {
        this.matrix[x][y] = value;
    }

    public void multiply(Matrix4d other) {
        double[][] result = new double[4][4];

        result[0][0] = this.get(0, 0) * other.get(0, 0) + this.get(1, 0) * other.get(0, 1) + this.get(2, 0) * other.get(0, 2) + this.get(3, 0) * other.get(0, 3);
        result[0][1] = this.get(0, 1) * other.get(0, 0) + this.get(1, 1) * other.get(0, 1) + this.get(2, 1) * other.get(0, 2) + this.get(3, 1) * other.get(0, 3);
        result[0][2] = this.get(0, 2) * other.get(0, 0) + this.get(1, 2) * other.get(0, 1) + this.get(2, 2) * other.get(0, 2) + this.get(3, 2) * other.get(0, 3);
        result[0][3] = this.get(0, 3) * other.get(0, 0) + this.get(1, 3) * other.get(0, 1) + this.get(2, 3) * other.get(0, 2) + this.get(3, 3) * other.get(0, 3);

        result[1][0] = this.get(0, 0) * other.get(1, 0) + this.get(1, 0) * other.get(1, 1) + this.get(2, 0) * other.get(1, 2) + this.get(3, 0) * other.get(1, 3);
        result[1][1] = this.get(0, 1) * other.get(1, 0) + this.get(1, 1) * other.get(1, 1) + this.get(2, 1) * other.get(1, 2) + this.get(3, 1) * other.get(1, 3);
        result[1][2] = this.get(0, 2) * other.get(1, 0) + this.get(1, 2) * other.get(1, 1) + this.get(2, 2) * other.get(1, 2) + this.get(3, 2) * other.get(1, 3);
        result[1][3] = this.get(0, 3) * other.get(1, 0) + this.get(1, 3) * other.get(1, 1) + this.get(2, 3) * other.get(1, 2) + this.get(3, 3) * other.get(1, 3);

        result[2][0] = this.get(0, 0) * other.get(2, 0) + this.get(1, 0) * other.get(2, 1) + this.get(2, 0) * other.get(2, 2) + this.get(3, 0) * other.get(2, 3);
        result[2][1] = this.get(0, 1) * other.get(2, 0) + this.get(1, 1) * other.get(2, 1) + this.get(2, 1) * other.get(2, 2) + this.get(3, 1) * other.get(2, 3);
        result[2][2] = this.get(0, 2) * other.get(2, 0) + this.get(1, 2) * other.get(2, 1) + this.get(2, 2) * other.get(2, 2) + this.get(3, 2) * other.get(2, 3);
        result[2][3] = this.get(0, 3) * other.get(2, 0) + this.get(1, 3) * other.get(2, 1) + this.get(2, 3) * other.get(2, 2) + this.get(3, 3) * other.get(2, 3);

        result[3][0] = this.get(0, 0) * other.get(3, 0) + this.get(1, 0) * other.get(3, 1) + this.get(2, 0) * other.get(3, 2) + this.get(3, 0) * other.get(3, 3);
        result[3][1] = this.get(0, 1) * other.get(3, 0) + this.get(1, 1) * other.get(3, 1) + this.get(2, 1) * other.get(3, 2) + this.get(3, 1) * other.get(3, 3);
        result[3][2] = this.get(0, 2) * other.get(3, 0) + this.get(1, 2) * other.get(3, 1) + this.get(2, 2) * other.get(3, 2) + this.get(3, 2) * other.get(3, 3);
        result[3][3] = this.get(0, 3) * other.get(3, 0) + this.get(1, 3) * other.get(3, 1) + this.get(2, 3) * other.get(3, 2) + this.get(3, 3) * other.get(3, 3);
        this.matrix = result;
    }

    public Vec3d transform(Vec3d vec) {
        double x = this.get(0, 0) * vec.x + this.get(1, 0) * vec.y + this.get(2, 0) * vec.z + this.get(3, 0) * 1;
        double y = this.get(0, 1) * vec.x + this.get(1, 1) * vec.y + this.get(2, 1) * vec.z + this.get(3, 1) * 1;
        double z = this.get(0, 2) * vec.x + this.get(1, 2) * vec.y + this.get(2, 2) * vec.z + this.get(3, 2) * 1;

        return new Vec3d(x, y, z);
    }
}
