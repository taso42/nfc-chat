package com.lyrisoft.util.properties;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

import com.lyrisoft.util.io.ResourceException;
import com.lyrisoft.util.io.ResourceLoader;


/**
 * <pre>
 * A helper class that:
 *     o loads Properties files from the CLASSPATH
 *     o reads typesafe values from Properties files
 *     o throws Exceptions when properties are not found
 * </pre>
 *
 * @author Taso Lyristis <taso@lyrisoft.com>
 *
 * $Id: PropertyTool.java,v 1.5 2003/07/03 10:03:59 latesta Exp $
 */
public class PropertyTool {
    /**
     * Will load and return a properties file from the specified pathname.
     */
    public static Properties loadProperties(String relativePath)
        throws PropertyException
    {
        try {
            InputStream is = ResourceLoader.getResource(relativePath);
            return loadProperties(is);
        }
        catch (ResourceException e) {
            throw new PropertyException(e.toString());
        }
    }

    public static Properties loadProperties(InputStream is)
        throws PropertyException
    {
        Properties p = new Properties();
        try {
            p.load(is);
        } catch (Exception e) {
            throw new PropertyException(e.getMessage());
        } finally {
            if (is != null) {
                try {
                    is.close();
                }
                catch (IOException e) {}
            }
        }
        return p;
    }

    public static String getString(String name, Properties p) throws PropertyException {
        String s = p.getProperty(name);
        if (s == null) {
            throw new PropertyException("No such property: " + name);
        }
        return s;
    }

    public static Vector getStringVector(String name, Properties p) throws PropertyException {
        String s = getString(name, p);
        StringTokenizer st = new StringTokenizer(s, ", ");
        Vector v = new Vector();
        while (st.hasMoreTokens()) {
            v.addElement(st.nextToken());
        }
        return v;
    }

    public static String[] getStringArray(String name, Properties p) throws PropertyException {
        Vector v = getStringVector(name, p);
        String[] strings = new String[v.size()];
        v.copyInto(strings);
        return strings;
    }

    public static int getInteger(String name, Properties p) throws PropertyException {
        String s = getString(name, p);
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            throw new PropertyException("Property " + name + " is not an integer.");
        }
    }

    public static float getFloat(String name, Properties p) throws PropertyException {
        String s = getString(name, p);
        try {
            return Float.valueOf(s).floatValue();
        } catch (NumberFormatException e) {
            throw new PropertyException("Property " + name + " is not a float.");
        }
    }

    public static double getDouble(String name, Properties p) throws PropertyException {
        String s = getString(name, p);
        try {
            return Double.valueOf(s).doubleValue();
        } catch (NumberFormatException e) {
            throw new PropertyException("Property " + name + " is not a double.");
        }
    }

    public static boolean getBoolean(String name, Properties p) throws PropertyException {
        String s = getString(name, p);
        return Boolean.valueOf(s).booleanValue();
    }
}
