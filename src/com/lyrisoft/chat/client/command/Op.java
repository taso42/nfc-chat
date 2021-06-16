/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.chat.client.command;

import com.lyrisoft.chat.client.Client;

public class Op extends UserCommandProcessor {
    public void process(String input, String arg, Client client) throws NotEnoughArgumentsException {
        try {
            String[] args = decompose(input, 4);
            if (args[2].equals("+")) {
                client.getServerInterface().op(args[3], args[1]);
            } else {
                client.getServerInterface().deop(args[3], args[1]);
            }
        }
        catch (NotEnoughArgumentsException e) {
            String[] args = decompose(input, 3); // this might throw again.. we let that one go up the stack
            if (args[1].equals("+")) {
                client.getServerInterface().op(args[2], arg);
            } else {
                client.getServerInterface().deop(args[2], arg);
            }
        }
    }
}
