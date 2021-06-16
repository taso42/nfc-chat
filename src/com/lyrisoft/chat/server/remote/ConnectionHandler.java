/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.chat.server.remote;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

import com.lyrisoft.chat.IConnectionHandler;
import com.lyrisoft.chat.IConnectionListener;

/**
 * The keeper of the Socket on the server side.  Spawns a thread for reading from the
 * socket.
 *
 * As each line is read from the socket, the server is notified, via the IConnectionListener
 * interface
 *
 * Outgoing messages are passed to the Dispatcher who queues them up 
 */
public class ConnectionHandler implements IConnectionHandler {
    protected PrintWriter _out;
    protected Socket _socket;
    protected ReaderThread _reader;
    protected InputStream _inputStream;
    protected IConnectionListener _connectionListener;
    protected boolean _isShutDown = false;

    protected Dispatcher _dispatcher;
    protected static final boolean DEBUG = ChatServer.getDebug();

    public void setListener(IConnectionListener listener) {
        _connectionListener = listener;
    }

    /**
     * Construct a ConnectionHandler for the given socket
     * @param s the socket
     * @param listener the object that will be notified with incoming messages
     * @exception IOException if there is a problem reading or writing to the socket
     */
    public ConnectionHandler(Socket s, IConnectionListener listener) throws IOException {
        _connectionListener = listener;
        _socket = s;
        _out = new PrintWriter(new OutputStreamWriter(s.getOutputStream()));
        _inputStream = s.getInputStream();
    }

    void init() {
        _reader = createReader();
        _reader.start();
    }

    public ReaderThread createReader() {
        return new ReaderThread(this, _connectionListener, _inputStream);
    }

    public void setDispatcher(Dispatcher d) {
        _dispatcher = d;
    }

    /**
     * Queue a message headed outbound
     */
    public void queueMessage(String message) {
        _dispatcher.queue(new Message(this, message));
    }

    public void sendImmediately(String message) {
        ChatServer.DEBUG("< " + message);
        _out.println(message);
        _out.flush();
    }

    /**
     * @param notify to notify the ConnectionListener.  Should be true for unexpected shutdowns
     *               (like if there is a socket error), and false otherwise (if client called this
     *                method on purpose)
     */
    public synchronized void shutdown(boolean notify) {
        if (!_isShutDown) {
            _isShutDown = true;

            _reader.pleaseStop();
            _reader.interrupt();

            try {
                _socket.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }

            if (notify) {
            	ChatServer.DEBUG("Notifying socketClosed: " + _connectionListener);
                _connectionListener.socketClosed();
            }
        }
    }
}
