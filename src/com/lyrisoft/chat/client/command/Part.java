/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.chat.client.command;

import com.lyrisoft.chat.client.Client;

/**
 * Handle a part message
 */
public class Part extends UserCommandProcessor {
    /**
     * @see com.lyrisoft.chat.server.local.ChatServerLocal#partRoom
     */
    public void process(String input, String arg, Client client) throws NotEnoughArgumentsException {
//        String[] args = decompose(input, 2);
        client.getServerInterface().partRoom(arg);
    }
}
