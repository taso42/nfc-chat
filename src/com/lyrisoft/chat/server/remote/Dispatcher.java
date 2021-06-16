/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.chat.server.remote;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Dispatcher implements Runnable {
    protected List _queue;
    protected boolean _keepGoing = true;

    public Dispatcher() {
        _queue = Collections.synchronizedList(new LinkedList());
    }

    public void queue(Message m) {
        _queue.add(m);
    }

    public void run() {
        while (_keepGoing) {
            try {
                flush();
                Thread.sleep(5);
            }
            catch (InterruptedException e) { 
                e.printStackTrace();
            }
        }
    }

    public void flush() {
        while (_queue.size() > 0) {
            Message m = (Message)_queue.remove(0);
            m.send();
        }
    }

    public void pleaseStop() {
        _keepGoing = false;
    }
}

