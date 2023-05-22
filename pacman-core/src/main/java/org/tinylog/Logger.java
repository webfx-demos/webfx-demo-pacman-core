package org.tinylog;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * @author Bruno Salmon
 * @author Armin Reichert
 */
public class Logger {

    private static void println(String message, Object... args) {
        message = message.replaceAll("\\{[^}]*\\}", "__");
        for (int i = 0; i < args.length; ++i) {
            message = message.replaceFirst("__", String.valueOf(args[i]));
        }
        var dateFormat = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss SSS");
        var prefix = LocalDateTime.now().format(dateFormat) + ": ";
        System.out.println(prefix + message);
    }

    public static void trace(String message, Object... parameters) {
        println(message, parameters); }

    public static void info(String message, Object... parameters) {
        println(message, parameters);
    }

    public static void error(String message, Object... parameters) {
        println(message, parameters);
    }
}