package org.tinylog;

import dev.webfx.platform.console.Console;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Function;
import java.util.regex.*;

/**
 * @author Bruno Salmon
 * @author Armin Reichert
 */
public class Logger {

/* GWT does not support Java regular expression stuff. It works fine in the VM version.
    private static void println(String message, Object... args) {
        var dateFormat = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss SSS");
        var prefix = LocalDateTime.now().format(dateFormat) + ": ";
        var pattern = Pattern.compile("\\{.*?\\}");
        int i = 0;
        do {
            var matcher = pattern.matcher(message);
            if (!matcher.find() || i == args.length) {
                break;
            }
            message = matcher.replaceFirst(Matcher.quoteReplacement(String.valueOf(args[i])));
            i += 1;
        } while (true);
        Console.log(prefix + message);
    }
*/
    private static void println(String message, Object... args) {
        var dateFormat = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss SSS");
        var prefix = LocalDateTime.now().format(dateFormat) + ": ";
        var argsText = new StringBuilder(" (");
        for (int i = 0; i < args.length; ++i) {
            argsText.append(args[i]);
            if (i < args.length - 1) {
                argsText.append(", ");
            }
        }
        argsText.append(")");
        Console.log(prefix + message + argsText);
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