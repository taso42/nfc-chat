/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission 
 *
 * Tasso Mulzer (tasso@cs.tu-berlin.de)
 */
package com.lyrisoft.chat.client.command;

import com.lyrisoft.chat.client.Client;

/**
 * Ignore another user
 */
public class Ignore extends UserCommandProcessor {
    public void process(String input, String arg, Client client) throws NotEnoughArgumentsException {
        try {
            String[] args = decompose(input, 3);
            client.getServerInterface().ignore(args[1], args[2]);
        }
        catch (NotEnoughArgumentsException e) {
            String[] args = decompose(input, 2); // this might throw again.. we let that one go up the stack
            client.getServerInterface().ignore(args[1], "");
        }
    }
}
