package com.theboss.kzeaddonfabric.wip;

import java.util.Scanner;

public class TestUtils {
    private TestUtils() {}

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
