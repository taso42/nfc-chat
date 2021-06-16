/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.chat.server.remote.command;

import com.lyrisoft.chat.server.remote.ChatClient;
import com.lyrisoft.chat.server.remote.ChatServer;
import com.lyrisoft.chat.server.remote.RoomServer;

public class EmoteRoom extends CommandBase {
    public boolean process(ChatClient client, String[] args) {
        if (args.length < 3) {
            client.generalError(getUsage(args[0]));
            return false;
        } else {
            RoomServer room = client.getServer().getRoom(args[1]);
            if (room == null) {
                //  client.generalError("No such room: " + args[1]);
                return true;
            } else {
                room.emote(client, args[2]);
                return true;
            }
        }
    }

    public void processDistributed(String client, String origin, String[] args, ChatServer server) {
        RoomServer room = server.getRoom(args[1]);
        if (room != null) {
            room.emote(client, args[2]);
        }
    }
}
