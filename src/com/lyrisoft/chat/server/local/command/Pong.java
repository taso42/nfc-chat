/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.chat.server.local.command;

import com.lyrisoft.chat.client.IChatClient;

// Pong.
public class Pong implements ICommandProcessorLocal {
    public void process(IChatClient client, String[] args) {
        if (args.length < 3) {
            client.generalError("Protocol error: " + args[0]);
        } else {
            client.pong(args[1], args[2]);
        }
    }
}
