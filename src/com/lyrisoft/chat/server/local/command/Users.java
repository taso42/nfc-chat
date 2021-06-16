/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.chat.server.local.command;

import com.lyrisoft.chat.client.IChatClient;

public class Users implements ICommandProcessorLocal {
    public void process(IChatClient client, String[] args) {
        String[] users = new String[args.length-1];
        System.arraycopy(args, 1, users, 0, users.length);
        client.globalUserList(users);
    }
}
