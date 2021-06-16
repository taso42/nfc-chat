/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.chat.server.remote.command;

import com.lyrisoft.chat.Translator;
import com.lyrisoft.chat.server.remote.ChatClient;
import com.lyrisoft.chat.server.remote.ChatServer;

public class UsersInRoom extends CommandBase {
    public boolean process(ChatClient client, String[] args) {
        if (args.length < 2) {
            client.generalError(getUsage(args[0]));
        } else {
            String roomName = args[1];
            String[] users = client.getServer().getUsersInRoom(roomName);
            if (users.length == 0) {
                client.generalError(Translator.getMessage("noroom", roomName));
            } else {
                client.roomUserList(roomName, users);
            }
        }
        return false;
    }

    // NOTE: when processing distributed style, the message contains information 
    // (not a request for a room list)
    public void processDistributed(String client, String origin, String[] args, ChatServer server) { 
        if (args.length < 3) {
            // this message does not obey protocol.  oh well, just return
            return;
        }
        
        String roomName = args[1];
        for (int i=2; i < args.length; i++) {
            String user = args[i];
            server.remoteJoinRoom(origin, user, roomName, null);
        }
    }
}
