/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.util;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Utilities for using a Map with multiple values for each key.
 *
 * The values for these maps end up being HashMaps themselves.  
 */
public class MultiValueHashMap {
    public static void put(Map map, Object key, Object value) {
        HashMap valueMap = (HashMap)map.get(key);
        if (valueMap == null) {
            valueMap = new HashMap();
            map.put(key, valueMap);
        }
        valueMap.put(key(value), value);
    }

    public static void remove(Map map, Object key, Object value) {
        HashMap valueMap = (HashMap)map.get(key);
        if (valueMap == null) {
            return;
        } 
        valueMap.remove(key(value));
    }

    public static int size(Map map, Object key) {
        HashMap valueMap = (HashMap)map.get(key);
        if (valueMap == null) {
            return 0;
        } else {
            return valueMap.size();
        }
    }

    public static String key(Object o) {
        return o.toString().toUpperCase();
    }

    public static void dump(Map m, PrintStream out) {
        for (Iterator i = m.keySet().iterator(); i.hasNext(); ) {
            Object key = i.next();
            out.println(key);
            Map inner = (Map)m.get(key);
            for (Iterator j = inner.keySet().iterator(); j.hasNext(); ) {
                key = j.next();
                Object value = inner.get(key);
                out.println("|- " + key + " = " + value);
            }
        }
    }
}
