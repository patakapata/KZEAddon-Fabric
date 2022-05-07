package com.theboss.kzeaddonfabric.wip;

import net.minecraft.util.math.MathHelper;

import java.util.Scanner;

public class Test {
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            String in;
            float v;
            while (true) {
                System.out.print("Enter value :");
                in = scanner.nextLine();
                if (in.equals("end")) break;
                try {
                    v = Float.parseFloat(in);
                    System.out.println("JaMath: " + in + " -> " + Math.floor(v));
                    System.out.println("JaCast: " + in + " -> " + ((int) v));
                    System.out.println("Helper: " + in + " -> " + MathHelper.floor(v));
                } catch (NumberFormatException ex) {
                    ex.printStackTrace();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
