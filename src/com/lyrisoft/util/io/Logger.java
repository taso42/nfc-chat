/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.util.io;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

public class Logger extends Thread {
    private PrintWriter _out;
    private boolean _enabled = true;
    private Vector _messages = new Vector();
    private boolean _keepgoing = true;
    private SimpleDateFormat _dateFormat = new SimpleDateFormat("MMM dd HH:mm:ss");

    /**
     * Create a new Logger that logs to outputFile.  Will append
     * if append is set to true.  Otherwise, it will overwrite
     */
    public Logger(String outputFile, boolean append) throws IOException {
        this(new PrintWriter(new FileWriter(outputFile, append)));
    }

    /**
     * Create a new Logger that logs to outputFile.
     * Don't append.  If the file exists, it will be overwritten
     */
    public Logger(String outputFile) throws IOException {
        this(outputFile, false);
    }

    public Logger(PrintWriter writer) {
        _out = writer;
        start();
    }

    public Logger(PrintStream ps) {
        _out = new PrintWriter(ps);
        (new Thread(this)).start();
    }

    public void run() {
        setPriority(Thread.MIN_PRIORITY);
        while (_keepgoing) {
            int len = _messages.size();
            for (int i=0; i < len; i++) {
                Object o = _messages.elementAt(0);
                _messages.removeElementAt(0);
                if (o instanceof Exception) {
                    _log((Exception)o);
                } else {
                    _log((String)o);
                }
            }
            _out.flush();
            try {
                sleep(500);
            }
            catch (InterruptedException e) {
                return;
            }
        }
        if (_out != null) {
            _out.close();
        }
        _out = null;
    }

    public void log(String s) {
        if (_enabled) {
            _messages.addElement(s);
        }
    }

    public void log(Exception e) {
        if (_enabled) {
            _messages.addElement(e);
        }
    }

    private void _log(String s) {
        _out.println(_dateFormat.format(new Date()) + " " + s);
    }

    private void _log(Exception e) {
        _out.print(_dateFormat.format(new Date()) + " " );
        e.printStackTrace(_out);
    }

    public void setEnabled(boolean b) {
        if (b) {
            System.err.println("Logger enabled.");
        } else {
            System.err.println("Logger disabled.");
        }

        _enabled = b;
    }

    public void close() {
        _enabled = false;
        _keepgoing = false;
    }

    public void finalize() throws Throwable {
        close();
    }
/*
    public static void main(String[] args) {
        Logger l = new Logger(System.out);
        int iters = 10000;

        for (int i=0; i < 100; i++) {
            q(l, iters);
        }
//            noq(l, iters);

//        l.close();
    }

    static void q(Logger l, int iters) {
        long begin = 0;
        long end = 0;

        begin = System.currentTimeMillis();
        for (int i=0; i < iters; i++) {
            l.log(i + ": qqqqqqqqqqqqqasdfasdfasdfkl;asdjfl;kasdjfl;kasdjf;lasdjf;sa");
        }
        end = System.currentTimeMillis();
        System.err.println("queued way: " + (end-begin));
    }

    static void noq(Logger l, int iters) {
        long begin = 0;
        long end = 0;

        begin = System.currentTimeMillis();
        for (int i=0; i < iters; i++) {
            l._log("qqqqqqqqqqqqqasdfasdfasdfkl;asdjfl;kasdjfl;kasdjf;lasdjf;sa");
        }
        end = System.currentTimeMillis();
        System.err.println("non-queued way: " + (end-begin));
    }
*/
}

