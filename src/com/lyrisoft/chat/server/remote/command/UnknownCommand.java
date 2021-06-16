/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.chat.server.remote.command;

import com.lyrisoft.chat.Translator;
import com.lyrisoft.chat.server.remote.ChatClient;
import com.lyrisoft.chat.server.remote.ChatServer;

public class UnknownCommand extends CommandBase {
    public boolean process(ChatClient client, String[] args) {
        client.generalError(Translator.getMessage("unknown_command"));
        return false;
    }

    public void processDistributed(String client, String origin, String[] args, ChatServer server) { 
    }
}
