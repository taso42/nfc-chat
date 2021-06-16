/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.chat.server.remote.command;

import com.lyrisoft.chat.ICommands;
import com.lyrisoft.chat.server.remote.ChatClient;
import com.lyrisoft.chat.server.remote.ChatServer;

public class Shutdown extends CommandBase implements ICommands {
    public boolean process(ChatClient client, String[] args) {
        if ("127.0.0.1".equals(client.getHost())) {
            System.exit(1);
        }
        return false;
    }

    public void processDistributed(String client, String origin, String[] args, ChatServer server) { 
    }
}
