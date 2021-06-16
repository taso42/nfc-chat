/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.chat;

import java.util.Properties;

/**
 * A wrapper around the com.lyrisoft.util.i18n.Translator object that
 * provides static methods that can be called from anywhere, and an
 * init() method that should be called once, early on.
 */
public class Translator {
    private static com.lyrisoft.util.i18n.Translator _translator;

    /**
     * Initialize the "real", underlying Translator object with the
     * given properties file.
     */
    public static void init(Properties p) {
        if (_translator == null) {
            _translator = new com.lyrisoft.util.i18n.Translator(p);
        } else {
            System.err.println("Warning:  Translator was initialized more than once");
        }
    }

    public static String getMessage(String key) {
        return _translator.getMessage(key);
    }

    public static String getMessage(String key, String arg1) {
        return _translator.getMessage(key, arg1);
    }

    public static String getMessage(String key, String arg1, String arg2) {
        return _translator.getMessage(key, arg1, arg2);
    }

    public static String getMessage(String key, String arg1, String arg2, String arg3) {
        return _translator.getMessage(key, arg1, arg2, arg3);
    }

    public static String getMessage(String key, String[] args) {
        return _translator.getMessage(key, args);
    }
}

