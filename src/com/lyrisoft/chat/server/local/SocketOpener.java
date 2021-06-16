/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 *
 * Based on code by Dan Bastone (dan@gibinc.com).
 */
package com.lyrisoft.chat.server.local;

import java.io.IOException;
import java.net.Socket;

public class SocketOpener implements Runnable {
    ISocketFactory socketFactory;
    Socket socket;
    IOException ioexception;
    
    public SocketOpener(String host, int port) {
        socketFactory = new SocketFactory(host, port);
    }

    public void run() {
        try {
            socket = socketFactory.makeSocket();
            return;
        }
        catch (IOException e) {
            ioexception = e;
        }
    }
    
    public Socket makeSocket(long timeout) throws IOException {
        socket = null;
        ioexception = null;
        Thread socketThread = new Thread(this, "MakeSocketThread");
        socketThread.start();
        try {
            socketThread.join(timeout);
        }
        catch (InterruptedException e) {}
        if ( socketThread.isAlive() ) {
            socketThread.stop();
        }
        if (ioexception != null) {
            throw ioexception;
        }
        if (socket == null) {
            throw new IOException ("Operation timed out");
        }
        return socket;
    }
}

interface ISocketFactory {
    Socket makeSocket() throws IOException;
}

class SocketFactory implements ISocketFactory {
    String _host;
    int _port;

    public SocketFactory(String host, int port) {
        _host = host;
        _port = port;
    }

    public Socket makeSocket() throws IOException {
        return new Socket(_host, _port);
    }
}
