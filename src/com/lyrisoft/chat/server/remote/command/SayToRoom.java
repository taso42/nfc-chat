/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.chat.server.remote.command;

import com.lyrisoft.chat.Translator;
import com.lyrisoft.chat.server.remote.ChatClient;
import com.lyrisoft.chat.server.remote.ChatServer;
import com.lyrisoft.chat.server.remote.RoomServer;

public class SayToRoom extends CommandBase {
    public boolean process(ChatClient client, String[] args) {
        if (args.length < 3) {
            client.generalError(getUsage(args[0]));
            return false;
        } else {
            String roomName = args[1];
            String message = args[2];
            RoomServer room = client.getServer().getRoom(roomName);
            if (room != null) {
                room.say(client, message);
            } else {
                if (!client.getServer().roomExists(roomName)) {
                    client.generalError(Translator.getMessage("noroom", roomName));
                    return false;
                }
            }
        }
        return true;
    }

    public void processDistributed(String client, String origin, String[] args, ChatServer server) {
        String roomName = args[1];
        String message = args[2];
        RoomServer room = server.getRoom(roomName);
        if (room != null) {
            room.say(client, message);
        } 
    }
}
