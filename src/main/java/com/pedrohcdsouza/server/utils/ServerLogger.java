package com.pedrohcdsouza.server.utils;

public class ServerLogger {
    public static void info(String message) {
        System.out.println("[INFO]: " + message);
    }

    public static void warning(String message) {
        System.out.println("[WARNING]: " + message);
    }

    public static void error(String message) {
        System.err.println("[ERROR]: " + message);
    }

    public static void auth(String message) {
        System.out.println("[AUTH]: " + message);
    }

    public static void connect(String message) {
        System.out.println("[CONNECT]: " + message);
    }
} 