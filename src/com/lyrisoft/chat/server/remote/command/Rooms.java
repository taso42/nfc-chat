/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.chat.server.remote.command;

import com.lyrisoft.chat.ICommands;
import com.lyrisoft.chat.server.remote.ChatClient;
import com.lyrisoft.chat.server.remote.ChatServer;

public class Rooms extends CommandBase {
    public boolean process(ChatClient client, String[] args) {
        ChatServer server = client.getServer();
        String[] roomNames = server.getRoomNames();
        for (int i=0; i < roomNames.length; i++) {
            String name = roomNames[i];
//            RoomServer room = server.getRoom(name);
//            int count = room.getUserCount();
            int count = server.getUserCountInRoom(name);
            roomNames[i] = name + ICommands.DELIMITER + count;
        }
        client.roomList(roomNames);
        return false;
    }

    // NOTE: when processing distributed style, the message contains information 
    // (not a request for a user list)
    public void processDistributed(String client, String origin, String[] args, ChatServer server) {
        /**
         * Ok, this is a strange case.  Currently, according to the protocol, a roomlist is always
         * followed by messages that describe the state of each room (in the form of /join commands).
         *
         * Those join commands are sufficient to populate the local state table, since a room
         * can only exist if it's non-empty
         *
         * Therefore, there is no implementation here.  We just return.  The UsersInRoom processor
         * will end up getting invoked, and we'll get the list of rooms that way.
         */
    }
}


