package com.example.usermanagement.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {
    private static final Properties props = new Properties();

    static {
        try (InputStream in = Config.class.getResourceAsStream("/config.properties")) {
            if (in != null) props.load(in);
        } catch (IOException ignored) {}
    }

    public static String get(String key, String defaultValue) {
        String v = System.getenv(key);
        if (v != null && !v.isEmpty()) return v;
        v = props.getProperty(key);
        if (v != null && !v.isEmpty()) return v;
        return defaultValue;
    }
}

