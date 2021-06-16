/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.chat.server.remote.command;

import com.lyrisoft.chat.server.remote.ChatClient;
import com.lyrisoft.chat.server.remote.ChatServer;

public class Users extends CommandBase {
    public boolean process(ChatClient client, String[] args) {
        String[] users = client.getServer().getUserNames();
        client.globalUserList(users);
        return false;
    }

    // NOTE: when processing distributed style, the message contains information 
    // (not a request for a user list)
    public void processDistributed(String client, String origin, String[] args, ChatServer server) { 
        if (args.length < 2) {
            // no users; just add the server
            server.serverSignOn(origin);
        } else {
            String[] userlist = new String[args.length - 1];
            System.arraycopy(args, 1, userlist, 0, userlist.length);
            server.setRemoteUserList(origin, userlist);
        }
    }
}
