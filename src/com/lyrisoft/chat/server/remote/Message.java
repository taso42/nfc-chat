/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.chat.server.remote;

public class Message {
    protected ConnectionHandler _handler;
    protected String _message;

    public Message(ConnectionHandler handler, String message) {
        _handler = handler;
        _message = message;
    }

    public void send() {
        _handler.sendImmediately(_message);
    }
}
