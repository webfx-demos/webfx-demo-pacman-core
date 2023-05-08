package org.tinylog;

/**
 * @author Bruno Salmon
 */
public class Logger {

    private static void println(String message, Object... args) {
        System.out.print(message + ": ");
        for (Object arg : args) {
            System.out.print(" " + arg);
        }
        System.out.println();
    }

    public static void trace(String message, Object... parameters) {
        println(message, parameters);
    }

    public static void info(String message, Object... parameters) {
        println(message, parameters);
    }

    public static void error(String message, Object... parameters) {
        println(message, parameters);
    }
}
