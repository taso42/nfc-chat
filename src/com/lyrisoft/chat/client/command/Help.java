/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.chat.client.command;

import com.lyrisoft.chat.client.Client;

/**
 * Handle a help request
 */
public class Help extends UserCommandProcessor {
    /**
     * @see com.lyrisoft.chat.server.local.ChatServerLocal#help
     */
    public void process(String input, String arg, Client client) {
        try {
            String[] args = decompose(input, 2);
            String command = args[1];
            client.getServerInterface().help(command);
        }
        catch (NotEnoughArgumentsException e) {
            client.getServerInterface().help(null);
        }
    }
}
