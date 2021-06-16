package com.lyrisoft.util.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletContext;

/**
 * A class that loads resources from locations relative to the
 * CLASSPATH, or relative to the ServletContext if one is specified.
 *
 * $Id: ResourceLoader.java,v 1.6 2003/07/03 10:03:59 latesta Exp $
 */
public class ResourceLoader {
    private static ServletContext servletContext;

    /**
     * All static class - don't instantiate
     */
    protected ResourceLoader() {
    }

    public static void setServletContext(ServletContext context) {
        servletContext = context;
    }

    /**
     * Load a resource from the CLASSPATH.  relativePath should use forward-slashes
     * and omit the first slash.
     *
     * e.g., getResource("path/to/my/resource");
     *
     * @param relativePath
     */
    public static InputStream getResource(String relativePath) throws ResourceException {
        if (servletContext != null) {
            return getServletResource(relativePath);
        }

        // we use the actual ClassLoader rather than the simple,
        // Class.getResource() method, because we want the SYSTEM
        // ClassLoader, not a Servlet ClassLoader, or any other loader.
        InputStream is = ClassLoader.getSystemResourceAsStream(relativePath);
        if (is == null) {
            throw new ResourceException("Could not locate resource, " + relativePath);
        }
        return is;
    } 

    /**
     * Brings a resource into existence if it does not already exist.
     * This is useful when running within a servlet context, to pop
     * the log files into existence before trying to use them.
     */
    public static String touch(String relativePath) throws ResourceException {
        if (servletContext == null) {
            throw new ResourceException("Sorry, you can't touch a resource unless you're running as a servlet");
        }
        String workDir = servletContext.getRealPath("/");
        if (workDir == null)
        	throw new ResourceException("Could not find working directory.");
        workDir = workDir.substring(0, workDir.lastIndexOf(File.separator));

        String fileName = workDir+File.separator+"WEB-INF"+File.separator+relativePath;
        File file = new File(fileName);
        File parent = new File(file.getParent());
        parent.mkdirs();
        if (!file.exists()) {
            try {
                if (!file.createNewFile()) {
                    throw new ResourceException("Couldn't create " + file);
                }
            }
            catch (IOException e) {
                throw new ResourceException("Couldn't create " + file);
            }
        }
        
        return fileName;
    }

    private static InputStream getServletResource(String relativePath) 
        throws ResourceException 
    {
        return servletContext.getResourceAsStream("/WEB-INF/" + relativePath);
    }
}
