package org.cduggan;

public class Logger {
    private static boolean LOG_VERBOSE = false;
    public static void log(String message, boolean verbose) {
        if (!verbose || LOG_VERBOSE) {
            System.out.println(message);
        }
    }
}
