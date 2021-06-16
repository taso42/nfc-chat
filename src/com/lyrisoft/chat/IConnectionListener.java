/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.chat;

/**
 * Interface the implement when you want to get raw messages from the socket connection
 */
public interface IConnectionListener {
    /**
     * Notification that a new line was read from the socket
     */
    public void incomingMessage(String message);

    /**
     * Notification that the socket got closed.
     */
    public void socketClosed();
}