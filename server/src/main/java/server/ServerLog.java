package server;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Минимальная прослойка для логирования на сервере.
 *
 * Если Log4J2 доступен в classpath, будет использован он (через reflection).
 * Иначе используется стандартный java.util.logging, чтобы проект продолжал
 * компилироваться/запускаться без внешних зависимостей.
 */
public final class ServerLog {

    private static final Logger jul = Logger.getLogger("server");

    private static final Object log4jLogger; // org.apache.logging.log4j.Logger
    private static final Method log4jInfo;
    private static final Method log4jWarn;
    private static final Method log4jError;

    static {
        Object logger = null;
        Method info = null;
        Method warn = null;
        Method error = null;

        try {
            Class<?> logManager = Class.forName("org.apache.logging.log4j.LogManager");
            Method getLogger = logManager.getMethod("getLogger", Class.class);
            logger = getLogger.invoke(null, ServerLog.class);

            Class<?> loggerClass = Class.forName("org.apache.logging.log4j.Logger");
            info = loggerClass.getMethod("info", Object.class);
            warn = loggerClass.getMethod("warn", Object.class);
            error = loggerClass.getMethod("error", Object.class);
        } catch (Throwable ignored) {
            // Log4J2 не доступен — остаёмся на JUL.
        }

        log4jLogger = logger;
        log4jInfo = info;
        log4jWarn = warn;
        log4jError = error;
    }

    private ServerLog() {
    }

    public static void info(String message) {
        log(Level.INFO, message, null);
    }

    public static void warn(String message) {
        log(Level.WARNING, message, null);
    }

    public static void error(String message) {
        log(Level.SEVERE, message, null);
    }

    public static void error(String message, Throwable t) {
        log(Level.SEVERE, message, t);
    }

    public static void info(String template, Object arg) {
        info(format(template, arg));
    }

    public static void warn(String template, Object arg) {
        warn(format(template, arg));
    }

    public static void error(String template, Object arg) {
        error(format(template, arg));
    }

    private static void log(Level level, String message, Throwable t) {
        if (log4jLogger != null) {
            try {
                if (level == Level.INFO && log4jInfo != null) {
                    log4jInfo.invoke(log4jLogger, message);
                    return;
                }
                if (level == Level.WARNING && log4jWarn != null) {
                    log4jWarn.invoke(log4jLogger, message);
                    return;
                }
                if (level == Level.SEVERE && log4jError != null) {
                    log4jError.invoke(log4jLogger, message);
                    return;
                }
            } catch (Throwable ignored) {
                // упали на log4j — фолбэк на JUL
            }
        }

        if (t == null) {
            jul.log(level, message);
        } else {
            jul.log(level, message, t);
        }
    }

    private static String format(String template, Object arg) {
        return template.replace("{}", Objects.toString(arg));
    }
}

