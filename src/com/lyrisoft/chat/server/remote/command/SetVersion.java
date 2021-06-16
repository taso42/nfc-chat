/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.chat.server.remote.command;

import com.lyrisoft.chat.server.remote.ChatClient;
import com.lyrisoft.chat.server.remote.ChatServer;

public class SetVersion extends CommandBase {
    public boolean process(ChatClient client, String[] args) {
        if (args.length < 2) {
            client.generalError(getUsage(args[0]));
        } else {
            client.setClientVersion(args[1]);
            ChatServer.log(client.getUserId() + " reports client version \"" + args[1] + "\"");
        }
        return false;
    }

    public void processDistributed(String client, String origin, String[] args, ChatServer server) { 
    }
}

