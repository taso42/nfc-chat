/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.chat.server.remote.command;

import com.lyrisoft.chat.ICommands;
import com.lyrisoft.chat.server.remote.ChatClient;
import com.lyrisoft.chat.server.remote.ChatServer;

public class SayToUser extends CommandBase implements ICommands {
    public boolean process(ChatClient client, String[] args) {
        if (args.length < 3) {
            client.generalError(getUsage(args[0]));
            return false;
        } else {
            String userName = args[1];
            String message = args[2];
            ChatClient remote = client.getServer().getClient(userName);
            if (remote != null) {
                remote.messageFromUserPrivate(client.getUserId(), message);
                return false;
            } else {
                if (!client.getServer().userExists(args[1])) {
                    client.error(NO_SUCH_USER, args[1]);
                    return false;
                }
                return true;
            }
        }
    }

    public void processDistributed(String client, String origin, String[] args, ChatServer server) {
        ChatClient rcpt = server.getClient(args[1]);
        if (rcpt != null) {
            rcpt.messageFromUserPrivate(client, args[2]);
        }
    }
}
