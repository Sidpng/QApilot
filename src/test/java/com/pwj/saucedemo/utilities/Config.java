/**
 * Project Title: SauceDemo Playwright Automation
 * File Name: Config.java
 * Author: Siddhartha Upadhyay
 * GitHub: https://github.com/Sidpng
 * Creation Date: 12-Aug-2025
 * Description: Centralized config loader with System property overrides.
 */
package com.pwj.saucedemo.utilities;

import java.io.InputStream;
import java.util.Properties;

public final class Config {
    private static final Properties P = new Properties();

    static {
        try (InputStream in = ClassLoader.getSystemResourceAsStream("config.properties")) {
            if (in != null) P.load(in);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load config.properties", e);
        }
    }

    private Config() {}

    public static String get(String key, String def) {
        return System.getProperty(key, P.getProperty(key, def));
    }

    public static String get(String key) {
        String v = System.getProperty(key);
        if (v != null) return v;
        v = P.getProperty(key);
        if (v == null) throw new IllegalArgumentException("Missing config key: " + key);
        return v;
    }

    public static boolean getBoolean(String key, boolean def) {
        return Boolean.parseBoolean(get(key, String.valueOf(def)));
    }

    public static int getInt(String key, int def) {
        try {
            return Integer.parseInt(get(key, String.valueOf(def)));
        } catch (NumberFormatException e) {
            return def;
        }
    }
}
