/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.chat.server.local.command;

import com.lyrisoft.chat.client.IChatClient;

public class UserDiff implements ICommandProcessorLocal {
    public void process(IChatClient client, String[] args) {
        if (args.length < 3) {
            client.generalError("Protocol error: " + args[0]);
        } else {
            char op = args[1].charAt(0);
            switch (op) {
            case '+':
                client.userSignOn(args[2]);
                break;
            case '-':
                client.userSignOff(args[2]);
                break;
            default:
                client.generalError("Protocol error: " + args[0] + " unknown op " + args[1]); 
                
            }
            
        }
    }
}
