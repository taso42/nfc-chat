/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.chat.server.remote.command;

import com.lyrisoft.chat.ICommands;
import com.lyrisoft.chat.server.remote.ChatClient;
import com.lyrisoft.chat.server.remote.ChatServer;

public class EmoteUser extends CommandBase implements ICommands {
    public boolean process(ChatClient client, String[] args) {
        if (args.length < 3) {
            client.generalError(getUsage(args[0]));
            return false;
        } else {
            ChatClient rcpt = client.getServer().getClient(args[1]);
            if (rcpt != null) {
                rcpt.emoteFromUserPrivate(client.getUserId(), args[2]);
                return false;
            } else {
                return true;
//                client.error(NO_SUCH_USER, args[1]);
            }
        }
    }

    public void processDistributed(String client, String origin, String[] args, ChatServer server) {
        ChatClient rcpt = server.getClient(args[1]);
        if (rcpt != null) {
            rcpt.emoteFromUserPrivate(client, args[2]);
        }
    }
}
