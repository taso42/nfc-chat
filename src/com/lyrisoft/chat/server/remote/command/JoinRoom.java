/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.chat.server.remote.command;

import com.lyrisoft.chat.server.remote.ChatClient;
import com.lyrisoft.chat.server.remote.ChatServer;
import com.lyrisoft.chat.server.remote.RoomJoinException;

public class JoinRoom extends CommandBase {
    public boolean process(ChatClient client, String[] args) {
        String room = null;
        try {
            if (args.length < 2) {
                client.generalError(getUsage(args[0]));
            } else {
            	String password = (args.length < 3) ? null : args[2];
                client.getServer().joinRoom(client, args[1], password);
                return true;
            }
        }
        catch (RoomJoinException e) {
            client.roomJoinError(e.getMessage(), args[1]);
        }
        return false;
    }

    public void processDistributed(String client, String origin, String[] args, ChatServer server) {
        if (args.length > 2) {
            server.remoteJoinRoom(origin, client, args[1], args[2]);
        } else {
            server.remoteJoinRoom(origin, client, args[1], null);
        }
    }
}
