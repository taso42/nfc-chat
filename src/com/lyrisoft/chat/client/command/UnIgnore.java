/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 *
 * Tasso Mulzer (tasso@cs.tu-berlin.de)
 */
package com.lyrisoft.chat.client.command;

import com.lyrisoft.chat.client.Client;

/**
 * Unignore another user
 */
public class UnIgnore extends UserCommandProcessor {
    public void process(String input, String arg, Client client) throws NotEnoughArgumentsException {
        String[] args = decompose(input, 2);
        client.getServerInterface().unignore(args[1]);
    }
}
