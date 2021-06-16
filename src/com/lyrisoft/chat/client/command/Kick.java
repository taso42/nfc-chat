/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.chat.client.command;

import com.lyrisoft.chat.client.Client;

public class Kick extends UserCommandProcessor {
    public void process(String input, String arg, Client client) throws NotEnoughArgumentsException {
        try {
            String[] args = decompose(input, 3);
            client.getServerInterface().kick(args[1], args[2]);
        }
        catch (NotEnoughArgumentsException e) {
            String[] args = decompose(input, 2); // this might throw again.. we let that one go up the stack
            client.getServerInterface().kick(args[1], arg);
        }
    }
}
