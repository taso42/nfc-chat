/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.chat.server.remote.command;

import com.lyrisoft.chat.Translator;
import com.lyrisoft.chat.server.remote.ChatClient;
import com.lyrisoft.chat.server.remote.ChatServer;

public class Invite extends CommandBase {
    public boolean process(ChatClient client, String[] args) {
        if (args.length < 3) {
            client.generalError(getUsage(args[0]));
        } else {
            client.getServer().getRoom(args[0]).invite(client, args[1]);
        } 
        client.generalMessage(Translator.getMessage("invitation.add", args[1], args[0]));
        return false;
    }

    public void processDistributed(String client, String origin, String[] args, ChatServer server) {
    }
}

