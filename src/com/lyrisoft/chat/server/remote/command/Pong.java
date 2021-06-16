/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.chat.server.remote.command;

import com.lyrisoft.chat.Constants;
import com.lyrisoft.chat.ICommands;
import com.lyrisoft.chat.server.remote.ChatClient;
import com.lyrisoft.chat.server.remote.ChatServer;

public class Pong extends CommandBase implements Constants, ICommands {
    public boolean process(ChatClient client, String[] args) {
        if (args.length < 3) {
            client.generalError(getUsage(args[0]));
            return false;
        } else {
            if (args[1].equals(Constants.SERVER_NAME)) {
                client.setLastServerPong(Long.parseLong(args[2]));
                return false;
            } else {
                ChatClient otherClient = client.getServer().getClient(args[1]);
                if (otherClient == null) {
                    if (!client.getServer().userExists(args[1])) {
                        client.error(NO_SUCH_USER, args[1]);
                        return false;
                    } else {
                        return true;
                    }
                } else {
                    otherClient.pong(client.getUserId(), args[2]);
                    return false;
                }
            }
        }
    }

    public void processDistributed(String client, String origin, String[] args, ChatServer server) {
        if (args.length < 3) {
            // this message does not obey protocol.  oh well, just return
            return;
        }

        if (args[1].equals(server.getName())) {
            server.handlePong(origin, args[2]);
        } else {
            ChatClient otherClient = server.getClient(args[1]);
            if (otherClient != null) {
                otherClient.pong(client, args[2]);
            }
        }
    }
}
