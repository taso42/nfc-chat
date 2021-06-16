/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.chat.server.remote.command;

import com.lyrisoft.chat.server.remote.ChatClient;
import com.lyrisoft.chat.server.remote.ChatServer;

public class IgnoreList extends CommandBase {
    public boolean process(ChatClient client, String[] args) {
        if (args.length != 1) {
            client.generalError(getUsage(args[0]));
        } else {
            client.getServer().ignoreList(client);
        }
        return false;
    }

    public void processDistributed(String client, String origin, String[] args, ChatServer server) {
    }
}
