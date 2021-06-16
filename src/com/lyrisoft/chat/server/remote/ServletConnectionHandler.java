/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.chat.server.remote;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.lyrisoft.chat.IConnectionHandler;
import com.lyrisoft.chat.IConnectionListener;

/**
 * Special connection handler that handles "tunneled" connections to the chat.
 *
 * The ChatServlet delegates calls to it's service method to us:
 * Upon a client's initial connection, the client is given a unique identifier, and the run
 * method here is called.  In the run method, we handle the output stream back to the client.
 * On subsequent requests from the client, the client passes the unique identifier, and a message.
 * That message is given to us in the incoming() method.  This acts as the "input stream" from
 * the client.
 */
public class ServletConnectionHandler implements IConnectionHandler {
    private String _id;
    private IConnectionListener _connectionListener;
    private String _host;
    protected LinkedList _queue;
    private TunnelServlet _tunnelServlet;

    public ServletConnectionHandler(TunnelServlet tunnelServlet, HttpServletRequest request, String id) {
        _tunnelServlet = tunnelServlet;
        _id = id;
        _host = request.getRemoteAddr();
        _queue = new LinkedList();
        queueMessage(id);
    }

    public String getId() {
        return _id;
    }

    // called by ChatClient
    public String getHost() {
        return _host;
    }

    // called by ChatClient
    public void setListener(IConnectionListener listener) {
        _connectionListener = listener;
    }

    // called by TunnelServlet; delegated to ChatClient
    public void incoming(String s) {
        _connectionListener.incomingMessage(s);
    }

    void block() {
        try {
            while (_queue.size() == 0) {
                Thread.sleep(25);
            }
        }
        catch (InterruptedException e) {
            return;
        }
    }

    protected void flush(PrintWriter out) {
        synchronized (_queue) {
            while (_queue.size() > 0) {
                String message = (String)_queue.removeFirst();
                out.println(message);
            }
        }
    }

    public void flushNewMessagesOrBlock(HttpServletRequest request,
                                        HttpServletResponse response)
        throws ServletException, IOException
    {
        block();
        PrintWriter out = response.getWriter();
        flush(out);
        out.flush();
        out.close();
    }
  
    /**
     * Queue a message headed outbound
     */
    public void queueMessage(String message) {
        synchronized (_queue) {
            _queue.add(message);
        }
    }

    public void sendImmediately(String message) {
        throw new UnsupportedOperationException("ServletConnectionHandler cannot send a message immeditately");
    }


    public void shutdown(boolean notify) {
        if (notify) {
            _connectionListener.socketClosed();
        }
        _tunnelServlet.remove(this);
    }
}
