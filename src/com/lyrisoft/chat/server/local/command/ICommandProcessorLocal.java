/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.chat.server.local.command;

import com.lyrisoft.chat.client.IChatClient;

/**
 * Process a message that came from the server.  All of the other classes in this package
 * implement this interface
 */
public interface ICommandProcessorLocal {
    /**
     * @param client the IChatClient instance on the local side
     * @param args the server message broken down into arguments
     */
    public void process(IChatClient client, String[] args);
}
