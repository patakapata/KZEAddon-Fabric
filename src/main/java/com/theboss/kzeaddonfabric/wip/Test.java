package com.theboss.kzeaddonfabric.wip;

import net.minecraft.util.math.Vec3d;

public class Test {
    public static void main(String[] args) {
        Matrix4d matrix = new Matrix4d();
        matrix.multiply(Matrix4d.translate(2, 50, 5));
        Vec3d vec = new Vec3d(2.0, 2.0, 2.0);
        vec = matrix.transform(vec);
        System.out.println(vec.toString());
    }
}
