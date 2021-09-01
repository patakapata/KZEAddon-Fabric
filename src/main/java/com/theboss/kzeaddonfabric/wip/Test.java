package com.theboss.kzeaddonfabric.wip;

import java.util.Scanner;

public class Test {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("Input 1 > ");
            String in1 = scanner.next();
            if (in1.equals("exit")) {
                System.out.println("終了します");
                break;
            }
            System.out.print("Input 2 > ");
            String in2 = scanner.next();

            int num1 = safeParse(in1);
            int num2 = safeParse(in2);

            System.out.println("Result > " + ChunkInstancedBarrierVisualizer.Chunk.wrap(num1, num2));
        }
    }

    public static int safeParse(String src) {
        try {
            return Integer.parseInt(src);
        } catch (NumberFormatException ex) {
            return -1;
        }
    }

    public static String safeRead(Scanner scanner) {
        try {
            return scanner.nextLine();
        } catch (Exception ex) {
            return "";
        }
    }
}
