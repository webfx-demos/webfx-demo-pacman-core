package org.tinylog;

import dev.webfx.platform.console.Console;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Function;
import java.util.regex.*;

/**
 * Crappy workaround for missing TinyLog library. No Java regex classes are used because
 * GWT does not support these.
 *
 * @author Bruno Salmon
 * @author Armin Reichert
 */
public class Logger {

    public static boolean traceEnabled = false;

    private static final char PLACEHOLDER_START = '{';
    private static final char PLACEHOLDER_END = '}';

    private static String format(String text, Object... args) {
        if (text == null || args == null) {
            return text;
        }

        // start and end index of placeholders in text
        int[][] placeholders = new int[text.length() / 2][2];
        int count = 0;
        int begin = 0;
        boolean started = false;
        int error = -1;
        char[] chars = text.toCharArray();
        for (int i = 0; i < chars.length; ++i) {
            if (error != -1) {
                break;
            }
            if (chars[i] == PLACEHOLDER_START) {
                if (started) {
                    error = i;
                } else {
                    begin = i;
                    started = true;
                }
            } else if (chars[i] == PLACEHOLDER_END) {
                if (started) {
                    placeholders[count][0] = begin;
                    placeholders[count][1] = i;
                    ++count;
                    started = false;
                } else {
                    error = i;
                }
            }
        }

        if (error != -1) {
            return "Error in format string '" + text + "' at index " + error + ", unexpected: " + chars[error];
        }

        if (args.length == 0 || count == 0) {
            return text;
        }

        var s = new StringBuilder();
        int phi = 0; // placeholder index
        int i = 0;
        while (i < chars.length) {
            if (phi < count && i == placeholders[phi][0] && phi < args.length) {
                s.append(args[phi]);
                i = placeholders[phi][1] + 1; // position after '}'
                ++phi;
            } else {
                s.append(chars[i++]);
            }
        }
        return s.toString();
    }

    private static void logFormattedMessage(String messageFormat, Object... args) {
        var dateFormat = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss SSS");
        var prefix = LocalDateTime.now().format(dateFormat) + ": ";
        var message = format(messageFormat, args);
        Console.log(prefix + message);
    }

    public static void trace(String message, Object... parameters) {
        if (traceEnabled) {
            logFormattedMessage(message, parameters);
        }
    }

    public static void info(String message, Object... parameters) {
        logFormattedMessage(message, parameters);
    }

    public static void error(String message, Object... parameters) {
        logFormattedMessage(message, parameters);
    }
}