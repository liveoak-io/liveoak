package io.liveoak.security.impl;

import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Just some simple utility until we have proper logging. TODO: remove
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class SimpleLogger {

    private static final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss,SSS");

    private final String simpleClassName;

    public SimpleLogger(Class<?> clazz) {
        simpleClassName = clazz.getSimpleName();
    }

    public void info(String message) {
        log("INFO", message, System.out);
    }

    public void warn(String message) {
        log("WARN", message, System.err);
    }

    public void debug(String message) {
        log("DEBUG", message, System.out);
    }

    public void trace(String message) {
        log("TRACE", message, System.out);
    }

    public void trace(String message, Exception e) {
        log("TRACE", message, System.out);
        e.printStackTrace();
    }

    public void error(String message) {
        log("ERROR", message, System.err);
    }

    public void error(String message, Throwable e) {
        log("ERROR", message, System.err);
        e.printStackTrace();
    }

    public boolean isDebugEnabled() {
        return Boolean.getBoolean("mbaas.log.verbose");
    }

    public boolean isTraceEnabled() {
        return Boolean.getBoolean("mbaas.log.verbose");
    }

    private void log(String prefix, String message, PrintStream stream) {
        String time = sdf.format(new Date());
        stream.printf("%s %s [%s] %s", time, prefix, simpleClassName, message);
        stream.println();
    }
}
