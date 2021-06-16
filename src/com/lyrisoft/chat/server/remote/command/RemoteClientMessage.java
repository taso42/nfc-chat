/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.chat.server.remote.command;

import com.lyrisoft.chat.ICommands;
import com.lyrisoft.chat.server.remote.ChatClient;
import com.lyrisoft.chat.server.remote.ChatServer;

public class RemoteClientMessage extends CommandBase implements ICommands {
    public boolean process(ChatClient client, String[] args) {
        return false;
    }

    public void processDistributed(String client, String origin, String[] args, ChatServer server) {
        ChatClient rcpt = server.getClient(args[1]);
        if (rcpt != null) {
            rcpt.generalMessage(args[2]);
        }
    }
}

