/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.chat.server.remote.command;

import com.lyrisoft.chat.Constants;
import com.lyrisoft.chat.ICommands;
import com.lyrisoft.chat.server.remote.ChatClient;
import com.lyrisoft.chat.server.remote.ChatServer;

public class Ping extends CommandBase implements Constants, ICommands {
    public boolean process(ChatClient client, String[] args) {
        if (args.length < 3) {
            client.generalError(getUsage(args[0]));
            return false;
        } else {
            if (args[1].equals(SERVER_NAME)) {
                client.pong(SERVER_NAME, args[2]);
                return false;
            } else {
                ChatClient otherClient = client.getServer().getClient(args[1]);
                if (otherClient == null) {
                    if (!client.getServer().userExists(args[1])) {
                        client.error(NO_SUCH_USER, args[1]);
                        return false;
                    } 
                    return true;
                } else {
                    otherClient.ping(client.getUserId(), args[2]);
                    return false;
                }
            }
        }
    }

    public void processDistributed(String client, String origin, String[] args, ChatServer server) {
        if (args.length < 3) {
            // this message does not obey protocol.  oh well, just return
            System.err.println("args < 3");
            return;
        }

        if (args[1].equalsIgnoreCase(server.getName())) {
            ChatServer.log("Ping from " + origin);
            // ping from a server 
            server.sendServerPong(origin, args[2]);
        } else {
            // ping from a user
            ChatClient otherClient = server.getClient(args[1]);
            if (otherClient != null) {
                otherClient.ping(client, args[2]);
            }
        }
    }
}
