package org.tinylog;

import dev.webfx.platform.console.Console;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author Bruno Salmon
 * @author Armin Reichert
 */
public class Logger {

    private static void println(String message, Object... args) {
        message = message.replaceAll("\\{[^}]*\\}", "__");
        for (var arg : args) {
            message = message.replaceFirst("__", String.valueOf(arg));
        }
        var dateFormat = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss SSS");
        var prefix = LocalDateTime.now().format(dateFormat) + ": ";
        Console.log(prefix + message);
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