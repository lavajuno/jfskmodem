package org.lavajuno.jfskmodem.log;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Log provides simple functionality for logging events by class.
 * Log events can have one of five severity levels:
 * DEBUG, INFO, WARN, ERROR and FATAL.
 */
@SuppressWarnings("unused")
public class Log {
    private static final SimpleDateFormat LOG_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final String LOG_PREFIX = " (jfskmodem)";
    private static final String LOG_DEBUG = " \u001B[34m[ DEBUG ]\u001B[0m ";
    private static final String LOG_INFO = " \u001B[32m[ INFO ]\u001B[0m  ";
    private static final String LOG_WARN = " \u001B[33m[ WARN ]\u001B[0m  ";
    private static final String LOG_ERROR = " \u001B[31m[ ERROR ]\u001B[0m ";
    private static final String LOG_FATAL = " \u001B[31m[ FATAL ]\u001B[0m ";

    public enum Level {
        DEBUG, INFO, WARN, ERROR, FATAL
    }

    private final String class_name;
    private final Level log_level;

    /**
     * Instantiates a Log for this class
     * @param class_name This class's name
     * @param log_level This class's log level
     */
    public Log(String class_name, Level log_level) {
        this.class_name = class_name;
        this.log_level = log_level;
    }

    /**
     * Logs an event with a specified severity
     * @param level Severity of the log event
     * @param message Message to log
     */
    private void print(Level level, String message) {
        if(level.ordinal() < log_level.ordinal()) { return; }
        StringBuilder sb = new StringBuilder();
        sb.append(LOG_DATE_FORMAT.format(new Date()));
        sb.append(LOG_PREFIX);
        switch (level) {
            case DEBUG -> sb.append(LOG_DEBUG);
            case INFO -> sb.append(LOG_INFO);
            case WARN -> sb.append(LOG_WARN);
            case ERROR -> sb.append(LOG_ERROR);
            case FATAL -> sb.append(LOG_FATAL);
        }
        sb.append(class_name);
        sb.append(" ".repeat(Math.max(0, 20 - class_name.length())));
        sb.append(": ");
        sb.append(message);
        System.out.println(sb);
    }

    /**
     * Logs an event with severity DEBUG
     * @param message Message to log
     */
    public void debug(String message) { this.print(Level.DEBUG, message); }

    /**
     * Logs an event with severity INFO
     * @param message Message to log
     */
    public void info(String message) { this.print(Level.INFO, message); }

    /**
     * Logs an event with severity WARN
     * @param message Message to log
     */
    public void warn(String message) { this.print(Level.WARN, message); }

    /**
     * Logs an event with severity ERROR
     * @param message Message to log
     */
    public void error(String message) { this.print(Level.ERROR, message); }

    /**
     * Logs an event with severity FATAL
     * @param message Message to log
     */
    public void fatal(String message) { this.print(Level.FATAL, message); }

}