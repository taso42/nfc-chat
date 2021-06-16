/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.chat;


/**
 * Interface that ConnectionHandlers must implement
 */ 
public interface IConnectionHandler {
    /**
     * Queue a message headed outbound.
     */
    public void queueMessage(String message);

    /**
     * Send a message immediately.
     */
    public void sendImmediately(String message);

    /**
     * Shutdown this connection listener.  The notify parameter
     * indicates whether or not the client (ConnectionListener) should
     * be notified of the shutdown.  Basically, notify should only be
     * false if the client itself called us.
     * @param notify to notify the ConnectionListener
     */
    public void shutdown(boolean notify);
    
    /**
     * Set the connection listener for this connection handler
     */
    public void setListener(IConnectionListener listener);
}
