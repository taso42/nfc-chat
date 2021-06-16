/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.chat.server.remote;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.util.HashMap;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.lyrisoft.util.io.ResourceLoader;

/**
 * A Servlet wrapper to the ChatServer.  
 */
public class TunnelServlet extends HttpServlet implements Runnable {
    protected ChatServer _chatServer;
    protected Thread _runner;
    protected HashMap _connectionHandlers;

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        
        ResourceLoader.setServletContext(getServletContext());

        _connectionHandlers = new HashMap();
        String chatServerClass = config.getInitParameter("chatServerClass");
        if (chatServerClass == null) {
            chatServerClass = "com.lyrisoft.chat.server.remote.ChatServer";
        } 
        
        try {
            String conf = config.getInitParameter("config");
            if (conf == null) {
                Class chatServer = Class.forName(chatServerClass);
                Constructor constructor = chatServer.getConstructor(new Class[]{  ServletContext.class });
                _chatServer = (ChatServer)constructor.newInstance(new Object[]{ getServletContext() });
            } else {
                Class chatServer = Class.forName(chatServerClass);
                Constructor constructor = chatServer.getConstructor(new Class[]{ String.class, ServletContext.class });
                _chatServer = (ChatServer)constructor.newInstance(new Object[]{ conf, getServletContext() });
            } 

            config.getServletContext().setAttribute("chatServer", _chatServer);

            ChatServer.log("ChatServer started by the Servlet");
            _runner = new Thread(this);
            _runner.start();
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new ServletException(e.toString());
        }
    }

    public void pleaseStop() {
        _chatServer.pleaseStop();
    }

    public void run() {
        _chatServer.acceptConnections();
    }

    void remove(ServletConnectionHandler handler) {
        _connectionHandlers.remove(handler.getId());
    }

    /**
     * doGet(): invoked by the client to read waiting messages
     *
     * If called without params:
     * <li>we assume this is a new connection
     * <li>create a new ServletConnectionHandler with a unique ID
     * <li>create a new ChatClient with the ServletConnectionHandler as its handler
     * <p>
     *
     * if called with a param named ID
     * <li>assume this is an existing connection
     * <li>pull the ServletConnectionHandler for this ID out of the hashmap
     * <li>tell the ServletConnectionHandler to flush any messages that are waiting
     * (or block until there is at least one message and send it)
     *
     * It is the client's responsibility to make a new HTTP request after this one is 
     * finished.
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response) 
        throws ServletException, IOException
    {
        response.setContentType("text/plain");
        response.setHeader("Pragma", "No-cache");
        response.setHeader("Cache-Control", "no-cache");
        
        String id = request.getParameter("id");
        if ( id == null ) {
            // new connection
            id = String.valueOf(System.currentTimeMillis());
            ServletConnectionHandler handler = createServletConnectionHandler(request, id);
            _connectionHandlers.put(id, handler);
            ChatClient c = createChatClient(handler);
            _chatServer._vulture.addClient(c);

            handler.flushNewMessagesOrBlock(request, response);
        } else {
            // existing connection
            ServletConnectionHandler handler = (ServletConnectionHandler)_connectionHandlers.get(id);
            if (handler != null) {
                handler.flushNewMessagesOrBlock(request, response);
            } else {
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            }
        }
    }

    public ServletConnectionHandler createServletConnectionHandler(HttpServletRequest request, 
                                                                   String id) 
    {
        return new ServletConnectionHandler(this, request, id);
    }

    public ChatClient createChatClient(ServletConnectionHandler handler) {
        return new ChatClient(_chatServer, handler);
    }

    /**
     * doPost(): invoked by the client to send a new message
     * <p>
     * required params: 
     * <li>id: the id which the servlet assigned returned during the first invocation
     * of doGet()
     * <li>arg: the message to send
     */
    public void doPost(HttpServletRequest request, HttpServletResponse response) 
        throws ServletException, IOException
    {
        response.setContentType("text/plain"); 
        PrintWriter out = response.getWriter();
        String id = request.getParameter("id");
        String arg = request.getParameter("arg");
        if (id != null && arg != null) {
            ServletConnectionHandler handler = (ServletConnectionHandler)_connectionHandlers.get(id);
            if (handler != null) {
                handler.incoming(arg);
                out.println("ok");
            } else {
                out.println("You do not exist.  Go away.");
            }
            out.close();
        } else {
            out.println("bad request.");
        }

        out.close();
    }

    public void destroy() {
        if (_runner == null) {
            return;
        }

        try {
            pleaseStop();
            if (_runner.isAlive()) {
                System.err.println("Forcefully killing the accept thread.");
                _runner.stop();
            }
        }
        catch (Throwable t) {
            System.err.println("Exception during TunnelServlet.destroy");
            t.printStackTrace();
        }
    }
}




