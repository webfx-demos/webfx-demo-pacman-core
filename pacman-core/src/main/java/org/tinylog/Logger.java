package org.tinylog;

/**
 * @author Bruno Salmon
 */
public class Logger {

    public static void trace(Object message, Object... parameters) {
        System.out.println(message);
    }

    public static void info(Object message, Object... parameters) {
        System.out.println(message);
    }

    public static void error(Object message, Object... parameters) {
        System.out.println(message);
    }

}
