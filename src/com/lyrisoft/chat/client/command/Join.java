/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.chat.client.command;

import com.lyrisoft.chat.client.Client;

/**
 * Handle a join message
 */
public class Join extends UserCommandProcessor {
    /**
     * @see com.lyrisoft.chat.server.local.ChatServerLocal#joinRoom
     */
    public void process(String input, String arg, Client client) throws NotEnoughArgumentsException {
        if (arg != null) {
            try {
                String[] args = decompose(input, 3);
                client.getServerInterface().joinRoom(args[1], args[2]);
            } catch (NotEnoughArgumentsException e) {
                String[] args = decompose(input, 2);
                client.getServerInterface().joinRoom(args[1], null);
            }
        } else {
            (new Throwable()).printStackTrace();
            System.err.println("FIXME: what should I do here?");
        }
    }
}
