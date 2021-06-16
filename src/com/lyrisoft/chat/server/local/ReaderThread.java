/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.chat.server.local;

import java.io.DataInputStream;
import java.io.IOException;

import com.lyrisoft.chat.IConnectionHandler;
import com.lyrisoft.chat.IConnectionListener;

/**
 * Constantly read from the socket's input stream
 */
class ReaderThread extends Thread {
    private boolean keepGoing = true;
    private DataInputStream _in;
    private IConnectionListener _listener;
    private IConnectionHandler _connectionHandler;

    ReaderThread(DataInputStream in, IConnectionHandler handler) {
        super("ConnectionHandler$ReaderThread");
        _in = in;
        _connectionHandler = handler;
    }
    
    public void setListener(IConnectionListener listener) {
        _listener = listener;
    }

    public void run() {
        try {
            String newLine;
            while (keepGoing &&
                   (newLine = _in.readLine()) != null) {
                if (_listener != null) {
                    ConnectionHandlerLocal.DEBUG("< " + newLine);
                    _listener.incomingMessage(newLine);
                } else {
                    System.err.println("no listener: " + newLine);
                }
            }
            System.err.println("ReaderThread: stopping gracefully.");
        }
        catch (IOException e) {
            keepGoing = false;
        }
        _connectionHandler.shutdown(false);
    }

    void pleaseStop() {
        keepGoing = false;
    }
}
